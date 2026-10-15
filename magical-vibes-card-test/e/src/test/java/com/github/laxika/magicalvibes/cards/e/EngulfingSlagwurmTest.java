package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EngulfingSlagwurm.class, CarapaceForger.class, Disperse.class, DarksteelMyr.class})
class EngulfingSlagwurmTest extends BaseCardTest {


    @Test
    @DisplayName("Blocking creates a trigger that destroys the attacker and gains life equal to its toughness")
    void blockingDestroysAttackerAndGainsLife() {
        harness.setLife(player2, 20);
        addCreatureReady(player2, new EngulfingSlagwurm());
        Permanent attacker = addCreatureReady(player1, new CarapaceForger());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Engulfing Slagwurm");
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());

        harness.passBothPriorities();

        // Attacker destroyed
        harness.assertNotOnBattlefield(player1, "Carapace Forger");
        harness.assertInGraveyard(player1, "Carapace Forger");

        // Slagwurm still alive
        harness.assertOnBattlefield(player2, "Engulfing Slagwurm");

        // Controller gained life equal to attacker's toughness (2)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }


    @Test
    @DisplayName("Becoming blocked creates a trigger that destroys the blocker and gains life equal to its toughness")
    void becomingBlockedDestroysBlockerAndGainsLife() {
        harness.setLife(player1, 20);
        Permanent slagwurm = addCreatureReady(player1, new EngulfingSlagwurm());
        slagwurm.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CarapaceForger());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(slagwurm.getId());

        harness.passBothPriorities();

        // Blocker destroyed
        harness.assertNotOnBattlefield(player2, "Carapace Forger");
        harness.assertInGraveyard(player2, "Carapace Forger");

        // Controller gained life equal to blocker's toughness (2)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures creates one trigger per blocker")
    void becomingBlockedByMultipleCreaturesCreatesMultipleTriggers() {
        harness.setLife(player1, 20);
        Permanent slagwurm = addCreatureReady(player1, new EngulfingSlagwurm());
        slagwurm.setAttacking(true);
        addCreatureReady(player2, new CarapaceForger());
        addCreatureReady(player2, new CarapaceForger());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long triggerCount = gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Engulfing Slagwurm"))
                .count();
        assertThat(triggerCount).isEqualTo(2);

        resolveAllTriggers();

        // Both blockers destroyed
        harness.assertNotOnBattlefield(player2, "Carapace Forger");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Carapace Forger"))
                .hasSize(2);

        // Gained 2 life per blocker (toughness 2 each)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }


    @Test
    @DisplayName("Life gain occurs even if target creature is indestructible")
    void lifeGainOccursEvenIfTargetIsIndestructible() {
        harness.setLife(player1, 20);
        Permanent slagwurm = addCreatureReady(player1, new EngulfingSlagwurm());
        slagwurm.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new CarapaceForger());
        blocker.getGrantedKeywords().add(com.github.laxika.magicalvibes.model.Keyword.INDESTRUCTIBLE);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Blocker survives (indestructible)
        harness.assertOnBattlefield(player2, "Carapace Forger");

        // Controller still gains life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }


    @Test
    @DisplayName("Life gain uses last known toughness when the blocker leaves before resolution")
    void gainsLifeIfBlockerLeavesBeforeResolution() {
        harness.setLife(player1, 20);
        Permanent slagwurm = addCreatureReady(player1, new EngulfingSlagwurm());
        slagwurm.setAttacking(true);
        addCreatureReady(player2, new CarapaceForger());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, blocker.getId());
        harness.passBothPriorities();
        harness.assertInHand(player2, "Carapace Forger");

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Blocking gains life even when the attacker leaves before resolution")
    void gainsLifeIfAttackerLeavesBeforeResolution() {
        harness.setLife(player2, 20);
        addCreatureReady(player2, new EngulfingSlagwurm());
        Permanent attacker = addCreatureReady(player1, new CarapaceForger());
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Carapace Forger");
        harness.passBothPriorities();

        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("An indestructible blocker survives and its toughness still grants life")
    void realIndestructibleBlockerStillGrantsLife() {
        harness.setLife(player1, 20);
        Permanent slagwurm = addCreatureReady(player1, new EngulfingSlagwurm());
        slagwurm.setAttacking(true);
        addCreatureReady(player2, new DarksteelMyr());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Myr");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("The trigger resolves after Slagwurm leaves the battlefield")
    void triggerResolvesWithoutItsSource() {
        harness.setLife(player1, 20);
        Permanent slagwurm = addCreatureReady(player1, new EngulfingSlagwurm());
        slagwurm.setAttacking(true);
        addCreatureReady(player2, new CarapaceForger());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, slagwurm.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Engulfing Slagwurm");
        harness.assertInGraveyard(player2, "Carapace Forger");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Life gain uses the creature's toughness at resolution rather than at blocking")
    void gainsLifeForCurrentToughness() {
        harness.setLife(player1, 20);
        Permanent slagwurm = addCreatureReady(player1, new EngulfingSlagwurm());
        slagwurm.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CarapaceForger());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Carapace Forger");
        harness.assertLife(player1, 24);
    }



}
