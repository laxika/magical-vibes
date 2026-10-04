package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.t.ThelonsChant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishHunter.class, ThelonsChant.class})
class ElvishHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Taps itself and makes the target creature skip its next untap step")
    void tapsItselfAndSkipsTargetUntap() {
        Permanent hunter = addHunter();
        Permanent target = addCreatureReady(player2, new ElvishHunter());
        target.tap();
        payActivationCost();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(hunter.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(target.isTapped()).isTrue();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not tap the target creature")
    void doesNotTapTarget() {
        addHunter();
        Permanent target = addCreatureReady(player2, new ElvishHunter());
        payActivationCost();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addHunter();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new ThelonsChant());
        payActivationCost();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target itself and stays tapped through its own next untap step")
    void canTargetItself() {
        Permanent hunter = addHunter();
        payActivationCost();

        harness.activateAbility(player1, 0, null, hunter.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(hunter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An untapped target consumes the restriction during its next untap step")
    void restrictionExpiresEvenWhenTargetIsUntapped() {
        addHunter();
        Permanent target = addCreatureReady(player2, new ElvishHunter());
        payActivationCost();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(target.isTapped()).isFalse();

        target.tap();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Two activations before the same untap step do not prevent two untaps")
    void overlappingActivationsExpireTogether() {
        addHunter();
        addHunter();
        Permanent target = addCreatureReady(player2, new ElvishHunter());
        target.tap();

        payActivationCost();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        payActivationCost();
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(target.isTapped()).isTrue();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without the required green mana")
    void requiresGreenMana() {
        Permanent hunter = addHunter();
        Permanent target = addCreatureReady(player2, new ElvishHunter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hunter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ElvishHunter());
        Permanent target = addCreatureReady(player2, new ElvishHunter());
        payActivationCost();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addHunter() {
        return addCreatureReady(player1, new ElvishHunter());
    }

    private void payActivationCost() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

}
