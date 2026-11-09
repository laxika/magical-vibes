#!/usr/bin/env python3
"""Step 15, run 17z7b: Next of Kin's card test for "when enchanted creature dies" judged as the creature last existed.

The class-keyed enchanted-death handler checked the dying permanent's card for the creature type; the card now writes
`Triggers.creatureDies(ObjectRelation.ENCHANTED)`, judged by the trigger pipeline on the permanent as it last existed on
the battlefield. A creature put into a graveyard from the battlefield dies (CR 700.4), and "lesser mana value" is that
creature's. The new case, an artifact animated by March of the Machines, guards the new trigger condition; it passes
at HEAD too.

Idempotent; an optional first argument is the repository root (used for the HEAD worktree).

Usage: step15-run17z7b-tests.py [<repository root>]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"


def test_path(letter, name):
    return ROOT / f"magical-vibes-card-test/{letter}/src/test/java" / PKG.replace(".", "/") / f"cards/{letter}/{name}.java"


def add_import(text, line):
    if line in text:
        return text
    lines = text.splitlines(keepends=True)
    imports = [i for i, l in enumerate(lines) if l.startswith("import ") and not l.startswith("import static")]
    target = line.strip()
    for i in imports:
        if lines[i].strip() > target and not lines[i].startswith("import java."):
            lines.insert(i, line)
            return "".join(lines)
    java = [i for i in imports if lines[i].startswith("import java.")]
    lines.insert(java[0] if java else imports[-1] + 1, line)
    return "".join(lines)


NEW_CASE = """    @Test
    @DisplayName("triggers when an animated artifact dies, offering a creature with lesser mana value than it had")
    void triggersWhenAnimatedArtifactCreatureDies() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        Permanent tome = addCreatureReady(player1, new JayemdaeTome());
        Card replacement = new GrizzlyBears();
        castAura(tome, new NextOfKin());

        destroyDyingCreature(tome.getId(), replacement);

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(replacement.getId());
        harness.handleMultipleCardsChosen(player1, List.of(replacement.getId()));
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

"""

CARD_USED = ("@CardUsed({NextOfKin.class, DoomBlade.class, HillGiant.class, GrizzlyBears.class})",
             "@CardUsed({NextOfKin.class, DoomBlade.class, HillGiant.class, GrizzlyBears.class,\n"
             "        MarchOfTheMachines.class, JayemdaeTome.class})")


def main():
    path = test_path("n", "NextOfKinTest")
    text = path.read_text()
    if "triggersWhenAnimatedArtifactCreatureDies" in text:
        print("NextOfKinTest: already converted")
        return
    anchor = "    private void castAura("
    if anchor not in text or CARD_USED[0] not in text:
        raise SystemExit(f"{path}: the expected text is missing")
    text = text.replace(anchor, NEW_CASE + anchor, 1)
    text = text.replace(CARD_USED[0], CARD_USED[1], 1)
    for name in ("cards.j.JayemdaeTome", "cards.m.MarchOfTheMachines"):
        text = add_import(text, f"import {PKG}.{name};\n")
    path.write_text(text)
    print("NextOfKinTest: converted")


if __name__ == "__main__":
    main()
