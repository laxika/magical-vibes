package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarBehemothTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new WarBehemoth()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent behemoth = findPermanent(player1, "War Behemoth");
        assertThat(behemoth.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int behemothIndex = gd.playerBattlefields.get(player1.getId()).indexOf(behemoth);
        harness.turnFaceUp(player1, behemothIndex);
        harness.passBothPriorities();

        assertThat(behemoth.isFaceDown()).isFalse();
    }
}
