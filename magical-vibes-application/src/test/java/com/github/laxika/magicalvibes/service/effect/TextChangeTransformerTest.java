package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.effect.AllLandsAreCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.NonbasicLandsBecomeTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.SpellFilter;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Word-rewriting rules of the layer-3 text-change transform (CR 612 / 613.1c). Behavior through
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
        GrantColorEffect grant = new GrantColorEffect(CardColor.BLACK, Subjects.attachedCreature(), true);

        CardEffect result = TextChangeTransformer.transform(grant, List.of(BLACK_TO_BLUE));

        assertThat(result).isEqualTo(new GrantColorEffect(CardColor.BLUE, Subjects.attachedCreature(), true));
    }

    @Test
    @DisplayName("A color word replacement preserves a grant's permanent filter")
    void colorReplacementPreservesGrantedColorFilter() {
        PermanentHasSubtypePredicate filter = new PermanentHasSubtypePredicate(CardSubtype.GOBLIN);
        GrantColorEffect grant = new GrantColorEffect(
                CardColor.BLACK, Subjects.otherCreatures(), false, filter);

        CardEffect result = TextChangeTransformer.transform(grant, List.of(BLACK_TO_BLUE));

        assertThat(result).isEqualTo(new GrantColorEffect(
                CardColor.BLUE, Subjects.otherCreatures(), false, filter));
    }

    @Test
    @DisplayName("A land type replacement rewrites a landwalk keyword, keeping boost and filter")
    void landTypeReplacementRewritesLandwalk() {
        PermanentHasAnySubtypePredicate filter = new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.GOBLIN));
        StaticBoostEffect boost = new StaticBoostEffect(1, 1, Set.of(Keyword.MOUNTAINWALK),
                Subjects.otherCreatures(), filter);

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
        GrantKeywordEffect grant = new GrantKeywordEffect(Set.of(Keyword.FEAR), Subjects.otherCreatures(),
                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), Duration.UNTIL_END_OF_TURN, null);

        CardEffect result = TextChangeTransformer.transform(grant, List.of(MOUNTAIN_TO_ISLAND));

        assertThat(((GrantKeywordEffect) result).filter())
                .isEqualTo(new PermanentHasSubtypePredicate(CardSubtype.ISLAND));
    }

    @Test
    @DisplayName("Replacements compose in application order")
    void replacementsComposeInOrder() {
        StaticBoostEffect boost = new StaticBoostEffect(1, 1, Set.of(Keyword.MOUNTAINWALK),
                Subjects.otherCreatures(), null);

        CardEffect result = TextChangeTransformer.transform(boost, List.of(
                MOUNTAIN_TO_ISLAND, new TextReplacement("Island", "Forest")));

        assertThat(((StaticBoostEffect) result).grantedKeywords()).containsExactly(Keyword.FORESTWALK);
    }

    @Test
    @DisplayName("A replacement that matches nothing returns the same effect instance")
    void nonMatchingReplacementPreservesIdentity() {
        StaticBoostEffect boost = new StaticBoostEffect(1, 1, Set.of(Keyword.MOUNTAINWALK),
                Subjects.otherCreatures(), new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.GOBLIN)));

        assertThat(TextChangeTransformer.transform(boost, List.of(
                new TextReplacement("Swamp", "Forest"), BLACK_TO_BLUE))).isSameAs(boost);
    }

    @Test
    @DisplayName("A color word replacement recurses into a granted effect")
    void colorReplacementRecursesIntoGrantedEffect() {
        GrantEffectEffect grant = new GrantEffectEffect(
                new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)), Subjects.otherCreaturesYouControl());

        CardEffect result = TextChangeTransformer.transform(grant, List.of(BLACK_TO_BLUE));

        assertThat(((GrantEffectEffect) result).effect())
                .isEqualTo(new ProtectionFromColorsEffect(Set.of(CardColor.BLUE)));
    }

    @Test
    @DisplayName("A cast trigger's spell filter is rewritten by the own replacements, then the global ones")
    void colorReplacementRewritesACastTriggersSpellFilter() {
        SpellFilter filter = SpellFilter.ANY.withSpell(new CardColorPredicate(CardColor.BLACK)).withNthThisTurn(2);
        TextReplacement blueToRed = new TextReplacement("blue", "red", false, 0L);

        assertThat(TextChangeTransformer.transformSpellFilter(filter, List.of(BLACK_TO_BLUE), List.of(blueToRed)))
                .isEqualTo(SpellFilter.ANY.withSpell(new CardColorPredicate(CardColor.RED)).withNthThisTurn(2));
        // A timestamped own change made after the global rewrite applies after it (CR 613.7).
        TextReplacement laterRedToGreen = new TextReplacement("red", "green", false, 2L);
        TextReplacement globalBlackToRed = new TextReplacement("black", "red", false, 1L);
        assertThat(TextChangeTransformer.transformSpellFilter(filter, List.of(laterRedToGreen),
                List.of(globalBlackToRed)))
                .isEqualTo(SpellFilter.ANY.withSpell(new CardColorPredicate(CardColor.GREEN)).withNthThisTurn(2));
    }

    @Test
    @DisplayName("A cast trigger's spell filter without the replaced word, or without a spell, is returned unchanged")
    void untouchedSpellFilterKeepsIdentity() {
        SpellFilter red = SpellFilter.ANY.withSpell(new CardColorPredicate(CardColor.RED));

        assertThat(TextChangeTransformer.transformSpellFilter(red, List.of(BLACK_TO_BLUE, MOUNTAIN_TO_ISLAND),
                List.of())).isSameAs(red);
        assertThat(TextChangeTransformer.transformSpellFilter(SpellFilter.ANY, List.of(BLACK_TO_BLUE), List.of()))
                .isSameAs(SpellFilter.ANY);
    }

    @Test
    @DisplayName("A replacement rewrites the land type and color of an all-lands animation, keeping identity otherwise")
    void replacementRewritesAllLandsAnimation() {
        AllLandsAreCreaturesEffect bell = new AllLandsAreCreaturesEffect(1, 1, CardSubtype.MOUNTAIN, CardColor.BLACK);

        assertThat(TextChangeTransformer.transform(bell, List.of(MOUNTAIN_TO_ISLAND, BLACK_TO_BLUE)))
                .isEqualTo(new AllLandsAreCreaturesEffect(1, 1, CardSubtype.ISLAND, CardColor.BLUE));
        AllLandsAreCreaturesEffect revolt = new AllLandsAreCreaturesEffect(2, 2);
        assertThat(TextChangeTransformer.transform(revolt, List.of(MOUNTAIN_TO_ISLAND, BLACK_TO_BLUE)))
                .isSameAs(revolt);
    }
}
