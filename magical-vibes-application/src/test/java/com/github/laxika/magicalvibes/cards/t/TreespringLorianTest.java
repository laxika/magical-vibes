package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TreespringLorian.class)
class TreespringLorianTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new TreespringLorian()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent lorian = findPermanent(player1, "Treespring Lorian");
        assertThat(lorian.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lorian));
        harness.passBothPriorities();

        assertThat(lorian.isFaceDown()).isFalse();
    }
}
