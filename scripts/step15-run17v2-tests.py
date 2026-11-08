#!/usr/bin/env python3
"""Step 15, run 17v2: Hateful Eidolon's new case. "Whenever an enchanted creature dies" is the trigger's filter, so a
creature enchanted only by an opponent's Aura triggers the ability, which draws no card (the deleted collector skipped
it). Idempotent.

Usage: step15-run17v2-tests.py <letter test module dir>...
"""
import pathlib
import sys

CASE = """    @Test
    @DisplayName("A creature enchanted only by an opponent's Aura triggers the ability, which draws nothing")
    void onlyOpponentsAuraTriggersAndDrawsNothing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        harness.addToBattlefield(player1, new HatefulEidolon());
        attachAura(player2, creature);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new NyxbornColossus()));

        creature.setMarkedDamage(8);
        harness.runStateBasedActions();

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Hateful Eidolon"));
        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Hateful Eidolon"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

"""
ANCHOR = "    @Test\n    void doesNotDrawForUnenchantedEnchantmentCreature() {"


def main(dirs):
    for d in dirs:
        for path in pathlib.Path(d).rglob("cards/h/HatefulEidolonTest.java"):
            text = path.read_text()
            if "onlyOpponentsAuraTriggersAndDrawsNothing" in text:
                continue
            if ANCHOR not in text:
                raise SystemExit(f"{path}: anchor not found")
            path.write_text(text.replace(ANCHOR, CASE + ANCHOR, 1))
            print(f"converted {path}")


if __name__ == "__main__":
    main(sys.argv[1:])
