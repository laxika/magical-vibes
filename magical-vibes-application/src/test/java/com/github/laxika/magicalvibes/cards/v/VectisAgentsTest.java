package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VectisAgents.class})
class VectisAgentsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives -2/-0 and makes Vectis Agents unblockable")
    void resolvingAbilityAppliesEffects() {
        Permanent agents = addCreatureReady(player1, new VectisAgents());
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
        Permanent agents = addCreatureReady(player1, new VectisAgents());
        int baseToughness = agents.getEffectiveToughness();
        addUbMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(agents.getEffectiveToughness()).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Both effects wear off at end of turn cleanup")
    void effectsWearOffAtEndOfTurn() {
        Permanent agents = addCreatureReady(player1, new VectisAgents());
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

    @Test
    @DisplayName("Repeated activations each reduce power and affect only their source")
    void repeatedActivationsStackOnlyOnSource() {
        Permanent agents = addCreatureReady(player1, new VectisAgents());
        Permanent other = addCreatureReady(player1, new VectisAgents());
        int basePower = agents.getEffectivePower();
        int otherPower = other.getEffectivePower();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(agents.getEffectivePower()).isEqualTo(basePower - 6);
        assertThat(agents.isCantBeBlocked()).isTrue();
        assertThat(other.getEffectivePower()).isEqualTo(otherPower);
        assertThat(other.isCantBeBlocked()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(agents);
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent agents = harness.addToBattlefieldAndReturn(player1, new VectisAgents());
        agents.setSummoningSick(true);
        agents.tap();
        int basePower = agents.getEffectivePower();
        addUbMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(agents.getEffectivePower()).isEqualTo(basePower - 2);
        assertThat(agents.isCantBeBlocked()).isTrue();
        assertThat(agents.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving the ability prevents an opposing creature from blocking")
    void preventsBlockDeclaration() {
        Permanent agents = addCreatureReady(player1, new VectisAgents());
        addCreatureReady(player2, new VectisAgents());
        addUbMana(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        agents.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    private void addUbMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
