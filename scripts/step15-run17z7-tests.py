#!/usr/bin/env python3
"""Step 15, run 17z7: the card tests of the converted abilities.

- Sanguine Bond, Vito, Thorn of the Dusk Rose, Defiant Bloodlord, Enduring Tenacity: "target opponent loses that much
  life" now chooses its target as the ability goes on the stack (CR 603.3d), where the class-keyed life-gain handler
  took the engine's one opponent without asking. Their tests pass priority and resolve the stack through helpers that
  answer each target prompt with the opponent; Sanguine Bond gains a three-player case (each trigger may target a
  different opponent, its ruling), the rules fix.
- Lynde, Cheerful Tormentor: a Curse that left its graveyard and came back before the ability resolved is a new object
  (CR 400.7), so no return is registered; the collector baked the card's id as the ability triggered and the handler
  registered the return of the new object.
- Emiel the Blessed: "put a +1/+1 counter on it" doesn't target the entering creature, so a creature with shroud gets
  its counter; the enter collector targeted it, and the ability did nothing.

Idempotent; an optional first argument is the repository root (used for the HEAD worktree).

Usage: step15-run17z7-tests.py [<repository root>]
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


def append_members(text, members):
    end = text.rstrip().rindex("}")
    return text[:end].rstrip() + "\n" + members + "}\n"


TARGETING_HELPERS = """
    /** Passes priority, then chooses the opponent as the target of each "target opponent" ability put on the stack. */
    private void passBothPrioritiesTargetingOpponent() {
        harness.passBothPriorities();
        chooseOpponentAsTarget();
    }

    /** {@link #resolveAllTriggers()}, choosing the opponent as the target of each "target opponent" ability. */
    private void resolveAllTriggersTargetingOpponent() {
        chooseOpponentAsTarget();
        while (gd.status != GameStatus.FINISHED && !gd.interaction.isAwaitingInput() && !gd.stack.isEmpty()) {
            passBothPrioritiesTargetingOpponent();
        }
    }

    /** Answers each pending target choice that offers the opponent with the opponent (CR 603.3d). */
    private void chooseOpponentAsTarget() {
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice choice
                && choice.validIds().contains(player2.getId())) {
            harness.handlePermanentChosen(player1, player2.getId());
        }
    }
"""

SANGUINE_MULTIPLAYER = """
    @Test
    @DisplayName("Its controller chooses which opponent loses the life in a multiplayer game")
    void controllerChoosesTheTargetOpponent() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player1, new SanguineBond());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(player2.getId(), player3.getId());
        harness.handlePermanentChosen(player1, player3.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        harness.assertLife(player3, 17);
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

LYNDE_REENTERED = """
    @Test
    @DisplayName("Registers no return for a Curse that left its graveyard and came back before the ability resolved")
    void registersNoReturnForACurseThatReenteredTheGraveyardBeforeResolution() {
        harness.addToBattlefield(player1, new LyndeCheerfulTormentor());
        CurseOfClingingWebs card = new CurseOfClingingWebs();
        Permanent curse = harness.addToBattlefieldAndReturn(player1, card);
        curse.setAttachedTo(player2.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, curse));
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Lynde, Cheerful Tormentor"));

        // The card leaves the graveyard and is put into it again: a new object (CR 400.7).
        gd.markGraveyardEntry(card);
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedReturnCurseAttachedToPlayer.class)).isEmpty();
    }
"""

EMIEL_SHROUD = """
    @Test
    @DisplayName("Puts the counter on an entering creature with shroud, which the ability doesn't target")
    void putsCounterOnEnteringCreatureWithShroud() {
        harness.addToBattlefield(player1, new EmielTheBlessed());
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent enteringCreature = harness.enterBattlefieldAndReturn(player1, new InkwellLeviathan());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(enteringCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
"""

TARGETING_TESTS = {"d": "DefiantBloodlordTest", "e": "EnduringTenacityTest", "s": "SanguineBondTest",
                   "v": "VitoThornOfTheDuskRoseTest"}


def convert_targeting(letter, name):
    path = test_path(letter, name)
    text = path.read_text()
    if "chooseOpponentAsTarget()" in text:
        return False
    text = text.replace("        harness.passBothPriorities();\n", "        passBothPrioritiesTargetingOpponent();\n")
    text = text.replace("        resolveAllTriggers();\n", "        resolveAllTriggersTargetingOpponent();\n")
    members = TARGETING_HELPERS
    if name == "SanguineBondTest":
        members = SANGUINE_MULTIPLAYER + members
        for line in [f"import {PKG}.model.ManaPool;\n", f"import {PKG}.model.Player;\n",
                     f"import {PKG}.testutil.FakeConnection;\n", "import java.util.ArrayList;\n",
                     "import java.util.UUID;\n"]:
            text = add_import(text, line)
    text = append_members(text, members)
    # The multiplayer case answers its own prompt; the helpers serve the other cases.
    text = text.replace("""        harness.castCreature(player1, 0);
        passBothPrioritiesTargetingOpponent();
        passBothPrioritiesTargetingOpponent();

        PendingInteraction.PermanentChoice choice""", """        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice""")
    for line in [f"import {PKG}.model.GameStatus;\n", f"import {PKG}.model.PendingInteraction;\n"]:
        text = add_import(text, line)
    path.write_text(text)
    return True


def add_case(letter, name, marker, case, imports):
    path = test_path(letter, name)
    text = path.read_text()
    if marker in text:
        return False
    text = append_members(text, case)
    for line in imports:
        text = add_import(text, line)
    path.write_text(text)
    return True


def main():
    for letter, name in sorted(TARGETING_TESTS.items()):
        print(f"{name}: {'converted' if convert_targeting(letter, name) else 'already converted'}")
    print("LyndeCheerfulTormentorTest:", "added" if add_case(
        "l", "LyndeCheerfulTormentorTest", "registersNoReturnForACurseThatReenteredTheGraveyardBeforeResolution",
        LYNDE_REENTERED, []) else "already added")
    print("EmielTheBlessedTest:", "added" if add_case(
        "e", "EmielTheBlessedTest", "putsCounterOnEnteringCreatureWithShroud", EMIEL_SHROUD,
        [f"import {PKG}.cards.i.InkwellLeviathan;\n"]) else "already added")
    emiel = test_path("e", "EmielTheBlessedTest")
    text = emiel.read_text()
    if "InkwellLeviathan.class" not in text:
        text = text.replace("@CardUsed({EmielTheBlessed.class, ", "@CardUsed({EmielTheBlessed.class, InkwellLeviathan.class, ")
        emiel.write_text(text)


if __name__ == "__main__":
    main()
