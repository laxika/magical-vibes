package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BasrisSolidarity;
import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.f.FinishingBlow;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.i.InvigoratingSurge;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WildwoodScourge;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConclaveMentor.class, JungleDelver.class, DiabolicEdict.class,
        BasrisSolidarity.class, FinishingBlow.class, GloriousAnthem.class, InvigoratingSurge.class,
        Shock.class, WildwoodScourge.class})
class ConclaveMentorTest extends BaseCardTest {

    @Test
    void addsAnExtraPlusOneCounter() {
        Permanent delver = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        harness.addToBattlefield(player1, new ConclaveMentor());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(delver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void gainsLifeEqualToItsPowerWhenItDies() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new ConclaveMentor());
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mentor);
    }

    @Test
    void usesItsLastKnownPowerWhenItDies() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new ConclaveMentor());
        mentor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void addsCountersToItselfAndEachControlledCreature() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new ConclaveMentor());
        Permanent otherMentor = harness.addToBattlefieldAndReturn(player1, new ConclaveMentor());

        harness.castFromHand(player1, new BasrisSolidarity(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(otherMentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void addsOnePerCounterPlacementRatherThanOnePerCounter() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new ConclaveMentor());
        mentor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new InvigoratingSurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, mentor.getId());

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    void doesNotAddCountersToOpponentsCreatures() {
        harness.addToBattlefield(player1, new ConclaveMentor());
        Permanent scourge = harness.addToBattlefieldAndReturn(player2, new WildwoodScourge());
        scourge.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new BasrisSolidarity(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(scourge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void addsOneToCountersOnAnEnteringCreature() {
        harness.addToBattlefield(player1, new ConclaveMentor());
        harness.setHand(player1, List.of(new WildwoodScourge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).get(1)
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void doesNotReplaceZeroCountersOnAnEnteringCreature() {
        harness.addToBattlefield(player1, new ConclaveMentor());
        harness.setHand(player1, List.of(new WildwoodScourge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof WildwoodScourge);
    }

    @Test
    void includesStaticPowerBonusesInLifeGainedOnDeath() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new ConclaveMentor());
        harness.setHand(player1, List.of(new FinishingBlow()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, mentor.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mentor);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void deathTriggerGainsLifeForItsControllerAndUsesTheStack() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player2, new ConclaveMentor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, mentor.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(mentor);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
