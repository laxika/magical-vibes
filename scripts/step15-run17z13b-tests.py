#!/usr/bin/env python3
"""Step 15, run 17z13b: the card tests of the emblem conversion.

- `TheMasamuneTest`: its two hand-built emblems held the removed `EmblemCreatureDeathTriggerEffect` marker; they hold
  the "whenever a creature dies, draw a card" ability as an emblem `TriggeredAbility`
  (`Triggers.creatureDies(ObjectRelation.ANY)`), which the emblem creature-death reader reads.
- New, the rules fixes: `DarettiScrapSavantTest#minusTenEmblemTriggersForArtifactTokenThatReturnsNothing` (an artifact
  token put into your graveyard triggers the emblem before it ceases to exist, CR 111.7; the ability returns nothing)
  and `TeferiWhoSlowsTheSunsetTest#emblemDrawsDuringOpponentsDrawStepWithoutTheStack` (the emblem's draw is no
  triggered ability: it happens as the opponent's draw step's draw does, nothing on the stack).
- New: `TamiyoTheMoonSageTest#emblemIgnoresTokenPutIntoGraveyard` (a token isn't a card, CR 108.2b).

Idempotent: a file already changed is left alone. An optional repository-root argument edits another checkout (the
HEAD worktree, to run the new cases there).

Usage: step15-run17z13b-tests.py [<repository root>]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "src/test/java/com/github/laxika/magicalvibes/cards"

MASAMUNE = TESTS / "t" / PKG / "t/TheMasamuneTest.java"
DARETTI = TESTS / "d" / PKG / "d/DarettiScrapSavantTest.java"
TEFERI = TESTS / "t" / PKG / "t/TeferiWhoSlowsTheSunsetTest.java"
TAMIYO = TESTS / "t" / PKG / "t/TamiyoTheMoonSageTest.java"

MASAMUNE_EDITS = [
    ("""        gd.emblems.add(new Emblem(player1.getId(), List.of(
                new EmblemCreatureDeathTriggerEffect(List.of(new DrawCardEffect()), null)),
                new ExultantCultist()));""",
     """        gd.emblems.add(new Emblem(player1.getId(), List.of(), new ExultantCultist(), List.of(), 0L,
                List.of(new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANY), new DrawCardEffect()))));"""),
    ("import com.github.laxika.magicalvibes.model.effect.EmblemCreatureDeathTriggerEffect;\n",
     "import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;\n"
     "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n"),
    ("import com.github.laxika.magicalvibes.model.Permanent;\n",
     "import com.github.laxika.magicalvibes.model.Permanent;\n"
     "import com.github.laxika.magicalvibes.model.TriggeredAbility;\n"),
]

DARETTI_CASE = '''
    @Test
    @DisplayName("-10 emblem triggers for an artifact token put into your graveyard, which returns nothing")
    void minusTenEmblemTriggersForArtifactTokenThatReturnsNothing() {
        addReadyDaretti(10);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent token = harness.addToBattlefieldAndReturn(player1, artifactToken());
        harness.sacrifice(token);

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getDescription()).isEqualTo("Daretti, Scrap Savant's emblem"));
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Artifact Token"));
    }

    private static Card artifactToken() {
        Card card = new Card();
        card.setName("Artifact Token");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("");
        card.setToken(true);
        card.setColor(null);
        card.setColors(List.of());
        return card;
    }
'''
DARETTI_ANCHOR = "\n    private Permanent addReadyDaretti(int loyalty) {"

TEFERI_CASE = '''
    @Test
    @DisplayName("Emblem draws during each opponent's draw step without using the stack")
    void emblemDrawsDuringOpponentsDrawStepWithoutTheStack() {
        addReadyTeferi(player1, 7);
        Card drawnByEmblem = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnByEmblem));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        advanceToDraw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnByEmblem);
    }
'''
TEFERI_ANCHOR = "\n    private Permanent addReadyTeferi(Player player, int loyalty) {"

TAMIYO_CASE = '''
        @Test
        @DisplayName("Emblem does not trigger for a token put into your graveyard")
        void emblemIgnoresTokenPutIntoGraveyard() {
            giveEmblem(player1);
            Card tokenCard = new Card();
            tokenCard.setName("Soldier");
            tokenCard.setType(CardType.CREATURE);
            tokenCard.setManaCost("");
            tokenCard.setToken(true);
            tokenCard.setColor(null);
            tokenCard.setColors(List.of());
            tokenCard.setPower(1);
            tokenCard.setToughness(1);
            Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);

            harness.sacrifice(token);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
'''
TAMIYO_ANCHOR = '''        @Test
        @DisplayName("Emblem does not trigger on an opponent's graveyard")'''


def add_import(text, line):
    """Adds {@code line} among the package's imports, before the first that sorts after it."""
    if line in text:
        return text
    lines = text.split("\n")
    ours = [i for i, l in enumerate(lines) if l.startswith("import com.")]
    index = next((i for i in ours if lines[i] > line), ours[-1] + 1)
    lines.insert(index, line)
    return "\n".join(lines)


def edit(path, change):
    text = path.read_text()
    new = change(text)
    if new != text:
        path.write_text(new)
        print(f"changed {path.name}")


def masamune(text):
    if "EmblemCreatureDeathTriggerEffect" not in text:
        return text
    for old, new in MASAMUNE_EDITS:
        if old not in text:
            raise SystemExit(f"TheMasamuneTest: expected text not found:\n{old}")
        text = text.replace(old, new)
    return text


def daretti(text):
    if "minusTenEmblemTriggersForArtifactTokenThatReturnsNothing" in text:
        return text
    text = text.replace(DARETTI_ANCHOR, DARETTI_CASE + DARETTI_ANCHOR, 1)
    return add_import(text, "import com.github.laxika.magicalvibes.model.CardType;")


def teferi(text):
    if "emblemDrawsDuringOpponentsDrawStepWithoutTheStack" in text:
        return text
    return text.replace(TEFERI_ANCHOR, TEFERI_CASE + TEFERI_ANCHOR, 1)


def tamiyo(text):
    if "emblemIgnoresTokenPutIntoGraveyard" in text:
        return text
    if TAMIYO_ANCHOR not in text:
        raise SystemExit("TamiyoTheMoonSageTest: anchor not found")
    text = text.replace(TAMIYO_ANCHOR, TAMIYO_CASE.lstrip("\n") + "\n" + TAMIYO_ANCHOR, 1)
    text = add_import(text, "import com.github.laxika.magicalvibes.model.Card;")
    return add_import(text, "import com.github.laxika.magicalvibes.model.CardType;")


def main():
    edit(MASAMUNE, masamune)
    edit(DARETTI, daretti)
    edit(TEFERI, teferi)
    edit(TAMIYO, tamiyo)


if __name__ == "__main__":
    main()
