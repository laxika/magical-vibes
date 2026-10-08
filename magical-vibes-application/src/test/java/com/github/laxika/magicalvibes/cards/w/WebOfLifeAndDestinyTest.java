package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WebOfLifeAndDestiny.class, GrizzlyBears.class, Shock.class})
class WebOfLifeAndDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat offers a creature from the top five")
    void offersCreatureFromTopFive() {
        GrizzlyBears creature = new GrizzlyBears();
        setupWebAndLibrary(creature, new Shock(), new Shock(), new Shock(), new Shock());

        advanceToCombat(player1);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a creature puts it onto the battlefield and bottoms the rest")
    void choosingCreaturePutsItOntoBattlefield() {
        GrizzlyBears creature = new GrizzlyBears();
        setupWebAndLibrary(creature, new Shock(), new Shock(), new Shock(), new Shock());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).doesNotContain(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The trigger does not fire during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new WebOfLifeAndDestiny());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayDeclineCreatureAndBottomAllFiveWithoutDisturbingOtherCards() {
        GrizzlyBears creature = new GrizzlyBears();
        List<Card> lookedAt = List.of(creature, new Shock(), new Shock(), new Shock(), new Shock());
        Shock sixth = new Shock();
        Shock seventh = new Shock();
        setupWebAndLibrary(lookedAt.toArray(Card[]::new));
        gd.playerDecks.get(player1.getId()).addAll(List.of(sixth, seventh));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(7);
        assertThat(deck.subList(0, 2)).containsExactly(sixth, seventh);
        assertThat(deck.subList(2, 7)).containsExactlyInAnyOrderElementsOf(lookedAt);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noCreatureAmongTopFiveDoesNotOfferCreatureBelowThem() {
        List<Card> lookedAt = List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        GrizzlyBears sixth = new GrizzlyBears();
        setupWebAndLibrary(lookedAt.toArray(Card[]::new));
        gd.playerDecks.get(player1.getId()).add(sixth);

        advanceToCombat(player1);
        harness.passBothPriorities();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(6);
        assertThat(deck.getFirst()).isSameAs(sixth);
        assertThat(deck.subList(1, 6)).containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void canChooseCreatureFromLibraryWithFewerThanFiveCards() {
        GrizzlyBears creature = new GrizzlyBears();
        Shock other = new Shock();
        setupWebAndLibrary(creature, other);

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .allSatisfy(permanent -> {
                    assertThat(permanent.isTapped()).isFalse();
                    assertThat(permanent.isSummoningSick()).isTrue();
                });
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void choosesOnlyOneOfMultipleCreaturesAndBottomsTheOther() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Shock third = new Shock();
        Shock fourth = new Shock();
        Shock fifth = new Shock();
        Shock sixth = new Shock();
        setupWebAndLibrary(first, second, third, fourth, fifth, sixth);

        advanceToCombat(player1);
        harness.passBothPriorities();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(second.getId());
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(5);
        assertThat(deck.getFirst()).isSameAs(sixth);
        assertThat(deck.subList(1, 5)).containsExactlyInAnyOrder(first, third, fourth, fifth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryResolvesWithoutChoice() {
        setupWebAndLibrary();

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void summoningSickGreenCreaturesCanConvokeBothGreenSymbols() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        harness.setHand(player1, List.of(new WebOfLifeAndDestiny()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Web of Life and Destiny");
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    private void setupWebAndLibrary(Card... topCards) {
        harness.addToBattlefield(player1, new WebOfLifeAndDestiny());
        harness.setLibrary(player1, List.of(topCards));
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
