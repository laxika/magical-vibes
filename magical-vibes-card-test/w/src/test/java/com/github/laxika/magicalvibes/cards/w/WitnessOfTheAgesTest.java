package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitnessOfTheAges.class})
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

    @Test
    void morphRequiresFiveManaAndTurnsFaceUpWithoutUsingTheStack() {
        harness.setHand(player1, List.of(new WitnessOfTheAges()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent witness = findPermanent(player1, "Witness of the Ages");
        int witnessIndex = gd.playerBattlefields.get(player1.getId()).indexOf(witness);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, witnessIndex))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(witness.isFaceDown()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, witnessIndex);

        assertThat(witness.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canBeCastFaceUpForItsNormalManaCost() {
        harness.setHand(player1, List.of(new WitnessOfTheAges()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent witness = findPermanent(player1, "Witness of the Ages");
        assertThat(witness.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
