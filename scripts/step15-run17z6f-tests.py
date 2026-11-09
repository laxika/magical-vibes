#!/usr/bin/env python3
"""Step 15, run 17z6f: card cases for the Kirins' "with that spell's mana value" read as the cast trigger resolves.

Each new case counters the triggering spell in response (Counterspell), so the ability resolves after the spell has left
the stack and reads its mana value from the entry's snapshot of the spell (CR 608.2h): Celestial Kirin's destroy on the
non-targeting entry (an X Arcane spell, so the snapshot's chosen X counts) and Infernal Kirin's discard on the
single-target choice's entry. The cases are guards: the collector baked the spell's mana value as the ability's X as it
triggered at HEAD.

Idempotent: a class that already has its case is left alone. An optional repository-root argument applies it to
another checkout (the HEAD worktree).

Usage: step15-run17z6f-tests.py [<repository root>]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "com.github.laxika.magicalvibes"

CASES = {
    "c/CelestialKirinTest": ("counteredXArcaneSpellStillDestroysPermanentsWithItsManaValue",
        ["cards.c.Counterspell"], ["Counterspell.class"], '''
    @Test
    @DisplayName("An X Arcane spell countered before the ability resolves still destroys permanents with its mana value")
    void counteredXArcaneSpellStillDestroysPermanentsWithItsManaValue() {
        harness.addToBattlefield(player1, new CelestialKirin());
        harness.addToBattlefield(player1, new ArabaMothrider());
        GhostLitRedeemer first = new GhostLitRedeemer();
        SakuraTribeScout second = new SakuraTribeScout();
        harness.setGraveyard(player1, List.of(first, second));
        DeathDenied deathDenied = new DeathDenied();
        harness.setHand(player1, List.of(deathDenied));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, 2, null);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, deathDenied.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Death Denied");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Celestial Kirin");
        harness.assertOnBattlefield(player1, "Araba Mothrider");
    }
'''),
    "i/InfernalKirinTest": ("counteredSpiritStillDiscardsCardsWithItsManaValue",
        ["cards.c.Counterspell"], ["Counterspell.class"], '''
    @Test
    @DisplayName("A Spirit countered before the ability resolves still discards the cards with its mana value")
    void counteredSpiritStillDiscardsCardsWithItsManaValue() {
        addInfernalKirin();
        GhostLitRedeemer redeemer = new GhostLitRedeemer();
        harness.castFromHand(player1, redeemer, "{W}");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player2, List.of(new Counterspell(), new GhostLitStalker(), new HandOfHonor()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, redeemer.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Ghost-Lit Redeemer");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Hand of Honor");
        harness.assertInGraveyard(player2, "Ghost-Lit Stalker");
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
    first_java = next(i for i, m in enumerate(imports) if m.group(0).startswith("import java"))
    if first_java == 0 or any(not m.group(0).startswith("import java") for m in imports[first_java:]):
        return text
    ours_end = imports[first_java - 1].end()
    return text[:start] + "\n".join(ours) + text[ours_end:]


def apply(rel, method, imports, used, body):
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
