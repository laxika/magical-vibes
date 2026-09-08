package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlluringScent.class, GrizzlyBears.class})
class AlluringScentTest extends BaseCardTest {

    // ===== Casting / resolving =====

    @Test
    @DisplayName("Resolving sets the must-be-blocked-by-all flag on the target")
    void resolvingSetsFlag() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new AlluringScent()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bears.isMustBeBlockedByAllThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Resolving sets only the by-all flag, not the weaker must-be-blocked-if-able one")
    void resolvingDoesNotSetTheIfAbleFlag() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new AlluringScent()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bears.isMustBeBlockedThisTurn()).isFalse();
        assertThat(bears.isMustAttackThisTurn()).isFalse();
        assertThat(bears.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    @DisplayName("Flag wears off at end of turn")
    void flagWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new AlluringScent()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bears.isMustBeBlockedByAllThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Casting Alluring Scent requires every able blocker to block the target")
    void castingRequiresEveryAbleBlockerToBlockTarget() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new AlluringScent()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, attacker.getId());
        harness.passBothPriorities();
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    // ===== Combat enforcement =====

    @Test
    @DisplayName("All able creatures must block the affected attacker")
    void allAbleCreaturesMustBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setMustBeBlockedByAllThisTurn(true);
        attacker.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        // Only one blocker assigned — illegal, both must block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block")
    void tappedCreaturesNotForcedToBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setMustBeBlockedByAllThisTurn(true);
        attacker.setAttacking(true);

        Permanent untapped = addCreatureReady(player2, new GrizzlyBears());
        Permanent tapped = addCreatureReady(player2, new GrizzlyBears());
        tapped.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

}
