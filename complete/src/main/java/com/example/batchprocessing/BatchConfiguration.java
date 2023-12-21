package com.example.batchprocessing;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
public class BatchConfiguration {

	// tag::readerwriterprocessor[]
	// 各メソッドが必要な型を返しているのが大事っぽい。
	@Bean
	public FlatFileItemReader<Person> reader() {
		System.out.println("start reader");
		return new FlatFileItemReaderBuilder<Person>()
			.name("personItemReader")
			.resource(new ClassPathResource("sample-data.csv"))// ここでリソース指定
			.delimited()
			.names("firstName", "lastName")
			.targetType(Person.class)
			.build();
	}

	@Bean
	public PersonItemProcessor processor() {
		System.out.println("start processor");
		// 処理クラスのインスタンス化。多分ここで処理もしてくれている。ItemProcessorのprocessコマンドを実行してるんじゃないかな
		return new PersonItemProcessor();
	}

	// ここのDataSource型はどこと関係あるんかな？PersonItemProcessorはPerson型返していたけど。
	@Bean
	public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
		System.out.println("start writer");
		return new JdbcBatchItemWriterBuilder<Person>()
			.sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
			.dataSource(dataSource)
			.beanMapped()
			.build();
	}
	// end::readerwriterprocessor[]

	// tag::jobstep[]
	@Bean
	public Job importUserJob(JobRepository jobRepository,Step step1, JobCompletionNotificationListener listener) {
		System.out.println("start importUserJob");
		return new JobBuilder("importUserJob", jobRepository)
			.listener(listener)
			.start(step1)
			.build();
	}

	// 各Stepはreader, processor, writerを含む事ができる
	@Bean
	public Step step1(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
					  FlatFileItemReader<Person> reader, PersonItemProcessor processor, JdbcBatchItemWriter<Person> writer) {
		System.out.println("start step1");
		return new StepBuilder("step1", jobRepository)
			.<Person, Person> chunk(3, transactionManager)
			.reader(reader)
			.processor(processor)
			.writer(writer)
			.build();
	}
	// end::jobstep[]
}
