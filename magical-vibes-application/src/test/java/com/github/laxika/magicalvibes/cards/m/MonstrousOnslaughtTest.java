package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonstrousOnslaughtTest extends BaseCardTest {

    @Test
    void dividesDamageAmongTargetCreaturesUsingGreatestControlledPower() {
        harness.addToBattlefield(player1, new HillGiant());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new MonstrousOnslaught()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, Map.of(first.getId(), 1, second.getId(), 2));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(first.getId())
                        && permanent.getMarkedDamage() == 1)
                .anyMatch(permanent -> permanent.getId().equals(second.getId())
                        && permanent.getMarkedDamage() == 2);
    }

    @Test
    void locksGreatestPowerAtCastTime() {
        Permanent strongest = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new MonstrousOnslaught()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, Map.of(target.getId(), 3));
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(strongest);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId())
                        && permanent.getMarkedDamage() == 3);
    }

    @Test
    void cannotAssignDamageToPlayer() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new MonstrousOnslaught()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void assignmentsMustSumToGreatestPower() {
        harness.addToBattlefield(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MonstrousOnslaught()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(target.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }
}
