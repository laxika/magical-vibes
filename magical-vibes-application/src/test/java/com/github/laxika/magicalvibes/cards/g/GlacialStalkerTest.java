package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlacialStalkerTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new GlacialStalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent stalker = findPermanent(player1, "Glacial Stalker");
        assertThat(stalker.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int stalkerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(stalker);
        harness.turnFaceUp(player1, stalkerIndex);
        harness.passBothPriorities();

        assertThat(stalker.isFaceDown()).isFalse();
    }
}
