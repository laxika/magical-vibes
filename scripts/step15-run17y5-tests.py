#!/usr/bin/env python3
"""Step 15, run 17y5: the card tests that prove the creature that dealt the trigger event's damage deals the damage of
"that Archer / it deals that much damage" (CR 608.2h), with its characteristics (CR 120.3).

- Greatbow Doyen: the Archer, not Doyen, is the source of the reflected damage (a guard: it passes at HEAD, whose
  handler made the Archer the entry's source); an Archer that left the battlefield before the reflection resolves
  deals it with the lifelink it had as it left (the rules fix: at HEAD the rebuilt entry named the departed Archer
  without its last-known information, so it had no lifelink).
- Kediss, Emberclaw Familiar: a commander that left the battlefield before the ability resolves deals the damage with
  the lifelink it had (the rules fix: at HEAD the rebuilt entry kept only the commander's card, whose printed keywords
  have no lifelink); protection from the commander's color, not Kediss's, prevents the damage (a guard).
- The banner comments of `GreatbowDoyenTest` go (AGENTS.md).

Idempotent: a test already added is left alone. Optional repository-root argument (for the HEAD worktree).

Usage: step15-run17y5-tests.py [<repository root>]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = "magical-vibes-card-test/{0}/src/test/java/com/github/laxika/magicalvibes/cards/{0}/{1}.java"

GREATBOW = '''    @Test
    @DisplayName("The Archer, not Greatbow Doyen, deals the reflected damage")
    void theArcherDealsTheReflectedDamage() {
        Permanent doyen = addCreatureReady(player1, new GreatbowDoyen());
        Permanent archer = addCreatureReady(player1, new FemerefArchers());
        Permanent flyer = addCreatureReady(player2, new SkyhunterSkirmisher());
        flyer.setAttacking(true);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, flyer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.damageDealtBySourceToPlayerThisTurn(archer.getId(), player2.getId())).isEqualTo(4);
        assertThat(gd.damageDealtBySourceToPlayerThisTurn(doyen.getId(), player2.getId())).isZero();
    }

    @Test
    @DisplayName("An Archer that left the battlefield deals the reflected damage with the lifelink it had")
    void departedArcherDealsTheReflectedDamageWithItsLifelink() {
        addCreatureReady(player1, new GreatbowDoyen());
        Permanent archer = addCreatureReady(player1, new FemerefArchers());
        Permanent warhammer = harness.addToBattlefieldAndReturn(player1, new LoxodonWarhammer());
        warhammer.setAttachedTo(archer.getId());
        Permanent flyer = addCreatureReady(player2, new SkyhunterSkirmisher());
        flyer.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, flyer.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, archer.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Femeref Archers");
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 28);
    }

'''

KEDISS = '''    @Test
    @DisplayName("A commander that left the battlefield deals the damage with the lifelink it had")
    void departedCommanderDealsTheDamageWithItsLifelink() {
        Player otherOpponent = addThirdPlayer();
        Card commanderCard = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commanderCard);
        addCreatureReady(player1, new KedissEmberclawFamiliar());
        Permanent commander = addCreatureReady(player1, commanderCard);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LoxodonWarhammer());
        equipment.setAttachedTo(commander.getId());

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackers(List.of(1));
            resolveCombat();
        });
        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, commander.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CommanderReturnChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(commander.getId()));
        harness.passBothPriorities();

        harness.assertLife(otherOpponent, 15);
        harness.assertLife(player1, 30);
    }

    @Test
    @DisplayName("Protection from the commander's color, not Kediss's, prevents the damage")
    void protectionFromTheCommandersColorPreventsTheDamage() {
        Player otherOpponent = addThirdPlayer();
        Card commanderCard = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commanderCard);
        addCreatureReady(player1, new KedissEmberclawFamiliar());
        addCreatureReady(player1, commanderCard);
        gd.playerProtectionFromColorsUntilEndOfTurn
                .computeIfAbsent(otherOpponent.getId(), k -> new HashSet<>()).add(CardColor.GREEN);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackers(List.of(1));
            resolveCombat();
            resolveAllTriggers();
        });

        harness.assertLife(player2, 18);
        harness.assertLife(otherOpponent, 20);
    }

'''

ADDITIONS = [
    ("g", "GreatbowDoyenTest", "departedArcherDealsTheReflectedDamageWithItsLifelink", GREATBOW,
     "    /** Adds {@code blockerCard} to {@code blocker}'s battlefield blocking the attacker at {@code attackerIndex}. */",
     [("import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;",
       "import com.github.laxika.magicalvibes.cards.k.KeenEyedArchers;")],
     [("@CardUsed({GreatbowDoyen.class, ActOfAggression.class, KeenEyedArchers.class, GrizzlyBears.class, SerraAngel.class,\n"
       "        FemerefArchers.class, SkyhunterSkirmisher.class, Terror.class})",
       "@CardUsed({GreatbowDoyen.class, ActOfAggression.class, KeenEyedArchers.class, GrizzlyBears.class, SerraAngel.class,\n"
       "        FemerefArchers.class, SkyhunterSkirmisher.class, Terror.class, LoxodonWarhammer.class})"),
      ("    // ===== Static: other Archer creatures you control get +1/+1 =====\n\n", ""),
      ("    // ===== Trigger: an Archer you control deals damage to a creature =====\n\n", "")]),
    ("k", "KedissEmberclawFamiliarTest", "departedCommanderDealsTheDamageWithItsLifelink", KEDISS,
     "    private Player addThirdPlayer() {",
     [("import com.github.laxika.magicalvibes.cards.t.Terror;", "import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;"),
      ("import com.github.laxika.magicalvibes.model.CardColor;", "import com.github.laxika.magicalvibes.model.Card;"),
      ("import com.github.laxika.magicalvibes.model.ManaColor;", "import com.github.laxika.magicalvibes.model.CardColor;"),
      ("import com.github.laxika.magicalvibes.model.PendingInteraction;", "import com.github.laxika.magicalvibes.model.ManaPool;"),
      ("import java.util.HashSet;", "import java.util.ArrayList;")],
     [("@CardUsed({KedissEmberclawFamiliar.class, GrizzlyBears.class, LoxodonWarhammer.class})",
       "@CardUsed({KedissEmberclawFamiliar.class, GrizzlyBears.class, LoxodonWarhammer.class, Terror.class})")]),
]


def main():
    for letter, name, method, body, anchor, imports, replacements in ADDITIONS:
        path = ROOT / TESTS.format(letter, name)
        text = path.read_text()
        original = text
        for old, new in replacements:
            text = text.replace(old, new)
        if method not in text:
            if anchor not in text:
                raise SystemExit(f"{name}: anchor not found")
            text = text.replace(anchor, body + anchor, 1)
        for line, after in imports:
            if line in text:
                continue
            if after not in text:
                raise SystemExit(f"{name}: import anchor {after} not found")
            text = text.replace(after, after + "\n" + line, 1)
        if text != original:
            path.write_text(text)
            print(f"updated {name}")


if __name__ == "__main__":
    main()
