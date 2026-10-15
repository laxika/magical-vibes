package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.cards.c.CreepingCorrosion;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagneticMine.class, MindStone.class, Naturalize.class, GrizzlyBears.class,
        CruelEdict.class, Memnite.class, CreepingCorrosion.class})
class MagneticMineTest extends BaseCardTest {


    @Test
    @DisplayName("Deals 2 damage to opponent when their artifact is destroyed")
    void dealsToOpponentWhenTheirArtifactDestroyed() {
        harness.addToBattlefield(player1, new MagneticMine());
        harness.addToBattlefield(player2, new MindStone());
        harness.setLife(player2, 20);

        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, mindStoneId);

        // Magnetic Mine's trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Magnetic Mine");

        harness.passBothPriorities(); // Resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }


    @Test
    @DisplayName("Deals 2 damage to self when own artifact is destroyed")
    void dealsToSelfWhenOwnArtifactDestroyed() {
        harness.addToBattlefield(player1, new MagneticMine());
        harness.addToBattlefield(player1, new MindStone());
        harness.setLife(player1, 20);

        UUID mindStoneId = harness.getPermanentId(player1, "Mind Stone");

        // Opponent destroys player1's artifact
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, mindStoneId);

        // Trigger on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Magnetic Mine");

        harness.passBothPriorities(); // Resolve trigger

        // Player1 takes 2 damage (their artifact was destroyed)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }


    @Test
    @DisplayName("Does not trigger when Magnetic Mine itself is destroyed")
    void doesNotTriggerOnSelf() {
        harness.addToBattlefield(player1, new MagneticMine());
        harness.setLife(player1, 20);

        UUID mineId = harness.getPermanentId(player1, "Magnetic Mine");

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, mineId);

        // Magnetic Mine is gone — no trigger
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }


    @Test
    @DisplayName("Does not trigger when a non-artifact creature dies")
    void doesNotTriggerOnNonArtifact() {
        harness.addToBattlefield(player1, new MagneticMine());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Grizzly Bears is not an artifact, so no trigger
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }


    @Test
    @DisplayName("Triggers when an artifact creature is sacrificed")
    void triggersOnArtifactCreature() {
        harness.addToBattlefield(player1, new MagneticMine());
        harness.addToBattlefield(player2, new Memnite());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Magnetic Mine triggers (Memnite is an artifact creature)
        assertThat(gd.stack).anyMatch(se ->
                se.getCard().getName().equals("Magnetic Mine"));

        // Resolve all triggers
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }


    @Test
    @DisplayName("Two Magnetic Mines each trigger when an artifact is destroyed")
    void twoMinesEachTrigger() {
        harness.addToBattlefield(player1, new MagneticMine());
        harness.addToBattlefield(player1, new MagneticMine());
        harness.addToBattlefield(player2, new MindStone());
        harness.setLife(player2, 20);

        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, mindStoneId);

        // Both Magnetic Mines should have triggers on the stack
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(se -> se.getCard().getName().equals("Magnetic Mine"));

        harness.passBothPriorities(); // Resolve first trigger
        harness.passBothPriorities(); // Resolve second trigger

        // 2 + 2 = 4 damage total
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }


    @Test
    @DisplayName("Trigger is logged when it fires")
    void triggerIsLogged() {
        harness.addToBattlefield(player1, new MagneticMine());
        harness.addToBattlefield(player2, new MindStone());

        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, mindStoneId);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Magnetic Mine") && log.contains("triggers"));
    }

    @Test
    @DisplayName("Simultaneous destruction of the Mine alone does not trigger it")
    void doesNotTriggerForItsOwnDeathInBoardWipe() {
        harness.addToBattlefield(player1, new MagneticMine());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new CreepingCorrosion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Magnetic Mine");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The Mine triggers for other artifacts destroyed simultaneously with it")
    void triggersForOtherArtifactInBoardWipe() {
        harness.addToBattlefield(player1, new MagneticMine());
        harness.addToBattlefield(player2, new MindStone());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CreepingCorrosion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Magnetic Mine");
        harness.assertInGraveyard(player2, "Mind Stone");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A pending trigger deals damage after the Mine is destroyed")
    void pendingTriggerSurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new MagneticMine());
        harness.addToBattlefield(player2, new MindStone());
        harness.setLife(player2, 20);
        UUID mineId = harness.getPermanentId(player1, "Magnetic Mine");
        UUID stoneId = harness.getPermanentId(player2, "Mind Stone");
        harness.setHand(player1, List.of(new Naturalize(), new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, stoneId);
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, mineId);
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Magnetic Mine");
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }
}
