#!/usr/bin/env python3
"""Step 15, run 17zb2: card tests for the cast abilities that now read the stack entry's triggering spell.

- `OrvarTheAllFormTest#copiesTheOtherTargetWhenTheSpellAlsoTargetsOrvar` (a rules fix: "one of those permanents" is
  one of the other permanents you control, so Orvar itself isn't offered), and the countered-spell guard
  `#copiesTheTargetOfASpellCounteredBeforeTheTriggerResolves` (Orvar's ruling: a spell that left the stack is judged
  by the targets it had then);
- `VesuvanDuplimancyTest#copiesTheOriginalTargetAfterTheSpellIsRedirected` (a rules fix: Vesuvan Duplimancy's ruling
  "the token that is created is a copy of the spell's original target, not the new target"), and the guard
  `#copiesEvenWhenTheSpellIsCounteredBeforeTheTriggerResolves`;
- `LokiGodOfLiesTest#gainsControlOfTheOriginalTargetAfterTheSpellIsRedirected` (a guard: "that creature" is the
  creature the spell targeted as the ability triggered);
- `ShikoAndNarsetUnifiedTest#copiesASpellTargetingAPermanentAfterItIsCountered` (a guard: "if it targets a permanent"
  is read from the spell as it last existed on the stack).

Idempotent: a test already present is left alone. The optional argument is the repository root (default: this
script's), so the cases can be added to a HEAD worktree.

Usage: step15-run17zb2-tests.py [<repository root>]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = "magical-vibes-card-test/{letter}/src/test/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"
CARDS = "com.github.laxika.magicalvibes.cards."

CASES = {
    ("o", "OrvarTheAllFormTest"): (
        [CARDS + "c.Counterspell"],
        ["Counterspell"],
        [("copiesTheOtherTargetWhenTheSpellAlsoTargetsOrvar", """
    @Test
    @DisplayName("Copies the other permanent you control when the spell also targets Orvar")
    void copiesTheOtherTargetWhenTheSpellAlsoTargetsOrvar() {
        Permanent orvar = harness.addToBattlefieldAndReturn(player1, new OrvarTheAllForm());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheVoid()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, List.of(orvar.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears"));
    }
"""),
         ("copiesTheTargetOfASpellCounteredBeforeTheTriggerResolves", """
    @Test
    @DisplayName("Copies the target of a spell countered before the ability resolves")
    void copiesTheTargetOfASpellCounteredBeforeTheTriggerResolves() {
        harness.addToBattlefield(player1, new OrvarTheAllForm());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        DepartTheRealm spell = new DepartTheRealm();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());
        harness.assertInGraveyard(player1, "Depart the Realm");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }
""")]),
    ("v", "VesuvanDuplimancyTest"): (
        [CARDS + "c.Counterspell", CARDS + "d.Deflection"],
        ["Counterspell", "Deflection"],
        [("copiesTheOriginalTargetAfterTheSpellIsRedirected", """
    @Test
    @DisplayName("Copies the spell's original target after the spell is redirected")
    void copiesTheOriginalTargetAfterTheSpellIsRedirected() {
        harness.addToBattlefield(player1, new VesuvanDuplimancy());
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent replacement = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        GiantGrowth spell = new GiantGrowth();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Deflection()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, original.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());
        harness.handlePermanentChosen(player2, replacement.getId());
        harness.passBothPriorities();

        assertThat(tokenCopies(player1))
                .singleElement()
                .satisfies(token -> assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears"));
    }
"""),
         ("copiesEvenWhenTheSpellIsCounteredBeforeTheTriggerResolves", """
    @Test
    @DisplayName("Creates the token even when the spell is countered before the ability resolves")
    void copiesEvenWhenTheSpellIsCounteredBeforeTheTriggerResolves() {
        harness.addToBattlefield(player1, new VesuvanDuplimancy());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GiantGrowth spell = new GiantGrowth();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());
        harness.assertInGraveyard(player1, "Giant Growth");
        harness.passBothPriorities();

        assertThat(tokenCopies(player1))
                .singleElement()
                .satisfies(token -> assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears"));
    }
""")]),
    ("l", "LokiGodOfLiesTest"): (
        [CARDS + "d.Deflection"],
        ["Deflection"],
        [("gainsControlOfTheOriginalTargetAfterTheSpellIsRedirected", """
    @Test
    void gainsControlOfTheOriginalTargetAfterTheSpellIsRedirected() {
        harness.addToBattlefield(player1, new LokiGodOfLies());
        Permanent original = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent replacement = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareMainPhase(player1);
        GiantGrowth spell = new GiantGrowth();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Deflection()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, original.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());
        harness.handlePermanentChosen(player2, replacement.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(original).doesNotContain(replacement);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(replacement);
    }
""")]),
    ("s", "ShikoAndNarsetUnifiedTest"): (
        [CARDS + "g.GrizzlyBears", "com.github.laxika.magicalvibes.model.Permanent"],
        ["GrizzlyBears"],
        [("copiesASpellTargetingAPermanentAfterItIsCountered", """
    @Test
    void copiesASpellTargetingAPermanentAfterItIsCountered() {
        harness.addToBattlefield(player1, new ShikoAndNarsetUnified());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new DarkRitual(), new DarkRitual()));
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(new DarkRitual(), bolt));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.castInstant(player1, 0, bears.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bolt.getId());
        harness.assertInGraveyard(player1, "Lightning Bolt");

        harness.passBothPriorities();
        assertThat(gd.stagedSpellCopies).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).anyMatch(StackEntry::isCopy);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
""")]),
}


def add_import(text, fqcn):
    line = f"import {fqcn};"
    if line in text:
        return text
    imports = list(re.finditer(r"^import (?!static)[^;]+;$", text, re.M))
    ours = [m for m in imports if not m.group(0).startswith("import java")]
    after = next((m for m in ours if m.group(0) > line), None)
    if after is not None:
        return text[:after.start()] + line + "\n" + text[after.start():]
    last = ours[-1]
    return text[:last.end()] + "\n" + line + text[last.end():]


def add_card_used(text, simple):
    match = re.search(r"@CardUsed\(\{(.*?)\}\)", text, re.S)
    if re.search(r"\b" + simple + r"\.class\b", match.group(1)):
        return text
    body = match.group(1).rstrip()
    return text[:match.start(1)] + body + ", " + simple + ".class" + text[match.end(1):]


def main():
    for (letter, name), (imports, card_used, methods) in CASES.items():
        path = ROOT / TESTS.format(letter=letter, name=name)
        text = path.read_text()
        original = text
        for method, source in methods:
            if re.search(r"\bvoid " + method + r"\(", text):
                continue
            end = text.rstrip().rfind("}")
            text = text[:end].rstrip() + "\n" + source + "}\n"
        if text == original:
            continue
        for fqcn in imports:
            text = add_import(text, fqcn)
        for simple in card_used:
            text = add_card_used(text, simple)
        path.write_text(text)
        print(f"updated {name}")


if __name__ == "__main__":
    main()
