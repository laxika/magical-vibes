#!/usr/bin/env python3
"""Card test changes of run 17z17d (effect-slot-refactor.md step 15).

A delayed triggered ability is created by the resolution of a spell or ability (CR 603.7a), so two resolutions of
one source create two delayed abilities, each a stack object of its own. The tests added here resolve two
resolutions of one source through the engine and count the stack objects:

* MoltenEchoesTest: two creatures of the chosen type enter, two tokens, two "exile it at the beginning of the next
  end step" abilities.
* CorpseDanceTest: the same Corpse Dance cast twice with buyback, two delayed exiles.
* TawnossCoffinTest: two activations that both resolve while the Coffin is tapped, two "when this artifact ...
  becomes untapped, return that exiled card" abilities.
* FlameholdGrapplerTest: two Grapplers' "copy the next spell you cast this turn" are two abilities.
* MagusLuceaKaneTest: two activations' "when you next cast a spell with {X} ... or activate an ability with {X}"
  are two abilities, for the spell and for the ability.
* SunkenPalaceTest: two Palaces' mana spent on one spell is two "copy that spell" abilities.

Eight card tests compared a queued DelayedPermanentAction with a freshly built one by equals; a queued action now
carries the resolution that scheduled it, so the expected one is built as scheduled by the resolution that just
happened (STAMPED: `.scheduledBy(gd.resolutionOrdinal)` after the constructor call).

    scripts/step15-run17z17d-tests.py [LETTER...]

Idempotent; fails if an edit does not apply.
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TESTS = "magical-vibes-card-test/{0}/src/test/java/com/github/laxika/magicalvibes/cards/{0}/{1}.java"

# letter -> [(test class, imports to add, @CardUsed classes to add, exact replacements, methods to append)]
EDITS = {
    "c": [("CorpseDanceTest",
           ["com.github.laxika.magicalvibes.model.StackEntryType"],
           [],
           [],
           '''
    @Test
    @DisplayName("Cast twice with buyback, each resolution's delayed exile is its own ability")
    void eachResolutionsDelayedExileIsItsOwnAbility() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new SerraAngel()));
        harness.setHand(player1, List.of(new CorpseDance()));
        harness.addMana(player1, ManaColor.BLACK, 10);

        harness.castInstantWithBuyback(player1, 0, null);
        harness.passBothPriorities();
        harness.castInstantWithBuyback(player1, 0, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Serra Angel");
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Serra Angel", "Grizzly Bears");
    }
''')],
    "f": [("FlameholdGrapplerTest",
           ["com.github.laxika.magicalvibes.model.StackEntryType"],
           [],
           [],
           '''
    @Test
    @DisplayName("Two Grapplers' delayed copies of the next spell are two abilities")
    void eachGrapplersDelayedCopyIsItsOwnAbility() {
        harness.castFromHand(player1, new FlameholdGrappler(), "{U}{R}{W}");
        resolveAllTriggers();
        harness.castFromHand(player1, new FlameholdGrappler(), "{U}{R}{W}");
        resolveAllTriggers();

        harness.castFromHand(player1, new KrumarInitiate(), "{1}{B}");

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(2);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> "Krumar Initiate".equals(p.getCard().getName())).hasSize(3);
    }
''')],
    "m": [("MoltenEchoesTest",
           ["org.junit.jupiter.api.DisplayName"],
           [],
           [],
           '''
    @Test
    @DisplayName("Two tokens made by two resolutions are exiled by two delayed abilities")
    void eachResolutionsDelayedExileIsItsOwnAbility() {
        castEchoes(CardSubtype.BEAR);
        castCreature(new GrizzlyBears(), "{1}{G}");
        castCreature(new GrizzlyBears(), "{1}{G}");
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(p -> p.getCard().isToken()).hasSize(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(p -> p.getCard().isToken()).isEmpty();
    }
'''),
          ("MagusLuceaKaneTest",
           ["com.github.laxika.magicalvibes.model.StackEntryType"],
           [],
           [],
           '''
    @Test
    @DisplayName("Two activations' delayed copies of the next X ability are two abilities")
    void eachActivationsDelayedCopyIsItsOwnAbility() {
        Permanent magus = addCreatureReady(player1, new MagusLuceaKane());
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        magus.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 2, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(2);
    }
'''),
          ("MagusLuceaKaneTest",
           [],
           [],
           [],
           '''
    @Test
    @DisplayName("Two activations' delayed copies of the next X spell are two abilities")
    void eachActivationsDelayedSpellCopyIsItsOwnAbility() {
        Permanent magus = addCreatureReady(player1, new MagusLuceaKane());
        harness.setHand(player1, List.of(new DevilsPlay()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        magus.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.castSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(2);
    }
''')],
    "s": [("SunkenPalaceTest",
           ["com.github.laxika.magicalvibes.model.StackEntryType"],
           [],
           [],
           '''
    @Test
    void twoPalacesManaSpentOnOneSpellIsTwoCopyAbilities() {
        harness.addToBattlefield(player1, new SunkenPalace());
        harness.addToBattlefield(player1, new SunkenPalace());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setGraveyard(player1, graveyardCards());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.setGraveyard(player1, graveyardCards());
        harness.activateAbility(player1, 1, 1, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new AuguryRaven()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(2);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Augury Raven")).isEqualTo(3);
    }
''')],
    "t": [("TawnossCoffinTest",
           [],
           [],
           [],
           '''
    @Test
    @DisplayName("Each activation's delayed return is its own ability when the Coffin untaps")
    void eachActivationsReturnIsItsOwnAbility() {
        Permanent coffin = addReadyCoffin();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, battlefieldIndex(coffin), null, first.getId());
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, coffin.getId());
        harness.handleMayAbilityChosen(player1, true);
        if (gd.stack.size() > 1) {
            harness.passBothPriorities();
        }
        harness.activateAbility(player1, battlefieldIndex(coffin), null, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanent(player2, first.getCard().getId())).isNull();
        assertThat(findPermanent(player2, second.getCard().getId())).isNull();

        untapStepWithMayPrompt(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(findByCardId(player2, first.getCard().getId()).isTapped()).isTrue();
        assertThat(findByCardId(player2, second.getCard().getId()).isTapped()).isTrue();
    }
''')],
}

# letter -> test classes whose expected DelayedPermanentAction is stamped with the resolution that just happened.
STAMPED = {
    "f": ["FableOfTheMirrorBreakerTest", "FelhideSpiritbinderTest", "FlamerushRiderTest"],
    "k": ["KikiJikiMirrorBreakerTest"],
    "o": ["OrthionHeroOfLavabrinkTest"],
    "s": ["SwiftWarkiteTest"],
    "t": ["TempestraDameOfGamesTest", "TreacherousUrgeTest"],
}
STAMP = ".scheduledBy(gd.resolutionOrdinal)"


def stamp(text):
    """The text with STAMP after every `new DelayedPermanentAction(...)` call that has none."""
    marker = "new DelayedPermanentAction("
    at = 0
    while True:
        start = text.find(marker, at)
        if start < 0:
            return text
        depth = 0
        end = start + len(marker) - 1
        for end in range(start + len(marker) - 1, len(text)):
            if text[end] == "(":
                depth += 1
            elif text[end] == ")":
                depth -= 1
                if depth == 0:
                    break
        if not text.startswith(STAMP, end + 1):
            text = text[:end + 1] + STAMP + text[end + 1:]
        at = end + 1


def apply(text, name, imports, card_used, replacements, methods):
    """The edited text and whether anything changed; raises ValueError when an edit does not apply."""
    original = text
    for old, new in replacements:
        if new in text:
            continue
        if text.count(old) != 1:
            raise ValueError("replacement does not apply: " + old.strip().splitlines()[0])
        text = text.replace(old, new)
    first_method = methods.strip().split("void ")[1].split("(")[0]
    if f"void {first_method}(" not in text:
        end = text.rstrip().rfind("}")
        text = text[:end].rstrip("\n") + "\n" + methods + "}\n"
    for qualified in imports:
        line = f"import {qualified};\n"
        if line not in text:
            anchor = "import static " if qualified.startswith("java.") else "import com.github.laxika"
            at = text.index(anchor)
            text = text[:at] + line + ("\n" if qualified.startswith("java.") else "") + text[at:]
    start = text.index("@CardUsed({")
    close = text.index("})", start)
    listed = text[start:close]
    for card in card_used:
        if f"{card}.class" not in listed:
            listed += f", {card}.class"
    text = text[:start] + listed + text[close:]
    if f"class {name} " not in text:
        raise ValueError("not the expected test class")
    return text, text != original


def main():
    letters = sys.argv[1:] or sorted(set(EDITS) | set(STAMPED))
    failed = False
    for letter in letters:
        for name in STAMPED.get(letter, []):
            relative = TESTS.format(letter, name)
            path = ROOT / relative
            text = path.read_text(encoding="utf-8")
            stamped = stamp(text)
            if "new DelayedPermanentAction(" not in text:
                print(f"FAILED (no DelayedPermanentAction): {relative}")
                failed = True
            elif stamped != text:
                path.write_text(stamped, encoding="utf-8")
                print(f"changed: {relative}")
            else:
                print(f"already done: {relative}")
        for name, imports, card_used, replacements, methods in EDITS.get(letter, []):
            relative = TESTS.format(letter, name)
            path = ROOT / relative
            try:
                text, changed = apply(path.read_text(encoding="utf-8"), name, imports, card_used, replacements,
                                      methods)
            except ValueError as error:
                print(f"FAILED ({error}): {relative}")
                failed = True
                continue
            if changed:
                path.write_text(text, encoding="utf-8")
                print(f"changed: {relative}")
            else:
                print(f"already done: {relative}")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
