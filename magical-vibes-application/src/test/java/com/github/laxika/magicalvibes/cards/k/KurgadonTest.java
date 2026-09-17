package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ElvishAberration;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.h.HangarbackWalker;
import com.github.laxika.magicalvibes.cards.r.RainOfBlades;
import com.github.laxika.magicalvibes.cards.t.TitanicBulvox;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Kurgadon.class, ElvishAberration.class, GoblinBrigand.class, RainOfBlades.class,
        TitanicBulvox.class, HangarbackWalker.class})
class KurgadonTest extends BaseCardTest {

    @Test
    void putsThreeCountersOnItselfWhenControllerCastsCreatureWithManaValueSix() {
        Permanent kurgadon = harness.addToBattlefieldAndReturn(player1, new Kurgadon());
        harness.castFromHand(player1, new ElvishAberration(), "{5}{G}");

        harness.passBothPriorities();

        assertThat(kurgadon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void putsThreeCountersOnItselfWhenControllerCastsCreatureWithManaValueAboveSix() {
        Permanent kurgadon = harness.addToBattlefieldAndReturn(player1, new Kurgadon());
        harness.castFromHand(player1, new TitanicBulvox(), "{6}{G}{G}");

        harness.passBothPriorities();

        assertThat(kurgadon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void doesNotTriggerForCreatureWithManaValueLessThanSix() {
        Permanent kurgadon = harness.addToBattlefieldAndReturn(player1, new Kurgadon());
        harness.castFromHand(player1, new GoblinBrigand(), "{1}{R}");

        harness.passBothPriorities();

        assertThat(kurgadon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForNoncreatureSpell() {
        Permanent kurgadon = harness.addToBattlefieldAndReturn(player1, new Kurgadon());
        harness.castFromHand(player1, new RainOfBlades(), "{W}");

        harness.passBothPriorities();

        assertThat(kurgadon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForOpponentCreatureSpell() {
        Permanent kurgadon = harness.addToBattlefieldAndReturn(player1, new Kurgadon());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new ElvishAberration(), "{5}{G}");

        harness.passBothPriorities();

        assertThat(kurgadon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void usesAllXSymbolsWhenCheckingTheCreatureSpellsManaValue() {
        Permanent kurgadon = harness.addToBattlefieldAndReturn(player1, new Kurgadon());
        harness.setHand(player1, List.of(new HangarbackWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(kurgadon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void doesNotTriggerForCreatureCastFaceDown() {
        Permanent kurgadon = harness.addToBattlefieldAndReturn(player1, new Kurgadon());
        harness.setHand(player1, List.of(new TitanicBulvox()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        assertThat(kurgadon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
