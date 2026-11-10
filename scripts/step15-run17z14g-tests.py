#!/usr/bin/env python3
"""Step 15, run 17z14g: the card tests the run adds.

`CASES`: Bumi, Unleashed's "whenever Bumi deals combat damage to a player, untap all lands you control. After this
phase, there is an additional combat phase. ..." and Niv-Mizzet, Guildpact's "whenever Niv-Mizzet deals combat damage
to a player, it deals X damage to any target, target player draws X cards, and you gain X life" are one ability each,
so one stack object (CR 603.3); written as two and three `addTrigger` calls they went on the stack as two and three
objects an opponent could respond between. Each case fails at `5db8dc2383` and passes after run 17z14g.

Usage: step15-run17z14g-tests.py [REPOSITORY_ROOT]. Fails if an edit didn't apply; idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "src/test/java/com/github/laxika/magicalvibes/cards"

# test class -> (anchor the case goes before, method signature, case)
CASES = {
    "BumiUnleashedTest": (
        "    @Test\n    @DisplayName(\"The restriction ends with Bumi's extra combat\")",
        "void combatDamageAbilityIsOneStackObject()",
        """    @Test
    @DisplayName("The combat damage ability is one stack object")
    void combatDamageAbilityIsOneStackObject() {
        Permanent attacker = addCreatureReady(player1, new BumiUnleashed());
        attacker.setAttacking(true);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());

        assertThat(gd.stack).hasSize(1);
    }

"""),
    "NivMizzetGuildpactTest": (
        "    @Test\n    void opponentCanTargetWithMonocoloredSpell() {",
        "void combatDamageAbilityIsOneStackObject()",
        """    @Test
    @DisplayName("The combat damage ability is one stack object with both its targets")
    void combatDamageAbilityIsOneStackObject() {
        Permanent niv = addCreatureReady(player1, new NivMizzetGuildpact());
        niv.setAttacking(true);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(3));
    }

"""),
}
# test class -> imports the case needs
IMPORTS = {
    "NivMizzetGuildpactTest": ["import com.github.laxika.magicalvibes.model.TurnStep;\n"],
}
IMPORT_ANCHOR = "import com.github.laxika.magicalvibes.testutil.BaseCardTest;\n"


def test_path(name):
    letter = name[0].lower()
    return TESTS / letter / PKG / letter / f"{name}.java"


def main():
    failed = []
    for name, (anchor, signature, case) in CASES.items():
        path = test_path(name)
        text = path.read_text()
        if signature in text:
            continue
        if text.count(anchor) != 1 or text.count(IMPORT_ANCHOR) != 1:
            failed.append(name)
            continue
        text = text.replace(anchor, case + anchor)
        for line in IMPORTS.get(name, []):
            if line not in text:
                text = text.replace(IMPORT_ANCHOR, line + IMPORT_ANCHOR)
        path.write_text(text)
    if failed:
        sys.exit("Edits not applied: " + ", ".join(failed))


if __name__ == "__main__":
    main()
