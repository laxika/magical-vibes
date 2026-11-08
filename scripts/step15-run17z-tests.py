#!/usr/bin/env python3
"""Step 15, run 17z: the card tests of the cast family's records turned into cast-trigger filters and existing effects.

- Prism Ring and Diamond Mare: "whenever you cast a spell of the chosen color, you gain 1 life" is a trigger condition
  (CR 603.2), not an intervening "if": a spell made colorless in response still gains the life (the rules fix: at
  HEAD the shared record's count of the spell's chosen colors was read as the ability resolved, so it gained none).
- Don't Worry About It: casting a card the Aura doesn't enchant copies nothing (a guard of the new stack-entry filter).
- Jace, Unraveler of Secrets: the emblem's tests build it with the −8, not by hand from the removed marker.

Idempotent: a test already added or converted is left alone. Optional repository-root argument (for the HEAD
worktree).

Usage: step15-run17z-tests.py [<repository root>]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = "magical-vibes-card-test/{0}/src/test/java/com/github/laxika/magicalvibes/cards/{0}/{1}.java"
PKG = "com.github.laxika.magicalvibes"


def colorless_in_response(chosen_setup):
    return '''    @Test
    @DisplayName("A spell of the chosen color made colorless in response still gains the life")
    void spellMadeColorlessInResponseStillGainsLife() {
''' + chosen_setup + '''        LeafGilder gilder = new LeafGilder();
        harness.setHand(player1, List.of(gilder, new Moonlace()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, gilder.getId());
        assertThat(gqs.getEffectiveCardColors(gd, gilder)).isEmpty();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }
'''


PRISM_RING = colorless_in_response(
    '''        Permanent ring = harness.addToBattlefieldAndReturn(player1, new PrismRing());
        ring.setChosenColor(CardColor.GREEN);
''')
DIAMOND_MARE = colorless_in_response(
    '''        Permanent mare = harness.addToBattlefieldAndReturn(player1, new DiamondMare());
        mare.setChosenColor(CardColor.GREEN);
''')

DONT_WORRY = '''    @Test
    @DisplayName("Casting a card the Aura doesn't enchant copies nothing")
    void castingAnotherCardCopiesNothing() {
        Card enchantedCard = new Divination();
        Card otherCard = new GrizzlyBears();
        DontWorryAboutIt auraCard = new DontWorryAboutIt();
        harness.setHand(player1, List.of(auraCard, enchantedCard, otherCard));
        addAuraMana();
        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
    }
'''

JACE_HAND_BUILT = re.compile(
    r"        gd\.emblems\.add\(new Emblem\(player1\.getId\(\), List\.of\(\n"
    r"                new CounterOpponentFirstSpellEachTurnEffect\.Marker\(\)\n"
    r"        \), new JaceUnravelerOfSecrets\(\)\)\);\n")
JACE_HELPER = '''
    /** Player 1 gets the emblem by Jace's −8, resolved. */
    private void getEmblem() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 8);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
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


def convert_jace():
    path = ROOT / TESTS.format("j", "JaceUnravelerOfSecretsTest")
    text = path.read_text()
    if "CounterOpponentFirstSpellEachTurnEffect" not in text:
        return
    text, count = JACE_HAND_BUILT.subn("        getEmblem();\n", text)
    if count != 4:
        raise SystemExit(f"JaceUnravelerOfSecretsTest: expected 4 hand-built emblems, found {count}")
    text = text.replace(f"import {PKG}.model.effect.CounterOpponentFirstSpellEachTurnEffect;\n", "")
    if not re.search(r"\bEmblem\b", text.replace(f"import {PKG}.model.Emblem;\n", "")):
        text = text.replace(f"import {PKG}.model.Emblem;\n", "")
    text = insert_before_last_brace(text, JACE_HELPER.lstrip("\n"))
    if "CounterOpponentFirstSpellEachTurnEffect" in text:
        raise SystemExit("JaceUnravelerOfSecretsTest still names the removed marker")
    path.write_text(text)
    print("converted JaceUnravelerOfSecretsTest")


def main():
    colorless_imports = [f"{PKG}.cards.l.LeafGilder", f"{PKG}.cards.m.Moonlace", f"{PKG}.model.ManaColor",
                         f"{PKG}.model.Permanent", f"{PKG}.model.CardColor", "java.util.List"]
    add_test("p", "PrismRingTest", "spellMadeColorlessInResponseStillGainsLife", PRISM_RING, colorless_imports,
             ["LeafGilder", "Moonlace"])
    add_test("d", "DiamondMareTest", "spellMadeColorlessInResponseStillGainsLife", DIAMOND_MARE, colorless_imports,
             ["LeafGilder", "Moonlace"])
    add_test("d", "DontWorryAboutItTest", "castingAnotherCardCopiesNothing", DONT_WORRY, [], [])
    convert_jace()


if __name__ == "__main__":
    main()
