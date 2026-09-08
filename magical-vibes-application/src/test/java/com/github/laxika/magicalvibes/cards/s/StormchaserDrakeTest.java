package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormchaserDrake.class, GiantGrowth.class})
class StormchaserDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when its controller's spell targets it")
    void drawsWhenOwnSpellTargetsIt() {
        Permanent drake = addCreatureReady(player1, new StormchaserDrake());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castInstant(player1, 0, drake.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Does not draw when an opponent's spell targets it")
    void doesNotDrawWhenOpponentSpellTargetsIt() {
        Permanent drake = addCreatureReady(player1, new StormchaserDrake());
        harness.setHand(player1, List.of());

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, drake.getId());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
