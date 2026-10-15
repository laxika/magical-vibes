package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IgnisScientia.class, Forest.class, GrizzlyBears.class})
class IgnisScientiaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may put a revealed land onto the battlefield tapped")
    void etbMayPutLandTapped() {
        Card land = new Forest();
        List<Card> topCards = List.of(land, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, topCards);
        harness.castFromHand(player1, new IgnisScientia(), "{1}{G}{U}");
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactlyElementsOf(topCards);
        assertThat(choice.validCardIds()).containsExactly(land.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Exiling a creature card creates a Food token")
    void exilingCreatureCreatesFood() {
        Permanent ignis = addReadyIgnis();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        addIgnisMana();

        harness.activateAbility(player1, indexOf(ignis), 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiling a noncreature card does not create a Food token")
    void exilingNoncreatureDoesNotCreateFood() {
        Permanent ignis = addReadyIgnis();
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land));
        addIgnisMana();

        harness.activateAbility(player1, indexOf(ignis), 0, null, land.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(land);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    void mayDeclineLandAndPutOnlyTopSixOnBottom() {
        Card land = new Forest();
        List<Card> lookedAt = List.of(land, new IgnisScientia(), new IgnisScientia(),
                new IgnisScientia(), new IgnisScientia(), new IgnisScientia());
        Card seventh = new Forest();
        java.util.ArrayList<Card> library = new java.util.ArrayList<>(lookedAt);
        library.add(seventh);
        harness.setLibrary(player1, library);

        harness.castFromHand(player1, new IgnisScientia(), "{1}{G}{U}");
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(seventh);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void shortLibraryCanStillPutLandOntoBattlefield() {
        Card land = new Forest();
        Card creature = new IgnisScientia();
        harness.setLibrary(player1, List.of(land, creature));

        harness.castFromHand(player1, new IgnisScientia(), "{1}{G}{U}");
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void noLandLeavesAllLookedAtCardsInLibrary() {
        List<Card> cards = List.of(new IgnisScientia(), new IgnisScientia());
        harness.setLibrary(player1, cards);

        harness.castFromHand(player1, new IgnisScientia(), "{1}{G}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canExileOwnCreatureAndSacrificeFoodToGainLife() {
        Permanent ignis = addReadyIgnis();
        Card creature = new IgnisScientia();
        harness.setGraveyard(player1, List.of(creature));
        addIgnisMana();

        harness.activateAbility(player1, indexOf(ignis), 0, null, creature.getId(), Zone.GRAVEYARD);
        assertThat(ignis.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        Permanent food = findPermanent(player1, "Food");
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(food), 0, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 3);
    }

    @Test
    void targetLeavingGraveyardDoesNotCreateFood() {
        Permanent ignis = addReadyIgnis();
        Card creature = new IgnisScientia();
        harness.setGraveyard(player2, List.of(creature));
        addIgnisMana();
        harness.activateAbility(player1, indexOf(ignis), 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(creature));

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(creature);
        harness.assertInHand(player2, "Ignis Scientia");
    }

    private Permanent addReadyIgnis() {
        return addCreatureReady(player1, new IgnisScientia());
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void addIgnisMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
