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

    private Permanent addOkoyeAndTarget() {
        harness.enterBattlefieldAndReturn(player1, new OkoyeMightyAndAdored());
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
        harness.passBothPriorities();
    }
}
