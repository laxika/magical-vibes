#!/usr/bin/env python3
"""Step 15, runs 17z11f4 and 17z11f5: the new card test case, and the imprint handler's unit test entries name the
Mimic Vat as their source permanent (run 17z11f5: the handler reads the source, not a bound targetId). Idempotent;
optional repository-root argument (the HEAD worktree)."""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
T = ROOT / "magical-vibes-card-test/f/src/test/java/com/github/laxika/magicalvibes/cards/f/FiveAlarmFireTest.java"

CASE = '''
    @Test
    @DisplayName("Five-Alarm Fire that lost its abilities to Song of the Dryads gets no blaze counter")
    void fireTurnedIntoForestDoesNotTrigger() {
        Permanent fire = addFire();
        Permanent song = harness.addToBattlefieldAndReturn(player2, new SongOfTheDryads());
        song.setAttachedTo(fire.getId());
        addReadyCreature(new GrizzlyBears(), player1).setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isZero();
    }
'''


IMPRINT = ROOT / ("magical-vibes-application/src/test/java/com/github/laxika/magicalvibes/service/effect/normalfx/"
                  "ImprintDyingCreatureEffectHandlerTest.java")


def imprint():
    s = IMPRINT.read_text()
    new = s.replace("List.of(effect), vatPerm.getId(), (UUID) null", "List.of(effect), (UUID) null, vatPerm.getId()")
    new = new.replace("List.of(effect), vatId, (UUID) null", "List.of(effect), (UUID) null, vatId")
    if new != s:
        IMPRINT.write_text(new)
        print("edited", IMPRINT.name)


def main():
    imprint()
    s = T.read_text()
    if "fireTurnedIntoForestDoesNotTrigger" in s:
        return
    s = s.replace("import com.github.laxika.magicalvibes.cards.s.SerraAngel;\n",
                  "import com.github.laxika.magicalvibes.cards.s.SerraAngel;\n"
                  "import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;\n", 1)
    s = s.replace("@CardUsed({FiveAlarmFire.class, GrizzlyBears.class, SerraAngel.class, GruulKeyrune.class})",
                  "@CardUsed({FiveAlarmFire.class, GrizzlyBears.class, SerraAngel.class, GruulKeyrune.class,\n"
                  "        SongOfTheDryads.class})", 1)
    end = s.rstrip().rindex("}")
    s = s[:end].rstrip("\n") + "\n" + CASE + "}\n"
    T.write_text(s)
    print("edited", T.name)


if __name__ == "__main__":
    main()
