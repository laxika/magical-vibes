package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.NonbasicLandsBecomeTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Word-rewriting rules of the layer-3 text-change transform (CR 612 / 613.2c). Behavior through
 * the engine (Mind Bend on Goblin King / Blood Moon / auras) is pinned by
 * {@code SevenLayerTest.Layer3Text}; this covers the transformer's own contract.
 */
class TextChangeTransformerTest {

    private static final TextReplacement BLACK_TO_BLUE = new TextReplacement("black", "blue");
    private static final TextReplacement MOUNTAIN_TO_ISLAND = new TextReplacement("Mountain", "Island");

    @Test
    @DisplayName("A color word replacement rewrites protection colors and keeps the others")
    void colorReplacementRewritesProtectionColors() {
        ProtectionFromColorsEffect protection =
                new ProtectionFromColorsEffect(Set.of(CardColor.BLACK, CardColor.RED));

        CardEffect result = TextChangeTransformer.transform(protection, List.of(BLACK_TO_BLUE));

        assertThat(result).isInstanceOf(ProtectionFromColorsEffect.class);
        assertThat(((ProtectionFromColorsEffect) result).colors())
                .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
    }

    @Test
    @DisplayName("A color word replacement rewrites a granted color")
    void colorReplacementRewritesGrantedColor() {
        GrantColorEffect grant = new GrantColorEffect(CardColor.BLACK, GrantScope.ENCHANTED_CREATURE, true);

        CardEffect result = TextChangeTransformer.transform(grant, List.of(BLACK_TO_BLUE));

        assertThat(result).isEqualTo(new GrantColorEffect(CardColor.BLUE, GrantScope.ENCHANTED_CREATURE, true));
    }

    @Test
    @DisplayName("A land type replacement rewrites a landwalk keyword, keeping boost and filter")
    void landTypeReplacementRewritesLandwalk() {
        PermanentHasAnySubtypePredicate filter = new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.GOBLIN));
        StaticBoostEffect boost = new StaticBoostEffect(1, 1, Set.of(Keyword.MOUNTAINWALK),
                GrantScope.ALL_CREATURES, filter);

        CardEffect result = TextChangeTransformer.transform(boost, List.of(MOUNTAIN_TO_ISLAND));

        StaticBoostEffect rewritten = (StaticBoostEffect) result;
        assertThat(rewritten.grantedKeywords()).containsExactly(Keyword.ISLANDWALK);
        assertThat(rewritten.powerBoost()).isEqualTo(1);
        assertThat(rewritten.filter()).isSameAs(filter);
    }

    @Test
    @DisplayName("A land type replacement rewrites land-type-setting effects")
    void landTypeReplacementRewritesTypeSetters() {
        assertThat(TextChangeTransformer.transform(
                new EnchantedPermanentBecomesTypeEffect(CardSubtype.MOUNTAIN), List.of(MOUNTAIN_TO_ISLAND)))
                .isEqualTo(new EnchantedPermanentBecomesTypeEffect(CardSubtype.ISLAND));
        assertThat(TextChangeTransformer.transform(
                new NonbasicLandsBecomeTypeEffect(CardSubtype.MOUNTAIN), List.of(MOUNTAIN_TO_ISLAND)))
                .isEqualTo(new NonbasicLandsBecomeTypeEffect(CardSubtype.ISLAND));
    }

    @Test
    @DisplayName("A land type replacement rewrites a basic land type predicate inside a filter")
    void landTypeReplacementRewritesFilterPredicate() {
        GrantKeywordEffect grant = new GrantKeywordEffect(Set.of(Keyword.FEAR), GrantScope.ALL_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), GrantDuration.END_OF_TURN, null);

        CardEffect result = TextChangeTransformer.transform(grant, List.of(MOUNTAIN_TO_ISLAND));

        assertThat(((GrantKeywordEffect) result).filter())
                .isEqualTo(new PermanentHasSubtypePredicate(CardSubtype.ISLAND));
    }

    @Test
    @DisplayName("Replacements compose in application order")
    void replacementsComposeInOrder() {
        StaticBoostEffect boost = new StaticBoostEffect(1, 1, Set.of(Keyword.MOUNTAINWALK),
                GrantScope.ALL_CREATURES, null);

        CardEffect result = TextChangeTransformer.transform(boost, List.of(
                MOUNTAIN_TO_ISLAND, new TextReplacement("Island", "Forest")));

        assertThat(((StaticBoostEffect) result).grantedKeywords()).containsExactly(Keyword.FORESTWALK);
    }

    @Test
    @DisplayName("A replacement that matches nothing returns the same effect instance")
    void nonMatchingReplacementPreservesIdentity() {
        StaticBoostEffect boost = new StaticBoostEffect(1, 1, Set.of(Keyword.MOUNTAINWALK),
                GrantScope.ALL_CREATURES, new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.GOBLIN)));

        assertThat(TextChangeTransformer.transform(boost, List.of(
                new TextReplacement("Swamp", "Forest"), BLACK_TO_BLUE))).isSameAs(boost);
    }

    @Test
    @DisplayName("A color word replacement recurses into a granted effect")
    void colorReplacementRecursesIntoGrantedEffect() {
        GrantEffectEffect grant = new GrantEffectEffect(
                new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)), GrantScope.OWN_CREATURES);

        CardEffect result = TextChangeTransformer.transform(grant, List.of(BLACK_TO_BLUE));

        assertThat(((GrantEffectEffect) result).effect())
                .isEqualTo(new ProtectionFromColorsEffect(Set.of(CardColor.BLUE)));
    }
}
