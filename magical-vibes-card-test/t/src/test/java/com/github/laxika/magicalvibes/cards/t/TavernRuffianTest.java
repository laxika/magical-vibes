package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TavernRuffian.class, TavernSmasher.class})
class TavernRuffianTest extends BaseCardTest {

    @Test
    void transformsToTavernSmasherWhenNoSpellsWereCastLastTurn() {
        gd.dayNight = DayNight.DAY;
        Permanent ruffian = harness.enterBattlefieldAndReturn(player1, new TavernRuffian());
        gd.spellsCastLastTurn.clear();

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(ruffian.isTransformed()).isTrue();
        assertThat(ruffian.getCard()).isInstanceOf(TavernSmasher.class);
    }

    @Test
    void transformsToTavernRuffianWhenTwoSpellsWereCastLastTurn() {
        gd.dayNight = DayNight.NIGHT;
        Permanent ruffian = harness.enterBattlefieldAndReturn(player1, new TavernRuffian());
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(ruffian.isTransformed()).isFalse();
        assertThat(ruffian.getCard()).isInstanceOf(TavernRuffian.class);
    }

    @Test
    void doesNotTransformBackWhenOnlyOneSpellWasCastLastTurn() {
        gd.dayNight = DayNight.NIGHT;
        Permanent ruffian = harness.enterBattlefieldAndReturn(player1, new TavernRuffian());
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.performUntapStep(player1);

        assertThat(ruffian.isTransformed()).isTrue();
    }

    @Test
    void establishesDayWhenEnteringBeforeEitherDesignationExists() {
        gd.dayNight = DayNight.NEITHER;

        Permanent ruffian = harness.enterBattlefieldAndReturn(player1, new TavernRuffian());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(ruffian.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entersTransformedAtNightWithoutUsingTheStack() {
        gd.dayNight = DayNight.NIGHT;

        Permanent ruffian = harness.enterBattlefieldAndReturn(player1, new TavernRuffian());

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(ruffian.isTransformed()).isTrue();
        assertThat(ruffian.getCard()).isInstanceOf(TavernSmasher.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void becomesNightEvenWhenTheNonactivePlayerCastSpells() {
        gd.dayNight = DayNight.DAY;
        Permanent ruffian = harness.enterBattlefieldAndReturn(player1, new TavernRuffian());
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(ruffian.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotBecomeDayOrTriggerWhenOnlyTheNonactivePlayerCastTwoSpells() {
        gd.dayNight = DayNight.NIGHT;
        Permanent ruffian = harness.enterBattlefieldAndReturn(player1, new TavernRuffian());
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(ruffian.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transformsBothControllersPermanentsTogetherDuringUntap() {
        gd.dayNight = DayNight.DAY;
        Permanent first = harness.enterBattlefieldAndReturn(player1, new TavernRuffian());
        Permanent second = harness.enterBattlefieldAndReturn(player2, new TavernRuffian());
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.clear();

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(first.isTransformed()).isTrue();
        assertThat(second.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void remainsDayWhenThePreviousActivePlayerCastOneSpell() {
        gd.dayNight = DayNight.DAY;
        Permanent ruffian = harness.enterBattlefieldAndReturn(player1, new TavernRuffian());
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(ruffian.isTransformed()).isFalse();
    }
}
