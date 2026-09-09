package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({SeasonedMarshal.class, GrizzlyBears.class, Island.class})
class SeasonedMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking queues attack trigger for creature target selection")
    void attackingQueuesTargetSelection() {
        addCreatureReady(player1, new SeasonedMarshal());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Accepting attack may taps target opponent creature")
    void acceptingMayTapsOpponentCreature() {
        addCreatureReady(player1, new SeasonedMarshal());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        attackChooseTargetAndAccept(bears);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting attack may taps a creature I control")
    void acceptingMayTapsOwnCreature() {
        addCreatureReady(player1, new SeasonedMarshal());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        attackChooseTargetAndAccept(bears);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting attack may leaves an already tapped target tapped")
    void acceptingMayLeavesAlreadyTappedTargetTapped() {
        addCreatureReady(player1, new SeasonedMarshal());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();

        attackChooseTargetAndAccept(bears);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining attack may leaves target creature untapped")
    void decliningMayLeavesTargetUntapped() {
        addCreatureReady(player1, new SeasonedMarshal());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attack trigger rejects noncreature targets")
    void attackTriggerRejectsNoncreatureTargets() {
        addCreatureReady(player1, new SeasonedMarshal());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private void attackChooseTargetAndAccept(Permanent target) {
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

}
