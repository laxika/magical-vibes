package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BoundingWolf;
import com.github.laxika.magicalvibes.cards.b.BirdAdmirer;
import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.cards.u.UntamedPup;
import com.github.laxika.magicalvibes.cards.w.WingShredder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoundTamer.class, UntamedPup.class, BoundingWolf.class, DawnhartRejuvenator.class,
        BirdAdmirer.class, WingShredder.class})
class HoundTamerTest extends BaseCardTest {

    @Test
    void activatedAbilityPutsCounterOnAnyTargetCreature() {
        harness.addToBattlefield(player1, new HoundTamer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BoundingWolf());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void untamedPupGivesOtherWolvesAndWerewolvesTrample() {
        gd.dayNight = DayNight.NIGHT;
        Permanent pup = harness.enterBattlefieldAndReturn(player1, new HoundTamer());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new BoundingWolf());
        Permanent nonWolf = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        Permanent werewolf = harness.enterBattlefieldAndReturn(player1, new BirdAdmirer());
        Permanent opposingWolf = harness.addToBattlefieldAndReturn(player2, new BoundingWolf());

        assertThat(pup.getCard()).isInstanceOf(UntamedPup.class);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonWolf, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, werewolf, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingWolf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void dayNightTransformsBothFaces() {
        gd.dayNight = DayNight.DAY;
        Permanent tamer = harness.enterBattlefieldAndReturn(player1, new HoundTamer());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(tamer.getCard()).isInstanceOf(UntamedPup.class);

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(tamer.getCard()).isInstanceOf(HoundTamer.class);
    }

    @Test
    void untamedPupRetainsTheCounterAbility() {
        gd.dayNight = DayNight.NIGHT;
        Permanent pup = harness.enterBattlefieldAndReturn(player1, new HoundTamer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BoundingWolf());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(pup.getCard()).isInstanceOf(UntamedPup.class);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    void enteringBeforeDayOrNightEstablishesDay() {
        Permanent tamer = harness.enterBattlefieldAndReturn(player1, new HoundTamer());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(tamer.getCard()).isInstanceOf(HoundTamer.class);
    }

    @Test
    void counterAbilityCanTargetItselfRepeatedlyWithoutTapping() {
        Permanent tamer = harness.enterBattlefieldAndReturn(player1, new HoundTamer());
        addAbilityMana();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, tamer.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, tamer.getId());
        harness.passBothPriorities();

        assertThat(tamer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(tamer.isTapped()).isFalse();
    }

    @Test
    void countersRemainWhenDayChangesToNight() {
        Permanent tamer = harness.enterBattlefieldAndReturn(player1, new HoundTamer());
        addAbilityMana();
        harness.activateAbility(player1, 0, null, tamer.getId());
        harness.passBothPriorities();
        gd.spellsCastLastTurn.clear();

        harness.performUntapStep(player2);

        assertThat(tamer.getCard()).isInstanceOf(UntamedPup.class);
        assertThat(tamer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void offTurnSpellsDoNotPreventNight() {
        Permanent tamer = harness.enterBattlefieldAndReturn(player1, new HoundTamer());
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(tamer.getCard()).isInstanceOf(UntamedPup.class);
    }

    @Test
    void offTurnSpellsDoNotCreateAnUpkeepAbilityOrMakeItDay() {
        gd.dayNight = DayNight.NIGHT;
        Permanent pup = harness.enterBattlefieldAndReturn(player1, new HoundTamer());
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(pup.getCard()).isInstanceOf(UntamedPup.class);
        assertThat(gd.stack).isEmpty();
    }
}
