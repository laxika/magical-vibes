package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.Attercop;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThroughTheForestGate.class, Forest.class, Island.class, Attercop.class, PsychogenicProbe.class})
class ThroughTheForestGateTest extends BaseCardTest {

    @Test
    @DisplayName("Puts any number of looked-at lands onto the battlefield tapped and gains 8 life")
    void putsLandsOntoBattlefieldTappedAndGainsLife() {
        Card forest = new Forest();
        Card island = new Island();
        Card creature = new Attercop();
        harness.setLibrary(player1, List.of(forest, creature, island));
        castThroughTheForestGate();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(forest.getId(), island.getId());

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), island.getId()));

        List<Permanent> enteredLands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest || permanent.getCard() == island)
                .toList();
        assertThat(enteredLands).hasSize(2).allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertLife(player1, 28);
    }

    @Test
    @DisplayName("With no land cards among the top cards, the library is shuffled and life is still gained")
    void noLandsStillGainsLife() {
        Card first = new Attercop();
        Card second = new Attercop();
        harness.setLibrary(player1, List.of(first, second));
        castThroughTheForestGate();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        harness.assertLife(player1, 28);
    }

    @Test
    @DisplayName("May choose no lands even when lands are available")
    void mayChooseNoLands() {
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        castThroughTheForestGate();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, island);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 28);
    }

    @Test
    @DisplayName("Unchosen lands remain in the library and life gain waits for the selection")
    void mayChooseOnlySomeLands() {
        Card forest = new Forest();
        Card island = new Island();
        Card creature = new Attercop();
        harness.setLibrary(player1, List.of(forest, island, creature));
        castThroughTheForestGate();
        harness.assertLife(player1, 20);

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1).allMatch(permanent -> permanent.getCard() == forest && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(island, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 28);
    }

    @Test
    @DisplayName("Only the top twenty cards can supply lands")
    void doesNotLookBeyondTwentyCards() {
        List<Card> topCards = new ArrayList<>();
        Card selectedLand = new Forest();
        topCards.add(selectedLand);
        for (int i = 1; i < 20; i++) {
            topCards.add(new Attercop());
        }
        Card unseenLand = new Island();
        List<Card> library = new ArrayList<>(topCards);
        library.add(unseenLand);
        harness.setLibrary(player1, library);
        castThroughTheForestGate();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(selectedLand.getId());
        harness.handleMultipleCardsChosen(player1, List.of(selectedLand.getId()));

        List<Card> remaining = new ArrayList<>(topCards.subList(1, 20));
        remaining.add(unseenLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(remaining);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1).allMatch(permanent -> permanent.getCard() == selectedLand && permanent.isTapped());
        harness.assertLife(player1, 28);
    }

    @Test
    @DisplayName("An empty library still grants life")
    void emptyLibraryStillGainsLife() {
        harness.setLibrary(player1, List.of());
        castThroughTheForestGate();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 28);
    }

    @Test
    @CardUsed({PsychogenicProbe.class})
    @DisplayName("Shuffling an empty library still triggers Psychogenic Probe")
    void emptyLibraryStillTriggersShuffleAbilities() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLibrary(player1, List.of());
        castThroughTheForestGate();
        harness.assertLife(player1, 28);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
    }

    private void castThroughTheForestGate() {
        harness.castFromHand(player1, new ThroughTheForestGate(), "{6}{G}{G}");
        harness.passBothPriorities();
    }
}
