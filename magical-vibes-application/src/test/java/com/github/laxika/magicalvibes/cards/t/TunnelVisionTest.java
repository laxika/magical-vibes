package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HuntedHorror;
import com.github.laxika.magicalvibes.cards.h.HuntedLammasu;
import com.github.laxika.magicalvibes.cards.h.HuntedPhantasm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TunnelVision.class, HuntedPhantasm.class, HuntedLammasu.class, HuntedHorror.class})
class TunnelVisionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving prompts the controller to name a card")
    void resolvingPromptsControllerToNameCard() {
        harness.setLibrary(player2, List.of(new HuntedLammasu(), new HuntedHorror()));
        castTunnelVision();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context())
                .isInstanceOf(ChoiceContext.ChooseNameRevealUntilNamedPutOnTopRestToGraveyardChoice.class);
    }

    @Test
    @DisplayName("Puts the named card on top and the other revealed cards into the target's graveyard")
    void foundCardGoesOnTopAndOtherRevealedCardsGoToGraveyard() {
        Card first = new HuntedPhantasm();
        Card chosen = new HuntedLammasu();
        Card tail = new HuntedHorror();
        harness.setLibrary(player2, List.of(first, chosen, tail));

        castTunnelVision();
        harness.handleListChoice(player1, chosen.getName());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(chosen, tail);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(chosen, tail);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Shuffles the target's library when the named card is not found")
    void missingCardShufflesLibrary() {
        Card first = new HuntedPhantasm();
        Card second = new HuntedLammasu();
        harness.setLibrary(player2, List.of(first, second));

        castTunnelVision();
        harness.handleListChoice(player1, "Tunnel Vision");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Leaves an empty target library empty when the named card is not found")
    void emptyLibraryRemainsEmptyWhenCardIsNotFound() {
        harness.setLibrary(player2, List.of());

        castTunnelVision();
        harness.handleListChoice(player1, "Tunnel Vision");

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Stops at the first matching copy and leaves later copies untouched")
    void stopsAtFirstMatchingCopy() {
        Card first = new HuntedPhantasm();
        Card chosen = new HuntedLammasu();
        Card laterCopy = new HuntedLammasu();
        Card tail = new HuntedHorror();
        harness.setLibrary(player2, List.of(first, chosen, laterCopy, tail));

        castTunnelVision();
        harness.handleListChoice(player1, chosen.getName());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(chosen, laterCopy, tail);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("A matching top card leaves the entire library unchanged")
    void matchingTopCardLeavesLibraryUnchanged() {
        Card chosen = new HuntedLammasu();
        Card tail = new HuntedHorror();
        harness.setLibrary(player2, List.of(chosen, tail));

        castTunnelVision();
        harness.handleListChoice(player1, chosen.getName());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(chosen, tail);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target its controller and find the bottom card")
    void canTargetControllerAndFindBottomCard() {
        Card first = new HuntedPhantasm();
        Card chosen = new HuntedLammasu();
        harness.setLibrary(player1, List.of(first, chosen));
        harness.setHand(player1, List.of(new TunnelVision()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleListChoice(player1, chosen.getName());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen);
        harness.assertInGraveyard(player1, first.getName());
        harness.assertInGraveyard(player1, "Tunnel Vision");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Allows choosing a real card name that is absent from the game")
    void canNameCardAbsentFromGame() {
        Card card = new HuntedHorror();
        harness.setLibrary(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(card));

        castTunnelVision();
        harness.handleListChoice(player1, "Hunted Lammasu");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Name options do not disclose the contents of the opponent's hidden hand")
    void nameOptionsDoNotDependOnOpponentsHiddenHand() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new HuntedHorror()));
        castTunnelVision();
        List<String> originalOptions = gd.interaction
                .activeInteraction(PendingInteraction.ColorChoice.class).options();
        harness.handleListChoice(player1, "Tunnel Vision");

        harness.setHand(player2, List.of(new HuntedLammasu()));
        castTunnelVision();
        List<String> changedOptions = gd.interaction
                .activeInteraction(PendingInteraction.ColorChoice.class).options();

        assertThat(changedOptions).containsExactlyElementsOf(originalOptions);
    }

    private void castTunnelVision() {
        harness.setHand(player1, List.of(new TunnelVision()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
