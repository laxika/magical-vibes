#!/usr/bin/env python3
"""Step 15, run 17z11e: the card tests the run changes.

- The Rack, Black Vise, Energy Vortex: "at the beginning of the chosen player's upkeep" triggers only for a chosen
  player, so the cases that put the permanent onto the battlefield without its as-enters choice record player2 as the
  chosen player (the collector picked the controller's opponent for them before).
- New `BlackViseTest#copyWithoutChosenPlayerDoesNotTrigger`: a Mizzium Transreliquat that becomes a copy of Black Vise
  has no chosen player (a chosen player isn't a copiable value), so only the original triggers.
- New `MalignantGrowthTest#thatPlayerWithShroudStillDrawsAndTakesDamage`: "that player" of an each-draw-step ability
  isn't a target (CR 115.10a), so a player with shroud still draws and is dealt the damage.

Idempotent. Usage: step15-run17z11e-tests.py [repository root]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"


def test(letter, name):
    return TESTS / letter / f"src/test/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"


CHOSEN = "setRememberedTargetPlayerId(player2.getId());"


def choose_player2(path, card):
    text = path.read_text()
    lines = text.split("\n")
    out = []
    for i, line in enumerate(lines):
        bare = re.fullmatch(rf"(\s*)harness\.addToBattlefield(?:AndReturn)?\(player1, new {card}\(\)\);", line)
        named = re.fullmatch(rf"(\s*)(?:var|Permanent) (\w+) = harness\.addToBattlefieldAndReturn\(player1, new {card}\(\)\);",
                             line)
        if bare:
            out.append(f"{bare.group(1)}harness.addToBattlefieldAndReturn(player1, new {card}()).{CHOSEN}")
        elif named:
            out.append(line)
            follow = f"{named.group(1)}{named.group(2)}.{CHOSEN}"
            if i + 1 >= len(lines) or lines[i + 1] != follow:
                out.append(follow)
        else:
            out.append(line)
    new = "\n".join(out)
    if new != text:
        path.write_text(new)
        print("edited", path.relative_to(ROOT))


def add_import(text, imp):
    line = f"import {imp};"
    lines = text.split("\n")
    if line in lines:
        return text
    plain = [i for i, l in enumerate(lines) if l.startswith("import ") and not l.startswith("import static")]
    at = next((i for i in plain if lines[i] > line), plain[-1] + 1)
    lines.insert(at, line)
    return "\n".join(lines)


def add_case(path, marker, case, imports=(), card_used=()):
    text = path.read_text()
    if marker in text:
        return
    for imp in imports:
        text = add_import(text, imp)
    for used in card_used:
        text = re.sub(r"@CardUsed\(\{([^}]*)\}\)", lambda m: "@CardUsed({" + m.group(1) + ", " + used + "})"
                      if used not in m.group(1) else m.group(0), text, count=1)
    end = text.rstrip().rfind("}")
    text = text[:end].rstrip("\n") + "\n\n" + case + "}\n"
    path.write_text(text)
    print("edited", path.relative_to(ROOT))


BLACK_VISE_CASE = '''    @Test
    @DisplayName("A permanent that becomes a copy of Black Vise has no chosen player, so its ability never triggers")
    void copyWithoutChosenPlayerDoesNotTrigger() {
        var blackVise = harness.addToBattlefieldAndReturn(player1, new BlackVise());
        blackVise.setRememberedTargetPlayerId(player2.getId());
        harness.addToBattlefield(player1, new MizziumTransreliquat());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, 1, null, blackVise.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Black Vise");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Black Vise")).hasSize(2);
        harness.setHand(player2, bears(6));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

'''

MALIGNANT_GROWTH_CASE = '''    @Test
    @DisplayName("\\"That player\\" isn't a target: a player with shroud still draws and is dealt the damage")
    void thatPlayerWithShroudStillDrawsAndTakesDamage() {
        Permanent growth = harness.addToBattlefieldAndReturn(player1, new MalignantGrowth());
        growth.setCounterCount(CounterType.GROWTH, 2);
        harness.addToBattlefield(player2, new IvoryMask());

        int deckBefore = gd.playerDecks.get(player2.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

'''


def main():
    choose_player2(test("t", "TheRackTest"), "TheRack")
    choose_player2(test("b", "BlackViseTest"), "BlackVise")
    choose_player2(test("e", "EnergyVortexTest"), "EnergyVortex")
    add_case(test("b", "BlackViseTest"), "copyWithoutChosenPlayerDoesNotTrigger", BLACK_VISE_CASE,
             imports=["com.github.laxika.magicalvibes.cards.m.MizziumTransreliquat"],
             card_used=["MizziumTransreliquat.class"])
    add_case(test("m", "MalignantGrowthTest"), "thatPlayerWithShroudStillDrawsAndTakesDamage", MALIGNANT_GROWTH_CASE,
             imports=["com.github.laxika.magicalvibes.cards.i.IvoryMask"], card_used=["IvoryMask.class"])


if __name__ == "__main__":
    main()
