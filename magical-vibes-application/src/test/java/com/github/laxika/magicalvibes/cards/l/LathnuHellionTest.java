package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addHellion() {
        harness.setHand(player1, List.of(new LathnuHellion()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Lathnu Hellion"))
                .findFirst()
                .orElseThrow();
    }

    private void beginEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
