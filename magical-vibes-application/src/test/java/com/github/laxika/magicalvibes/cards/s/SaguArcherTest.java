package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SaguArcherTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new SaguArcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent archer = findPermanent(player1, "Sagu Archer");
        assertThat(archer.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int archerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(archer);
        harness.turnFaceUp(player1, archerIndex);
        harness.passBothPriorities();

        assertThat(archer.isFaceDown()).isFalse();
    }
}
