package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;




@CardUsed({WrennAndSix.class, Forest.class, Shock.class})
class WrennAndSixTest extends BaseCardTest {

    @Test
    @DisplayName("+1 returns a target land card from the graveyard to hand")
    void plusOneReturnsTargetLand() {
        Permanent wrenn = addReadyWrenn(player1, 5);
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        harness.activateAbility(player1, 0, 0, null, forest.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("-1 deals 1 damage to any target")
    void minusOneDealsDamageToAnyTarget() {
        Permanent wrenn = addReadyWrenn(player1, 5);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("-7 grants retrace to instants and sorceries in the graveyard")
    void minusSevenGrantsRetrace() {
        Permanent wrenn = addReadyWrenn(player1, 7);
        Card shock = new Shock();
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(forest));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);

        harness.castRetrace(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Forest");
    }

    private Permanent addReadyWrenn(Player player, int loyalty) {
        Permanent permanent = new Permanent(new WrennAndSix());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}

@CardUsed({WrennAndSix.class, Forest.class, Shock.class, GrizzlyBears.class})
class Mh1WrennAndSixTest extends BaseCardTest {

    @Test
    @DisplayName("+1 returns a target land card from the graveyard to hand")
    void plusOneReturnsLand() {
        Permanent wrenn = addReadyWrenn(player1, 5);
        Card forest = new Forest();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(forest, shock));

        harness.activateAbility(player1, 0, 0, null, forest.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("-1 deals 1 damage to any target")
    void minusOneDealsDamage() {
        Permanent wrenn = addReadyWrenn(player1, 5);
        Permanent shockTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, shockTarget.getId());
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(shockTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("-7 grants retrace to instant and sorcery cards in your graveyard")
    void minusSevenGrantsRetrace() {
        Permanent wrenn = addReadyWrenn(player1, 7);
        Card shock = new Shock();
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(forest));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isZero();

        harness.castRetrace(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Forest");
    }

    private Permanent addReadyWrenn(Player player, int loyalty) {
        Permanent permanent = new Permanent(new WrennAndSix());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
