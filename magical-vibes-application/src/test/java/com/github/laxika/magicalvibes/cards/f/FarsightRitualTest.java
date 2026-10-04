package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FarsightRitual.class, FountainOfYouth.class, GrizzlyBears.class, Shock.class, GloriousAnthem.class})
class FarsightRitualTest extends BaseCardTest {

    @Test
    void looksAtFourCardsAndPutsTwoIntoHandWithoutBargain() {
        List<Card> library = List.of(
                new GrizzlyBears(), new Shock(), new GrizzlyBears(), new Shock());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new FarsightRitual()));
        addBaseMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactlyElementsOf(library);

        harness.handleMultipleCardsChosen(player1, List.of(library.get(0).getId(), library.get(1).getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(library.get(0), library.get(1));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(library.get(2), library.get(3));
    }

    @Test
    void bargainLooksAtEightCardsAndStillPutsOnlyTwoIntoHand() {
        List<Card> library = List.of(
                new GrizzlyBears(), new Shock(), new GrizzlyBears(), new Shock(),
                new GrizzlyBears(), new Shock(), new GrizzlyBears(), new Shock());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new FarsightRitual()));
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth()).getId();
        addBaseMana();

        harness.castKickedInstantWithSacrifice(player1, 0, null, sacrificeId);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactlyElementsOf(library);

        harness.handleMultipleCardsChosen(player1, List.of(library.get(0).getId(), library.get(1).getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(library.get(0), library.get(1));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                library.get(2), library.get(3), library.get(4), library.get(5), library.get(6), library.get(7));
        harness.assertInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    void bargainCannotSacrificeACreature() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new FarsightRitual()));
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        addBaseMana();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(player1, 0, null, sacrificeId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("an artifact, enchantment, or token");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void putsAllAvailableCardsIntoHandWhenLibraryHasAtMostTwoCards(int size) {
        List<Card> library = List.<Card>of(new GrizzlyBears(), new Shock()).subList(0, size);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new FarsightRitual()));
        addBaseMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void requiresExactlyTwoDistinctCardsAndLeavesUnseenCardsAboveTheRemainder() {
        List<Card> library = List.of(new GrizzlyBears(), new Shock(), new GrizzlyBears(),
                new Shock(), new GrizzlyBears(), new Shock());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new FarsightRitual()));
        addBaseMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(library.get(0).getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(library.get(0).getId(), library.get(1).getId(), library.get(2).getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(library.get(0).getId(), library.get(0).getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(library.get(0).getId(), library.get(4).getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(library.get(0).getId(), library.get(2).getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(library.get(0), library.get(2));
        List<Card> remaining = gd.playerDecks.get(player1.getId());
        assertThat(remaining.subList(0, 2)).containsExactly(library.get(4), library.get(5));
        assertThat(remaining.subList(2, 4)).containsExactlyInAnyOrder(library.get(1), library.get(3));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void bargainCanSacrificeAnEnchantmentAndLookAtAFewerThanEightCardLibrary() {
        List<Card> library = List.of(new GrizzlyBears(), new Shock(), new GrizzlyBears(),
                new Shock(), new GrizzlyBears());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new FarsightRitual()));
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem()).getId();
        addBaseMana();

        harness.castKickedInstantWithSacrifice(player1, 0, null, sacrificeId);
        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Glorious Anthem");
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactlyElementsOf(library);
        harness.handleMultipleCardsChosen(player1, List.of(library.get(0).getId(), library.get(4).getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(library.get(0), library.get(4));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                library.get(1), library.get(2), library.get(3));
    }

    @Test
    void bargainCanSacrificeACreatureToken() {
        List<Card> library = List.of(new GrizzlyBears(), new Shock(), new GrizzlyBears(), new Shock(),
                new GrizzlyBears(), new Shock(), new GrizzlyBears(), new Shock());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new FarsightRitual()));
        GrizzlyBears tokenCopy = new GrizzlyBears();
        tokenCopy.setToken(true);
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, tokenCopy).getId();
        addBaseMana();

        harness.castKickedInstantWithSacrifice(player1, 0, null, sacrificeId);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactlyElementsOf(library);
        harness.handleMultipleCardsChosen(player1, List.of(library.get(6).getId(), library.get(7).getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(library.get(6), library.get(7));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library.subList(0, 6));
    }

    @Test
    void bargainCannotSacrificeAnOpponentsArtifact() {
        harness.setHand(player1, List.of(new FarsightRitual()));
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId();
        addBaseMana();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(player1, 0, null, sacrificeId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        harness.assertInHand(player1, "Farsight Ritual");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
