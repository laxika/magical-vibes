package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.n.NervousGardener;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TopiaryPanther;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreakOut.class, TopiaryPanther.class, NervousGardener.class, Shock.class})
class BreakOutTest extends BaseCardTest {

    @Test
    void putsASelectedLowManaValueCreatureOntoTheBattlefieldWithHaste() {
        Card creature = new NervousGardener();
        List<Card> library = libraryWith(creature);
        castBreakOut(library);

        chooseCard(creature);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        Permanent permanent = findPermanentByCardId(creature.getId());
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(idsExcept(library, creature.getId()));
    }

    @Test
    void decliningTheBattlefieldChoicePutsTheCreatureIntoHand() {
        Card creature = new NervousGardener();
        List<Card> library = libraryWith(creature);
        castBreakOut(library);

        chooseCard(creature);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(idsExcept(library, creature.getId()));
    }

    @Test
    void putsASelectedHighManaValueCreatureIntoHand() {
        Card creature = new TopiaryPanther();
        List<Card> library = libraryWith(creature);
        castBreakOut(library);

        chooseCard(creature);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(idsExcept(library, creature.getId()));
    }

    @Test
    void decliningTheCreatureRevealReturnsAllLookedAtCardsToTheLibrary() {
        List<Card> library = libraryWith(new NervousGardener());
        castBreakOut(library);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(library.stream().map(Card::getId).toArray(UUID[]::new));
    }

    @Test
    void keepsTheRevealedCreatureOutOfHandUntilTheBattlefieldChoiceIsDeclined() {
        Card creature = new NervousGardener();
        castBreakOut(libraryWith(creature));

        chooseCard(creature);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void onlyLooksAtSixCardsAndBottomsTheRestAfterTheUntouchedLibrary() {
        Card creature = new NervousGardener();
        Card seventh = new TopiaryPanther();
        Card eighth = new Shock();
        List<Card> lookedAt = libraryWith(creature);
        List<Card> library = new ArrayList<>(lookedAt);
        library.add(seventh);
        library.add(eighth);
        castBreakOut(library);

        PendingInteraction.LibrarySearch search = gd.interaction
                .activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(creature);
        chooseCard(creature);
        harness.handleMayAbilityChosen(player1, true);

        List<Card> remaining = gd.playerDecks.get(player1.getId());
        assertThat(remaining.subList(0, 2)).containsExactly(seventh, eighth);
        assertThat(remaining.subList(2, remaining.size())).extracting(Card::getId)
                .containsExactlyInAnyOrder(idsExcept(lookedAt, creature.getId()));
    }

    @Test
    void resolvesWithFewerThanSixCards() {
        Card creature = new NervousGardener();
        castBreakOut(List.of(creature));

        chooseCard(creature);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanentByCardId(creature.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void resolvesWithoutAChoiceWhenNoCreatureIsFound() {
        List<Card> library = List.of(new Shock(), new Shock(), new Shock());
        castBreakOut(library);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(library.stream().map(Card::getId).toArray(UUID[]::new));
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        castBreakOut(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void grantedHasteExpiresAfterTheTurn() {
        Card creature = new NervousGardener();
        castBreakOut(libraryWith(creature));
        chooseCard(creature);
        harness.handleMayAbilityChosen(player1, true);
        Permanent permanent = findPermanentByCardId(creature.getId());
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isFalse();
    }

    private void castBreakOut(List<Card> library) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new BreakOut(), "{R}{G}");
        harness.passBothPriorities();
    }

    private List<Card> libraryWith(Card creature) {
        return List.of(creature, new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
    }

    private void chooseCard(Card card) {
        PendingInteraction.LibrarySearch search = gd.interaction
                .activeInteraction(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, search.params().cards().indexOf(card));
    }

    private Permanent findPermanentByCardId(UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }

    private UUID[] idsExcept(List<Card> cards, UUID excludedId) {
        return cards.stream()
                .filter(card -> !card.getId().equals(excludedId))
                .map(Card::getId)
                .toArray(UUID[]::new);
    }
}
