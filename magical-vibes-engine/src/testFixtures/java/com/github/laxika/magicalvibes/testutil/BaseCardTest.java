package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.combat.attack.AttackLegalityService;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
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

    protected Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    protected Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
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
        harness.forceActivePlayer(player1);
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
