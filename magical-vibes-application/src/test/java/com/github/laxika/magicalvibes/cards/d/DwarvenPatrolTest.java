package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AnaDisciple;
import com.github.laxika.magicalvibes.cards.g.GoblinLegionnaire;
import com.github.laxika.magicalvibes.cards.r.RakaDisciple;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwarvenPatrol.class, AnaDisciple.class, Dodecapod.class, RakaDisciple.class,
        GoblinLegionnaire.class})
class DwarvenPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent patrol = addTappedPatrol();

        advanceToUpkeep(player1);

        assertThat(patrol.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps when its controller casts a nonred spell")
    void untapsWhenControllerCastsNonredSpell() {
        Permanent patrol = addTappedPatrol();

        harness.castFromHand(player1, new AnaDisciple(), "{G}");
        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap when its controller casts a red spell")
    void doesNotUntapWhenControllerCastsRedSpell() {
        Permanent patrol = addTappedPatrol();

        harness.castFromHand(player1, new RakaDisciple(), "{R}");
        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps when its controller casts a colorless spell")
    void untapsWhenControllerCastsColorlessSpell() {
        Permanent patrol = addTappedPatrol();

        harness.castFromHand(player1, new Dodecapod(), "{4}");
        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap when its controller casts a multicolored red spell")
    void doesNotUntapWhenControllerCastsMulticoloredRedSpell() {
        Permanent patrol = addTappedPatrol();

        harness.castFromHand(player1, new GoblinLegionnaire(), "{R}{W}");
        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts a nonred spell")
    void opponentCastingNonredSpellDoesNotUntapIt() {
        Permanent patrol = addTappedPatrol();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new AnaDisciple(), "{G}");
        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isTrue();
    }

    private Permanent addTappedPatrol() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new DwarvenPatrol());
        patrol.tap();
        return patrol;
    }
}
