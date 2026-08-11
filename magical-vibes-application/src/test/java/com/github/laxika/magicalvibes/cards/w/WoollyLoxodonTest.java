package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WoollyLoxodonTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new WoollyLoxodon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent loxodon = findPermanent(player1, "Woolly Loxodon");
        assertThat(loxodon.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int loxodonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(loxodon);
        harness.turnFaceUp(player1, loxodonIndex);
        harness.passBothPriorities();

        assertThat(loxodon.isFaceDown()).isFalse();
    }
}
