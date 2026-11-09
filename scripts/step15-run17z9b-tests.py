#!/usr/bin/env python3
"""Step 15, run 17z9b: card tests for the abilities that read their options and the stack entry.

- Duelists' Convocation International: its intervening "if" (CR 603.4) is the ability's option, judged as the spell is
  cast or the land played and again as the ability resolves. `adventureSpellUsesTheAdventuresManaValue` casts Bonecrusher
  Giant's Stomp, which on the stack has only the Adventure's characteristics (CR 715.3b) and so mana value 2;
  `digitCrossedOutBeforeResolutionStopsTheAbility` crosses the only matching digit with a second Shock's trigger before
  the first's resolves, which then does nothing.
- Cool but Rude: "Whenever you discard a card, …" is its level 2 ability, which the Class has only at level 2 or greater
  (CR 716.2a); the "you discard a card" collector now judges the ability's intervening "if" as it triggers, so below
  level 2 a discard puts nothing on the stack (`levelOneDiscardDoesNotTriggerTheLevelTwoAbility`).

Idempotent; an optional first argument is the repository root (used for the HEAD worktree).

Usage: step15-run17z9b-tests.py [<repository root>]
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


DUELISTS = """    @Test
    @DisplayName("A spell cast as an Adventure has the Adventure's mana value")
    void adventureSpellUsesTheAdventuresManaValue() {
        Permanent source = addCard();
        source.setChosenNumberDigits(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        DarksteelCitadel drawnCard = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new BonecrusherGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(source.getCrossedNumberDigitPositions()).containsExactly(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("A digit crossed out before the ability resolves stops it: the \\"if\\" is checked again")
    void digitCrossedOutBeforeResolutionStopsTheAbility() {
        Permanent source = addCard();
        source.setChosenNumberDigits(List.of(1, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        DarksteelCitadel firstDraw = new DarksteelCitadel();
        DarksteelCitadel secondDraw = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(source.getCrossedNumberDigitPositions()).containsExactly(0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondDraw);
    }

"""

COOL_BUT_RUDE = """    @Test
    @DisplayName("Below level 2 the Class doesn't have the discard ability, so a discard doesn't trigger it")
    void levelOneDiscardDoesNotTriggerTheLevelTwoAbility() {
        castCoolButRude();
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Cool but Rude"));
    }

"""


def insert_before(text, anchor, block, marker):
    if marker in text:
        return text, False
    if anchor not in text:
        raise SystemExit(f"anchor missing: {anchor.strip()[:80]}")
    return text.replace(anchor, block + anchor, 1), True


def main():
    path = test_path("d", "DuelistsConvocationInternationalTest")
    text = path.read_text()
    text, changed = insert_before(text, "    private Permanent addCard() {", DUELISTS,
                                  "adventureSpellUsesTheAdventuresManaValue")
    if changed:
        text = text.replace("@CardUsed({DuelistsConvocationInternational.class, DarksteelCitadel.class, Counterspell.class,\n"
                            "        ErraticPortal.class, Memnite.class, WalkingBallista.class})",
                            "@CardUsed({DuelistsConvocationInternational.class, DarksteelCitadel.class, Counterspell.class,\n"
                            "        ErraticPortal.class, Memnite.class, WalkingBallista.class, BonecrusherGiant.class, Shock.class})")
        for name in ("cards.b.BonecrusherGiant", "cards.s.Shock"):
            text = add_import(text, f"import {PKG}.{name};\n")
        text = add_import(text, "import org.junit.jupiter.api.DisplayName;\n")
        path.write_text(text)
    print(f"DuelistsConvocationInternationalTest: {'updated' if changed else 'already updated'}")

    path = test_path("c", "CoolButRudeTest")
    text = path.read_text()
    text, changed = insert_before(text, "    @Test\n    void levelThreeStillDiscardsWhenLibraryIsEmpty() {", COOL_BUT_RUDE,
                                  "levelOneDiscardDoesNotTriggerTheLevelTwoAbility")
    if changed:
        path.write_text(text)
    print(f"CoolButRudeTest: {'updated' if changed else 'already updated'}")


if __name__ == "__main__":
    main()
