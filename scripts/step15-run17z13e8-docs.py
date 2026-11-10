#!/usr/bin/env python3
"""Step 15, run 17z13e8: the agent-docs rows of the forms the step readers judged by an effect's class.

Each edit replaces one substring of a doc row (the rest of the row is kept). The script fails if a doc still names a
removed form: `DealDamageIfFewCardsInHandEffect`, `OpponentDrawStepOnlyEffect`, `MonarchEndStepTriggeredEffect`,
`forOpponentDrawStep`, the `opponentDrawStepOnly` component. Idempotent. Usage: step15-run17z13e8-docs.py
"""
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| SPELL + Triggers.upkeep(ActivePlayer.YOU) from exile | All Hallow's Eve (`LEG`). The upkeep trigger is only "
         "collected for its owner while the card remains exiled with a scream counter;",
         "| SPELL + Triggers.upkeep(ActivePlayer.YOU) from exile, `withInterveningIf(new "
         "SourceCardInExileWithScreamCounter())` | All Hallow's Eve (`LEG`). \"If this card is exiled with a scream "
         "counter on it\" is the ability's intervening \"if\" (CR 603.4);"),
        ("| `AdditionalUpkeepStepEffect()` | `Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER)` | Paradox Haze (`TSP`).",
         "| `AdditionalUpkeepStepEffect()` | `Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER)` with "
         "`withEventCondition(new FirstUpkeepThisTurn())` (\"first upkeep each turn\") | Paradox Haze (`TSP`)."),
        ("| `EnterWithCountersEffect(CounterType.TIME, new Fixed(N))` + `RemoveCounterAndSacrificeSelfOnLastEffect("
         "CounterType.TIME)` | STATIC + Triggers.upkeep(ActivePlayer.YOU) | The upkeep effect removes a time counter "
         "only while one remains,",
         "| `EnterWithCountersEffect(CounterType.TIME, new Fixed(N))` + `RemoveCounterAndSacrificeSelfOnLastEffect("
         "CounterType.TIME)` | STATIC + Triggers.upkeep(ActivePlayer.YOU) with `TriggerOptions.DEFAULT."
         "withInterveningIf(new SourceCounterThreshold(1, CounterType.TIME))` (\"if this permanent has a time counter "
         "on it\", CR 702.63a; no reader judges the effect's class) | The upkeep effect removes a time counter,"),
        ("| Triggers.drawStep(ActivePlayer.ANY) | Malignant Growth (with Triggers.upkeep(ActivePlayer.YOU) "
         "`CumulativeUpkeepEffect(\"{1}\")` + `PutCountersOnSelfEffect(CounterType.GROWTH)`). \"Each **opponent's** draw "
         "step\" is the `OpponentDrawStepOnlyEffect` marker — `StepTriggerService` skips the trigger when the draw-step "
         "player is the source's controller |",
         "| Triggers.drawStep(ActivePlayer.OPPONENT) | Malignant Growth (with Triggers.upkeep(ActivePlayer.YOU) "
         "`CumulativeUpkeepEffect(\"{1}\")` + `PutCountersOnSelfEffect(CounterType.GROWTH)`). \"Each **opponent's** draw "
         "step\" is the trigger's player relation |"),
        ("| `DrawCardForTargetPlayerEffect.forOpponentDrawStep(1)` | Triggers.drawStep(ActivePlayer.ANY) | Well of Ideas "
         "(2XM 75). The factory marks the existing target-player draw effect as opponent-draw-step-only, so the "
         "controller's own draw step is skipped |",
         "| `DrawCardForTargetPlayerEffect(1)` | Triggers.drawStep(ActivePlayer.OPPONENT) | Well of Ideas (2XM 75). "
         "\"That player\" is the player whose draw step it is, recorded on the entry |"),
        ("| `SacrificeSelfAndReturnCardsExiledWithSourceEffect(N)` | `Triggers.endStep(ActivePlayer.ANY)` | Colfenor's "
         "Urn. Intervening-if (`>= N` cards exiled with the source) checked at trigger and resolution;",
         "| `SacrificeSelfAndReturnCardsExiledWithSourceEffect(N)` | `Triggers.endStep(ActivePlayer.ANY)` with "
         "`withInterveningIf(new SourceExiledCardsThreshold(N))` | Colfenor's Urn. The intervening \"if\" (`>= N` cards "
         "exiled with the source) is checked at trigger and resolution (CR 603.4);"),
        ("+ `Triggers.endStep(ActivePlayer.ANY) DrawCardForMonarchEndStepEffect()` | combat-damage trigger + monarch "
         "end-step trigger | Archivist of Gondor (LTC 18). The end-step effect triggers only for the active player who "
         "is monarch and snapshots that player for resolution. |",
         "+ `Triggers.endStep(ActivePlayer.ANY)` with `withEventCondition(new ActivePlayerIsMonarch())` "
         "`DrawCardForMonarchEndStepEffect()` | combat-damage trigger + monarch end-step trigger | Archivist of Gondor "
         "(LTC 18). \"The monarch's end step\" is the event condition; the entry records that player as \"that "
         "player\". |"),
        ("| `PlayerWithMostLifeGainsControlOfSourceCreatureEffect()` | Triggers.upkeep(ActivePlayer.YOU) |",
         "| `PlayerWithMostLifeGainsControlOfSourceCreatureEffect()` | Triggers.upkeep(ActivePlayer.YOU) with "
         "`withInterveningIf(new APlayerHasMoreLifeThanEachOtherPlayer())` |"),
    ],
    "EFFECTS_INDEX.md": [
        ("`APlayerHasMoreCardsInHandThanEachOtherPlayer`,\n",
         "`APlayerHasMoreCardsInHandThanEachOtherPlayer`, `APlayerHasMoreLifeThanEachOtherPlayer` (Ghazb\u00e1n Ogre, Wild "
         "Dogs), `FirstUpkeepThisTurn` (not an additional upkeep: Paradox Haze's event condition), "
         "`ActivePlayerIsMonarch` (\"the monarch's end step\": Archivist of Gondor's event condition), "
         "`SourceCardInExileWithScreamCounter` (All Hallow's Eve's intervening \"if\"),\n"),
        ("| `DealDamageIfFewCardsInHandEffect` | `(int maxCards, int damage)` | deal N damage to target player if they "
         "have maxCards or fewer in hand |\n", ""),
        ("For vanishing, gate the upkeep trigger on a remaining time counter;",
         "For vanishing, the upkeep trigger's intervening \"if\" is `SourceCounterThreshold(1, TIME)` (CR 702.63a);"),
        ("`(DynamicAmount amount, boolean requireSourceUntapped, boolean targetsPlayer[, int targetGroup]"
         "[, boolean opponentDrawStepOnly][, boolean triggeringCardOwner])`",
         "`(DynamicAmount amount, boolean requireSourceUntapped, boolean targetsPlayer[, int targetGroup]"
         "[, boolean triggeringCardOwner])`"),
        (" `forOpponentDrawStep(amount)` marks an `Triggers.drawStep(ActivePlayer.ANY)` instance as applying only during "
         "an opponent's draw step.",
         " \"Each opponent's draw step\" is `Triggers.drawStep(ActivePlayer.OPPONENT)` (Well of Ideas)."),
        ("| `DrawCardForMonarchEndStepEffect` | `()` | `Triggers.endStep(ActivePlayer.ANY)` marker: only triggers during "
         "the monarch's end step, snapshots that active player as the stack entry target, and makes that player draw "
         "one card",
         "| `DrawCardForMonarchEndStepEffect` | `()` | \"that player draws a card\" of \"at the beginning of the monarch's "
         "end step\": `Triggers.endStep(ActivePlayer.ANY)` with `withEventCondition(new ActivePlayerIsMonarch())`; the "
         "entry records the end-step player as \"that player\", who draws one card"),
        ("Triggers.endStep(ActivePlayer.ANY): intervening-if � if `>= minCount` cards have been exiled with the source "
         "permanent, sacrifice it",
         "Triggers.endStep(ActivePlayer.ANY) with the intervening \"if\" `SourceExiledCardsThreshold(minCount)`: "
         "sacrifice the source"),
        ("Condition re-checked at trigger (StepTriggerService) and resolution. Used by Colfenor's Urn (`3`)",
         "The effect checks the count again as it resolves. Used by Colfenor's Urn (`3`)"),
        ("no-op on a tie for most life (intervening-if). GhazbA?n Ogre (`Triggers.upkeep(ActivePlayer.YOU)`) |",
         "no-op on a tie for most life. GhazbA?n Ogre, Wild Dogs (`Triggers.upkeep(ActivePlayer.YOU)` with the "
         "intervening \"if\" `APlayerHasMoreLifeThanEachOtherPlayer`) |"),
        ("| `DrawPerSourceCounterThenDamageEffect` | `(CounterType counterType)` | Triggers.drawStep(ActivePlayer.ANY):",
         "| `DrawPerSourceCounterThenDamageEffect` | `(CounterType counterType)` | "
         "Triggers.drawStep(ActivePlayer.OPPONENT):"),
        (" Implements `OpponentDrawStepOnlyEffect`, so `StepTriggerService` skips the trigger on the controller's own "
         "draw step (\"each opponent's draw step\").", ""),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `ControllerIsMonarch()` / `NoMonarch()` - conditions for checking whether the source controller is the "
         "monarch or whether no player is currently the monarch.",
         "- `ControllerIsMonarch()` / `NoMonarch()` / `ActivePlayerIsMonarch()` - conditions for checking whether the "
         "source controller is the monarch, whether no player is currently the monarch, or whether the player whose "
         "turn it is is the monarch (\"at the beginning of the monarch's end step\", an event condition)."),
        ("No-ops when the source has no such counters, matching vanishing (Calciderm)",
         "No-ops when the source has no such counters; vanishing's upkeep trigger has the intervening \"if\" "
         "`SourceCounterThreshold(1, TIME)` (CR 702.63a)"),
        ("- `DrawCardForMonarchEndStepEffect()` — `Triggers.endStep(ActivePlayer.ANY)`: the monarch draws one card at "
         "the beginning of their end step; `StepTriggerService` snapshots the active end-step player and only queues "
         "the effect when that player is the monarch.",
         "- `DrawCardForMonarchEndStepEffect()` — `Triggers.endStep(ActivePlayer.ANY)` with `withEventCondition(new "
         "ActivePlayerIsMonarch())`: the monarch draws one card at the beginning of their end step, the entry's "
         "\"that player\"."),
        ("`DrawCardForTargetPlayerEffect(DynamicAmount, boolean requiresUntapped, boolean targets[, int targetGroup]"
         "[, boolean opponentDrawStepOnly])`",
         "`DrawCardForTargetPlayerEffect(DynamicAmount, boolean requiresUntapped, boolean targets[, int targetGroup])`"),
        ("`forOpponentDrawStep(amount)` restricts an `Triggers.drawStep(ActivePlayer.ANY)` draw to opponents' draw "
         "steps;",
         "\"each opponent's draw step\" is `Triggers.drawStep(ActivePlayer.OPPONENT)`;"),
        ("- `DrawPerSourceCounterThenDamageEffect(CounterType)` — Triggers.drawStep(ActivePlayer.ANY):",
         "- `DrawPerSourceCounterThenDamageEffect(CounterType)` — Triggers.drawStep(ActivePlayer.OPPONENT):"),
        ("; Malignant Growth (`GROWTH`). Implements the `OpponentDrawStepOnlyEffect` marker so the trigger is skipped "
         "entirely on the controller's own draw step",
         "; Malignant Growth (`GROWTH`)"),
        ("No-op on a tie for most life (intervening-if)",
         "No-op on a tie for most life; the trigger's intervening \"if\" is `APlayerHasMoreLifeThanEachOtherPlayer`"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("Triggers.endStep(ActivePlayer.ANY) `DrawCardForMonarchEndStepEffect()` — a `MonarchEndStepTriggeredEffect` "
         "snapshots the active end-step player and only triggers when that player is the monarch;",
         "Triggers.endStep(ActivePlayer.ANY) with `withEventCondition(new ActivePlayerIsMonarch())` "
         "`DrawCardForMonarchEndStepEffect()` — the entry records the end-step player as \"that player\";"),
    ],
    "CARD_PATTERNS_CREATURES_ETB.md": [
        ("| Triggers.upkeep(ActivePlayer.YOU) PlayerWithMostLifeGainsControlOfSourceCreatureEffect —",
         "| Triggers.upkeep(ActivePlayer.YOU) with `withInterveningIf(new APlayerHasMoreLifeThanEachOtherPlayer())` "
         "PlayerWithMostLifeGainsControlOfSourceCreatureEffect —"),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("For \"each **opponent's** draw step\", use an effect implementing `OpponentDrawStepOnlyEffect` with "
         "`opponentDrawStepOnly()` true — `StepTriggerService` then skips the trigger on the controller's own draw step "
         "(Malignant Growth, Well of Ideas) |",
         "For \"each **opponent's** draw step\", use `Triggers.drawStep(ActivePlayer.OPPONENT)` (Malignant Growth, Well "
         "of Ideas) |"),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("| `Triggers.upkeep(ActivePlayer.YOU) from exile` | The card owner's upkeep while it is exiled with a positive "
         "scream-counter entry;",
         "| `Triggers.upkeep(ActivePlayer.YOU) from exile` with `withInterveningIf(new "
         "SourceCardInExileWithScreamCounter())` | The card owner's upkeep while it is exiled with a scream counter "
         "(the intervening \"if\", checked again as it resolves);"),
    ],
}

GONE = ["DealDamageIfFewCardsInHandEffect", "OpponentDrawStepOnlyEffect", "MonarchEndStepTriggeredEffect",
        "forOpponentDrawStep", "opponentDrawStepOnly"]


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if old in text:
                text = text.replace(old, new, 1)
            elif new and new in text:
                continue
            elif not new:
                continue
            else:
                raise SystemExit(f"{name}: no match for {old[:70]!r}")
        path.write_text(text)
    for path in DOCS.glob("*.md"):
        if path.name == "EFFECT_MERGE_PLAN.md":
            continue
        text = path.read_text()
        for gone in GONE:
            if gone in text:
                raise SystemExit(f"{path.name} still names {gone}")


if __name__ == "__main__":
    main()
