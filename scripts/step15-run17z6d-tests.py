#!/usr/bin/env python3
"""Step 15, run 17z6d: a card case for the non-targeting counters read as an effect property.

Counterbalance's "counter that spell if it has the same mana value" doesn't target the spell, so its ability still
resolves, offering the reveal, after the spell was countered in response (CR 608.2b): a guard that passes at HEAD,
where the collector named the class. Swindler's Scheme's and Mirrored Depths' tests already have that case, and
Magmablood Archaic's has its countered-spell case (the colors spent read from the spell as it last existed).

Idempotent: a class that already has its case is left alone. An optional repository-root argument applies it to
another checkout (the HEAD worktree).

Usage: step15-run17z6d-tests.py [<repository root>]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "com.github.laxika.magicalvibes"

CASES = {
    "c/CounterbalanceTest": ("abilityResolvesAfterTheSpellWasCounteredInResponse", [], ["Counterspell.class"], '''
    @Test
    @DisplayName("The ability still resolves and offers the reveal after the spell was countered in response")
    void abilityResolvesAfterTheSpellWasCounteredInResponse() {
        harness.addToBattlefield(player1, new Counterbalance());
        harness.setLibrary(player1, List.of(new KjeldoranOutrider()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        KjeldoranOutrider spell = new KjeldoranOutrider();
        harness.castFromHand(player2, spell, "{1}{W}");
        harness.setHand(player1, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, spell.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card.getId().equals(spell.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
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
