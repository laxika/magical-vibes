package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        Permanent dragon = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Dragon"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(5);
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
        Permanent target = new Permanent(new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 7);
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-4 rejects a creature target")
    void minusFourRejectsCreatureTarget() {
        addReadySarkhan(player1, 5);
        harness.addToBattlefield(player1, new ShivanDragon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySarkhan(Player player, int loyalty) {
        Permanent permanent = new Permanent(new SarkhanTheMad());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }
}
