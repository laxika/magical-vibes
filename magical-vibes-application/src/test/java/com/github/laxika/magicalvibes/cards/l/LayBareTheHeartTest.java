package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HazeOfPollen;
import com.github.laxika.magicalvibes.cards.n.NissaStewardOfElements;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LayBareTheHeart.class, Forest.class, NissaStewardOfElements.class, Colossapede.class, HazeOfPollen.class})
class LayBareTheHeartTest extends BaseCardTest {

    @Test
    @DisplayName("Caster chooses a nonlegendary, nonland card and it is discarded")
    void choosingCardDiscardsIt() {
        harness.setHand(player2, new ArrayList<>(List.of(new Colossapede(), new HazeOfPollen())));

        harness.setHand(player1, List.of(new LayBareTheHeart()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Colossapede");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).get(0).getName()).isEqualTo("Haze of Pollen");
    }

    @Test
    @DisplayName("Legendary cards are excluded from valid choices")
    void legendaryCardsExcluded() {
        harness.setHand(player2, new ArrayList<>(List.of(new NissaStewardOfElements(), new Colossapede())));

        harness.setHand(player1, List.of(new LayBareTheHeart()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Nissa (index 0) is legendary and excluded; only Colossapede (index 1) is valid.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Land cards are excluded from valid choices")
    void landCardsExcluded() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Colossapede())));

        harness.setHand(player1, List.of(new LayBareTheHeart()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Hand of only legendary and land cards yields no valid choices")
    void onlyLegendaryAndLandNoChoices() {
        harness.setHand(player2, new ArrayList<>(List.of(new NissaStewardOfElements(), new Forest())));

        harness.setHand(player1, List.of(new LayBareTheHeart()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no valid choices"));
    }

    @Test
    @DisplayName("Cannot target self: must target an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new LayBareTheHeart(), new Colossapede())));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An empty opposing hand resolves without a choice")
    void emptyHandResolves() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new LayBareTheHeart()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Lay Bare the Heart");
    }

    @Test
    @DisplayName("The caster must choose a legal card and the opponent cannot choose")
    void rejectsIllegalChoices() {
        harness.setHand(player2, List.of(new Forest(), new NissaStewardOfElements(), new HazeOfPollen()));
        harness.setHand(player1, List.of(new LayBareTheHeart()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 2))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);

        harness.handleCardChosen(player1, 2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Haze of Pollen");
        harness.assertInHand(player2, "Forest");
        harness.assertInHand(player2, "Nissa, Steward of Elements");
    }
}
