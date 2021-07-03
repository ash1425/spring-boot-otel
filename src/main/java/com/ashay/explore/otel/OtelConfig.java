package com.ashay.explore.otel;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.spring.autoconfigure.SamplerProperties;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.extension.aws.trace.AwsXrayIdGenerator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class OtelConfig {

    @Bean
    public OpenTelemetry openTelemetry(
            SamplerProperties samplerProperties,
            ObjectProvider<List<SpanExporter>> spanExportersProvider) {
        SdkTracerProviderBuilder tracerProviderBuilder = SdkTracerProvider.builder();

        spanExportersProvider.getIfAvailable(Collections::emptyList).stream()
                .map(SimpleSpanProcessor::create)
                .forEach(tracerProviderBuilder::addSpanProcessor);

        SdkTracerProvider tracerProvider =
                tracerProviderBuilder
                        .setIdGenerator(AwsXrayIdGenerator.getInstance())
                        .setSampler(Sampler.traceIdRatioBased(samplerProperties.getProbability()))
                        .build();
        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .buildAndRegisterGlobal();
    }

}
