package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WolfridersSaddle.class, GreenwoodSentinel.class})
class WolfridersSaddleTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Wolfrider's Saddle creates and equips a 2/2 Wolf token")
    void enteringCreatesAndEquipsWolf() {
        harness.setHand(player1, List.of(new WolfridersSaddle()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent saddle = findPermanent(player1, "Wolfrider's Saddle");
        Permanent wolf = findPermanent(player1, "Wolf");

        assertThat(wolf.getCard().getPower()).isEqualTo(2);
        assertThat(wolf.getCard().getToughness()).isEqualTo(2);
        assertThat(saddle.getAttachedTo()).isEqualTo(wolf.getId());
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip {3} moves Wolfrider's Saddle and its bonus to another creature")
    void equipMovesSaddleAndBonus() {
        harness.setHand(player1, List.of(new WolfridersSaddle()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent saddle = findPermanent(player1, "Wolfrider's Saddle");
        Permanent wolf = findPermanent(player1, "Wolf");

        assertThat(saddle.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Wolfrider's Saddle limits its equipped creature to one blocker")
    void equippedWolfCannotBeBlockedByTwoCreatures() {
        harness.setHand(player1, List.of(new WolfridersSaddle()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent wolf = findPermanent(player1, "Wolf");
        wolf.setSummoningSick(false);
        wolf.setAttacking(true);

        harness.addToBattlefield(player2, new GreenwoodSentinel());

        harness.addToBattlefield(player2, new GreenwoodSentinel());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wolf);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, attackerIndex),
                new BlockerAssignment(1, attackerIndex)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("The Wolf is still created if the Saddle leaves before its trigger resolves")
    void createsWolfAfterSaddleLeavesBattlefield() {
        harness.setHand(player1, List.of(new WolfridersSaddle()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent saddle = findPermanent(player1, "Wolfrider's Saddle");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, saddle));
        harness.passBothPriorities();

        Permanent wolf = findPermanent(player1, "Wolf");
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Wolfrider's Saddle");
        harness.assertInGraveyard(player1, "Wolfrider's Saddle");
    }

    @Test
    @DisplayName("Moving the Saddle moves its blocking restriction and permits one blocker")
    void blockingRestrictionMovesWithEquipment() {
        harness.setHand(player1, List.of(new WolfridersSaddle()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent wolf = findPermanent(player1, "Wolf");
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.activateAbility(player1, 0, null, sentinel.getId());
        harness.passBothPriorities();

        wolf.setSummoningSick(false);
        wolf.setAttacking(true);
        sentinel.setSummoningSick(false);
        sentinel.setAttacking(true);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int wolfIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wolf);
        int sentinelIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sentinel);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, wolfIndex),
                new BlockerAssignment(1, wolfIndex),
                new BlockerAssignment(2, sentinelIndex)));

        assertThat(first.getBlockingTargetIds()).containsExactly(wolf.getId());
        assertThat(second.getBlockingTargetIds()).containsExactly(wolf.getId());
        assertThat(third.getBlockingTargetIds()).containsExactly(sentinel.getId());
    }
}
