package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WitnessOfTheAgesTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new WitnessOfTheAges()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent witness = findPermanent(player1, "Witness of the Ages");
        assertThat(witness.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int witnessIndex = gd.playerBattlefields.get(player1.getId()).indexOf(witness);
        harness.turnFaceUp(player1, witnessIndex);
        harness.passBothPriorities();

        assertThat(witness.isFaceDown()).isFalse();
    }
}
