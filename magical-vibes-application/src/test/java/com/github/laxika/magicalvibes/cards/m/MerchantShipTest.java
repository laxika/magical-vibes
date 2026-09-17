package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MerchantShip.class, Island.class, StoneRain.class})
class MerchantShipTest extends BaseCardTest {

    @Test
    @DisplayName("Is sacrificed when its controller controls no Islands")
    void sacrificedWhenNoIslands() {
        harness.castFromHand(player1, new MerchantShip(), "{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Merchant Ship");
        harness.assertInGraveyard(player1, "Merchant Ship");
    }

    @Test
    @DisplayName("Survives while its controller controls an Island")
    void survivesWithIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.castFromHand(player1, new MerchantShip(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Merchant Ship");
    }

    @Test
    @DisplayName("Is sacrificed when its last Island leaves the battlefield")
    void sacrificesWhenLastIslandLeavesBattlefield() {
        var island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent ship = addCreatureReady(player1, new MerchantShip());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, island.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ship);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Merchant Ship");
        harness.assertInGraveyard(player1, "Merchant Ship");
    }

    @Test
    @DisplayName("Cannot attack a player who controls no Islands")
    void cannotAttackWhenDefenderHasNoIsland() {
        harness.addToBattlefield(player1, new Island());
        Permanent ship = addCreatureReady(player1, new MerchantShip());

        assertThatThrownBy(() -> declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(ship))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gains 2 life when it attacks and is not blocked")
    void gainsLifeWhenUnblocked() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        Permanent ship = addCreatureReady(player1, new MerchantShip());

        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(ship)));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }
}
