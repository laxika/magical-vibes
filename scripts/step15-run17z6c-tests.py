#!/usr/bin/env python3
"""Step 15, run 17z6c: card cases for Foul Emissary's "when you sacrifice this while casting a spell with emerge".

Its ruling: the ability "triggers if it's sacrificed for any reason while you're casting a spell with emerge, whether or
not you're casting that spell for its emerge cost", Ashnod's Altar while casting Decimator of the Provinces for {10}
its example. The new cases: that case with Elder Deep-Fiend cast for its mana cost (a mana ability activated to pay for
the held-back cast, the rules fix; it fails at HEAD, where only a sacrifice for the spell's own cost was seen; Elder
Deep-Fiend has flash, because the Altar's triggers go on the stack as it is activated, before the held-back spell, a
limitation of the engine's mana payment noted in Findings), the ability above the spell when it is
sacrificed for the emerge cost (a guard), and two guards that it doesn't trigger when sacrificed to a mana ability
while casting a spell without emerge, or with priority while casting nothing.

Idempotent: a class that already has its case is left alone. An optional repository-root argument applies it to
another checkout (the HEAD worktree).

Usage: step15-run17z6c-tests.py [<repository root>]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "com.github.laxika.magicalvibes"

IMPORTS = ["cards.a.AshnodsAltar", "model.ManaPaymentIntent"]
USED = ["AshnodsAltar.class", "Forest.class"]

CASES = {
    "f/FoulEmissaryTest#1": ("createsTokenWhenSacrificedToAManaAbilityWhileCastingAnEmergeSpell", IMPORTS, USED, '''
    @Test
    @DisplayName("Sacrificed to Ashnod's Altar while casting an emerge spell for its mana cost, it creates a token")
    void createsTokenWhenSacrificedToAManaAbilityWhileCastingAnEmergeSpell() {
        harness.addToBattlefield(player1, new AshnodsAltar());
        addCreatureReady(player1, new FoulEmissary());
        harness.setHand(player1, List.of(new ElderDeepFiend()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityToPayFor(player1, 0, ManaPaymentIntent.forCast(0, 0));
        harness.assertInGraveyard(player1, "Foul Emissary");
        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.getCard().getName()).isEqualTo("Foul Emissary"));
        for (int i = 0; i < 3 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
        assertThat(findPermanents(player1, "Eldrazi Horror")).hasSize(1);
        harness.assertOnBattlefield(player1, "Elder Deep-Fiend");
    }
'''),
    "f/FoulEmissaryTest#2": ("sacrificedForTheEmergeCostTriggersAboveTheSpell", IMPORTS, USED, '''
    @Test
    @DisplayName("Sacrificed for the emerge cost, its ability goes on the stack above the spell")
    void sacrificedForTheEmergeCostTriggersAboveTheSpell() {
        Permanent foulEmissary = addCreatureReady(player1, new FoulEmissary());
        harness.setHand(player1, List.of(new ElderDeepFiend()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(foulEmissary.getId()));

        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Elder Deep-Fiend");
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Foul Emissary");
    }
'''),
    "f/FoulEmissaryTest#3": ("doesNotCreateTokenWhenSacrificedToAManaAbilityWhileCastingANonEmergeSpell", IMPORTS, USED,
        '''
    @Test
    @DisplayName("Sacrificed to Ashnod's Altar while casting a spell without emerge, it creates no token")
    void doesNotCreateTokenWhenSacrificedToAManaAbilityWhileCastingANonEmergeSpell() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new AshnodsAltar());
        addCreatureReady(player1, new FoulEmissary());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.tapPermanentToPayFor(player1, 0, ManaPaymentIntent.forCast(0, 0));
        harness.activateAbilityToPayFor(player1, 1, ManaPaymentIntent.forCast(0, 0));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getCard().getName()).isEqualTo("Grizzly Bears"));
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Eldrazi Horror")).isEmpty();
    }
'''),
    "f/FoulEmissaryTest#4": ("doesNotCreateTokenWhenSacrificedToAManaAbilityWhileCastingNothing", IMPORTS, USED, '''
    @Test
    @DisplayName("Sacrificed to Ashnod's Altar with priority while casting nothing, it creates no token")
    void doesNotCreateTokenWhenSacrificedToAManaAbilityWhileCastingNothing() {
        harness.addToBattlefield(player1, new AshnodsAltar());
        addCreatureReady(player1, new FoulEmissary());
        harness.setHand(player1, List.of(new ElderDeepFiend()));

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Foul Emissary");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
    }
'''),
}

def sort_imports(text):
    imports = list(re.finditer(r"^import (?!static)[^;]+;$", text, re.M))
    if not imports:
        return text
    start, end = imports[0].start(), imports[-1].end()
    block = text[start:end]
    if re.search(r"^(?!import |\s*$).+", block, re.M):
        return text
    lines = [m.group(0) for m in imports]
    ours = sorted(l for l in lines if not l.startswith("import java"))
    java = sorted(l for l in lines if l.startswith("import java"))
    if not java:
        return text[:start] + "\n".join(ours) + text[end:]
    # Keep the file's own layout: only the package's block is re-sorted.
    first_java = next(i for i, m in enumerate(imports) if m.group(0).startswith("import java"))
    if first_java == 0 or any(not m.group(0).startswith("import java") for m in imports[first_java:]):
        return text
    ours_end = imports[first_java - 1].end()
    return text[:start] + "\n".join(ours) + text[ours_end:]


def apply(rel, method, imports, used, body):
    rel = rel.split("#")[0]
    path = TESTS / f"{rel.split('/')[0]}/src/test/java/com/github/laxika/magicalvibes/cards/{rel}.java"
    text = path.read_text()
    if f"void {method}(" in text:
        return
    for imp in imports:
        line = f"import {PKG}.{imp};"
        if line not in text:
            first = re.search(r"^import [^;]+;$", text, re.M)
            text = text[:first.start()] + line + "\n" + text[first.start():]
    m = re.search(r"@CardUsed\(\{([^}]*)\}\)", text)
    names = m.group(1)
    for u in used:
        if u not in names:
            names = names.rstrip() + ", " + u
    text = text[:m.start(1)] + names + text[m.end(1):]
    text = sort_imports(text)
    close = text.rstrip().rfind("}")
    text = text[:close].rstrip() + "\n" + body + "}\n"
    path.write_text(text)
    print(f"added {rel}#{method}")


def main():
    for rel, (method, imports, used, body) in CASES.items():
        apply(rel, method, imports, used, body)


if __name__ == "__main__":
    main()
