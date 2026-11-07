#!/usr/bin/env python3
"""Step 15, run 17h: agent-docs for emblems' and granted abilities written as `TriggeredAbility`s.

Exact edits in EFFECTS_QUICK_REFERENCE.md, EFFECTS_INDEX.md, ORACLE_TEXT_EFFECT_MAP.md and PREDICATES_REFERENCE.md.
Idempotent; fails if an edit's old text is neither present nor already replaced, or if a removed form
(`SpellCastTriggerEffect.anyPlayer`, an emblem's `SpellCastTriggerEffect`, a granted `StateTriggerEffect`) is still
named in agent-docs or the skills.
"""
import pathlib
import re
import sys

DOCS = pathlib.Path("agent-docs")

EMBLEM_ABILITY = ("`new CreateEmblemEffect(new TriggeredAbility(Triggers.youCastSpell().withFilter(spellPredicate), "
                  "effects…)[.withTargetFilter(filter)], reminderText)`")

EDITS = {
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `CreateEmblemEffect(..., List<ActivatedAbility>)` — creates an emblem with directly activatable abilities "
         "in addition to its static/trigger markers; the ability list is exposed through the emblem activation path.\n",
         "- `CreateEmblemEffect(..., List<ActivatedAbility>)` — creates an emblem with directly activatable abilities "
         "in addition to its static/trigger markers; the ability list is exposed through the emblem activation path.\n"
         "- `CreateEmblemEffect(TriggeredAbility, String reminderText)` — \"you get an emblem with 'whenever [a player] "
         "casts a [quality] spell, …'\": the emblem's ability is a `TriggeredAbility` with a cast trigger "
         "(`Triggers.youCastSpell()` / `playerCastsSpell()` / `opponentCastsSpell()`, `.withFilter(…)`, "
         "`.withStackEntryFilter(…)`, `.anySpell()`), read by the generic cast collector from the command zone "
         "(CR 114.4). An emblem has no card to bind a target group, so a targeted ability names its target filter "
         "itself: `.withTargetFilter(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(OPPONENT), …))` "
         "(Ajani, Sleeper Agent). The stack entry is described as \"<card>'s emblem\". Also the canonical "
         "`(staticEffects, reminderText, recipient, activatedAbilities, triggeredAbilities)` (effect-slot-refactor.md "
         "step 15, run 17h).\n"),
        ("- `SpellCastTriggerEffect.anyPlayer(CardPredicate, List<CardEffect>)` — emblem-only (`CreateEmblemEffect`) "
         "spell-cast trigger that fires for every player's matching spells rather than only the emblem controller's "
         "spells (Kaito, Cunning Infiltrator)\n",
         "- `SpellCastTriggerEffect.anyPlayer` — **Removed** from authoring (effect-slot-refactor.md step 15, run 17h): "
         "an emblem's \"whenever a player casts a spell\" is `new CreateEmblemEffect(new "
         "TriggeredAbility(Triggers.playerCastsSpell().anySpell(), effects…), reminderText)` (Kaito, Cunning "
         "Infiltrator)\n"),
        ("- `DealDamageEqualToManaSpentToCastToAnyTargetEffect(CardPredicate)` — whenever you cast a matching spell,",
         "- `GainControlOfAllPermanentsMatchingEffect(new PermanentIsTargetOfTriggeringSpellPredicate())` — \"gain "
         "control of those permanents\" of \"whenever you cast a spell that targets one or more permanents\" "
         "(`Triggers.youCastSpell().withStackEntryFilter(new StackEntryTargetsPermanentPredicate(new "
         "PermanentTruePredicate()))`): the permanents the triggering spell targets, on the stack or as it was when the "
         "ability triggered (Dack Fayden's emblem)\n"
         "- `DealDamageEqualToManaSpentToCastToAnyTargetEffect(CardPredicate)` — whenever you cast a matching spell,"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `SpellCastTriggerEffect.anyPlayer` | `(CardPredicate spellFilter, List<CardEffect> resolvedEffects)` | "
         "emblem-only (`CreateEmblemEffect`; a card ability is `Triggers.playerCastsSpell().withFilter(...)`): "
         "spell-cast trigger that fires for every player's matching spells; ordinary emblem spell-cast effects are "
         "scanned only for their controller's casts. Used by Kaito, Cunning Infiltrator |",
         "| `SpellCastTriggerEffect.anyPlayer` | (don't author) | an emblem's \"whenever a player casts a spell\" is "
         "`new CreateEmblemEffect(new TriggeredAbility(Triggers.playerCastsSpell().anySpell(), effects…), "
         "reminderText)`, step 15 run 17h of effect-slot-refactor.md (Kaito, Cunning Infiltrator) |"),
        ("Don't author `StateTriggerEffect` (the state-trigger reader, `MiscAbilityReader.stateTriggers`, builds it "
         "from the ability, and only a static grant of a state trigger, Olivia, Crimson Bride, still writes one).",
         "Don't author `StateTriggerEffect` (the state-trigger reader, `MiscAbilityReader.stateTriggers`, builds it "
         "from the ability). A granted state trigger is a granted ability: `new GrantEffectEffect(new "
         "GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.sourceState(predicate), effects…), "
         "GrantScope.TARGET), GrantScope.TARGET)` (Olivia, Crimson Bride's returned creature), which "
         "`StateTriggerService` reads from the permanent's granted effects."),
        ("| `CreateEmblemEffect` | `(List<CardEffect> staticEffects, String reminderText)` / `(…, EmblemRecipient "
         "recipient[, List<ActivatedAbility> activatedAbilities])` |",
         "| `CreateEmblemEffect` | `(List<CardEffect> staticEffects, String reminderText)` / `(…, EmblemRecipient "
         "recipient[, List<ActivatedAbility> activatedAbilities[, List<TriggeredAbility> triggeredAbilities]])` / "
         "`(TriggeredAbility triggeredAbility, String reminderText)` | An emblem's \"whenever [a player] casts a "
         "[quality] spell, …\" is a `TriggeredAbility` with a cast trigger, read by the generic cast collector from "
         "the command zone; a targeted one names its target filter with `TriggeredAbility.withTargetFilter(…)` "
         "(Ajani, Sleeper Agent: `PlayerPredicateTargetFilter(OPPONENT)`) — " + EMBLEM_ABILITY + ". "),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [],
    "PREDICATES_REFERENCE.md": [
        ("| `PermanentIsTriggeringPermanentPredicate` | `()` | the permanent whose event caused the resolving ability "
         "to trigger, matched by the trigger's captured permanent id | `triggeringPermanentId` |\n",
         "| `PermanentIsTriggeringPermanentPredicate` | `()` | the permanent whose event caused the resolving ability "
         "to trigger, matched by the trigger's captured permanent id | `triggeringPermanentId` |\n"
         "| `PermanentIsTargetOfTriggeringSpellPredicate` | `()` | a permanent the spell that triggered the resolving "
         "ability targets (\"gain control of those permanents\", Dack Fayden's emblem, with "
         "`GainControlOfAllPermanentsMatchingEffect`) | `triggeringSpellTargetIds` |\n"),
    ],
}

ORACLE_ROWS = [
    ("| \"You get an emblem with \\\"Whenever you cast a spell that targets one or more permanents, gain control of "
     "those permanents.\\\"\" | `CreateEmblemEffect(new TriggeredAbility(Triggers.youCastSpell().withStackEntryFilter("
     "new StackEntryTargetsPermanentPredicate(new PermanentTruePredicate())), new GainControlOfAllPermanentsMatchingEffect("
     "new PermanentIsTargetOfTriggeringSpellPredicate())), \"…\")` | loyalty ability | Dack Fayden (VMA 247) |\n"),
    ("| \"You get an emblem with \\\"Whenever you cast a creature or planeswalker spell, target opponent gets two "
     "poison counters.\\\"\" | `CreateEmblemEffect(new TriggeredAbility(Triggers.youCastSpell().withFilter("
     "creatureOrPlaneswalker), new GivePoisonCountersEffect(2, TARGET_PLAYER)).withTargetFilter(new "
     "PlayerPredicateTargetFilter(new PlayerRelationPredicate(OPPONENT), \"Target must be an opponent\")), \"…\")` | "
     "loyalty ability | An emblem names its ability's target filter itself. Ajani, Sleeper Agent (DMU 192) |\n"),
]
ORACLE_ANCHOR = ("| \"You get an emblem with \\\"Whenever you cast a spell, target opponent mills five cards.\\\"\" |")

REMOVED = [
    re.compile(r"CreateEmblemEffect\(List\.of\((new )?SpellCastTriggerEffect"),
    re.compile(r"emblem-only \(`CreateEmblemEffect`\) spell-cast trigger"),
    re.compile(r"only a static grant of a state trigger, Olivia, Crimson Bride, still writes one"),
    re.compile(r"specializedSpellCastTrigger"),
]


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: expected one occurrence of {old[:120]!r}")
            text = text.replace(old, new)
        if name == "ORACLE_TEXT_EFFECT_MAP.md":
            lines = text.splitlines(keepends=True)
            anchor = next((i for i, line in enumerate(lines) if line.startswith(ORACLE_ANCHOR)), None)
            if anchor is None:
                raise SystemExit("ORACLE_TEXT_EFFECT_MAP.md: anchor row missing")
            for row in reversed(ORACLE_ROWS):
                if row not in text:
                    lines.insert(anchor + 1, row)
            text = "".join(lines)
        path.write_text(text)
    offenders = []
    for path in list(DOCS.rglob("*.md")) + list(pathlib.Path(".claude/skills").rglob("*.md")):
        for number, line in enumerate(path.read_text().splitlines(), 1):
            if any(pattern.search(line) for pattern in REMOVED):
                offenders.append(f"{path}:{number}")
    if offenders:
        raise SystemExit("removed forms still named:\n" + "\n".join(offenders))
    print("docs updated")


if __name__ == "__main__":
    sys.exit(main())
