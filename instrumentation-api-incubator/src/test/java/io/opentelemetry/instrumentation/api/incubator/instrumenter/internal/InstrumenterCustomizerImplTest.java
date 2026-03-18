/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.incubator.instrumenter.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.instrumentation.api.incubator.instrumenter.InstrumenterCustomizer.InstrumentationType;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.ContextCustomizer;
import io.opentelemetry.instrumentation.api.instrumenter.OperationMetrics;
import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.SpanStatusExtractor;
import io.opentelemetry.instrumentation.api.internal.InternalInstrumenterCustomizer;
import io.opentelemetry.instrumentation.api.internal.SpanKey;
import java.util.Collections;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InstrumenterCustomizerImplTest {

  @Mock InternalInstrumenterCustomizer<Object, Object> internalCustomizer;

  @InjectMocks InstrumenterCustomizerImpl underTest;

  @Test
  void getInstrumentationName() {
    when(internalCustomizer.getInstrumentationName()).thenReturn("test");
    assertThat(underTest.getInstrumentationName()).isEqualTo("test");
  }

  @Test
  void hasType() {
    when(internalCustomizer.hasType(SpanKey.HTTP_CLIENT)).thenReturn(true);
    when(internalCustomizer.hasType(SpanKey.HTTP_SERVER)).thenReturn(false);

    assertThat(underTest.hasType(InstrumentationType.HTTP_CLIENT)).isTrue();
    assertThat(underTest.hasType(InstrumentationType.HTTP_SERVER)).isFalse();
  }

  @Test
  void hasTypeUnexpected() {
    assertThatThrownBy(() -> underTest.hasType(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("unexpected instrumentation type: null");
  }

  @Test
  void addAttributesExtractor() {
    AttributesExtractor<Object, Object> extractor = AttributesExtractor.constant(null, null);
    underTest.addAttributesExtractor(extractor);
    verify(internalCustomizer).addAttributesExtractor(extractor);
  }

  @Test
  void addAttributesExtractors() {
    AttributesExtractor<Object, Object> extractor = AttributesExtractor.constant(null, null);
    underTest.addAttributesExtractors(Collections.singletonList(extractor));
    verify(internalCustomizer).addAttributesExtractors(any());
  }

  @Test
  void addOperationMetrics() {
    OperationMetrics metrics = (meter) -> null;
    underTest.addOperationMetrics(metrics);
    verify(internalCustomizer).addOperationMetrics(metrics);
  }

  @Test
  void addContextCustomizer() {
    ContextCustomizer<Object> customizer = (context, request, startAttributes) -> context;
    underTest.addContextCustomizer(customizer);
    verify(internalCustomizer).addContextCustomizer(customizer);
  }

  @Test
  void setSpanNameExtractorCustomizer() {
    UnaryOperator<SpanNameExtractor<?>> customizer = UnaryOperator.identity();
    underTest.setSpanNameExtractorCustomizer(customizer);
    verify(internalCustomizer).setSpanNameExtractorCustomizer(any());
  }

  @Test
  void setSpanStatusExtractorCustomizer() {
    UnaryOperator<SpanStatusExtractor<?, ?>> customizer = UnaryOperator.identity();
    underTest.setSpanStatusExtractorCustomizer(customizer);
    verify(internalCustomizer).setSpanStatusExtractorCustomizer(any());
  }
}
