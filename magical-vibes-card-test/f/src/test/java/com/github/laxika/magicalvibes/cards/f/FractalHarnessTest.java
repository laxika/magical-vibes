package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AdrixAndNevTwincasters;
import com.github.laxika.magicalvibes.cards.m.MagneticTheft;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FractalHarness.class, SakuraTribeElder.class, AdrixAndNevTwincasters.class, MagneticTheft.class})
class FractalHarnessTest extends BaseCardTest {

    @Test
    void createsAndAttachesFractalWithXPlusOneCounters() {
        castHarness(3);

        Permanent harnessPermanent = findPermanent(player1, "Fractal Harness");
        Permanent fractal = findPermanent(player1, "Fractal");

        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(harnessPermanent.getAttachedTo()).isEqualTo(fractal.getId());
    }

    @Test
    void doublesEquippedFractalsCountersWhenTheyAttack() {
        castHarness(2);
        Permanent fractal = findPermanent(player1, "Fractal");
        fractal.setSummoningSick(false);
        int fractalIndex = gd.playerBattlefields.get(player1.getId()).indexOf(fractal);

        declareAttackers(List.of(fractalIndex));
        resolveAllTriggers();

        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void equipAbilityCanMoveHarnessToAnotherCreature() {
        castHarness(1);
        Permanent harnessPermanent = findPermanent(player1, "Fractal Harness");
        Permanent fractal = findPermanent(player1, "Fractal");
        Permanent creature = addCreatureReady(player1, new SakuraTribeElder());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(harnessPermanent.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void zeroXTokenDiesAndLeavesHarnessUnattached() {
        castHarness(0);

        assertThat(countPermanents(player1, "Fractal")).isZero();
        assertThat(findPermanent(player1, "Fractal Harness").getAttachedTo()).isNull();
    }

    @Test
    void reequippedCreatureDoublesOnlyItsPlusOneCounters() {
        castHarness(2);
        Permanent fractal = findPermanent(player1, "Fractal");
        Permanent creature = addCreatureReady(player1, new SakuraTribeElder());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        creature.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(creature.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void attackTriggerStillDoublesTheAttackerAfterHarnessMoves() {
        castHarness(2);
        Permanent equipment = findPermanent(player1, "Fractal Harness");
        Permanent fractal = findPermanent(player1, "Fractal");
        Permanent creature = addCreatureReady(player1, new SakuraTribeElder());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        fractal.setSummoningSick(false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(fractal)));

        harness.setHand(player1, List.of(new MagneticTheft()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, List.of(equipment.getId(), creature.getId()));
        resolveAllTriggers();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void controllerChoosesWhichDoubledTokenHarnessEquips() {
        harness.addToBattlefield(player1, new AdrixAndNevTwincasters());
        castHarness(2);

        List<Permanent> fractals = findPermanents(player1, "Fractal");
        assertThat(fractals).hasSize(2);
        assertThat(fractals).allSatisfy(fractal ->
                assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, fractals.getFirst().getId());

        assertThat(findPermanent(player1, "Fractal Harness").getAttachedTo())
                .isEqualTo(fractals.getFirst().getId());
    }

    private void castHarness(int xValue) {
        harness.setHand(player1, List.of(new FractalHarness()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue + 2);
        harness.castArtifact(player1, 0, xValue);
        resolveAllTriggers();
    }
}
