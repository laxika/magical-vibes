#!/usr/bin/env python3
"""Step 15, run 17z3: card tests for Aradesh, the Founder, whose ability is now an attack trigger with the intervening
"if" "if it enlisted a creature this combat", and for the abilities that read the triggering permanent's mana value as
they resolve.

- `AradeshTheFounderTest#isshinMakesTheAbilityTriggerAnAdditionalTime` (a rules fix: the ability triggers on a creature
  attacking, so Isshin, Two Heavens as One makes it trigger an additional time; it rode the enlist trigger, which
  Isshin doubled, but the ability itself triggered once); the guards
  `#delneyDoublesTheAbilityForAnAttackerWithGreaterPower` (Delney, Streetwise Lookout judges Aradesh's power, not the
  attacker's) and `#abilityOrderedBeforeTheEnlistBoostSeesTheUnboostedPower` (the controller orders the ability and
  the enlist trigger, CR 603.3b). The class's other enlisting cases order Aradesh's ability below the enlist boost
  (`stackAradeshsAbilityBelowTheEnlistBoost`), the order their power checks were written for, since the harness's
  automatic order now resolves the attack trigger first;
- `DelneyStreetwiseLookoutTest#additionalTriggersCanBeOrderedOneByOne` (a rules fix: each additional time an ability
  triggers is a stack object of its own, CR 603.3, so a controller asked for the order, CR 603.3b, can order them; the
  copies shared one identity and the order prompt failed on it);
- the guards `BrainstealerDragonTest#ownerLosesLastKnownManaValueWhenThePermanentLeftFirst` ("they lose life equal to
  its mana value" reads the permanent as it last existed, CR 608.2h),
  `KodamaOfTheEastTreeTest#boundsByTheLastKnownManaValueOfAPermanentThatLeft`,
  `JunkyardScrapperTest#seeksBelowTheLastKnownManaValueOfAnArtifactThatLeft` and
  `ArniMetalbrowTest#attackerThatLeftBoundsTheChoiceByItsLastKnownManaValue` (the mana value was baked into the entry
  as the ability triggered; it is read as it resolves, from the departure snapshot).

Idempotent: a test already present is left alone. The optional argument is the repository root (default: this
script's), so the cases can be added to a HEAD worktree.

Usage: step15-run17z3-tests.py [<repository root>]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = "magical-vibes-card-test/{letter}/src/test/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"
CARDS = "com.github.laxika.magicalvibes.cards."
MODEL = "com.github.laxika.magicalvibes.model."

ENLIST_CHOSEN = """        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
"""

ARADESH_EDITS = [
    ("""class AradeshTheFounderTest extends BaseCardTest {
""", """class AradeshTheFounderTest extends BaseCardTest {

    @BeforeEach
    void askForTheTriggerOrder() {
        harness.setAutoOrderTriggers(player1, false);
    }
"""),
    (ENLIST_CHOSEN, ENLIST_CHOSEN + """        stackAradeshsAbilityBelowTheEnlistBoost();
"""),
]

ARADESH_HELPER = ("stackAradeshsAbilityBelowTheEnlistBoost", """
    /**
     * CR 603.3b: the controller orders the abilities that triggered together. Puts Aradesh's ability on the stack
     * first, so the enlist boost resolves before it.
     */
    private void stackAradeshsAbilityBelowTheEnlistBoost() {
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.ColorChoice order
                && order.context() instanceof ChoiceContext.TriggerOrder) {
            harness.handleListChoice(player1, order.options().stream()
                    .filter(option -> !option.contains("enlist"))
                    .findFirst().orElse(order.options().getFirst()));
        }
    }
""")

CASES = {
    ("a", "AradeshTheFounderTest"): (
        [CARDS + "d.DelneyStreetwiseLookout", CARDS + "i.IsshinTwoHeavensAsOne", CARDS + "l.LinebreakerBaloth",
         MODEL + "ChoiceContext",
         "org.junit.jupiter.api.BeforeEach", "org.junit.jupiter.api.DisplayName"],
        ["DelneyStreetwiseLookout", "IsshinTwoHeavensAsOne", "LinebreakerBaloth"],
        [("delneyDoublesTheAbilityForAnAttackerWithGreaterPower", """
    @Test
    @DisplayName("Delney makes the ability trigger an additional time: it is Aradesh's ability, so Aradesh's power counts")
    void delneyDoublesTheAbilityForAnAttackerWithGreaterPower() {
        harness.setAutoOrderTriggers(player1, true);
        addCreatureReady(player1, new AradeshTheFounder());
        addCreatureReady(player1, new DelneyStreetwiseLookout());
        Permanent baloth = addCreatureReady(player1, new LinebreakerBaloth());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(baloth)));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, baloth, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
    }
"""), ("isshinMakesTheAbilityTriggerAnAdditionalTime", """
    @Test
    @DisplayName("Isshin makes the ability trigger an additional time: a creature attacking triggers it")
    void isshinMakesTheAbilityTriggerAnAdditionalTime() {
        harness.setAutoOrderTriggers(player1, true);
        addCreatureReady(player1, new AradeshTheFounder());
        addCreatureReady(player1, new IsshinTwoHeavensAsOne());
        Permanent baloth = addCreatureReady(player1, new LinebreakerBaloth());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(baloth)));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, baloth, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
    }
"""), ("abilityOrderedBeforeTheEnlistBoostSeesTheUnboostedPower", """
    @Test
    @DisplayName("Its controller may order the ability to resolve before the enlist boost; it then sees the lower power")
    void abilityOrderedBeforeTheEnlistBoostSeesTheUnboostedPower() {
        Permanent aradesh = addCreatureReady(player1, new AradeshTheFounder());
        Permanent supporter = addCreatureReady(player1, new CentaurCourser());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(aradesh)));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.chooseTriggerToStackNext(player1, "enlist");
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, aradesh, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(aradesh.getPowerModifier()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }
"""), ARADESH_HELPER]),
    ("d", "DelneyStreetwiseLookoutTest"): (
        [MODEL + "ChoiceContext", MODEL + "PendingInteraction", MODEL + "StackEntry"], [],
        [("additionalTriggersCanBeOrderedOneByOne", """
    @Test
    @DisplayName("Each additional trigger is its own stack object, so a controller asked for the order can order them")
    void additionalTriggersCanBeOrderedOneByOne() {
        harness.setAutoOrderTriggers(player1, false);
        addCreatureReady(player1, new DelneyStreetwiseLookout());
        addCreatureReady(player1, new MentorOfTheMeek());
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        while (gd.interaction.activeInteraction() instanceof PendingInteraction.ColorChoice order
                && order.context() instanceof ChoiceContext.TriggerOrder) {
            harness.handleListChoice(player1, order.options().getFirst());
        }

        assertThat(gd.stack).hasSize(4);
        assertThat(gd.stack).extracting(StackEntry::getTargetableId).doesNotHaveDuplicates();
    }
""")]),
    ("b", "BrainstealerDragonTest"): (
        [], [],
        [("ownerLosesLastKnownManaValueWhenThePermanentLeftFirst", """
    @Test
    @DisplayName("A permanent that left before the ability resolves still costs its owner its last-known mana value")
    void ownerLosesLastKnownManaValueWhenThePermanentLeftFirst() {
        harness.addToBattlefield(player1, new BrainstealerDragon());
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());

        Permanent bears = harness.enterBattlefieldAndReturn(player1, creature);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }
""")]),
    ("k", "KodamaOfTheEastTreeTest"): (
        [MODEL + "Permanent"], [],
        [("boundsByTheLastKnownManaValueOfAPermanentThatLeft", """
    @Test
    @DisplayName("A permanent that left before the ability resolves bounds the choice by its last-known mana value")
    void boundsByTheLastKnownManaValueOfAPermanentThatLeft() {
        harness.addToBattlefield(player1, new KodamaOfTheEastTree());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        Permanent giant = harness.enterBattlefieldAndReturn(player1, new HillGiant());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, giant);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandCardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);
    }
""")]),
    ("j", "JunkyardScrapperTest"): (
        [], [],
        [("seeksBelowTheLastKnownManaValueOfAnArtifactThatLeft", """
    @Test
    void seeksBelowTheLastKnownManaValueOfAnArtifactThatLeft() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new JunkyardScrapper());
        Card candidate = new FountainOfYouth();
        harness.setLibrary(player1, List.of(candidate));

        Permanent sphere = harness.enterBattlefieldAndReturn(player1, new CommandersSphere());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, sphere);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(candidate);
    }
""")]),
    ("a", "ArniMetalbrowTest"): (
        [], [],
        [("attackerThatLeftBoundsTheChoiceByItsLastKnownManaValue", """
    @Test
    @DisplayName("An attacker that left before the ability resolves bounds the choice by its last-known mana value")
    void attackerThatLeftBoundsTheChoiceByItsLastKnownManaValue() {
        addCreatureReady(player1, new ArniMetalbrow());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(List.of(1));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, giant);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
""")]),
}


def add_import(text, fqcn):
    line = f"import {fqcn};"
    if line in text:
        return text
    imports = list(re.finditer(r"^import (?!static)[^;]+;$", text, re.M))
    ours = [m for m in imports if not m.group(0).startswith("import java")]
    after = next((m for m in ours if m.group(0) > line), None)
    if after is not None:
        return text[:after.start()] + line + "\n" + text[after.start():]
    last = ours[-1]
    return text[:last.end()] + "\n" + line + text[last.end():]


def add_card_used(text, simple):
    match = re.search(r"@CardUsed\(\{(.*?)\}\)", text, re.S)
    if re.search(r"\b" + simple + r"\.class\b", match.group(1)):
        return text
    body = match.group(1).rstrip()
    return text[:match.start(1)] + body + ", " + simple + ".class" + text[match.end(1):]


def main():
    for (letter, name), (imports, card_used, methods) in CASES.items():
        path = ROOT / TESTS.format(letter=letter, name=name)
        text = path.read_text()
        original = text
        if name == "AradeshTheFounderTest" and "stackAradeshsAbilityBelowTheEnlistBoost" not in text:
            for old, new in ARADESH_EDITS:
                if old not in text:
                    raise SystemExit(f"{name}: expected text not found:\n{old}")
                text = text.replace(old, new)
        for method, source in methods:
            if re.search(r"\bvoid " + method + r"\(", text):
                continue
            end = text.rstrip().rfind("}")
            text = text[:end].rstrip() + "\n" + source + "}\n"
        if text == original:
            continue
        for fqcn in imports:
            text = add_import(text, fqcn)
        for simple in card_used:
            text = add_card_used(text, simple)
        path.write_text(text)
        print(f"updated {name}")


if __name__ == "__main__":
    main()
