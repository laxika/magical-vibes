#!/usr/bin/env python3
"""Step 15, follow-up after run 17x5b: Ashnod the Uncaring sees a chosen sacrifice.

- AshnodTheUncaringTest: two new cases. Bloodthrone Vampire's "Sacrifice a creature" paid with another creature
  triggers Ashnod, and the copy resolves too (+4/+4 in all): only a sacrifice-self cost or an "any number" sacrifice
  used to be recorded on the ability, so a chosen sacrifice was never seen. A Blazing Torch granted to a creature
  ("{T}, Sacrifice Blazing Torch") triggers it as well: the granting Equipment's sacrifice is recorded the same way.
- HarshMentorTest: a new case, the CR 109.2 fix made with it: an ability of a card off the battlefield (a suspended
  Greater Gargadon in exile) has no activated permanent, so "an ability of an artifact, creature, or land on the
  battlefield" doesn't match it. The old path judged the exiled card as if it were a permanent and dealt 2 damage.

Each edit is (old, new): the old text is replaced; a file that already has the new text is left alone. A new test is
added before the class's closing brace, with its imports. Idempotent. Per letter module.

Usage: step15-ashnod-sacrifice-tests.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

ASHNOD_CHOSEN_SACRIFICE_TEST = '''
    @Test
    @DisplayName("Sacrificing another creature to Bloodthrone Vampire triggers Ashnod, and the copy resolves")
    void chosenSacrificeTriggersAndCopyResolves() {
        harness.addToBattlefield(player1, new AshnodTheUncaring());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new BloodthroneVampire());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vampire), null, null);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(vampire.getPowerModifier()).isEqualTo(4);
        assertThat(vampire.getToughnessModifier()).isEqualTo(4);
    }
'''

ASHNOD_EQUIPMENT_SACRIFICE_TEST = '''
    @Test
    @DisplayName("Sacrificing Blazing Torch for its equipped creature's ability triggers Ashnod")
    void equipmentSacrificeTriggers() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AshnodTheUncaring());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent torch = harness.addToBattlefieldAndReturn(player1, new BlazingTorch());
        torch.setAttachedTo(bears.getId());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bears), null,
                player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(torch);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
'''

HARSH_MENTOR_TEST = '''
    @Test
    @DisplayName("An opponent's ability of a suspended creature card does not trigger Harsh Mentor")
    void suspendedCreatureCardAbilityDoesNotTrigger() {
        harness.addToBattlefield(player2, new HarshMentor());
        harness.setLife(player1, 20);
        GreaterGargadon gargadon = new GreaterGargadon();
        harness.setHand(player1, List.of(gargadon));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateHandAbility(player1, 0, null);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.activateExiledAbility(player1, gargadon.getId());
        harness.handlePermanentChosen(player1, forest.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(GreaterGargadon.class);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.exiledCardTimeCounters).containsEntry(gargadon.getId(), 9);
    }
'''

TESTS = {
    "HarshMentorTest": (
        [("@CardUsed({HarshMentor.class, BloodlustInciter.class, EvolvingWilds.class, Censor.class, SacredCat.class})",
          "@CardUsed({HarshMentor.class, BloodlustInciter.class, EvolvingWilds.class, Censor.class, SacredCat.class,\n"
          "        GreaterGargadon.class, Forest.class})")],
        [("void suspendedCreatureCardAbilityDoesNotTrigger()", HARSH_MENTOR_TEST)],
        ["cards.g.GreaterGargadon", "cards.f.Forest", "model.Permanent"]),
    "AshnodTheUncaringTest": (
        [("@CardUsed({AshnodTheUncaring.class, AetherSpellbomb.class, GrizzlyBears.class, ProdigalPyromancer.class,\n"
          "        GreaterGargadon.class, Forest.class})",
          "@CardUsed({AshnodTheUncaring.class, AetherSpellbomb.class, GrizzlyBears.class, ProdigalPyromancer.class,\n"
          "        GreaterGargadon.class, Forest.class, BloodthroneVampire.class, BlazingTorch.class})")],
        [("void chosenSacrificeTriggersAndCopyResolves()", ASHNOD_CHOSEN_SACRIFICE_TEST),
         ("void equipmentSacrificeTriggers()", ASHNOD_EQUIPMENT_SACRIFICE_TEST)],
        ["cards.b.BloodthroneVampire", "cards.b.BlazingTorch", "java:org.junit.jupiter.api.DisplayName"]),
}


def import_line(fqcn):
    return f"import {fqcn[5:]};" if fqcn.startswith("java:") else f"import {PKG}.{fqcn};"


def add_import(text, fqcn):
    line = import_line(fqcn)
    if line in text:
        return text
    imports = list(re.finditer(r"^import (?!static )[^;]+;$", text, re.M))
    pos = imports[-1].end()
    return text[:pos] + "\n" + line + text[pos:]


def convert_file(path, name):
    if name not in TESTS:
        return
    text = path.read_text()
    original = text
    edits, added, imports = TESTS[name]
    for old, new in edits:
        if old in text:
            text = text.replace(old, new)
        elif new not in text:
            raise SystemExit(f"{name}: expected text not found:\n{old}")
    for marker, test in added:
        if marker not in text:
            text = text.rstrip()
            if not text.endswith("}"):
                raise SystemExit(f"{name}: no closing brace")
            text = text[:-1].rstrip("\n") + "\n" + test + "}\n"
    for fqcn in imports:
        text = add_import(text, fqcn)
    if text != original:
        path.write_text(text)
        print(f"converted {name}")


def main():
    modules = sorted(set("abcdefghijklmnopqrstuvwxyz")) if sys.argv[1:] == ["all"] else sys.argv[1:]
    if not modules:
        raise SystemExit(__doc__)
    for module in modules:
        root = ROOT / f"magical-vibes-card-test/{module}/src/test/java"
        if not root.exists():
            continue
        for path in sorted(root.rglob("*.java")):
            convert_file(path, path.stem)


if __name__ == "__main__":
    main()
