package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlackWidowDoubleAgent.class, GrizzlyBears.class})
class BlackWidowDoubleAgentTest extends BaseCardTest {

    @Test
    @DisplayName("A creature attacking alone gains first strike and menace")
    void attackingAloneGainsFirstStrikeAndMenace() {
        addCreatureReady(player1, new BlackWidowDoubleAgent());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(bears.hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The keywords wear off at end of turn")
    void keywordsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new BlackWidowDoubleAgent());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(bears.hasKeyword(Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The ability does not trigger when multiple creatures attack")
    void doesNotTriggerWhenMultipleCreaturesAttack() {
        addCreatureReady(player1, new BlackWidowDoubleAgent());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Black Widow, Double Agent"));
        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(bears.hasKeyword(Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Black Widow gains both keywords when she attacks alone")
    void grantsKeywordsToHerself() {
        Permanent widow = addCreatureReady(player1, new BlackWidowDoubleAgent());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(widow.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(widow.hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's lone attacker does not receive the keywords")
    void doesNotTriggerForOpponent() {
        addCreatureReady(player1, new BlackWidowDoubleAgent());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(bears.hasKeyword(Keyword.MENACE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger grants both keywords even if Black Widow leaves before resolution")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent widow = addCreatureReady(player1, new BlackWidowDoubleAgent());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));
        assertThat(gd.stack).hasSize(1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, widow);
        resolveAllTriggers();

        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(bears.hasKeyword(Keyword.MENACE)).isTrue();
    }
}
