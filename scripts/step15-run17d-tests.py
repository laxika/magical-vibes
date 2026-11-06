#!/usr/bin/env python3
"""Step 15 run 17d of effect-slot-refactor.md: card test changes.

Idempotent; run per letter module (``--module f``) or with no argument for every module this run touches.

- Feline Sovereign: "destroy up to one target artifact or enchantment that player controls" is a target chosen as the
  ability goes on the stack (CR 603.3d), where the old effect's handler offered an up-to-one choice as the ability
  resolved. The three cases that answered that resolution-time choice answer the target choice instead (declining
  by choosing the controller, as Wakka's up-to-one target does) and then resolve the ability.
"""
import argparse
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "src/test/java/com/github/laxika/magicalvibes/cards"

EDITS = {
    ("f", "FelineSovereignTest"): [
        ('''    @DisplayName("A Cat dealing combat damage presents up to one artifact or enchantment")
    void combatDamageDestroysUpToOneArtifactOrEnchantment() {
        addCreatureReady(player1, new FelineSovereign());
        Permanent attacker = addCreatureReady(player1, new SabertoothMauler());
        attacker.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ChromeReplicator());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GarruksUprising());
        Permanent creature = addCreatureReady(player2, new PackLeader());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(artifact.getId(), enchantment.getId())
                .doesNotContain(creature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(artifact.getId()));
''', '''    @DisplayName("A Cat dealing combat damage targets up to one artifact or enchantment that player controls")
    void combatDamageDestroysUpToOneArtifactOrEnchantment() {
        addCreatureReady(player1, new FelineSovereign());
        Permanent attacker = addCreatureReady(player1, new SabertoothMauler());
        attacker.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ChromeReplicator());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GarruksUprising());
        Permanent creature = addCreatureReady(player2, new PackLeader());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(artifact.getId(), enchantment.getId())
                .doesNotContain(creature.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
'''),
        ('''        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
''', '''        resolveCombat();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Chrome Replicator");
'''),
        ('''        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(enchantment.getId()).doesNotContain(ownArtifact.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(enchantment.getId()));
''', '''        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(enchantment.getId()).doesNotContain(ownArtifact.getId());
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();
'''),
    ],
}

# A marker that is present once a test's edits have been applied.
DONE = {
    "FelineSovereignTest": "targets up to one artifact or enchantment that player controls",
}


def convert(letter, name, edits):
    path = TESTS / letter / PKG / letter / f"{name}.java"
    text = path.read_text()
    if DONE[name] in text:
        print(f"unchanged {path.relative_to(ROOT)}")
        return
    for old, new in edits:
        if text.count(old) != 1:
            sys.exit(f"{path}: expected one occurrence of {old!r}")
        text = text.replace(old, new)
    if "List." not in text.split("\nclass ", 1)[-1]:
        text = text.replace("import java.util.List;\n", "")
    path.write_text(text)
    print(f"converted {path.relative_to(ROOT)}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--module", help="letter module to convert (default: all this run touches)")
    args = parser.parse_args()
    for (letter, name), edits in EDITS.items():
        if args.module is None or args.module == letter:
            convert(letter, name, edits)


if __name__ == "__main__":
    main()
