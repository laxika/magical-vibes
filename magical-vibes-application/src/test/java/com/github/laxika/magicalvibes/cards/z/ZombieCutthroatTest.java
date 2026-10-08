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

    @Test
    void canPayExactlyFiveLifeToTurnFaceUp() {
        Permanent cutthroat = castFaceDown();
        harness.setLife(player1, 5);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cutthroat));

        assertThat(cutthroat.isFaceDown()).isFalse();
        harness.assertLife(player1, 0);
    }

    @Test
    void turningFaceUpNeedsNoManaAndDoesNotUseTheStack() {
        Permanent cutthroat = castFaceDown();
        harness.setLife(player1, 10);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cutthroat));

        assertThat(cutthroat.isFaceDown()).isFalse();
        harness.assertLife(player1, 5);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void castingFaceDownDoesNotRequireOrPayLife() {
        harness.setLife(player1, 4);

        Permanent cutthroat = castFaceDown();

        assertThat(cutthroat.isFaceDown()).isTrue();
        harness.assertLife(player1, 4);
    }

    @Test
    void castingNormallyDoesNotPayTheMorphLifeCost() {
        harness.setLife(player1, 4);
        harness.castFromHand(player1, new ZombieCutthroat(), "{3}{B}{B}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Zombie Cutthroat").isFaceDown()).isFalse();
        harness.assertLife(player1, 4);
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new ZombieCutthroat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Zombie Cutthroat");
    }
}
