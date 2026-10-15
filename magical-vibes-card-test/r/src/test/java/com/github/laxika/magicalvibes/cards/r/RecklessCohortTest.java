package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecklessCohort.class, ExpeditionEnvoy.class})
class RecklessCohortTest extends BaseCardTest {

    @Test
    void mustAttackWithoutAnotherAlly() {
        addCreatureReady(player1, new RecklessCohort());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void anotherAllyLetsItStayHome() {
        addCreatureReady(player1, new RecklessCohort());
        addCreatureReady(player1, new ExpeditionEnvoy());

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    void theCohortDoesNotCountAsAnotherAlly() {
        addCreatureReady(player1, new RecklessCohort());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void opponentsAllyDoesNotLetItStayHome() {
        addCreatureReady(player1, new RecklessCohort());
        addCreatureReady(player2, new ExpeditionEnvoy());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void twoCohortsCanBothStayHome() {
        addCreatureReady(player1, new RecklessCohort());
        addCreatureReady(player1, new RecklessCohort());

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    void tappedCohortIsNotRequiredToAttack() {
        Permanent cohort = addCreatureReady(player1, new RecklessCohort());
        cohort.tap();

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    void summoningSickCohortIsNotRequiredToAttack() {
        Permanent cohort = addCreatureReady(player1, new RecklessCohort());
        cohort.setSummoningSick(true);

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    void tappedSummoningSickAllyStillLetsCohortStayHome() {
        addCreatureReady(player1, new RecklessCohort());
        Permanent ally = addCreatureReady(player1, new ExpeditionEnvoy());
        ally.tap();
        ally.setSummoningSick(true);

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    void mustAttackAfterOtherAllyLeavesBattlefield() {
        addCreatureReady(player1, new RecklessCohort());
        Permanent ally = addCreatureReady(player1, new ExpeditionEnvoy());
        gd.playerBattlefields.get(player1.getId()).remove(ally);
        gd.playerGraveyards.get(player1.getId()).add(ally.getCard());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void canSatisfyRequirementByAttacking() {
        addCreatureReady(player1, new RecklessCohort());

        assertThatCode(() -> declareAttackers(List.of(0))).doesNotThrowAnyException();
    }
}
