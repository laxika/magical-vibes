package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YouCannotHideFromMe.class, GrizzlyBears.class})
class YouCannotHideFromMeTest extends BaseCardTest {

    @Test
    void boostsTargetCreatureAndMakesItVigilantAndUnblockableAtCombat() {
        harness.addToBattlefield(player1, new YouCannotHideFromMe());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void combatBonusWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new YouCannotHideFromMe());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void abandonsAtOpponentEndStepBelowHalfStartingLife() {
        YouCannotHideFromMe scheme = new YouCannotHideFromMe();
        harness.addToBattlefield(player1, scheme);
        gd.playerLifeTotals.put(player2.getId(), 9);

        advanceToEndStep(player2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof YouCannotHideFromMe);
    }

    @Test
    void doesNotAbandonAtExactlyHalfStartingLife() {
        YouCannotHideFromMe scheme = new YouCannotHideFromMe();
        harness.addToBattlefield(player1, scheme);
        gd.playerLifeTotals.put(player2.getId(), 10);

        advanceToEndStep(player2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == scheme);
    }

    @Test
    void doesNotTriggerDuringControllersCombatOrEndStep() {
        harness.addToBattlefield(player1, new YouCannotHideFromMe());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerLifeTotals.put(player2.getId(), 9);

        advanceToCombat(player2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getEffectivePower()).isEqualTo(2);

        advanceToEndStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof YouCannotHideFromMe);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
