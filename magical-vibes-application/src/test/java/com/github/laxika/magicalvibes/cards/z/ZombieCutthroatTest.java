package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ZombieCutthroat.class)
class ZombieCutthroatTest extends BaseCardTest {

    @Test
    void turnsFaceUpByPayingFiveLife() {
        Permanent cutthroat = castFaceDown();
        harness.setLife(player1, 10);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cutthroat));

        assertThat(cutthroat.isFaceDown()).isFalse();
        harness.assertLife(player1, 5);
    }

    @Test
    void cannotTurnFaceUpWithoutFiveLife() {
        Permanent cutthroat = castFaceDown();
        harness.setLife(player1, 4);

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(cutthroat)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        assertThat(cutthroat.isFaceDown()).isTrue();
        harness.assertLife(player1, 4);
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new ZombieCutthroat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Zombie Cutthroat");
    }
}
