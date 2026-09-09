package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WearyPrisoner.class, WrathfulJailbreaker.class})
class WearyPrisonerTest extends BaseCardTest {

    @Test
    void entersAsWearyPrisonerDuringDay() {
        gd.dayNight = DayNight.DAY;

        Permanent prisoner = harness.enterBattlefieldAndReturn(player1, new WearyPrisoner());

        assertThat(prisoner.isTransformed()).isFalse();
        assertThat(prisoner.getCard()).isInstanceOf(WearyPrisoner.class);
    }

    @Test
    void entersAsWrathfulJailbreakerDuringNight() {
        gd.dayNight = DayNight.NIGHT;

        Permanent prisoner = harness.enterBattlefieldAndReturn(player1, new WearyPrisoner());

        assertThat(prisoner.isTransformed()).isTrue();
        assertThat(prisoner.getCard()).isInstanceOf(WrathfulJailbreaker.class);
    }

    @Test
    void transformsWithDayAndNight() {
        gd.dayNight = DayNight.DAY;
        Permanent prisoner = harness.enterBattlefieldAndReturn(player1, new WearyPrisoner());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(prisoner.getCard()).isInstanceOf(WrathfulJailbreaker.class);

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(prisoner.getCard()).isInstanceOf(WearyPrisoner.class);
    }

    @Test
    void wrathfulJailbreakerMustAttackWhenAble() {
        gd.dayNight = DayNight.NIGHT;
        Permanent prisoner = harness.enterBattlefieldAndReturn(player1, new WearyPrisoner());
        prisoner.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        assertThat(prisoner.getCard()).isInstanceOf(WrathfulJailbreaker.class);
    }
}
