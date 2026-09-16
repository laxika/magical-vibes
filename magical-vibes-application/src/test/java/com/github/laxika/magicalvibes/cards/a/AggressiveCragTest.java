package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AggressiveCrag.class)
class AggressiveCragTest extends BaseCardTest {

    @Test
    void tapsItselfAtTheBeginningOfYourCombatStep() {
        Permanent crag = harness.addToBattlefieldAndReturn(player1, new AggressiveCrag());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(crag.isTapped()).isTrue();
    }

    @Test
    void tapsForRedOrWhiteMana() {
        Permanent crag = harness.addToBattlefieldAndReturn(player1, new AggressiveCrag());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(crag.isTapped()).isTrue();
    }
}
