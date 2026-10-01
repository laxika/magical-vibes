package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BallynockCohort;
import com.github.laxika.magicalvibes.cards.t.TattermungeManiac;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ResplendentMentor.class, BallynockCohort.class, TattermungeManiac.class})
class ResplendentMentorTest extends BaseCardTest {

    @Test
    @DisplayName("White creature you control gains the tap-for-life ability")
    void whiteCreatureGainsLifeAbility() {
        harness.addToBattlefield(player1, new ResplendentMentor());
        Permanent cohort = addCreatureReady(player1, new BallynockCohort());

        int cohortIndex = gd.playerBattlefields.get(player1.getId()).indexOf(cohort);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, cohortIndex, null, null);
        harness.passBothPriorities();

        assertThat(cohort.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Resplendent Mentor gains the tap-for-life ability itself")
    void whiteSourceCreatureGainsLifeAbility() {
        Permanent mentor = addCreatureReady(player1, new ResplendentMentor());

        int mentorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mentor);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, mentorIndex, null, null);
        harness.passBothPriorities();

        assertThat(mentor.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Non-white creature does not gain the granted ability")
    void nonWhiteCreatureDoesNotGainAbility() {
        harness.addToBattlefield(player1, new ResplendentMentor());
        Permanent maniac = addCreatureReady(player1, new TattermungeManiac());

        int maniacIndex = gd.playerBattlefields.get(player1.getId()).indexOf(maniac);

        assertThatThrownBy(() -> harness.activateAbility(player1, maniacIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Opponent's white creatures do not gain the granted ability")
    void opponentWhiteCreatureDoesNotGainAbility() {
        harness.addToBattlefield(player1, new ResplendentMentor());
        Permanent cohort = addCreatureReady(player2, new BallynockCohort());

        int cohortIndex = gd.playerBattlefields.get(player2.getId()).indexOf(cohort);

        assertThatThrownBy(() -> harness.activateAbility(player2, cohortIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Granted ability is lost when Resplendent Mentor leaves the battlefield")
    void abilityLostWhenMentorLeaves() {
        harness.addToBattlefield(player1, new ResplendentMentor());
        Permanent cohort = addCreatureReady(player1, new BallynockCohort());

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Resplendent Mentor"));

        int cohortIndex = gd.playerBattlefields.get(player1.getId()).indexOf(cohort);

        assertThatThrownBy(() -> harness.activateAbility(player1, cohortIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }
}
