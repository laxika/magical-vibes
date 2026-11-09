#!/usr/bin/env python3
"""Step 15, run 17z11e2: the new card cases.

- Necrotic Plague and Shifting Shadow: the upkeep ability the Aura grants is the creature's own, so a creature that
  loses all its abilities after the Aura attached (Dress Down) doesn't have it (CR 613.1f). The collector put the Aura's
  printed stand-in on the stack whatever the creature's abilities were.
- Endless Whispers: "each creature has '…'" grants the ability to Endless Whispers itself only while it is a creature;
  the self grant handler granted it to the enchantment.

Idempotent (a case is added once). Usage: step15-run17z11e2-tests.py [repository root]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"


def test_file(letter, name):
    return TESTS / letter / f"src/test/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"


def add_case(path, marker, case, imports=(), card_used=None):
    text = path.read_text()
    if marker in text:
        return
    end = text.rstrip().rfind("}")
    text = text[:end].rstrip("\n") + "\n\n" + case.rstrip("\n") + "\n}\n"
    for imp in imports:
        line = f"import {imp};\n"
        if line not in text:
            lines = text.split("\n")
            imports_at = [i for i, existing in enumerate(lines) if existing.startswith("import com.github.laxika.")]
            position = next((i for i in imports_at if lines[i] > line.rstrip("\n")), imports_at[-1] + 1)
            lines.insert(position, line.rstrip("\n"))
            text = "\n".join(lines)
    if card_used:
        old, new = card_used
        if new not in text:
            if old not in text:
                raise SystemExit(f"{path}: no {old!r}")
            text = text.replace(old, new, 1)
    path.write_text(text)
    print(f"added {marker} to {path.name}")


add_case(
    test_file("n", "NecroticPlagueTest"),
    "creatureThatLostItsAbilitiesIsNotSacrificed",
    """    @Test
    @DisplayName("A creature that loses all abilities after the Aura attaches doesn't have its upkeep ability")
    void creatureThatLostItsAbilitiesIsNotSacrificed() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        castNecroticPlagueOn(player1, creature);
        harness.addToBattlefield(player1, new DressDown());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }
""",
    imports=["com.github.laxika.magicalvibes.cards.d.DressDown"],
    card_used=("DoomBlade.class})", "DoomBlade.class,\n        DressDown.class})"))

add_case(
    test_file("s", "ShiftingShadowTest"),
    "creatureThatLostItsAbilitiesIsNotDestroyed",
    """    @Test
    @DisplayName("A creature that loses all abilities after the Aura attaches doesn't have its upkeep ability")
    void creatureThatLostItsAbilitiesIsNotDestroyed() {
        Permanent enchanted = addCreature(player2);
        Permanent aura = attachShadow(player1, enchanted);
        harness.addToBattlefield(player1, new DressDown());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
        assertThat(aura.getAttachedTo()).isEqualTo(enchanted.getId());
    }
""",
    imports=["com.github.laxika.magicalvibes.cards.d.DressDown"],
    card_used=("@CardUsed({ShiftingShadow.class,", "@CardUsed({ShiftingShadow.class, DressDown.class,"))

add_case(
    test_file("e", "EndlessWhispersTest"),
    "nonCreatureEndlessWhispersDoesNotHaveItsGrantedAbility",
    """    @Test
    @DisplayName("Endless Whispers that isn't a creature doesn't have the ability it grants each creature")
    void nonCreatureEndlessWhispersDoesNotHaveItsGrantedAbility() {
        harness.addToBattlefield(player1, new EndlessWhispers());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Endless Whispers"));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();
        harness.assertInGraveyard(player1, "Endless Whispers");
    }
""",
    imports=["com.github.laxika.magicalvibes.cards.n.Naturalize"],
    card_used=("Opalescence.class})", "Opalescence.class, Naturalize.class})"))
