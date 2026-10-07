package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunriseCavalier.class, CandlegroveWitch.class})
class SunriseCavalierTest extends BaseCardTest {

    @Test
    void becomesDayAsItEntersWhenThereIsNoDesignation() {
        harness.setHand(player1, List.of(new SunriseCavalier()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void putsCounterOnTargetCreatureWhenDayBecomesNight() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new SunriseCavalier());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CandlegroveWitch());

        makeItNight();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsCounterOnTargetCreatureWhenNightBecomesDay() {
        gd.dayNight = DayNight.NIGHT;
        harness.addToBattlefield(player1, new SunriseCavalier());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        gd.recordSpellCast(player1.getId(), new CandlegroveWitch());
        gd.recordSpellCast(player1.getId(), new CandlegroveWitch());

        makeItDay();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void enteringDuringNightDoesNotChangeTheDesignationOrTrigger() {
        gd.dayNight = DayNight.NIGHT;
        castCavalier();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void enteringDuringDayDoesNotTrigger() {
        gd.dayNight = DayNight.DAY;
        castCavalier();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canPutTheCounterOnItself() {
        gd.dayNight = DayNight.DAY;
        Permanent cavalier = harness.addToBattlefieldAndReturn(player1, new SunriseCavalier());

        makeItNight();

        harness.handlePermanentChosen(player1, cavalier.getId());
        harness.passBothPriorities();

        assertThat(cavalier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void twoSpellsByTheNonactivePlayerDoNotMakeItDay() {
        gd.dayNight = DayNight.NIGHT;
        Permanent cavalier = harness.addToBattlefieldAndReturn(player1, new SunriseCavalier());
        gd.recordSpellCast(player2.getId(), new CandlegroveWitch());
        gd.recordSpellCast(player2.getId(), new CandlegroveWitch());

        makeItDay();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(cavalier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castCavalier() {
        harness.setHand(player1, List.of(new SunriseCavalier()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
    private void makeItNight() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
    }

    private void makeItDay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
    }
}
