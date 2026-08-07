package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.AwesomePresence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GorillaBerserkers;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeatWave;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The additional cost of declaring a block (Awesome Presence, Hipparion, Heat Wave) is validated
 * by the engine against the whole declaration, so a block the defending player can't pay for costs
 * them every other block too. Every difficulty must therefore declare only blocks it can pay for,
 * floating the mana they need (CR 509.1e) and giving up the rest (CR 509.1c).
 */
@Tag("scryfall")
class AiBlockCostTest {

    private GameTestHarness harness;
    private GameData gd;
    private Player attackingPlayer;
    private Player aiPlayer;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        gd = harness.getGameData();
        attackingPlayer = harness.getPlayer1();
        aiPlayer = harness.getPlayer2();
    }

    @ParameterizedTest(name = "{0} AI")
    @ValueSource(strings = {"easy", "medium", "hard"})
    @DisplayName("Lands are tapped to pay a block cost instead of the block being given up")
    void tapsLandsToPayTheBlockCost(String difficulty) {
        harness.setLife(aiPlayer, 2);
        taxBlockersOf(attacking(new GrizzlyBears()));
        Permanent blocker = blocker(new HillGiant());
        List<Permanent> forests = List.of(land(), land(), land());
        enterDeclareBlockers();

        declareBlockers(difficulty);

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(forests).allMatch(Permanent::isTapped);
        assertThat(gd.playerManaPools.get(aiPlayer.getId()).getTotal()).isZero();
    }

    @ParameterizedTest(name = "{0} AI")
    @ValueSource(strings = {"easy", "medium", "hard"})
    @DisplayName("A block whose cost can't be paid is given up rather than declared and rejected")
    void declinesTheBlockItCannotPayFor(String difficulty) {
        harness.setLife(aiPlayer, 2);
        taxBlockersOf(attacking(new GrizzlyBears()));
        Permanent blocker = blocker(new HillGiant());
        enterDeclareBlockers();

        declareBlockers(difficulty);

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    /**
     * Hard is left out: its MCTS search declines to block this board at all, with or without a
     * block cost in play, so there would be no free block to keep. Its cost handling is covered by
     * the {@code prepareBlockersForTax} cases below, which every difficulty shares.
     */
    @ParameterizedTest(name = "{0} AI")
    @ValueSource(strings = {"easy", "medium"})
    @DisplayName("An unpayable block costs only itself — the free block against the other attacker stands")
    void keepsTheFreeBlockWhenAnotherIsUnaffordable(String difficulty) {
        // Four incoming damage against three life: both difficulties want to block here.
        harness.setLife(aiPlayer, 3);
        taxBlockersOf(attacking(new GrizzlyBears()));
        Permanent freeAttacker = attacking(new ScatheZombies());
        blocker(new HillGiant());
        blocker(new HillGiant());
        enterDeclareBlockers();

        declareBlockers(difficulty);

        assertThat(blocking()).isNotEmpty()
                .allMatch(blocker -> blocker.getBlockingTargetIds().equals(List.of(freeAttacker.getId())));
    }

    @ParameterizedTest(name = "{0} AI")
    @ValueSource(strings = {"easy", "medium", "hard"})
    @DisplayName("Only as many blockers as the mana covers are kept on one attacker")
    void keepsOnlyTheBlockersTheManaCovers(String difficulty) {
        harness.addMana(aiPlayer, ManaColor.GREEN, 3);
        Permanent attacker = attacking(new GrizzlyBears());
        taxBlockersOf(attacker);
        blocker(new HillGiant());
        blocker(new HillGiant());
        enterDeclareBlockers();

        List<BlockerAssignment> affordable = engineFor(difficulty)
                .prepareBlockersForTax(gd, blockAllOf(attacker));

        assertThat(affordable).hasSize(1);
    }

    @ParameterizedTest(name = "{0} AI")
    @ValueSource(strings = {"easy", "medium", "hard"})
    @DisplayName("A block needing more blockers than the mana covers is given up entirely")
    void dropsTheBlockWhenTooFewBlockersAreAffordable(String difficulty) {
        // Gorilla Berserkers can't be blocked by fewer than three creatures, and Awesome Presence
        // charges {3} for each of them — {9} in total, of which only {6} can be paid.
        harness.addMana(aiPlayer, ManaColor.GREEN, 6);
        Permanent attacker = attacking(new GorillaBerserkers());
        taxBlockersOf(attacker);
        blocker(new HillGiant());
        blocker(new HillGiant());
        blocker(new HillGiant());
        enterDeclareBlockers();

        List<BlockerAssignment> affordable = engineFor(difficulty)
                .prepareBlockersForTax(gd, blockAllOf(attacker));

        assertThat(affordable).isEmpty();
    }

    @ParameterizedTest(name = "{0} AI")
    @ValueSource(strings = {"easy", "medium", "hard"})
    @DisplayName("Blocks are given up once their life cost outgrows the life total")
    void dropsBlocksWhoseLifeCostExceedsTheLifeTotal(String difficulty) {
        // Heat Wave: nonblue creatures can't block the enchanting player's creatures unless their
        // controller pays 1 life for each blocking creature they control.
        harness.addToBattlefield(attackingPlayer, new HeatWave());
        harness.setLife(aiPlayer, 1);
        Permanent firstAttacker = attacking(new GrizzlyBears());
        Permanent secondAttacker = attacking(new ScatheZombies());
        blocker(new HillGiant());
        blocker(new HillGiant());
        enterDeclareBlockers();

        List<BlockerAssignment> affordable = engineFor(difficulty).prepareBlockersForTax(gd, List.of(
                assignment(0, firstAttacker), assignment(1, secondAttacker)));

        assertThat(affordable).hasSize(1);
    }

    @ParameterizedTest(name = "{0} AI")
    @ValueSource(strings = {"easy", "medium", "hard"})
    @DisplayName("A declaration that costs nothing is sent untouched")
    void leavesFreeBlocksAlone(String difficulty) {
        Permanent attacker = attacking(new GrizzlyBears());
        blocker(new HillGiant());
        blocker(new HillGiant());
        enterDeclareBlockers();
        List<BlockerAssignment> declared = blockAllOf(attacker);

        assertThat(engineFor(difficulty).prepareBlockersForTax(gd, declared))
                .isEqualTo(declared);
    }

    /**
     * Runs the AI's blocker declaration and fails if the engine refused it — a refusal is only
     * logged before the AI falls back to declaring no blockers at all, so the board alone can't
     * tell an intentionally declined block from a rejected one.
     */
    private void declareBlockers(String difficulty) {
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engineFor(difficulty).handleEvent(AiDecisionKind.BLOCKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }
    }

    private AiDecisionEngine engineFor(String difficulty) {
        return switch (difficulty) {
            case "easy" -> new EasyAiDecisionEngine(gd.id, aiPlayer, harness.getGameRegistry(),
                    harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(),
                    harness.getCombatAttackService(), harness.getGameActionAvailabilityService(),
                    harness.getCastingCostService(), harness.getCastingPermissionService(),
                    harness.getTargetValidationService(), harness.getTargetLegalityService());
            case "medium" -> new MediumAiDecisionEngine(gd.id, aiPlayer, harness.getGameRegistry(),
                    harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(),
                    harness.getCombatAttackService(), harness.getGameActionAvailabilityService(),
                    harness.getCastingCostService(), harness.getCastingPermissionService(),
                    harness.getTargetValidationService(), harness.getTargetLegalityService());
            default -> new HardAiDecisionEngine(gd.id, aiPlayer, harness.getGameRegistry(),
                    harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(),
                    harness.getCombatAttackService(), harness.getGameActionAvailabilityService(),
                    harness.getCastingCostService(), harness.getCastingPermissionService(),
                    harness.getTargetValidationService(), harness.getTargetLegalityService());
        };
    }

    private Permanent attacking(Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(attackingPlayer, card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent blocker(Card card) {
        Permanent blocker = harness.addToBattlefieldAndReturn(aiPlayer, card);
        blocker.setSummoningSick(false);
        return blocker;
    }

    private Permanent land() {
        Permanent forest = harness.addToBattlefieldAndReturn(aiPlayer, new Forest());
        forest.setSummoningSick(false);
        return forest;
    }

    /** Attaches an Awesome Presence: every creature blocking {@code attacker} costs {3}. */
    private void taxBlockersOf(Permanent attacker) {
        Permanent aura = harness.addToBattlefieldAndReturn(attackingPlayer, new AwesomePresence());
        aura.setAttachedTo(attacker.getId());
    }

    private void enterDeclareBlockers() {
        harness.forceActivePlayer(attackingPlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    /** Every creature the AI controls blocking {@code attacker}. */
    private List<BlockerAssignment> blockAllOf(Permanent attacker) {
        List<BlockerAssignment> assignments = new ArrayList<>();
        List<Permanent> battlefield = gd.playerBattlefields.get(aiPlayer.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getPower() != null) {
                assignments.add(assignment(i, attacker));
            }
        }
        return assignments;
    }

    private BlockerAssignment assignment(int blockerIndex, Permanent attacker) {
        return new BlockerAssignment(blockerIndex,
                gd.playerBattlefields.get(attackingPlayer.getId()).indexOf(attacker));
    }

    private List<Permanent> blocking() {
        return gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(Permanent::isBlocking)
                .toList();
    }
}
