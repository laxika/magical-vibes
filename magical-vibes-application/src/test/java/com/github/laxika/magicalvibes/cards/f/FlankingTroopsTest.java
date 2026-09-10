package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlankingTroops.class, ForestBear.class, Forest.class})
class FlankingTroopsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking queues attack trigger for creature target selection")
    void attackingQueuesTargetSelection() {
        addCreatureReady(player1, new FlankingTroops());
        addCreatureReady(player2, new ForestBear());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Accepting attack may taps target opponent creature")
    void acceptingMayTapsOpponentCreature() {
        addCreatureReady(player1, new FlankingTroops());
        Permanent forestBear = addCreatureReady(player2, new ForestBear());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, forestBear.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(forestBear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting attack may taps a creature controlled by the attacker")
    void acceptingMayTapsOwnCreature() {
        addCreatureReady(player1, new FlankingTroops());
        Permanent forestBear = addCreatureReady(player1, new ForestBear());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, forestBear.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(forestBear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining attack may leaves target creature untapped")
    void decliningMayLeavesTargetUntapped() {
        addCreatureReady(player1, new FlankingTroops());
        Permanent forestBear = addCreatureReady(player2, new ForestBear());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, forestBear.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(forestBear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attack trigger rejects noncreature targets")
    void attackTriggerRejectsNoncreatureTargets() {
        addCreatureReady(player1, new FlankingTroops());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        addCreatureReady(player2, new ForestBear());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }
}
