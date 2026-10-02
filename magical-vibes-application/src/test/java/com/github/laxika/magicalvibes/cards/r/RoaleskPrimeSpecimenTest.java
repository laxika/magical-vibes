package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DogWalker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoaleskPrimeSpecimen.class, DogWalker.class})
class RoaleskPrimeSpecimenTest extends BaseCardTest {

    @Test
    void paysXToConjureAndCloakAnExactManaValueCreature() {
        harness.addToBattlefield(player1, new RoaleskPrimeSpecimen());
        Permanent dogWalker = castFaceDownDogWalker();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(dogWalker));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);

        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isCloaked)
                .findFirst()
                .orElseThrow();
        assertThat(cloaked.getCard().getManaValue()).isEqualTo(2);
        assertThat(cloaked.isFaceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void cloakedCreatureCanTurnFaceUpForHybridMana() {
        harness.addToBattlefield(player1, new RoaleskPrimeSpecimen());
        Permanent dogWalker = castFaceDownDogWalker();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(dogWalker));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isCloaked)
                .findFirst()
                .orElseThrow();
        int cloakedIndex = gd.playerBattlefields.get(player1.getId()).indexOf(cloaked);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, cloakedIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(cloaked.isFaceDown()).isFalse();
    }

    private Permanent castFaceDownDogWalker() {
        harness.setHand(player1, java.util.List.of(new DogWalker()));
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
}
