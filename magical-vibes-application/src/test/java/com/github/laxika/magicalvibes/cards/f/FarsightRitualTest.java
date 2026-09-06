package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FarsightRitual.class, FountainOfYouth.class, GrizzlyBears.class, Shock.class})
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
        harness.addToBattlefield(player1, new FountainOfYouth());
        UUID sacrificeId = harness.getPermanentId(player1, "Fountain of Youth");
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
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID sacrificeId = harness.getPermanentId(player1, "Grizzly Bears");
        addBaseMana();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(player1, 0, null, sacrificeId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("an artifact, enchantment, or token");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
