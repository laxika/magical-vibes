package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RanklesPrank.class, GrizzlyBears.class})
class RanklesPrankTest extends BaseCardTest {

    @Test
    @DisplayName("Discard mode makes each player discard two cards")
    void eachPlayerDiscardsTwoCards() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new RanklesPrank(), new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        castModes(0);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Life-loss mode makes each player lose four life")
    void eachPlayerLosesFourLife() {
        harness.setHand(player1, List.of(new RanklesPrank()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        castModes(1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(6);
    }

    @Test
    @DisplayName("Sacrifice mode lets each player choose two creatures")
    void eachPlayerSacrificesTwoChosenCreatures() {
        List<Permanent> ownCreatures = addCreatures(player1, 3);
        List<Permanent> opponentCreatures = addCreatures(player2, 3);
        harness.setHand(player1, List.of(new RanklesPrank()));
        castModes(2);

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice).isNotNull();
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Choice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1,
                List.of(ownCreatures.get(0).getId(), ownCreatures.get(1).getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player2,
                List.of(opponentCreatures.get(0).getId(), opponentCreatures.get(1).getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("All selected modes resolve")
    void allModesResolve() {
        addCreatures(player1, 3);
        addCreatures(player2, 3);
        harness.setHand(player1, new ArrayList<>(List.of(
                new RanklesPrank(), new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 1, 2}, List.of(), null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice).isNotNull();
        harness.handleMultiplePermanentsChosen(player1,
                gd.playerBattlefields.get(player1.getId()).stream()
                        .map(Permanent::getId).limit(2).toList());
        harness.handleMultiplePermanentsChosen(player2,
                gd.playerBattlefields.get(player2.getId()).stream()
                        .map(Permanent::getId).limit(2).toList());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    private void castModes(int mode) {
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{mode}, List.of(), null);
        harness.passBothPriorities();
    }

    private List<Permanent> addCreatures(Player player, int count) {
        List<Permanent> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(harness.addToBattlefieldAndReturn(player, new GrizzlyBears()));
        }
        return creatures;
    }
}
