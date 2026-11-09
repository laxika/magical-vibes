#!/usr/bin/env python3
"""Step 15, run 17z6: the card tests this run adds.

- GlorifierOfSufferingTest#targetsAreChosenWhenTheReflexiveAbilityTriggers: the enters ability goes on the stack with
  no target, and the "when you do" ability's targets are chosen after the sacrifice, among the creatures then on the
  battlefield (CR 603.12, the card's ruling); the reflexive ability is a stack object players can respond to.
- LightPawsEmperorsVoiceTest#boundsTheSearchByTheLastKnownManaValueOfAnAuraThatLeft: "mana value less than or equal to
  that Aura" is read as the ability resolves, from the Aura as it last existed (CR 608.2h), where the collector baked
  it as the ability triggered.
- ProfessorDellianFelTest#emblemCantTargetAnOpponentWithHexproof: the emblem's "target opponent" is a target chosen as
  the ability goes on the stack (CR 603.3d), so an opponent with hexproof can't be chosen and the ability is removed
  (a guard: the marker's entry, its opponent set without a choice, failed its target check as it resolved). It adds
  Leyline of Sanctity to the class's `@CardUsed`.
- ProfessorDellianFelTest#minusSixCreatesLifeGainEmblem and #emblemIsNotNamedProfessorDellianFel choose the emblem
  ability's target opponent as it goes on the stack (the marker chose none).

Idempotent: a test already added is left alone. Optional argument: the repository root (default: this checkout), so
the same tests can be added to a HEAD worktree.

Usage: step15-run17z6-tests.py [<repository root>]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"

ADDITIONS = {
    "g/src/test/java/com/github/laxika/magicalvibes/cards/g/GlorifierOfSufferingTest.java": (
        "targetsAreChosenWhenTheReflexiveAbilityTriggers",
        "    private void castGlorifier() {",
        '''    @Test
    @DisplayName("The counters' targets are chosen when the reflexive ability triggers, after the sacrifice")
    void targetsAreChosenWhenTheReflexiveAbilityTriggers() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DeconstructionHammer());
        harness.castFromHand(player1, new GlorifierOfSuffering(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).singleElement().satisfies(entry -> assertThat(entry.getTargetIds()).isEmpty());

        Permanent latecomer = harness.addToBattlefieldAndReturn(player1, new ArmoredKincaller());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, latecomer.getId());
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(latecomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).singleElement().satisfies(entry ->
                assertThat(entry.getTargetIds()).containsExactly(latecomer.getId()));
        harness.passBothPriorities();

        assertThat(latecomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Deconstruction Hammer");
    }

'''),
    "l/src/test/java/com/github/laxika/magicalvibes/cards/l/LightPawsEmperorsVoiceTest.java": (
        "boundsTheSearchByTheLastKnownManaValueOfAnAuraThatLeft",
        "    private Card aura(String name, String manaCost) {",
        '''    @Test
    void boundsTheSearchByTheLastKnownManaValueOfAnAuraThatLeft() {
        Permanent lightPaws = addCreatureReady(player1, new LightPawsEmperorsVoice());
        Card enteringAura = aura("Entering Aura", "{2}");
        Card eligibleAura = aura("Eligible Aura", "{2}");
        Card sameNameAura = aura("Entering Aura", "{1}");
        Card tooExpensiveAura = aura("Too Expensive Aura", "{3}");

        harness.setHand(player1, List.of(enteringAura));
        harness.setLibrary(player1, List.of(eligibleAura, sameNameAura, tooExpensiveAura));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, lightPaws.getId());
        harness.passBothPriorities();
        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == enteringAura)
                .findFirst().orElseThrow();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, entered);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        // "With mana value less than or equal to that Aura": the Aura as it last existed, {2}. It no longer shares its
        // name with an Aura you control, so the same-name card is eligible now.
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(eligibleAura, sameNameAura);
    }

'''),
    "p/src/test/java/com/github/laxika/magicalvibes/cards/p/ProfessorDellianFelTest.java": (
        "emblemCantTargetAnOpponentWithHexproof",
        "    private Permanent addReadyProfessor(Player player, int loyalty) {",
        '''    @Test
    @DisplayName("The emblem's ability can't target an opponent with hexproof, so that player loses no life")
    void emblemCantTargetAnOpponentWithHexproof() {
        addReadyProfessor(player1, 6);
        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        int opponentLifeBefore = gd.getLife(player2.getId());
        int controllerLifeBefore = gd.getLife(player1.getId());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

'''),
}


TARGET_CHOICES = {
    "p/src/test/java/com/github/laxika/magicalvibes/cards/p/ProfessorDellianFelTest.java": (
        ["minusSixCreatesLifeGainEmblem", "emblemIsNotNamedProfessorDellianFel"],
        """        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
""",
        """        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
"""),
}


def edit_methods(text, methods, old, new):
    """Replaces {@code old} with {@code new} inside each named test method only."""
    for method in methods:
        start = text.index(f"void {method}()")
        end = text.find("    @Test", start)
        end = len(text) if end < 0 else end
        body = text[start:end]
        if new in body:
            continue
        if old not in body:
            raise SystemExit(f"{method}: expected text not found")
        text = text[:start] + body.replace(old, new, 1) + text[end:]
    return text


def main():
    for relative, (name, anchor, test) in ADDITIONS.items():
        path = TESTS / relative
        text = path.read_text()
        if f"void {name}()" in text:
            continue
        if anchor not in text:
            raise SystemExit(f"{relative}: anchor not found")
        text = text.replace(anchor, test + anchor, 1)
        if name == "emblemCantTargetAnOpponentWithHexproof":
            text = text.replace("RunedHalo.class})", "RunedHalo.class, LeylineOfSanctity.class})", 1)
            text = text.replace("import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;\n",
                                "import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;\n"
                                "import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;\n", 1)
        path.write_text(text)
        print(f"added {name}")
    for relative, (methods, old, new) in TARGET_CHOICES.items():
        path = TESTS / relative
        text = path.read_text()
        edited = edit_methods(text, methods, old, new)
        if edited != text:
            path.write_text(edited)
            print(f"edited {', '.join(methods)}")


if __name__ == "__main__":
    main()
