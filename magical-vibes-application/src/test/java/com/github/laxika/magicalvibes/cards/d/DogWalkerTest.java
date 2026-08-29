package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DogWalker.class)
class DogWalkerTest extends BaseCardTest {

    @Test
    void normalCastDoesNotCreateDogs() {
        harness.setHand(player1, List.of(new DogWalker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dog")).isEmpty();
    }

    @Test
    void turningDogWalkerFaceUpCreatesTwoTappedDogs() {
        harness.setHand(player1, List.of(new DogWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent dogWalker = findPermanent(player1, "Dog Walker");
        assertThat(dogWalker.isFaceDown()).isTrue();
        assertThat(findPermanents(player1, "Dog")).isEmpty();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(dogWalker));
        harness.passBothPriorities();

        assertThat(dogWalker.isFaceDown()).isFalse();
        assertThat(findPermanents(player1, "Dog")).hasSize(2).allSatisfy(dog -> {
            assertThat(dog.isTapped()).isTrue();
            assertThat(dog.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(dog.getCard().getSubtypes()).containsExactly(CardSubtype.DOG);
            assertThat(dog.getEffectivePower()).isEqualTo(1);
            assertThat(dog.getEffectiveToughness()).isEqualTo(1);
        });
    }
}
