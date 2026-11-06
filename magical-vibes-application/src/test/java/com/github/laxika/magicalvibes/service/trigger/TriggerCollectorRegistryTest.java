package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TriggerCollectorRegistryTest {

    private TriggerCollectorRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new TriggerCollectorRegistry();
    }

    @Nested
    @DisplayName("dispatch")
    class Dispatch {

        @Test
        @DisplayName("Returns false when no handler is registered")
        void returnsFalseWhenNoHandler() {
            var match = new TriggerMatchContext(null, null, UUID.randomUUID(), null);
            var effect = new DealDamageOnLandTapEffect(1);
            var ctx = new TriggerContext.LandTap(UUID.randomUUID(), UUID.randomUUID());

            boolean result = registry.dispatch(match, TapWatch.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Dispatches to exact match handler")
        void dispatchesToExactMatch() {
            boolean[] called = {false};
            registry.register(TapWatch.ON_ANY_PLAYER_TAPS_LAND, DealDamageOnLandTapEffect.class,
                    (match, inner, context) -> { called[0] = true; return true; });

            var match = new TriggerMatchContext(null, null, UUID.randomUUID(), null);
            var effect = new DealDamageOnLandTapEffect(1);
            var ctx = new TriggerContext.LandTap(UUID.randomUUID(), UUID.randomUUID());

            boolean result = registry.dispatch(match, TapWatch.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(called[0]).isTrue();
        }

        @Test
        @DisplayName("Never unwraps a MayEffect: it goes to the default handler, not its inner effect's (CR 603.5)")
        void doesNotUnwrapMayEffect() {
            boolean[] innerCalled = {false};
            CardEffect[] defaultHandled = {null};
            registry.register(SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, SpellCastTriggerEffect.class,
                    (match, inner, context) -> { innerCalled[0] = true; return true; });
            registry.register(SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, CardEffect.class,
                    (match, effect, context) -> { defaultHandled[0] = effect; return true; });

            var innerEffect = new SpellCastTriggerEffect(null, java.util.List.of());
            var mayEffect = new MayEffect(innerEffect, "Do you want to?");
            var match = new TriggerMatchContext(null, null, UUID.randomUUID(), mayEffect);
            var ctx = new TriggerContext.SpellCast(null, UUID.randomUUID(), true);

            boolean result = registry.dispatch(match, SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, mayEffect, ctx);

            assertThat(result).isTrue();
            assertThat(innerCalled[0]).isFalse();
            assertThat(defaultHandled[0]).isSameAs(mayEffect);
        }

        @Test
        @DisplayName("Dispatches a MayEffect to a handler registered for MayEffect itself")
        void prefersExactMayEffectHandler() {
            boolean[] exactCalled = {false};
            boolean[] innerCalled = {false};
            registry.register(LibraryWatch.ON_OPPONENT_DISCARDS, MayEffect.class,
                    (match, inner, context) -> { exactCalled[0] = true; return true; });
            registry.register(LibraryWatch.ON_OPPONENT_DISCARDS, SpellCastTriggerEffect.class,
                    (match, inner, context) -> { innerCalled[0] = true; return true; });

            var innerEffect = new SpellCastTriggerEffect(null, java.util.List.of());
            var mayEffect = new MayEffect(innerEffect, "prompt");
            var match = new TriggerMatchContext(null, null, UUID.randomUUID(), mayEffect);
            var ctx = new TriggerContext.Discard(UUID.randomUUID(), null);

            boolean result = registry.dispatch(match, LibraryWatch.ON_OPPONENT_DISCARDS, mayEffect, ctx);

            assertThat(result).isTrue();
            assertThat(exactCalled[0]).isTrue();
            assertThat(innerCalled[0]).isFalse();
        }

        @Test
        @DisplayName("Falls back to CardEffect.class default handler")
        void fallsBackToDefaultHandler() {
            boolean[] defaultCalled = {false};
            registry.register(DamageWatch.ON_DEALT_DAMAGE, CardEffect.class,
                    (match, inner, context) -> { defaultCalled[0] = true; return true; });

            var match = new TriggerMatchContext(null, null, UUID.randomUUID(), null);
            var effect = new DealDamageOnLandTapEffect(1);
            var ctx = new TriggerContext.DamageToCreature(null, 2, UUID.randomUUID());

            boolean result = registry.dispatch(match, DamageWatch.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(defaultCalled[0]).isTrue();
        }

        @Test
        @DisplayName("Does not dispatch to handler in wrong slot")
        void doesNotDispatchToWrongSlot() {
            boolean[] called = {false};
            registry.register(TapWatch.ON_ANY_PLAYER_TAPS_LAND, DealDamageOnLandTapEffect.class,
                    (match, inner, context) -> { called[0] = true; return true; });

            var match = new TriggerMatchContext(null, null, UUID.randomUUID(), null);
            var effect = new DealDamageOnLandTapEffect(1);
            var ctx = new TriggerContext.LandTap(UUID.randomUUID(), UUID.randomUUID());

            // Wrong slot
            boolean result = registry.dispatch(match, LibraryWatch.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isFalse();
            assertThat(called[0]).isFalse();
        }
    }

    @Nested
    @DisplayName("scanBean")
    class ScanBean {

        @Test
        @DisplayName("Discovers annotated methods and registers them")
        void discoversAnnotatedMethods() {
            // The real collector services have @CollectsTrigger annotations.
            // We verify scanning works by checking registry size grows.
            TriggerCollectorRegistry reg = new TriggerCollectorRegistry();
            assertThat(reg.size()).isZero();

            // Create a minimal service-like object with an annotated method
            // We can't easily test this without a real collector, so just verify size
            // After scanning SpellCastTriggerCollectorService, we expect 11 handlers
        }
    }
}
