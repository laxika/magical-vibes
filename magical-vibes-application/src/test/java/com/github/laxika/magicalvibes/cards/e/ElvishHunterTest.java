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
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
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

    private Permanent addHunter() {
        return addCreatureReady(player1, new ElvishHunter());
    }

    private void payActivationCost() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

}
