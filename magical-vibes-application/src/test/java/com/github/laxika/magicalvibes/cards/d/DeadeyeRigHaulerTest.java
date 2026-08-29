package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeadeyeRigHaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Raid ETB may return a target creature to its owner's hand")
    void raidEtbReturnsTargetCreature() {
        Permanent bears = addBears(player2);
        markAttackedThisTurn();
        castDeadeyeRigHauler();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the Raid ETB leaves the target creature on the battlefield")
    void decliningRaidEtbLeavesTargetCreature() {
        Permanent bears = addBears(player2);
        markAttackedThisTurn();
        castDeadeyeRigHauler();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("The ETB does not trigger when Raid is not met")
    void etbDoesNotTriggerWithoutRaid() {
        addBears(player2);
        castDeadeyeRigHauler();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ETB does nothing if Raid is lost before resolution")
    void etbDoesNothingIfRaidIsLostBeforeResolution() {
        Permanent bears = addBears(player2);
        markAttackedThisTurn();
        castDeadeyeRigHauler();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        gd.playersDeclaredAttackersThisTurn.clear();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castDeadeyeRigHauler() {
        harness.setHand(player1, List.of(new DeadeyeRigHauler()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
    }

    private Permanent addBears(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }
}
