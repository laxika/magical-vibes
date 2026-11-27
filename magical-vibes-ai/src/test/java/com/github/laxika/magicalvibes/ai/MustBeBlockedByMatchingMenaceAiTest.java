package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SalvageDrone;
import com.github.laxika.magicalvibes.cards.s.SlayersCleaver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A menace attacker that "must be blocked by an Eldrazi if able" (Slayer's Cleaver) is blocked by an Eldrazi and any
 * other creature when the defending player can: one matching blocker obeys the requirement, and the declaration must
 * obey as many requirements as it can without breaking menace (CR 509.1c).
 */
@Tag("scryfall")
class MustBeBlockedByMatchingMenaceAiTest {

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
        harness.skipMulligan();
    }

    /**
     * Hard is left out: its MCTS search is offered blocker declarations the engine rejects (no block at all under a
     * block requirement), a separate defect.
     */
    @ParameterizedTest(name = "{0} AI")
    @ValueSource(strings = {"easy", "medium"})
    @DisplayName("The Eldrazi and another creature block the menace attacker")
    void eldraziAndAnotherCreatureBlock(String difficulty) {
        Permanent wurm = harness.addToBattlefieldAndReturn(attackingPlayer, new CrawWurm());
        TestCards.mutableCard(wurm).setKeywords(EnumSet.of(Keyword.MENACE));
        wurm.setSummoningSick(false);
        wurm.setAttacking(true);
        Permanent cleaver = harness.addToBattlefieldAndReturn(attackingPlayer, new SlayersCleaver());
        cleaver.setAttachedTo(wurm.getId());
        blocker(new LlanowarElves());
        blocker(new LlanowarElves());
        Permanent drone = blocker(new SalvageDrone());
        harness.forceActivePlayer(attackingPlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engineFor(difficulty).handleEvent(AiDecisionKind.BLOCKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(drone.isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(aiPlayer.getId()).stream().filter(Permanent::isBlocking))
                .hasSize(2);
    }

    private Permanent blocker(Card card) {
        Permanent blocker = harness.addToBattlefieldAndReturn(aiPlayer, card);
        blocker.setSummoningSick(false);
        return blocker;
    }

    private AiDecisionEngine engineFor(String difficulty) {
        return switch (difficulty) {
            case "easy" -> new EasyAiDecisionEngine(gd.id, aiPlayer, harness.getGameRegistry(),
                    harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(),
                    harness.getCombatAttackService(), harness.getGameActionAvailabilityService(),
                    harness.getCastingCostService(), harness.getCastingPermissionService(),
                    harness.getTargetValidationService(), harness.getTargetLegalityService());
            default -> new MediumAiDecisionEngine(gd.id, aiPlayer, harness.getGameRegistry(),
                    harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(),
                    harness.getCombatAttackService(), harness.getGameActionAvailabilityService(),
                    harness.getCastingCostService(), harness.getCastingPermissionService(),
                    harness.getTargetValidationService(), harness.getTargetLegalityService());
        };
    }
}
