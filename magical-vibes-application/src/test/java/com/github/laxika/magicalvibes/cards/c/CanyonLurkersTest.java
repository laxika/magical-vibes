package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CanyonLurkersTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new CanyonLurkers()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent lurkers = findPermanent(player1, "Canyon Lurkers");
        assertThat(lurkers.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lurkersIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lurkers);
        harness.turnFaceUp(player1, lurkersIndex);
        harness.passBothPriorities();

        assertThat(lurkers.isFaceDown()).isFalse();
    }
}
