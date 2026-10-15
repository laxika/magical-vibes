package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Snakeform;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FigureOfDestiny.class, Snakeform.class})
class FigureOfDestinyTest extends BaseCardTest {

    private Permanent addFigure() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return harness.addToBattlefieldAndReturn(player1, new FigureOfDestiny());
    }

    private void resetPriority() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("First ability sets base power and toughness to 2/2")
    void firstAbilityMakesTwoByTwo() {
        Permanent figure = addFigure();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, figure)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, figure, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Full level-up chain reaches 8/8 with flying and first strike")
    void fullChainToAvatar() {
        Permanent figure = addFigure();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(2);

        resetPriority();
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(4);

        resetPriority();
        harness.addMana(player1, ManaColor.RED, 6);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, figure)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, figure, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, figure, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Higher ability does nothing without the required subtype")
    void gatedAbilityDoesNothingWithoutPrerequisite() {
        Permanent figure = addFigure();

        // The subtype condition is checked at resolution, not activation.
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, figure)).isEqualTo(1);
    }

    private void resolveAbility(int abilityIndex, int redMana) {
        resetPriority();
        harness.addMana(player1, ManaColor.RED, redMana);
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.passBothPriorities();
    }

    private Permanent addAvatar() {
        Permanent figure = addFigure();
        resolveAbility(0, 1);
        resolveAbility(1, 3);
        resolveAbility(2, 6);
        return figure;
    }

    @Test
    void firstAbilityReplacesAvatarTypesButRetainsKeywords() {
        Permanent figure = addAvatar();

        resolveAbility(0, 1);

        assertThat(gqs.effectiveCreatureSubtypes(gd, figure))
                .containsExactlyInAnyOrder(CardSubtype.KITHKIN, CardSubtype.SPIRIT);
        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, figure)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, figure, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, figure, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void secondAbilityReplacesAvatarTypesButRetainsKeywords() {
        Permanent figure = addAvatar();

        resolveAbility(1, 3);

        assertThat(gqs.effectiveCreatureSubtypes(gd, figure))
                .containsExactlyInAnyOrder(CardSubtype.KITHKIN, CardSubtype.SPIRIT, CardSubtype.WARRIOR);
        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, figure)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, figure, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, figure, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void downgradedSpiritCannotSkipWarriorStage() {
        Permanent figure = addAvatar();
        resolveAbility(0, 1);

        resolveAbility(2, 6);

        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, figure)).isEqualTo(2);
    }

    @Test
    void thirdAbilityDoesNothingWithoutWarriorSubtype() {
        Permanent figure = addFigure();
        resolveAbility(0, 1);

        resolveAbility(2, 6);

        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, figure, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, figure, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void prerequisitesAreCheckedWhenAbilitiesResolve() {
        Permanent figure = addFigure();
        harness.addMana(player1, ManaColor.WHITE, 10);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 0, null, null);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, figure)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, figure, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, figure, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void resolvingAvatarAbilityGrantsKeywordsAfterEarlierAbilityRemoval() {
        Permanent figure = addFigure();
        harness.setHand(player1, List.of(new Snakeform()));
        harness.setLibrary(player1, List.of(new FigureOfDestiny()));
        harness.addMana(player1, ManaColor.RED, 10);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.castInstant(player1, 0, figure.getId());

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, figure)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, figure, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, figure, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, figure)).containsExactlyInAnyOrder(
                CardSubtype.KITHKIN, CardSubtype.SPIRIT, CardSubtype.WARRIOR, CardSubtype.AVATAR);
    }
}
