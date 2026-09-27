package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrosDefenseContractor.class, GrizzlyBears.class})
class KrosDefenseContractorTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep shield counter taps, goads, and grants trample to the opposing creature")
    void upkeepAbilityTapsGoadsAndGrantsTrample() {
        addCreatureReady(player1, new KrosDefenseContractor());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The temporary trample and goad expire at Kros's next turn")
    void temporaryEffectsExpireAtNextTurn() {
        addCreatureReady(player1, new KrosDefenseContractor());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isZero();
    }
}
