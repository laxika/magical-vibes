package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoardTheWeatherlight.class, ArvadTheCursed.class, BenalishHonorGuard.class,
        Opt.class, ShortSword.class, HistoryOfBenalia.class})
class BoardTheWeatherlightTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Board the Weatherlight puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(BoardTheWeatherlight.class);
    }

    @Test
    @DisplayName("Resolves by offering legendary creature among top five")
    void resolvesOfferingLegendaryCreature() {
        harness.setLibrary(player1, List.of(
                new ArvadTheCursed(),
                new BenalishHonorGuard(),
                new Opt(),
                new BenalishHonorGuard(),
                new BenalishHonorGuard()
        ));
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(offeredHistoricCards()).hasSize(1);
        assertThat(offeredHistoricCards().getFirst().getName()).isEqualTo("Arvad the Cursed");
    }

    @Test
    @DisplayName("Resolves by offering artifact among top five")
    void resolvesOfferingArtifact() {
        ShortSword artifact = new ShortSword();
        harness.setLibrary(player1, List.of(
                new BenalishHonorGuard(),
                artifact,
                new Opt(),
                new BenalishHonorGuard(),
                new BenalishHonorGuard()
        ));
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(offeredHistoricCards()).hasSize(1);
        assertThat(offeredHistoricCards().getFirst().getName()).isEqualTo("Short Sword");
    }

    @Test
    @DisplayName("Offers multiple historic cards when several are among top five")
    void offersMultipleHistoricCards() {
        harness.setLibrary(player1, List.of(
                new ArvadTheCursed(),
                new ShortSword(),
                new Opt(),
                new BenalishHonorGuard(),
                new BenalishHonorGuard()
        ));
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(offeredHistoricCards()).hasSize(2);
        assertThat(offeredHistoricCards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Arvad the Cursed", "Short Sword");
    }

    @Test
    @DisplayName("Choosing a historic card puts it into hand and bottoms the rest without an ordering choice")
    void choosingHistoricCardRandomlyBottomsRest() {
        ArvadTheCursed arvad = new ArvadTheCursed();
        harness.setLibrary(player1, List.of(
                arvad,
                new BenalishHonorGuard(),
                new Opt(),
                new BenalishHonorGuard(),
                new BenalishHonorGuard()
        ));
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        chooseHistoricCard(0);

        harness.assertInHand(player1, "Arvad the Cursed");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("You may choose no card and all looked cards go to the bottom without an ordering choice")
    void mayChooseNoCard() {
        harness.setLibrary(player1, List.of(
                new ArvadTheCursed(),
                new BenalishHonorGuard(),
                new Opt(),
                new BenalishHonorGuard(),
                new BenalishHonorGuard()
        ));
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        chooseHistoricCard(-1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("If top five has no historic cards, bottom them without an ordering choice")
    void noHistoricCardsRandomlyBottomed() {
        harness.setLibrary(player1, List.of(
                new BenalishHonorGuard(),
                new Opt(),
                new BenalishHonorGuard(),
                new Opt(),
                new BenalishHonorGuard()
        ));
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("With empty library, Board the Weatherlight does nothing")
    void emptyLibraryDoesNothing() {
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of());

        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Board the Weatherlight goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setLibrary(player1, List.of(
                new ArvadTheCursed(),
                new BenalishHonorGuard(),
                new Opt(),
                new BenalishHonorGuard(),
                new BenalishHonorGuard()
        ));
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        chooseHistoricCard(0);

        harness.assertInGraveyard(player1, "Board the Weatherlight");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With fewer than five cards in library, looks at all available")
    void fewerThanFiveCardsInLibrary() {
        harness.setLibrary(player1, List.of(
                new ArvadTheCursed(),
                new BenalishHonorGuard()
        ));
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(offeredHistoricCards()).hasSize(1);
        assertThat(offeredHistoricCards().getFirst().getName()).isEqualTo("Arvad the Cursed");
    }

    @Test
    @DisplayName("A nonlegendary Saga can be chosen as a historic card")
    void sagaCanBeChosen() {
        HistoryOfBenalia saga = new HistoryOfBenalia();
        harness.setLibrary(player1, List.of(saga, new BenalishHonorGuard()));
        castAndResolve();

        assertThat(offeredHistoricCards()).containsExactly(saga);
        chooseHistoricCard(0);

        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactly(saga);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Board the Weatherlight");
    }

    @Test
    @DisplayName("Only the top five are considered and unlooked cards stay above the bottomed cards")
    void unlookedCardsStayAboveRandomlyBottomedCards() {
        ArvadTheCursed chosen = new ArvadTheCursed();
        List<Card> rest = List.of(new BenalishHonorGuard(), new Opt(),
                new BenalishHonorGuard(), new Opt());
        HistoryOfBenalia sixth = new HistoryOfBenalia();
        ShortSword seventh = new ShortSword();
        harness.setLibrary(player1, List.of(chosen, rest.get(0), rest.get(1),
                rest.get(2), rest.get(3), sixth, seventh));
        castAndResolve();

        assertThat(offeredHistoricCards()).containsExactly(chosen);
        chooseHistoricCard(0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(6);
        assertThat(deck.subList(0, 2)).containsExactly(sixth, seventh);
        assertThat(deck.subList(2, 6)).containsExactlyInAnyOrderElementsOf(rest);
        harness.assertInGraveyard(player1, "Board the Weatherlight");
    }

    @Test
    @DisplayName("Declining with multiple historic cards bottoms all five and preserves the unlooked tail")
    void decliningMultipleHistoricCardsPreservesTail() {
        List<Card> looked = List.of(new ArvadTheCursed(), new ShortSword(),
                new HistoryOfBenalia(), new BenalishHonorGuard(), new Opt());
        Opt tail = new Opt();
        harness.setLibrary(player1, List.of(looked.get(0), looked.get(1), looked.get(2),
                looked.get(3), looked.get(4), tail));
        castAndResolve();
        chooseHistoricCard(-1);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(6);
        assertThat(deck.getFirst()).isSameAs(tail);
        assertThat(deck.subList(1, 6)).containsExactlyInAnyOrderElementsOf(looked);
        harness.assertInGraveyard(player1, "Board the Weatherlight");
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new BoardTheWeatherlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private List<Card> offeredHistoricCards() {
        PendingInteraction interaction = harness.getGameData().interaction.activeInteraction();
        if (interaction instanceof PendingInteraction.LibrarySearch search) {
            return search.params().cards();
        }
        assertThat(interaction).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        PendingInteraction.LibraryRevealChoice choice = (PendingInteraction.LibraryRevealChoice) interaction;
        return choice.allCards().stream().filter(card -> choice.validCardIds().contains(card.getId())).toList();
    }

    private void chooseHistoricCard(int index) {
        List<Card> offered = offeredHistoricCards();
        if (harness.getGameData().interaction.activeInteraction() instanceof PendingInteraction.LibrarySearch) {
            harness.handleCardChosen(player1, index);
        } else {
            harness.handleMultipleCardsChosen(player1,
                    index < 0 ? List.of() : List.of(offered.get(index).getId()));
        }
    }
}
