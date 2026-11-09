#!/usr/bin/env python3
"""Step 15, run 17z6b: card cases for "that spell's mana value" read as the cast trigger resolves.

Each new case counters the triggering spell in response (Counterspell), so the ability resolves after the spell has left
the stack and reads its mana value as it last existed there (CR 608.2h), through each path the generic cast collector
puts an ability on the stack by: the non-targeting entry (Vial Smasher, Metallurgic Summonings, G'raha Tia's "you may
pay" and its Hero token, Imoti's granted cascade), the single-target choice (Keeper of Secrets) and the multi-target
"up to one" choice (Dusty Parlor). Each card's rulings read the countered spell's mana value as it last existed; the
cases are guards (the collector baked the value as the ability triggered at HEAD).

Idempotent: a class that already has its case is left alone. An optional repository-root argument applies it to
another checkout (the HEAD worktree).

Usage: step15-run17z6b-tests.py [<repository root>]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "com.github.laxika.magicalvibes"

CASES = {
    "v/VialSmasherTheFierceTest": ("counteredFirstSpellStillDealsItsManaValueAsItLastExisted",
        ["cards.c.Counterspell"], ["Counterspell.class"], '''
    @Test
    @DisplayName("A first spell countered before the ability resolves deals its mana value as it last existed")
    void counteredFirstSpellStillDealsItsManaValueAsItLastExisted() {
        harness.addToBattlefield(player1, new VialSmasherTheFierce());
        WalkingBallista ballista = new WalkingBallista();
        harness.setHand(player1, List.of(ballista));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        gs.playCard(gd, player1, 0, 3, null, null);
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, ballista.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Walking Ballista");
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }
'''),
    "m/MetallurgicSummoningsTest": ("counteredSpellStillCreatesConstructOfItsManaValue",
        ["cards.c.Counterspell"], ["Counterspell.class"], '''
    @Test
    @DisplayName("A spell countered before the ability resolves still sizes the Construct by its mana value")
    void counteredSpellStillCreatesConstructOfItsManaValue() {
        harness.addToBattlefield(player1, new MetallurgicSummonings());
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

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(3);
    }
'''),
    "g/GrahaTiaScionRebornTest": ("counteredSpellStillSetsTheLifeAndCounters",
        ["cards.c.Counterspell"], ["Counterspell.class"], '''
    @Test
    void counteredSpellStillSetsTheLifeAndCounters() {
        harness.addToBattlefield(player1, new GrahaTiaScionReborn());
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
        harness.handleMayAbilityChosen(player1, true);

        Permanent hero = findPermanent(player1, "Hero");
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }
'''),
    "i/ImotiCelebrantOfBountyTest": ("grantedCascadeOfACounteredSpellUsesItsManaValue",
        ["cards.c.Counterspell", "model.ManaColor"], ["Counterspell.class"], '''
    @Test
    @DisplayName("A granted cascade of a spell countered in response compares with its mana value as it last existed")
    void grantedCascadeOfACounteredSpellUsesItsManaValue() {
        prepareTurn(player1);
        harness.addToBattlefield(player1, new ImotiCelebrantOfBounty());
        harness.setLibrary(player1, List.of(new MammothSpider(), new LlanowarElves()));
        CrawWurm wurm = new CrawWurm();
        harness.castFromHand(player1, wurm, "{4}{G}{G}");
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, wurm.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Craw Wurm");
        harness.passBothPriorities();

        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Mammoth Spider");
    }
'''),
    "k/KeeperOfSecretsTest": ("counteredSpellStillDealsItsManaValue",
        ["cards.c.Counterspell"], ["Counterspell.class"], '''
    @Test
    @DisplayName("A spell countered before the ability resolves still deals its mana value to the target opponent")
    void counteredSpellStillDealsItsManaValue() {
        harness.addToBattlefield(player1, new KeeperOfSecrets());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, spell.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }
'''),
    "s/SecretArcadeDustyParlorTest": ("dustyParlorUsesACounteredEnchantmentsManaValue",
        ["cards.c.Counterspell"], ["Counterspell.class"], '''
    @Test
    void dustyParlorUsesACounteredEnchantmentsManaValue() {
        castRoom(1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GhostlyPrison prison = new GhostlyPrison();
        harness.setHand(player1, List.of(prison));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, prison.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Ghostly Prison");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
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
