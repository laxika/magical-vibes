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

        gd.spellsCastLastTurn.put(player1.getId(), 2);
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
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToUntap(Player activePlayer) {
        harness.performUntapStep(activePlayer);
    }
}
