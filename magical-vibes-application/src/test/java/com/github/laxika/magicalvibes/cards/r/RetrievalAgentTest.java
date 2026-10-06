package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RetrievalAgent.class})
class RetrievalAgentTest extends BaseCardTest {

    @Test
    @DisplayName("Two generic mana gives Retrieval Agent +1/-1 until end of turn")
    void activatedAbilityBoostsUntilEndOfTurn() {
        Permanent retrievalAgent = addRetrievalAgentReady();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(retrievalAgent.getPowerModifier()).isEqualTo(1);
        assertThat(retrievalAgent.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(retrievalAgent.getPowerModifier()).isZero();
        assertThat(retrievalAgent.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Repeated activations accumulate and affect only their source")
    void repeatedActivationsAccumulate() {
        Permanent retrievalAgent = addRetrievalAgentReady();
        Permanent otherAgent = harness.addToBattlefieldAndReturn(player1, new RetrievalAgent());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(retrievalAgent.getPowerModifier()).isEqualTo(4);
        assertThat(retrievalAgent.getToughnessModifier()).isEqualTo(-4);
        harness.assertOnBattlefield(player1, "Retrieval Agent");
        assertThat(otherAgent.getPowerModifier()).isZero();
        assertThat(otherAgent.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A fifth activation puts Retrieval Agent into the graveyard for zero toughness")
    void repeatedActivationCanReduceToughnessToZero() {
        addRetrievalAgentReady();
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        for (int i = 0; i < 5; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Retrieval Agent");
        harness.assertInGraveyard(player1, "Retrieval Agent");
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent retrievalAgent = harness.addToBattlefieldAndReturn(player1, new RetrievalAgent());
        retrievalAgent.setSummoningSick(true);
        retrievalAgent.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(retrievalAgent.getPowerModifier()).isEqualTo(1);
        assertThat(retrievalAgent.getToughnessModifier()).isEqualTo(-1);
        assertThat(retrievalAgent.isTapped()).isTrue();
    }

    private Permanent addRetrievalAgentReady() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new RetrievalAgent());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
