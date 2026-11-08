#!/usr/bin/env python3
"""Step 15, run 17y2: the agent-docs name the "a source deals damage", "deals damage to you", chosen-color creature and
fight abilities as trigger filters with existing effects.

Whole-line edits, each keyed on the line's start: the removed records' rows become "Gone" rows, the patterns and
Oracle-text rows name the new form (`Triggers.sourceDealsDamage()` / `sourceYouControlDealsDamage()` with `withFilter`
and `withSpellFilter`, `permanentDealsDamageToYou().withFilter(…)`, `creatureDealsDamageToYouOrYourPermanent()` with
`withFilter` and `withDamagedFilter`, `RegisterDelayedCountersOnTriggeringPermanentEffect` written by the card), and
`GainLifeEffect` names `GainLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER`. Fails if a doc or skill still names a
removed record outside a "Gone" line. Idempotent.

Usage: step15-run17y2-docs.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"
RUN = "effect-slot-refactor.md step 15, run 17y2"

JUSTICE = ("`Triggers.sourceDealsDamage().withFilter(new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), "
           "new PermanentColorInPredicate(Set.of(CardColor.RED))))).withSpellFilter(new CardColorPredicate(CardColor.RED))` + "
           "`DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER)`")
SLIVER = ("`Triggers.sourceDealsDamage().withFilter(new PermanentHasSubtypePredicate(CardSubtype.SLIVER))"
          ".withSpellFilter(new CardSubtypePredicate(CardSubtype.SLIVER))` + `GainLifeEffect(new EventValue(), "
          "GainLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER)`")
TERROR = ("`Triggers.sourceYouControlDealsDamage().withFilter(new PermanentColorInPredicate(Set.of(CardColor.RED)))"
          ".withSpellFilter(new CardColorPredicate(CardColor.RED))` + `PutCountersOnSourceCardEffect(CounterType.PLUS_ONE_PLUS_ONE)`")
DESTROY = ("`Triggers.permanentDealsDamageToYou().withFilter(new PermanentIsCreaturePredicate())` (a Human: "
           "`PermanentHasSubtypePredicate(CardSubtype.HUMAN)`) + `DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING)`")
MANGARA = ("`Triggers.creatureDealsDamageToYouOrYourPermanent().withFilter(new PermanentHasSourceChosenColorPredicate())"
           ".withDamagedFilter(white creature)` + `DealDamageToTriggeringPermanentEffect(new EventValue())`")
FIGHT = ("`RegisterDelayedCountersOnTriggeringPermanentEffect(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2))` "
         "on `Triggers.creatureYouControlFights()`")
SOURCE_FILTERS = ("the trigger's `withFilter` judges the source as a permanent (its card's characteristics once it has left), "
                  "`withSpellFilter` a source that isn't a permanent (a spell, a card dealing damage from another zone); a trigger "
                  "with either triggers only for a source of a kind it has a filter for, one with neither for every source. Colors "
                  "are the source's own: Ghostly Flame's colorless damage doesn't stop \"a red creature or spell deals damage\"")


def gone(name, signature, what):
    return f"| `{name}` | (gone) | **Gone** ({RUN}): {what} |"


EDITS = {
    "EFFECTS_INDEX.md": [
        ("| `PutCounterOnTargetPermanentEffect` in `Triggers.creatureYouControlFights()` |",
         f"| `RegisterDelayedCountersOnTriggeringPermanentEffect` in `Triggers.creatureYouControlFights()` | "
         f"`(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2))` | the card writes it: the fight "
         f"collector puts it on the stack as written with the fighting creature as its triggering permanent, and it then "
         f"queues the non-targeting counter delayed action for the next end step. Used by Foe-Razer Regent |"),
        ("| `DestroyDamageSourcePermanentEffect` |",
         gone("DestroyDamageSourcePermanentEffect", "", f"\"whenever a creature / a Human deals damage to you, destroy it\" is {DESTROY}: the trigger's filter on the dealer, the dealer the entry's triggering permanent (No Mercy, Dread, Mikaeus)")),
        ("| `ReflectDamageToChosenColorCreatureEffect` |",
         gone("ReflectDamageToChosenColorCreatureEffect", "", f"\"whenever a creature of the chosen color deals damage to you or a white creature you control, this deals that much damage to that creature\" is {MANGARA}: the filter on the creature, the damaged filter on the permanent (damage to you always counts), the creature the entry's triggering permanent and the damage its event value (Mangara's Equity)")),
        ("| `ReflectSourceDamageToItsControllerEffect` |",
         gone("ReflectSourceDamageToItsControllerEffect", "", f"\"whenever a red creature or spell deals damage, this deals that much damage to that creature's or spell's controller\" is {JUSTICE}; {SOURCE_FILTERS} (Justice)")),
        ("| `SourceDamageGainLifeForSourceControllerEffect` |",
         gone("SourceDamageGainLifeForSourceControllerEffect", "", f"\"whenever a Sliver deals damage, its controller gains that much life\" is {SLIVER}: its controller as the ability resolves (Essence Sliver)")),
        ("| `PutCountersOnSourceCardForColorSourceDamageEffect` |",
         gone("PutCountersOnSourceCardForColorSourceDamageEffect", "", f"\"whenever a red source you control deals damage, put a +1/+1 counter on this\" is {TERROR} (The Red Terror)")),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"Whenever a creature you control fights, put two +1/+1 counters on it at the beginning of the next end step\" |",
         f"| \"Whenever a creature you control fights, put two +1/+1 counters on it at the beginning of the next end step\" | "
         f"`RegisterDelayedCountersOnTriggeringPermanentEffect(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2))` | "
         f"`Triggers.creatureYouControlFights()` | Foe-Razer Regent. The trigger goes on the stack as written with the fighting "
         f"creature as its triggering permanent; resolving it creates the delayed counter trigger (CR 603.7a) without targeting. |"),
        ("| \"As this enchantment enters, choose black or red. Whenever a creature of the chosen color deals damage to you or a white creature you control",
         f"| \"As this enchantment enters, choose black or red. Whenever a creature of the chosen color deals damage to you or a "
         f"white creature you control, this enchantment deals that much damage to that creature.\" | `STATIC "
         f"ChooseColorOnEnterEffect(CardColor.BLACK, CardColor.RED)` + {MANGARA} | enchantment | Mangara's Equity (MIR). "
         f"`ChooseColorOnEnterEffect` takes a varargs list of allowed colors for \"choose black or red\"; damage to the controller "
         f"themself always qualifies, the damaged filter only gates the \"or a … you control\" half |"),
        ("| \"whenever a red creature or spell deals damage, this enchantment deals that much damage to that creature's or spell's controller\" |",
         f"| \"whenever a red creature or spell deals damage, this enchantment deals that much damage to that creature's or spell's "
         f"controller\" | {JUSTICE} | `Triggers.sourceDealsDamage()` | Justice — all damage one source deals at once is summed into "
         f"a single reflection (CR ruling), dealt by the watcher to the source's controller (a creature's as the ability resolves); "
         f"{SOURCE_FILTERS}. \"Whenever a Sliver deals damage, its controller gains that much life\" is {SLIVER} (Essence Sliver) |"),
        ("| \"Whenever a red source you control deals damage to one or more permanents and/or players, put a +1/+1 counter on The Red Terror\" |",
         f"| \"Whenever a red source you control deals damage to one or more permanents and/or players, put a +1/+1 counter on The "
         f"Red Terror\" | {TERROR} | `Triggers.sourceYouControlDealsDamage()` | The Red Terror (`40K` 83) — one trigger for each "
         f"damage event from a red source its controller controls, whether the recipients are permanents, players, or both. "
         f"\"A noncreature source you control\" (Tamanoa) is `withFilter(new PermanentNotPredicate(new PermanentIsCreaturePredicate()))"
         f".withSpellFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))` |"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)` in `Triggers.creatureYouControlFights()`",
         f"- {FIGHT} — the card writes the delayed counters; the fight trigger goes on the stack as written with the fighting "
         f"creature as its triggering permanent, and on resolution schedules two +1/+1 counters on that creature at the next end "
         f"step (Foe-Razer Regent)"),
        ("- `ReflectDamageToChosenColorCreatureEffect(",
         f"- {MANGARA} — whenever a creature of the source's chosen color damages you or a white creature you control, the "
         f"source deals that much damage back to that creature (Mangara's Equity). Pair with `ChooseColorOnEnterEffect`"),
        ("- `ReflectSourceDamageToItsControllerEffect(",
         f"- {JUSTICE} — whenever any red creature or spell deals damage, this permanent deals that much to that source's "
         f"controller (Justice): a creature's controller as the ability resolves, a spell's as it dealt the damage. Damage from "
         f"one source is summed into one reflection (CR ruling); {SOURCE_FILTERS}"),
        ("- `SourceDamageGainLifeForSourceControllerEffect(",
         f"- {SLIVER} — whenever a Sliver deals damage, its controller (as the ability resolves) gains that much life; the "
         f"ability is controlled by the watcher's controller (Essence Sliver)."),
        ("- `PutCountersOnSourceCardForColorSourceDamageEffect(",
         f"- {TERROR} — when a red source you control deals damage, put a +1/+1 counter on this permanent; one trigger per "
         f"source per damage event (The Red Terror)"),
    ],
    "CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("| Chosen-color creature damage punisher | `m/MangarasEquity.java` |",
         f"| Chosen-color creature damage punisher | `m/MangarasEquity.java` | STATIC ChooseColorOnEnterEffect(BLACK, RED) + "
         f"Triggers.upkeep(ActivePlayer.YOU) ForcedCostOrElseEffect(PayManaCost(\"{{1}}{{W}}\"), List.of(SacrificeSelfEffect()), true) + "
         f"{MANGARA} — whenever a creature of the chosen color damages you or a white creature you control, deals that much "
         f"back to that creature |"),
        ("| Reflect a color's damage to its controller | `j/Justice.java` |",
         f"| Reflect a color's damage to its controller | `j/Justice.java` | Triggers.upkeep(ActivePlayer.YOU) "
         f"ForcedCostOrElseEffect(PayManaCost(\"{{W}}{{W}}\"), List.of(SacrificeSelfEffect()), true) + {JUSTICE} — whenever "
         f"any red creature or spell deals damage, deals that much to the source's controller (all simultaneous damage from one "
         f"source summed into one reflection) |"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| Damage-to-controller destroy matching source | `m/MikaeusTheUnhallowed.java` |",
         f"| Damage-to-controller destroy matching source | `m/MikaeusTheUnhallowed.java` | {DESTROY} — the trigger's filter "
         f"on the permanent that dealt the damage; \"destroy it\" reads the entry's triggering permanent |"),
    ],
    "CARD_PATTERNS_LANDS_SPELLS.md": [
        ("| Fight-triggered delayed counters | `f/FoeRazerRegent.java` |",
         f"| Fight-triggered delayed counters | `f/FoeRazerRegent.java` | {FIGHT}; the fighting creature is the entry's "
         f"triggering permanent and the counters are put on it at the next end step |"),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("| `Triggers.creatureDealsDamageToYouOrYourPermanent()` |",
         "| `Triggers.creatureDealsDamageToYouOrYourPermanent()` | \"Whenever a creature of the chosen color deals damage to you or "
         "a white creature you control, ...\" — a watcher on the *damaged* side. Scans the damaged player's battlefield; the "
         "trigger's `withFilter` gates which damaging creatures qualify (`PermanentHasSourceChosenColorPredicate`) and its "
         "`withDamagedFilter` the \"or a … you control\" half (damage to the player themself always qualifies). Fires once per "
         "damaging creature per damage event from all four choke points (combat to player + to creature, non-combat to player + "
         "to creature) via `TriggerCollectionService.checkCreatureDamageToYouOrYourPermanentTriggers`; the creature is the "
         "entry's triggering permanent, the damage its event value. Used by Mangara's Equity "
         "(`DealDamageToTriggeringPermanentEffect(new EventValue())`) |"),
        ("| `Triggers.sourceDealsDamage()` |",
         "| `Triggers.sourceDealsDamage()` / `sourceYouControlDealsDamage()` | Global watcher: any source (creature or spell; for "
         "the second, one its controller controls) deals damage to anything. Fires on every permanent with this ability across "
         "all battlefields; damage one source deals simultaneously is summed into one trigger. `withFilter` narrows the source as "
         "a permanent, `withSpellFilter` as a spell or card (Justice: a red creature or a red spell); the source is the entry's "
         "triggering permanent (none for a spell), its controller as it dealt the damage the fallback and the total the event "
         "value. Driven from `CombatDamageService` (per source) and `EffectResolutionService` (non-combat flush) |"),
    ],
}

GAIN_LIFE_OLD = "`recipient` defaults to `CONTROLLER`; `TARGET_CONTROLLER` routes the life to the effect's target permanent's controller"
GAIN_LIFE_NEW = ("`recipient` defaults to `CONTROLLER`; `TRIGGERING_PERMANENT_CONTROLLER` gives it to the controller of the "
                 "entry's triggering permanent as the ability resolves (a spell source's controller as it dealt the damage: "
                 "Essence Sliver's \"its controller gains that much life\"); `TARGET_CONTROLLER` routes the life to the effect's "
                 "target permanent's controller")

REMOVED = ["ReflectSourceDamageToItsControllerEffect", "SourceDamageGainLifeForSourceControllerEffect",
           "PutCountersOnSourceCardForColorSourceDamageEffect", "ReflectDamageToChosenColorCreatureEffect",
           "DestroyDamageSourcePermanentEffect"]


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        lines = path.read_text().split("\n")
        for prefix, new in edits:
            if new in lines:
                continue
            hits = [i for i, line in enumerate(lines) if line.startswith(prefix)]
            if len(hits) != 1:
                raise SystemExit(f"{name}: {len(hits)} lines start with {prefix!r}")
            lines[hits[0]] = new
        path.write_text("\n".join(lines))
    index = DOCS / "EFFECTS_INDEX.md"
    text = index.read_text()
    if GAIN_LIFE_NEW not in text:
        if text.count(GAIN_LIFE_OLD) != 1:
            raise SystemExit("EFFECTS_INDEX.md: GainLifeEffect recipient text not found")
        index.write_text(text.replace(GAIN_LIFE_OLD, GAIN_LIFE_NEW))
    paths = list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md"))
    for path in paths:
        for line in path.read_text().split("\n"):
            for removed in REMOVED:
                if re.search(r"\b" + removed + r"\b", line) and "**Gone**" not in line:
                    raise SystemExit(f"{path.name} still names {removed}: {line[:120]}")


if __name__ == "__main__":
    main()
