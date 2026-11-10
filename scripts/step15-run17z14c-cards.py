#!/usr/bin/env python3
"""Step 15, run 17z14c: the cards that wrote one Oracle ability as several `addTrigger` calls on a watch whose
collector now reads each ability whole (rows 17z14b2 and 17z14c; row 17z14h's list, `step15-run17z14-scan.py`).

The collectors outside the registry (`PermanentCounterSupport`, the library search and shuffle helpers, "a player
loses the game", the self-cycled, championed, evolve, train and Class-level collectors) and the hand-rolled collectors
of `TriggerCollectionService` put each triggered ability on the stack as one object of its effects (CR 603.3), no
longer one object of every ability of a watch, so an ability split into several calls is several objects. Each card
below was read against its Oracle text and prints one ability there; its calls become one `addTrigger`, its effects in
the order the merging collector put them on the stack.

Usage: step15-run17z14c-cards.py [LETTER...] (run per letter module; no letter means all). Fails if an edit didn't
apply; idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "magical-vibes-card"
PKG = "src/main/java/com/github/laxika/magicalvibes/cards"

# card -> [(old, new)]
EDITS = {
    # "When you cycle this card, shuffle it into your library from your graveyard. If you've cycled a card named
    # Yidaro, Wandering Monster four or more times this game, put it onto the battlefield from your graveyard
    # instead." One ability; the self-cycled collector joined the two calls into one object.
    "YidaroWanderingMonster": [(
        """                .returnAll(true)
                .build()));
        addTrigger(Triggers.youCycleThisCard(), new ConditionalEffect(new NotCondition(threshold),
                new ShuffleSelfFromGraveyardIntoLibraryEffect()));""",
        """                .returnAll(true)
                .build()),
                new ConditionalEffect(new NotCondition(threshold),
                        new ShuffleSelfFromGraveyardIntoLibraryEffect()));""")],
    # "When a spell or ability an opponent controls causes you to discard this card, you gain 2 life, and you return
    # this card from your graveyard to your hand at the beginning of the next end step." One ability; the
    # self-discarded collector joined the two calls into one object.
    "MangarasBlessing": [(
        """        addTrigger(Triggers.opponentCausesYouToDiscardThisCard(), new GainLifeEffect(2));
        addTrigger(Triggers.opponentCausesYouToDiscardThisCard(),
                new RegisterDelayedReturnCardFromGraveyardToHandEffect());""",
        """        addTrigger(Triggers.opponentCausesYouToDiscardThisCard(), new GainLifeEffect(2),
                new RegisterDelayedReturnCardFromGraveyardToHandEffect());""")],
}


def find(name):
    for path in ROOT.glob(f"*/{PKG}/*/{name}.java"):
        return path.relative_to(ROOT).parts[0], path
    raise SystemExit(f"{name}: not found")


def main():
    letters = set(sys.argv[1:])
    for name, edits in EDITS.items():
        module, path = find(name)
        if letters and module not in letters:
            continue
        text = path.read_text()
        changed = False
        for old, new in edits:
            if new in text:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: edit didn't apply")
            text = text.replace(old, new)
            changed = True
        if changed:
            path.write_text(text)
            print(f"merged {module}/{name}")


if __name__ == "__main__":
    main()
