package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OkoyeMightyAndAdored.class, GrizzlyBears.class})
class OkoyeMightyAndAdoredTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and makes its controller the monarch")
    void entersAndMakesControllerMonarch() {
        harness.enterBattlefieldAndReturn(player1, new OkoyeMightyAndAdored());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("At the beginning of combat, puts a counter on the chosen creature")
    void beginningOfCombatPutsCounterOnChosenCreature() {
        addOkoyeAndTarget();
        Permanent target = gd.playerBattlefields.get(player1.getId()).get(1);

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The chosen creature gains double strike and trample when attacking the monarch")
    void chosenCreatureGainsKeywordsWhenAttackingMonarch() {
        Permanent target = addOkoyeAndTarget();
        gd.monarchPlayerId = player2.getId();
        chooseTargetAtBeginningOfCombat(target);

        declareAttackers(player1, List.of(1));
        assertThat(target.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The attack trigger does not fire when the creature attacks a nonmonarch")
    void doesNotGrantKeywordsWhenAttackingNonmonarch() {
        Permanent target = addOkoyeAndTarget();
        gd.monarchPlayerId = player1.getId();
        chooseTargetAtBeginningOfCombat(target);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The granted keywords wear off at end of turn")
    void grantedKeywordsWearOffAtEndOfTurn() {
        Permanent target = addOkoyeAndTarget();
        gd.monarchPlayerId = player2.getId();
        chooseTargetAtBeginningOfCombat(target);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void canChooseAnOpponentsCreature() {
        harness.enterBattlefieldAndReturn(player1, new OkoyeMightyAndAdored());
        resolveAllTriggers();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        chooseTargetAtBeginningOfCombat(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerAtBeginningOfOpponentsCombat() {
        addOkoyeAndTarget();

        advanceToBeginningOfCombat(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackTriggerHasOkoyeAsItsSource() {
        Permanent target = addOkoyeAndTarget();
        gd.monarchPlayerId = player2.getId();
        chooseTargetAtBeginningOfCombat(target);

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(OkoyeMightyAndAdored.class);
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void delayedTriggerRemainsControlledByOkoyesControllerAfterControlChanges() {
        Permanent target = addOkoyeAndTarget();
        chooseTargetAtBeginningOfCombat(target);
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        target.setSummoningSick(false);
        gd.monarchPlayerId = player1.getId();

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void changingMonarchAfterAttackDoesNotPreventKeywordGrant() {
        Permanent target = addOkoyeAndTarget();
        gd.monarchPlayerId = player2.getId();
        chooseTargetAtBeginningOfCombat(target);
        declareAttackers(player1, List.of(1));
        gd.monarchPlayerId = player1.getId();

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void delayedTriggerSurvivesOkoyeLeavingBattlefield() {
        Permanent target = addOkoyeAndTarget();
        gd.monarchPlayerId = player2.getId();
        chooseTargetAtBeginningOfCombat(target);
        gd.playerBattlefields.get(player1.getId()).removeFirst();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addOkoyeAndTarget() {
        harness.enterBattlefieldAndReturn(player1, new OkoyeMightyAndAdored());
        harness.passBothPriorities();
        return addCreatureReady(player1, new GrizzlyBears());
    }

    private void chooseTargetAtBeginningOfCombat(Permanent target) {
        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
