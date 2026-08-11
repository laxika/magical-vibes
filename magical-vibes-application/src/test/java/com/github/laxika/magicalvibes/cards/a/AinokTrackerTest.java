package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AinokTrackerTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new AinokTracker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent tracker = findPermanent(player1, "Ainok Tracker");
        assertThat(tracker.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int trackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tracker);
        harness.turnFaceUp(player1, trackerIndex);
        harness.passBothPriorities();

        assertThat(tracker.isFaceDown()).isFalse();
    }
}
