#!/usr/bin/env python3
"""Step 15, run 17x3: the agent docs for the enter family's values that read the stack entry's triggering permanent.

Rewrites the doc fragments naming a form the run changed: the removed enter-trigger markers the collector rebuilt with
a value of the entering creature (`DrawCardIfEnteringCreatureHasUniqueNameEffect`, `GainLifeEqualToToughnessEffect`,
`GainLifeEqualToPowerEffect`, `PutCountersOnSourceEqualToEnteringPowerEffect`, `IntensifySourceByEnteringPowerEffect`,
`LookAtTopCardsEqualToEnteringPowerPutOneOnTopRestOnBottomEffect`: existing effects with `TriggeringPermanentToughness`
/ `TriggeringPermanentPower`, Guardian Project an intervening "if"), Wild Pair's fieldless record and its intervening
"if", the two amounts read as the ability resolves (and a sentence in `ARCHITECTURE.md`), and the land-tap records
that name their `triggeredEffect()`.

Exact fragment edits: a fragment already rewritten is skipped, so the script is idempotent. It fails if an edit's old
fragment and new fragment are both missing, or if a doc or skill still names a removed effect.

Usage: step15-run17x3-docs.py
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

TOUGHNESS_AMOUNT = (
    "- `TriggeringPermanentToughness()` - \"its / that creature's toughness\": the toughness of the stack entry's "
    "triggering permanent as the ability resolves, or as it last existed on the battlefield (CR 608.2h: the value the "
    "collector recorded as the ability triggered, e.g. a dying creature's, else the departure's), at least 0. Angelic "
    "Chorus's, Trostani's and Orchard Warden's `GainLifeEffect` on an entering creature, Proper Burial's on a dying one\n")

# Table rows dropped whole, by the start of the row (an already-dropped row is skipped).
DROP_ROWS = {
    "EFFECTS_INDEX.md": ["| `GainLifeEqualToToughnessEffect` | `()` |"],
}

EDITS = {
    "ARCHITECTURE.md": [
        ("judged as the ability triggers and again as it resolves (departure snapshots for a creature that left).",
         "judged as the ability triggers and again as it resolves (departure snapshots for a creature that left). "
         "\"That creature's power / toughness\" is the amount `TriggeringPermanentPower` / `TriggeringPermanentToughness`: "
         "the triggering permanent's current value while it is on the battlefield, else the value the collector recorded "
         "as the ability triggered (a dying creature's last-known one), else its departure snapshot; an enter collector "
         "records the entering creature on the entry, never in a rebuilt effect (Archon of Redemption, Hamletback "
         "Goliath, Cream of the Crop)."),
    ],
    "EFFECTS_INDEX.md": [
        ("- `TriggeringPermanentPower()` - the power of the stack entry's triggering permanent as it last existed when the "
         "ability triggered (`triggeringPermanentPowerAtTrigger`), at least 0; 0 when the entry recorded none. Grand "
         "Ossuary's \"a number of +1/+1 counters equal to its power\"\n",
         "- `TriggeringPermanentPower()` - \"its / that creature's power\": the power of the stack entry's triggering "
         "permanent as the ability resolves, or as it last existed on the battlefield (CR 608.2h: the value the collector "
         "recorded as the ability triggered, `triggeringPermanentPowerAtTrigger`, e.g. a dying creature's, else the "
         "departure's), at least 0; 0 when the entry names none. Grand Ossuary's \"a number of +1/+1 counters equal to "
         "its power\"; Archon of Redemption's `GainLifeEffect`, Hamletback Goliath's `PutCountersOnSelfEffect`, Quickbeast "
         "Amulet's `IntensifySourceCardByAmountEffect` and Cream of the Crop's "
         "`LookAtTopCardsEffect.putOneOnTopRestOnBottom(DynamicAmount)` on an entering creature\n" + TOUGHNESS_AMOUNT),
        ("| `SearchLibraryForCreatureWithSameTotalPowerToughnessEffect` | `()` / `(Integer powerAtTrigger, Integer "
         "toughnessAtTrigger)` | `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER)` trigger marker for Wild "
         "Pair. When a creature cast from hand enters, the collector captures its effective power and toughness for "
         "last-known information, offers the ability to its controller, and the normal handler searches that "
         "controller's library for a creature card with exactly the same power-plus-toughness total and puts it onto the "
         "battlefield.",
         "| `SearchLibraryForCreatureWithSameTotalPowerToughnessEffect` | `()` | Wild Pair, inside the card's own "
         "`MayEffect` on `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER)` with the intervening \"if\" "
         "`TriggeringPermanentMatches(PermanentAllOfPredicate(PermanentCastFromZonePredicate(HAND), "
         "PermanentCastBySourceControllerThisTurnPredicate()))` (\"if you cast it from your hand\"). The handler reads the "
         "stack entry's triggering permanent's power plus toughness as it resolves, or as it last existed on the "
         "battlefield (CR 608.2h), searches the controller's library for a creature card with exactly that "
         "power-plus-toughness total and puts it onto the battlefield."),
        ("| `DealDamageOnLandTapEffect` | `(int damage)`, `(int damage, PermanentPredicate landFilter)` or `(int damage, "
         "PermanentPredicate landFilter, boolean tappedLandDeals)` | Triggers.playerTapsLandForMana() trigger: deal N "
         "damage to a player whenever they tap a land for mana (symmetric).",
         "| `DealDamageOnLandTapEffect` | `(int damage)`, `(int damage, PermanentPredicate landFilter)` or `(int damage, "
         "PermanentPredicate landFilter, boolean tappedLandDeals)` | Triggers.playerTapsLandForMana() trigger-condition "
         "record: deal N damage to a player whenever they tap a land for mana (symmetric). The land-tap collector judges "
         "it by class and puts its `triggeredEffect()`, `DealDamageToPlayersEffect(damage, TRIGGERING_PLAYER)`, on the "
         "stack (or resolves it at once inside a triggered mana ability) with the tapping player as the entry's player."),
        ("| `GainLifeWhenOpponentTapsLandOfSubtypeEffect` | `(CardSubtype subtype, int lifeAmount)` | "
         "Triggers.playerTapsLandForMana() trigger: when an opponent taps a land of the given subtype for mana, queues an "
         "optional lifeAmount life gain.",
         "| `GainLifeWhenOpponentTapsLandOfSubtypeEffect` | `(CardSubtype subtype, int lifeAmount)` | "
         "Triggers.playerTapsLandForMana() trigger-condition record: when an opponent taps a land of the given subtype "
         "for mana, the collector puts its `triggeredEffect()`, `MayEffect(GainLifeEffect(lifeAmount))`, on the stack."),
        ("| `OpponentTappedLandDoesntUntapEffect` | `()` | Triggers.playerTapsLandForMana() trigger: when an opponent "
         "taps a land for mana, increments skipUntapCount on the tapped land",
         "| `OpponentTappedLandDoesntUntapEffect` | `()` | Triggers.playerTapsLandForMana() trigger-condition record: "
         "when an opponent taps a land for mana, the collector puts its `triggeredEffect()`, "
         "`SkipNextUntapEffect(TapUntapScope.TRIGGERING)`, on the stack with the land as the entry's triggering "
         "permanent, which increments skipUntapCount on the tapped land"),
        ("| `LookAtTopCardsEqualToEnteringPowerPutOneOnTopRestOnBottomEffect` | `()` | `Triggers.enters(Triggers.CREATURE, "
         "ObjectRelation.ANOTHER_YOU_CONTROL)` trigger-materialising marker: \"you may look at the top X cards where X is "
         "that creature's power, put one on top and the rest on the bottom.\" The ally-enter collector reads the entering "
         "creature's power and queues a `MayEffect(LookAtTopCardsEffect.putOneOnTopRestOnBottom(power))` (power 0 skips "
         "the prompt). Cream of the Crop |\n", ""),
        ("| `GainLifeEqualToPowerEffect` | `()` | enter-trigger marker for gain life equal to the entering creature's "
         "power; the collector materializes it to `GainLifeEffect(new TargetPower())` and preserves the entering "
         "permanent as resolution context so modifiers are included. Used by Archon of Redemption |\n", ""),
        ("| `PutCountersOnSourceEqualToEnteringPowerEffect` | `(int powerModifier, int toughnessModifier, boolean "
         "optional)` | Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER) slot: put N counters on this creature "
         "where N = the entering creature's power (read at trigger time, mirrors `GainLifeEqualToToughnessEffect`). "
         "`optional=true` queues it as a \"you may\". Resolved via `PutCountersOnSourceEffect`. Used by Hamletback Goliath "
         "(`(1, 1, true)`) |\n", ""),
        ("| `IntensifySourceByEnteringPowerEffect` | `()` | Triggers.enters(Triggers.CREATURE, "
         "ObjectRelation.ANOTHER_YOU_CONTROL) marker: the source intensifies (via "
         "`IntensifySourceCardByAmountEffect(ChosenPermanentPower)`) by the entering creature's effective power; the "
         "collector captures last-known power for resolution if the creature leaves. Quickbeast Amulet |\n", ""),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `SearchLibraryForCreatureWithSameTotalPowerToughnessEffect()` — Wild Pair trigger marker; the enter-trigger "
         "collector captures the entering creature's effective power and toughness, offers the controller a may choice, "
         "and delegates the exact-total creature search to `SearchLibraryEffect(..., BATTLEFIELD)` using existing "
         "predicates. The captured values provide last-known information if the entering creature leaves before "
         "resolution.",
         "- `SearchLibraryForCreatureWithSameTotalPowerToughnessEffect()` — Wild Pair, inside the card's `MayEffect` with "
         "the intervening \"if\" `TriggeringPermanentMatches(cast from your hand)`; the handler reads the stack entry's "
         "triggering permanent's power plus toughness as it resolves, or as it last existed (CR 608.2h), and delegates "
         "the exact-total creature search to `SearchLibraryEffect(..., BATTLEFIELD)` using existing predicates."),
        ("- `GainLifeEqualToPowerEffect()` — enter-trigger marker for \"gain life equal to the entering creature's "
         "power\"; the collector materializes it to `GainLifeEffect(new TargetPower())` and preserves the entering "
         "permanent as resolution context so modifiers are included (Archon of Redemption)\n",
         "- \"Gain life equal to that creature's power / toughness\" of an entering creature: `GainLifeEffect(new "
         "TriggeringPermanentPower())` / `GainLifeEffect(new TriggeringPermanentToughness())`, read as the ability "
         "resolves (Archon of Redemption, Angelic Chorus, Orchard Warden in a `MayEffect`)\n"),
        ("- `PutCountersOnSourceEqualToEnteringPowerEffect(int power, int toughness, boolean optional)` — "
         "Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER): put counters on self = entering creature's power; "
         "`optional` = \"you may\" (Hamletback Goliath)\n",
         "- \"You may put X +1/+1 counters on this creature, where X is that creature's power\": "
         "`MayEffect(PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE, new TriggeringPermanentPower()))` (Hamletback Goliath)\n"),
        ("- `IntensifySourceByEnteringPowerEffect()` — Triggers.enters(Triggers.CREATURE, "
         "ObjectRelation.ANOTHER_YOU_CONTROL): intensify the source by the entering creature's effective power, "
         "preserving last-known power if it leaves before resolution (Quickbeast Amulet)\n",
         "- \"This Equipment intensifies by X, where X is that creature's power\": "
         "`IntensifySourceCardByAmountEffect(new TriggeringPermanentPower())` (Quickbeast Amulet)\n"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("`Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER) SearchLibraryForCreatureWithSameTotalPowerToughness"
         "Effect()` | triggered ability | Wild Pair (`PLC`). The collector requires the entering permanent to have been "
         "cast from hand, captures its current effective power and toughness, and the accepted branch reuses",
         "`Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER)` + "
         "`TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentMatches(PermanentAllOfPredicate("
         "PermanentCastFromZonePredicate(HAND), PermanentCastBySourceControllerThisTurnPredicate())))` + "
         "`MayEffect(SearchLibraryForCreatureWithSameTotalPowerToughnessEffect())` | triggered ability | Wild Pair "
         "(`PLC`). The intervening \"if\" is judged as the creature enters and as the ability resolves; the handler reads "
         "the triggering permanent's power and toughness as it resolves (or as it last existed), and the accepted branch "
         "reuses"),
        ("`Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL) IntensifySourceByEnteringPowerEffect()`"
         " + `EquipActivatedAbility(\"{2}\")` | STATIC + trigger | Quickbeast Amulet (YDFT 24). Intensity is the "
         "persistent card-identity value read by `SourceIntensity`; the trigger intensifies the physical card by the "
         "entering creature's effective power and retains its last-known power if that creature leaves before "
         "resolution.",
         "`Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL) "
         "IntensifySourceCardByAmountEffect(new TriggeringPermanentPower())` + `EquipActivatedAbility(\"{2}\")` | STATIC + "
         "trigger | Quickbeast Amulet (YDFT 24). Intensity is the persistent card-identity value read by "
         "`SourceIntensity`; the trigger intensifies the physical card by the triggering creature's power as the ability "
         "resolves, or as it last existed if that creature has left."),
        ("+ `MayEffect(GainLifeEqualToPowerEffect())` | trigger | Archon of Redemption; the collector preserves the "
         "entering permanent as resolution context so `GainLifeEffect(new TargetPower())` includes power modifiers |",
         "+ `MayEffect(GainLifeEffect(new TriggeringPermanentPower()))` | trigger | Archon of Redemption; the power of the "
         "stack entry's triggering permanent as the ability resolves, power modifiers included |"),
        ("| \"whenever an opponent taps a [Subtype] for mana, you may gain N life\" | "
         "`GainLifeWhenOpponentTapsLandOfSubtypeEffect(subtype, N)` | `Triggers.playerTapsLandForMana()` | Sanctimony "
         "(Mountain, 1). Queues a triggered ability for opponents' mana taps of a land with the subtype; the controller "
         "chooses whether to gain life on resolution |",
         "| \"whenever an opponent taps a [Subtype] for mana, you may gain N life\" | "
         "`GainLifeWhenOpponentTapsLandOfSubtypeEffect(subtype, N)` | `Triggers.playerTapsLandForMana()` | Sanctimony "
         "(Mountain, 1). A trigger-condition record: the collector puts its `triggeredEffect()`, "
         "`MayEffect(GainLifeEffect(N))`, on the stack for opponents' mana taps of a land with the subtype; the "
         "controller chooses whether to gain life on resolution |"),
        ("| `LookAtTopCardsEqualToEnteringPowerPutOneOnTopRestOnBottomEffect()` | `Triggers.enters(Triggers.CREATURE, "
         "ObjectRelation.ANOTHER_YOU_CONTROL)` | Cream of the Crop. Trigger-materialising marker (like "
         "`PutCountersOnSourceEqualToEnteringPowerEffect`): the ally-enter collector reads the entering creature's power "
         "and queues a `MayEffect(LookAtTopCardsEffect.putOneOnTopRestOnBottom(power))`. That effect",
         "| `MayEffect(LookAtTopCardsEffect.putOneOnTopRestOnBottom(new TriggeringPermanentPower()))` | "
         "`Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` | Cream of the Crop. X is the stack "
         "entry's triggering creature's power as the ability resolves. The look"),
        ("| `PutCountersOnSourceEqualToEnteringPowerEffect(1, 1, true)` | `Triggers.enters(Triggers.CREATURE, "
         "ObjectRelation.ANOTHER)` | Hamletback Goliath. Entering creature's power read at trigger time; `optional=true` "
         "= \"you may\". Set `optional=false` for a mandatory variant. |",
         "| `MayEffect(PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new TriggeringPermanentPower()), prompt)` | "
         "`Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER)` | Hamletback Goliath. X is the entering creature's "
         "power as the ability resolves, or as it last existed (CR 608.2h); drop the `MayEffect` for a mandatory "
         "variant. |"),
    ],
    "CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL) "
         "`DrawCardIfEnteringCreatureHasUniqueNameEffect()` — checks the entering creature's name against other "
         "controlled creatures and creature cards in the controller's graveyard both at trigger creation and resolution |",
         "Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL) with the intervening \"if\" "
         "`TriggeringPermanentMatches(PermanentNotPredicate(PermanentSharesNameWithControlledCreatureOrGraveyardCreature"
         "Predicate()))` and `DrawCardEffect()` — judged against the entering creature as it enters and again as the "
         "ability resolves (as it last existed if it has left, CR 603.4) |"),
        ("`Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL) IntensifySourceByEnteringPowerEffect()`",
         "`Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL) "
         "IntensifySourceCardByAmountEffect(new TriggeringPermanentPower())`"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("+ `MayEffect(GainLifeEqualToPowerEffect())` — the collector preserves the entering permanent as resolution "
         "context so the optional life gain uses its effective power |",
         "+ `MayEffect(GainLifeEffect(new TriggeringPermanentPower()))` — the power of the stack entry's triggering "
         "permanent as the ability resolves |"),
        ("Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER) PutCountersOnSourceEqualToEnteringPowerEffect(1, 1, "
         "true) — you may put X +1/+1 counters on this creature where X = entering creature's power |",
         "Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER) MayEffect(PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE, "
         "new TriggeringPermanentPower())) — you may put X +1/+1 counters on this creature where X = the entering "
         "creature's power as the ability resolves |"),
        ("MayEffect(GainLifeEqualToToughnessEffect()) — the collector reads the entering creature's toughness at trigger "
         "time and queues the may-ability as MayEffect(GainLifeEffect(toughness)) |",
         "MayEffect(GainLifeEffect(new TriggeringPermanentToughness())) — the entering creature's toughness as the "
         "ability resolves |"),
        ("| Land tap trigger | `m/Manabarbs.java` | Triggers.playerTapsLandForMana() DealDamageOnLandTapEffect |",
         "| Land tap trigger | `m/Manabarbs.java` | Triggers.playerTapsLandForMana() DealDamageOnLandTapEffect — a "
         "trigger-condition record; the collector puts its `triggeredEffect()`, "
         "`DealDamageToPlayersEffect(1, TRIGGERING_PLAYER)`, on the stack with the tapping player as the entry's player |"),
    ],
    "EFFECT_MERGE_PLAN.md": [
        ("- `GainLifeEqualToToughness/DamageDealt/DyingCreatureToughness/ControlledCreatureCombatDamage` — marker records",
         "- `GainLifeEqualToDamageDealt/ControlledCreatureCombatDamage` — marker records"),
    ],
}

REMOVED = (
    "DrawCardIfEnteringCreatureHasUniqueNameEffect",
    "GainLifeEqualToToughnessEffect",
    "GainLifeEqualToPowerEffect",
    "PutCountersOnSourceEqualToEnteringPowerEffect",
    "IntensifySourceByEnteringPowerEffect",
    "LookAtTopCardsEqualToEnteringPowerPutOneOnTopRestOnBottomEffect",
)


def main():
    failed = False
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        original = text
        for old, new in edits:
            if new and new in text:
                continue
            if old in text:
                text = text.replace(old, new, 1)
            elif not new:
                continue
            else:
                print(f"{name}: fragment not found: {old[:90]}")
                failed = True
        if text != original:
            path.write_text(text)
            print(f"updated {name}")
    for name, prefixes in DROP_ROWS.items():
        path = DOCS / name
        lines = path.read_text().split("\n")
        kept = [line for line in lines if not any(line.startswith(prefix) for prefix in prefixes)]
        if len(kept) != len(lines):
            path.write_text("\n".join(kept))
            print(f"dropped rows in {name}")
    for path in list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md")):
        text = path.read_text()
        for removed in REMOVED:
            if re.search(r"(?<![A-Za-z])" + removed, text):
                print(f"{path.relative_to(ROOT)} still names {removed}")
                failed = True
    if failed:
        sys.exit(1)


if __name__ == "__main__":
    main()
