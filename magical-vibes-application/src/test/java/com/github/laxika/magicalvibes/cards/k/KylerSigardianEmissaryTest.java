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

    @Test
    void countsEveryCounterIncludingMultipleCountersOfTheSameType() {
        Permanent kyler = addCreatureReady(player1, new KylerSigardianEmissary());
        kyler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        kyler.setCounterCount(CounterType.VIGILANCE, 1);
        kyler.setCounterCount(CounterType.STUN, 2);
        Permanent human = addCreatureReady(player1, new FugitiveWizard());
        Permanent opposingHuman = addCreatureReady(player2, new FugitiveWizard());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opposingHuman)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingHuman)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, kyler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kyler)).isEqualTo(4);
    }

    @Test
    void givesOtherHumansABonusWithNoPlusOneCounters() {
        Permanent kyler = addCreatureReady(player1, new KylerSigardianEmissary());
        kyler.setCounterCount(CounterType.VIGILANCE, 1);
        Permanent human = addCreatureReady(player1, new FugitiveWizard());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
    }

    @Test
    void bonusUpdatesWhenCountersChangeAndEndsWhenKylerLeaves() {
        Permanent kyler = addCreatureReady(player1, new KylerSigardianEmissary());
        Permanent human = addCreatureReady(player1, new FugitiveWizard());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
        kyler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(4);
        kyler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
        gd.playerBattlefields.get(player1.getId()).remove(kyler);
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForItsOwnEntry() {
        harness.setHand(player1, List.of(new KylerSigardianEmissary()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent kyler = findPermanent(player1, "Kyler, Sigardian Emissary");
        assertThat(kyler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
