package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({RoarOfChallenge.class, GrizzlyBears.class, AirElemental.class})
class RoarOfChallengeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving requires all able creatures to block the target")
    void resolvingRequiresAllAbleCreaturesToBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent blocker2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castRoar(attacker);

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(blocker1.isBlocking()).isTrue();
        assertThat(blocker2.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Ferocious grants the target indestructible")
    void ferociousGrantsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        castRoar(target);

        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Without ferocious the target does not gain indestructible")
    void withoutFerociousDoesNotGrantIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castRoar(target);

        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Ferocious indestructible wears off at end of turn")
    void ferociousIndestructibleWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        castRoar(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The targeted creature itself can satisfy ferocious")
    void targetItselfSatisfiesFerocious() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        castRoar(target);

        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's four-power creature does not satisfy ferocious")
    void opponentsCreatureDoesNotSatisfyFerocious() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castRoar(target);

        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Ferocious can grant indestructible to an opponent's creature")
    void ferociousCanProtectOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        castRoar(target);

        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Ferocious checks the battlefield at resolution")
    void ferociousChecksAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RoarOfChallenge()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, target.getId());

        harness.addToBattlefield(player1, new AirElemental());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Ground creatures need not block a flying target")
    void groundCreaturesNeedNotBlockFlyingTarget() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent groundBlocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent flyingBlocker = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castRoar(attacker);

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(groundBlocker.isBlocking()).isFalse();
        assertThat(flyingBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures need not block the target")
    void tappedCreaturesNeedNotBlockTarget() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent tappedBlocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ableBlocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tappedBlocker.tap();
        castRoar(attacker);

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(tappedBlocker.isBlocking()).isFalse();
        assertThat(ableBlocker.isBlocking()).isTrue();
    }

    private void castRoar(Permanent target) {
        harness.setHand(player1, List.of(new RoarOfChallenge()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
