#!/usr/bin/env python3
"""Step 15, run 17y3: the card tests of the two rules fixes.

- Circle of Affliction: "whenever a source of the chosen color deals damage to you" asks about the source's color,
  not the damage's, so Ghostly Flame ("black and/or red permanents and spells are colorless sources of damage")
  doesn't stop it (Ghostly Flame's ruling: "It does not change the color of the source"). The old handler judged the
  damage source's color and didn't trigger.
- Teysa, Envoy of Ghosts: the "a creature deals combat damage to you" collector put its abilities straight on the
  stack, outside the collector's dispatch, so "triggers an additional time" (Roaming Throne) never applied to them;
  they now go through it.

Idempotent: a test already added is left alone. Optional repository-root argument (for the HEAD worktree).

Usage: step15-run17y3-tests.py [<repository root>]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = "magical-vibes-card-test/{0}/src/test/java/com/github/laxika/magicalvibes/cards/{0}/{1}.java"

CIRCLE = '''    @Test
    @CardUsed({CircleOfAffliction.class, GhostlyFlame.class, ProdigalPyromancer.class})
    @DisplayName("A red source still triggers it while Ghostly Flame makes the source a colorless source of damage")
    void redSourceTriggersUnderGhostlyFlame() {
        addCircleChoosing(CardColor.RED);
        harness.addToBattlefield(player2, new GhostlyFlame());
        preparePlayer1MainPhase();
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

'''

TEYSA = '''    @Test
    @CardUsed({TeysaEnvoyOfGhosts.class, GrizzlyBears.class, RoamingThrone.class})
    @DisplayName("With Roaming Throne naming Advisor, the ability triggers an additional time")
    void triggersAnAdditionalTimeWithRoamingThrone() {
        harness.addToBattlefield(player2, new TeysaEnvoyOfGhosts());
        Permanent throne = harness.addToBattlefieldAndReturn(player2, new RoamingThrone());
        throne.setChosenSubtype(CardSubtype.ADVISOR);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player2, "Spirit")).isEqualTo(2);
    }

'''

ADDITIONS = [
    ("c", "CircleOfAfflictionTest", "redSourceTriggersUnderGhostlyFlame", CIRCLE,
     '''    @Test
    void decliningPaymentDoesNotDrainTargetPlayer() {''',
     [("import com.github.laxika.magicalvibes.cards.g.GhostlyFlame;", None),
      ("import org.junit.jupiter.api.DisplayName;", "import com.github.laxika.magicalvibes.testutil.CardUsed;")]),
    ("t", "TeysaEnvoyOfGhostsTest", "triggersAnAdditionalTimeWithRoamingThrone", TEYSA,
     '''    @Test
    @DisplayName("Noncombat damage to Teysa's controller does not trigger her ability")''',
     [("import com.github.laxika.magicalvibes.cards.r.RoamingThrone;",
       "import com.github.laxika.magicalvibes.cards.l.LightningBolt;")]),
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
