package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VolcanicWindTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new VolcanicWind()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Divides damage equal to all creatures on the battlefield among target creatures")
    void dividesBattlefieldCreatureCountDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepare();

        harness.castSorcery(player1, 0, Map.of(first.getId(), 2, second.getId(), 1));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Locks the creature count at cast time")
    void locksCreatureCountAtCastTime() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepare();

        harness.castSorcery(player1, 0, Map.of(target.getId(), 3));
        harness.getGameData().playerBattlefields.get(player2.getId()).remove(other);
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot assign damage to a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepare();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
