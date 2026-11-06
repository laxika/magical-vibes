#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 8: card tests for "that source's controller" as the ability resolves.

Idempotent; run once per letter test module:

    for m in magical-vibes-card-test/*/; do python3 scripts/step15-damage-source-tests.py "$m"; done

Archfiend of Spite's official ruling: "The player who loses life or permanents is the player who controls the source
as Archfiend of Spite's triggered ability resolves ... If the source is no longer in the zone it was in as it dealt
damage, the source's last known information is used" (CR 608.2h). Belltower Sphinx, Reaper of Sheoldred, Archfiend of
Spite and Contested War Zone used the controller the source had as the ability triggered; each test changes the
source's controller with Act of Aggression while the ability is on the stack.
"""
import pathlib
import re
import sys

COMBAT_SETUP = """        Permanent chancellor = addCreatureReady(player1, new ChancellorOfTheTangle());
        Permanent blocker = addCreatureReady(player2, new {card}());
        chancellor.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);

        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new ActOfAggression()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, chancellor.getId());
        harness.assertOnBattlefield(player2, "Chancellor of the Tangle");
        harness.passBothPriorities();
"""

TESTS = {
    "BelltowerSphinxTest.java": ("BelltowerSphinx", """
    @Test
    @DisplayName("The source's controller as the ability resolves mills, not the one it had as it dealt damage")
    void sourcesControllerAsTheAbilityResolvesMills() {
        harness.setLibrary(player1, library(8));
        harness.setLibrary(player2, library(8));
""" + COMBAT_SETUP.format(card="BelltowerSphinx") + """
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }
"""),
    "ReaperOfSheoldredTest.java": ("ReaperOfSheoldred", """
    @Test
    @DisplayName("The source's controller as the ability resolves gets the poison counter")
    void sourcesControllerAsTheAbilityResolvesGetsThePoisonCounter() {
""" + COMBAT_SETUP.format(card="ReaperOfSheoldred") + """
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }
"""),
    "ArchfiendOfSpiteTest.java": ("ArchfiendOfSpite", """
    @Test
    @DisplayName("The source's controller as the ability resolves loses the life (official ruling)")
    void sourcesControllerAsTheAbilityResolvesLosesTheLife() {
""" + COMBAT_SETUP.format(card="ArchfiendOfSpite") + """
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
    }
"""),
    "ContestedWarZoneTest.java": ("ContestedWarZone", """
    @Test
    @DisplayName("The creature's controller as the ability resolves gains control")
    void creaturesControllerAsTheAbilityResolvesGainsControl() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.COMBAT_DAMAGE));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(TurnStep.COMBAT_DAMAGE));
        addContestedWarZone(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        resolveCombat(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new ActOfAggression()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Contested War Zone");
        harness.assertNotOnBattlefield(player1, "Contested War Zone");
    }
"""),
}

IMPORTS = [
    "com.github.laxika.magicalvibes.cards.a.ActOfAggression",
    "com.github.laxika.magicalvibes.cards.c.ChancellorOfTheTangle",
    "com.github.laxika.magicalvibes.model.ManaColor",
    "com.github.laxika.magicalvibes.model.Permanent",
    "com.github.laxika.magicalvibes.model.TurnStep",
    "java.util.List",
]


def add_imports(src, test_package, method):
    """Inserts each missing import before the first existing one that sorts after it, keeping the file's grouping."""
    for name in IMPORTS:
        simple = name.rsplit(".", 1)[1]
        if (name.rsplit(".", 1)[0] == test_package or f"import {name};\n" in src
                or not re.search(rf"\b{simple}\b", method)):
            continue
        line = f"import {name};\n"
        imports = re.findall(r"^import (?!static)[^;]+;\n", src, flags=re.M)
        later = [existing for existing in imports if existing > line]
        position = src.index(later[0]) if later else src.rindex(imports[-1]) + len(imports[-1])
        src = src[:position] + line + src[position:]
    return src


def add_card_used(src, method):
    match = re.search(r"@CardUsed\(\{([^}]*)\}\)", src)
    used = [name.strip() for name in match.group(1).split(",") if name.strip()]
    for card in ("ActOfAggression.class", "ChancellorOfTheTangle.class"):
        if card not in used and card.removesuffix(".class") in method:
            used.append(card)
    annotation = "@CardUsed({" + ", ".join(used) + "})"
    if len(annotation) > 120:
        lines, current = [], "@CardUsed({"
        for index, card in enumerate(used):
            piece = card + ("," if index < len(used) - 1 else "})")
            if len(current) + len(piece) + 1 > 120:
                lines.append(current.rstrip())
                current = "        "
            current += piece + " "
        lines.append(current.rstrip())
        annotation = "\n".join(lines)
    return src[:match.start()] + annotation + src[match.end():]


def convert(path, method):
    src = path.read_text()
    name = re.search(r"void (\w+)\(\)", method).group(1)
    if f"void {name}()" in src:
        return
    package = re.search(r"^package ([\w.]+);", src, flags=re.M).group(1)
    converted = add_card_used(add_imports(src, package, method), method)
    closing = converted.rstrip().rindex("}")
    converted = converted[:closing].rstrip("\n") + "\n" + method + "}\n"
    path.write_text(converted)
    print(f"converted {path}")


def main(module):
    for name, (_, method) in TESTS.items():
        for path in sorted(pathlib.Path(module).rglob(name)):
            if "/build/" not in str(path):
                convert(path, method)


if __name__ == "__main__":
    main(sys.argv[1])
