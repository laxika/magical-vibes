package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeartfireHero.class, GiantGrowth.class, Shock.class})
class HeartfireHeroTest extends BaseCardTest {

    @Test
    void valiantPutsOnlyOneCounterOnTheFirstSpellOrAbilityYouControlEachTurn() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeartfireHero());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, hero.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castInstant(player1, 0, hero.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void valiantDoesNotTriggerForAnOpponentsSpell() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeartfireHero());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, hero.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void deathTriggerDealsLastKnownPowerToEachOpponent() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeartfireHero());
        hero.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, hero.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Heartfire Hero");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
