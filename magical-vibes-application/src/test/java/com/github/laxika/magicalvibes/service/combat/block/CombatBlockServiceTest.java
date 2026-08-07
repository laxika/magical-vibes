package com.github.laxika.magicalvibes.service.combat.block;

import com.github.laxika.magicalvibes.cards.a.AwesomePresence;
import com.github.laxika.magicalvibes.cards.d.DreamProwler;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.Hipparion;
import com.github.laxika.magicalvibes.cards.j.JackalFamiliar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PalaceGuard;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.s.ScrapdiverSerpent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.combat.CombatResult;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Spec for {@link CombatBlockService}, the declare-blockers step itself: which creatures and
 * attackers the defender is offered, what the step returns when no block is possible, and the
 * declaration-time validation that no single card's legality can express — index bounds, how many
 * times one blocker may be used, and the summed additional cost of a whole declaration.
 *
 * <p>Per-pair evasion legality is not retested here; that is {@link BlockLegalityContextTest}'s
 * spec. Cards appear only as the cheapest way to reach a rule.
 */
class CombatBlockServiceTest extends BaseCardTest {

    private CombatBlockService service() {
        return GameTestEngineContext.get().getBean(CombatBlockService.class);
    }

    private Permanent attacking(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.setAttacking(true);
        return perm;
    }

    /** Puts the game in the state {@code declareBlockers} expects: player1 attacking, player2 to block. */
    private void enterDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private int defenderIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player2.getId()).indexOf(permanent);
    }

    private int attackerIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    @Nested
    @DisplayName("Blockers offered to the defender")
    class BlockableCreatures {

        @Test
        @DisplayName("Only untapped creatures are offered")
        void onlyUntappedCreaturesAreOffered() {
            Permanent ready = addCreatureReady(player2, new GrizzlyBears());
            Permanent tapped = addCreatureReady(player2, new ScatheZombies());
            tapped.tap();
            harness.addToBattlefield(player2, new Forest());

            assertThat(service().getBlockableCreatureIndices(gd, player2.getId()))
                    .containsExactly(defenderIndex(ready));
        }

        @Test
        @DisplayName("CR 509.1a: a lone \"can't block alone\" creature leaves no legal blockers")
        void loneCantBlockAloneCreatureIsNotOffered() {
            Permanent familiar = addCreatureReady(player2, new JackalFamiliar());

            assertThat(service().getBlockableCreatureIndices(gd, player2.getId())).isEmpty();

            Permanent ally = addCreatureReady(player2, new GrizzlyBears());
            assertThat(service().getBlockableCreatureIndices(gd, player2.getId()))
                    .containsExactly(defenderIndex(familiar), defenderIndex(ally));
        }
    }

    @Nested
    @DisplayName("Attackers the defender may be asked to block")
    class BlockableAttackers {

        @Test
        @DisplayName("Non-attacking creatures and unblockable attackers are filtered out")
        void unblockableAttackersAreFilteredOut() {
            Permanent blockable = attacking(player1, new GrizzlyBears());
            Permanent unblockable = attacking(player1, new ScatheZombies());
            unblockable.setCantBeBlocked(true);
            addCreatureReady(player1, new HillGiant());
            addCreatureReady(player2, new GrizzlyBears());

            assertThat(service().getBlockableAttackerIndices(gd, player1.getId(), player2.getId()))
                    .containsExactly(attackerIndex(blockable));
        }

        @Test
        @DisplayName("An attacker unblockable because of the defender's board is filtered out")
        void defenderConditionUnblockableAttackerIsFilteredOut() {
            Permanent serpent = attacking(player1, new ScrapdiverSerpent());
            addCreatureReady(player2, new GrizzlyBears());

            assertThat(service().getBlockableAttackerIndices(gd, player1.getId(), player2.getId()))
                    .containsExactly(attackerIndex(serpent));

            // Scrapdiver Serpent can't be blocked while the defending player controls an artifact.
            addCreatureReady(player2, new Ornithopter());
            assertThat(service().getBlockableAttackerIndices(gd, player1.getId(), player2.getId()))
                    .isEmpty();
        }

        @Test
        @DisplayName("An attacker unblockable while attacking alone becomes blockable once joined")
        void attackingAloneUnblockableAttackerBecomesBlockableWhenJoined() {
            Permanent prowler = attacking(player1, new DreamProwler());
            addCreatureReady(player2, new GrizzlyBears());

            assertThat(service().getBlockableAttackerIndices(gd, player1.getId(), player2.getId()))
                    .isEmpty();

            Permanent friend = attacking(player1, new GrizzlyBears());
            assertThat(service().getBlockableAttackerIndices(gd, player1.getId(), player2.getId()))
                    .containsExactly(attackerIndex(prowler), attackerIndex(friend));
        }
    }

    @Nested
    @DisplayName("Declare-blockers step")
    class DeclareBlockersStep {

        private CombatResult handleStep() {
            CombatResult[] result = new CombatResult[1];
            harness.inMutationScope(() -> result[0] = service().handleDeclareBlockersStep(gd));
            return result[0];
        }

        @Test
        @DisplayName("With no possible block the step auto-passes without asking the defender")
        void noPossibleBlockAutoPasses() {
            attacking(player1, new GrizzlyBears());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);

            assertThat(handleStep()).isEqualTo(CombatResult.AUTO_PASS_ONLY);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class)).isNull();
        }

        @Test
        @DisplayName("With a possible block the defender is asked, and the legal pairs are captured")
        void possibleBlockOpensTheDeclarationWithLegalPairs() {
            Permanent attacker = attacking(player1, new GrizzlyBears());
            Permanent unblockable = attacking(player1, new ScatheZombies());
            unblockable.setCantBeBlocked(true);
            Permanent blocker = addCreatureReady(player2, new HillGiant());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);

            assertThat(handleStep()).isEqualTo(CombatResult.DONE);

            PendingInteraction.BlockerDeclaration declaration =
                    gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
            assertThat(declaration).isNotNull();
            assertThat(declaration.defenderId()).isEqualTo(player2.getId());
            assertThat(declaration.blockerIndices()).containsExactly(defenderIndex(blocker));
            assertThat(declaration.attackerIndices()).containsExactly(attackerIndex(attacker));
            assertThat(declaration.legalBlockPairs())
                    .containsOnlyKeys(defenderIndex(blocker));
            assertThat(declaration.legalBlockPairs().get(defenderIndex(blocker)))
                    .containsExactly(attackerIndex(attacker));
        }
    }

    @Nested
    @DisplayName("Declaration validation")
    class DeclarationValidation {

        @Test
        @DisplayName("Declaring outside the blocker-declaration interaction is rejected")
        void declaringWithoutAPendingDeclarationIsRejected() {
            attacking(player1, new GrizzlyBears());
            addCreatureReady(player2, new GrizzlyBears());
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();

            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not awaiting blocker declaration");
        }

        @Test
        @DisplayName("Only the defending player may declare blockers")
        void attackingPlayerMayNotDeclareBlockers() {
            attacking(player1, new GrizzlyBears());
            addCreatureReady(player2, new GrizzlyBears());
            enterDeclareBlockers();

            assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only the defending player can declare blockers");
        }

        @Test
        @DisplayName("A creature that can't block at all is rejected as a blocker index")
        void ineligibleBlockerIndexIsRejected() {
            Permanent attacker = attacking(player1, new GrizzlyBears());
            Permanent tapped = addCreatureReady(player2, new GrizzlyBears());
            tapped.tap();
            enterDeclareBlockers();

            assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                    List.of(new BlockerAssignment(defenderIndex(tapped), attackerIndex(attacker)))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid blocker index");
        }

        @Test
        @DisplayName("An out-of-range or non-attacking attacker index is rejected")
        void invalidAttackerIndexIsRejected() {
            attacking(player1, new GrizzlyBears());
            Permanent idle = addCreatureReady(player1, new HillGiant());
            Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
            enterDeclareBlockers();

            int blockerIdx = defenderIndex(blocker);
            assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                    List.of(new BlockerAssignment(blockerIdx, 99))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid attacker index");

            harness.beginBlockerDeclarationInput();
            assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                    List.of(new BlockerAssignment(blockerIdx, attackerIndex(idle)))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid attacker index");
        }

        @Test
        @DisplayName("The same blocker-attacker pair may not be declared twice")
        void duplicatePairIsRejected() {
            Permanent attacker = attacking(player1, new GrizzlyBears());
            // Palace Guard may block any number of creatures, so only the duplicate-pair rule
            // can reject this declaration.
            Permanent guard = addCreatureReady(player2, new PalaceGuard());
            enterDeclareBlockers();

            int blockerIdx = defenderIndex(guard);
            int attackerIdx = attackerIndex(attacker);
            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(blockerIdx, attackerIdx),
                    new BlockerAssignment(blockerIdx, attackerIdx))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Duplicate blocker-attacker pair");
        }

        @Test
        @DisplayName("CR 509.1a: one blocker blocks one attacker unless something says otherwise")
        void blockerMayNotBeAssignedMoreOftenThanItsBlockLimit() {
            Permanent first = attacking(player1, new GrizzlyBears());
            Permanent second = attacking(player1, new ScatheZombies());
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            enterDeclareBlockers();

            int bearsIdx = defenderIndex(bears);
            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(bearsIdx, attackerIndex(first)),
                    new BlockerAssignment(bearsIdx, attackerIndex(second)))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("assigned too many times");
        }

        @Test
        @DisplayName("\"Can block any number of creatures\" lifts the block limit")
        void canBlockAnyNumberLiftsTheBlockLimit() {
            Permanent first = attacking(player1, new GrizzlyBears());
            Permanent second = attacking(player1, new ScatheZombies());
            Permanent third = attacking(player1, new HillGiant());
            Permanent guard = addCreatureReady(player2, new PalaceGuard());
            enterDeclareBlockers();

            int guardIdx = defenderIndex(guard);
            gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(guardIdx, attackerIndex(first)),
                    new BlockerAssignment(guardIdx, attackerIndex(second)),
                    new BlockerAssignment(guardIdx, attackerIndex(third))));

            assertThat(guard.getBlockingTargetIds())
                    .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        }
    }

    @Nested
    @DisplayName("Additional block costs")
    class BlockTax {

        @Test
        @DisplayName("The cost of every declared block is summed and must be payable as one total")
        void blockCostsAreSummedAcrossTheWholeDeclaration() {
            // Hipparion: "can't block creatures with power 3 or greater unless you pay {1}".
            Permanent firstGiant = attacking(player1, new HillGiant());
            Permanent secondGiant = attacking(player1, new HillGiant());
            Permanent firstHipparion = addCreatureReady(player2, new Hipparion());
            Permanent secondHipparion = addCreatureReady(player2, new Hipparion());
            enterDeclareBlockers();

            List<BlockerAssignment> bothBlock = List.of(
                    new BlockerAssignment(defenderIndex(firstHipparion), attackerIndex(firstGiant)),
                    new BlockerAssignment(defenderIndex(secondHipparion), attackerIndex(secondGiant)));

            // Enough for one block but not for both.
            harness.addMana(player2, ManaColor.WHITE, 1);
            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, bothBlock))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough mana to pay block cost (2 required)");

            harness.addMana(player2, ManaColor.WHITE, 1);
            harness.beginBlockerDeclarationInput();
            assertThatCode(() -> gs.declareBlockers(gd, player2, bothBlock)).doesNotThrowAnyException();

            assertThat(firstHipparion.isBlocking()).isTrue();
            assertThat(secondHipparion.isBlocking()).isTrue();
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("An attacker-side per-blocker tax stacks with the blocker's own block cost")
        void attackerSideTaxStacksWithBlockerCost() {
            // Awesome Presence on the attacker ({3} per blocker) + Hipparion blocking power 3+ ({1}).
            Permanent giant = attacking(player1, new HillGiant());
            Permanent presence = new Permanent(new AwesomePresence());
            presence.setAttachedTo(giant.getId());
            gd.playerBattlefields.get(player1.getId()).add(presence);
            Permanent hipparion = addCreatureReady(player2, new Hipparion());
            enterDeclareBlockers();

            List<BlockerAssignment> block = List.of(
                    new BlockerAssignment(defenderIndex(hipparion), attackerIndex(giant)));

            harness.addMana(player2, ManaColor.WHITE, 3);
            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, block))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough mana to pay block cost (4 required)");

            harness.addMana(player2, ManaColor.WHITE, 1);
            harness.beginBlockerDeclarationInput();
            assertThatCode(() -> gs.declareBlockers(gd, player2, block)).doesNotThrowAnyException();

            assertThat(hipparion.isBlocking()).isTrue();
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("A block below the cost threshold is free and spends nothing")
        void untaxedBlockSpendsNoMana() {
            // Grizzly Bears is power 2, under Hipparion's "power 3 or greater" threshold.
            Permanent bears = attacking(player1, new GrizzlyBears());
            Permanent hipparion = addCreatureReady(player2, new Hipparion());
            harness.addMana(player2, ManaColor.WHITE, 1);
            enterDeclareBlockers();

            gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(defenderIndex(hipparion), attackerIndex(bears))));

            assertThat(hipparion.isBlocking()).isTrue();
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
        }

        @Test
        @DisplayName("CR 509.1e: the defender may tap lands for the cost while blockers are declared")
        void defenderMayFloatManaDuringTheDeclaration() {
            Permanent giant = attacking(player1, new HillGiant());
            Permanent hipparion = addCreatureReady(player2, new Hipparion());
            Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
            enterDeclareBlockers();

            // Nobody holds priority during the declaration; the mana window is open anyway.
            gs.tapPermanent(gd, player2, defenderIndex(forest));

            assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(defenderIndex(hipparion), attackerIndex(giant)))))
                    .doesNotThrowAnyException();
            assertThat(hipparion.isBlocking()).isTrue();
            assertThat(forest.isTapped()).isTrue();
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("CR 509.1e: the mana window belongs to the defender, not the attacking player")
        void attackingPlayerCannotFloatManaDuringTheDeclaration() {
            attacking(player1, new HillGiant());
            addCreatureReady(player2, new Hipparion());
            Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
            enterDeclareBlockers();

            assertThatThrownBy(() -> gs.tapPermanent(gd, player1, attackerIndex(forest)))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(forest.isTapped()).isFalse();
        }
    }

    @Test
    @DisplayName("An accepted declaration records the blocks, clears the prompt and logs once")
    void acceptedDeclarationRecordsTheBlocks() {
        Permanent attacker = attacking(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent bystander = addCreatureReady(player2, new ScatheZombies());
        enterDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(defenderIndex(blocker), attackerIndex(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(bystander.isBlocking()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class)).isNull();
        assertThat(gameLogContains("declares 1 blocker.")).isTrue();
    }

    @Test
    @DisplayName("An empty declaration is accepted and blocks nothing")
    void emptyDeclarationIsAccepted() {
        attacking(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        enterDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class)).isNull();
    }
}
