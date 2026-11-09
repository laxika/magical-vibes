#!/usr/bin/env python3
"""Step 15, run 17z9: the agent-docs rows of the cast triggers' "that player", the punisher effects' trigger filters and
"target opponent or planeswalker" of a dealt-damage trigger.

The unfiltered cast triggers whose class-keyed handlers bound the caster as the entry's `targetId` are filtered cast
triggers (`anySpell()`, or the spell filter the effect carried as `withFilter`) that the generic cast collector puts on
the stack with the caster as the non-targeting player; the "target player" recipients that meant "that player" are
`TRIGGERING_PLAYER`. `LoseLifeUnlessPaysEffect`, `DamageUnlessPaysEffect` and `DrawCardUnlessPaysEffect` lost their
`spellFilter`. Horn of Greed's opponent half goes through the land-play default. A dealt-damage "target opponent or
planeswalker" is always chosen as the ability goes on the stack.

Idempotent (whole-fragment edits); fails if a doc or skill still names a removed form outside a "Gone" line.

Usage: step15-run17z9-docs.py
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

GENERIC = ("the generic cast collector records the caster as the entry's non-targeting player (`targetId`, which "
           "the `TRIGGERING_PLAYER` recipients read)")

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| `DrawCardForTargetPlayerEffect(N)` | `Triggers.opponentCastsSpell()` | Forced Fruition (N=7). Collector sets "
         "`targetId` to the casting opponent, who draws |",
         "| `DrawCardForTargetPlayerEffect(N)` | `Triggers.opponentCastsSpell().anySpell()` | Forced Fruition (N=7). "
         + GENERIC[0].upper() + GENERIC[1:] + ", who draws |"),
        ("| `MillEffect(N, MillRecipient.TARGET_PLAYER)` | `Triggers.opponentCastsSpell()` | Memory Erosion (N=2). "
         "Collector stamps `targetId` to the casting opponent (no chosen target); reuses `MillEffectHandler`'s "
         "`TARGET_PLAYER` branch |",
         "| `MillEffect(N, MillRecipient.TRIGGERING_PLAYER)` | `Triggers.opponentCastsSpell().anySpell()` | Memory "
         "Erosion (N=2). " + GENERIC[0].upper() + GENERIC[1:] + "; no target is chosen |"),
        ("| `Triggers.playerCastsSpell()` | Mana Breach. Collector stamps the casting player onto `targetId`;",
         "| `Triggers.playerCastsSpell().anySpell()` | Mana Breach. " + GENERIC[0].upper() + GENERIC[1:] + ";"),
        ("| `DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, false)` | `Triggers.playerCastsSpell()` | Oppression. "
         "Dedicated collector stamps the casting player onto `targetId` (marks `nonTargeting`) so the `TARGET_PLAYER` "
         "discard lands on whoever cast the spell, not the enchantment's controller.",
         "| `DiscardEffect(1, DiscardRecipient.TRIGGERING_PLAYER, false)` | `Triggers.playerCastsSpell().anySpell()` | "
         "Oppression. " + GENERIC[0].upper() + GENERIC[1:] + ", so the discard lands on whoever cast the spell, not "
         "the enchantment's controller."),
        ("| `LoseLifeUnlessPaysEffect(N, M, new CardTypePredicate(CardType.CREATURE))` | `Triggers.opponentCastsSpell()` "
         "| Isolation Cell (2, 2). Casting opponent chooses pay-mana vs lose-life; can't-pay auto-loses. `spellFilter` "
         "null = any spell |",
         "| `LoseLifeUnlessPaysEffect.forTriggeringPlayer(N, M)` | `Triggers.opponentCastsSpell().withFilter(new "
         "CardTypePredicate(CardType.CREATURE))` | Isolation Cell (2, 2). The spells that trigger it are the trigger's "
         "filter; " + GENERIC + ". Casting opponent chooses pay-mana vs lose-life; can't-pay auto-loses |"),
        ("| `DamageUnlessPaysEffect(N, M, new CardTypePredicate(CardType.CREATURE))` | `Triggers.opponentCastsSpell()` "
         "| Soul Barrier (2, 2).",
         "| `DamageUnlessPaysEffect(N, M)` | `Triggers.opponentCastsSpell().withFilter(new "
         "CardTypePredicate(CardType.CREATURE))` | Soul Barrier (2, 2). The spells that trigger it are the trigger's "
         "filter; " + GENERIC + "."),
        ("| `DrawCardUnlessPaysEffect(1, M, new CardNotPredicate(new CardTypePredicate(CREATURE)))` | "
         "`Triggers.opponentCastsSpell()` | Mystic Remora (1, 4).",
         "| `DrawCardUnlessPaysEffect(1, M)` | `Triggers.opponentCastsSpell().withFilter(new CardNotPredicate(new "
         "CardTypePredicate(CREATURE)))` | Mystic Remora (1, 4). " + GENERIC[0].upper() + GENERIC[1:] + "."),
        ("Rhystic Study = `(1, 1, null)` (any spell) |",
         "Rhystic Study = `(1, 1)` on `Triggers.opponentCastsSpell().anySpell()` |"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `LoseLifeUnlessPaysEffect(int lifeLoss, int payAmount, CardPredicate spellFilter[, boolean "
         "controllerGainsLifeLost])` — `Triggers.opponentCastsSpell()` punisher: casting opponent loses `lifeLoss` life "
         "unless they pay `{payAmount}` (filter null = any).",
         "- `LoseLifeUnlessPaysEffect(int lifeLoss, int payAmount[, boolean controllerGainsLifeLost])`, "
         "`forTriggeringPlayer(lifeLoss, payAmount)` — punisher: that player loses `lifeLoss` life unless they pay "
         "`{payAmount}`; on a filtered cast trigger (`Triggers.opponentCastsSpell().withFilter(…)`, the filter is the "
         "spells that trigger it) the `forTriggeringPlayer` form makes the caster that player."),
        ("Isolation Cell (2, 2, CREATURE); Rhystic Syphon (5, 3, drain)",
         "Isolation Cell (`forTriggeringPlayer(2, 2)`, creature-spell filter); Rhystic Syphon (5, 3, drain)"),
        ("- `DamageUnlessPaysEffect(int damage, int payAmount, CardPredicate spellFilter)` — "
         "`Triggers.opponentCastsSpell()` punisher,",
         "- `DamageUnlessPaysEffect(int damage, int payAmount)` — filtered cast trigger "
         "(`Triggers.opponentCastsSpell().withFilter(…)`) punisher on the caster,"),
        ("Soul Barrier (2, 2, CREATURE)", "Soul Barrier (2, 2; creature-spell filter)"),
        ("- `DrawCardUnlessPaysEffect(int drawCount, int payAmount, CardPredicate spellFilter)` — "
         "`Triggers.opponentCastsSpell()` Rhystic draw (Mystic Remora `(1, 4, noncreature)`, Rhystic Study "
         "`(1, 1, null)`).",
         "- `DrawCardUnlessPaysEffect(int drawCount, int payAmount)` — filtered cast trigger Rhystic draw (Mystic Remora "
         "`(1, 4)` on `Triggers.opponentCastsSpell().withFilter(noncreature)`, Rhystic Study `(1, 1)` on "
         "`Triggers.opponentCastsSpell().anySpell()`; the caster is the entry's non-targeting player)."),
    ],
    "EFFECTS_INDEX.md": [
        ("Only `OPPONENT` supports the Triggers.dealtDamage() ability's auto-targeting (opponent when no planeswalkers, "
         "otherwise a target prompt) via `DamageTriggerCollectorService` + `CombatDamageService`; the `ANY` form queues "
         "as an ordinary trigger.",
         "On a `Triggers.dealtDamage()` ability the `OPPONENT` form's target is chosen as the ability goes on the stack "
         "(`DamageTriggerCollectorService`, `CombatDamageService`), among the opponents and planeswalkers (the card's "
         "target filter, else \"opponent or planeswalker\"), even when only one is legal; the `ANY` form queues as an "
         "ordinary trigger."),
        ("Spell-cast trigger collector (`Triggers.playerCastsSpell()`) stamps the casting player onto `targetId` (Mana "
         "Breach, `PermanentIsLandPredicate`);",
         "On a filtered cast trigger (`Triggers.playerCastsSpell().anySpell()`) the generic cast collector records the "
         "caster as the entry's non-targeting `targetId` (Mana Breach, `PermanentIsLandPredicate`);"),
        ("| `LoseLifeUnlessPaysEffect` | `(int lifeLoss, int payAmount, CardPredicate spellFilter[, boolean "
         "controllerGainsLifeLost])` | target player loses N life unless they pay {M}. Optional `spellFilter` restricts "
         "which spells trigger it (null = any); optional drain flag",
         "| `LoseLifeUnlessPaysEffect` | `(int lifeLoss, int payAmount[, boolean controllerGainsLifeLost])`, "
         "`forTriggeringPlayer(lifeLoss, payAmount)` | target player (or, `forTriggeringPlayer`, the entry's "
         "non-targeting player: a cast trigger's caster) loses N life unless they pay {M}. The spells that trigger a "
         "cast trigger are its filter (`Triggers.opponentCastsSpell().withFilter(…)`); optional drain flag"),
        ("Place in `Triggers.opponentCastsSpell()` ability. If player can't pay, auto-applies the penalty.",
         "If player can't pay, auto-applies the penalty."),
        ("| `DamageUnlessPaysEffect` | `(int damage, int payAmount, CardPredicate spellFilter)` |",
         "| `DamageUnlessPaysEffect` | `(int damage, int payAmount)` |"),
        ("`DamageDealingEffect`. Optional `spellFilter` (null = any). Place in `Triggers.opponentCastsSpell()` ability.",
         "`DamageDealingEffect`. Place in a filtered cast trigger (`Triggers.opponentCastsSpell().withFilter(…)`, the "
         "spells that trigger it), whose caster is the entry's non-targeting player."),
        ("Convenience ctor `(int damage, int payAmount)`. Soul Barrier (2, {2}, creature spells) |",
         "Soul Barrier (2, {2}; creature-spell filter) |"),
        ("| `DrawCardUnlessPaysEffect` | `(int drawCount, int payAmount, CardPredicate spellFilter)` |",
         "| `DrawCardUnlessPaysEffect` | `(int drawCount, int payAmount)`, `(int drawCount, DynamicAmount "
         "dynamicPayAmount)`, `mandatory(…)` |"),
        ("`CardDrawingEffect`. Optional `spellFilter` (null = any). Place in `Triggers.opponentCastsSpell()`.",
         "`CardDrawingEffect`. Place in a filtered cast trigger (`Triggers.opponentCastsSpell().anySpell()` or "
         "`.withFilter(…)`), whose caster is the entry's non-targeting player."),
        ("Convenience ctor `(drawCount, payAmount)`. Mystic Remora (1, 4, noncreature); Rhystic Study (1, 1, null) |",
         "Mystic Remora (1, 4; noncreature filter); Rhystic Study (1, 1; `anySpell()`) |"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| `p/PainfulQuandary.java` | Triggers.opponentCastsSpell() LoseLifeUnlessDiscardEffect(5) —",
         "| `p/PainfulQuandary.java` | Triggers.opponentCastsSpell().anySpell() LoseLifeUnlessDiscardEffect(5) (the caster "
         "the entry's non-targeting player) —"),
        ("| `i/IsolationCell.java` | Triggers.opponentCastsSpell() LoseLifeUnlessPaysEffect(2, 2, "
         "CardTypePredicate(CREATURE)) — filtered to creature spells only,",
         "| `i/IsolationCell.java` | Triggers.opponentCastsSpell().withFilter(CardTypePredicate(CREATURE)) "
         "LoseLifeUnlessPaysEffect.forTriggeringPlayer(2, 2) — filtered to creature spells only,"),
        ("| `s/SoulBarrier.java` | Triggers.opponentCastsSpell() DamageUnlessPaysEffect(2, 2, CardTypePredicate(CREATURE))",
         "| `s/SoulBarrier.java` | Triggers.opponentCastsSpell().withFilter(CardTypePredicate(CREATURE)) "
         "DamageUnlessPaysEffect(2, 2)"),
    ],
}

GONE = ["LoseLifeUnlessPaysEffect(2, 2, CardTypePredicate", "DamageUnlessPaysEffect(2, 2, CardTypePredicate",
        "DamageUnlessPaysEffect(int damage, int payAmount, CardPredicate spellFilter)",
        "DrawCardUnlessPaysEffect(int drawCount, int payAmount, CardPredicate spellFilter)",
        "CardPredicate spellFilter[, boolean controllerGainsLifeLost]",
        "DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, false)` | `Triggers.playerCastsSpell()`",
        "auto-targeting (opponent when no planeswalkers"]


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: expected exactly one occurrence of: {old[:100]}")
            text = text.replace(old, new)
        path.write_text(text)
        print(f"{name}: up to date")
    stale = []
    for path in list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md")):
        for number, line in enumerate(path.read_text().splitlines(), 1):
            if "Gone" not in line and any(form in line for form in GONE):
                stale.append(f"{path.relative_to(ROOT)}:{number}")
    if stale:
        raise SystemExit("a removed form is still named: " + ", ".join(stale))


if __name__ == "__main__":
    sys.exit(main())
