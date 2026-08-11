package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArdentSoldierTest extends BaseCardTest {

    @Test
    void castWithoutKickerDoesNotPutOnACounter() {
        harness.setHand(player1, List.of(new ArdentSoldier()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent soldier = findSoldier(player1);
        assertThat(soldier).isNotNull();
        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castWithKickerEntersWithOnePlusOneCounter() {
        harness.setHand(player1, List.of(new ArdentSoldier()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent soldier = findSoldier(player1);
        assertThat(soldier).isNotNull();
        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void castWithKickerRequiresAdditionalTwoMana() {
        harness.setHand(player1, List.of(new ArdentSoldier()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent findSoldier(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Ardent Soldier"))
                .findFirst()
                .orElse(null);
    }
}
