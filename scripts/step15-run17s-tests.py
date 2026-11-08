#!/usr/bin/env python3
"""Step 15, run 17s: card tests for the conditional-judging collector sites.

- Hellish Rebuke (AFC): a case for the granted ability's event condition, "deals damage to the player who cast
  Hellish Rebuke": damage the permanent deals to another player (its own controller) doesn't trigger it.

Idempotent.

Usage: step15-run17s-tests.py <letter test module dir>...
"""
import pathlib
import sys

HELLISH_REBUKE_CASE = '''
    @Test
    @DisplayName("Damage to a player other than the caster doesn't trigger the granted ability")
    void damageToAnotherPlayerDoesNotTrigger() {
        harness.setHand(player1, List.of(new HellishRebuke()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        addCreatureReady(player2, new ProdigalSorcerer());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, null, player2.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Prodigal Sorcerer");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
'''

CASES = {
    "HellishRebukeTest": ("void damageToAnotherPlayerDoesNotTrigger()", HELLISH_REBUKE_CASE),
}


def main():
    changed = []
    seen = set()
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).glob("src/test/java/**/*.java")):
            case = CASES.get(path.stem)
            if case is None:
                continue
            seen.add(path.stem)
            marker, body = case
            source = path.read_text()
            if marker in source:
                continue
            stripped = source.rstrip()
            if not stripped.endswith("}"):
                raise SystemExit(f"{path}: no closing brace")
            path.write_text(stripped[:-1].rstrip() + "\n" + body)
            changed.append(str(path))
    for path in changed:
        print("added", path)
    print(f"{len(changed)} changed, {len(seen)} seen")


if __name__ == "__main__":
    main()
