package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.ChildOfAlara;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YavimayaKavu;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({JaredCarthalion.class, ChildOfAlara.class, GrizzlyBears.class, YavimayaKavu.class})
class JaredCarthalionTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates an all-color Kavu with trample")
    void plusOneCreatesAllColorKavu() {
        Permanent jared = addReadyJared(player1, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(jared.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        Permanent kavu = findPermanents(player1, "Kavu").getFirst();
        assertThat(kavu.getCard().getPower()).isEqualTo(3);
        assertThat(kavu.getCard().getToughness()).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, kavu)).containsExactlyInAnyOrder(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
        assertThat(gqs.hasKeyword(gd, kavu, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("-3 puts each target's color count in +1/+1 counters on it")
    void minusThreeUsesEachTargetsColorCount() {
        Permanent jared = addReadyJared(player1, 5);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new YavimayaKavu());

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(bears.getId(), kavu.getId()));
        harness.passBothPriorities();

        assertThat(jared.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(kavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("-6 returns a multicolored card without the all-colors rider")
    void minusSixReturnsMulticoloredCardWithoutRider() {
        addReadyJared(player1, 6);
        Card kavu = new YavimayaKavu();
        harness.setGraveyard(player1, List.of(kavu));

        harness.activateAbility(player1, 0, 2, null, kavu.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Yavimaya Kavu");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("-6 draws and creates Treasures when the returned card is all colors")
    void minusSixAppliesAllColorsRider() {
        addReadyJared(player1, 6);
        Card child = new ChildOfAlara();
        harness.setGraveyard(player1, List.of(child));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, child.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Child of Alara");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    private Permanent addReadyJared(Player player, int loyalty) {
        Permanent perm = new Permanent(new JaredCarthalion());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
