package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BodyCount.class, Forest.class})
class BodyCountTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card for each creature that died under your control this turn")
    void drawsForControllerCreatureDeaths() {
        gd.creatureDeathCountThisTurn.put(player1.getId(), 2);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new BodyCount()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1 + 2);
    }

    @Test
    @DisplayName("Does not draw for creatures that died under an opponent's control")
    void onlyCountsControllerCreatureDeaths() {
        gd.creatureDeathCountThisTurn.put(player2.getId(), 3);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        harness.setHand(player1, List.of(new BodyCount()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1);
    }

    @Test
    @DisplayName("Spectacle casts Body Count for {B} after an opponent loses life")
    void spectacleUsesAlternateCost() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        harness.setHand(player1, List.of(new BodyCount()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Spectacle is unavailable when no opponent has lost life")
    void spectacleRequiresOpponentLifeLoss() {
        harness.setHand(player1, List.of(new BodyCount()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
