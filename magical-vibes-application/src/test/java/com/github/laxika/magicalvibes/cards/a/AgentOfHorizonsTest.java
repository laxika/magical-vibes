package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgentOfHorizons.class})
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

    @Test
    @DisplayName("A summoning-sick and tapped Agent can activate its ability")
    void abilityDoesNotRequireTapOrHaste() {
        Permanent agent = harness.addToBattlefieldAndReturn(player1, new AgentOfHorizons());
        agent.setSummoningSick(true);
        agent.setTapped(true);
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(agent.isCantBeBlocked()).isFalse();
        harness.passBothPriorities();

        assertThat(agent.isCantBeBlocked()).isTrue();
        assertThat(agent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Three generic mana cannot pay the blue part of the activation cost")
    void abilityRequiresBlueMana() {
        Permanent agent = addAgent();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(agent.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The activated Agent cannot be blocked and other Agents are unaffected")
    void abilityPreventsBlockingOnlyItsSource() {
        Permanent agent = addAgent();
        Permanent otherAgent = addCreatureReady(player1, new AgentOfHorizons());
        Permanent blocker = addCreatureReady(player2, new AgentOfHorizons());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(otherAgent.isCantBeBlocked()).isFalse();
        assertThat(blocker.isCantBeBlocked()).isFalse();
        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(agent.isCantBeBlocked()).isTrue();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
    }

    private Permanent addAgent() {
        return addCreatureReady(player1, new AgentOfHorizons());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
