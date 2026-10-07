package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DimirGuildgate;
import com.github.laxika.magicalvibes.cards.d.DouserOfLights;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThoughtErasure.class, DimirGuildgate.class, DouserOfLights.class})
class ThoughtErasureTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a chosen nonland card from an opponent's hand and surveils 1")
    void discardsNonlandAndSurveils() {
        Card topCard = new DouserOfLights();
        harness.setHand(player2, List.of(new DimirGuildgate(), new DouserOfLights()));
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new ThoughtErasure()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Douser of Lights");
        harness.assertInGraveyard(player1, "Douser of Lights");
        harness.assertInGraveyard(player1, "Thought Erasure");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Dimir Guildgate");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new ThoughtErasure()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canKeepSurveilledCardOnTopAfterChoosingAmongNonlands() {
        Card chosen = new DouserOfLights();
        Card remaining = new ThoughtErasure();
        Card top = new DimirGuildgate();
        Card next = new DouserOfLights();
        harness.setHand(player2, List.of(chosen, remaining));
        harness.setLibrary(player1, List.of(top, next));
        harness.setHand(player1, List.of(new ThoughtErasure()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        harness.assertInGraveyard(player1, "Thought Erasure");
    }

    @Test
    void cannotChooseLandFromRevealedHand() {
        Card land = new DimirGuildgate();
        Card nonland = new DouserOfLights();
        harness.setHand(player2, List.of(land, nonland));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ThoughtErasure()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(nonland);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Thought Erasure");
    }

    @Test
    void surveilsEvenWhenOpponentHasOnlyLands() {
        Card land = new DimirGuildgate();
        Card top = new DouserOfLights();
        harness.setHand(player2, List.of(land));
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new ThoughtErasure()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top);
        harness.assertInGraveyard(player1, "Thought Erasure");
    }

    @Test
    void surveilsEvenWhenOpponentHasEmptyHand() {
        Card top = new DimirGuildgate();
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new ThoughtErasure()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Thought Erasure");
    }
}
