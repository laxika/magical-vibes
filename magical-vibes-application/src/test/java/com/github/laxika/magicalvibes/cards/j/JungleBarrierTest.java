package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(JungleBarrier.class)
class JungleBarrierTest extends BaseCardTest {

    @Test
    @DisplayName("ETB ability draws one card")
    void etbDrawsOneCard() {
        JungleBarrier drawnCard = new JungleBarrier();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new JungleBarrier(), "{2}{G}{U}");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Defender prevents Jungle Barrier from attacking")
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new JungleBarrier());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
