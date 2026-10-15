package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BallistaWatcher.class, BallistaWielder.class, DawnhartDisciple.class})
class BallistaWatcherTest extends BaseCardTest {

    @Test
    void watcherDealsDamageToAnyTargetAndTaps() {
        Permanent watcher = addCreatureReady(player1, new BallistaWatcher());
        Permanent bear = addCreatureReady(player2, new DawnhartDisciple());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(watcher.isTapped()).isTrue();
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void wielderMakesCreatureUnableToBlockAfterDamagingIt() {
        Permanent wielder = addTransformedWatcher(player1);
        Permanent bear = addCreatureReady(player2, new DawnhartDisciple());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(wielder.isTapped()).isFalse();
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
        assertThat(bls.canBlock(gd, bear)).isFalse();
    }

    @Test
    void wielderCanTargetAPlayer() {
        Permanent wielder = addTransformedWatcher(player1);
        harness.setLife(player2, 20);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(wielder.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void transformsWithTheDayNightDesignation() {
        gd.previousTurnActivePlayerId = player1.getId();
        gd.dayNight = DayNight.DAY;
        Permanent watcher = addCreatureReady(player1, new BallistaWatcher());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(watcher.isTransformed()).isTrue();
        assertThat(watcher.getCard()).isInstanceOf(BallistaWielder.class);

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(watcher.isTransformed()).isFalse();
        assertThat(watcher.getCard()).isInstanceOf(BallistaWatcher.class);
    }

    @Test
    void watcherCanDamageAPlayer() {
        addCreatureReady(player1, new BallistaWatcher());
        harness.setLife(player2, 20);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void enteringWatcherEstablishesDay() {
        Permanent watcher = harness.enterBattlefieldAndReturn(player1, new BallistaWatcher());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(watcher.isTransformed()).isFalse();
    }

    @Test
    void wielderCanActivateRepeatedlyWhileTappedAndSummoningSick() {
        gd.dayNight = DayNight.NIGHT;
        Permanent wielder = harness.enterBattlefieldAndReturn(player1, new BallistaWatcher());
        wielder.tap();
        harness.setLife(player2, 20);
        addAbilityMana();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(wielder.isTapped()).isTrue();
    }

    @Test
    void preventedDamageDoesNotStopCreatureFromBlocking() {
        addTransformedWatcher(player1);
        Permanent target = addCreatureReady(player2, new DawnhartDisciple());
        target.setDamagePreventionShield(1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(bls.canBlock(gd, target)).isTrue();
    }

    @Test
    void dayRemainsWhenPreviousActivePlayerCastOneSpell() {
        gd.previousTurnActivePlayerId = player1.getId();
        gd.dayNight = DayNight.DAY;
        Permanent watcher = addCreatureReady(player1, new BallistaWatcher());
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(watcher.isTransformed()).isFalse();
    }

    @Test
    void nonactivePlayersSpellsDoNotPreventNight() {
        gd.previousTurnActivePlayerId = player1.getId();
        gd.dayNight = DayNight.DAY;
        Permanent watcher = addCreatureReady(player1, new BallistaWatcher());
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(watcher.isTransformed()).isTrue();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent addTransformedWatcher(Player player) {
        gd.dayNight = DayNight.NIGHT;
        Permanent watcher = harness.enterBattlefieldAndReturn(player, new BallistaWatcher());
        watcher.setSummoningSick(false);
        return watcher;
    }
}
