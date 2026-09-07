package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(IntrepidTenderfoot.class)
class IntrepidTenderfootTest extends BaseCardTest {

    @Test
    @DisplayName("Intrepid Tenderfoot's ability puts a +1/+1 counter on it")
    void putsCounterOnItself() {
        Permanent tenderfoot = addCreatureReady(player1, new IntrepidTenderfoot());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(tenderfoot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Intrepid Tenderfoot's ability can only be activated at sorcery speed")
    void abilityRequiresSorcerySpeed() {
        Permanent tenderfoot = addCreatureReady(player1, new IntrepidTenderfoot());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(tenderfoot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
