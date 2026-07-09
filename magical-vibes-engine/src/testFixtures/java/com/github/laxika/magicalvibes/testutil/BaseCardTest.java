package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

@Tag("scryfall")
public abstract class BaseCardTest {

    protected GameTestHarness harness;
    protected Player player1;
    protected Player player2;
    protected GameService gs;
    protected GameQueryService gqs;
    protected GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();

        // Auto-pass halts for a merely-playable card only when the priority holder is AI-controlled
        // or the game is a headless simulation; a human otherwise stops solely at configured
        // auto-stop steps (see AutoPassService#shouldStopForPlayableCards). Card tests drive
        // priority deterministically and rely on the older "stop whenever you can act" behavior:
        // passPriority(activePlayer) must leave the opponent holding priority so they can respond
        // at instant speed (combat tricks, counterspells), while combat with no available response
        // must still cascade to the damage step. Marking both players AI-controlled flips
        // shouldStopForPlayableCards to true, reproducing exactly that behavior. Within the engine,
        // aiPlayerIds is read only by auto-pass — no decision is auto-made from it — so this is a
        // pure priority-window toggle for tests.
        gd.aiPlayerIds.add(player1.getId());
        gd.aiPlayerIds.add(player2.getId());
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
}
