#!/usr/bin/env python3
"""Step 15, run 17y2: the card tests of the two rules fixes.

- Justice: "whenever a red creature or spell deals damage" asks about the source's color, not the damage's, so Ghostly
  Flame ("black and/or red permanents and spells are colorless sources of damage") doesn't stop it (Ghostly Flame's
  ruling: "things that trigger on a red spell doing damage will still trigger"). The old record judged the damage
  source's color and didn't trigger.
- Essence Sliver: "its controller gains that much life" is read as the ability resolves (CR 608.2h): a Sliver whose
  controller changes while the ability waits gives the life to its new controller. The old handler bound the
  controller as the ability triggered.

Idempotent: a test already added is left alone. Optional repository-root argument (for the HEAD worktree).

Usage: step15-run17y2-tests.py [<repository root>]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = "magical-vibes-card-test/{0}/src/test/java/com/github/laxika/magicalvibes/cards/{0}/{1}.java"

JUSTICE = '''    @Test
    @CardUsed({Justice.class, GhostlyFlame.class, Incinerate.class})
    @DisplayName("A red spell still triggers Justice while Ghostly Flame makes it a colorless source of damage")
    void redSpellTriggersJusticeUnderGhostlyFlame() {
        harness.addToBattlefield(player2, new Justice());
        harness.addToBattlefield(player2, new GhostlyFlame());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // Justice resolves: 3 to Incinerate's controller (player1)

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

'''

SLIVER = '''    @Test
    @CardUsed({EssenceSliver.class, ActOfAggression.class})
    @DisplayName("The Sliver's controller as the ability resolves gains the life")
    void sliversControllerAsTheAbilityResolvesGainsTheLife() {
        Permanent sliver = addCreatureReady(player1, new EssenceSliver());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        sliver.setAttacking(true);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);

        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new ActOfAggression()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, sliver.getId());
        harness.assertOnBattlefield(player2, "Essence Sliver");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

'''

ADDITIONS = [
    ("j", "JusticeTest", "redSpellTriggersJusticeUnderGhostlyFlame", JUSTICE,
     '''    @Test
    @DisplayName("A red spell dealing damage reflects that much to the spell's controller")''',
     [("import com.github.laxika.magicalvibes.cards.g.GhostlyFlame;",
       "import com.github.laxika.magicalvibes.cards.f.FolkOfThePines;")]),
    ("e", "EssenceSliverTest", "sliversControllerAsTheAbilityResolvesGainsTheLife", SLIVER,
     '''    @Test
    @DisplayName("Essence Sliver gains life for the damage it deals")''',
     [("import com.github.laxika.magicalvibes.cards.a.ActOfAggression;",
       None),
      ("import com.github.laxika.magicalvibes.model.ManaColor;",
       "import com.github.laxika.magicalvibes.cards.s.Squire;"),
      ("import com.github.laxika.magicalvibes.model.TurnStep;",
       "import com.github.laxika.magicalvibes.model.Permanent;")]),
]


def main():
    for letter, name, method, body, anchor, imports in ADDITIONS:
        path = ROOT / TESTS.format(letter, name)
        text = path.read_text()
        if method in text:
            continue
        if anchor not in text:
            raise SystemExit(f"{name}: anchor not found")
        text = text.replace(anchor, body + anchor, 1)
        for line, after in imports:
            if line in text:
                continue
            if after is None:
                first = text.index("\nimport ") + 1
                text = text[:first] + line + "\n" + text[first:]
            else:
                if after not in text:
                    raise SystemExit(f"{name}: import anchor {after} not found")
                text = text.replace(after, after + "\n" + line, 1)
        path.write_text(text)
        print(f"added {name}#{method}")


if __name__ == "__main__":
    main()
