package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.ElvishFury;
import com.github.laxika.magicalvibes.cards.f.FlowstoneGiant;
import com.github.laxika.magicalvibes.cards.r.Rootwalla;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoQuarter.class, FlowstoneGiant.class, Rootwalla.class, ElvishFury.class})
class NoQuarterTest extends BaseCardTest {

    @Test
    @DisplayName("A blocker with lesser power than the attacker is destroyed")
    void weakerBlockerDestroyed() {
        Permanent attacker = addCreatureReady(player1, new FlowstoneGiant());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Rootwalla());
        addCreatureReady(player1, new NoQuarter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Rootwalla");
        harness.assertInGraveyard(player2, "Rootwalla");
        harness.assertOnBattlefield(player1, "Flowstone Giant");
    }

    @Test
    @DisplayName("An attacker with lesser power than the blocker is destroyed")
    void weakerAttackerDestroyed() {
        Permanent attacker = addCreatureReady(player1, new Rootwalla());
        attacker.setAttacking(true);
        addCreatureReady(player2, new FlowstoneGiant());
        addCreatureReady(player1, new NoQuarter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Rootwalla");
        harness.assertInGraveyard(player1, "Rootwalla");
        harness.assertOnBattlefield(player2, "Flowstone Giant");
    }

    @Test
    @DisplayName("Equal power on both sides destroys neither creature")
    void equalPowerDestroysNothing() {
        Permanent attacker = addCreatureReady(player1, new Rootwalla());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Rootwalla());
        addCreatureReady(player1, new NoQuarter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Rootwalla");
        harness.assertOnBattlefield(player2, "Rootwalla");
    }

    @Test
    @DisplayName("No Quarter triggers when an opponent's creature becomes blocked")
    void triggersForOpponentControlledCombat() {
        Permanent attacker = addCreatureReady(player1, new FlowstoneGiant());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Rootwalla());
        addCreatureReady(player2, new NoQuarter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Rootwalla");
    }

    @Test
    @DisplayName("Each weaker blocker is destroyed for its own attacker-blocker pair")
    void eachWeakerBlockerDestroyed() {
        Permanent attacker = addCreatureReady(player1, new FlowstoneGiant());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Rootwalla());
        addCreatureReady(player2, new Rootwalla());
        addCreatureReady(player1, new NoQuarter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Rootwalla");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Rootwalla"))
                .hasSize(2);
        harness.assertOnBattlefield(player1, "Flowstone Giant");
    }

    @Test
    @DisplayName("A power increase after blockers are declared does not undo the trigger")
    void powerIncreaseAfterBlockDeclarationDoesNotUndoTrigger() {
        Permanent attacker = addCreatureReady(player1, new FlowstoneGiant());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Rootwalla());
        addCreatureReady(player1, new NoQuarter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player2, List.of(new ElvishFury()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, blocker.getId());
        harness.passBothPriorities();

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Rootwalla");
        harness.assertInGraveyard(player2, "Rootwalla");
    }
}
