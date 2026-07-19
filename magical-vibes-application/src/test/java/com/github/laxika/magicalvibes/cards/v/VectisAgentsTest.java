package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VectisAgentsTest extends BaseCardTest {

    // ===== {U}{B}: -2/-0 until end of turn and can't be blocked this turn =====

    @Test
    @DisplayName("Resolving ability gives -2/-0 and makes Vectis Agents unblockable")
    void resolvingAbilityAppliesEffects() {
        Permanent agents = addAgentsReady(player1);
        int basePower = agents.getEffectivePower();
        addUbMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(agents.getEffectivePower()).isEqualTo(basePower - 2);
        assertThat(agents.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Toughness is unchanged by the ability")
    void toughnessUnchanged() {
        Permanent agents = addAgentsReady(player1);
        int baseToughness = agents.getEffectiveToughness();
        addUbMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(agents.getEffectiveToughness()).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Both effects wear off at end of turn cleanup")
    void effectsWearOffAtEndOfTurn() {
        Permanent agents = addAgentsReady(player1);
        int basePower = agents.getEffectivePower();
        addUbMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(agents.getEffectivePower()).isEqualTo(basePower - 2);
        assertThat(agents.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(agents.getEffectivePower()).isEqualTo(basePower);
        assertThat(agents.isCantBeBlocked()).isFalse();
    }

    // ===== Helpers =====

    private Permanent addAgentsReady(Player player) {
        Permanent perm = new Permanent(new VectisAgents());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addUbMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
