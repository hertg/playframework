/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import play.ApplicationLoader.Context;
import play.Environment;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;

public class NamedDatabaseTest {

  @Rule public ExpectedException exception = ExpectedException.none();

  @Test
  public void bindDatabasesByName() {
    Map<String, Object> config =
        ImmutableMap.of(
            "db.default.driver", "org.h2.Driver",
            "db.default.url", "jdbc:h2:mem:default",
            "db.other.driver", "org.h2.Driver",
            "db.other.url", "jdbc:h2:mem:other");
    Injector injector = createInjector(config);
    assertThat(injector.getInstance(DefaultComponent.class).db.getUrl())
        .isEqualTo("jdbc:h2:mem:default");
    assertThat(injector.getInstance(NamedDefaultComponent.class).db.getUrl())
        .isEqualTo("jdbc:h2:mem:default");
    assertThat(injector.getInstance(NamedOtherComponent.class).db.getUrl())
        .isEqualTo("jdbc:h2:mem:other");
  }

  @Test
  public void notBindDefaultDatabaseWithoutConfiguration() {
    Map<String, Object> config =
        ImmutableMap.of(
            "db.other.driver", "org.h2.Driver",
            "db.other.url", "jdbc:h2:mem:other");
    Injector injector = createInjector(config);
    assertThat(injector.getInstance(NamedOtherComponent.class).db.getUrl())
        .isEqualTo("jdbc:h2:mem:other");
    exception.expect(com.google.inject.ConfigurationException.class);
    injector.getInstance(DefaultComponent.class);
  }

  @Test
  public void notBindNamedDefaultDatabaseWithoutConfiguration() {
    Map<String, Object> config =
        ImmutableMap.of(
            "db.other.driver", "org.h2.Driver",
            "db.other.url", "jdbc:h2:mem:other");
    Injector injector = createInjector(config);
    assertThat(injector.getInstance(NamedOtherComponent.class).db.getUrl())
        .isEqualTo("jdbc:h2:mem:other");
    exception.expect(com.google.inject.ConfigurationException.class);
    injector.getInstance(NamedDefaultComponent.class);
  }

  @Test
  public void allowDefaultDatabaseNameToBeConfigured() {
    Map<String, Object> config =
        ImmutableMap.of(
            "play.db.default", "other",
            "db.other.driver", "org.h2.Driver",
            "db.other.url", "jdbc:h2:mem:other");
    Injector injector = createInjector(config);
    assertThat(injector.getInstance(DefaultComponent.class).db.getUrl())
        .isEqualTo("jdbc:h2:mem:other");
    assertThat(injector.getInstance(NamedOtherComponent.class).db.getUrl())
        .isEqualTo("jdbc:h2:mem:other");
    exception.expect(com.google.inject.ConfigurationException.class);
    injector.getInstance(NamedDefaultComponent.class);
  }

  @Test
  public void allowDbConfigKeyToBeConfigured() {
    Map<String, Object> config =
        ImmutableMap.of(
            "play.db.config", "databases",
            "databases.default.driver", "org.h2.Driver",
            "databases.default.url", "jdbc:h2:mem:default");
    Injector injector = createInjector(config);
    assertThat(injector.getInstance(DefaultComponent.class).db.getUrl())
        .isEqualTo("jdbc:h2:mem:default");
    assertThat(injector.getInstance(NamedDefaultComponent.class).db.getUrl())
        .isEqualTo("jdbc:h2:mem:default");
  }

  private Injector createInjector(Map<String, Object> config) {
    GuiceApplicationBuilder builder =
        new GuiceApplicationLoader().builder(new Context(Environment.simple(), config));
    return Guice.createInjector(builder.applicationModule());
  }

  public static class DefaultComponent {
    @Inject Database db;
  }

  public static class NamedDefaultComponent {
    @Inject
    @NamedDatabase("default")
    Database db;
  }

  public static class NamedOtherComponent {
    @Inject
    @NamedDatabase("other")
    Database db;
  }
}
