package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KylerSigardianEmissary.class, FugitiveWizard.class, GrizzlyBears.class})
class KylerSigardianEmissaryTest extends BaseCardTest {

    @Test
    void putsACounterOnKylerWhenAnotherHumanEnters() {
        Permanent kyler = addCreatureReady(player1, new KylerSigardianEmissary());

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(kyler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForNonHumansOrOpponentsHumans() {
        Permanent kyler = addCreatureReady(player1, new KylerSigardianEmissary());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(kyler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void givesOtherHumansAPowerAndToughnessBonusEqualToKylerCounters() {
        Permanent kyler = addCreatureReady(player1, new KylerSigardianEmissary());
        kyler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent human = addCreatureReady(player1, new FugitiveWizard());
        Permanent nonHuman = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonHuman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonHuman)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, kyler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kyler)).isEqualTo(4);
    }
}
