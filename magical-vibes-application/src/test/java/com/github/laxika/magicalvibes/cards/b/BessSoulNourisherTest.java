package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BessSoulNourisher.class, FugitiveWizard.class, GrizzlyBears.class, RaiseTheAlarm.class})
class BessSoulNourisherTest extends BaseCardTest {

    @Test
    void putsOneCounterOnBessForABatchOfBaseOneOneTokens() {
        Permanent bess = addCreatureReady(player1, new BessSoulNourisher());

        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(bess.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
    }

    @Test
    void doesNotTriggerForANonBaseOneOneCreature() {
        Permanent bess = addCreatureReady(player1, new BessSoulNourisher());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(bess.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackBoostsOtherBaseOneOneCreaturesByBessCounters() {
        Permanent bess = addCreatureReady(player1, new BessSoulNourisher());
        bess.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        wizard.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        int bessIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bess);
        declareAttackers(List.of(bessIndex));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bess)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bess)).isEqualTo(3);
    }
}
