package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SunCrestedPterodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeadeyeRigHauler.class, SunCrestedPterodon.class})
class DeadeyeRigHaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Raid ETB may return a target creature to its owner's hand")
    void raidEtbReturnsTargetCreature() {
        Permanent target = addPterodon(player2);
        markAttackedThisTurn();
        castDeadeyeRigHauler();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Sun-Crested Pterodon");
        harness.assertInHand(player2, "Sun-Crested Pterodon");
    }

    @Test
    @DisplayName("Declining the Raid ETB leaves the target creature on the battlefield")
    void decliningRaidEtbLeavesTargetCreature() {
        Permanent target = addPterodon(player2);
        markAttackedThisTurn();
        castDeadeyeRigHauler();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The ETB does not trigger when Raid is not met")
    void etbDoesNotTriggerWithoutRaid() {
        addPterodon(player2);
        castDeadeyeRigHauler();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Sun-Crested Pterodon");
    }

    @Test
    @DisplayName("Raid still applies after the attacking creature leaves the battlefield")
    void raidStillAppliesAfterAttackerLeavesBattlefield() {
        Permanent attacker = addPterodon(player1);
        Permanent target = addPterodon(player2);
        markAttackedThisTurn();
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        harness.setGraveyard(player1, List.of(attacker.getCard()));
        castDeadeyeRigHauler();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Sun-Crested Pterodon");
        harness.assertNotOnBattlefield(player2, "Sun-Crested Pterodon");
        harness.assertInHand(player2, "Sun-Crested Pterodon");
    }

    @Test
    @DisplayName("Raid can return Deadeye Rig-Hauler itself")
    void raidCanReturnItself() {
        markAttackedThisTurn();
        castDeadeyeRigHauler();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Deadeye Rig-Hauler"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Deadeye Rig-Hauler");
        harness.assertInHand(player1, "Deadeye Rig-Hauler");
    }

    @Test
    @DisplayName("An opponent's attack does not satisfy Raid")
    void opponentsAttackDoesNotSatisfyRaid() {
        addPterodon(player2);
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        castDeadeyeRigHauler();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Sun-Crested Pterodon");
    }

    @Test
    @DisplayName("Raid may return another creature controlled by its controller")
    void raidCanReturnOwnCreature() {
        Permanent target = addPterodon(player1);
        markAttackedThisTurn();
        castDeadeyeRigHauler();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Sun-Crested Pterodon");
        harness.assertInHand(player1, "Sun-Crested Pterodon");
        harness.assertOnBattlefield(player1, "Deadeye Rig-Hauler");
    }

    @Test
    @DisplayName("Raid returns a creature to its owner's hand rather than its controller's")
    void raidReturnsCreatureToOwnersHand() {
        SunCrestedPterodon card = new SunCrestedPterodon();
        card.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, card);
        markAttackedThisTurn();
        castDeadeyeRigHauler();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Sun-Crested Pterodon");
        harness.assertInHand(player1, "Sun-Crested Pterodon");
        harness.assertNotInHand(player2, "Sun-Crested Pterodon");
    }

    @Test
    @DisplayName("Raid does not resolve when its target leaves the battlefield")
    void raidDoesNotResolveWithMissingTarget() {
        Permanent target = addPterodon(player2);
        markAttackedThisTurn();
        castDeadeyeRigHauler();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Sun-Crested Pterodon");
        harness.assertNotInHand(player2, "Sun-Crested Pterodon");
        harness.assertOnBattlefield(player1, "Deadeye Rig-Hauler");
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castDeadeyeRigHauler() {
        harness.setHand(player1, List.of(new DeadeyeRigHauler()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
    }

    private Permanent addPterodon(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new SunCrestedPterodon());
    }
}
