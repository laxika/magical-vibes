#!/usr/bin/env python3
"""Step 15 run 1: the agent-docs stop describing a dying card bound into the effect.

`DyingCreatureCardAwareEffect` is gone: the effects that act on "that card" of a death read it from the stack
entry's triggering card. This rewrites the constructor signatures that listed a `dyingCardId` / `cardId` /
`returnedCardId` form and the passages that said a collector binds the card into the effect. Exact
replacements, each applied where its text occurs; rerunnable (a replaced passage is not found again).
"""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"
ENTRY = "the stack entry's triggering card"

REPLACEMENTS = [
    # Constructor signatures.
    ("`(UUID dyingCardId)` / `()`", "`()`"),
    ("`()` / `(UUID dyingCardId)`", "`()`"),
    ("`(UUID dyingCardId)` or `()`", "`()`"),
    ("`()` (card def) / `(UUID dyingCardId)` (bound)", "`()`"),
    ("`(CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect)` (card def) / "
     "`(UUID dyingCardId, CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect)` (bound)",
     "`(CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect)`"),
    ("`(CreateTokenEffect token)` (card def) / `(UUID dyingCardId, CreateTokenEffect token)` (bound)",
     "`(CreateTokenEffect token)`"),
    (" / bound `(Supplier<? extends Card>, UUID dyingCardId)`", ""),
    ("`()` / bound `(UUID dyingCardId, int oilCounterCount)`", "`()` / bound `(int oilCounterCount)`"),
    ("effect (bound dying card and counters): for each oil counter",
     f"effect (the dying card is {ENTRY}, its counters bound): for each oil counter"),
    ("| `ReturnDyingCreatureToBattlefieldFaceDownAsCybermanEffect` | `(UUID dyingCardId)` |",
     "| `ReturnDyingCreatureToBattlefieldFaceDownAsCybermanEffect` | `()` |"),
    ("`(UUID dyingCardId, boolean attachSource[, CounterType, int])` or `(boolean attachSource[, CounterType, int])`",
     "`(boolean attachSource[, CounterType, int])`"),
    ("`(String landName)` or `(UUID dyingCardId, String landName)`", "`(String landName)`"),
    ("`(UUID dyingCardId, UUID handOwnerId)` / `()`", "`(UUID handOwnerId)` / `()`"),
    ("| `SuspectReturnedPermanentEffect` | `()` / `(UUID returnedCardId)` |",
     "| `SuspectReturnedPermanentEffect` | `()` |"),
    ("| `RegisterDelayedReturnCardFromGraveyardToHandEffect` | `(UUID cardId)` |",
     "| `RegisterDelayedReturnCardFromGraveyardToHandEffect` | `()` / `(boolean optional)` |"),
    ("`RegisterDelayedReturnCardFromGraveyardToHandEffect(null)`", "`RegisterDelayedReturnCardFromGraveyardToHandEffect()`"),
    # EFFECTS_QUICK_REFERENCE.md and EFFECTS_INDEX.md passages.
    ("the died card's id is bound at trigger collection (`DyingCreatureCardAwareEffect`), so a copy returns the "
     "copying card", f"the card that returns is {ENTRY}, so a copy returns the copying card"),
    (" — on `Triggers.creatureDies(YOU_CONTROL)` wrapped in a `MayEffect`, where the collector binds the dying card "
     "id via `DyingCreatureCardAwareEffect`.", " — on `Triggers.creatureDies(YOU_CONTROL)` wrapped in a `MayEffect`."),
    ("Returns the bound dying creature from", f"Returns the dying creature ({ENTRY}) from"),
    ("Returns the bound permanent card from", f"Returns the dying permanent card ({ENTRY}) from"),
    ("Returns the bound dying permanent card from", f"Returns the dying permanent card ({ENTRY}) from"),
    ("Returns the bound card tapped", f"Returns the dying card ({ENTRY}) tapped"),
    ("return the bound dying creature card", f"return the dying creature card ({ENTRY})"),
    ("return the bound dying permanent card", f"return the dying permanent card ({ENTRY})"),
    ("moves the bound dying card", f"moves the dying card ({ENTRY})"),
    ("ally-creature-death effect that binds the dying card, makes",
     f"ally-creature-death effect that reads the dying card ({ENTRY}), makes"),
    ("`DrainLifeFromDyingCreatureOwnerEffect(int)` binds the dying card during",
     f"`DrainLifeFromDyingCreatureOwnerEffect(int)` reads the dying card ({ENTRY}) during"),
    ("On a death trigger it forwards `DyingCreatureCardAwareEffect.boundToDyingCard` to its payload, so "
     "`MayEffect(SacrificeSelfThenEffect(ReturnTriggeringCreatureToBattlefieldEffect()))` gets the dying card bound "
     "(Angelic Renewal).",
     "On a death trigger the payload reads the dying card from the stack entry, so "
     "`MayEffect(SacrificeSelfThenEffect(ReturnTriggeringCreatureToBattlefieldEffect()))` returns it "
     "(Angelic Renewal)."),
    ("the death collector binds the card ID and the effect fizzles", f"the card is {ENTRY} and the effect fizzles"),
    ("the collector binds its card ID and does not track it with the source",
     f"the card is {ENTRY}; it is not tracked with the source"),
    ("`SuspectReturnedPermanentEffect(UUID)` is the death-trigger-bound form used after a self-return sequence "
     "(Presumed Dead).", f"After a self-return sequence on a death trigger it suspects {ENTRY} (Presumed Dead)."),
    ("bind the targeted creature's card and owner when the delayed trigger is registered",
     "bind the targeted creature's owner when the delayed trigger is registered (the card is the delayed "
     "trigger's triggering card)"),
    ("Implements `DyingCreatureCardAwareEffect` so the death-trigger collector binds the dying card id (the may "
     "flow drops the stack entry's triggering-card id).", f"The dying card is {ENTRY}."),
    ("if its bound card is an instant or sorcery", f"if the dying card ({ENTRY}) is an instant or sorcery"),
    ("next turn. Implements `DyingCreatureCardAwareEffect`.", "next turn."),
    ("Implements `DyingCreatureCardAwareEffect` so the death-trigger collector binds the dying card ID; if",
     f"The dying card is {ENTRY}; if"),
    ("Implements `DyingCreatureCardAwareEffect`; use the nested copy profile", "Use the nested copy profile"),
    ("the death collector binds the card ID and resolution fizzles", f"the card is {ENTRY} and resolution fizzles"),
    (" It binds nested `DyingCreatureCardAwareEffect` values when used by a death trigger.", ""),
    ("The death collector binds the dying card ID; no-op", f"The card is {ENTRY}; no-op"),
    ("Implements `DyingCreatureCardAwareEffect`; the death-trigger collector binds the dying card ID before queuing "
     "the may choice.", f"The dying card is {ENTRY}."),
    ("a token copy of the exiled card. Implements `DyingCreatureCardAwareEffect` |",
     f"a token copy of the exiled card. The dying card is {ENTRY} |"),
    ("Implements `DyingCreatureCardAwareEffect`; the death-trigger collector binds the dying card ID.",
     f"The dying card is {ENTRY}."),
    ("Implements `DyingCreatureCardAwareEffect`; the copy profile", "The copy profile"),
    ("The death collector binds the card ID before stacking; it does not track", f"The card is {ENTRY}; it does not track"),
    ("; the bound form is used in a death-trigger sequence.", f"; in a death-trigger sequence it suspects {ENTRY}."),
    ("same `DyingCreatureCardAwareEffect` binding.", f"the dying card is {ENTRY}."),
    ("The graveyard trigger collector bakes the dead card id and owner.",
     f"The card is {ENTRY}; the graveyard trigger collector bakes its owner."),
    ("No-arg constructor (dyingCardId is null) used in card definition; dyingCardId populated at trigger time",
     f"The dying card is {ENTRY}"),
    ("Card definition uses the no-arg ctor; `DeathTriggerCollectorService.handleAllyCreatureMay` / "
     "`handleAllyNontokenExileAndTrack` binds the concrete `dyingCardId` at trigger time.", f"The dying card is {ENTRY}."),
    ("Implements `DyingCreatureCardAwareEffect`; `GraveyardService.triggerDamagedCreatureDiesAbilities` binds the "
     "dying creature's card id onto the wrapped effect (the may flow drops `StackEntry.triggeringCardId`).",
     f"The dying card is {ENTRY}."),
    ("`ReturnTriggeringCardToOwnerHandEffect()` is bound to the registered target's card and owner for Together "
     "Forever's targeted delayed hand return.",
     "`ReturnTriggeringCardToOwnerHandEffect()` is bound to the registered target's owner for Together Forever's "
     "targeted delayed hand return; the card is the delayed trigger's triggering card."),
    # ORACLE_TEXT_EFFECT_MAP.md and the CARD_PATTERNS files.
    ("the enchanted-player Curse trigger binds the dying card and creates", f"the dying card is {ENTRY}; the Curse creates"),
    ("The sequence binds the dying card ID before resolving so the returned permanent is suspected after the "
     "owner-controlled return.", f"The suspect step reads {ENTRY}, so the returned permanent is suspected after the "
     "owner-controlled return."),
    ("Handler falls back to the stack-entry source card when `cardId` is null.",
     "With no triggering card the handler returns the stack entry's source card."),
    ("The ally-death collector binds the dying card's id before the delayed-return effect resolves;",
     f"The card is {ENTRY};"),
    ("`DeathTriggerCollectorService` binds the *dying* creature's card id at collection time, so the null fallback "
     "to the source card never applies here.", f"The card is {ENTRY}, the *dying* creature's."),
    ("The card is the *dying* creature's, the stack entry's triggering card.",
     "The card is the stack entry's triggering card, the *dying* creature's."),
    ("The ally-death collector binds the dying card for the exile step and chooses",
     f"The exile step reads {ENTRY}; the collector chooses"),
    ("`SacrificeSelfThenEffect` forwards `boundToDyingCard` to its payload, and the ally-death may-collector binds "
     "the dying card id onto any `DyingCreatureCardAwareEffect` it wraps", f"The payload reads {ENTRY}"),
    ("Angelic Renewal. the payload reads", "Angelic Renewal. The payload reads"),
    (" `dyingCardId` is null in the card definition and bound at trigger time via "
     "`DyingCreatureCardAwareEffect#boundToDyingCard`.", f" The card is {ENTRY}."),
    ("Dying card id bound via `DyingCreatureCardAwareEffect` when the trigger is stacked;", f"The card is {ENTRY};"),
    ("The dying card id is bound via `DyingCreatureCardAwareEffect`;", f"The card is {ENTRY};"),
    ("The death effect binds the dying card ID, checks its type", f"The death effect reads {ENTRY}, checks its type"),
    ("the collector binds the dying card id. Wrap", f"the card is {ENTRY}. Wrap"),
    ("The collector binds the dying card id and the returned creature", f"The card is {ENTRY} and the returned creature"),
    ("the `null` cardId makes the delayed return use the discarded card,",
     "the delayed return takes the discarded card, the stack entry's triggering card,"),
    ("`handleAllyCreatureMay` binds the dying card id onto the wrapped `DyingCreatureCardAwareEffect`, because the "
     "may flow does not carry `StackEntry.triggeringCardId`", f"the card to return is {ENTRY}"),
    ("The effect implements `DyingCreatureCardAwareEffect` so the collector binds the dying card id;",
     f"The card is {ENTRY};"),
    ("the death collector binds the dying card ID and the handler uses", f"the card is {ENTRY} and the handler uses"),
    ("The death collector binds the dying card so it is tracked with the Equipment;",
     f"The card is {ENTRY}, tracked with the Equipment;"),
    ("the death trigger binds the dying card identity, applies", f"the dying card is {ENTRY}; the effect applies"),
    ("The dying card id is bound onto the wrapped effect via `DyingCreatureCardAwareEffect` (the may flow drops the "
     "stack entry's triggering-card id);", f"The card is {ENTRY};"),
    ("The death collector binds the dying card id, and the static effect", f"The card is {ENTRY}, and the static effect"),
    ("(the death collector binds the dying card id)", f"(the card is {ENTRY})"),
    ("(mandatory — no MayEffect; the collector binds the dying card id)", f"(mandatory — no MayEffect; the card is {ENTRY})"),
    ("— the collector binds the dying card ID; if it is still", f"— the card is {ENTRY}; if it is still"),
]

PATTERNS = [
    # Tiana / Rienne / Liesa / The Locust God: one cell describing how the card was chosen.
    (re.compile(r"Pass an explicit `cardId` when the collector bakes it \(Tiana, [^|]*?`entry\.getCard\(\)\.getId\(\)`"),
     "The card is the stack entry's triggering card: Tiana's dying Aura or Equipment "
     "(`Triggers.dies(Triggers.AURA_OR_EQUIPMENT, ObjectRelation.YOU_CONTROL)`, the optional form), the dying creature "
     "on `Triggers.creatureDies(YOU_CONTROL)` or `Triggers.nontokenCreatureDies(YOU_CONTROL)` (\"return *that card*\" — "
     "Rienne, Angel of Rebirth and Liesa, Forgotten Archangel); with none it is the source card (`Triggers.dies(THIS)`, "
     "The Locust God)"),
]

changed = []
for path in sorted(DOCS.glob("*.md")):
    text = path.read_text()
    new = text
    for old, replacement in REPLACEMENTS:
        new = new.replace(old, replacement)
    for pattern, replacement in PATTERNS:
        new = pattern.sub(replacement, new)
    if new != text:
        path.write_text(new)
        changed.append(path.name)
print("changed:", ", ".join(changed) if changed else "nothing")
