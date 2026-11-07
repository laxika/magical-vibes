#!/usr/bin/env python3
"""Step 15, run 17h: card cases for an ability's own target filter (TriggeredAbility.withTargetFilter).

Ajani, Sleeper Agent's emblem and Passionate Archaeologist's granted ability name "target opponent" on the ability
itself (no card binds their effects to a target group); the cases check that the target choice offers only the
opponent. Idempotent.

Usage: step15-run17h-card-tests.py <card-test letter module dir>...
"""
import pathlib
import sys

ROOT = "src/test/java/com/github/laxika/magicalvibes/cards"
CASES = {
    "a/AjaniSleeperAgentTest.java": ("emblemCanTargetOnlyAnOpponent", """    @Test
    void emblemCanTargetOnlyAnOpponent() {
        addReadyAjani(6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
    }

""", "    @Test\n    void emblemDoesNotTriggerForNoncreatureSpell() {"),
    "p/PassionateArchaeologistTest.java": ("grantedAbilityCanTargetOnlyAnOpponent", """    @Test
    void grantedAbilityCanTargetOnlyAnOpponent() {
        harness.addToBattlefield(player1, new PassionateArchaeologist());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        gd.makeCommander(player1.getId(), commander.getOriginalCard());

        GrizzlyBears spell = new GrizzlyBears();
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        harness.castFromExile(player1, spell.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
    }

""", "    @Test\n    void doesNotTriggerForHandCastOrNonCommanderCreature() {"),
}
IMPORT = "import com.github.laxika.magicalvibes.model.PendingInteraction;\n"


def main(modules):
    for module in modules:
        letter = pathlib.Path(module).name
        for relative, (name, case, anchor) in CASES.items():
            if not relative.startswith(letter + "/"):
                continue
            path = pathlib.Path(module) / ROOT / relative
            source = path.read_text()
            if f"void {name}()" in source:
                continue
            if source.count(anchor) != 1:
                raise SystemExit(f"{relative}: anchor not found")
            source = source.replace(anchor, case + anchor)
            if IMPORT not in source:
                first = source.index("\nimport ") + 1
                source = source[:first] + IMPORT + source[first:]
            path.write_text(source)
            print(f"added {relative}#{name}")


if __name__ == "__main__":
    main(sys.argv[1:])
