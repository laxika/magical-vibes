#!/usr/bin/env python3
"""Step 15, run 17w: card test cases for the cast triggers that read the triggering spell as they resolve.

Adds Rashmi, Eternities Crafter's case for its ruling "If Rashmi's ability resolves and the spell that caused it to
trigger has been countered, use that spell's mana value as it last existed on the stack": the ability reads the stack
entry's triggering spell. Idempotent. Per letter module.

Usage: step15-run17w-tests.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test/{letter}/src/test/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"

RASHMI_CASE = '''    @Test
    @DisplayName("A first spell countered before the ability resolves still sets the mana value")
    void counteredSpellsManaValueIsUsedAsItLastExistedOnTheStack() {
        setupRashmi();
        Card top = new LlanowarElves();
        setLibraryTop(top);
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

'''

EDITS = {
    ("r", "RashmiEternitiesCrafterTest"): [
        ("import com.github.laxika.magicalvibes.cards.f.Forest;\n",
         "import com.github.laxika.magicalvibes.cards.c.Cancel;\nimport com.github.laxika.magicalvibes.cards.f.Forest;\n"),
        ("@CardUsed({RashmiEternitiesCrafter.class, Forest.class,",
         "@CardUsed({RashmiEternitiesCrafter.class, Cancel.class, Forest.class,"),
        ("    private void setupRashmi() {", RASHMI_CASE + "    private void setupRashmi() {"),
    ],
}


def main():
    letters = set("abcdefghijklmnopqrstuvwxyz") if sys.argv[1:] == ["all"] else set(sys.argv[1:])
    if not letters:
        raise SystemExit(__doc__)
    for (letter, name), edits in EDITS.items():
        if letter not in letters:
            continue
        path = pathlib.Path(str(TESTS).format(letter=letter, name=name))
        text = path.read_text()
        if "counteredSpellsManaValueIsUsedAsItLastExistedOnTheStack" in text:
            continue
        for old, new in edits:
            if text.count(old) != 1:
                raise SystemExit(f"{name}: expected text not found once: {old!r}")
            text = text.replace(old, new)
        path.write_text(text)
        print(f"updated {name}")


if __name__ == "__main__":
    main()
