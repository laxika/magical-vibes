#!/usr/bin/env python3
"""Step 15, run 17z4: card tests for the played-name and land-tap abilities that read the stack entry.

- `BarbflareGremlinTest`: a land sacrificed for its mana still deals "that land deals 1 damage" as it last existed
  (CR 608.2h), where the collector rebuilt the damage from the land on the battlefield and dealt none (a rules fix);
- `TheMatrixOfTimeTest`: playing an opponent's tracked land makes its owner, read from the stack entry's triggering
  card as the ability resolves, lose 3 life and exile the top card of their library.

Each case is added once, with the imports and `@CardUsed` entries it needs. Idempotent; optional repository-root
argument.

Usage: step15-run17z4-tests.py [<repository root>]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"

CASES = {
    "b/BarbflareGremlinTest": (
        "landSacrificedForItsManaStillDealsTheDamage",
        """
    @Test
    @DisplayName("A land sacrificed for its mana still deals the damage, as it last existed")
    void landSacrificedForItsManaStillDealsTheDamage() {
        Permanent gremlin = addCreatureReady(player1, new BarbflareGremlin());
        gremlin.tap();
        Permanent spring = harness.addToBattlefieldAndReturn(player2, new AncientSpring());
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.handleListChoice(player2, "WHITE");

        var manaPool = gd.playerManaPools.get(player2.getId());
        assertThat(manaPool.get(ManaColor.WHITE) + manaPool.get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.damageDealtThisTurnBySource.getOrDefault(spring.getId(), 0)).isEqualTo(1);
        assertThat(gd.damageDealtThisTurnBySource.getOrDefault(gremlin.getId(), 0)).isZero();
    }
""",
        ["com.github.laxika.magicalvibes.cards.a.AncientSpring"],
        ["AncientSpring.class"]),
    "t/TheMatrixOfTimeTest": (
        "playingAnOpponentsTrackedLandMakesItsOwnerLoseLife",
        """
    @Test
    void playingAnOpponentsTrackedLandMakesItsOwnerLoseLife() {
        Card ownTop = new Forest();
        ownTop.setOwnerId(player1.getId());
        Card opposingLand = new Forest();
        opposingLand.setOwnerId(player2.getId());
        Card nextOpposingTop = new Forest();
        nextOpposingTop.setOwnerId(player2.getId());
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opposingLand, nextOpposingTop));
        revealMatrix();

        int ownLife = gd.getLife(player1.getId());
        int opposingLife = gd.getLife(player2.getId());
        harness.castFromExile(player1, opposingLand.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(ownLife);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opposingLife - 3);
        assertThat(gd.findExiledCard(nextOpposingTop.getId())).isNotNull();
        assertThat(gd.findExiledCard(nextOpposingTop.getId()).sourcePermanentId()).isNull();
    }
""",
        [],
        []),
}


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
    for rel, (name, body, imports, used) in CASES.items():
        letter, cls = rel.split("/")
        path = TESTS / letter / f"src/test/java/com/github/laxika/magicalvibes/cards/{letter}/{cls}.java"
        text = path.read_text()
        if f"void {name}()" in text:
            continue
        for fqcn in imports:
            text = add_import(text, fqcn)
        text = add_card_used(text, used)
        index = text.index("\n    @Test")
        text = text[:index] + "\n" + body.strip("\n") + "\n" + text[index:]
        path.write_text(text)
        print(f"added {cls}#{name}")


if __name__ == "__main__":
    main()
