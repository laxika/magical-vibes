#!/usr/bin/env python3
"""Step 15, run 17z9: card tests for "that player" read from the stack entry, not targeted.

The class-keyed cast and land-play handlers that bound "that player" (the caster, the player who played the land) as the
entry's `targetId` left three entries targeting: Painful Quandary's, Rhystic Study's and Horn of Greed's. "That player"
isn't a target (CR 115.10a: only the word "target" makes one), so a player's hexproof (CR 702.11c) doesn't stop it;
the generic collectors' entries are non-targeting. The new cases give the triggering player hexproof (Leyline of
Sanctity): Painful Quandary's loss, Rhystic Study's payment offer and Horn of Greed's draw still happen.

The damage collector put a "whenever this creature is dealt damage, it deals N damage to target opponent or
planeswalker" ability on the stack aimed at `getOpponentId`'s one opponent when no planeswalker was on the battlefield,
asking no one, and so did the combat damage service for combat damage. The target is chosen as the ability goes on the stack (CR 603.3d), so Frilled Deathspitter's, Mogg
Maniac's and Sun-Crowned Hunters' cases that didn't answer a target prompt now choose the opponent; their assertions are
unchanged. The new cases `FrilledDeathspitterTest#controllerChoosesWhichOpponentIsDealtDamage` (noncombat damage) and
`#controllerChoosesWhichOpponentIsDealtDamageAfterCombatDamage` offer both opponents, not the controller, in a
three-player game and damage the one chosen.

Idempotent; an optional first argument is the repository root (used for the HEAD worktree).

Usage: step15-run17z9-tests.py [<repository root>]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"


def test_path(letter, name):
    return ROOT / f"magical-vibes-card-test/{letter}/src/test/java" / PKG.replace(".", "/") / f"cards/{letter}/{name}.java"


def add_import(text, line):
    if line in text:
        return text
    lines = text.splitlines(keepends=True)
    imports = [i for i, l in enumerate(lines) if l.startswith("import ") and not l.startswith("import static")]
    target = line.strip()
    for i in imports:
        if lines[i].strip() > target and not lines[i].startswith("import java."):
            lines.insert(i, line)
            return "".join(lines)
    java = [i for i in imports if lines[i].startswith("import java.")]
    lines.insert(java[0] if java else imports[-1] + 1, line)
    return "".join(lines)


PAINFUL_QUANDARY = """    @Test
    @CardUsed(LeylineOfSanctity.class)
    @DisplayName("That player isn't targeted: an opponent with hexproof still loses 5 life")
    void opponentWithHexproofStillLosesLife() {
        harness.addToBattlefield(player1, new PainfulQuandary());
        harness.addToBattlefield(player2, new LeylineOfSanctity());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player2, new CarapaceForger(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
    }

"""

RHYSTIC_STUDY = """    @Test
    @CardUsed(LeylineOfSanctity.class)
    @DisplayName("That player isn't targeted: an opponent with hexproof is still asked to pay")
    void opponentWithHexproofIsStillAskedToPay() {
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        castOpponentSpellWithManaToPay();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

"""

HORN_OF_GREED = """    @Test
    @CardUsed(LeylineOfSanctity.class)
    @DisplayName("That player isn't targeted: an opponent with hexproof who plays a land still draws")
    void opponentWithHexproofStillDraws() {
        harness.addToBattlefield(player1, new HornOfGreed());
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.setHand(player2, List.of(new VolrathsStronghold()));
        harness.setLibrary(player2, List.of(new VolrathsStronghold()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

"""

FRILLED_DEATHSPITTER = """    @Test
    void controllerChoosesWhichOpponentIsDealtDamage() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player2, new FrilledDeathspitter());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Frilled Deathspitter"));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player3.getId());
        harness.handlePermanentChosen(player2, player3.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player3, 18);
    }

    @Test
    void controllerChoosesWhichOpponentIsDealtDamageAfterCombatDamage() {
        Player player3 = addThirdPlayer();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new FanaticalFirebrand());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FrilledDeathspitter());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player3.getId());
        harness.handlePermanentChosen(player2, player3.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player3, 18);
    }

    private Player addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        Player player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
        return player3;
    }
"""

CHOOSE_OPPONENT = "        harness.handlePermanentChosen(player2, player1.getId());\n"

# The cases that didn't answer the target prompt: each (anchor, replacement) is applied once, in order.
TARGET_ANSWERS = {
    ("f", "FrilledDeathspitterTest"): [
        ("""        harness.resolveCombatDamage();
        harness.passBothPriorities();
""", """        harness.resolveCombatDamage();
""" + CHOOSE_OPPONENT + """        harness.passBothPriorities();
"""),
        ("""        harness.castAndResolveInstant(player1, 0, deathspitterId);
        harness.passBothPriorities();
""", """        harness.castAndResolveInstant(player1, 0, deathspitterId);
""" + CHOOSE_OPPONENT + """        harness.passBothPriorities();
"""),
        ("""        harness.activateAbility(player1, 0, null, deathspitterId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Frilled Deathspitter");""",
         """        harness.activateAbility(player1, 0, null, deathspitterId);
        harness.passBothPriorities();
""" + CHOOSE_OPPONENT + """        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Frilled Deathspitter");"""),
        ("""        harness.activateAbility(player1, 0, null, deathspitterId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Frilled Deathspitter");""",
         """        harness.activateAbility(player1, 0, null, deathspitterId);
        harness.passBothPriorities();
""" + CHOOSE_OPPONENT + """        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Frilled Deathspitter");"""),
    ],
    ("m", "MoggManiacTest"): [
        ("""        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);""",
         """        resolveCombat();
""" + CHOOSE_OPPONENT + """        harness.passBothPriorities();

        harness.assertLife(player1, 19);"""),
        ("""        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Mogg Maniac"));
        resolveAllTriggers();

        harness.assertLife(player1, 18);""",
         """        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Mogg Maniac"));
        harness.passBothPriorities();
""" + CHOOSE_OPPONENT + """        resolveAllTriggers();

        harness.assertLife(player1, 18);"""),
    ],
    ("s", "SunCrownedHuntersTest"): [
        ("""        // Resolve combat damage — auto-pass will resolve the trigger too
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);""",
         """        // Resolve combat damage, choose the trigger's target, then resolve the trigger
        harness.passBothPriorities();
""" + CHOOSE_OPPONENT + """        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);"""),
        ("""        // Resolve combat damage and enrage trigger
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);""",
         """        // Resolve combat damage, choose the enrage trigger's target, then resolve it
        harness.passBothPriorities();
""" + CHOOSE_OPPONENT + """        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);"""),
        ("""        harness.castAndResolveInstant(player1, 0, huntersId);

        // ON_DEALT_DAMAGE trigger should be on the stack with target set to opponent""",
         """        harness.castAndResolveInstant(player1, 0, huntersId);
""" + CHOOSE_OPPONENT + """
        // ON_DEALT_DAMAGE trigger should be on the stack with target set to opponent"""),
        ("""        harness.castAndResolveInstant(player1, 0, huntersId);

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType())""",
         """        harness.castAndResolveInstant(player1, 0, huntersId);
""" + CHOOSE_OPPONENT + """
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType())"""),
        ("""        harness.castAndResolveInstant(player1, 0, huntersId);
        harness.passBothPriorities(); // Resolve first trigger (3 to opponent)""",
         """        harness.castAndResolveInstant(player1, 0, huntersId);
""" + CHOOSE_OPPONENT + """        harness.passBothPriorities(); // Resolve first trigger (3 to opponent)"""),
        ("""        harness.castAndResolveInstant(player1, 0, huntersId);

        // Enrage trigger should be on the stack even though Hunters died""",
         """        harness.castAndResolveInstant(player1, 0, huntersId);
""" + CHOOSE_OPPONENT + """
        // Enrage trigger should be on the stack even though Hunters died"""),
    ],
}

CASES = [
    ("p", "PainfulQuandaryTest", "opponentWithHexproofStillLosesLife", PAINFUL_QUANDARY,
     "    @Test\n    @DisplayName(\"Auto-loses 5 life when opponent has empty hand\")"),
    ("r", "RhysticStudyTest", "opponentWithHexproofIsStillAskedToPay", RHYSTIC_STUDY,
     "    private void castOpponentSpellWithManaToPay() {"),
    ("h", "HornOfGreedTest", "opponentWithHexproofStillDraws", HORN_OF_GREED,
     "    @Test\n    @DisplayName(\"Each Horn of Greed triggers when its controller plays a land\")"),
]


def answer_target_prompts():
    for (letter, name), edits in TARGET_ANSWERS.items():
        path = test_path(letter, name)
        text = path.read_text()
        changed = False
        for old, new in edits:
            if new in text:
                continue
            if old not in text:
                raise SystemExit(f"{path}: the expected text is missing: {old.strip()[:80]}")
            text = text.replace(old, new, 1)
            changed = True
        path.write_text(text)
        print(f"{name}: {'target prompts answered' if changed else 'target prompts already answered'}")


def add_frilled_deathspitter_case():
    path = test_path("f", "FrilledDeathspitterTest")
    text = path.read_text()
    if "controllerChoosesWhichOpponentIsDealtDamageAfterCombatDamage" in text:
        print("FrilledDeathspitterTest: new case already added")
        return
    anchor = "    @Test\n    void combatDamageTriggersEnrage() {"
    if anchor not in text:
        raise SystemExit(f"{path}: the expected text is missing: {anchor.strip()[:80]}")
    text = text.replace(anchor, FRILLED_DEATHSPITTER + "\n" + anchor, 1)
    for name in ("model.ManaPool", "model.PendingInteraction", "model.Player", "testutil.FakeConnection",
                 "model.ManaColor"):
        text = add_import(text, f"import {PKG}.{name};\n")
    text = add_import(text, "import java.util.ArrayList;\n")
    path.write_text(text)
    print("FrilledDeathspitterTest: new case added")


def main():
    answer_target_prompts()
    add_frilled_deathspitter_case()
    for letter, name, method, case, anchor in CASES:
        path = test_path(letter, name)
        text = path.read_text()
        if method in text:
            print(f"{name}: already converted")
            continue
        if anchor not in text:
            raise SystemExit(f"{path}: the expected text is missing: {anchor.strip()[:80]}")
        text = text.replace(anchor, case + anchor, 1)
        text = add_import(text, f"import {PKG}.cards.l.LeylineOfSanctity;\n")
        path.write_text(text)
        print(f"{name}: converted")


if __name__ == "__main__":
    main()
