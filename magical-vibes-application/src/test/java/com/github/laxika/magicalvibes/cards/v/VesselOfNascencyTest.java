package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EvolutionaryLeap;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VesselOfNascencyTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself, offers permanent cards, and puts the rest into the graveyard")
    void sacrificesItselfOffersPermanentCardsAndBinsRest() {
        Card artifact = new FountainOfYouth();
        Card creature = new GrizzlyBears();
        Card enchantment = new EvolutionaryLeap();
        Card land = new Forest();
        harness.addToBattlefield(player1, new VesselOfNascency());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(artifact, creature, enchantment, land));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Vessel of Nascency");
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice.params().cards()).containsExactly(artifact, creature, enchantment, land);

        chooseCard(1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(artifact, enchantment, land)
                .doesNotContain(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Offers planeswalker cards but not instants, and may decline the card")
    void offersPlaneswalkersButNotInstantsAndMayDecline() {
        Card planeswalker = card("Test Walker", CardType.PLANESWALKER);
        Card instant = new Shock();
        harness.addToBattlefield(player1, new VesselOfNascency());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(planeswalker, instant));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice.params().cards()).containsExactly(planeswalker);

        chooseCard(-1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(planeswalker, instant);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void chooseCard(int index) {
        harness.getGameService().handleInteractionAnswer(
                harness.getGameData(), player1, new InteractionAnswer.LibraryCardChosen(index));
    }

    private static Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }
}
