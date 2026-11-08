#!/usr/bin/env python3
"""Step 15, run 17zb: the card tests of the emblems' enter abilities read as emblem `TriggeredAbility`s.

- Koth, Fire of Resistance and Nissa, Vital Force: an emblem isn't a permanent (CR 110.1), so Ancient Greenwarden's
  "a triggered ability of a permanent you control ... triggers an additional time" doesn't apply to the emblem's
  landfall ability (the rules fix: at HEAD the emblem collector counted the permanents' extra triggers, so the emblem
  triggered twice).
- Chandra, Spark Hunter: three Clues entering one after another in one resolution (Confirm Suspicions' "investigate
  three times") each trigger the emblem, and each of the three waiting target choices is asked (the fix: at HEAD the
  becomes-the-target check after the first answer began the second choice and the answer handler then began the
  third over it, so one ability was lost).
- Pyrogoyf: "that creature deals damage equal to its power" from a Lhurgoyf that left the battlefield before the
  ability resolves is dealt with its last-known power (the same handler fix; at HEAD it dealt none).
- Mila, Crafty Companion: Lukka's emblem's creature that left the battlefield before the ability resolves still deals
  the damage, its power as it last existed there (CR 608.2h, CR 113.7a).
- Mila, Crafty Companion: Lukka's emblem's target choice is the generic spell-target choice the emblem enters reader
  queues (`SpellTargetTriggerAnyTarget`), no longer the entering-permanent one the removed marker's branch queued.

Idempotent: a test already added is left alone. Optional repository-root argument (for the HEAD worktree).

Usage: step15-run17zb-tests.py [<repository root>]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = "magical-vibes-card-test/{0}/src/test/java/com/github/laxika/magicalvibes/cards/{0}/{1}.java"
PKG = "com.github.laxika.magicalvibes"

KOTH = '''    @Test
    @DisplayName("Ancient Greenwarden doesn't make the emblem trigger an additional time")
    void emblemTriggersOnceWithAncientGreenwarden() {
        addReadyKoth(player1, 7);
        harness.addToBattlefield(player1, new AncientGreenwarden());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Mountain()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
'''

NISSA = '''    @Test
    @DisplayName("Ancient Greenwarden doesn't make the emblem trigger an additional time")
    void emblemTriggersOnceWithAncientGreenwarden() {
        addReadyNissa(player1, 7);
        harness.addToBattlefield(player1, new AncientGreenwarden());
        harness.setLibrary(player1, List.of(new RiparianTiger(), new RiparianTiger()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
'''

CHANDRA = '''    @Test
    void emblemTriggersForEachOfThreeArtifactsEnteringInOneResolution() {
        Permanent chandra = addChandra();
        chandra.setCounterCount(CounterType.LOYALTY, 7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new ConfirmSuspicions()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, player2.getId());
        }
        assertThat(gd.stack).hasSize(3);
        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
        }

        harness.assertLife(player2, 11);
    }
'''

LUKKA_LEFT = '''    @Test
    void lukkaEmblemCreatureThatLeftStillDealsDamageEqualToItsLastKnownPower() {
        addReadyLukka(7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        int lifeBefore = gd.getLife(player2.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Grizzly Bears").getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }
'''

PYROGOYF = '''    @Test
    void entryDamageUsesLastKnownPowerWhenPyrogoyfLeavesFirst() {
        setThreeCardTypesInGraveyards();
        harness.castFromHand(player1, new Pyrogoyf(), "{3}{R}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Pyrogoyf").getId());
        harness.assertInGraveyard(player1, "Pyrogoyf");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
'''


def add_import(text, fqcn):
    line = f"import {fqcn};"
    if line in text:
        return text
    first = re.search(r"^import [^;]+;$", text, re.M)
    return text[:first.start()] + line + "\n" + text[first.start():]


def add_card_used(text, simple):
    match = re.search(r"@CardUsed\(\{([^}]*)\}\)", text)
    if re.search(r"\b" + simple + r"\.class\b", match.group(1)):
        return text
    return text[:match.end(1)] + ", " + simple + ".class" + text[match.end(1):]


def insert_before_last_brace(text, block):
    last = text.rstrip().rfind("}")
    return text[:last].rstrip("\n") + "\n\n" + block + "}\n"


def add_test(letter, name, method, block, imports, card_used):
    path = ROOT / TESTS.format(letter, name)
    text = path.read_text()
    if f"void {method}(" in text:
        return
    text = insert_before_last_brace(text, block)
    for fqcn in imports:
        text = add_import(text, fqcn)
    for simple in card_used:
        text = add_card_used(text, simple)
    path.write_text(text)
    print(f"added {name}#{method}")


def convert_mila():
    path = ROOT / TESTS.format("m", "MilaCraftyCompanionTest")
    text = path.read_text()
    old = "PermanentChoiceContext.EnteringPermanentAnyTargetTrigger.class"
    if old not in text:
        return
    path.write_text(text.replace(old, "PermanentChoiceContext.SpellTargetTriggerAnyTarget.class"))
    print("converted MilaCraftyCompanionTest")


def main():
    greenwarden = [f"{PKG}.cards.a.AncientGreenwarden"]
    add_test("k", "KothFireOfResistanceTest", "emblemTriggersOnceWithAncientGreenwarden", KOTH, greenwarden,
             ["AncientGreenwarden"])
    add_test("n", "NissaVitalForceTest", "emblemTriggersOnceWithAncientGreenwarden", NISSA, greenwarden,
             ["AncientGreenwarden"])
    add_test("c", "ChandraSparkHunterTest", "emblemTriggersForEachOfThreeArtifactsEnteringInOneResolution", CHANDRA,
             [f"{PKG}.cards.c.ConfirmSuspicions"], ["ConfirmSuspicions"])
    add_test("m", "MilaCraftyCompanionTest", "lukkaEmblemCreatureThatLeftStillDealsDamageEqualToItsLastKnownPower",
             LUKKA_LEFT, [], [])
    add_test("p", "PyrogoyfTest", "entryDamageUsesLastKnownPowerWhenPyrogoyfLeavesFirst", PYROGOYF,
             [f"{PKG}.cards.d.DoomBlade", f"{PKG}.model.ManaColor"], ["DoomBlade"])
    convert_mila()


if __name__ == "__main__":
    main()
