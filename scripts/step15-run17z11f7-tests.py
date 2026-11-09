#!/usr/bin/env python3
"""Step 15, run 17z11f7: card tests divide through the real announcement instead of staging the ETB buffer.

A divided triggered ability announces its targets and division as it is put on the stack (CR 601.2d, 603.3d), so the
engine no longer takes a division staged in `GameData.pendingETBDamageAssignments` before the ability triggered. Each
test that staged one declares it with the harness (`harness.declareDivision(...)`), which answers the ability's target
and division prompts once they open; tests of a resolution-time distribution answer its prompts themselves.

Idempotent; run per letter module. Usage: step15-run17z11f7-tests.py [repository-root]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
STAGE = re.compile(r"gd\.pendingETBDamageAssignments = ([^;]+);")

# Per-file edits applied before the generic one: (old, new) pairs, each expected once unless already applied.
SPECIAL = {
    # Wurmskin Forger's helper chose the staged keys as the ETB targets; it reads the declared division now.
    "WurmskinForgerTest.java": [
        ("""        var targets = java.util.List.copyOf(gd.pendingETBDamageAssignments.keySet());""",
         """        var targets = java.util.List.copyOf(division.keySet());"""),
        ("""    private void chooseAssignments() {""",
         """    private void divide(Map<UUID, Integer> declared) {
        division = declared;
        harness.declareDivision(declared);
    }

    private void chooseAssignments() {"""),
        ("""import java.util.Map;
""", """import java.util.Map;
import java.util.UUID;
"""),
        ("""class WurmskinForgerTest extends BaseCardTest {
""",
         """class WurmskinForgerTest extends BaseCardTest {

    private Map<UUID, Integer> division = Map.of();
"""),
    ],
}

COPY_CAST = ("""        gd.pendingETBDamageAssignments = Map.of(player1.getId(), 2, player2.getId(), 3);
        harness.castCreature(player1, 0);""",
             """        gd.pendingETBDamageAssignments = Map.of(player1.getId(), 2, player2.getId(), 3);
        harness.castCreature(player1, 0, List.of(player1.getId(), player2.getId()));""")
# Bogardan Hellkite's "any number of targets" is a declared target group: its tests choose the declared division's
# targets as the Hellkite's ETB targets, and choose none when they declare none.
SPECIAL["BogardanHellkiteTest.java"] = [
    ("""import java.util.List;
import java.util.Map;
""", """import java.util.List;
import java.util.Map;
import java.util.UUID;
"""),
    ("""class BogardanHellkiteTest extends BaseCardTest {
""", """class BogardanHellkiteTest extends BaseCardTest {

    private Map<UUID, Integer> division;
"""),
    ("""    private void castBogardanHellkite() {
        harness.castFromHand(player1, new BogardanHellkite(), "{6}{R}{R}");
    }""", """    private void divide(Map<UUID, Integer> declared) {
        division = declared;
        harness.declareDivision(declared);
    }

    private void castBogardanHellkite() {
        if (division == null) {
            harness.castFromHand(player1, new BogardanHellkite(), "{6}{R}{R}");
            return;
        }
        harness.setHand(player1, List.of(new BogardanHellkite()));
        harness.addMana(player1, ManaColor.RED, 8);
        harness.castCreature(player1, 0, List.copyOf(division.keySet()));
    }"""),
]
# The rules fix: "any number of targets" admits planeswalkers, which the target group without a filter didn't offer.
SPECIAL["BogardanHellkiteTest.java"].append((
    """        @Test
        @DisplayName("Damage assigned to a removed target is lost rather than redistributed")""",
    """        @Test
        @DisplayName("ETB can target a planeswalker chosen from its target prompt")
        void etbTargetPromptOffersPlaneswalkers() {
            Permanent garruk = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
            garruk.setCounterCount(CounterType.LOYALTY, 3);

            castBogardanHellkite();
            harness.passBothPriorities();
            harness.handleMultiplePermanentsChosen(player1, List.of(garruk.getId(), player2.getId()));
            harness.handleListChoice(player1, "2");
            harness.handleListChoice(player1, "3");
            harness.passBothPriorities();

            assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
            harness.assertLife(player2, 17);
        }

        @Test
        @DisplayName("Damage assigned to a removed target is lost rather than redistributed")"""))
# The copied Bogardan Hellkite trigger chooses the declared division's targets as its ETB targets.
SPECIAL["StrionicResonatorTest.java"] = [COPY_CAST]
SPECIAL["MisterFantasticTest.java"] = [COPY_CAST]
SPECIAL["AdricMathematicalGeniusTest.java"] = [COPY_CAST]
# "None was supplied" no longer needs clearing a staged division: none is ever staged.
SPECIAL["AngelOfSalvationTest.java"] = [
    ("""    void etbAsksForTargetsAndDivisionWhenNoneWasSupplied() {
        gd.pendingETBDamageAssignments = Map.of();

""", """    void etbAsksForTargetsAndDivisionWhenNoneWasSupplied() {
"""),
]
# Sarkhan Unbroken entered with no loyalty and died before the cast, so it was never a legal ETB target; the staged
# division hid that.
SPECIAL["DragonlordAtarkaTest.java"] = [
    ("""        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new SarkhanUnbroken());
""", """        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new SarkhanUnbroken());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
"""),
]
# A single recipient takes the whole distribution, so a wrong total is only possible with several: the division
# prompt rejects an amount the total doesn't allow.
SPECIAL["JuganTheRisingStarTest.java"] = [
    ("""        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new DeathcurseOgre());
        gd.pendingETBDamageAssignments = Map.of(recipient.getId(), 6);
""", """        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new DeathcurseOgre());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new DeathcurseOgre());
        gd.pendingETBDamageAssignments = Map.of(recipient.getId(), 6, other.getId(), 1);
"""),
    ("""        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new DeathcurseOgre());
        gd.pendingETBDamageAssignments = Map.of(recipient.getId(), 4);
""", """        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new DeathcurseOgre());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new DeathcurseOgre());
        gd.pendingETBDamageAssignments = Map.of(recipient.getId(), 1, other.getId(), 3);
"""),
]
# Vastwood Hydra's distribution isn't targeted, so it is made as the ability resolves, one creature at a time in
# battlefield order: the tests answer those prompts instead of staging the handler's scratch.
SPECIAL["VastwoodHydraTest.java"] = [
    ("""        gd.pendingETBDamageAssignments = Map.of(bears.getId(), 2, giant.getId(), 1);

        killHydra(hydra);

        harness.passBothPriorities(); // death trigger resolves → "you may" prompt
        harness.handleMayAbilityChosen(player1, true);
""", """        killHydra(hydra);

        harness.passBothPriorities(); // death trigger resolves → "you may" prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 2);
        harness.handleXValueChosen(player1, 1);
"""),
    ("""        gd.pendingETBDamageAssignments = Map.of(bears.getId(), 3);

        killHydra(hydra);
""", """        killHydra(hydra);
"""),
    ("""        gd.pendingETBDamageAssignments = Map.of(ownBears.getId(), 3);

        killHydra(hydra);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
""", """        killHydra(hydra);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 3);
"""),
    ("""        gd.pendingETBDamageAssignments = Map.of(creature.getId(), 2);

        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hydra);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
""", """        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hydra);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 2);
"""),
]

# The rules fix: the ability's controller chooses its targets and divides the damage as it is put on the stack (CR
# 601.2d, 603.3d); at the baseline no production code asked, so the ability dealt no damage.
SPECIAL["KuldothaFlamefiendTest.java"] = [
    ("""    @Test
    void multipleArtifacts_playerChoosesWhichToSacrifice() {""", """    @Test
    void targetsAndDivisionAreAnnouncedAsTheAbilityGoesOnTheStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KuldothaFlamefiend()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId(), player2.getId()));
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "2");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getDamageAssignments())
                .containsExactlyInAnyOrderEntriesOf(Map.of(bears.getId(), 2, player2.getId(), 2));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    void multipleArtifacts_playerChoosesWhichToSacrifice() {"""),
]

# The call a file's staged divisions become (default: the harness declaration).
STAGE_CALL = {"WurmskinForgerTest.java": "divide", "BogardanHellkiteTest.java": "divide"}


def convert(path: pathlib.Path) -> bool:
    text = path.read_text(encoding="utf-8")
    original = text
    for old, new in SPECIAL.get(path.name, []):
        applied = new in text if old in new else old not in text
        if applied:
            continue
        if text.count(old) != 1:
            raise SystemExit(f"{path.name}: expected one occurrence of {old[:60]!r}, found {text.count(old)}")
        text = text.replace(old, new)
    call = STAGE_CALL.get(path.name, "harness.declareDivision")
    text = STAGE.sub(lambda m: f"{call}({m.group(1)});", text)
    if "pendingETBDamageAssignments" in text:
        raise SystemExit(f"{path.name}: still stages pendingETBDamageAssignments")
    if text != original:
        path.write_text(text, encoding="utf-8")
        return True
    return False


def main() -> None:
    changed = []
    for module in sorted(p for p in TESTS.iterdir() if (p / "src").is_dir()):
        for path in sorted((module / "src/test/java").rglob("*Test.java")):
            text = path.read_text(encoding="utf-8")
            if "pendingETBDamageAssignments" in text or path.name in SPECIAL and any(
                    old in text for old, _ in SPECIAL[path.name]):
                if convert(path):
                    changed.append(path.name)
    print(f"{len(changed)} test classes converted")
    for name in changed:
        print(" ", name)


if __name__ == "__main__":
    main()
