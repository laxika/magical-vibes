package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeclareDominance.class, GrizzlyBears.class})
class DeclareDominanceTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +3/+3 and all able creatures must block it")
    void pumpsAndSetsLureRequirement() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID targetId = bears.getId();

        harness.setHand(player1, List.of(new DeclareDominance()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
        assertThat(bears.isMustBeBlockedByAllThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The pump and lure requirement wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID targetId = bears.getId();

        harness.setHand(player1, List.of(new DeclareDominance()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.isMustBeBlockedByAllThisTurn()).isFalse();
    }

    @Test
    void everyAbleCreatureMustBlockTheTargetInsteadOfAnotherAttacker() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeclareDominance()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 1))))
                .isInstanceOf(IllegalStateException.class);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
    }

    @Test
    void tappedCreaturesAreNotRequiredToBlock() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent tapped = addCreatureReady(player2, new GrizzlyBears());
        tapped.tap();
        harness.setHand(player1, List.of(new DeclareDominance()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    void canTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeclareDominance()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(target.isMustBeBlockedByAllThisTurn()).isTrue();
    }

    @Test
    void removedTargetDoesNotTransferEffectsToAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeclareDominance()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
        assertThat(other.isMustBeBlockedByAllThisTurn()).isFalse();
    }
}
