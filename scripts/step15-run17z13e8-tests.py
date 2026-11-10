#!/usr/bin/env python3
"""Step 15, run 17z13e8: the service test changes (effect-slot-refactor.md, run 17z13e8's Findings).

`StepTriggerServiceTest`'s two opponent-upkeep "few cards in hand" cases used the deleted
`DealDamageIfFewCardsInHandEffect`, whose class the reader judged; they write the intervening "if"
`ActivePlayerHandAtMost(3)` with `DealDamageToPlayersEffect(2, TRIGGERING_PLAYER)` (the assertions are unchanged).

`ObliviousBookwormTest` (card, the rules fix): "at the beginning of your end step, you may draw a card" goes on the
stack while drawing is prevented (CR 603.5); the your-end-step reader skipped it by the "you may"'s class.
The new `triggersWhileDrawingIsPrevented` asserts the ability is on the stack (`preventedOptionalDrawDoesNotForceDiscard`
passes unchanged: the "you may" with nothing it can do isn't asked as it resolves, and nothing is drawn or discarded).

Idempotent. Usage: step15-run17z13e8-tests.py [REPOSITORY_ROOT]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TEST = ROOT / ("magical-vibes-application/src/test/java/com/github/laxika/magicalvibes/service/turn/"
               "StepTriggerServiceTest.java")

OLD_CARD = "card.addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT), new DealDamageIfFewCardsInHandEffect(3, 2));"
NEW_CARD = ("card.addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),\n"
            "                    TriggerOptions.DEFAULT.withInterveningIf(new ActivePlayerHandAtMost(3)),\n"
            "                    new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PLAYER));")
OLD_NAME = "with DealDamageIfFewCardsInHandEffect"
NEW_NAME = "with an \\\"if that player has N or fewer cards in hand\\\" intervening if"
IMPORTS = [
    "import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandAtMost;",
    "import com.github.laxika.magicalvibes.model.effect.DamageRecipient;",
    "import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;",
]


BOOKWORM = ROOT / ("magical-vibes-card-test/o/src/test/java/com/github/laxika/magicalvibes/cards/o/"
                   "ObliviousBookwormTest.java")
NEW_CASE = """
    @Test
    @CardUsed(NarsetParterOfVeils.class)
    void triggersWhileDrawingIsPrevented() {
        harness.addToBattlefield(player1, new ObliviousBookworm());
        harness.addToBattlefieldAndReturn(player2, new NarsetParterOfVeils()).setCounterCount(CounterType.LOYALTY, 5);
        harness.setLibrary(player1, List.of(new CanyonLurkers()));
        harness.setHand(player1, List.of(new CanyonLurkers()));
        gd.cardsDrawnThisTurn.put(player1.getId(), 1);

        advanceToBookwormMay();

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Oblivious Bookworm"));
    }

    private void resolveBookwormMay() {"""


def bookworm():
    text = BOOKWORM.read_text()
    if "void triggersWhileDrawingIsPrevented()" not in text:
        text = text.replace("\n    private void resolveBookwormMay() {", NEW_CASE, 1)
    if "void triggersWhileDrawingIsPrevented()" not in text:
        raise SystemExit("ObliviousBookwormTest: an edit did not apply")
    BOOKWORM.write_text(text)


def main():
    bookworm()
    text = TEST.read_text()
    text = text.replace(OLD_CARD, NEW_CARD).replace(OLD_NAME, NEW_NAME)
    text = text.replace("import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;\n", "")
    anchor = "import com.github.laxika.magicalvibes.model.Card;\n"
    for line in IMPORTS:
        if line not in text:
            text = text.replace(anchor, anchor + line + "\n", 1)
    if "DealDamageIfFewCardsInHandEffect" in text:
        raise SystemExit("an old form is left")
    TEST.write_text(text)


if __name__ == "__main__":
    main()
