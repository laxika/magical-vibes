package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QalSismaBehemoth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SightOfTheScalelords.class, QalSismaBehemoth.class, GrizzlyBears.class})
class SightOfTheScalelordsTest extends BaseCardTest {

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Beginning of combat boosts and grants vigilance to qualifying creatures you control")
    void boostsCreaturesWithToughnessAtLeastFour() {
        Permanent eligible = harness.addToBattlefieldAndReturn(player1, new QalSismaBehemoth());
        Permanent ineligible = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new QalSismaBehemoth());
        harness.addToBattlefield(player1, new SightOfTheScalelords());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, eligible)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, eligible)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, eligible, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ineligible)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ineligible)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ineligible, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The boost and vigilance wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent eligible = harness.addToBattlefieldAndReturn(player1, new QalSismaBehemoth());
        harness.addToBattlefield(player1, new SightOfTheScalelords());

        advanceToCombatAndResolve(player1);
        assertThat(gqs.getEffectivePower(gd, eligible)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, eligible, Keyword.VIGILANCE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, eligible)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, eligible, Keyword.VIGILANCE)).isFalse();
    }
}
