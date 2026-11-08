#!/usr/bin/env python3
"""Step 15, run 17z3b: Armory Automaton's attack cases answer the multi-permanent attack-target choice.

The self-attack collector no longer routes an ability that attaches Equipment to the slot-by-slot
`ETBTokenMultiTargetTrigger` choice by its effect's class: Armory Automaton's "whenever this creature attacks, you may
attach any number of target Equipment to it" is one static "up to N" group, so it takes the multi-permanent
`AttackTriggerTargets` choice as every such group does, all its targets announced at once (CR 601.2c, 603.3d). Its
enters ability keeps the slot-by-slot choice of the enters collector. The six attack cases of `ArmoryAutomatonTest`
answered one target at a time and finished with the player's own id; they choose the Equipment in one answer (none
for "choose zero"). `canTargetMoreThanNinetyNineEquipment` checks the choice offers all one hundred.

Edits are scoped to the named test methods. Idempotent: a method already converted is left alone.

Usage: step15-run17z3b-tests.py [<repository root>]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TEST = ROOT / "magical-vibes-card-test/a/src/test/java/com/github/laxika/magicalvibes/cards/a/ArmoryAutomatonTest.java"

METHOD_EDITS = {
    "attackAttachesAnyTargetedEquipment": [(
        """        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, ownEquipment.getId());
        harness.handlePermanentChosen(player1, opposingEquipment.getId());
""",
        """        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(ownEquipment.getId(), opposingEquipment.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(ownEquipment.getId(), opposingEquipment.getId()));
""")],
    "canDeclineAttachmentAtResolutionAfterChoosingAttackTargets": [(
        """        harness.handlePermanentChosen(player1, equipment.getId());
""",
        """        harness.handleMultiplePermanentsChosen(player1, List.of(equipment.getId()));
""")],
    "canChooseZeroEquipmentOnAttack": [(
        """        harness.handlePermanentChosen(player1, player1.getId());
""",
        """        harness.handleMultiplePermanentsChosen(player1, List.of());
""")],
    "equipmentStaysOnItsPreviousCreatureWhenAutomatonLeavesBeforeResolution": [(
        """        harness.handlePermanentChosen(player1, equipment.getId());
""",
        """        harness.handleMultiplePermanentsChosen(player1, List.of(equipment.getId()));
""")],
    "remainingLegalEquipmentMovesFromItsPreviousCreatureWhenAnotherTargetLeaves": [(
        """        harness.handlePermanentChosen(player1, survivingEquipment.getId());
        harness.handlePermanentChosen(player1, removedEquipment.getId());
""",
        """        harness.handleMultiplePermanentsChosen(player1, List.of(survivingEquipment.getId(), removedEquipment.getId()));
""")],
    "canTargetMoreThanNinetyNineEquipment": [(
        """        for (int i = 0; i < 99; i++) {
            harness.handlePermanentChosen(player1, equipment.get(i).getId());
        }

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, equipment.get(99).getId());
""",
        """        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.maxCount()).isEqualTo(100);
        harness.handleMultiplePermanentsChosen(player1, equipment.stream().map(Permanent::getId).toList());
""")],
}


def method_span(text, name):
    start = text.find(f"    void {name}() {{")
    if start < 0:
        raise SystemExit(f"{TEST.name}: method {name} not found")
    end = text.find("\n    }\n", start)
    return start, end + len("\n    }\n")


def main():
    text = TEST.read_text()
    changed = 0
    for name, edits in METHOD_EDITS.items():
        start, end = method_span(text, name)
        body = text[start:end]
        new_body = body
        for old, new in edits:
            if new in new_body:
                continue
            if old not in new_body:
                raise SystemExit(f"{TEST.name}#{name}: expected text not found")
            new_body = new_body.replace(old, new, 1)
        if new_body != body:
            text = text[:start] + new_body + text[end:]
            changed += 1
    TEST.write_text(text)
    print(f"{TEST.name}: {changed} method(s) converted")


if __name__ == "__main__":
    main()
