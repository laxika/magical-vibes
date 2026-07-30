package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.combat.attack.AttackLegalityService;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

import java.util.List;

@Tag("scryfall")
public abstract class BaseCardTest {

    protected GameTestHarness harness;
    protected Player player1;
    protected Player player2;
    protected GameService gs;
    protected GameQueryService gqs;
    protected AttackLegalityService als;
    protected BlockLegalityService bls;
    protected GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        als = harness.getAttackLegalityService();
        bls = harness.getBlockLegalityService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();

        // Auto-pass halts for a merely-playable card only when the priority holder's priority is
        // policy-driven or the game is a headless simulation; a human otherwise stops solely at
        // configured auto-stop steps (see AutoPassService#shouldStopForPlayableCards). Card tests
        // drive priority deterministically and rely on the "stop whenever you can act" behavior:
        // passPriority(activePlayer) must leave the opponent holding priority so they can respond
        // at instant speed (combat tricks, counterspells), while combat with no available response
        // must still cascade to the damage step.
        //
        // This must NOT be done by adding both seats to aiPlayerIds: an AI seat is by definition
        // not a transport consumer, so GameEventProjectionSubscriber drops every message addressed
        // to one. Marking both players AI silently left conn1/conn2 empty, breaking every test that
        // asserts on sent messages. alwaysOfferPriorityWindows buys the priority behavior alone.
        gd.alwaysOfferPriorityWindows = true;
    }

    /**
     * No test may end with a stack entry parked in {@code GameData.pendingEffectResolutionEntry}
     * while nothing is left to resume it. Effect resolution parks the entry when a handler begins
     * an interaction mid-resolution; the completing handler must resume it via
     * {@code InputCompletionService}. A dangling park silently drops the spell's remaining effects
     * and — because {@code deferPlayerLossCheck} only clears when a resolution unwinds unparked —
     * permanently suppresses the 0-life state-based action, so no player can ever lose again.
     *
     * <p>Only the AI fuzzer caught this class of bug before, and only by chance: it took a game
     * where every spell cast from Improvisation Capstone happened to need no target choice. This
     * runs the same invariant deterministically on every card test. A test that legitimately ends
     * at a prompt is skipped — the parked entry is exactly what answering it will resume.
     */
    @AfterEach
    void assertNoDanglingEffectResolutionPark() {
        if (gd == null || gd.status == GameStatus.FINISHED) {
            return;
        }
        if (gd.interaction.isAwaitingInput() || !gd.pendingInteractions.isEmpty()) {
            return;
        }
        StackEntry parked = gd.pendingEffectResolutionEntry;
        if (parked != null) {
            Card card = parked.getCard();
            throw new AssertionError("Test ended with '" + (card != null ? card.getName() : "<no card>")
                    + "' parked mid-resolution but no interaction is active or queued to resume it."
                    + " End the completing handler through InputCompletionService"
                    + " (processMayAbilitiesThenAutoPass / sbaProcessMayAbilitiesThenAutoPass).");
        }
    }

    protected Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    /**
     * The first permanent {@code player} controls with this name.
     *
     * @throws java.util.NoSuchElementException if they control no such permanent
     */
    protected Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    /** Every permanent {@code player} controls with this name, in battlefield order. */
    protected List<Permanent> findPermanents(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .toList();
    }

    /** How many permanents named {@code name} {@code player} controls. */
    protected long countPermanents(Player player, String name) {
        return findPermanents(player, name).size();
    }

    /**
     * Forces {@code activePlayer} to be the active player and advances to their upkeep, resolving
     * anything that triggers on the way. Starts from {@link TurnStep#UNTAP} because untap and upkeep
     * are reached by a single priority round-trip.
     */
    protected void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    /**
     * Declares the battlefield permanents at {@code attackerIndices} as attackers for {@code player},
     * forcing the active player and combat step first so the declaration is legal.
     */
    protected void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }

    /** Declares attackers for {@link #player1}, the usual attacker in card tests. */
    protected void declareAttackers(List<Integer> attackerIndices) {
        declareAttackers(player1, attackerIndices);
    }

    /**
     * Advances from declare-blockers through combat damage with neither player responding. Note that
     * this resolves the damage itself but not any ability it triggers — pass priority again for that.
     */
    protected void resolveCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    /** Resolves combat with {@link #player1} attacking. */
    protected void resolveCombat() {
        resolveCombat(player1);
    }

    /**
     * Puts the game in the declare-blockers step with blocker input open, so a test can call
     * {@code gs.declareBlockers(...)} directly.
     */
    protected void prepareDeclareBlockers() {
        prepareDeclareBlockers(player1);
    }

    /** As {@link #prepareDeclareBlockers()}, for combats where {@code activePlayer} is the attacker. */
    protected void prepareDeclareBlockers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    /** Passes priority until the stack is empty, resolving every waiting trigger. */
    protected void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    protected boolean gameLogContains(String substring) {
        return gd.gameLog.stream().anyMatch(entry -> entry.plainText().contains(substring));
    }

    protected boolean gameLogContains(GameData data, String substring) {
        return data.gameLog.stream().anyMatch(entry -> entry.plainText().contains(substring));
    }
}
