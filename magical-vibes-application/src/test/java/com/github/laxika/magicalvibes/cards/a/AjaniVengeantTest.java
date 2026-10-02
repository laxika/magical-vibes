package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AjaniVengeant.class, GrizzlyBears.class, Mountain.class, Forest.class})
class AjaniVengeantTest extends BaseCardTest {


    @Test
    @DisplayName("+1 sets skipUntapCount on target permanent and adds loyalty")
    void plusOneSkipsUntapOfTarget() {
        Permanent ajani = addReadyAjani(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(4); // 3 + 1
    }


    @Test
    @DisplayName("-2 deals 3 damage to target player, controller gains 3 life, loses 2 loyalty")
    void minusTwoDamagesPlayerAndGainsLife() {
        Permanent ajani = addReadyAjani(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        GameData g = harness.getGameData();
        assertThat(g.playerLifeTotals.get(player2.getId())).isEqualTo(17); // 20 - 3
        assertThat(g.playerLifeTotals.get(player1.getId())).isEqualTo(23); // 20 + 3
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(1); // 3 - 2
    }

    @Test
    @DisplayName("-2 deals 3 damage to target creature and controller still gains 3 life")
    void minusTwoKillsCreatureAndGainsLife() {
        addReadyAjani(player1);
        harness.setLife(player1, 20);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        GameData g = harness.getGameData();
        assertThat(g.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(g.playerLifeTotals.get(player1.getId())).isEqualTo(23); // 20 + 3
    }


    @Test
    @DisplayName("-7 destroys all lands the target player controls but not the controller's")
    void minusSevenDestroysTargetPlayersLands() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setCounterCount(CounterType.LOYALTY, 7);
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new Mountain());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData g = harness.getGameData();
        // Target player's lands destroyed
        assertThat(g.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().hasType(com.github.laxika.magicalvibes.model.CardType.LAND));
        assertThat(g.playerGraveyards.get(player2.getId())).hasSize(2);
        // Controller's own land untouched
        assertThat(g.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mountain"));
    }

    @Test
    @DisplayName("-7 spares the target player's nonland permanents")
    void minusSevenSparesTargetPlayersNonlands() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setCounterCount(CounterType.LOYALTY, 7);
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData g = harness.getGameData();
        assertThat(g.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mountain"));
        assertThat(g.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    void plusOneDoesNotTapUntappedLandAndOnlySkipsOneUntapStep() {
        addReadyAjani(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
        land.tap();
        harness.performUntapStep(player1);
        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void plusOneExpiresEvenIfTargetIsUntappedDuringItsNextUntapStep() {
        addReadyAjani(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();
        harness.performUntapStep(player2);
        land.tap();
        harness.performUntapStep(player2);

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void minusTwoDamagesPlaneswalkerAndGainsLife() {
        addReadyAjani(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AjaniVengeant());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player1, 23);
    }

    @Test
    void minusTwoGainsThreeLifeEvenWhenAllDamageIsPrevented() {
        addReadyAjani(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setDamagePreventionShield(3);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertLife(player1, 23);
    }

    @Test
    void minusTwoDoesNotGainLifeWhenItsOnlyTargetLeavesBattlefield() {
        Permanent ajani = addReadyAjani(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    private Permanent addReadyAjani(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AjaniVengeant());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
