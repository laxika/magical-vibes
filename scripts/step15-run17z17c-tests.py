#!/usr/bin/env python3
"""Card test changes of run 17z17c (effect-slot-refactor.md step 15).

The activation collector runs once the activated ability is on the stack and its costs are paid (CR 602.2b,
CR 601.2i), for every activation ability. The tests added or rewritten here resolve each case through the engine:

* ArtifactPossessionTest: three tests activated an ability whose cost sacrifices the enchanted artifact and expected
  the Aura to deal damage. As the ability becomes activated the Aura is attached to nothing (the Illusionist's
  Bracers ruling of 2013-01-24: "At the time the ability is considered activated (after all costs are paid),
  Illusionist's Bracers is no longer equipped to that creature"), so "an ability of enchanted artifact" doesn't
  match. The two positive tests activate Clay Statue's "{2}: Regenerate", a mana one Ashnod's Altar's, and two new
  tests assert that the sacrificed artifact's ability deals no damage.
* BurningTreeShamanTest: a ninjutsu ability activated from a hand triggers it (CR 702.49a: ninjutsu is an activated
  ability); discarding Circling Vultures does not (CR 116.2e: a special action).
* WizenedMentorTest, PowerleechTest: an ability of a card in a hand is not an ability of a permanent or of an
  artifact (CR 109.2).

    scripts/step15-run17z17c-tests.py [LETTER...]

Idempotent; fails if an edit does not apply.
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TESTS = "magical-vibes-card-test/{0}/src/test/java/com/github/laxika/magicalvibes/cards/{0}/{1}.java"

ARTIFACT_POSSESSION_REPLACEMENTS = [
    ('''    void controllerActivatingNonTapAbilityDealsDamage() {
        attachAuraTo(player1, player1, new AetherSpellbomb());
        gd.playerDecks.get(player1.getId()).add(new Ornithopter());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();
''', '''    void controllerActivatingNonTapAbilityDealsDamage() {
        attachAuraTo(player1, player1, new ClayStatue());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
'''),
    ('''    void activatingNonTapManaAbilityDealsDamage() {
        attachAuraTo(player1, player2, new DromarsAttendant());
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();
''', '''    void activatingNonTapManaAbilityDealsDamage() {
        attachAuraTo(player1, player2, new AshnodsAltar());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        harness.handlePermanentChosen(player2, sacrificed.getId());
        resolveAllTriggers();
'''),
    ('''    void opponentActivatingNonTapAbilityDealsDamageToArtifactController() {
        attachAuraTo(player1, player2, new AetherSpellbomb());
        gd.playerDecks.get(player2.getId()).add(new Ornithopter());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        resolveAllTriggers();
''', '''    void opponentActivatingNonTapAbilityDealsDamageToArtifactController() {
        attachAuraTo(player1, player2, new ClayStatue());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();
'''),
]

# letter -> [(test class, imports to add, @CardUsed classes to add, exact replacements, methods to append)]
EDITS = {
    "a": [("ArtifactPossessionTest",
           ["com.github.laxika.magicalvibes.cards.a.AshnodsAltar"],
           ["AshnodsAltar"],
           ARTIFACT_POSSESSION_REPLACEMENTS,
           '''
    @Test
    @DisplayName("An ability whose cost sacrifices the enchanted artifact deals no damage: the Aura is attached to nothing")
    void sacrificingTheEnchantedArtifactAsACostDoesNotTrigger() {
        attachAuraTo(player1, player2, new AetherSpellbomb());
        gd.playerDecks.get(player2.getId()).add(new Ornithopter());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A mana ability whose cost sacrifices the enchanted artifact deals no damage")
    void sacrificingTheEnchantedArtifactForManaDoesNotTrigger() {
        attachAuraTo(player1, player2, new DromarsAttendant());
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
''')],
    "b": [("BurningTreeShamanTest",
           ["com.github.laxika.magicalvibes.cards.c.CirclingVultures",
            "com.github.laxika.magicalvibes.cards.g.GrizzlyBears",
            "com.github.laxika.magicalvibes.cards.n.NinjaOfTheDeepHours",
            "com.github.laxika.magicalvibes.model.TurnStep"],
           ["CirclingVultures", "GrizzlyBears", "NinjaOfTheDeepHours"],
           [],
           '''
    @Test
    @DisplayName("A ninjutsu ability activated from a hand triggers it")
    void ninjutsuActivationTriggers() {
        addCreatureReady(player2, new BurningTreeShaman());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player2, List.of()));
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NinjaOfTheDeepHours()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Discarding Circling Vultures is a special action, not an activated ability")
    void specialActionDiscardDoesNotTrigger() {
        addCreatureReady(player2, new BurningTreeShaman());
        harness.setHand(player1, List.of(new CirclingVultures()));
        harness.setLife(player1, 20);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }
''')],
    "p": [("PowerleechTest",
           ["com.github.laxika.magicalvibes.cards.z.ZagothCrystal",
            "java.util.List"],
           ["ZagothCrystal"],
           [],
           '''
    @Test
    @DisplayName("Cycling an artifact card is not an artifact's ability: a card in a hand is not an artifact")
    void cyclingAnArtifactCardDoesNotTrigger() {
        harness.addToBattlefield(player1, new Powerleech());
        harness.setHand(player2, List.of(new ZagothCrystal()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }
''')],
    "w": [("WizenedMentorTest",
           ["com.github.laxika.magicalvibes.cards.z.ZagothCrystal",
            "com.github.laxika.magicalvibes.model.ManaColor",
            "java.util.List"],
           ["ZagothCrystal"],
           [],
           '''
    @Test
    @DisplayName("An ability of a card in a hand is not an ability of a permanent")
    void cyclingDoesNotTrigger() {
        addCreatureReady(player1, new WizenedMentor());
        harness.setHand(player2, List.of(new ZagothCrystal()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Zombie");
    }
''')],
}


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
    letters = sys.argv[1:] or sorted(EDITS)
    failed = False
    for letter in letters:
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
