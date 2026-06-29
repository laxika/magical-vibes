package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MagneticMineTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Magnetic Mine has the artifact-to-graveyard triggered ability")
    void hasCorrectEffects() {
        MagneticMine card = new MagneticMine();

        assertThat(card.getEffects(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD).getFirst())
                .isInstanceOf(DealDamageToTriggeringPermanentControllerEffect.class);

        DealDamageToTriggeringPermanentControllerEffect effect =
                (DealDamageToTriggeringPermanentControllerEffect) card.getEffects(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD).getFirst();
        assertThat(effect.damage()).isEqualTo(2);
    }

    // ===== Triggering on opponent's artifact =====

    @Test
    @DisplayName("Deals 2 damage to opponent when their artifact is destroyed")
    void dealsToOpponentWhenTheirArtifactDestroyed() {
        harness.addToBattlefield(player1, new MagneticMine());
        harness.addToBattlefield(player2, new MindStone());
        harness.setLife(player2, 20);

        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, mindStoneId);
        harness.passBothPriorities(); // Resolve Naturalize

        // Magnetic Mine's trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Magnetic Mine");

        harness.passBothPriorities(); // Resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Triggering on own artifact =====

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
        harness.castInstant(player2, 0, mindStoneId);
        harness.passBothPriorities(); // Resolve Naturalize

        // Trigger on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Magnetic Mine");

        harness.passBothPriorities(); // Resolve trigger

        // Player1 takes 2 damage (their artifact was destroyed)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    // ===== Does NOT trigger on itself =====

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
        harness.castInstant(player2, 0, mineId);
        harness.passBothPriorities(); // Resolve Naturalize

        // Magnetic Mine is gone — no trigger
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Does NOT trigger on non-artifact =====

    @Test
    @DisplayName("Does not trigger when a non-artifact creature dies")
    void doesNotTriggerOnNonArtifact() {
        harness.addToBattlefield(player1, new MagneticMine());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        // Naturalize can't target Grizzly Bears (not artifact/enchantment), so let's use a different removal
        // Instead, use combat or another removal
        // Actually, let's just directly test: destroy a non-artifact creature via Cruel Edict
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.c.CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict

        // Grizzly Bears is not an artifact, so no trigger
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Triggers on artifact creature =====

    @Test
    @DisplayName("Triggers when an artifact creature is destroyed")
    void triggersOnArtifactCreature() {
        harness.addToBattlefield(player1, new MagneticMine());
        harness.addToBattlefield(player2, new Memnite());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.c.CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict — Memnite dies

        // Magnetic Mine triggers (Memnite is an artifact creature)
        assertThat(gd.stack).anyMatch(se ->
                se.getCard().getName().equals("Magnetic Mine"));

        // Resolve all triggers
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Two Magnetic Mines each trigger =====

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
        harness.castInstant(player1, 0, mindStoneId);
        harness.passBothPriorities(); // Resolve Naturalize

        // Both Magnetic Mines should have triggers on the stack
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(se -> se.getCard().getName().equals("Magnetic Mine"));

        harness.passBothPriorities(); // Resolve first trigger
        harness.passBothPriorities(); // Resolve second trigger

        // 2 + 2 = 4 damage total
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    // ===== Trigger is logged =====

    @Test
    @DisplayName("Trigger is logged when it fires")
    void triggerIsLogged() {
        harness.addToBattlefield(player1, new MagneticMine());
        harness.addToBattlefield(player2, new MindStone());

        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, mindStoneId);
        harness.passBothPriorities(); // Resolve Naturalize

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Magnetic Mine") && log.contains("triggers"));
    }
}
