package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwinBolt.class, GrizzlyBears.class, Forest.class})
class TwinBoltTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToOneTarget() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new TwinBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, Map.of(player2.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void dividesDamageAmongTwoTargets() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new TwinBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, Map.of(bears.getId(), 1, player2.getId(), 1));
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
        harness.setHand(player1, List.of(new TwinBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        Permanent bears1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bears3 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(
                bears1.getId(), 1,
                bears2.getId(), 1,
                bears3.getId(), 1
        ))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dealsLethalDamageToOneCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new TwinBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castInstant(player1, 0, Map.of(bears.getId(), 2));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Twin Bolt");
    }

    @Test
    void canDivideDamageBetweenBothPlayers() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new TwinBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, Map.of(player1.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    void cannotAssignZeroDamageToATarget() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new TwinBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                Map.of(player1.getId(), 0, player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotAssignLessThanTwoTotalDamage() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new TwinBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotAssignMoreThanTwoTotalDamage() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new TwinBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetANoncreatureLand() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new TwinBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(forest.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotRedistributeDamageWhenOneTargetLeavesTheBattlefield() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new TwinBolt(), new TwinBolt()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castInstant(player1, 0, Map.of(bears.getId(), 1, player2.getId(), 1));
        harness.castInstant(player1, 0, Map.of(bears.getId(), 2));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof TwinBolt).hasSize(2);
    }
}
