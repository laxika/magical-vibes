package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.Lurker;
import com.github.laxika.magicalvibes.cards.t.Twiddle;
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

@CardUsed({SorrowsPath.class, Lurker.class, Twiddle.class, SpittingSlug.class})
class SorrowsPathTest extends BaseCardTest {

    @Test
    @DisplayName("Swaps the creatures blocked by two target blockers")
    void swapsBlockingAssignments() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new SorrowsPath());
        Permanent attackerA = addCreatureReady(player1, new Lurker());
        Permanent attackerB = addCreatureReady(player1, new Lurker());
        Permanent blockerA = addCreatureReady(player2, new Lurker());
        Permanent blockerB = addCreatureReady(player2, new Lurker());
        setupCombat(attackerA, attackerB);
        assignBlocker(blockerA, attackerA);
        assignBlocker(blockerB, attackerB);

        activatePath(path, blockerA, blockerB);
        resolveAllTriggers();

        assertThat(path.isTapped()).isTrue();
        assertThat(blockerA.getBlockingTargetIds()).containsExactly(attackerB.getId());
        assertThat(blockerB.getBlockingTargetIds()).containsExactly(attackerA.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(attackerA.getMarkedDamage()).isEqualTo(2);
        assertThat(attackerB.getMarkedDamage()).isEqualTo(2);
        assertThat(blockerA.getMarkedDamage()).isZero();
        assertThat(blockerB.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not swap when a blocker cannot block the other's attacker")
    void doesNotSwapWhenBlockLegalityFails() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new SorrowsPath());
        Permanent groundAttacker = addCreatureReady(player1, new Lurker());
        Permanent flyingAttacker = addCreatureReady(player1, new Lurker());
        flyingAttacker.getGrantedKeywords().add(Keyword.FLYING);
        Permanent blockerA = addCreatureReady(player2, new Lurker());
        Permanent blockerB = addCreatureReady(player2, new Lurker());
        setupCombat(groundAttacker, flyingAttacker);
        blockerB.getGrantedKeywords().add(Keyword.REACH);
        assignBlocker(blockerA, groundAttacker);
        assignBlocker(blockerB, flyingAttacker);

        activatePath(path, blockerA, blockerB);
        resolveAllTriggers();

        assertThat(blockerA.getBlockingTargetIds()).containsExactly(groundAttacker.getId());
        assertThat(blockerB.getBlockingTargetIds()).containsExactly(flyingAttacker.getId());
    }

    @Test
    @DisplayName("Does not swap when a blocker cannot block the entire opposing group")
    void doesNotSwapWhenBlockCapacityFails() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new SorrowsPath());
        Permanent attackerA = addCreatureReady(player1, new Lurker());
        Permanent attackerB = addCreatureReady(player1, new Lurker());
        Permanent attackerC = addCreatureReady(player1, new Lurker());
        Permanent blockerA = addCreatureReady(player2, new Lurker());
        Permanent blockerB = addCreatureReady(player2, new Lurker());
        setupCombat(attackerA, attackerB, attackerC);
        blockerA.setAdditionalBlocksUntilEndOfTurn(1);
        assignBlocker(blockerA, attackerA);
        assignBlocker(blockerA, attackerB);
        assignBlocker(blockerB, attackerC);

        activatePath(path, blockerA, blockerB);
        resolveAllTriggers();

        assertThat(blockerA.getBlockingTargetIds()).containsExactly(attackerA.getId(), attackerB.getId());
        assertThat(blockerB.getBlockingTargetIds()).containsExactly(attackerC.getId());
    }

    @Test
    void triggersWhenTappedByAnotherEffect() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new SorrowsPath());
        Permanent ownCreature = addCreatureReady(player1, new Lurker());
        Permanent opponentCreature = addCreatureReady(player2, new Lurker());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Twiddle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, path.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(path.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opponentCreature.getMarkedDamage()).isZero();
    }

    @Test
    void swapsWithEmptyGroupAfterAttackerLeavesCombat() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new SorrowsPath());
        Permanent attackerA = addCreatureReady(player1, new Lurker());
        Permanent attackerB = addCreatureReady(player1, new Lurker());
        Permanent blockerA = addCreatureReady(player2, new Lurker());
        Permanent blockerB = addCreatureReady(player2, new Lurker());
        setupCombat(attackerA, attackerB);
        assignBlocker(blockerA, attackerA);
        assignBlocker(blockerB, attackerB);

        activatePath(path, blockerA, blockerB);
        harness.passBothPriorities();
        attackerA.setAttacking(false);
        resolveAllTriggers();

        assertThat(blockerA.getBlockingTargetIds()).containsExactly(attackerB.getId());
        assertThat(blockerB.isBlocking()).isFalse();
        assertThat(blockerB.getBlockingTargetIds()).isEmpty();
    }

    @Test
    void swappingCausesBlockingAbilityToTriggerAgain() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new SorrowsPath());
        Permanent attackerA = addCreatureReady(player1, new Lurker());
        Permanent attackerB = addCreatureReady(player1, new Lurker());
        Permanent slug = addCreatureReady(player2, new SpittingSlug());
        Permanent blocker = addCreatureReady(player2, new Lurker());
        setupCombat(attackerA, attackerB);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 2)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gqs.hasKeyword(gd, attackerA, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attackerB, Keyword.FIRST_STRIKE)).isFalse();

        activatePath(path, slug, blocker);
        resolveAllTriggers();

        assertThat(slug.getBlockingTargetIds()).containsExactly(attackerB.getId());
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gqs.hasKeyword(gd, attackerB, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void tappedBlockerPreventsSwapButNotTapDamage() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new SorrowsPath());
        Permanent attackerA = addCreatureReady(player1, new Lurker());
        Permanent attackerB = addCreatureReady(player1, new Lurker());
        Permanent blockerA = addCreatureReady(player2, new Lurker());
        Permanent blockerB = addCreatureReady(player2, new Lurker());
        setupCombat(attackerA, attackerB);
        assignBlocker(blockerA, attackerA);
        assignBlocker(blockerB, attackerB);
        blockerA.tap();

        activatePath(path, blockerA, blockerB);
        resolveAllTriggers();

        assertThat(blockerA.getBlockingTargetIds()).containsExactly(attackerA.getId());
        assertThat(blockerB.getBlockingTargetIds()).containsExactly(attackerB.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(attackerA.getMarkedDamage()).isEqualTo(2);
        assertThat(attackerB.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void removingOneTargetDoesNotPreventTapDamage() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new SorrowsPath());
        Permanent attackerA = addCreatureReady(player1, new Lurker());
        Permanent attackerB = addCreatureReady(player1, new Lurker());
        Permanent blockerA = addCreatureReady(player2, new Lurker());
        Permanent blockerB = addCreatureReady(player2, new Lurker());
        setupCombat(attackerA, attackerB);
        assignBlocker(blockerA, attackerA);
        assignBlocker(blockerB, attackerB);

        activatePath(path, blockerA, blockerB);
        gd.playerBattlefields.get(player2.getId()).remove(blockerA);
        resolveAllTriggers();

        assertThat(blockerB.getBlockingTargetIds()).containsExactly(attackerB.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(attackerA.getMarkedDamage()).isEqualTo(2);
        assertThat(attackerB.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void untappingPathDoesNotTriggerDamage() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new SorrowsPath());
        path.tap();
        Permanent creature = addCreatureReady(player1, new Lurker());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Twiddle()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, path.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(path.isTapped()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    void tapDamageStillResolvesAfterPathLeavesBattlefield() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new SorrowsPath());
        Permanent attackerA = addCreatureReady(player1, new Lurker());
        Permanent attackerB = addCreatureReady(player1, new Lurker());
        Permanent blockerA = addCreatureReady(player2, new Lurker());
        Permanent blockerB = addCreatureReady(player2, new Lurker());
        setupCombat(attackerA, attackerB);
        assignBlocker(blockerA, attackerA);
        assignBlocker(blockerB, attackerB);

        activatePath(path, blockerA, blockerB);
        gd.playerBattlefields.get(player1.getId()).remove(path);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(attackerA.getMarkedDamage()).isEqualTo(2);
        assertThat(attackerB.getMarkedDamage()).isEqualTo(2);
        assertThat(blockerA.getBlockingTargetIds()).containsExactly(attackerB.getId());
        assertThat(blockerB.getBlockingTargetIds()).containsExactly(attackerA.getId());
    }

    private void activatePath(Permanent path, Permanent blockerA, Permanent blockerB) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(path),
                0,
                List.of(blockerA.getId(), blockerB.getId()));
    }

    private void setupCombat(Permanent... attackers) {
        for (Permanent attacker : attackers) {
            attacker.setAttacking(true);
        }
    }

    private void assignBlocker(Permanent blocker, Permanent attacker) {
        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));
        blocker.addBlockingTargetId(attacker.getId());
    }
}
