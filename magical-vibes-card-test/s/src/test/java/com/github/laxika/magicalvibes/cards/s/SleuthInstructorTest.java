package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DogWalker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SleuthInstructor.class, DogWalker.class, GrizzlyBears.class, AirElemental.class})
class SleuthInstructorTest extends BaseCardTest {

    @Test
    void seeksAndCloaksOneNonElephantCreatureWithManaValueAtMostThree() {
        Card validCard = new GrizzlyBears();
        Card tooExpensive = new AirElemental();
        harness.setLibrary(player1, List.of(validCard, tooExpensive));
        harness.addToBattlefield(player1, new SleuthInstructor());

        Permanent dogWalker = castFaceDownDogWalker();
        turnFaceUp(dogWalker);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isCloaked)
                .extracting(Permanent::getCard)
                .containsExactly(validCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(tooExpensive);
    }

    @Test
    void abilityTriggersOnlyOnceForThePermanentObject() {
        harness.addToBattlefield(player1, new SleuthInstructor());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        Permanent firstDogWalker = castFaceDownDogWalker();
        turnFaceUp(firstDogWalker);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        Permanent secondDogWalker = castFaceDownDogWalker();
        turnFaceUp(secondDogWalker);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isCloaked)
                .hasSize(1);
    }

    private Permanent castFaceDownDogWalker() {
        harness.setHand(player1, List.of(new DogWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanents(player1, "Dog Walker").stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
    }

    private void turnFaceUp(Permanent permanent) {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(permanent));
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
