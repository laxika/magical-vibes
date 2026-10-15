package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HexgoldSlash;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExpandTheSphere.class, Forest.class, CopperLonglegs.class, HexgoldSlash.class})
class ExpandTheSphereTest extends BaseCardTest {

    @Test
    @DisplayName("Puts up to two revealed lands onto the battlefield tapped without proliferating")
    void putsTwoLandsTappedWithoutProliferating() {
        Permanent bears = addCounteredBears();
        Card forest1 = new Forest();
        Card forest2 = new Forest();
        setLibrary(forest1, new HexgoldSlash(), forest2, new HexgoldSlash(), new HexgoldSlash(), new HexgoldSlash());

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(forest1.getId(), forest2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(forest1.getId(), forest2.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(permanentFor(forest1).isTapped()).isTrue();
        assertThat(permanentFor(forest2).isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Proliferates once when one revealed land is put onto the battlefield")
    void proliferatesOnceForOneLand() {
        Permanent bears = addCounteredBears();
        Card forest = new Forest();
        setLibrary(forest, new HexgoldSlash(), new HexgoldSlash(), new HexgoldSlash(), new HexgoldSlash(), new HexgoldSlash());

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(permanentFor(forest).isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Proliferates twice when no revealed land is put onto the battlefield")
    void proliferatesTwiceForNoLands() {
        Permanent bears = addCounteredBears();
        List<Card> topCards = List.of(new HexgoldSlash(), new HexgoldSlash(), new HexgoldSlash(),
                new HexgoldSlash(), new HexgoldSlash(), new HexgoldSlash());
        setLibrary(topCards.toArray(Card[]::new));

        castAndResolve();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
    }

    @Test
    @DisplayName("Available lands may be declined and each proliferation has its own choices")
    void declinesLandsAndChoosesDifferentObjectsForEachProliferation() {
        Permanent creature = addCounteredBears();
        gd.playerPoisonCounters.put(player2.getId(), 1);
        Card forest1 = new Forest();
        Card forest2 = new Forest();
        Card untouched = new HexgoldSlash();
        List<Card> lookedAt = List.of(forest1, forest2, new HexgoldSlash(),
                new HexgoldSlash(), new HexgoldSlash(), new HexgoldSlash());
        setLibrary(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2), lookedAt.get(3),
                lookedAt.get(4), lookedAt.get(5), untouched);

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library with fewer than six cards still allows choosing its land")
    void resolvesWithShortLibrary() {
        Permanent creature = addCounteredBears();
        Card forest = new Forest();
        Card other = new HexgoldSlash();
        setLibrary(forest, other);

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(permanentFor(forest).isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library still proliferates twice and each choice may be empty")
    void emptyLibraryStillProliferatesTwice() {
        Permanent creature = addCounteredBears();
        setLibrary();

        castAndResolve();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A player with only energy counters can be chosen in both proliferations")
    void proliferatesEnergyCountersTwice() {
        setLibrary();
        gd.setPlayerEnergyCounters(player1.getId(), 1);

        castAndResolve();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A player with only experience counters can be chosen in both proliferations")
    void proliferatesExperienceCountersTwice() {
        setLibrary();
        gd.playerExperienceCounters.put(player1.getId(), 1);

        castAndResolve();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));

        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addCounteredBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return bears;
    }

    private Permanent permanentFor(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new ExpandTheSphere()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
