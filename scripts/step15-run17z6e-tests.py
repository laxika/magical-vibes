#!/usr/bin/env python3
"""Step 15, run 17z6e: card cases for "the amount of mana spent to cast that spell" read as the cast trigger resolves.

Each new case counters the triggering spell in response (Counterspell), so the ability resolves after the spell has left
the stack and reads the mana spent on it from the entry's snapshot of the spell (CR 608.2h), through each path the
cast triggers reading it go on the stack by: the generic collector's non-targeting entry (Aberrant Manawurm's
`ManaSpentToCast` boost; Tellah's `SpellManaSpentAtLeast` "if"s and `ManaSpentToCast` damage), its single-target choice
(Chandra, Dressed to Kill's emblem), the intervening "if" checked again as it resolves (Inga and Esika's
`SpellCreatureManaSpentAtLeast`) and the graveyard spell-cast collector (Summitfest Closing Ceremony's covercast). The
rulings of Aberrant Manawurm and Tellah resolve the ability even if the spell is countered; the cases are guards (the
collector baked the mana spent as the ability's X as it triggered at HEAD).

Idempotent: a class that already has its case is left alone. An optional repository-root argument applies it to
another checkout (the HEAD worktree).

Usage: step15-run17z6e-tests.py [<repository root>]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "com.github.laxika.magicalvibes"

CASES = {
    "a/AberrantManawurmTest": ("counteredSpellStillBoostsByTheManaSpentToCastIt",
        ["cards.c.Counterspell"], ["Counterspell.class"], '''
    @Test
    @DisplayName("A spell countered before the ability resolves still boosts by the mana spent to cast it")
    void counteredSpellStillBoostsByTheManaSpentToCastIt() {
        Permanent manawurm = addManawurm(player1);
        setUpMainPhase(player1);
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, divination.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Divination");
        harness.passBothPriorities();

        assertThat(manawurm.getPowerModifier()).isEqualTo(3);
        assertThat(manawurm.getToughnessModifier()).isZero();
    }
'''),
    "t/TellahGreatSageTest": ("counteredEightManaSpellStillDrawsAndDealsTheManaSpent",
        ["cards.c.Counterspell"], ["Counterspell.class"], '''
    @Test
    @DisplayName("A spell with eight mana spent countered before the ability resolves still draws and deals that damage")
    void counteredEightManaSpellStillDrawsAndDealsTheManaSpent() {
        castTellah();
        MindSpring mindSpring = new MindSpring();
        harness.setHand(player1, List.of(mindSpring));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castSorcery(player1, 0, 6);
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, mindSpring.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Mind Spring");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Hero")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Tellah, Great Sage");
        harness.assertLife(player2, 12);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterCast + 2);
    }
'''),
    "c/ChandraDressedToKillTest": ("emblemDealsTheManaSpentOnASpellCounteredInResponse",
        ["cards.c.Counterspell"], ["Counterspell.class"], '''
    @Test
    @DisplayName("Emblem deals the mana spent on a red spell countered before its ability resolves")
    void emblemDealsTheManaSpentOnASpellCounteredInResponse() {
        createEmblem();
        FieryTemper temper = new FieryTemper();
        harness.setHand(player1, List.of(temper));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, temper.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Fiery Temper");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }
'''),
    "i/IngaAndEsikaTest": ("counteredCreatureSpellStillDrawsByTheCreatureManaSpentOnIt",
        ["cards.c.Counterspell"], ["Counterspell.class"], '''
    @Test
    void counteredCreatureSpellStillDrawsByTheCreatureManaSpentOnIt() {
        harness.addToBattlefield(player1, new IngaAndEsika());
        harness.setHand(player1, List.of(createPriorityHoldingCreature()));
        activateGrantedManaAbility(addCreatureReady(player1, new GrizzlyBears()));
        harness.handleListChoice(player1, "GREEN");
        activateGrantedManaAbility(addCreatureReady(player1, createCreature("Source Two")));
        harness.handleListChoice(player1, "GREEN");
        activateGrantedManaAbility(addCreatureReady(player1, createCreature("Source Three")));
        harness.handleListChoice(player1, "GREEN");
        Card spell = createCreature("Three Mana Creature");
        harness.setHand(player1, List.of(spell));
        harness.castCreature(player1, 0);
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Three Mana Creature");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
'''),
    "s/SummitfestClosingCeremonyTest": ("covercastIntensifiesForAFiveManaSpellCounteredInResponse",
        ["cards.c.Counterspell"], ["Counterspell.class"], '''
    @Test
    void covercastIntensifiesForAFiveManaSpellCounteredInResponse() {
        SummitfestClosingCeremony ceremony = new SummitfestClosingCeremony();
        harness.castFromHand(player1, ceremony, "{3}{U}{R}");
        harness.passBothPriorities();
        JacesIngenuity ingenuity = new JacesIngenuity();
        harness.castFromHand(player1, ingenuity, "{3}{U}{U}");
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, ingenuity.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Jace's Ingenuity");
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(ceremony.getId())).isEqualTo(4);
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
