package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.w.WayfarersBauble;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeliveryMoogle.class, LeoninScimitar.class, WayfarersBauble.class,
        WornPowerstone.class, GrizzlyBears.class})
class DeliveryMoogleTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield ability offers artifact cards with mana value 2 or less")
    void searchesEligibleArtifactsFromLibraryAndGraveyard() {
        Card libraryArtifact = new LeoninScimitar();
        Card graveyardArtifact = new WayfarersBauble();
        Card expensiveArtifact = new WornPowerstone();
        Card nonArtifact = new GrizzlyBears();
        setLibrary(libraryArtifact, expensiveArtifact, nonArtifact);
        harness.setGraveyard(player1, List.of(graveyardArtifact));
        castMoogle();

        resolveEnterTheBattlefieldTrigger();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                libraryArtifact.getId(), graveyardArtifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardArtifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(graveyardArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardArtifact);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(libraryArtifact, expensiveArtifact, nonArtifact);
    }

    @Test
    @DisplayName("The enter-the-battlefield ability does nothing when no eligible artifact exists")
    void doesNotFindIneligibleCards() {
        Card expensiveArtifact = new WornPowerstone();
        Card nonArtifact = new GrizzlyBears();
        setLibrary(expensiveArtifact, nonArtifact);
        castMoogle();

        resolveEnterTheBattlefieldTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(expensiveArtifact, nonArtifact);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(expensiveArtifact, nonArtifact);
    }

    private void castMoogle() {
        harness.setHand(player1, List.of(new DeliveryMoogle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveEnterTheBattlefieldTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
