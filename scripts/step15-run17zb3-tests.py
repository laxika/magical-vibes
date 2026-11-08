#!/usr/bin/env python3
"""Step 15, run 17zb3: card tests for Harness the Storm, Toofer, Keeper of the Full Grip, Spellweaver Helix and Bucket
List, whose abilities now read the stack entry's triggering spell.

- `TooferKeeperOfTheFullGripTest#triggersForACopyOfASpellYouControl` and `#triggersWhenItArrivesAfterTheSpellWasCast`
  (rules fixes: "whenever a spell you control causes you to gain card advantage" is an event of the spell's
  resolution, so a copy, which isn't cast, counts, and Toofer need only be on the battlefield as the spell resolves);
- `SpellweaverHelixTest#doesNothingIfTheMatchingImprintedCardLeavesExileFirst` (a rules fix: "if it has the same name
  as one of the cards exiled with this artifact" is an intervening "if", checked again as the ability resolves,
  CR 603.4);
- `BucketListTest#doesNotTriggerForASpellOfNoShowingType` (a rules fix: "a spell of a type showing on Bucket List" is
  the trigger condition), and the guard `#drawsEvenIfBucketListLeavesBeforeItsAbilityResolves` (the source as it was
  when the ability triggered);
- `HarnessTheStormTest#castsTheTargetAfterTheHandSpellIsCountered` (a guard: its ruling, "if the spell you cast from
  your hand is countered in response ..., you can still cast the target card"; the name is read from the spell as it
  last existed on the stack).

Idempotent: a test already present is left alone. The optional argument is the repository root (default: this
script's), so the cases can be added to a HEAD worktree.

Usage: step15-run17zb3-tests.py [<repository root>]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = "magical-vibes-card-test/{letter}/src/test/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"
CARDS = "com.github.laxika.magicalvibes.cards."

CASES = {
    ("t", "TooferKeeperOfTheFullGripTest"): (
        [CARDS + "t.Twincast"],
        ["Twincast"],
        [("triggersForACopyOfASpellYouControl", """
    @Test
    @DisplayName("A copy of a spell you control that gives you card advantage triggers it, though it wasn't cast")
    void triggersForACopyOfASpellYouControl() {
        addCreatureReady(player1, new TooferKeeperOfTheFullGrip());
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination, new Twincast()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0);
        harness.castInstant(player1, 0, divination.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }
"""),
         ("triggersWhenItArrivesAfterTheSpellWasCast", """
    @Test
    @DisplayName("Triggers when it is on the battlefield as the spell resolves, though it wasn't when it was cast")
    void triggersWhenItArrivesAfterTheSpellWasCast() {
        harness.castFromHand(player1, new Divination(), "{2}{U}");
        addCreatureReady(player1, new TooferKeeperOfTheFullGrip());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }
""")]),
    ("s", "SpellweaverHelixTest"): (
        [],
        [],
        [("doesNothingIfTheMatchingImprintedCardLeavesExileFirst", """
    @Test
    @DisplayName("Does nothing if the exiled card with the cast card's name leaves exile before it resolves")
    void doesNothingIfTheMatchingImprintedCardLeavesExileFirst() {
        Permanent helix = harness.addToBattlefieldAndReturn(player1, new SpellweaverHelix());
        Fabricate matching = new Fabricate();
        SylvanScrying other = new SylvanScrying();
        gd.addToExile(player1.getId(), matching, helix.getId());
        gd.addToExile(player1.getId(), other, helix.getId());
        harness.castFromHand(player1, new Fabricate(), "{2}{U}");
        gd.removeFromExile(matching.getId());
        gd.playerHands.get(player1.getId()).add(matching);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getCard().getName()).isEqualTo("Fabricate"));
    }
""")]),
    ("b", "BucketListTest"): (
        [CARDS + "n.Naturalize"],
        ["Naturalize"],
        [("doesNotTriggerForASpellOfNoShowingType", """
    @Test
    void doesNotTriggerForASpellOfNoShowingType() {
        harness.addToBattlefield(player1, new BucketList());
        castAndResolve(new DarksteelRelic(), "{0}");

        DarksteelRelic secondArtifact = new DarksteelRelic();
        harness.castFromHand(player1, secondArtifact, "{0}");

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getCard().getId()).isEqualTo(secondArtifact.getId()));
    }
"""),
         ("drawsEvenIfBucketListLeavesBeforeItsAbilityResolves", """
    @Test
    void drawsEvenIfBucketListLeavesBeforeItsAbilityResolves() {
        Permanent bucketList = harness.addToBattlefieldAndReturn(player1, new BucketList());
        harness.setHand(player1, List.of());
        harness.castFromHand(player1, new DarksteelRelic(), "{0}");
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castInstant(player2, 0, bucketList.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bucketList);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
""")]),
    ("h", "HarnessTheStormTest"): (
        [CARDS + "c.Cancel"],
        ["Cancel"],
        [("castsTheTargetAfterTheHandSpellIsCountered", """
    @Test
    @DisplayName("Still casts the target card when the spell cast from hand is countered in response")
    void castsTheTargetAfterTheHandSpellIsCountered() {
        MagmaticChasm graveyardCard = new MagmaticChasm();
        MagmaticChasm handCard = new MagmaticChasm();
        harness.addToBattlefield(player1, new HarnessTheStorm());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(handCard));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player2, 0, handCard.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(handCard).doesNotContain(graveyardCard);
        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getCard().getId()).isEqualTo(graveyardCard.getId()));
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
