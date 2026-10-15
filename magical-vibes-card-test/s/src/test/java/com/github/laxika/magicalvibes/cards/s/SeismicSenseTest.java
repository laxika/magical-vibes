package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BadgermoleCub;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeismicSense.class, Forest.class, BadgermoleCub.class})
class SeismicSenseTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at as many cards as lands controlled and offers a creature or land")
    void usesLandCountAndOffersCreatureOrLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        BadgermoleCub creature = new BadgermoleCub();
        Forest land = new Forest();
        SeismicSense nonCreature = new SeismicSense();
        setLibrary(nonCreature, creature, land);

        castSeismicSense();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).extracting(Card::getId)
                .containsExactly(nonCreature.getId(), creature.getId());
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        harness.assertInHand(player1, "Badgermole Cub");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nonCreature, land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May choose a land and decline to choose a card")
    void offersLandAndMayDecline() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Forest land = new Forest();
        SeismicSense nonCreature = new SeismicSense();
        setLibrary(land, nonCreature);

        castSeismicSense();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(land.getId());

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, nonCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no lands, the spell looks at no cards")
    void noLandsLooksAtNoCards() {
        BadgermoleCub creature = new BadgermoleCub();
        SeismicSense nonCreature = new SeismicSense();
        setLibrary(creature, nonCreature);

        castSeismicSense();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, nonCreature);
    }

    @Test
    @DisplayName("May select a land and puts only unchosen looked-at cards below the untouched library")
    void selectsLandAndPreservesUntouchedLibraryOrder() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Forest land = new Forest();
        BadgermoleCub creature = new BadgermoleCub();
        SeismicSense nonCreature = new SeismicSense();
        Forest untouchedFirst = new Forest();
        BadgermoleCub untouchedSecond = new BadgermoleCub();
        setLibrary(land, creature, nonCreature, untouchedFirst, untouchedSecond);

        castSeismicSense();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(untouchedFirst, untouchedSecond);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 4))
                .containsExactlyInAnyOrder(creature, nonCreature);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("Forest") && log.contains("into their hand"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Seismic Sense");
    }

    @Test
    @DisplayName("Cannot select two cards or a noncreature nonland card")
    void rejectsInvalidSelectionsAndAllowsOneEligibleCard() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Forest land = new Forest();
        BadgermoleCub creature = new BadgermoleCub();
        SeismicSense nonCreature = new SeismicSense();
        setLibrary(land, creature, nonCreature);

        castSeismicSense();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, nonCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Counts only the controller's lands when the spell resolves")
    void countsLandsAtResolutionAndIgnoresOpponentsLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Forest first = new Forest();
        BadgermoleCub second = new BadgermoleCub();
        Forest third = new Forest();
        setLibrary(first, second, third);
        harness.setHand(player1, List.of(new SeismicSense()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(first, second);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(third);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Looks at the entire library when fewer cards remain than lands controlled")
    void fewerCardsThanLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        BadgermoleCub creature = new BadgermoleCub();
        setLibrary(creature);

        castSeismicSense();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(creature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("An empty library does not cause a loss or require a choice")
    void emptyLibraryWithLands() {
        harness.addToBattlefield(player1, new Forest());
        setLibrary();

        castSeismicSense();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertInGraveyard(player1, "Seismic Sense");
    }

    @Test
    @DisplayName("With no eligible cards, all looked-at cards go below the untouched library without a choice")
    void noEligibleCardsGoToBottom() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        SeismicSense first = new SeismicSense();
        SeismicSense second = new SeismicSense();
        Forest untouched = new Forest();
        setLibrary(first, second, untouched);

        castSeismicSense();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(first, second);
    }

    private void castSeismicSense() {
        harness.setHand(player1, List.of(new SeismicSense()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
