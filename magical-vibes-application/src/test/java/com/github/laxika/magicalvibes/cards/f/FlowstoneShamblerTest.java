package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlowstoneShamblerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives +1/-1")
    void activatingAbilityBoosts() {
        Permanent shambler = addReadyShambler(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shambler.getPowerModifier()).isEqualTo(1);
        assertThat(shambler.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Repeated activations lower toughness to 0, destroying it")
    void repeatedActivationsCanKillIt() {
        Permanent shambler = addReadyShambler(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        // 2/2 -> +1/-1 -> 3/1, then +1/-1 again -> 4/0 -> dies to state-based action.
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(shambler);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c instanceof FlowstoneShambler);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent shambler = addReadyShambler(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shambler.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shambler.getPowerModifier()).isEqualTo(0);
        assertThat(shambler.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addReadyShambler(Player player) {
        FlowstoneShambler card = new FlowstoneShambler();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
