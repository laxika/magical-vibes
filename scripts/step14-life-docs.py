#!/usr/bin/env python3
"""Step 14, run 13 of effect-slot-refactor.md: rewrites the agent-docs passages that still show a life ability's
trigger-only wrappers (exact replacements; a passage already rewritten is skipped, one that is missing stops the
script). Running it again changes nothing.

Usage: python3 scripts/step14-life-docs.py [--dry-run]
"""
import sys

DOCS = "agent-docs/"

REPLACEMENTS = [
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| `OncePerTurnTriggerEffect.markOnAcceptance(NykthosParagonLifeGainEffect())` | Triggers.youGainLife() |",
     "| `NykthosParagonLifeGainEffect()` | Triggers.youGainLife(), "
     "`TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN)` |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| `FirstOpponentLifeLossEachTurnTriggerEffect(List.of(new PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE), "
     "new DrawCardEffect(1)))` | Triggers.opponentLosesLife() |",
     "| `PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE)`, `DrawCardEffect(1)` | "
     "Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn() |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| `OncePerTurnTriggerEffect(ReturnSourceCardFromGraveyardToOwnerHandEffect())` | "
     "`Triggers.youGainLife() from the graveyard` |",
     "| `ReturnSourceCardFromGraveyardToOwnerHandEffect()` | "
     "`Triggers.youGainLife().firstTimeEachTurn()` from the graveyard |"),
    ("EFFECTS_QUICK_REFERENCE.md",
     "independently for each opponent of each source permanent in `Triggers.opponentLosesLife()` "
     "(Valgavoth, Harrower of Souls)",
     "independently for each opponent of each source permanent in `Triggers.opponentLosesLife()` "
     "(Valgavoth, Harrower of Souls). Not authored on a card: the legacy view builds it from "
     "`Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn()`"),
    ("EFFECTS_QUICK_REFERENCE.md",
     "on first fire (Deathless Knight; tap triggers (Interface Ace, Nanoform Sentinel, Sharae of Numbing Depths)",
     "on first fire (tap and life triggers (Interface Ace, Nanoform Sentinel, Sharae of Numbing Depths, "
     "Moonstone Harbinger, MACH-1)"),
    ("EFFECTS_QUICK_REFERENCE.md",
     "- `OncePerTurnTriggerEffect.firstLifeGain(CardEffect)` — `Triggers.youGainLife()` variant for the first life "
     "gain event of the turn, including life gained before the source entered (Attended Healer).",
     "- `OncePerTurnTriggerEffect.firstLifeGain(CardEffect)` — `Triggers.youGainLife()` variant for the first life "
     "gain event of the turn, including life gained before the source entered (Attended Healer). Not authored on a "
     "card: the legacy view builds it, keyed by the ability, from `Triggers.youGainLife().firstTimeEachTurn()`, and "
     "`firstLifeLoss` from `Triggers.youLoseLife().firstTimeEachTurn()`."),
    ("EFFECTS_INDEX.md",
     "in `Triggers.opponentLosesLife()`; Valgavoth, Harrower of Souls uses it with a +1/+1 counter and a card draw. |",
     "in `Triggers.opponentLosesLife()`; Valgavoth, Harrower of Souls. Not authored on a card: the legacy view builds "
     "it from `Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn()` and the ability's effects. |"),
    ("EFFECTS_INDEX.md",
     "Used by Deathless Knight; tap triggers (Interface Ace, Nanoform Sentinel) and creature-death triggers",
     "Tap and life triggers (Interface Ace, Nanoform Sentinel, Moonstone Harbinger) and creature-death triggers"),
    ("EFFECTS_INDEX.md",
     "`firstLifeGain(wrapped)` gates controller life gain by the turn's first life gain event, including gains "
     "before entry (Attended Healer). |",
     "`firstLifeGain(wrapped)` gates controller life gain by the turn's first life gain event, including gains "
     "before entry (Attended Healer); not authored on a card: the legacy view builds it, and `firstLifeLoss`, from "
     "`Triggers.youGainLife().firstTimeEachTurn()` / `youLoseLife().firstTimeEachTurn()`. |"),
    ("EFFECTS_INDEX.md",
     "Place it in `Triggers.opponentLosesLife()` for an opponent-only wording or also in `Triggers.youLoseLife()` "
     "when the controller can lose life.",
     "Use `Triggers.opponentLosesLife()` for an opponent-only wording, or one ability with "
     "`Triggers.opponentLosesLife()` and `Triggers.youLoseLife()` for \"whenever a player loses life\"."),
    ("EFFECTS_INDEX.md",
     "used inside `OncePerTurnTriggerEffect.markOnAcceptance(...)` so the source is marked only when counters are "
     "actually accepted.",
     "with `TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN)` so the source is marked only when "
     "counters are actually accepted."),
    ("CARD_PATTERNS_CREATURES_TRIGGERED.md",
     "`Triggers.opponentLosesLife()` and, for wording that includes the controller, `Triggers.youLoseLife()`, both "
     "using `MillOpponentOnLifeLossEffect`",
     "`Triggers.opponentLosesLife()` and, for wording that includes the controller, one ability with "
     "`Triggers.youLoseLife()` too (`addTrigger(List.of(Triggers.opponentLosesLife(), Triggers.youLoseLife()), …)`), "
     "using `MillOpponentOnLifeLossEffect`"),
]


def main():
    dry = "--dry-run" in sys.argv
    done = 0
    for name, old, new in REPLACEMENTS:
        path = DOCS + name
        src = open(path).read()
        if src.count(new) == 1:
            continue
        if src.count(old) != 1:
            raise SystemExit("%s: the passage isn't there once: %s" % (name, old[:80]))
        src = src.replace(old, new)
        done += 1
        if not dry:
            open(path, "w").write(src)
    print("%d passages" % done)


if __name__ == "__main__":
    main()
