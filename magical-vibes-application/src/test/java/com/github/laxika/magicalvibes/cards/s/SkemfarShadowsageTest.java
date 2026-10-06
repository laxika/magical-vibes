package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElderfangDisciple;
import com.github.laxika.magicalvibes.cards.i.IcehideTroll;
import com.github.laxika.magicalvibes.cards.j.JasperaSentinel;
import com.github.laxika.magicalvibes.cards.m.MaskedVandal;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkemfarShadowsage.class, ElderfangDisciple.class, IcehideTroll.class,
        JasperaSentinel.class, MaskedVandal.class})
class SkemfarShadowsageTest extends BaseCardTest {

    private void castShadowsage() {
        harness.addToBattlefield(player1, new JasperaSentinel());
        harness.addToBattlefield(player1, new ElderfangDisciple());
        harness.addToBattlefield(player1, new IcehideTroll());
        harness.castFromHand(player1, new SkemfarShadowsage(), "{3}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void eachOpponentLosesTheLargestSharedCreatureTypeCount() {
        int opponentLifeBefore = gd.getLife(player2.getId());

        castShadowsage();
        harness.handleListChoice(player1, "Each opponent loses X life.");
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 3);
    }

    @Test
    void controllerGainsTheLargestSharedCreatureTypeCount() {
        int controllerLifeBefore = gd.getLife(player1.getId());

        castShadowsage();
        harness.handleListChoice(player1, "You gain X life.");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 3);
    }

    @Test
    void shadowsageCountsItselfOnlyOnceDespiteHavingTwoTypes() {
        harness.castFromHand(player1, new SkemfarShadowsage(), "{3}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Each opponent loses X life.");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    void changelingCountsTowardTheLargestTypeGroup() {
        harness.addToBattlefield(player1, new MaskedVandal());
        castShadowsage();
        harness.handleListChoice(player1, "You gain X life.");
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    void countsCreaturesAtResolutionAndIgnoresOpposingCreatures() {
        castShadowsage();
        harness.handleListChoice(player1, "Each opponent loses X life.");
        harness.addToBattlefield(player1, new JasperaSentinel());
        harness.addToBattlefield(player2, new JasperaSentinel());
        harness.addToBattlefield(player2, new ElderfangDisciple());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
    }

    @Test
    void largestGroupNeedNotShareATypeWithShadowsage() {
        harness.addToBattlefield(player1, new IcehideTroll());
        harness.addToBattlefield(player1, new IcehideTroll());
        harness.addToBattlefield(player1, new IcehideTroll());
        harness.castFromHand(player1, new SkemfarShadowsage(), "{3}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Each opponent loses X life.");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void triggerStillResolvesForZeroWhenNoCreaturesRemain() {
        harness.castFromHand(player1, new SkemfarShadowsage(), "{3}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "You gain X life.");
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }
}
