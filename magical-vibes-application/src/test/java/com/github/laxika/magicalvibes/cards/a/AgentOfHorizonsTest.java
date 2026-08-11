package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentOfHorizonsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability makes Agent of Horizons unblockable this turn")
    void abilityMakesSelfUnblockable() {
        Permanent agent = addAgent();
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(agent.isCantBeBlocked()).isTrue();
        assertThat(agent.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Unblockable wears off during cleanup")
    void unblockableWearsOff() {
        Permanent agent = addAgent();
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(agent.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(agent.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Ability cannot be activated without enough mana")
    void abilityRequiresMana() {
        Permanent agent = addAgent();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(agent.isCantBeBlocked()).isFalse();
    }

    private Permanent addAgent() {
        Permanent agent = harness.addToBattlefieldAndReturn(player1, new AgentOfHorizons());
        agent.setSummoningSick(false);
        return agent;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
