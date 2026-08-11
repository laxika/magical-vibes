package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonasteryFlockTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUpForBlue() {
        harness.setHand(player1, List.of(new MonasteryFlock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent flock = findPermanent(player1, "Monastery Flock");
        assertThat(flock.isFaceDown()).isTrue();
        assertThat(flock.getEffectivePower()).isEqualTo(2);
        assertThat(flock.getEffectiveToughness()).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(flock));
        harness.passBothPriorities();

        assertThat(flock.isFaceDown()).isFalse();
        assertThat(flock.getEffectivePower()).isZero();
        assertThat(flock.getEffectiveToughness()).isEqualTo(5);
    }
}
