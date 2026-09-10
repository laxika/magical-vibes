package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Telethopter.class, ThalakosSentry.class, LotusPetal.class})
class TelethopterTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an untapped creature you control gives Telethopter flying")
    void tappingCreatureGrantsFlying() {
        Permanent thopter = addCreatureReady(player1, new Telethopter());
        thopter.tap();
        Permanent fodder = addCreatureReady(player1, new ThalakosSentry());

        activate(thopter);

        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        assertThat(fodder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Telethopter can tap itself to pay the activation cost")
    void canTapItselfToPay() {
        Permanent thopter = addCreatureReady(player1, new Telethopter());

        activate(thopter);

        assertThat(thopter.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOff() {
        Permanent thopter = addCreatureReady(player1, new Telethopter());
        thopter.tap();
        addCreatureReady(player1, new ThalakosSentry());

        activate(thopter);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();

        advanceToNextTurn();

        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the activation cost")
    void cannotUseOpponentsCreatureToPay() {
        Permanent thopter = addCreatureReady(player1, new Telethopter());
        thopter.tap();
        Permanent opponentCreature = addCreatureReady(player2, new ThalakosSentry());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(thopter);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An untapped noncreature permanent cannot pay the activation cost")
    void cannotUseNoncreaturePermanentToPay() {
        Permanent thopter = addCreatureReady(player1, new Telethopter());
        thopter.tap();
        Permanent lotusPetal = harness.addToBattlefieldAndReturn(player1, new LotusPetal());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(thopter);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(lotusPetal.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate with no untapped creature to tap")
    void cannotActivateWithoutUntappedCreature() {
        Permanent thopter = addCreatureReady(player1, new Telethopter());
        thopter.tap();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(thopter);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activate(Permanent thopter) {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(thopter);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
    }

    private void advanceToNextTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UNTAP);
    }
}
