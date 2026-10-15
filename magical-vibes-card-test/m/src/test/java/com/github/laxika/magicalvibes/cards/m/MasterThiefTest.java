package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.g.Greatsword;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MasterThief.class, Manalith.class, RuneclawBear.class, Unsummon.class, MindControl.class, Greatsword.class})
class MasterThiefTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains control of target artifact")
    void etbGainsControlOfTargetArtifact() {
        Permanent artifact = addArtifact(player2);

        castMasterThief(artifact.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));

        Permanent thief = findPermanent(player1, "Master Thief");
        assertThat(gd.newestControlEffectFor(artifact.getId()).sourcePermanentId()).isEqualTo(thief.getId());
    }

    @Test
    @DisplayName("Stolen artifact returns to its owner when Master Thief leaves the battlefield")
    void stolenArtifactReturnsWhenThiefBounced() {
        Permanent artifact = addArtifact(player2);

        castMasterThief(artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent thief = findPermanent(player1, "Master Thief");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, thief.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.controlEffectsFor(artifact.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        addArtifact(player2); // legal target exists so the spell is castable
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new MasterThief()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canEnterWithoutAnyArtifacts() {
        harness.setHand(player1, List.of(new MasterThief()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Master Thief");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetArtifactAlreadyControlledByYou() {
        Permanent artifact = addArtifact(player1);
        castMasterThief(artifact.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Manalith");
        harness.assertNotOnBattlefield(player2, "Manalith");
    }

    @Test
    void doesNotGainControlIfThiefLeavesBeforeTriggerResolves() {
        Permanent artifact = addArtifact(player2);
        castMasterThief(artifact.getId());
        harness.passBothPriorities();
        Permanent thief = findPermanent(player1, "Master Thief");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, thief.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Manalith");
        harness.assertNotOnBattlefield(player1, "Manalith");
        harness.assertInHand(player1, "Master Thief");
    }

    @Test
    void losingControlOfThiefEndsArtifactControlPermanently() {
        Permanent artifact = addArtifact(player2);
        castMasterThief(artifact.getId());
        resolveAllTriggers();
        Permanent thief = findPermanent(player1, "Master Thief");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MindControl()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castEnchantment(player2, 0, thief.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Master Thief");
        harness.assertOnBattlefield(player2, "Manalith");
        harness.assertNotOnBattlefield(player1, "Manalith");

        harness.setHand(player1, List.of(new MindControl()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castEnchantment(player1, 0, thief.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Master Thief");
        harness.assertOnBattlefield(player2, "Manalith");
        harness.assertNotOnBattlefield(player1, "Manalith");
    }

    @Test
    void equipmentRemainsAttachedWhenThiefLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new Greatsword());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 1, 0, bear.getId());
        resolveAllTriggers();
        assertThat(equipment.getAttachedTo()).isEqualTo(bear.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castMasterThief(equipment.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Greatsword");
        assertThat(equipment.getAttachedTo()).isEqualTo(bear.getId());

        Permanent thief = findPermanent(player1, "Master Thief");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, thief.getId());

        harness.assertOnBattlefield(player2, "Greatsword");
        assertThat(equipment.getAttachedTo()).isEqualTo(bear.getId());
    }

    private void castMasterThief(UUID targetId) {
        harness.setHand(player1, List.of(new MasterThief()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, 0, targetId);
    }

    private Permanent addArtifact(Player player) {
        return addCreatureReady(player, new Manalith());
    }
}
