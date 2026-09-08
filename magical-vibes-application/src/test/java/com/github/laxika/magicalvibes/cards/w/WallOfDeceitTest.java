package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WallOfDeceit.class)
class WallOfDeceitTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new WallOfDeceit()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent wall = findPermanent(player1, "Wall of Deceit");
        assertThat(wall.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(wall));
        harness.passBothPriorities();

        assertThat(wall.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(5);
    }

    @Test
    void canTurnItselfFaceDownForThreeMana() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfDeceit());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
