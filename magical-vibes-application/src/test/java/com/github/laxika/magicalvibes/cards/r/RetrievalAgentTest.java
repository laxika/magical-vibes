package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addRetrievalAgentReady() {
        Permanent permanent = new Permanent(new RetrievalAgent());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
