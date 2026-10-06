package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeasonedConsultant.class})
class SeasonedConsultantTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get a bonus when fewer than three creatures attack")
    void noBonusWithFewerThanThreeAttackers() {
        Permanent consultant = addCreatureReady(player1, new SeasonedConsultant());
        addCreatureReady(player1, new SeasonedConsultant());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(consultant.getPowerModifier()).isZero();
        assertThat(consultant.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Gets +2/+0 when three creatures attack")
    void getsBonusWithThreeAttackers() {
        Permanent consultant = addCreatureReady(player1, new SeasonedConsultant());
        addCreatureReady(player1, new SeasonedConsultant());
        addCreatureReady(player1, new SeasonedConsultant());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(consultant.getPowerModifier()).isEqualTo(2);
        assertThat(consultant.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Non-attacking creatures do not count")
    void nonAttackingCreaturesDoNotCount() {
        Permanent consultant = addCreatureReady(player1, new SeasonedConsultant());
        addCreatureReady(player1, new SeasonedConsultant());
        addCreatureReady(player1, new SeasonedConsultant());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(consultant.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Bonus wears off at end of turn")
    void bonusWearsOffAtEndOfTurn() {
        Permanent consultant = addCreatureReady(player1, new SeasonedConsultant());
        addCreatureReady(player1, new SeasonedConsultant());
        addCreatureReady(player1, new SeasonedConsultant());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(consultant.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(consultant.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Gets the bonus even when it is not attacking")
    void getsBonusWhileNotAttacking() {
        Permanent consultant = addCreatureReady(player1, new SeasonedConsultant());
        addCreatureReady(player1, new SeasonedConsultant());
        addCreatureReady(player1, new SeasonedConsultant());
        addCreatureReady(player1, new SeasonedConsultant());

        declareAttackers(player1, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(consultant.getPowerModifier()).isEqualTo(2);
        assertThat(consultant.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Four attackers give the bonus only once")
    void fourAttackersGiveOneBonus() {
        Permanent consultant = addCreatureReady(player1, new SeasonedConsultant());
        addCreatureReady(player1, new SeasonedConsultant());
        addCreatureReady(player1, new SeasonedConsultant());
        addCreatureReady(player1, new SeasonedConsultant());

        declareAttackers(player1, List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(consultant.getPowerModifier()).isEqualTo(2);
        assertThat(consultant.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's attack does not give the bonus")
    void opponentsAttackDoesNotGiveBonus() {
        Permanent consultant = addCreatureReady(player1, new SeasonedConsultant());
        addCreatureReady(player2, new SeasonedConsultant());
        addCreatureReady(player2, new SeasonedConsultant());
        addCreatureReady(player2, new SeasonedConsultant());

        declareAttackers(player2, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(consultant.getPowerModifier()).isZero();
        assertThat(consultant.getToughnessModifier()).isZero();
    }
}
