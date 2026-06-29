package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MolderBeastTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Molder Beast has the artifact-to-graveyard triggered ability")
    void hasCorrectEffects() {
        MolderBeast card = new MolderBeast();

        assertThat(card.getEffects(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD).getFirst())
                .isInstanceOf(BoostSelfEffect.class);

        BoostSelfEffect boost = (BoostSelfEffect) card.getEffects(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
    }

    // ===== Triggering =====

    @Test
    @DisplayName("Triggers when an artifact creature is destroyed")
    void triggersWhenArtifactCreatureDies() {
        harness.addToBattlefield(player1, new MolderBeast());
        harness.addToBattlefield(player2, new Memnite());

        // Use Cruel Edict to force player2 to sacrifice Memnite (artifact creature)
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Memnite"));

        // Molder Beast's triggered ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Molder Beast");
        assertThat(trigger.getEffectsToResolve()).hasSize(1);
        assertThat(trigger.getEffectsToResolve().getFirst()).isInstanceOf(BoostSelfEffect.class);
    }

    @Test
    @DisplayName("Triggers when a non-creature artifact is destroyed by Naturalize")
    void triggersWhenNonCreatureArtifactIsDestroyed() {
        harness.addToBattlefield(player1, new MolderBeast());
        harness.addToBattlefield(player2, new MindStone());

        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, mindStoneId);
        harness.passBothPriorities(); // Resolve Naturalize

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mind Stone"));

        // Molder Beast's triggered ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Molder Beast");
    }

    @Test
    @DisplayName("Does not trigger when a non-artifact creature dies")
    void doesNotTriggerForNonArtifactCreature() {
        harness.addToBattlefield(player1, new MolderBeast());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // No triggered ability on the stack
        assertThat(gd.stack).isEmpty();
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving the trigger gives Molder Beast +2/+0 until end of turn")
    void resolvingTriggerBoostsMolderBeast() {
        harness.addToBattlefield(player1, new MolderBeast());
        harness.addToBattlefield(player2, new Memnite());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict
        harness.passBothPriorities(); // Resolve Molder Beast trigger

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        Permanent molderBeast = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Molder Beast"))
                .findFirst().orElseThrow();
        assertThat(molderBeast.getPowerModifier()).isEqualTo(2);
        assertThat(molderBeast.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Triggers for own artifact going to graveyard too")
    void triggersForOwnArtifact() {
        harness.addToBattlefield(player1, new MolderBeast());
        harness.addToBattlefield(player1, new MindStone());

        UUID mindStoneId = harness.getPermanentId(player1, "Mind Stone");

        // Player2 casts Naturalize targeting player1's Mind Stone
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, mindStoneId);
        harness.passBothPriorities(); // Resolve Naturalize

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mind Stone"));

        // Molder Beast triggers even for own artifact
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Molder Beast");
    }

    @Test
    @DisplayName("Multiple artifact deaths trigger multiple times")
    void multipleArtifactDeathsTriggerMultipleTimes() {
        harness.addToBattlefield(player1, new MolderBeast());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new MindStone());

        UUID memniteId = harness.getPermanentId(player2, "Memnite");
        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        // Naturalize the Memnite first
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, memniteId);
        harness.passBothPriorities(); // Resolve Naturalize → trigger

        // Resolve the first trigger
        harness.passBothPriorities();

        // Now Naturalize the Mind Stone
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, mindStoneId);
        harness.passBothPriorities(); // Resolve Naturalize → trigger

        GameData gd = harness.getGameData();
        // Second trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Molder Beast");

        // Resolve second trigger
        harness.passBothPriorities();

        gd = harness.getGameData();
        Permanent molderBeast = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Molder Beast"))
                .findFirst().orElseThrow();

        // Should have gotten +2/+0 twice = +4/+0
        assertThat(molderBeast.getPowerModifier()).isEqualTo(4);
        assertThat(molderBeast.getToughnessModifier()).isEqualTo(0);
    }
}
