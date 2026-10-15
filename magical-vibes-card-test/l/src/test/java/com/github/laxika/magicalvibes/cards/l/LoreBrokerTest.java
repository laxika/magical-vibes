package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoreBroker.class, Forest.class, Watchwolf.class})
class LoreBrokerTest extends BaseCardTest {

    @Test
    @DisplayName("Each player draws before players discard in APNAP order")
    void eachPlayerDrawsThenDiscards() {
        addCreatureReady(player1, new LoreBroker());
        harness.setHand(player1, List.of(new Watchwolf()));
        harness.setHand(player2, List.of(new Watchwolf()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each player can discard the card drawn from an empty hand")
    void eachPlayerCanDiscardTheCardDrawnFromAnEmptyHand() {
        addCreatureReady(player1, new LoreBroker());
        Forest player1Draw = new Forest();
        Forest player2Draw = new Forest();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(player1Draw));
        harness.setLibrary(player2, List.of(player2Draw));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(player1Draw);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(player2Draw);
    }

    @Test
    @DisplayName("Discard choices stay hidden until both players choose, even when the nonactive player controls the ability")
    void discardsHappenSimultaneouslyAfterBothPlayersChoose() {
        var broker = addCreatureReady(player2, new LoreBroker());
        Watchwolf player1Discard = new Watchwolf();
        Watchwolf player2Discard = new Watchwolf();
        Forest player1Draw = new Forest();
        Forest player2Draw = new Forest();
        harness.setHand(player1, List.of(player1Discard));
        harness.setHand(player2, List.of(player2Discard));
        harness.setLibrary(player1, List.of(player1Draw));
        harness.setLibrary(player2, List.of(player2Draw));

        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, null);
        assertThat(broker.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1Discard, player1Draw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2Discard, player2Draw);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(player1Discard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(player2Discard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1Draw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2Draw);
    }
}
