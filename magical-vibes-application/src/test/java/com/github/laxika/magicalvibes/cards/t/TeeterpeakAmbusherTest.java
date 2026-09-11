package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TeeterpeakAmbusher.class)
class TeeterpeakAmbusherTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +2/+0 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent ambusher = addReadyAmbusher();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ambusher.getPowerModifier()).isEqualTo(2);
        assertThat(ambusher.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent ambusher = addReadyAmbusher();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ambusher.getPowerModifier()).isEqualTo(0);
        assertThat(ambusher.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addReadyAmbusher() {
        return addCreatureReady(player1, new TeeterpeakAmbusher());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
