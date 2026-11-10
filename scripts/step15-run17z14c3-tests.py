#!/usr/bin/env python3
"""Step 15, run 17z14c3: the card tests that built what the run removed (row 17z14c3).

- `RamsesAssassinLordTest.resolveLossTrigger` put `WinGameIfPlayerAttackedByControlledSubtypeThisTurnEffect` on the
  stack by hand; the class is gone. It puts the ability's effects as its collector reads them: `WinGameEffect` under
  the intervening "if" `TargetPlayerAttackedBySubtypeControllerControlledThisTurn` (CR 603.4).
- `TemporalExtortionTest.playersMayStillPayAfterTheSpellHasLeftTheStack`, new: "When you cast this spell, any player may
  pay half their life, rounded up. If a player does, counter this spell" has no target, so it resolves once the spell
  has left the stack (CR 608.2b removes only an ability whose targets are all illegal). The generic cast collector no
  longer aims an ability at the spell as a target by the class of its effect; this pins the behaviour for the
  cast-this-spell path too (the case passes before the run as well).

Usage: step15-run17z14c3-tests.py [LETTER...] (run per letter module; no letter means all). Fails if an edit didn't
apply; idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "magical-vibes-card-test"
PKG = "src/test/java/com/github/laxika/magicalvibes/cards"

# test class -> [(old, new)]
EDITS = {
    "RamsesAssassinLordTest": [(
        """                List.of(new WinGameIfPlayerAttackedByControlledSubtypeThisTurnEffect(CardSubtype.ASSASSIN)),""",
        """                List.of(new ConditionalEffect(
                        new TargetPlayerAttackedBySubtypeControllerControlledThisTurn(CardSubtype.ASSASSIN),
                        new WinGameEffect())),"""), (
        """import com.github.laxika.magicalvibes.model.effect.WinGameIfPlayerAttackedByControlledSubtypeThisTurnEffect;""",
        """import com.github.laxika.magicalvibes.model.condition.TargetPlayerAttackedBySubtypeControllerControlledThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;""")],
    "TemporalExtortionTest": [(
        """    private void castTemporalExtortion() {""",
        """    @Test
    @DisplayName("Players may still pay after the spell has left the stack: the ability has no target")
    void playersMayStillPayAfterTheSpellHasLeftTheStack() {
        harness.setLife(player2, 20);
        castTemporalExtortion();
        gd.stack.removeIf(entry -> entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 10);
        assertThat(gd.extraTurns).isEmpty();
    }

    private void castTemporalExtortion() {"""), (
        """import com.github.laxika.magicalvibes.model.PendingInteraction;""",
        """import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;""")],
}


def find(name):
    for path in ROOT.glob(f"*/{PKG}/*/{name}.java"):
        return path.relative_to(ROOT).parts[0], path
    raise SystemExit(f"{name}: not found")


def main():
    letters = set(sys.argv[1:])
    for name, edits in EDITS.items():
        module, path = find(name)
        if letters and module not in letters:
            continue
        text = path.read_text()
        changed = False
        for old, new in edits:
            if new in text:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: edit didn't apply")
            text = text.replace(old, new)
            changed = True
        if changed:
            path.write_text(text)
            print(f"edited {module}/{name}")


if __name__ == "__main__":
    main()
