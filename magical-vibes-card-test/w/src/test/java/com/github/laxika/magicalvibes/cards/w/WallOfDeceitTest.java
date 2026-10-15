package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfDeceit.class})
class WallOfDeceitTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new WallOfDeceit()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        Permanent wall = findPermanent(player1, "Wall of Deceit");
        assertThat(wall.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(wall));
        harness.passBothPriorities();

        assertThat(wall.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(5);
    }

    @Test
    void canTurnItselfFaceDownForThreeMana() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfDeceit());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canTurnItselfFaceDownAndBackUpForItsMorphCost() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfDeceit());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(wall.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void faceDownAbilityUsesTheStackAndDoesNotRequireTapping() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfDeceit());
        wall.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(wall.isFaceDown()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(wall.isFaceDown()).isTrue();
        assertThat(wall.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(wall);
    }

    @Test
    void faceDownWallCannotActivateItsPrintedAbility() {
        harness.addToBattlefield(player1, new WallOfDeceit());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void morphIsImmediateAndRequiresBlueMana() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfDeceit());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wall.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(wall.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void faceUpWallCannotAttack() {
        addCreatureReady(player1, new WallOfDeceit());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void turningFaceDownAllowsAttackingAndMorphDoesNotRemoveItFromCombat() {
        Permanent wall = addCreatureReady(player1, new WallOfDeceit());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(wall.isAttacking()).isTrue();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(wall.isFaceDown()).isFalse();
        assertThat(wall.isAttacking()).isTrue();
        assertThat(wall.isTapped()).isTrue();
    }
}
