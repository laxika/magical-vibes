package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindFuneral.class, Forest.class, GrizzlyBears.class, Divination.class})
class MindFuneralTest extends BaseCardTest {

    private void castMindFuneral() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new MindFuneral()));
        harness.addMana(player1, ManaColor.BLUE, 2); // {1}{U}
        harness.addMana(player1, ManaColor.BLACK, 1); // {B}
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Reveals until four lands are found and mills every revealed card, stopping after the fourth land")
    void millsUntilFourLands() {
        harness.setLibrary(player2, List.of(
                new Forest(),        // land 1
                new GrizzlyBears(),
                new Forest(),        // land 2
                new Divination(),
                new Forest(),        // land 3
                new Forest(),        // land 4 -> stop
                new GrizzlyBears()   // stays in library
        ));

        castMindFuneral();

        // Everything revealed up to and including the fourth land is milled.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name")
                .containsExactlyInAnyOrder("Forest", "Forest", "Forest", "Forest", "Grizzly Bears", "Divination");

        // Revealing stops at the fourth land — the last card is untouched.
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting("name").containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("A library with fewer than four lands is entirely milled")
    void millsEntireLibraryWhenFewerThanFourLands() {
        harness.setLibrary(player2, List.of(
                new Forest(),
                new GrizzlyBears(),
                new Forest()
        ));

        castMindFuneral();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name").containsExactlyInAnyOrder("Forest", "Forest", "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library reveals nothing and leaves the opponent's graveyard unchanged")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player2, List.of());
        GrizzlyBears existingCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(existingCard));

        castMindFuneral();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(existingCard);
        harness.assertInGraveyard(player1, "Mind Funeral");
    }

    @Test
    @DisplayName("A library containing no lands is entirely put into its owner's graveyard")
    void noLandsMovesEntireLibraryToGraveyard() {
        GrizzlyBears creature = new GrizzlyBears();
        Divination sorcery = new Divination();
        harness.setLibrary(player2, List.of(creature, sorcery));
        Forest existingCard = new Forest();
        harness.setGraveyard(player2, List.of(existingCard));
        Forest casterLibraryCard = new Forest();
        harness.setLibrary(player1, List.of(casterLibraryCard));

        castMindFuneral();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(existingCard, creature, sorcery);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(casterLibraryCard);
        harness.assertInGraveyard(player1, "Mind Funeral");
    }

    @Test
    @DisplayName("Cannot target self — must target an opponent")
    void cannotTargetSelf() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new MindFuneral()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
