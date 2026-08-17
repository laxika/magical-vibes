package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ForkedBoltTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToOneTarget() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, Map.of(player2.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void dividesDamageAmongTwoTargets() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, Map.of(bears.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gameData.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(bears.getId())
                        && permanent.getMarkedDamage() == 1);
    }

    @Test
    void cannotChooseMoreThanTwoTargets() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent bears1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bears3 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(
                bears1.getId(), 1,
                bears2.getId(), 1,
                bears3.getId(), 1
        ))).isInstanceOf(IllegalStateException.class);
    }
}
