#!/usr/bin/env python3
"""Step 15, follow-up after run 17y5: the card test of Sower of Discord's per-event trigger (a rules fix).

"Whenever damage is dealt to one of the chosen players, the other chosen player also loses that much life" triggers
once each time a chosen player is dealt damage, however many sources deal it at once (CR 603.2c; Boros Reckoner's
ruling: "If Boros Reckoner is dealt damage by multiple sources at once, such as by two creatures blocking it, its
ability triggers once"), "that much" the total. Run 17y4's per-source-and-recipient trigger made one trigger for each
source.

- `SowerOfDiscordTest#combatDamageFromTwoCreaturesTriggersOnceForTheTotal`: two Grizzly Bears deal combat damage to a
  chosen player at once: Sower's ability triggers exactly once (one "triggers" log line), and the other chosen player
  loses 4.
- `SowerOfDiscordTest#noncombatDamageFromTwoSourcesAtOnceTriggersOncePerPlayer`: Rakdos Charm's "each creature deals 1
  damage to its controller" with Sower and Grizzly Bears under the first chosen player and Grizzly Bears under the
  second: one trigger for each player (two "triggers" log lines, three at HEAD), both end at 17.

Idempotent: a test already holding its case is left alone. Optional repository-root argument (for the HEAD worktree).

Usage: step15-sower-per-event-tests.py [<repository root>]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
PATH = ROOT / "magical-vibes-card-test/s/src/test/java/com/github/laxika/magicalvibes/cards/s/SowerOfDiscordTest.java"
METHODS = ["combatDamageFromTwoCreaturesTriggersOnceForTheTotal",
           "noncombatDamageFromTwoSourcesAtOnceTriggersOncePerPlayer"]
ANCHOR = "    @Test\n    void lethalCombatDamageToSowerDoesNotSuppressItsSimultaneousPlayerDamageTrigger() {"
CASE = """    @Test
    void combatDamageFromTwoCreaturesTriggersOnceForTheTotal() {
        castSowerOfDiscord();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        Permanent firstBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player2, new GrizzlyBears());
        firstBear.setAttacking(true);
        secondBear.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().equals("Sower of Discord's ability triggers."))
                .hasSize(1);
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    void noncombatDamageFromTwoSourcesAtOnceTriggersOncePerPlayer() {
        castSowerOfDiscord();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RakdosCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 2, null);
        resolveAllTriggers();

        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().equals("Sower of Discord's ability triggers."))
                .hasSize(2);
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

"""
IMPORTS = [("import com.github.laxika.magicalvibes.cards.e.Exsanguinate;\n",
            "import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;\n"),
           ("import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;\n",
            "import com.github.laxika.magicalvibes.cards.r.RakdosCharm;\n")]
USED_OLD = "@CardUsed({SowerOfDiscord.class, Shock.class, ArchfiendOfDespair.class, Exsanguinate.class})"
USED_NEW = ("@CardUsed({SowerOfDiscord.class, Shock.class, ArchfiendOfDespair.class, Exsanguinate.class,\n"
            "        GrizzlyBears.class, RakdosCharm.class})")


def replace_once(text, old, new, what):
    if text.count(old) != 1:
        raise SystemExit(f"SowerOfDiscordTest: {what} not found")
    return text.replace(old, new, 1)


def main():
    text = PATH.read_text()
    if CASE in text:
        return
    for method in METHODS:
        if f"void {method}()" in text:
            raise SystemExit(f"SowerOfDiscordTest#{method} differs from this script's cases")
    text = replace_once(text, ANCHOR, CASE + ANCHOR, "the lethal-damage case anchor")
    for anchor, line in IMPORTS:
        if line not in text:
            text = replace_once(text, anchor, anchor + line, "the import anchor " + anchor.strip())
    if USED_NEW not in text:
        text = replace_once(text, USED_OLD, USED_NEW, "the @CardUsed list")
    PATH.write_text(text)
    for method in METHODS:
        print(f"added SowerOfDiscordTest#{method}")


if __name__ == "__main__":
    main()
