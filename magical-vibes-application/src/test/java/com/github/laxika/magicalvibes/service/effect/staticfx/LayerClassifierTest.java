package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseAllCreatureTypesEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.layer.Layer;
import com.github.laxika.magicalvibes.service.effect.LayerClassifier;
import com.github.laxika.magicalvibes.service.effect.LayerClassifier.LayerClassification;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CR 613 layer classification (step 3 of the layer-system migration). The coverage walk keeps
 * future static effects honest: every handler registered in {@code StaticEffectHandlerRegistry}
 * must have a {@link LayerClassifier} entry. The direct assertions pin the rules-critical
 * mappings the layered engine will rely on.
 */
class LayerClassifierTest {

    @Test
    @DisplayName("Every registered static effect handler's effect type has a layer classification")
    void everyRegisteredStaticHandlerEffectTypeIsClassified() {
        var handlers = GameTestEngineContext.get().getBeansOfType(StaticEffectHandlerBean.class).values();

        assertThat(handlers).isNotEmpty();
        for (StaticEffectHandlerBean handler : handlers) {
            assertThat(LayerClassifier.possibleLayers(handler.handledEffect()))
                    .as("layer classification for %s (handled by %s) — add it to LayerClassifier",
                            handler.handledEffect().getSimpleName(), handler.getClass().getSimpleName())
                    .isNotEmpty();
        }
    }

    @Test
    @DisplayName("An unclassified effect type fails fast")
    void unknownEffectTypeThrows() {
        CardEffect unknown = new CardEffect() {
        };

        assertThat(LayerClassifier.possibleLayers(unknown.getClass())).isEmpty();
        assertThatThrownBy(() -> LayerClassifier.classify(unknown, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Nested
    @DisplayName("Multi-layer effects")
    class MultiLayerEffects {

        @Test
        @DisplayName("A boost that also grants keywords contributes to layer 6 AND sublayer 7c")
        void staticBoostWithKeywordsSpansTwoLayers() {
            LayerClassification withKeywords = LayerClassifier.classify(
                    new StaticBoostEffect(1, 1, Set.of(Keyword.MOUNTAINWALK), GrantScope.OWN_CREATURES, null), false);
            LayerClassification withoutKeywords = LayerClassifier.classify(
                    new StaticBoostEffect(1, 1, Set.of(), GrantScope.OWN_CREATURES, null), false);

            assertThat(withKeywords.layers()).containsExactlyInAnyOrder(Layer.L6_ABILITIES, Layer.L7C_MODIFY_PT);
            assertThat(withoutKeywords.layers()).containsExactly(Layer.L7C_MODIFY_PT);
        }

        @Test
        @DisplayName("Animate-noncreature-artifacts spans layer 4 and sublayer 7b (CR 613.4)")
        void animateArtifactsSpansTypeAndBasePTLayers() {
            LayerClassification classification = LayerClassifier.classify(
                    new AnimateNoncreatureArtifactsEffect(), false);

            assertThat(classification.layers()).containsExactlyInAnyOrder(Layer.L4_TYPE, Layer.L7B_SET_PT);
        }
    }

    @Nested
    @DisplayName("P/T-defining effects (7a vs 7b)")
    class PowerToughnessSetting {

        @Test
        @DisplayName("SetPowerToughnessToAmount in the creature's own static slot is the 7a CDA")
        void ownSlotAmountSetterIsCda() {
            SetPowerToughnessToAmountEffect effect =
                    new SetPowerToughnessToAmountEffect(new Fixed(0), new Fixed(0));

            LayerClassification ownSlot = LayerClassifier.classify(effect, true);
            LayerClassification external = LayerClassifier.classify(effect, false);

            assertThat(ownSlot.layers()).containsExactly(Layer.L7A_CDA);
            assertThat(ownSlot.characteristicDefining()).isTrue();
            assertThat(external.layers()).containsExactly(Layer.L7B_SET_PT);
            assertThat(external.characteristicDefining()).isFalse();
        }

        @Test
        @DisplayName("SetBasePowerToughness is always 7b, even from the object's own static slot")
        void basePTSetterIsAlways7b() {
            SetBasePowerToughnessEffect effect = new SetBasePowerToughnessEffect(0, 4);

            assertThat(LayerClassifier.classify(effect, true).layers()).containsExactly(Layer.L7B_SET_PT);
            assertThat(LayerClassifier.classify(effect, false).layers()).containsExactly(Layer.L7B_SET_PT);
        }
    }

    @Nested
    @DisplayName("Color effects record additive vs setting")
    class ColorEffects {

        @Test
        @DisplayName("GrantColorEffect follows its overriding flag (Deep Freeze adds, Nim Deathmantle sets)")
        void grantColorFollowsOverridingFlag() {
            LayerClassification additive = LayerClassifier.classify(
                    new GrantColorEffect(CardColor.BLUE, GrantScope.ENCHANTED_CREATURE), false);
            LayerClassification setting = LayerClassifier.classify(
                    new GrantColorEffect(CardColor.BLACK, GrantScope.EQUIPPED_CREATURE, true), false);

            assertThat(additive.layers()).containsExactly(Layer.L5_COLOR);
            assertThat(additive.colorSetting()).isFalse();
            assertThat(setting.colorSetting()).isTrue();
        }

        @Test
        @DisplayName("GrantColorUntilEndOfTurn is setting (\"becomes red\" — Incite)")
        void untilEndOfTurnColorIsSetting() {
            LayerClassification classification = LayerClassifier.classify(
                    new GrantColorUntilEndOfTurnEffect(CardColor.RED), false);

            assertThat(classification.layers()).containsExactly(Layer.L5_COLOR);
            assertThat(classification.colorSetting()).isTrue();
        }
    }

    @Nested
    @DisplayName("Wrappers classify by their wrapped effect")
    class Wrappers {

        @Test
        @DisplayName("ConditionalEffect delegates to the wrapped effect")
        void conditionalDelegatesToWrapped() {
            LayerClassification classification = LayerClassifier.classify(
                    new ConditionalEffect(null,
                            new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ENCHANTED_CREATURE)), false);

            assertThat(classification.layers()).containsExactly(Layer.L6_ABILITIES);
        }

        @Test
        @DisplayName("EnchantedPermanentConditionalEffect unions both branches")
        void enchantedConditionalUnionsBranches() {
            LayerClassification classification = LayerClassifier.classify(
                    new EnchantedPermanentConditionalEffect(null,
                            new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ENCHANTED_CREATURE),
                            new StaticBoostEffect(2, 2, Set.of(), GrantScope.ENCHANTED_CREATURE, null)), false);

            assertThat(classification.layers())
                    .containsExactlyInAnyOrder(Layer.L6_ABILITIES, Layer.L7C_MODIFY_PT);
        }
    }

    @Nested
    @DisplayName("Single-layer mappings")
    class SingleLayerMappings {

        @Test
        @DisplayName("Copy, control, text, type-loss, ability-loss and switch effects land in their CR 613 layers")
        void oneShotContinuousEffectsLandInTheirLayers() {
            assertThat(LayerClassifier.classify(new BecomeCopyOfTargetCreatureEffect(), false).layers())
                    .containsExactly(Layer.L1_COPY);
            assertThat(LayerClassifier.classify(new GainControlOfTargetEffect(ControlDuration.END_OF_TURN), false)
                    .layers()).containsExactly(Layer.L2_CONTROL);
            assertThat(LayerClassifier.classify(new ControlEnchantedCreatureEffect(), false).layers())
                    .containsExactly(Layer.L2_CONTROL);
            assertThat(LayerClassifier.classify(new ChangeColorTextEffect(), false).layers())
                    .containsExactly(Layer.L3_TEXT);
            assertThat(LayerClassifier.classify(new LoseAllCreatureTypesEffect(), false).layers())
                    .containsExactly(Layer.L4_TYPE);
            assertThat(LayerClassifier.classify(new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE), false)
                    .layers()).containsExactly(Layer.L6_ABILITIES);
            assertThat(LayerClassifier.classify(new SwitchPowerToughnessEffect(), false).layers())
                    .containsExactly(Layer.L7D_SWITCH_PT);
        }

        @Test
        @DisplayName("The graveyard keyword-scan self effect is layer 6 with the CDA flag")
        void graveyardKeywordScanIsCdaFlagged() {
            LayerClassification classification = LayerClassifier.classify(
                    new GainKeywordsOfCreatureCardsInAllGraveyardsEffect(), true);

            assertThat(classification.layers()).containsExactly(Layer.L6_ABILITIES);
            assertThat(classification.characteristicDefining()).isTrue();
        }
    }
}
