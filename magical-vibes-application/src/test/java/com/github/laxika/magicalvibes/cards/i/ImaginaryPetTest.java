package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImaginaryPetTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private Card filler() {
        Card card = new Card();
        card.setName("Filler Card");
        card.setType(CardType.INSTANT);
        return card;
    }

    @Test
    @DisplayName("Returns itself to hand during controller's upkeep when a card is in hand")
    void returnsSelfWhenCardInHand() {
        ImaginaryPet pet = new ImaginaryPet();
        harness.addToBattlefield(player1, pet);
        harness.setHand(player1, List.of(filler()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield.stream()
                .filter(p -> p.getCard() == pet)
                .toList()).isEmpty();
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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == pet);
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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == pet);
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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == pet);
    }
}
