package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SunscorchedDesert;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HourOfPromiseTest extends BaseCardTest {

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void setupLibrary(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void castHourOfPromise() {
        harness.setHand(player1, List.of(new HourOfPromise()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Resolving offers up to two land cards to the battlefield tapped")
    void resolvesOffersUpToTwoLandsToBattlefieldTapped() {
        setupLibrary(List.of(new Plains(), new Forest(), new GrizzlyBears()));
        castHourOfPromise();

        harness.passBothPriorities();

        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().remainingCount()).isEqualTo(2);
        assertThat(activeSearch().params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(activeSearch().params().canFailToFind()).isTrue();
        assertThat(activeSearch().params().cards())
                .allMatch(c -> c.hasType(CardType.LAND))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Chosen lands enter the battlefield tapped")
    void chosenLandsEnterTapped() {
        setupLibrary(List.of(new Plains(), new Forest(), new GrizzlyBears()));
        castHourOfPromise();

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(p -> p.isTapped());
        harness.assertInGraveyard(player1, "Hour of Promise");
    }

    @Test
    @DisplayName("Fetched Deserts count toward creating two Zombie tokens")
    void fetchedDesertsCountTowardZombies() {
        harness.addToBattlefield(player1, new SunscorchedDesert());
        setupLibrary(List.of(new SunscorchedDesert(), new SunscorchedDesert(), new GrizzlyBears()));
        castHourOfPromise();

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getSubtypes().contains(CardSubtype.ZOMBIE))
                .hasSize(2)
                .allMatch(p -> p.getEffectivePower() == 2 && p.getEffectiveToughness() == 2);
    }

    @Test
    @DisplayName("Without three Deserts, no Zombie tokens are created")
    void fewerThanThreeDesertsCreatesNoZombies() {
        harness.addToBattlefield(player1, new SunscorchedDesert());
        setupLibrary(List.of(new Forest(), new Plains(), new GrizzlyBears()));
        castHourOfPromise();

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.ZOMBIE));
    }
}
