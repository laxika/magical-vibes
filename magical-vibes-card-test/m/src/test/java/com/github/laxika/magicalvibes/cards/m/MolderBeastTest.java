package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.r.RatchetBomb;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MolderBeast.class, Memnite.class, MindStone.class, CruelEdict.class,
        GrizzlyBears.class, Naturalize.class, Disperse.class, RatchetBomb.class})
class MolderBeastTest extends BaseCardTest {


    @Test
    @DisplayName("Triggers when an artifact creature is sacrificed")
    void triggersWhenArtifactCreatureDies() {
        harness.addToBattlefield(player1, new MolderBeast());
        harness.addToBattlefield(player2, new Memnite());

        // Use Cruel Edict to force player2 to sacrifice Memnite (artifact creature)
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Memnite");

        // Molder Beast's triggered ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Molder Beast");
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Molder Beast").getPowerModifier()).isEqualTo(2);
        assertThat(findPermanent(player1, "Molder Beast").getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Triggers when a non-creature artifact is destroyed by Naturalize")
    void triggersWhenNonCreatureArtifactIsDestroyed() {
        harness.addToBattlefield(player1, new MolderBeast());
        harness.addToBattlefield(player2, new MindStone());

        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, mindStoneId);

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Mind Stone");

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
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // No triggered ability on the stack
        assertThat(gd.stack).isEmpty();
    }


    @Test
    @DisplayName("Resolving the trigger gives Molder Beast +2/+0 until end of turn")
    void resolvingTriggerBoostsMolderBeast() {
        harness.addToBattlefield(player1, new MolderBeast());
        harness.addToBattlefield(player2, new Memnite());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Molder Beast trigger

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        Permanent molderBeast = findPermanent(player1, "Molder Beast");
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
        harness.castAndResolveInstant(player2, 0, mindStoneId);

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Mind Stone");

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
        harness.castAndResolveInstant(player1, 0, memniteId);

        // Resolve the first trigger
        harness.passBothPriorities();

        // Now Naturalize the Mind Stone
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, mindStoneId);

        GameData gd = harness.getGameData();
        // Second trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Molder Beast");

        // Resolve second trigger
        harness.passBothPriorities();

        Permanent molderBeast = findPermanent(player1, "Molder Beast");

        // Should have gotten +2/+0 twice = +4/+0
        assertThat(molderBeast.getPowerModifier()).isEqualTo(4);
        assertThat(molderBeast.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost expires during cleanup")
    void boostExpiresDuringCleanup() {
        Permanent beast = harness.addToBattlefieldAndReturn(player1, new MolderBeast());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Memnite());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, artifact.getId());
        harness.passBothPriorities();
        assertThat(beast.getPowerModifier()).isEqualTo(2);
        assertThat(beast.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(beast.getPowerModifier()).isZero();
        assertThat(beast.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each artifact destroyed simultaneously triggers separately")
    void simultaneousArtifactDeathsEachTrigger() {
        harness.addToBattlefield(player1, new RatchetBomb());
        Permanent beast = harness.addToBattlefieldAndReturn(player1, new MolderBeast());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Memnite());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Ratchet Bomb");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(beast.getPowerModifier()).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(countPermanents(player2, "Memnite")).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(beast.getPowerModifier()).isEqualTo(6);
        assertThat(beast.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Returning an artifact to hand does not trigger")
    void returningArtifactToHandDoesNotTrigger() {
        Permanent beast = harness.addToBattlefieldAndReturn(player1, new MolderBeast());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Memnite());
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        harness.assertInHand(player2, "Memnite");
        harness.assertNotInGraveyard(player2, "Memnite");
        assertThat(gd.stack).isEmpty();
        assertThat(beast.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("A pending trigger does not boost a new Molder Beast")
    void pendingTriggerDoesNotBoostNewPermanent() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new MolderBeast());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Memnite());
        harness.setHand(player1, List.of(new Naturalize(), new Disperse()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, artifact.getId());
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, original.getId());
        harness.assertInHand(player1, "Molder Beast");
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new MolderBeast());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(replacement.getPowerModifier()).isZero();
        assertThat(replacement.getToughnessModifier()).isZero();
    }
}
