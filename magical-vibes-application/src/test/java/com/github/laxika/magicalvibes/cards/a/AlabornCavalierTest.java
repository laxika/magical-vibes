package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({AlabornCavalier.class, AlabornTrooper.class, Plains.class})
class AlabornCavalierTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking queues attack trigger for creature target selection")
    void attackingQueuesTargetSelection() {
        addCreatureReady(player1, new AlabornCavalier());
        addCreatureReady(player2, new AlabornTrooper());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Accepting attack may taps target opponent creature")
    void acceptingMayTapsOpponentCreature() {
        addCreatureReady(player1, new AlabornCavalier());
        Permanent trooper = addCreatureReady(player2, new AlabornTrooper());

        attackChooseTargetAndAccept(trooper);

        assertThat(trooper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting attack may taps target creature controlled by the attacker")
    void acceptingMayTapsOwnCreature() {
        addCreatureReady(player1, new AlabornCavalier());
        Permanent trooper = addCreatureReady(player1, new AlabornTrooper());

        attackChooseTargetAndAccept(trooper);

        assertThat(trooper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining attack may leaves target creature untapped")
    void decliningMayLeavesTargetUntapped() {
        addCreatureReady(player1, new AlabornCavalier());
        Permanent trooper = addCreatureReady(player2, new AlabornTrooper());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, trooper.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(trooper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attack trigger rejects noncreature targets")
    void attackTriggerRejectsNoncreatureTargets() {
        addCreatureReady(player1, new AlabornCavalier());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        addCreatureReady(player2, new AlabornTrooper());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, plains.getId()))
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
