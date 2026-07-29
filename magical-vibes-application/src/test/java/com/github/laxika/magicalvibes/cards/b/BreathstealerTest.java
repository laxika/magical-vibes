package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BreathstealerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives +1/-1")
    void activatingAbilityBoosts() {
        Permanent breathstealer = addReadyBreathstealer(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(breathstealer.getPowerModifier()).isEqualTo(1);
        assertThat(breathstealer.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Repeated activations lower toughness to 0, destroying it")
    void repeatedActivationsCanKillIt() {
        Permanent breathstealer = addReadyBreathstealer(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        // 2/2 -> +1/-1 -> 3/1, then +1/-1 again -> 4/0 -> dies to state-based action.
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(breathstealer);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c instanceof Breathstealer);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent breathstealer = addReadyBreathstealer(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(breathstealer.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(breathstealer.getPowerModifier()).isEqualTo(0);
        assertThat(breathstealer.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addReadyBreathstealer(Player player) {
        Breathstealer card = new Breathstealer();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
