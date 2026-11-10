#!/usr/bin/env python3
"""Step 15, run 17z13e4: the card cases of the creature-death defaults.

- `RoamingThroneTest.doublesAllyCreatureDeathTrigger` (the rules fix): Roaming Throne makes a chosen-type creature's
  "whenever this creature or another creature you control dies" (Zulaport Cutthroat) trigger an additional time. The
  "a creature you control dies" collector put its ability on the stack outside the copies Roaming Throne adds (only the
  creature-death doublers counted, and only for its untargeted effects).

- `ReveillarkTest`: its leave ability now waits for its targets in the self-trigger target queue, as every targeted
  "when this leaves the battlefield" ability does, instead of its handler beginning the graveyard choice while the
  ability was collected; the test removes Reveillark through the removal service directly, outside an action that
  processes the queue, so it processes the queue after the removal (as the Zagoth Mamba, Vadrok and Three Tree Scribe
  tests do).

Run per letter module: step15-run17z13e4-tests.py [LETTER...] (r). Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "com/github/laxika/magicalvibes/cards"

ROAMING_THRONE_CASE = '''
    @Test
    @DisplayName("Roaming Throne doubles a chosen-type creature's \\"whenever another creature you control dies\\"")
    void doublesAllyCreatureDeathTrigger() {
        addThrone(CardSubtype.HUMAN);
        harness.addToBattlefieldAndReturn(player1, new ZulaportCutthroat());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).filteredOn(entry -> entry.getCard().getName().equals("Zulaport Cutthroat")).hasSize(2);
    }
}
'''

EDITS = {
    "r/RoamingThroneTest.java": {
        "marker": "void doublesAllyCreatureDeathTrigger()",
        "append": ROAMING_THRONE_CASE,
        "imports": ["com.github.laxika.magicalvibes.cards.g.GrizzlyBears",
                    "com.github.laxika.magicalvibes.cards.z.ZulaportCutthroat"],
        "card_used": ("FugitiveWizard.class})", "FugitiveWizard.class, GrizzlyBears.class, ZulaportCutthroat.class})"),
    },
}


def add_import(text, name):
    line = f"import {name};\n"
    if line in text:
        return text
    imports = [l for l in text.splitlines(keepends=True) if l.startswith("import com.github")]
    ordered = sorted(imports + [line])
    index = ordered.index(line)
    if index + 1 < len(ordered):
        return text.replace(ordered[index + 1], line + ordered[index + 1], 1)
    return text.replace(ordered[index - 1], ordered[index - 1] + line, 1)


REVEILLARK_REPLACEMENTS = [(
    "() -> harness.getPermanentRemovalService().removePermanentTo{zone}(gd, reveillark));",
    "() -> {{\n                    harness.getPermanentRemovalService().removePermanentTo{zone}(gd, reveillark);\n"
    "                    harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd);\n"
    "                }});",
)]


def convert_reveillark():
    path = TESTS / "r/src/test/java" / PKG / "r/ReveillarkTest.java"
    text = path.read_text()
    before = text
    for old, new in REVEILLARK_REPLACEMENTS:
        for zone in ("Graveyard", "Exile"):
            text = text.replace(old.format(zone=zone), new.format(zone=zone))
    path.write_text(text)
    print(f"r/ReveillarkTest.java: {'changed' if text != before else 'unchanged'}")


def main():
    letters = set(sys.argv[1:]) or {key.split("/")[0] for key in EDITS}
    for key, edit in EDITS.items():
        letter, name = key.split("/")
        if letter not in letters:
            continue
        path = TESTS / letter / "src/test/java" / PKG / letter / name
        text = path.read_text()
        if edit["marker"] not in text:
            text = text.rstrip()
            assert text.endswith("}"), key
            text = text[:-1].rstrip() + "\n" + edit["append"]
            old, new = edit["card_used"]
            assert old in text, key
            text = text.replace(old, new)
            for imp in edit["imports"]:
                text = add_import(text, imp)
            path.write_text(text)
            print(f"{key}: case added")
        else:
            print(f"{key}: unchanged")
    if "r" in letters:
        convert_reveillark()


if __name__ == "__main__":
    main()
