#!/usr/bin/env python3
"""Step 15, run 17z5: card tests for the reveal abilities that are a static reveal and filtered reveal abilities.

- `InquisitorEisenhornTest`: Roaming Throne naming Human makes "whenever you reveal an instant or sorcery card this
  way" trigger an additional time, where the reveal choice's handler put one ability on the stack by hand (a rules
  fix); Cherubael is still created when the revealed card left the hand before the ability resolves (its ruling);
- `GodEternalKefnetTest`: the copy is made from the revealed card's last-known information when the card left the hand
  before the ability resolves (its ruling; the stack entry's triggering card).

Each case is added once, with the imports and `@CardUsed` entries it needs. Idempotent; optional repository-root
argument.

Usage: step15-run17z5-tests.py [<repository root>]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"

CASES = [
    ("i/InquisitorEisenhornTest",
        "roamingThroneMakesTheRevealAbilityTriggerAnAdditionalTime",
        """
    @Test
    @DisplayName("Roaming Throne naming Human makes the reveal ability trigger an additional time")
    void roamingThroneMakesTheRevealAbilityTriggerAnAdditionalTime() {
        harness.addToBattlefield(player1, new InquisitorEisenhorn());
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new RoamingThrone());
        throne.setChosenSubtype(CardSubtype.HUMAN);
        harness.setLibrary(player1, List.of(new DreadReturn(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allSatisfy(entry -> assertThat(entry.getCard().getName()).isEqualTo("Inquisitor Eisenhorn"));
    }
""",
        ["com.github.laxika.magicalvibes.cards.r.RoamingThrone", "com.github.laxika.magicalvibes.model.CardSubtype"],
        ["RoamingThrone.class"]),
    ("i/InquisitorEisenhornTest",
        "createsCherubaelWhenTheRevealedCardLeftTheHand",
        """
    @Test
    @DisplayName("Cherubael is still created when the revealed card leaves the hand before the ability resolves")
    void createsCherubaelWhenTheRevealedCardLeftTheHand() {
        harness.addToBattlefield(player1, new InquisitorEisenhorn());
        harness.setLibrary(player1, List.of(new DreadReturn(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cherubael")).hasSize(1);
    }
""",
        [], []),
    ("g/GodEternalKefnetTest",
        "copiesTheRevealedCardWhenItLeftTheHand",
        """
    @Test
    @DisplayName("The revealed card is copied from its last-known information when it leaves the hand first")
    void copiesTheRevealedCardWhenItLeftTheHand() {
        harness.addToBattlefield(player1, new GodEternalKefnet());
        harness.setLibrary(player1, List.of(new Divination(), new Forest(), new Mountain(), new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.setHand(player1, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }
""",
        [], []),
]


def add_import(text, fqcn):
    line = f"import {fqcn};"
    if line in text:
        return text
    imports = list(re.finditer(r"^import [^;]+;$", text, re.M))
    pos = imports[0].start()
    return text[:pos] + line + "\n" + text[pos:]


def add_card_used(text, classes):
    match = re.search(r"@CardUsed\(\{([^}]*)\}\)", text)
    if not match:
        raise SystemExit("no @CardUsed")
    used = match.group(1)
    missing = [c for c in classes if c not in used]
    if not missing:
        return text
    return text[:match.end(1)] + ", " + ", ".join(missing) + text[match.end(1):]


def main():
    for rel, name, body, imports, used in CASES:
        letter, cls = rel.split("/")
        path = TESTS / letter / f"src/test/java/com/github/laxika/magicalvibes/cards/{letter}/{cls}.java"
        text = path.read_text()
        if f"void {name}()" in text:
            continue
        for fqcn in imports:
            text = add_import(text, fqcn)
        if used:
            text = add_card_used(text, used)
        index = text.index("\n    @Test")
        text = text[:index] + "\n" + body.strip("\n") + "\n" + text[index:]
        path.write_text(text)
        print(f"added {cls}#{name}")


if __name__ == "__main__":
    main()
