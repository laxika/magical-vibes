package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TajuruPreserver;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SarkhanTheMad.class, GrizzlyBears.class, ShivanDragon.class, ChandraNalaar.class,
        TajuruPreserver.class})
class SarkhanTheMadTest extends BaseCardTest {

    @Test
    @DisplayName("0 reveals the top card, puts it into hand, and deals its mana value as damage to Sarkhan")
    void zeroRevealsAndDamagesSarkhan() {
        Permanent sarkhan = addReadySarkhan(player1, 5);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("-2 sacrifices the target creature and creates a Dragon for its controller")
    void minusTwoSacrificesAndCreatesDragon() {
        Permanent sarkhan = addReadySarkhan(player1, 5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        Permanent dragon = findPermanent(player2, "Dragon");
        assertThat(dragon.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("-4 has each Dragon deal its power to a target player")
    void minusFourDamagesTargetPlayerWithDragons() {
        Permanent sarkhan = addReadySarkhan(player1, 5);
        harness.addToBattlefield(player1, new ShivanDragon());
        harness.addToBattlefield(player1, new ShivanDragon());
        Permanent nonDragon = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        assertThat(nonDragon.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("-4 can target a planeswalker")
    void minusFourDamagesTargetPlaneswalkerWithDragons() {
        addReadySarkhan(player1, 5);
        harness.addToBattlefield(player1, new ShivanDragon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 7);
        target.setSummoningSick(false);

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-4 rejects a creature target")
    void minusFourRejectsCreatureTarget() {
        addReadySarkhan(player1, 5);
        harness.addToBattlefield(player1, new ShivanDragon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("0 with an empty library neither damages Sarkhan nor loses the game")
    void zeroWithEmptyLibrary() {
        Permanent sarkhan = addReadySarkhan(player1, 5);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertOnBattlefield(player1, "Sarkhan the Mad");
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("0 puts the revealed card into hand even when its damage kills Sarkhan")
    void zeroCanKillSarkhan() {
        addReadySarkhan(player1, 2);
        harness.setLibrary(player1, List.of(new TajuruPreserver()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Tajuru Preserver");
        harness.assertNotOnBattlefield(player1, "Sarkhan the Mad");
        harness.assertInGraveyard(player1, "Sarkhan the Mad");
    }

    @Test
    @DisplayName("-2 respects Tajuru Preserver but still creates the Dragon")
    void minusTwoCannotForceSacrificeThroughPreserver() {
        addReadySarkhan(player1, 5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TajuruPreserver());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Tajuru Preserver");
        harness.assertNotInGraveyard(player2, "Tajuru Preserver");
        assertThat(countPermanents(player2, "Dragon")).isEqualTo(1);
        assertThat(countPermanents(player1, "Dragon")).isZero();
    }

    @Test
    @DisplayName("-2 can sacrifice your own creature despite your Tajuru Preserver")
    void minusTwoCanSacrificeOwnPreserver() {
        addReadySarkhan(player1, 5);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TajuruPreserver());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tajuru Preserver");
        harness.assertNotOnBattlefield(player1, "Tajuru Preserver");
        assertThat(countPermanents(player1, "Dragon")).isEqualTo(1);
        assertThat(countPermanents(player2, "Dragon")).isZero();
    }

    @Test
    @DisplayName("-4 resolves even if paying its loyalty cost puts Sarkhan in the graveyard")
    void minusFourResolvesAfterSarkhanDiesToCost() {
        addReadySarkhan(player1, 4);
        harness.addToBattlefield(player1, new ShivanDragon());
        harness.addToBattlefield(player2, new ShivanDragon());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sarkhan the Mad");
        harness.assertLife(player2, 15);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("-4 deals no damage without Dragons you control")
    void minusFourWithoutControlledDragons() {
        addReadySarkhan(player1, 5);
        harness.addToBattlefield(player1, new TajuruPreserver());
        harness.addToBattlefield(player2, new ShivanDragon());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("-4 uses Dragon power when the ability resolves")
    void minusFourUsesCurrentPower() {
        addReadySarkhan(player1, 5);
        harness.addToBattlefield(player1, new ShivanDragon());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("-4 may target its controller")
    void minusFourCanTargetYourself() {
        addReadySarkhan(player1, 5);
        harness.addToBattlefield(player1, new ShivanDragon());

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
    }

    private Permanent addReadySarkhan(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SarkhanTheMad());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }
}
