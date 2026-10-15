package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImaginaryPet.class, Island.class, Confiscate.class})
class ImaginaryPetTest extends BaseCardTest {

    private Island filler() {
        return new Island();
    }

    @Test
    @DisplayName("Returns itself to hand during controller's upkeep when a card is in hand")
    void returnsSelfWhenCardInHand() {
        ImaginaryPet pet = new ImaginaryPet();
        harness.addToBattlefield(player1, pet);
        harness.setHand(player1, List.of(filler()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertNotOnBattlefield(player1, pet.getName());
        assertThat(gd.playerHands.get(player1.getId())).contains(pet);
    }

    @Test
    @DisplayName("Does not trigger when hand is empty")
    void doesNotTriggerWhenHandEmpty() {
        ImaginaryPet pet = new ImaginaryPet();
        harness.addToBattlefield(player1, pet);
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, pet.getName());
    }

    @Test
    @DisplayName("Does nothing if hand becomes empty before the trigger resolves (intervening-if)")
    void doesNothingIfHandEmptyAtResolution() {
        ImaginaryPet pet = new ImaginaryPet();
        harness.addToBattlefield(player1, pet);
        harness.setHand(player1, List.of(filler()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        // Empty the hand before the trigger resolves.
        harness.setHand(player1, List.of());
        harness.passBothPriorities(); // resolve trigger — condition no longer met

        harness.assertOnBattlefield(player1, pet.getName());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(pet);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        ImaginaryPet pet = new ImaginaryPet();
        harness.addToBattlefield(player1, pet);
        harness.setHand(player1, List.of(filler()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, pet.getName());
    }

    @Test
    @DisplayName("Checks the controller's hand rather than an opponent's hand")
    void doesNotTriggerWhenOnlyOpponentHasCardInHand() {
        ImaginaryPet pet = new ImaginaryPet();
        harness.addToBattlefield(player1, pet);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(filler()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, pet.getName());
    }

    @Test
    @DisplayName("Returns to its owner's hand when its controller's upkeep begins")
    void returnsToOwnersHandWhenControlledByOpponent() {
        ImaginaryPet pet = new ImaginaryPet();
        Permanent petPermanent = harness.addToBattlefieldAndReturn(player1, pet);
        harness.setHand(player2, List.of(new Confiscate()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castEnchantment(player2, 0, petPermanent.getId());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(filler()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, pet.getName());
        assertThat(gd.playerHands.get(player1.getId())).contains(pet);
    }
    @Test
    @DisplayName("Gaining a card after an empty-hand upkeep begins does not create a trigger")
    void gainingCardAfterUpkeepDoesNotTrigger() {
        ImaginaryPet pet = new ImaginaryPet();
        harness.addToBattlefield(player1, pet);
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(filler()));

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, pet.getName());
    }

    @Test
    @DisplayName("Returns when the hand becomes empty and gains a card again before resolution")
    void returnsWhenHandIsNonemptyAgainAtResolution() {
        ImaginaryPet pet = new ImaginaryPet();
        harness.addToBattlefield(player1, pet);
        harness.setHand(player1, List.of(filler()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of());
        harness.setHand(player1, List.of(filler()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, pet.getName());
        assertThat(gd.playerHands.get(player1.getId())).contains(pet);
    }
}
