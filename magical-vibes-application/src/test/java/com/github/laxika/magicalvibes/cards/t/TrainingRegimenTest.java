package com.github.laxika.magicalvibes.cards.t;

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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrainingRegimen.class, GrizzlyBears.class})
class TrainingRegimenTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control with +1/+1 counters have trample")
    void counteredOwnCreaturesHaveTrample() {
        addRegimen();
        Permanent countered = addCreature(player1);
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent uncountered = addCreature(player1);
        Permanent opponentCountered = addCreature(player2);
        opponentCountered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, countered, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCountered, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Beginning of combat puts a +1/+1 counter on a target creature you control")
    void beginningOfCombatPutsCounterOnOwnTarget() {
        addRegimen();
        Permanent target = addCreature(player1);
        Permanent opponentCreature = addCreature(player2);

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId())
                .doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        addRegimen();
        Permanent target = addCreature(player1);

        advanceToCombat(player2);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addRegimen() {
        return harness.addToBattlefieldAndReturn(player1, new TrainingRegimen());
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
