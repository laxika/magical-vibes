package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MournwhelkTest extends BaseCardTest {

    private void castMournwhelk(java.util.UUID targetPlayerId) {
        harness.setHand(player1, new ArrayList<>(List.of(new Mournwhelk())));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.getGameService().playCard(gd, player1, 0, 0, targetPlayerId, null);
    }

    @Test
    @DisplayName("Hardcast: ETB makes target player discard two cards and Mournwhelk stays on the battlefield")
    void hardcastDiscardsTwoAndStays() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant())));
        castMournwhelk(player2.getId());

        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mournwhelk"));
    }

    @Test
    @DisplayName("Can target itself controller (any player): controller discards two cards")
    void canTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new Mournwhelk(), new GrizzlyBears(), new HillGiant())));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Evoke: cast for {3}{B}, ETB still forces the discard, then Mournwhelk is sacrificed")
    void evokeDiscardsThenSacrifices() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant())));
        harness.setHand(player1, new ArrayList<>(List.of(new Mournwhelk())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithEvoke(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mournwhelk"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mournwhelk"));
    }

    @Test
    @DisplayName("ETB discards only the available card when target has fewer than two cards")
    void discardsWhatIsAvailable() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        castMournwhelk(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
