package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScorchingSpear.class, GrizzlyBears.class, Forest.class, JaceBeleren.class})
class ScorchingSpearTest extends BaseCardTest {

    @Test
    @DisplayName("Scorching Spear deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ScorchingSpear()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Scorching Spear deals 1 damage to target creature")
    void deals1DamageToCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScorchingSpear()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Scorching Spear deals 1 damage to target planeswalker")
    void deals1DamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new ScorchingSpear()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Scorching Spear cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ScorchingSpear()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Scorching Spear goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new ScorchingSpear()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Scorching Spear");
    }
}
