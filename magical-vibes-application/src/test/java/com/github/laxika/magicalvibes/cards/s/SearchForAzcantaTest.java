package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SearchForAzcanta.class, JungleDelver.class, Opt.class, Island.class})
class SearchForAzcantaTest extends BaseCardTest {

    @Test
    @DisplayName("Surveil puts top card into graveyard when accepted")
    void surveilAccepted() {
        addEnchantmentReady(player1);

        Card topCard = new JungleDelver();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability — queues surveil may
        harness.handleMayAbilityChosen(player1, true); // accept: put into graveyard

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        harness.assertInGraveyard(player1, "Jungle Delver");
    }

    @Test
    @DisplayName("Surveil leaves card on top when declined")
    void surveilDeclined() {
        addEnchantmentReady(player1);

        Card topCard = new JungleDelver();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability
        harness.handleMayAbilityChosen(player1, false); // decline: leave on top

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName())
                .isEqualTo("Jungle Delver");
    }

    @Test
    @DisplayName("Transforms after surveil when graveyard reaches 7 cards")
    void transformsWithSevenCardsInGraveyard() {
        Permanent enchantment = addEnchantmentReady(player1);

        // Put 6 cards in graveyard, plus one on top that surveil will put in graveyard = 7
        fillGraveyard(player1, 6);
        Card topCard = new JungleDelver();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability
        harness.handleMayAbilityChosen(player1, true); // surveil: put into graveyard (now 7 in GY)
        // Condition met — may transform prompt appears
        harness.handleMayAbilityChosen(player1, true); // accept transform

        assertThat(enchantment.isTransformed()).isTrue();
        assertThat(enchantment.getCard().getName()).isEqualTo("Azcanta, the Sunken Ruin");
    }

    @Test
    @DisplayName("Does not offer transform when graveyard has fewer than 7 cards")
    void noTransformWithFewerThanSevenCards() {
        Permanent enchantment = addEnchantmentReady(player1);

        // Put 5 cards in graveyard, plus surveil puts 1 more = 6 total (not enough)
        fillGraveyard(player1, 5);
        Card topCard = new JungleDelver();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability
        harness.handleMayAbilityChosen(player1, true); // surveil: put into graveyard (now 6)
        // Condition not met — no transform prompt, ability finishes

        assertThat(enchantment.isTransformed()).isFalse();
        assertThat(enchantment.getCard().getName()).isEqualTo("Search for Azcanta");
    }

    @Test
    @DisplayName("Does not transform when declining surveil with 6 cards in graveyard")
    void noTransformWhenDecliningSurveilWithSixCards() {
        Permanent enchantment = addEnchantmentReady(player1);

        // Put 6 cards in graveyard, but decline surveil — stays at 6
        fillGraveyard(player1, 6);
        Card topCard = new JungleDelver();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve
        harness.handleMayAbilityChosen(player1, false); // decline surveil — 6 cards, not 7

        assertThat(enchantment.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Transform is optional (may decline)")
    void transformIsOptional() {
        Permanent enchantment = addEnchantmentReady(player1);

        fillGraveyard(player1, 7); // Already at 7
        Card topCard = new JungleDelver();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve
        harness.handleMayAbilityChosen(player1, false); // decline surveil — still 7
        // Condition met — transform prompt
        harness.handleMayAbilityChosen(player1, false); // decline transform

        assertThat(enchantment.isTransformed()).isFalse();
        assertThat(enchantment.getCard().getName()).isEqualTo("Search for Azcanta");
    }

    @Test
    @DisplayName("Trigger does not fire during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent enchantment = addEnchantmentReady(player1);

        Card topCard = new JungleDelver();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        // Advance to opponent's upkeep instead
        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(enchantment.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Azcanta, the Sunken Ruin taps for blue mana")
    void azantaTapsForBlueMana() {
        Permanent azcanta = addTransformedAzcanta(player1);

        int blueManaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);
        int azcantaIdx = indexOf(player1, azcanta);
        harness.activateAbility(player1, azcantaIdx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE))
                .isEqualTo(blueManaBefore + 1);
    }

    @Test
    void transformsWithSevenCardsEvenWhenSurveilIsDeclined() {
        Permanent enchantment = addEnchantmentReady(player1);
        fillGraveyard(player1, 7);
        Card top = new JungleDelver();
        harness.setLibrary(player1, List.of(top));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(enchantment.isTransformed()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void emptyLibraryStillAllowsTransformation() {
        Permanent enchantment = addEnchantmentReady(player1);
        fillGraveyard(player1, 7);
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(enchantment.isTransformed()).isTrue();
    }

    @Test
    void azcantaSelectsOnlyNoncreatureNonlandCardsAndOrdersTheRest() {
        Permanent azcanta = addTransformedAzcanta(player1);
        Card creature = new JungleDelver();
        Card land = new Island();
        Card instant = new Opt();
        Card enchantment = new SearchForAzcanta();
        Card unseen = new Opt();
        harness.setLibrary(player1, List.of(creature, land, instant, enchantment, unseen));
        harness.setHand(player1, List.of());

        activateSearch(azcanta);
        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice.params().cards()).containsExactly(instant, enchantment);
        harness.handleCardChosen(player1, 0);
        orderBottom(enchantment, land, creature);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(unseen, enchantment, land, creature);
        assertThat(azcanta.isTapped()).isTrue();
    }

    @Test
    void azcantaMayDeclineAnEligibleCard() {
        Permanent azcanta = addTransformedAzcanta(player1);
        Card instant = new Opt();
        Card enchantment = new SearchForAzcanta();
        harness.setLibrary(player1, List.of(instant, enchantment));
        harness.setHand(player1, List.of());

        activateSearch(azcanta);
        harness.handleCardChosen(player1, -1);
        orderBottom(enchantment, instant);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(enchantment, instant);
    }

    @Test
    void azcantaOrdersAllCardsWhenNoneAreEligible() {
        Permanent azcanta = addTransformedAzcanta(player1);
        Card creature = new JungleDelver();
        Card land = new Island();
        harness.setLibrary(player1, List.of(creature, land));
        harness.setHand(player1, List.of());

        activateSearch(azcanta);
        orderBottom(land, creature);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, creature);
    }

    @Test
    void azcantaCanSelectTheOnlyCardInAShortLibrary() {
        Permanent azcanta = addTransformedAzcanta(player1);
        Card instant = new Opt();
        harness.setLibrary(player1, List.of(instant));
        harness.setHand(player1, List.of());

        activateSearch(azcanta);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void azcantaWithEmptyLibraryFinishesWithoutAChoice() {
        Permanent azcanta = addTransformedAzcanta(player1);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());

        activateSearch(azcanta);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(azcanta.isTapped()).isTrue();
    }

    private void activateSearch(Permanent azcanta) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, azcanta), 1, null, null);
        harness.passBothPriorities();
    }

    private void orderBottom(Card... cards) {
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactlyInAnyOrder(cards);
        assertThat(reorder.toBottom()).isTrue();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(
                java.util.Arrays.stream(cards).map(reorder.cards()::indexOf).toList()));
    }

    private Permanent addEnchantmentReady(Player player) {
        return addCreatureReady(player, new SearchForAzcanta());
    }

    private Permanent addTransformedAzcanta(Player player) {
        Permanent perm = addEnchantmentReady(player);
        Card card = perm.getCard();
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }

    private void fillGraveyard(Player player, int count) {
        harness.setGraveyard(player, IntStream.range(0, count)
                .mapToObj(i -> (Card) new Opt()).toList());
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
