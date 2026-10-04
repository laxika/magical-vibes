package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EchoCirclet.class, CopperMyr.class})
class EchoCircletTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature can block two attackers")
    void equippedCreatureCanBlockTwo() {
        Permanent circletPerm = harness.addToBattlefieldAndReturn(player2, new EchoCirclet());
        circletPerm.setSummoningSick(false);

        CopperMyr blocker = new CopperMyr();
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, blocker);
        blockerPerm.setSummoningSick(false);

        // Attach circlet to blocker
        circletPerm.setAttachedTo(blockerPerm.getId());

        // Player1 has two attacking creatures
        CopperMyr atk1 = new CopperMyr();
        Permanent atkPerm1 = harness.addToBattlefieldAndReturn(player1, atk1);
        atkPerm1.setSummoningSick(false);
        atkPerm1.setAttacking(true);

        CopperMyr atk2 = new CopperMyr();
        Permanent atkPerm2 = harness.addToBattlefieldAndReturn(player1, atk2);
        atkPerm2.setSummoningSick(false);
        atkPerm2.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ));

        assertThat(blockerPerm.isBlocking()).isTrue();
        assertThat(blockerPerm.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Unequipped creature cannot block two attackers even with Echo Circlet on battlefield")
    void unequippedCreatureCannotBlockTwo() {
        // Echo Circlet on battlefield but NOT attached to any creature
        Permanent circletPerm = harness.addToBattlefieldAndReturn(player2, new EchoCirclet());
        circletPerm.setSummoningSick(false);

        CopperMyr blocker = new CopperMyr();
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, blocker);
        blockerPerm.setSummoningSick(false);

        CopperMyr atk1 = new CopperMyr();
        Permanent atkPerm1 = harness.addToBattlefieldAndReturn(player1, atk1);
        atkPerm1.setSummoningSick(false);
        atkPerm1.setAttacking(true);

        CopperMyr atk2 = new CopperMyr();
        Permanent atkPerm2 = harness.addToBattlefieldAndReturn(player1, atk2);
        atkPerm2.setSummoningSick(false);
        atkPerm2.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Only the equipped creature can block two, other creature cannot")
    void onlyEquippedCreatureGetsBonus() {
        Permanent circletPerm = harness.addToBattlefieldAndReturn(player2, new EchoCirclet());
        circletPerm.setSummoningSick(false);

        CopperMyr equipped = new CopperMyr();
        Permanent equippedPerm = harness.addToBattlefieldAndReturn(player2, equipped);
        equippedPerm.setSummoningSick(false);

        CopperMyr other = new CopperMyr();
        Permanent otherPerm = harness.addToBattlefieldAndReturn(player2, other);
        otherPerm.setSummoningSick(false);

        // Attach circlet to first creature only
        circletPerm.setAttachedTo(equippedPerm.getId());

        // Three attackers
        for (int i = 0; i < 3; i++) {
            CopperMyr atk = new CopperMyr();
            Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, atk);
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int otherIdx = gd.playerBattlefields.get(player2.getId()).indexOf(otherPerm);

        // Other creature tries to block two — should fail
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(otherIdx, 0),
                new BlockerAssignment(otherIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Equipped creature cannot block three attackers with one Echo Circlet")
    void equippedCreatureCannotExceedMaxBlocks() {
        Permanent circletPerm = harness.addToBattlefieldAndReturn(player2, new EchoCirclet());
        circletPerm.setSummoningSick(false);

        CopperMyr blocker = new CopperMyr();
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, blocker);
        blockerPerm.setSummoningSick(false);

        circletPerm.setAttachedTo(blockerPerm.getId());

        for (int i = 0; i < 3; i++) {
            CopperMyr atk = new CopperMyr();
            Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, atk);
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1),
                new BlockerAssignment(blockerIdx, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Equip ability attaches Echo Circlet to target creature")
    void equipAbilityAttaches() {
        Permanent circletPerm = harness.addToBattlefieldAndReturn(player1, new EchoCirclet());
        circletPerm.setSummoningSick(false);

        CopperMyr creature = new CopperMyr();
        Permanent creaturePerm = harness.addToBattlefieldAndReturn(player1, creature);
        creaturePerm.setSummoningSick(false);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, creaturePerm.getId());
        harness.passBothPriorities();

        assertThat(circletPerm.getAttachedTo()).isEqualTo(creaturePerm.getId());
    }

    @Test
    @DisplayName("Two Echo Circlets allow the same creature to block three attackers")
    void additionalBlocksAreCumulative() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CopperMyr());
        harness.addToBattlefieldAndReturn(player2, new EchoCirclet()).setAttachedTo(blocker.getId());
        harness.addToBattlefieldAndReturn(player2, new EchoCirclet()).setAttachedTo(blocker.getId());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefieldAndReturn(player1, new CopperMyr()).setAttacking(true);
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)));

        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    @DisplayName("Echo Circlet still grants an additional block when another player controls it")
    void equipmentWithDifferentControllerStillGrantsAdditionalBlock() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CopperMyr());
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefieldAndReturn(player1, new CopperMyr()).setAttacking(true);
        }
        Permanent circlet = harness.addToBattlefieldAndReturn(player1, new EchoCirclet());
        circlet.setAttachedTo(blocker.getId());
        harness.runStateBasedActions();
        assertThat(circlet.getAttachedTo()).isEqualTo(blocker.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));

        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Equipping another creature moves Echo Circlet and pays one mana")
    void equipMovesAttachmentAndPaysCost() {
        Permanent circlet = harness.addToBattlefieldAndReturn(player1, new EchoCirclet());
        Permanent oldCreature = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        Permanent newCreature = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        circlet.setAttachedTo(oldCreature.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, newCreature.getId());
        assertThat(circlet.getAttachedTo()).isEqualTo(oldCreature.getId());
        harness.passBothPriorities();

        assertThat(circlet.getAttachedTo()).isEqualTo(newCreature.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, oldCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentCreature() {
        Permanent circlet = harness.addToBattlefieldAndReturn(player1, new EchoCirclet());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CopperMyr());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(circlet.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRequiresSorceryTiming() {
        Permanent circlet = harness.addToBattlefieldAndReturn(player1, new EchoCirclet());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(circlet.getAttachedTo()).isNull();
    }
}
