#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 16: the docs for the triggering card and player.

Idempotent; run from the repository root:

    python3 scripts/step15-triggering-card-docs.py

Exact edits in agent-docs: Mind's Dilation's "first spell each turn" and Mazzy's "an Aura" are trigger filters, the
hand return reads its owner from the graveyard that holds the card, Garland's monarch is the entry's triggering player.
Fails if a removed form is still named.
"""
import pathlib
import sys

DOCS = pathlib.Path("agent-docs")

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| `ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect()` | `Triggers.opponentCastsSpell()` | Mind's "
         "Dilation (EMN). Collector gates on the triggering player's per-turn spell count equaling one; resolution",
         "| `ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect()` | `Triggers.opponentCastsSpell().nthThisTurn(1)` "
         "| Mind's Dilation (EMN). \"Their first spell each turn\" is the trigger's `nthThisTurn(1)`, which the generic "
         "cast collector judges; it gives the caster as the entry's non-targeting \"that player\". Resolution"),
        ("Garland, Royal Kidnapper (FIC 442); the monarch-trigger target captures the new monarch as the required "
         "target controller, and control ends when that player loses the designation.",
         "Garland, Royal Kidnapper (FIC 442); \"that player\" is the stack entry's triggering player (the new monarch), "
         "the target restricted by the card's own filter, and control ends when that player loses the designation."),
        ("| `ExileTriggeringAuraFromGraveyardMayCastUntilNextTurnEffect()` | `Triggers.dies(Triggers.AURA_OR_EQUIPMENT, "
         "ObjectRelation.YOU_CONTROL)` | The death-trigger collector filters the shared Aura/Equipment event to Auras, "
         "binds the dying Aura's card ID, then the effect exiles it",
         "| `ExileTriggeringAuraFromGraveyardMayCastUntilNextTurnEffect()` | `Triggers.dies(new PermanentAllOfPredicate("
         "List.of(Triggers.AURA_OR_EQUIPMENT, new PermanentHasSubtypePredicate(CardSubtype.AURA))), "
         "ObjectRelation.YOU_CONTROL)` | \"An Aura\" is the trigger's filter (inside the Aura-or-Equipment filter the "
         "watch is read by); the dying Aura is the entry's triggering card, and the effect exiles it"),
        ("| Enduring Renewal. The card is the stack entry's triggering card; returns from the controller's graveyard to "
         "their hand (fizzles if removed in response / token) |",
         "| Enduring Renewal. The card is the stack entry's triggering card; returns from its owner's graveyard to that "
         "player's hand (fizzles if removed in response / token) |"),
        ("Fires for any player's permanent of any type; the collector bakes the dead card's owner so it goes to the "
         "owner's hand.",
         "Fires for any player's permanent of any type; the card is the entry's triggering card, and the graveyard that "
         "holds it names the owner's hand."),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `GainControlOfTargetCreatureWhileMonarchEffect()` - targeted monarch-triggered control effect; captures the "
         "new monarch as the required target controller and expires",
         "- `GainControlOfTargetCreatureWhileMonarchEffect()` - targeted monarch-triggered control effect; \"that "
         "player\" is the stack entry's triggering player (the new monarch, `StackEntry.getTriggeringPlayerId()`), the "
         "target restricted by the card's own filter, and it expires"),
        ("- `ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect()` — an opponent-cast trigger checks that this "
         "is that opponent's first spell of the turn, then exiles",
         "- `ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect()` — on "
         "`Triggers.opponentCastsSpell().nthThisTurn(1)` (\"their first spell each turn\", a trigger filter), exiles"),
        ("- `ResolveEffectOnTargetDeathThisTurnEffect(new ReturnTriggeringCardToOwnerHandEffect())` — activated-ability "
         "composition (Together Forever): bind the targeted creature's owner when the delayed trigger is registered "
         "(the card is the delayed trigger's triggering card), then return",
         "- `ResolveEffectOnTargetDeathThisTurnEffect(new ReturnTriggeringCardToOwnerHandEffect())` — activated-ability "
         "composition (Together Forever): the card is the delayed trigger's triggering card; return"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect` | `()` | `Triggers.opponentCastsSpell()` trigger "
         "checks the caster's per-turn spell count and fires only for that opponent's first spell each turn; "
         "resolution exiles",
         "| `ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect` | `()` | "
         "`Triggers.opponentCastsSpell().nthThisTurn(1)` (\"their first spell each turn\" is the trigger's filter); "
         "resolution exiles"),
        ("| `ReturnTriggeringCardToOwnerHandEffect` | `(UUID handOwnerId)` / `()` |",
         "| `ReturnTriggeringCardToOwnerHandEffect` | `()` |"),
        ("The card is the stack entry's triggering card; the graveyard trigger collector bakes its owner.",
         "The card is the stack entry's triggering card; the graveyard that holds it names the owner's hand."),
        ("`ReturnTriggeringCardToOwnerHandEffect()` is bound to the registered target's owner for Together Forever's "
         "targeted delayed hand return; the card is the delayed trigger's triggering card.",
         "`ReturnTriggeringCardToOwnerHandEffect()` is Together Forever's targeted delayed hand return; the card is the "
         "delayed trigger's triggering card."),
        ("| `KayaSpiritsJusticeEffect` | `()` | `ON_CONTROLLER_CREATURES_OR_CREATURE_CARDS_EXILED` trigger marker: offers "
         "a creature card from exiled event cards,",
         "| `KayaSpiritsJusticeEffect` | `()` | `ON_CONTROLLER_CREATURES_OR_CREATURE_CARDS_EXILED` trigger marker: offers "
         "a creature card from the stack entry's triggering cards (the exiled cards, carried by its token-target "
         "choice),"),
    ],
    "CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("| `m/MindsDilation.java` | Triggers.opponentCastsSpell() `ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect()` "
         "— collector gates on the opponent's per-turn spell count equaling one, carries the caster as non-target "
         "context,",
         "| `m/MindsDilation.java` | Triggers.opponentCastsSpell().nthThisTurn(1) "
         "`ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect()` — the generic cast collector judges \"their "
         "first spell each turn\" and carries the caster as non-target context,"),
    ],
}

REMOVED = ("TargetControllerIsTriggeringPlayerEffect", "(UUID handOwnerId)", "bakes the dead card's owner",
           "captures the new monarch as the required target controller")


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        converted = text
        for old, new in edits:
            if new in converted:
                continue
            if old not in converted:
                raise SystemExit(f"{path}: neither the old nor the new form of {old[:80]!r}")
            converted = converted.replace(old, new)
        if converted != text:
            path.write_text(converted)
            print(f"edited {path}")
    stale = [(path, form) for path in DOCS.glob("*.md") for form in REMOVED if form in path.read_text()]
    if stale:
        for path, form in stale:
            print(f"{path}: still names {form!r}", file=sys.stderr)
        raise SystemExit(1)


if __name__ == "__main__":
    main()
