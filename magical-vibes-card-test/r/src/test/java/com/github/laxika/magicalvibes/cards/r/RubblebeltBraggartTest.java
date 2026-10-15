package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(RubblebeltBraggart.class)
class RubblebeltBraggartTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking lets Rubblebelt Braggart become suspected")
    void attackingMaySuspectIt() {
        Permanent braggart = addCreatureReady(player1, new RubblebeltBraggart());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(braggart.isSuspected()).isTrue();
    }

    @Test
    @DisplayName("Declining the attack trigger leaves Rubblebelt Braggart unsuspected")
    void decliningDoesNotSuspectIt() {
        Permanent braggart = addCreatureReady(player1, new RubblebeltBraggart());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(braggart.isSuspected()).isFalse();
    }

    @Test
    @DisplayName("A suspected Rubblebelt Braggart does not trigger when it attacks")
    void suspectedBraggartDoesNotTrigger() {
        Permanent braggart = addCreatureReady(player1, new RubblebeltBraggart());
        braggart.setSuspected(true);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(braggart.isSuspected()).isTrue();
    }

    @Test
    @DisplayName("Becoming suspected in response prevents the optional choice on resolution")
    void suspectedConditionIsCheckedAgainOnResolution() {
        Permanent braggart = addCreatureReady(player1, new RubblebeltBraggart());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        braggart.setSuspected(true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(braggart.isSuspected()).isTrue();
    }

    @Test
    @DisplayName("Suspecting the attacker gives it menace before blockers are declared")
    void suspectedAttackerCannotBeBlockedByOneCreature() {
        addCreatureReady(player1, new RubblebeltBraggart());
        addCreatureReady(player2, new RubblebeltBraggart());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A suspected Rubblebelt Braggart cannot block")
    void suspectedBraggartCannotBlock() {
        addCreatureReady(player1, new RubblebeltBraggart());
        Permanent blocker = addCreatureReady(player2, new RubblebeltBraggart());
        blocker.setSuspected(true);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
