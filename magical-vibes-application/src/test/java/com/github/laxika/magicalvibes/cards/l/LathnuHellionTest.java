package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LathnuHellion.class})
class LathnuHellionTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two energy counters")
    void entersWithTwoEnergyCounters() {
        addHellion();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Pays two energy to keep Lathnu Hellion")
    void paysEnergyToKeepHellion() {
        Permanent hellion = addHellion();

        beginEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hellion);
    }

    @Test
    @DisplayName("Declining to pay sacrifices Lathnu Hellion")
    void decliningPaymentSacrificesHellion() {
        addHellion();

        beginEndStep();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Lathnu Hellion");
        harness.assertInGraveyard(player1, "Lathnu Hellion");
    }

    @Test
    @DisplayName("Sacrifices without enough energy to pay")
    void sacrificesWithoutEnoughEnergy() {
        addHellion();
        gd.playerEnergyCounters.put(player1.getId(), 1);

        beginEndStep();

        harness.assertNotOnBattlefield(player1, "Lathnu Hellion");
        harness.assertInGraveyard(player1, "Lathnu Hellion");
    }

    @Test
    @DisplayName("Entry adds energy to the controller's existing counters")
    void addsToExistingEnergy() {
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.playerEnergyCounters.put(player2.getId(), 1);

        addHellion();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's end step does not require payment")
    void doesNotTriggerOnOpponentEndStep() {
        Permanent hellion = addHellion();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hellion);
    }

    @Test
    @DisplayName("Payment is required again at the next controller end step")
    void requiresPaymentEveryEndStep() {
        addHellion();
        beginEndStep();
        harness.handleMayAbilityChosen(player1, true);

        beginEndStep();

        harness.assertNotOnBattlefield(player1, "Lathnu Hellion");
        harness.assertInGraveyard(player1, "Lathnu Hellion");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    @DisplayName("Declining payment preserves the player's energy")
    void decliningPaymentDoesNotSpendEnergy() {
        addHellion();
        beginEndStep();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertInGraveyard(player1, "Lathnu Hellion");
    }

    @Test
    @DisplayName("Haste allows attacking on the turn it enters")
    void canAttackImmediately() {
        Permanent hellion = addHellion();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(hellion.isTapped()).isTrue();
        harness.assertLife(player2, 16);
    }

    private Permanent addHellion() {
        harness.setHand(player1, List.of(new LathnuHellion()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Lathnu Hellion");
    }

    private void beginEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
