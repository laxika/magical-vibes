package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DireWolfProwler.class})
class DireWolfProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Activation gives Dire Wolf Prowler +2/+2 and haste")
    void activationBoostsAndGrantsHaste() {
        Permanent prowler = addReadyProwler();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, prowler, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Activation can be used only once each turn")
    void activationOnlyOncePerTurn() {
        addReadyProwler();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boost and haste wear off at end of turn")
    void boostAndHasteWearOffAtEndOfTurn() {
        Permanent prowler = addReadyProwler();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, prowler, Keyword.HASTE)).isFalse();
    }

    @Test
    void cannotActivateAgainWhileFirstActivationIsOnStack() {
        Permanent prowler = addReadyProwler();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, prowler, Keyword.HASTE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(4);
    }

    @Test
    void eachCopyHasItsOwnActivationLimit() {
        Permanent first = addReadyProwler();
        Permanent second = addReadyProwler();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isFalse();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        prowler.setSummoningSick(true);
        prowler.tap();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, prowler, Keyword.HASTE)).isTrue();
        assertThat(prowler.isTapped()).isTrue();
    }

    @Test
    void canActivateAgainDuringOpponentsNextTurn() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        Permanent prowler = addReadyProwler();
        addActivationMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, prowler, Keyword.HASTE)).isFalse();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, prowler, Keyword.HASTE)).isTrue();
    }

    private Permanent addReadyProwler() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        prowler.setSummoningSick(false);
        return prowler;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
