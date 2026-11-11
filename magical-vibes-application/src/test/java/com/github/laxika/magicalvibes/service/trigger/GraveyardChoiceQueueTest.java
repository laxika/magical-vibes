package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A "when this enters" ability that targets a card in a graveyard and triggers while another such ability's target is
 * being chosen is an object of its own with its own targets (CR 603.3b, 603.3d): it waits its turn instead of
 * replacing the choice in progress, and both abilities reach the stack with the card chosen for each.
 */
@CardUsed({GrizzlyBears.class, Shock.class})
class GraveyardChoiceQueueTest extends BaseCardTest {

    private static Card entering(String name, CardEffect effect) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        card.addTrigger(Triggers.enters(ObjectRelation.THIS), effect);
        return card;
    }

    private static CardEffect returnCreatureToHand() {
        return new ReturnTargetCardsFromGraveyardToHandEffect(new CardTypePredicate(CardType.CREATURE), 1);
    }

    private StackEntry entryOf(String name) {
        List<StackEntry> entries = gd.stack.stream()
                .filter(entry -> entry.getCard() != null && name.equals(entry.getCard().getName()))
                .toList();
        assertThat(entries).as("stack entries of %s", name).hasSize(1);
        return entries.getFirst();
    }

    private void assertBothChoseTheirOwnCard(Card first, Card second, Card firstTarget, Card secondTarget) {
        harness.enterBattlefieldAndReturn(player1, first);
        assertThat(gd.interaction.isAwaitingInput()).as("the first ability asks for its target").isTrue();
        harness.enterBattlefieldAndReturn(player1, second);

        harness.handleMultipleCardsChosen(player1, List.of(firstTarget.getId()));
        assertThat(gd.interaction.isAwaitingInput()).as("the second ability asks for its target").isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(secondTarget.getId()));

        assertThat(entryOf(first.getName()).getTargetCardIds()).containsExactly(firstTarget.getId());
        assertThat(entryOf(second.getName()).getTargetCardIds()).containsExactly(secondTarget.getId());
    }

    @Test
    @DisplayName("Two \"return target creature card from your graveyard to your hand\" enter abilities each choose")
    void secondReturnToHandAbilityWaitsForTheFirstChoice() {
        Card firstBears = new GrizzlyBears();
        Card secondBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstBears, secondBears));

        assertBothChoseTheirOwnCard(entering("First Digger", returnCreatureToHand()),
                entering("Second Digger", returnCreatureToHand()), firstBears, secondBears);
    }

    @Test
    @DisplayName("A \"target instant card in your graveyard gains flashback\" enter ability waits for a return choice")
    void flashbackAbilityWaitsForAnotherAbilitysChoice() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(bears, shock));

        assertBothChoseTheirOwnCard(entering("Digger", returnCreatureToHand()),
                entering("Echo Mage", new GrantFlashbackToTargetGraveyardCardEffect(
                        Set.of(CardType.INSTANT, CardType.SORCERY))), bears, shock);
    }

    @Test
    @DisplayName("A return choice waits for a \"put target card from your graveyard on top of your library\" choice")
    void returnAbilityWaitsForAnotherAbilitysChoice() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(bears, shock));

        assertBothChoseTheirOwnCard(entering("Librarian", new PutTargetCardsFromGraveyardOnTopOfLibraryEffect(
                        new CardTypePredicate(CardType.INSTANT), 1)),
                entering("Digger", returnCreatureToHand()), shock, bears);
    }

    @Test
    @DisplayName("The waiting ability's choice is offered the graveyard as it is when its turn comes")
    void waitingAbilityChoosesFromTheGraveyardAsItIsThen() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        Card first = entering("First Digger", returnCreatureToHand());
        Card second = entering("Second Digger", returnCreatureToHand());

        harness.enterBattlefieldAndReturn(player1, first);
        harness.enterBattlefieldAndReturn(player1, second);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        assertThat(gd.interaction.isAwaitingInput()).as("the second ability asks for its target").isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        List<UUID> chosen = List.of(bears.getId());
        assertThat(entryOf(first.getName()).getTargetCardIds()).isEqualTo(chosen);
        assertThat(entryOf(second.getName()).getTargetCardIds()).isEqualTo(chosen);
    }
}
