package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeartfireHero.class, GiantGrowth.class, Shock.class, ProdigalPyromancer.class})
class HeartfireHeroTest extends BaseCardTest {

    @Test
    void valiantPutsOnlyOneCounterOnTheFirstSpellOrAbilityYouControlEachTurn() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeartfireHero());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, hero.getId());
        resolveAllTriggers();

        harness.castInstant(player1, 0, hero.getId());
        resolveAllTriggers();

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void valiantDoesNotTriggerForAnOpponentsSpell() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeartfireHero());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, hero.getId());
        resolveAllTriggers();

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
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Heartfire Hero");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void controlledActivatedAbilityTriggersValiantBeforeItsDamageResolves() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeartfireHero());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player1, 1, null, hero.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Heartfire Hero");
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentsTargetingDoesNotConsumeValiantForTheTurn() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeartfireHero());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, hero.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, hero.getId());
        resolveAllTriggers();

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void valiantCanTriggerAgainOnTheOpponentsTurn() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeartfireHero());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.setLibrary(player2, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, hero.getId());
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, hero.getId());
        resolveAllTriggers();

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void deathDamageIncludesTemporaryPowerBoostAndDoesNotDamageController() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeartfireHero());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, hero.getId());
        resolveAllTriggers();

        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        for (int i = 0; i < 3; i++) {
            harness.castInstant(player2, 0, hero.getId());
            resolveAllTriggers();
        }

        harness.assertInGraveyard(player1, "Heartfire Hero");
        harness.assertLife(player2, 15);
        harness.assertLife(player1, 20);
    }
}
