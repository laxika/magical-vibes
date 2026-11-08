#!/usr/bin/env python3
"""Step 15, run 17x5: the card tests of the run.

- FiresongAndSunspeakerTest: a new case, the rules fix: "deals 3 damage to target creature or player" offers
  creatures and players, never a planeswalker or a battle.
- SkyclaveApparitionTest: "Removing the Apparition in response" passes priority twice: the leaves-the-battlefield
  ability triggers with no exiled card and goes on the stack above the enters ability (no intervening "if"; the
  collector no longer declines it), resolves first and creates no token, then the enters ability exiles the target.

Each edit is (old, new): the old text is replaced; a file that already has the new text is left alone. A new test is
added before the class's closing brace, with its imports. Idempotent. Per letter module.

Usage: step15-run17x5-tests.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

FIRESONG_TEST = '''
    @Test
    @DisplayName("Target creature or player: a planeswalker or a battle is not a legal target")
    void lifeGainTriggerOffersOnlyCreaturesAndPlayers() {
        harness.addToBattlefield(player1, new FiresongAndSunspeaker());
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        var battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player1.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        harness.setHand(player1, List.of(new WhitesunsPassage()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(jace, battle);
        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(bears.getId(), player1.getId(), player2.getId());
        assertThat(choice.validIds()).doesNotContain(jace.getId(), battle.getId());
    }
'''

TESTS = {
    ("s", "SkyclaveApparitionTest"): {
        "edits": [
            ("""        destroyApparition(harness.getPermanentId(player1, "Skyclave Apparition"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skyclave Apparition");""",
             """        destroyApparition(harness.getPermanentId(player1, "Skyclave Apparition"));
        // The leaves ability goes on the stack above the enters ability and finds no exiled card.
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Illusion"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skyclave Apparition");"""),
        ],
    },
    ("f", "FiresongAndSunspeakerTest"): {
        "edits": [
            ("@CardUsed({FiresongAndSunspeaker.class, Shock.class, GrizzlyBears.class, WhitesunsPassage.class,\n"
             "        SoothingBalm.class, DeepFreeze.class, ColdWaterSnapper.class})",
             "@CardUsed({FiresongAndSunspeaker.class, Shock.class, GrizzlyBears.class, WhitesunsPassage.class,\n"
             "        SoothingBalm.class, DeepFreeze.class, ColdWaterSnapper.class, JaceBeleren.class,\n"
             "        InvasionOfZendikar.class})"),
        ],
        "append": [("lifeGainTriggerOffersOnlyCreaturesAndPlayers", FIRESONG_TEST)],
        "imports": ["cards.j.JaceBeleren", "cards.i.InvasionOfZendikar", "model.CounterType"],
    },
}


def add_import(text, fqcn):
    line = f"import {PKG}.{fqcn};"
    if line in text:
        return text
    imports = list(re.finditer(r"^import [^;]+;$", text, re.M))
    pos = imports[0].start()
    return text[:pos] + line + "\n" + text[pos:]


def main():
    modules = sorted(set("abcdefghijklmnopqrstuvwxyz")) if sys.argv[1:] == ["all"] else sys.argv[1:]
    if not modules:
        raise SystemExit(__doc__)
    for (module, name), spec in TESTS.items():
        if module not in modules:
            continue
        path = (ROOT / f"magical-vibes-card-test/{module}/src/test/java/com/github/laxika/magicalvibes/cards"
                / module / f"{name}.java")
        text = path.read_text()
        original = text
        for old, new in spec.get("edits", []):
            if old in text:
                text = text.replace(old, new)
            elif new not in text:
                raise SystemExit(f"{name}: expected text not found:\n{old}")
        for marker, body in spec.get("append", []):
            if f"void {marker}(" in text:
                start = text.rfind("\n    @Test", 0, text.index(f"void {marker}("))
                end = text.index("\n    }\n", text.index(f"void {marker}(")) + len("\n    }\n")
                # An earlier version of the test is replaced by the current one.
                text = text[:start + 1] + body.lstrip("\n") + text[end:]
                continue
            end = text.rstrip().rfind("}")
            text = text[:end].rstrip() + "\n" + body + "}\n"
        for fqcn in spec.get("imports", []):
            text = add_import(text, fqcn)
        if text != original:
            path.write_text(text)
            print(f"converted {name}")


if __name__ == "__main__":
    main()
