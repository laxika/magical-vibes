package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

class SpreadingFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Divides 6 damage among target creatures")
    void dividesDamageAmongCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SpreadingFlames()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstant(player1, 0, Map.of(
                first.getId(), 2,
                second.getId(), 2,
                third.getId(), 2
        ));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(first.getId()))
                .noneMatch(p -> p.getId().equals(second.getId()))
                .anyMatch(p -> p.getId().equals(third.getId()) && p.getMarkedDamage() == 2);
    }

    @Test
    @DisplayName("Cannot assign damage to a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new SpreadingFlames()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(player2.getId(), 6))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage assignments must sum to 6")
    void damageAssignmentsMustSumToSix() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SpreadingFlames()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(target.getId(), 5))
        ).isInstanceOf(IllegalStateException.class);
    }
}
