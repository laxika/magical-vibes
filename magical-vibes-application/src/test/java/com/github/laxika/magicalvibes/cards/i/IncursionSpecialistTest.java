package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IncursionSpecialistTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell boosts Incursion Specialist and makes it unblockable until end of turn")
    void secondSpellBoostsAndMakesUnblockableUntilEndOfTurn() {
        Permanent specialist = addCreatureReady(player1, new IncursionSpecialist());
        int initialPower = specialist.getEffectivePower();

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(specialist.getEffectivePower()).isEqualTo(initialPower);
        assertThat(specialist.isCantBeBlocked()).isFalse();
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(specialist.getEffectivePower()).isEqualTo(initialPower + 2);
        assertThat(specialist.isCantBeBlocked()).isTrue();
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(specialist.getEffectivePower()).isEqualTo(initialPower + 2);
        assertThat(specialist.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(specialist.getEffectivePower()).isEqualTo(initialPower);
        assertThat(specialist.isCantBeBlocked()).isFalse();
    }

}
