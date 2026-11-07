#!/usr/bin/env python3
"""Step 15 run 17f of effect-slot-refactor.md: agent-docs for the enters-side event bindings.

Idempotent: exact line edits; fails if a removed form is still named in agent-docs or the skills.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"
SKILLS = ROOT / ".claude" / "skills"

EVOLVE_IF = ("`TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentHasGreaterPowerOrToughnessThanSource())`")

REPLACE = {
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `ChampionOfTheHareishTriggerEffect(List<CardSubtype>)` — `Triggers.enters(Triggers.CREATURE, "
         "ObjectRelation.ANOTHER_YOU_CONTROL)` trigger marker; at resolution, puts a +1/+1 counter on the source when "
         "an entering type is already on the controller's buddy list, otherwise records one new type, prompting when "
         "multiple new types are available.",
         "- `BuddyListGrowOrWriteEffect()` — on `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)`: "
         "as it resolves, puts a +1/+1 counter on the source when one of the stack entry's triggering creature's types "
         "(as it is then, or as it last existed once it left) is already on the controller's buddy list, otherwise "
         "writes one new type, prompting when several are available (Champion of the Hareish)."),
        ("- `EvolveTriggerEffect()` — keyword-driven ally-creature entry trigger: compares the entering creature's power "
         "and toughness to the source at trigger and resolution, using the entering creature's saved stats if it has "
         "left before resolution, then puts one +1/+1 counter on the source, then fires the source's "
         "`Triggers.evolves()` triggers if the counter was actually placed",
         "- `EvolveEffect()` — engine-only: the evolve keyword's \"put a +1/+1 counter on this creature\", which its "
         "collector wraps in the keyword's intervening \"if\" `TriggeringPermanentHasGreaterPowerOrToughnessThanSource` "
         "(CR 702.100a); fires the source's `Triggers.evolves()` triggers if the counter was placed (CR 702.100b). "
         "Cards with the keyword add nothing; a card's own \"if it has greater power or toughness than this creature, "
         "put a counter on this creature\" is " + EVOLVE_IF + " with `PutCountersOnSourceEffect` / "
         "`PutCountersOnSelfEffect` (Hulkling, Evolving Adaptive, Sharp-Eyed Rookie)"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `ChampionOfTheHareishTriggerEffect` | `(List<CardSubtype>)` | `Triggers.enters(Triggers.CREATURE, "
         "ObjectRelation.ANOTHER_YOU_CONTROL)` trigger marker; resolves against the controller's buddy list, adding one "
         "new entering type or putting a +1/+1 counter on the source when a type is already listed | Champion of the "
         "Hareish (MB2 275/511) |",
         "| `BuddyListGrowOrWriteEffect` | `()` | on `Triggers.enters(Triggers.CREATURE, "
         "ObjectRelation.ANOTHER_YOU_CONTROL)`: as it resolves, reads the stack entry's triggering creature (as it last "
         "existed once it left) against the controller's buddy list, putting a +1/+1 counter on the source when one of "
         "its types is listed, otherwise writing one new type | Champion of the Hareish (MB2 275/511) |"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("`Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL) ChampionOfTheHareishTriggerEffect()`",
         "`Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL) BuddyListGrowOrWriteEffect()` (reads "
         "the triggering creature as the ability resolves)"),
        ("Evolve is auto-loaded as `Keyword.EVOLVE`; the ally-creature entry scan creates `EvolveTriggerEffect` with "
         "trigger-time stats, and its handler rechecks the comparison at resolution using last-known stats if needed. "
         "Add nothing to the card (e.g. Clinging Anemones) |",
         "Evolve is auto-loaded as `Keyword.EVOLVE`; the ally-creature entry collector triggers it with the intervening "
         "\"if\" `TriggeringPermanentHasGreaterPowerOrToughnessThanSource` around `EvolveEffect`, checked again as it "
         "resolves (last-known stats once a creature left). Add nothing to the card (e.g. Clinging Anemones) |\n"
         "| \"Whenever another creature you control enters, if it has greater power or toughness than this creature, put "
         "a +1/+1 counter on this creature [and investigate]\" (not the keyword) | `addTrigger(Triggers.enters("
         "Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL), " + EVOLVE_IF[1:-1] + ", new "
         "PutCountersOnSourceEffect(1, 1, 1)[, CreateTokenEffect.ofClueToken(1)])` | `Triggers.enters(...)` | Hulkling, "
         "Burgeoning Bruiser; Sharp-Eyed Rookie; Evolving Adaptive (`PutCountersOnSelfEffect(CounterType.OIL)`). It "
         "doesn't evolve the creature (`Triggers.evolves()` watches only the keyword) |\n"
         "| \"Whenever another permanent you control enters, if it wasn't put onto the battlefield with this ability, "
         "...\" | `Triggers.enters(new PermanentNotPredicate(new PermanentPutOntoBattlefieldBySourcePredicate()), "
         "ObjectRelation.ANOTHER_YOU_CONTROL)` | `Triggers.enters(...)` | Kodama of the East Tree; "
         "`PutCardToBattlefieldEffect` records the source on the permanent it puts |\n"
         "| \"Landfall — ..., put a +1/+1 counter on this creature. If that land is a Forest, put two +1/+1 counters on "
         "this creature instead\" | `new ConditionalReplacementEffect(new TriggeringPermanentHasSubtype(FOREST), base, "
         "upgraded)` on `Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL)` | `Triggers.enters(...)` | Oran-Rief "
         "Hydra, Emeria Shepherd (a \"you may\" per branch); the land is read as the ability resolves, as it last existed "
         "once it left |"),
        ("(`EvolveTriggerEffectHandler`)", "(`EvolveEffectHandler`)"),
    ],
    "CARD_PATTERN_INDEX.md": [
        ("+ `BuddyListOnEnterEffect` + `ChampionOfTheHareishTriggerEffect` |",
         "+ `BuddyListOnEnterEffect` + `BuddyListGrowOrWriteEffect` |"),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("fired by `EvolveTriggerEffectHandler` only when the evolve trigger",
         "fired by `EvolveEffectHandler` only when the evolve keyword's ability"),
    ],
}

INSERT_AFTER = {
    "EFFECTS_INDEX.md": [
        ("| `EvolveTriggerEffect` | `()` |",
         "| `EvolveEffect` | `()` | Engine-only: the `EVOLVE` keyword's \"put a +1/+1 counter on this creature\". The "
         "ally-creature entry collector judges the keyword's intervening \"if\" "
         "(`TriggeringPermanentHasGreaterPowerOrToughnessThanSource`, CR 702.100a) as it triggers and wraps this effect "
         "in it, so it is checked again as it resolves, with last-known stats once a creature left (CR 608.2h). After "
         "the counter lands the handler fires the source's `Triggers.evolves()` triggers (CR 702.100b, Renegade "
         "Krasis); no counter, no trigger. A card's own evolve-like ability is " + EVOLVE_IF + " and existing effects "
         "(Hulkling, Sharp-Eyed Rookie, Evolving Adaptive) |\n"
         "| `TriggeringPermanentHasGreaterPowerOrToughnessThanSource` (condition) | `()` | the stack entry's triggering "
         "permanent has greater power or greater toughness than the source, each as it is on the battlefield or as it "
         "last existed there; a permanent that isn't a creature never compares (CR 702.100c). Evolve's intervening "
         "\"if\" (`TriggerOptions.withInterveningIf`) | Hulkling, Burgeoning Bruiser; Sharp-Eyed Rookie; Evolving "
         "Adaptive |"),
    ],
    "PREDICATES_REFERENCE.md": [
        ("| `PermanentIsSourcePermanentPredicate` | `()` |",
         "| `PermanentPutOntoBattlefieldBySourcePredicate` | `()` | a permanent put onto the battlefield with an ability "
         "of the source permanent (`PutCardToBattlefieldEffect` records it). Wrap in `PermanentNotPredicate` for \"if it "
         "wasn't put onto the battlefield with this ability\" as an enters trigger's filter (Kodama of the East Tree) | "
         "`sourcePermanentId` |"),
    ],
}

# The EvolveTriggerEffect row is replaced by the inserted rows.
DELETE_LINES_STARTING = {
    "EFFECTS_INDEX.md": ["| `EvolveTriggerEffect` | `()` |"],
}

REMOVED = ["EvolveTriggerEffect", "ChampionOfTheHareishTriggerEffect", "TriggeringPermanentEntryExclusionEffect",
           "withTriggeringPermanentEntrySuppressed", "suppressesTriggeringPermanentEntry"]


def main():
    for name, edits in REPLACE.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new in text and old not in text:
                continue
            if text.count(old) != 1:
                sys.exit(f"{path}: expected one occurrence of {old[:80]!r}")
            text = text.replace(old, new)
        path.write_text(text)
    for name, inserts in INSERT_AFTER.items():
        path = DOCS / name
        lines = path.read_text().split("\n")
        for prefix, new in inserts:
            first_new = new.split("\n")[0]
            if any(line == first_new for line in lines):
                continue
            index = next((i for i, line in enumerate(lines) if line.startswith(prefix)), None)
            if index is None:
                sys.exit(f"{path}: no line starting {prefix!r}")
            lines[index + 1:index + 1] = new.split("\n")
        path.write_text("\n".join(lines))
    for name, prefixes in DELETE_LINES_STARTING.items():
        path = DOCS / name
        lines = path.read_text().split("\n")
        lines = [line for line in lines if not any(line.startswith(p) for p in prefixes)]
        path.write_text("\n".join(lines))
    stale = []
    for path in list(DOCS.rglob("*.md")) + list(SKILLS.rglob("*.md")):
        text = path.read_text()
        stale += [f"{path.relative_to(ROOT)}: {name}" for name in REMOVED if name in text]
    if stale:
        sys.exit("still named:\n" + "\n".join(stale))
    print("docs updated")


if __name__ == "__main__":
    main()
