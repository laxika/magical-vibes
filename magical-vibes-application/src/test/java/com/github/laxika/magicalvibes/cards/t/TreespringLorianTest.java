package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TreespringLorian.class)
class TreespringLorianTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new TreespringLorian()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent lorian = findPermanent(player1, "Treespring Lorian");
        assertThat(lorian.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lorian));
        harness.passBothPriorities();

        assertThat(lorian.isFaceDown()).isFalse();
    }

    @Test
    void turningFaceUpRequiresGreenMana() {
        harness.setHand(player1, List.of(new TreespringLorian()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent lorian = findPermanent(player1, "Treespring Lorian");
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        int lorianIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lorian);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, lorianIndex))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(lorian.isFaceDown()).isTrue();
    }

    @Test
    void turningFaceUpRequiresFullGenericManaCost() {
        harness.setHand(player1, List.of(new TreespringLorian()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent lorian = findPermanent(player1, "Treespring Lorian");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(lorian.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTurnFaceUpImmediatelyDuringOpponentsTurnWithoutUsingStack() {
        harness.setHand(player1, List.of(new TreespringLorian()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent lorian = findPermanent(player1, "Treespring Lorian");
        assertThat(lorian.isSummoningSick()).isTrue();
        assertThat(gqs.getEffectivePower(gd, lorian)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lorian)).isEqualTo(2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.turnFaceUp(player1, 0);

        assertThat(lorian.isFaceDown()).isFalse();
        assertThat(lorian.isSummoningSick()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(lorian);
    }
}
