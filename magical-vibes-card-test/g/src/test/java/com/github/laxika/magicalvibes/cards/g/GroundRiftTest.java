package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.cards.s.SpriteNoble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GroundRift.class, AshcoatBear.class, PrismaticLens.class, SpriteNoble.class})
class GroundRiftTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature without flying can't block this turn")
    void targetCreatureCannotBlockThisTurn() {
        Permanent attacker = addCreatureReady(player1, new AshcoatBear());
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());

        castGroundRift(blocker.getId());
        resolveAllTriggers();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetCreatureWithFlying() {
        Permanent flier = harness.addToBattlefieldAndReturn(player2, new SpriteNoble());
        harness.setHand(player1, List.of(new GroundRift()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, flier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature without flying");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PrismaticLens());
        harness.setHand(player1, List.of(new GroundRift()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature without flying");
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Ground Rift")
    void stormCreatesCopiesForEachPriorSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        gd.recordSpellCast(player1.getId(), new AshcoatBear());
        gd.recordSpellCast(player2.getId(), new AshcoatBear());

        castGroundRift(target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    @DisplayName("Storm copy can retarget another creature without flying")
    void stormCopyCanRetargetAnotherCreature() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        gd.recordSpellCast(player1.getId(), new AshcoatBear());

        castGroundRift(originalTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(originalTarget.isCantBlockThisTurn()).isTrue();
        assertThat(copyTarget.isCantBlockThisTurn()).isTrue();
    }

    private void castGroundRift(UUID targetId) {
        harness.setHand(player1, List.of(new GroundRift()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, targetId);
    }

    @Test
    @DisplayName("With no prior spells, Ground Rift affects only its target and creates no copies")
    void noPriorSpellsCreatesNoCopies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());

        castGroundRift(target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).isEmpty();
        resolveAllTriggers();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(other.isCantBlockThisTurn()).isFalse();
        harness.assertInGraveyard(player1, "Ground Rift");
    }

    @Test
    @DisplayName("Declining to retarget a storm copy retains the original target")
    void stormCopyCanKeepOriginalTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        gd.recordSpellCast(player1.getId(), new PrismaticLens());

        castGroundRift(target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(other.isCantBlockThisTurn()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card instanceof GroundRift)).hasSize(1);
    }

    @Test
    @DisplayName("Storm can retarget a copy after the original target leaves the battlefield")
    void stormCopySurvivesLossOfOriginalTarget() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        gd.recordSpellCast(player1.getId(), new PrismaticLens());

        castGroundRift(originalTarget.getId());
        gd.playerBattlefields.get(player2.getId()).remove(originalTarget);
        gd.playerGraveyards.get(player2.getId()).add(originalTarget.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(copyTarget.isCantBlockThisTurn()).isTrue();
        assertThat(originalTarget.isCantBlockThisTurn()).isFalse();
        harness.assertInGraveyard(player1, "Ground Rift");
    }

    @Test
    @DisplayName("The blocking restriction expires at the end of the turn")
    void blockingRestrictionExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());

        castGroundRift(target.getId());
        resolveAllTriggers();
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }
}
