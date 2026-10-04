package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DireStrainDemolisher;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurlyBreaker.class, DireStrainDemolisher.class, Shock.class})
class BurlyBreakerTest extends BaseCardTest {

    @Test
    void entersAsBurlyBreakerDuringTheDay() {
        gd.dayNight = DayNight.DAY;
        Permanent breaker = harness.enterBattlefieldAndReturn(player1, new BurlyBreaker());

        assertThat(breaker.isTransformed()).isFalse();
        assertThat(breaker.getCard()).isInstanceOf(BurlyBreaker.class);
    }

    @Test
    void entersAsDireStrainDemolisherDuringTheNight() {
        gd.dayNight = DayNight.NIGHT;
        Permanent breaker = harness.enterBattlefieldAndReturn(player1, new BurlyBreaker());

        assertThat(breaker.isTransformed()).isTrue();
        assertThat(breaker.getCard()).isInstanceOf(DireStrainDemolisher.class);
    }

    @Test
    void dayNightTransformsBothFaces() {
        gd.dayNight = DayNight.DAY;
        Permanent breaker = harness.enterBattlefieldAndReturn(player1, new BurlyBreaker());

        gd.spellsCastLastTurn.clear();
        advanceToUntap(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(breaker.getCard()).isInstanceOf(DireStrainDemolisher.class);

        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUntap(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(breaker.getCard()).isInstanceOf(BurlyBreaker.class);
    }

    @Test
    void frontFaceWardAllowsItsControllerToPayOne() {
        Permanent breaker = addReadyBreaker(DayNight.DAY);

        castShockAt(breaker, 1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotInGraveyard(player2, "Shock");
    }

    @Test
    void backFaceWardCountersShockWhenOpponentCanPayOnlyTwo() {
        Permanent breaker = addReadyBreaker(DayNight.NIGHT);

        castShockAt(breaker, 2);

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void enteringWhenNeitherDayNorNightEstablishesDay() {
        gd.dayNight = DayNight.NEITHER;

        Permanent breaker = harness.enterBattlefieldAndReturn(player1, new BurlyBreaker());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(breaker.isTransformed()).isFalse();
    }

    @Test
    void remainsDayWhenPreviousActivePlayerCastOneSpell() {
        Permanent breaker = addReadyBreaker(DayNight.DAY);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        gd.previousTurnActivePlayerId = player2.getId();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(breaker.isTransformed()).isFalse();
    }

    @Test
    void remainsNightWhenOnlyNonactivePlayerCastTwoSpellsLastTurn() {
        Permanent breaker = addReadyBreaker(DayNight.NIGHT);
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        gd.previousTurnActivePlayerId = player2.getId();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(breaker.isTransformed()).isTrue();
    }

    @Test
    void frontFaceWardCountersSpellWhenPaymentIsDeclined() {
        Permanent breaker = addReadyBreaker(DayNight.DAY);
        castShockAt(breaker, 1);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        assertThat(breaker.getMarkedDamage()).isZero();
    }

    @Test
    void backFaceWardAllowsPaymentOfThreeAndSpellResolves() {
        Permanent breaker = addReadyBreaker(DayNight.NIGHT);
        castShockAt(breaker, 3);

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(breaker.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void wardDoesNotTriggerForControllersOwnSpell() {
        Permanent breaker = addReadyBreaker(DayNight.NIGHT);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, breaker.getId());

        assertThat(breaker.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shock");
    }

    private Permanent addReadyBreaker(DayNight dayNight) {
        gd.dayNight = dayNight;
        Permanent breaker = harness.enterBattlefieldAndReturn(player1, new BurlyBreaker());
        breaker.setSummoningSick(false);
        return breaker;
    }

    private void castShockAt(Permanent target, int extraMana) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1 + extraMana);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }

    private void advanceToUntap(Player activePlayer) {
        gd.previousTurnActivePlayerId = activePlayer.equals(player1) ? player2.getId() : player1.getId();
        harness.performUntapStep(activePlayer);
    }
}
