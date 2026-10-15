package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReapersScythe.class, GrizzlyBears.class})
class ReapersScytheTest extends BaseCardTest {

    @Test
    void addsOneSoulCounterForEachPlayerWhoLostLifeThisTurn() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ReapersScythe());
        gd.lifeLostThisTurn.put(player1.getId(), 3);
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(scythe.getCounterCount(CounterType.SOUL)).isEqualTo(2);
    }

    @Test
    void soulCountersBoostAndMakeTheEquippedCreatureAnAssassin() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ReapersScythe());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        scythe.setAttachedTo(creature.getId());
        scythe.setCounterCount(CounterType.SOUL, 2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.ASSASSIN)).isTrue();
    }

    @Test
    void equipTwoAttachesTheScytheToAcreatureYouControl() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ReapersScythe());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scythe.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void enteringCreatesAHeroAndAttachesTheScythe() {
        harness.castFromHand(player1, new ReapersScythe(), "{2}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent scythe = findPermanent(player1, "Reaper's Scythe");
        assertThat(countPermanents(player1, "Hero")).isEqualTo(1);
        Permanent hero = findPermanent(player1, "Hero");
        assertThat(scythe.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO, CardSubtype.ASSASSIN);
    }

    @Test
    void addsNoCountersWhenNobodyLostLife() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ReapersScythe());
        scythe.setCounterCount(CounterType.SOUL, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(scythe.getCounterCount(CounterType.SOUL)).isEqualTo(3);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ReapersScythe());
        gd.lifeLostThisTurn.put(player2.getId(), 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(scythe.getCounterCount(CounterType.SOUL)).isZero();
    }

    @Test
    void countsPlayersWhoLostLifeBeforeTheEndStepTriggerResolves() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ReapersScythe());
        scythe.setCounterCount(CounterType.SOUL, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        gd.lifeLostThisTurn.put(player2.getId(), 7);
        resolveAllTriggers();

        assertThat(scythe.getCounterCount(CounterType.SOUL)).isEqualTo(3);
    }

    @Test
    void movingEquipmentTransfersTheBonusAndPreservesExistingCreatureTypes() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ReapersScythe());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        scythe.setAttachedTo(first.getId());
        scythe.setCounterCount(CounterType.SOUL, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(scythe.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).contains(CardSubtype.BEAR)
                .doesNotContain(CardSubtype.ASSASSIN);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second))
                .contains(CardSubtype.BEAR, CardSubtype.ASSASSIN);
    }
}
