#!/usr/bin/env python3
"""Step 15, run 17z4b: card tests for the triggered mana abilities whose produced-mana effects resolve by handler.

- `RegalBehemothTest`: "add an additional one mana of any color" offers the five colors (CR 106.1a) when a land that
  produced colorless mana is tapped, where the collector offered the land's produced types and the five colors
  together, colorless among them (a rules fix);
- `SasayaTest`: Sasaya's Essence counts the other lands with the name of a land sacrificed for its mana, as it last
  existed (CR 608.2h), where the collector required the land on the battlefield and added nothing (a rules fix).

Each case is added once, with the imports and `@CardUsed` entries it needs. Idempotent; optional repository-root
argument.

Usage: step15-run17z4b-tests.py [<repository root>]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"

CASES = {
    "r/RegalBehemothTest": (
        "extraManaIsOfAColorWhenTheLandProducedColorless",
        """
    @Test
    @DisplayName("The extra mana is one of the five colors, never colorless, when the land produced colorless mana")
    void extraManaIsOfAColorWhenTheLandProducedColorless() {
        harness.addToBattlefield(player1, new RegalBehemoth());
        gd.monarchPlayerId = player1.getId();
        harness.addToBattlefield(player1, new ReliquaryTower());

        harness.activateAbility(player1, 1, 0, null, null);

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }
""",
        ["com.github.laxika.magicalvibes.cards.r.ReliquaryTower"],
        ["ReliquaryTower.class"]),
    "s/SasayaTest": (
        "landSacrificedForItsManaStillCountsOtherLandsWithItsName",
        """
    @Test
    @DisplayName("A land sacrificed for its mana still counts the other lands with its name, as it last existed")
    void landSacrificedForItsManaStillCountsOtherLandsWithItsName() {
        addTransformedSasaya();
        harness.addToBattlefield(player1, new AncientSpring());
        harness.addToBattlefield(player1, new AncientSpring());

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }
""",
        ["com.github.laxika.magicalvibes.cards.a.AncientSpring", "com.github.laxika.magicalvibes.model.PendingInteraction"],
        ["AncientSpring.class"]),
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
