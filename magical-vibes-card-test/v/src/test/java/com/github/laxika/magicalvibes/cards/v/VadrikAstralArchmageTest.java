package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.s.SecretsOfTheKey;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VadrikAstralArchmage.class, Divination.class, SecretsOfTheKey.class})
class VadrikAstralArchmageTest extends BaseCardTest {

    @Test
    void becomesDayAsItEntersWhenThereIsNoDesignation() {
        harness.setHand(player1, List.of(new VadrikAstralArchmage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
    }

    @Test
    void reducesInstantAndSorceryCostsByItsPower() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new VadrikAstralArchmage());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void usesItsUpdatedPowerForTheCostReduction() {
        gd.dayNight = DayNight.DAY;
        Permanent vadrik = harness.addToBattlefieldAndReturn(player1, new VadrikAstralArchmage());
        vadrik.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void putsCounterOnItWhenDayBecomesNight() {
        gd.dayNight = DayNight.DAY;
        Permanent vadrik = harness.addToBattlefieldAndReturn(player1, new VadrikAstralArchmage());

        finishTurnAndResolveTrigger();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);

        assertThat(vadrik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsCounterOnItWhenNightBecomesDay() {
        gd.dayNight = DayNight.NIGHT;
        gd.recordSpellCast(player1.getId(), new SecretsOfTheKey());
        gd.recordSpellCast(player1.getId(), new SecretsOfTheKey());
        Permanent vadrik = harness.addToBattlefieldAndReturn(player1, new VadrikAstralArchmage());

        finishTurnAndResolveTrigger();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);

        assertThat(vadrik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void enteringAtNightDoesNotMakeItDayOrPutACounterOnVadrik() {
        gd.dayNight = DayNight.NIGHT;
        harness.setHand(player1, List.of(new VadrikAstralArchmage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void firstDayDesignationDoesNotTriggerTheCounterAbility() {
        harness.setHand(player1, List.of(new VadrikAstralArchmage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reducesFlashbackCostsForInstantSpells() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new VadrikAstralArchmage());
        harness.setGraveyard(player1, List.of(new SecretsOfTheKey()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void excessPowerDoesNotReduceColoredManaCosts() {
        gd.dayNight = DayNight.DAY;
        Permanent vadrik = harness.addToBattlefieldAndReturn(player1, new VadrikAstralArchmage());
        vadrik.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.setHand(player1, List.of(new SecretsOfTheKey()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotReduceAnOpponentsSpells() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new VadrikAstralArchmage());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castSorcery(player2, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotReduceCreatureSpellCosts() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new VadrikAstralArchmage());
        harness.setHand(player1, List.of(new VadrikAstralArchmage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void zeroPowerProvidesNoCostReduction() {
        gd.dayNight = DayNight.DAY;
        Permanent vadrik = harness.addToBattlefieldAndReturn(player1, new VadrikAstralArchmage());
        vadrik.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
    private void finishTurnAndResolveTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
