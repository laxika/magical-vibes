#!/usr/bin/env python3
"""Step 15, run 17z14e: the cards whose abilities `CombatAttackService`'s attack collectors read differently now that
each ability goes on the stack whole (row 17z14e; row 17z14h's list, `step15-run17z14-scan.py`).

The attack collectors put each triggered ability on the stack as one object of its effects (CR 603.3), no longer one
object of every ability of a watch, so an ability split into several `addTrigger` calls is several objects. Each card
under `EDITS` was read against its Oracle text (Scryfall MCP) and prints one ability there; its calls become one
`addTrigger`, its effects in the order they were written.

Usage: step15-run17z14e-cards.py [LETTER...] (run per letter module; no letter means all). Fails if an edit didn't
apply; idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "magical-vibes-card"
PKG = "src/main/java/com/github/laxika/magicalvibes/cards"

# card -> [(old, new)]
EDITS = {
    # "Whenever you attack a player, tap target creature that player controls and goad it." One ability with one
    # target; the "you attack a player" collector joined the two calls into one target choice.
    "KarazikarTheEyeTyrant": [(
        """                .addTrigger(Triggers.youAttackPlayer(),
                        new TapPermanentsEffect(TapUntapScope.TARGET))
                .addTrigger(Triggers.youAttackPlayer(),
                        new GoadTargetCreatureUntilNextTurnEffect());""",
        """                .addTrigger(Triggers.youAttackPlayer(),
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new GoadTargetCreatureUntilNextTurnEffect());""")],
    # "Whenever an opponent attacks a planeswalker you control with one or more creatures, Oath of Kaya deals 2 damage
    # to that player and you gain 2 life." One ability; the collector joined the two calls into one object.
    "OathOfKaya": [(
        """        addTrigger(Triggers.opponentAttacksYourPlaneswalker(),
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER));
        addTrigger(Triggers.opponentAttacksYourPlaneswalker(), new GainLifeEffect(2));""",
        """        addTrigger(Triggers.opponentAttacksYourPlaneswalker(),
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER), new GainLifeEffect(2));""")],
    # "Whenever this creature attacks or blocks, each player mills a card." One ability of one effect; the two calls
    # went on the stack as two objects when it attacked.
    "TenuredOilcaster": [(
        """        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), new MillEffect(1, MillRecipient.CONTROLLER));
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), new MillEffect(1, MillRecipient.EACH_OPPONENT));""",
        """        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), new MillEffect(1, MillRecipient.EACH_PLAYER));""")],
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
            print(f"edited {module}/{name}")


if __name__ == "__main__":
    main()
