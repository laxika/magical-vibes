#!/usr/bin/env python3
"""Step 15, run 17z2: the agent-docs lines that name the removed dying-controller, life-gain and crew
trigger-condition records (`DyingCreatureControllerDiscardsCardEffect`, `DyingCreatureControllerSacrificesPermanentsEffect`,
`DyingCreatureControllerMayDrawCardEffect`, `DealDamageOnSpellLifeGainEffect`,
`PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect`), the new `DiscardRecipient.TRIGGERING_PERMANENT_CONTROLLER`,
`LifeTrigger.causedBySpell` and `ObjectActionTrigger.withObjectFilter`, and `DiscardCardThenEffect`'s `useEntryTarget`,
which no collector binds any more.

Whole-line edits keyed by the line's start (the old line is replaced, or a new line inserted after an anchor line),
or a fragment replaced inside a line, so a second run changes nothing. Fails if a doc or skill still names a removed
record outside a "Gone" line.

Usage: step15-run17z2-docs.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
RUN = "effect-slot-refactor.md step 15, run 17z2"
QR = "agent-docs/EFFECTS_QUICK_REFERENCE.md"
IX = "agent-docs/EFFECTS_INDEX.md"
OM = "agent-docs/ORACLE_TEXT_EFFECT_MAP.md"
CI = "agent-docs/CARD_PATTERN_INDEX.md"
CP = "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md"
TS = "agent-docs/TRIGGER_SLOT_TARGETING.md"
AG = "agent-docs/ACTIVATED_ABILITY_GUIDE.md"
LS = "agent-docs/LAYER_SYSTEM.md"

DISCARD = "`DiscardEffect(1, DiscardRecipient.TRIGGERING_PERMANENT_CONTROLLER)`"
SACRIFICE = ("`SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), "
             "SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER)`")
DRAW = ("`MayEffect(new DrawCardForTriggeringPermanentControllerEffect(), \"Draw a card?\", null, "
        "MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER)`")
FIRESONG = ("`Triggers.youGainLife().causedBySpell(new CardAllOfPredicate(List.of(new CardColorPredicate(WHITE), "
            "instant or sorcery)))` + `DealDamageToTargetCreatureOrPlayerEffect(3)`")
TIANA = ("`Triggers.creatureYouControlCrewsVehicle().withObjectFilter(new PermanentAnyOfPredicate(List.of(new "
         "PermanentIsSourceCardPredicate(), new PermanentHasSupertypePredicate(LEGENDARY))))` + "
         "`PerpetuallyBoostCardEffect.ofTriggeringPermanent(1, 0)`")
DYING = ("the death collector records the creature that died as the entry's triggering permanent and its controller "
         "as it died, so \"its / that creature's controller\" is read from the entry as the ability resolves (as the "
         "creature last existed on the battlefield, CR 608.2h)")
SPELL_GAIN = ("\"whenever a [filter] spell causes you to gain life\" is the trigger's `causedBySpell(CardPredicate)`, "
              "judged by the \"you gain life\" collector as the ability triggers: the life gain's source is a spell "
              "(the spell gaining the life as it resolves, or its lifelink damage), not a triggered or activated "
              "ability of its card, and its card matches the filter")
CREW_FILTER = ("\"when this or another [filter] creature you control crews a Vehicle\" is the trigger's "
               "`withObjectFilter(PermanentPredicate)` on the crewing creature, judged from the ability's source as the "
               "crew cost is paid; the effects go on the stack as written with the Vehicle as the entry's triggering "
               "permanent (\"that Vehicle\")")

EDITS = [
    (OM, "| \"When Tiana, Angelic Mechanic or another legendary creature you control crews a Vehicle,",
     "| \"When Tiana, Angelic Mechanic or another legendary creature you control crews a Vehicle, that Vehicle "
     "perpetually gets +1/+0.\" | `PerpetuallyBoostCardEffect.ofTriggeringPermanent(1, 0)` | "
     "`Triggers.creatureYouControlCrewsVehicle().withObjectFilter(new PermanentAnyOfPredicate(List.of(new "
     "PermanentIsSourceCardPredicate(), new PermanentHasSupertypePredicate(LEGENDARY))))` | Tiana, Angelic Mechanic "
     "(YDMU 28); the crewed Vehicle is the entry's triggering permanent, whose card as it was then gets the perpetual "
     "modifier, which survives zone changes. |", "line"),
    (OM, "| \"Whenever a creature dies, that creature's controller may draw a card\" |",
     "| \"Whenever a creature dies, that creature's controller may draw a card\" | " + DRAW + " | "
     "`Triggers.creatureDies(ANY)` | Fecundity. The dying creature's controller (may be an opponent) chooses as the "
     "ability resolves (CR 603.5) and draws; " + DYING + " |", "line"),
    (OM, "| \"Whenever a green creature dies, its controller discards a card\" |",
     "| \"Whenever a green creature dies, its controller discards a card\" | " + DISCARD + " | "
     "`Triggers.creatureDies(new PermanentColorInPredicate(Set.of(GREEN)), ANY)` | Bereavement. Mandatory discard by "
     "the DYING creature's controller (may be an opponent); " + DYING + ". The trigger's filter restricts it to "
     "creatures that were green as they died |", "line"),
    (OM, "| \"Whenever a creature dies, that creature's controller sacrifices a land of their choice\" |",
     "| \"Whenever a creature dies, that creature's controller sacrifices a land of their choice\" | " + SACRIFICE
     + " | `Triggers.creatureDies(ANY)` | Earthlink, Burning Sands. Mandatory sacrifice by the DYING creature's "
     "controller (may be an opponent); " + DYING + " |", "line"),
    (OM, "| \"Whenever a creature dies, that creature's controller sacrifices a land of their choice\" |",
     "| \"Whenever a white instant or sorcery spell causes you to gain life, this deals 3 damage to target creature or "
     "player.\" | `DealDamageToTargetCreatureOrPlayerEffect(3)` | `Triggers.youGainLife().causedBySpell(...)` | "
     "Firesong and Sunspeaker (DOM 280); " + SPELL_GAIN + ". The target is chosen as the ability goes on the stack |",
     "after"),
    (QR, "- `PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect(PermanentPredicate, int, int)` -",
     "- `PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect` — **Gone** (" + RUN + "): Tiana, Angelic Mechanic is "
     + TIANA + "; " + CREW_FILTER + ".", "line"),
    (QR, "- `DyingCreatureControllerMayDrawCardEffect()` —",
     "- `DyingCreatureControllerMayDrawCardEffect` — **Gone** (" + RUN + "): Fecundity is " + DRAW + "; " + DYING + ".",
     "line"),
    (QR, "- `DyingCreatureControllerDiscardsCardEffect()` —",
     "- `DyingCreatureControllerDiscardsCardEffect` — **Gone** (" + RUN + "): Bereavement is " + DISCARD + "; " + DYING
     + ".", "line"),
    (QR, "- `DyingCreatureControllerSacrificesPermanentsEffect(int count, PermanentPredicate filter)` —",
     "- `DyingCreatureControllerSacrificesPermanentsEffect` — **Gone** (" + RUN + "): Earthlink and Burning Sands are "
     + SACRIFICE + "; " + DYING + ".", "line"),
    (QR, "- `DyingCreatureControllerSacrificesPermanentsEffect` — **Gone**",
     "- `DealDamageOnSpellLifeGainEffect` — **Gone** (" + RUN + "): Firesong and Sunspeaker is " + FIRESONG + "; "
     + SPELL_GAIN + ".", "after"),
    (QR, "- `Triggers.youGainLife().firstTimeEachTurn()` / `Triggers.youLoseLife().firstTimeEachTurn()` —",
     "- `Triggers.youGainLife().causedBySpell(CardPredicate)` — " + SPELL_GAIN + " (Firesong and Sunspeaker). Only a "
     "\"you gain life\" trigger takes one; the other life collectors throw on it.", "after"),
    (QR, "- `DiscardEffect(DynamicAmount, DiscardRecipient, boolean random[, CardType stopAfterDiscardingType])` —", "",
     "fragment:`recipient` routes who discards=>`recipient` routes who discards (`TRIGGERING_PERMANENT_CONTROLLER`: the "
     "controller of the entry's triggering permanent, as it last existed on the battlefield if it left — Bereavement's "
     "\"its controller discards a card\")"),
    (QR, "- `DiscardCardThenEffect(CardPredicate, CardEffect, String[, DiscardRecipient][, useEntryTarget])` —", "",
     "fragment:`useEntryTarget` preserves the existing target for the reflexive effect.=>`useEntryTarget` preserves "
     "the target the ability already chose (its own target, chosen as it went on the stack) for the reflexive effect; "
     "no collector binds an event's object as it any more (" + RUN + ")."),
    (IX, "| `PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect` |",
     "| `PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect` | — | **Gone** (" + RUN + "): Tiana, Angelic Mechanic "
     "writes " + TIANA + " |", "line"),
    (IX, "| `DyingCreatureControllerMayDrawCardEffect` |",
     "| `DyingCreatureControllerMayDrawCardEffect` | — | **Gone** (" + RUN + "): Fecundity writes " + DRAW + " |",
     "line"),
    (IX, "| `DyingCreatureControllerDiscardsCardEffect` |",
     "| `DyingCreatureControllerDiscardsCardEffect` | — | **Gone** (" + RUN + "): Bereavement writes " + DISCARD + " |",
     "line"),
    (IX, "| `DyingCreatureControllerSacrificesPermanentsEffect` |",
     "| `DyingCreatureControllerSacrificesPermanentsEffect` | — | **Gone** (" + RUN + "): Earthlink and Burning Sands "
     "write " + SACRIFICE + " |", "line"),
    (IX, "| `DealDamageOnSpellLifeGainEffect` |",
     "| `DealDamageOnSpellLifeGainEffect` | — | **Gone** (" + RUN + "): Firesong and Sunspeaker writes " + FIRESONG
     + " |", "line"),
    (IX, "| `DiscardEffect` |", "",
     "fragment:`DEFENDING_PLAYER` (no own target;=>`TRIGGERING_PERMANENT_CONTROLLER` (no own target; the controller of "
     "the entry's triggering permanent, its current one on the battlefield, else as it last existed there — "
     "Bereavement's \"its controller discards a card\"), `DEFENDING_PLAYER` (no own target;"),
    (IX, "| `DiscardCardThenEffect` recipient overload |", "",
     "fragment:the reflexive effect is pushed with the pre-bound target when `useEntryTarget`=>the reflexive effect is "
     "pushed with the target the ability chose as it went on the stack when `useEntryTarget`"),
    (CI, "| perpetually boost a Vehicle when a matching creature crews it |",
     "| perpetually boost a Vehicle when a matching creature crews it | "
     "`Triggers.creatureYouControlCrewsVehicle().withObjectFilter(...)` + "
     "`PerpetuallyBoostCardEffect.ofTriggeringPermanent(p, t)` |", "line"),
    (CP, "| Any creature dies — its controller may draw (enchantment) |",
     "| Any creature dies — its controller may draw (enchantment) | `f/Fecundity.java` | Triggers.creatureDies(ANY) "
     + DRAW + " — the DYING creature's controller (may be an opponent) chooses and draws |", "line"),
    (CP, "| Any creature dies — its controller sacrifices a land (enchantment) |",
     "| Any creature dies — its controller sacrifices a land (enchantment) | `e/Earthlink.java` | "
     "Triggers.creatureDies(ANY) " + SACRIFICE + " + Triggers.upkeep(ActivePlayer.YOU) "
     "ForcedCostOrElseEffect(PayManaCost(\"{2}\"), List.of(SacrificeSelfEffect()), true) — the DYING creature's "
     "controller sacrifices, not the source's controller |", "line"),
    (TS, "| `Triggers.creatureYouControlCrewsVehicle()` |",
     "| `Triggers.creatureYouControlCrewsVehicle()` | `TriggerCollectionService.checkAllyCreatureCrewsVehicleTriggers` "
     "from `CrewCostHandler`; " + CREW_FILTER + " | Non-targeting |", "line"),
    (AG, "| `Triggers.youGainLife()` | Controller gains life |",
     "| `Triggers.youGainLife().causedBySpell(CardPredicate)` | Controller gains life from a matching spell (Firesong "
     "and Sunspeaker): the spell itself, not an ability of its card |", "after"),
    (LS, "`BoostColorSourceDamageThisTurnEffect`, `DealDamageOnSpellLifeGainEffect`,",
     "`BoostColorSourceDamageThisTurnEffect`, a life-gain trigger's `causedBySpell` filter,", "line"),
]

GONE = ["DyingCreatureControllerDiscardsCardEffect", "DyingCreatureControllerSacrificesPermanentsEffect",
        "DyingCreatureControllerMayDrawCardEffect", "DealDamageOnSpellLifeGainEffect",
        "PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect"]


def apply(lines, start, new, mode):
    idx = [i for i, line in enumerate(lines) if line.startswith(start)]
    if mode == "after":
        if new in lines:
            return False
        if not idx:
            raise SystemExit(f"anchor not found: {start}")
        lines.insert(idx[0] + 1, new)
        return True
    if mode == "delete":
        if not idx:
            return False
        del lines[idx[0]]
        return True
    if mode.startswith("fragment:"):
        old, repl = mode[len("fragment:"):].split("=>", 1)
        if any(repl in line for line in lines) and not any(old in lines[i] for i in idx):
            return False
        for i in idx:
            if repl in lines[i]:
                return False
            if old in lines[i]:
                lines[i] = lines[i].replace(old, repl)
                return True
        raise SystemExit(f"fragment not found under {start}: {old}")
    if new in lines:
        return False
    if not idx:
        raise SystemExit(f"line not found: {start}")
    lines[idx[0]] = new
    return True


def main():
    by_file = {}
    for path, start, new, mode in EDITS:
        by_file.setdefault(path, []).append((start, new, mode))
    for path, edits in by_file.items():
        file = ROOT / path
        lines = file.read_text().split("\n")
        changed = False
        for start, new, mode in edits:
            changed |= apply(lines, start, new, mode)
        if changed:
            file.write_text("\n".join(lines))
            print(f"updated {path}")
    docs = list((ROOT / "agent-docs").glob("*.md")) + list((ROOT / ".claude/skills").rglob("SKILL.md")) \
        + list((ROOT / ".codex/skills").rglob("SKILL.md"))
    for doc in docs:
        for number, line in enumerate(doc.read_text().split("\n"), 1):
            for gone in GONE:
                if re.search(r"\b" + gone + r"\b", line) and "**Gone**" not in line:
                    raise SystemExit(f"{doc.relative_to(ROOT)}:{number} still names {gone}")


if __name__ == "__main__":
    main()
