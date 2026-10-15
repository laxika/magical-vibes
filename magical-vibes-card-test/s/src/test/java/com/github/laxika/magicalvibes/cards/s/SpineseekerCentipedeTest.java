package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FriendlyTeddy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpineseekerCentipede.class, Forest.class, GrizzlyBears.class, LeoninScimitar.class,
        Pacifism.class, Plains.class, Shock.class, FriendlyTeddy.class})
class SpineseekerCentipedeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters as a 2/1 without delirium")
    void noDelirium() {
        Permanent centipede = addCentipede(List.of(new Forest(), new Shock(), new LeoninScimitar()));

        assertThat(gqs.getEffectivePower(gd, centipede)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, centipede)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, centipede, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+2 and vigilance with delirium")
    void delirium() {
        Permanent centipede = addCentipede(List.of(
                new Forest(), new Shock(), new LeoninScimitar(), new Pacifism()));

        assertThat(gqs.getEffectivePower(gd, centipede)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, centipede)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, centipede, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("ETB searches for a basic land and puts it into its controller's hand")
    void searchesForBasicLand() {
        harness.setHand(player1, List.of(new SpineseekerCentipede()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Delirium updates when card types enter and leave the graveyard")
    void deliriumUpdatesWithGraveyard() {
        Permanent centipede = addCentipede(List.of(new Forest(), new Shock(), new LeoninScimitar()));

        harness.setGraveyard(player1, List.of(
                new Forest(), new Shock(), new LeoninScimitar(), new Pacifism()));

        assertThat(gqs.getEffectivePower(gd, centipede)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, centipede)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, centipede, Keyword.VIGILANCE)).isTrue();

        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new LeoninScimitar()));

        assertThat(gqs.getEffectivePower(gd, centipede)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, centipede)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, centipede, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Four cards of one type do not enable delirium")
    void countsDistinctTypesRatherThanCards() {
        Permanent centipede = addCentipede(List.of(
                new Forest(), new Forest(), new Plains(), new Plains()));

        assertThat(gqs.getEffectivePower(gd, centipede)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, centipede)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, centipede, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable delirium")
    void ignoresOpponentsGraveyard() {
        Permanent centipede = addCentipede(List.of());
        harness.setGraveyard(player2, List.of(
                new Forest(), new Shock(), new LeoninScimitar(), new Pacifism()));

        assertThat(gqs.getEffectivePower(gd, centipede)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, centipede)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, centipede, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The basic land search may fail to find even when a land is available")
    void mayFailToFind() {
        harness.setHand(player1, List.of(new SpineseekerCentipede()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactlyInAnyOrder("Forest", "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The basic land search finishes when the library has no basic lands")
    void noBasicLandsInLibrary() {
        harness.setHand(player1, List.of(new SpineseekerCentipede()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A card with two card types contributes both types to delirium")
    void countsAllTypesOnOneCard() {
        Permanent centipede = addCentipede(List.of(new Forest(), new Shock(), new FriendlyTeddy()));

        assertThat(gqs.getEffectivePower(gd, centipede)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, centipede)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, centipede, Keyword.VIGILANCE)).isTrue();
    }

    private Permanent addCentipede(List<Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        return harness.addToBattlefieldAndReturn(player1, new SpineseekerCentipede());
    }
}
