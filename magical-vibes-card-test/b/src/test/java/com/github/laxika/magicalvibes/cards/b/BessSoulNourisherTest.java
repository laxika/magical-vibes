package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LivingDeath;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BessSoulNourisher.class, FugitiveWizard.class, GrizzlyBears.class, RaiseTheAlarm.class, LivingDeath.class})
class BessSoulNourisherTest extends BaseCardTest {

    @Test
    void putsOneCounterOnBessForABatchOfBaseOneOneTokens() {
        Permanent bess = addCreatureReady(player1, new BessSoulNourisher());

        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);
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

    @Test
    void putsACounterOnBessForANontokenBaseOneOneCreature() {
        Permanent bess = addCreatureReady(player1, new BessSoulNourisher());
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(bess.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForItsOwnEntry() {
        harness.setHand(player1, List.of(new BessSoulNourisher()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Bess, Soul Nourisher")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForOpponentsTokens() {
        Permanent bess = addCreatureReady(player1, new BessSoulNourisher());
        harness.setHand(player2, List.of(new RaiseTheAlarm()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0);
        resolveAllTriggers();

        assertThat(bess.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player2, "Soldier")).hasSize(2);
    }

    @Test
    void separateEntryEventsEachPutACounterOnBess() {
        Permanent bess = addCreatureReady(player1, new BessSoulNourisher());
        harness.setHand(player1, List.of(new RaiseTheAlarm(), new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveInstant(player1, 0);
        resolveAllTriggers();
        harness.castAndResolveInstant(player1, 0);
        resolveAllTriggers();

        assertThat(bess.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Soldier")).hasSize(4);
    }

    @Test
    void simultaneousNontokenEntriesPutOnlyOneCounterOnBess() {
        harness.setGraveyard(player1, List.of(new BessSoulNourisher(),
                new FugitiveWizard(), new FugitiveWizard()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new LivingDeath()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Fugitive Wizard")).hasSize(2);
        assertThat(findPermanent(player1, "Bess, Soul Nourisher")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void attackDoesNotBoostOpponentsBaseOneOneCreatures() {
        Permanent bess = addCreatureReady(player1, new BessSoulNourisher());
        bess.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent wizard = addCreatureReady(player2, new FugitiveWizard());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(1);
    }
}
