package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GloriousEnforcer.class)
class GloriousEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of each combat, gains double strike when its controller has more life")
    void gainsDoubleStrikeWhenControllerHasMoreLife() {
        var enforcer = addCreatureReady(player1, new GloriousEnforcer());
        harness.setLife(player1, 21);
        harness.setLife(player2, 20);

        advanceToBeginningOfCombat(player1);

        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not gain double strike when its controller does not have more life")
    void doesNotGainDoubleStrikeWhenControllerDoesNotHaveMoreLife() {
        var enforcer = addCreatureReady(player1, new GloriousEnforcer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToBeginningOfCombat(player1);

        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The double strike grant wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        var enforcer = addCreatureReady(player1, new GloriousEnforcer());
        harness.setLife(player1, 21);
        harness.setLife(player2, 20);

        advanceToBeginningOfCombat(player1);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Triggers during an opponent's combat")
    void triggersDuringOpponentsCombat() {
        var enforcer = addCreatureReady(player1, new GloriousEnforcer());
        harness.setLife(player1, 21);
        harness.setLife(player2, 20);

        advanceToBeginningOfCombat(player2);

        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Life advantage is rechecked when the trigger resolves")
    void lifeAdvantageMustRemainAtResolution() {
        var enforcer = addCreatureReady(player1, new GloriousEnforcer());
        harness.setLife(player1, 21);
        harness.setLife(player2, 20);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.setLife(player2, 21);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gaining a life advantage after combat begins does not create a trigger")
    void lifeAdvantageMustExistWhenCombatBegins() {
        var enforcer = addCreatureReady(player1, new GloriousEnforcer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).isEmpty();
        harness.setLife(player1, 21);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Losing the life advantage after resolution does not remove double strike")
    void retainsDoubleStrikeAfterLifeAdvantageIsLost() {
        var enforcer = addCreatureReady(player1, new GloriousEnforcer());
        harness.setLife(player1, 21);
        harness.setLife(player2, 20);

        advanceToBeginningOfCombat(player1);
        harness.setLife(player1, 19);

        assertThat(gqs.hasKeyword(gd, enforcer, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Each Enforcer compares its own controller's life")
    void comparesEachEnforcersControllersLife() {
        var advantagedEnforcer = addCreatureReady(player1, new GloriousEnforcer());
        var disadvantagedEnforcer = addCreatureReady(player2, new GloriousEnforcer());
        harness.setLife(player1, 21);
        harness.setLife(player2, 20);

        advanceToBeginningOfCombat(player2);

        assertThat(gqs.hasKeyword(gd, advantagedEnforcer, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, disadvantagedEnforcer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("With a life advantage, an unblocked Enforcer deals damage twice and gains life twice")
    void doubleStrikeAndLifelinkApplyInBothDamageSteps() {
        addCreatureReady(player1, new GloriousEnforcer());
        harness.setLife(player1, 21);
        harness.setLife(player2, 20);

        advanceToBeginningOfCombat(player1);
        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 31);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("Lifelink during regular combat damage does not retroactively grant double strike")
    void lifelinkDoesNotRetroactivelyGrantDoubleStrike() {
        addCreatureReady(player1, new GloriousEnforcer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToBeginningOfCombat(player1);
        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
    }
}
