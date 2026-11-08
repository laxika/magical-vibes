#!/usr/bin/env python3
"""Step 15, run 17v: Funeral March's new case.

Funeral March (5ED) is the existing `SacrificePermanentsEffect` with `TRIGGERING_PERMANENT_CONTROLLER`, which honours
"spells and abilities your opponents control can't cause you to sacrifice permanents" (Tajuru Preserver, ROE); the
deleted `EnchantedControllerSacrificesCreatureOnLeaveEffect` ignored it (a rules fix).

Idempotent.

Usage: step15-run17v-tests.py <letter test module dir>...
"""
import pathlib
import sys

EDITS = {
    "FuneralMarchTest": [
        ("import com.github.laxika.magicalvibes.cards.h.HillGiant;\n",
         "import com.github.laxika.magicalvibes.cards.h.HillGiant;\n"
         "import com.github.laxika.magicalvibes.cards.t.TajuruPreserver;\n"),
        ("@CardUsed({FuneralMarch.class, FountainOfYouth.class, GrizzlyBears.class, HillGiant.class})",
         "@CardUsed({FuneralMarch.class, FountainOfYouth.class, GrizzlyBears.class, HillGiant.class,\n"
         "        TajuruPreserver.class})"),
        ("""    @Test
    @DisplayName("Removing Funeral March itself does not trigger its ability")
""", """    @Test
    @DisplayName("An opponent's Funeral March can't make a player with Tajuru Preserver sacrifice")
    void opponentsAbilityCantCauseSacrificeUnderTajuruPreserver() {
        // Aura controlled by player1 enchants player2's creature; player2's Tajuru Preserver stops the sacrifice.
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent preserver = harness.addToBattlefieldAndReturn(player2, new TajuruPreserver());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        attachFuneralMarch(player1, enchanted);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, enchanted));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(preserver, giant);
    }

    @Test
    @DisplayName("Removing Funeral March itself does not trigger its ability")
"""),
    ],
}


def main(dirs):
    for d in dirs:
        root = pathlib.Path(d)
        for name, edits in EDITS.items():
            for path in root.rglob(f"cards/*/{name}.java"):
                text = path.read_text()
                original = text
                for old, new in edits:
                    if new in text:
                        continue
                    if old not in text:
                        raise SystemExit(f"{path}: expected text not found: {old!r}")
                    text = text.replace(old, new, 1)
                if text != original:
                    path.write_text(text)
                    print(f"converted {path}")


if __name__ == "__main__":
    main(sys.argv[1:])
