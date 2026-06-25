package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PyreheartWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Pyreheart Wolf grants menace to all creatures you control")
    void attackGrantsMenaceToAllControlledCreatures() {
        Permanent wolf = addCreatureReady(player1, new PyreheartWolf());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        attackWithWolf();

        assertThat(wolf.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(bears.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(opponentCreature.hasKeyword(Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Menace granted by Pyreheart Wolf stops a single blocker")
    void grantedMenaceStopsSingleBlocker() {
        addCreatureReady(player1, new PyreheartWolf());
        addCreatureReady(player2, new GrizzlyBears());

        attackWithWolf();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    @DisplayName("Menace granted by Pyreheart Wolf allows two blockers")
    void grantedMenaceAllowsTwoBlockers() {
        addCreatureReady(player1, new PyreheartWolf());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        attackWithWolf();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Pyreheart Wolf's granted menace wears off at end of turn")
    void grantedMenaceWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new PyreheartWolf());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        attackWithWolf();

        assertThat(bears.hasKeyword(Keyword.MENACE)).isTrue();

        bears.resetModifiers();

        assertThat(bears.hasKeyword(Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Pyreheart Wolf returns with a +1/+1 counter when it dies without one")
    void undyingReturnsWithCounter() {
        Permanent wolf = addCreatureReady(player1, new PyreheartWolf());

        harness.getPermanentRemovalService().tryDestroyPermanent(gd, wolf);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Pyreheart Wolf"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(perm -> perm.getCard().getName().equals("Pyreheart Wolf"))
                .singleElement()
                .satisfies(returnedWolf ->
                        assertThat(returnedWolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    @DisplayName("Pyreheart Wolf does not return from undying if it had a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent wolf = addCreatureReady(player1, new PyreheartWolf());
        wolf.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.getPermanentRemovalService().tryDestroyPermanent(gd, wolf);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(perm -> perm.getCard().getName().equals("Pyreheart Wolf"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Pyreheart Wolf"));
    }

    private void attackWithWolf() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();
    }
}
