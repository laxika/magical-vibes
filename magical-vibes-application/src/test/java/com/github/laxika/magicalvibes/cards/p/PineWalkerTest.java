package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CanyonLurkers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PineWalkerTest extends BaseCardTest {

    @Test
    void turnsFaceUpAndUntapsItself() {
        harness.setHand(player1, List.of(new PineWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent walker = findPermanent(player1, "Pine Walker");
        walker.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(walker));
        harness.passBothPriorities();

        assertThat(walker.isTapped()).isFalse();
    }

    @Test
    void turnsFaceUpAndUntapsAnotherCreatureYouControl() {
        addCreatureReady(player1, new PineWalker());
        harness.setHand(player1, List.of(new CanyonLurkers()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent lurkers = findPermanent(player1, "Canyon Lurkers");
        lurkers.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lurkers));
        harness.passBothPriorities();

        assertThat(lurkers.isTapped()).isFalse();
    }
}
