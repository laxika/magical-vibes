package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bedlam.class, GrizzlyBears.class, Disenchant.class})
class BedlamTest extends BaseCardTest {

    @Test
    @DisplayName("No creature can block while Bedlam is on the battlefield")
    void creatureCannotBlockWithBedlam() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Bedlam());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures can't block");
    }

    @Test
    @DisplayName("Bedlam even stops its own controller's creatures from blocking")
    void controllerCreatureCannotBlockWithBedlam() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Bedlam());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures can't block");
    }

    @Test
    @DisplayName("Without Bedlam a creature blocks normally")
    void creatureBlocksWithoutBedlam() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Bedlam skips blocker input when no legal block exists")
    void bedlamDoesNotOpenBlockerInputWhenNoBlockIsPossible() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Bedlam());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.inMutationScope(() -> harness.getCombatBlockService().handleDeclareBlockersStep(gd));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("Creatures entering after Bedlam cannot block, but can still attack")
    void laterCreaturesCannotBlockButCanAttack() {
        harness.addToBattlefield(player1, new Bedlam());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThat(attacker.isAttacking()).isTrue();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures can't block");
    }

    @Test
    @DisplayName("Destroying Bedlam restores the ability to block")
    void destroyingBedlamAllowsBlocking() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent bedlam = harness.addToBattlefieldAndReturn(player1, new Bedlam());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, bedlam.getId());
        harness.assertInGraveyard(player1, "Bedlam");
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Destroying one Bedlam does not end another Bedlam's restriction")
    void anotherBedlamStillPreventsBlocking() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent bedlam = harness.addToBattlefieldAndReturn(player1, new Bedlam());
        harness.addToBattlefield(player2, new Bedlam());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, bedlam.getId());
        harness.assertInGraveyard(player1, "Bedlam");
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures can't block");
    }
}
