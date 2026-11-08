#!/usr/bin/env python3
"""Step 15, run 17t: the agent docs show the trigger conditions that were wrapper effects as trigger filters, options
and effect "if"s: Wooden Stake's and Hedron Blade's block filters, Sigil Captain's intervening "if", and "when you
cast this spell, copy it" as `CopyTriggeringSpellEffect`.

Exact edits: each must apply once, or have been applied (idempotent). Fails when a doc or skill still names
`CombatOpponentFilterEffect`, `CombatOpponentConditionalEffect`, `DestroySubtypeCombatOpponentEffect`,
`EnteringCreatureExactStatsConditionalEffect` or `CopyThisSpellIfConditionEffect` (`EFFECT_MERGE_PLAN.md`'s history
may name them).

Usage: step15-run17t-docs.py
"""
import pathlib
import sys

DOCS = pathlib.Path("agent-docs")
SKILLS = pathlib.Path(".claude/skills")

SIGIL_IF = ("`TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentMatches(new PermanentAllOfPredicate("
            "List.of(new PermanentPowerAtLeastPredicate(1), new PermanentPowerAtMostPredicate(1), "
            "new PermanentToughnessAtLeastPredicate(1), new PermanentToughnessAtMostPredicate(1)))))`")
SIGIL_EFFECT = ("`PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE, "
                "new Fixed(2))`")

EDITS = {
    DOCS / "EFFECTS_QUICK_REFERENCE.md": [
        ("""- `EnteringCreatureExactStatsConditionalEffect(int power, int toughness, CardEffect)` — entering creature's printed P/T both equal the given values ("if that creature is 1/1" — Sigil Captain `(1, 1, …)`)
""",
         """- "Whenever a creature you control enters, if that creature is X/Y": the ability's intervening "if" on the entering creature (judged as it enters and again as the ability resolves, CR 603.4), Sigil Captain """ + SIGIL_IF + """ with """ + SIGIL_EFFECT + """
"""),
        (""" or wrap in `EnteringCreatureExactStatsConditionalEffect` for "is X/Y" (Sigil Captain: `(1, 1, new PutCountersOnEnteringCreatureEffect(2, false))`).""",
         """; an "if that creature is X/Y" is the ability's intervening "if" `TriggeringPermanentMatches` with `PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, …)` (Sigil Captain, above)."""),
        ("""- `CopyThisSpellIfConditionEffect(Condition[, boolean tokenCopy])` — "When you cast this spell, copy it if <condition>. You may choose new targets for the copy." Place in the `Triggers.youCastThisSpell()` ability (the spell's own cast trigger); the copy is created with an optional choose-new-targets prompt only when the condition holds at resolution. Pass `tokenCopy = true` when a permanent spell's copy must enter as a token (Sage of the Skies). Used by the SOS Infusion copy cycle (e.g. Lumaret's Favor with `new GainedLifeThisTurn()`)
- `CopyThisSpellIfConditionEffect(Condition)` — "When you cast this spell, copy it if <condition>." Place in the `Triggers.youCastThisSpell()` ability (the spell's own cast trigger); the condition is checked when the copy trigger resolves. For wording such as "when you cast this spell while <condition>, you may copy this spell", use `CopyThisSpellIfConditionEffect.whenCastWhile(condition, true)` so the condition is checked when casting finishes and the optional copy choice is offered only when it holds. Used by the SOS Infusion copy cycle (e.g. Lumaret's Favor with `new GainedLifeThisTurn()`) and Social Snub (`whenCastWhile(new ControlsPermanent(new PermanentIsCreaturePredicate()), true)`)
""",
         """- "When you cast this spell, copy it": `Triggers.youCastThisSpell()` with `CopyTriggeringSpellEffect` (this spell is the stack entry's triggering spell; the ability is one object of its own). "Copy it if <condition>" is an effect "if" checked as the ability resolves, `new ConditionalEffect(condition, new CopyTriggeringSpellEffect())` (Lumaret's Favor, Mentor's Guidance); "when you cast this spell while <condition>" the event condition, `TriggerOptions.DEFAULT.withEventCondition(condition)` (Social Snub, Ancestral Communion); "when you cast this spell, if <condition>" the intervening "if", `TriggerOptions.DEFAULT.withInterveningIf(condition)` (Malicious Affliction's morbid, Sage of the Skies, Bygone Marvels); "you may copy" `MayEffect(copy, prompt)`, asked as it resolves; "(The copy becomes a token.)" `new CopyTriggeringSpellEffect(true)` (Brass Knuckles, Sage of the Skies); "copy this spell twice" `.withCopies(2)`; "copy it if …, then copy it if …" one ability, `SequenceEffect.of(new ConditionalEffect(a, copy), new ConditionalEffect(b, copy))` (Banish into Fable)
"""),
        ("""- `CombatOpponentConditionalEffect(PermanentPredicate opponentFilter, CardEffect wrapped)` — trigger-time condition for an attached Aura/Equipment: unwraps and fires `wrapped` when at least one combat opponent matches `opponentFilter`; use the normal Triggers.becomesBlocked() registration for "one or more" wording so multiple blockers still create one trigger. Hedron Blade uses it with `PermanentIsColorlessPredicate` around `BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(0, 0, DEATHTOUCH)`.
""",
         """- "Whenever equipped / enchanted creature blocks or becomes blocked by a [filter] creature": the trigger's filter on the creature on the other side of the block (CR 509.3f), `Triggers.blocks().withFilter(p)` / `Triggers.becomesBlocked().withFilter(p)`, judged by the attached combat collector as the ability triggers. With `TriggerOptions.EACH_BLOCKER` it triggers once for each matching blocker ("becomes blocked by a Vampire", Wooden Stake with `DestroyCombatOpponentEffect(true)` for "destroy that creature"); without it once if one or more blockers match ("becomes blocked by one or more colorless creatures", Hedron Blade with `BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(0, 0, DEATHTOUCH)`).
"""),
        ("""`withCopies(n)`; `tokenCopy=true` makes every copy a token, and `targetingSource()` controls the copy and retargets every copied target occurrence to the trigger source without a choose-new-targets prompt.""",
         """`withCopies(n)`; `tokenCopy=true` makes every copy a token, and `targetingSource()` controls the copy and retargets every copied target occurrence to the trigger source without a choose-new-targets prompt. In a `Triggers.youCastThisSpell()` ability it copies this spell ("when you cast this spell, copy it", below)."""),
    ],
    DOCS / "EFFECTS_INDEX.md": [
        ("""| `EnteringCreatureExactStatsConditionalEffect` | `(int power, int toughness, CardEffect wrapped)` | Wraps any Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL) effect with "entering creature's printed power AND toughness both equal the given values" condition ("if that creature is 1/1"). Checked at trigger time. Used by Sigil Captain (`(1, 1, PutCountersOnEnteringCreatureEffect(2, false))`) |
""", ""),
        (""" or wrap in `EnteringCreatureExactStatsConditionalEffect` ("is X/Y" gate � Sigil Captain `(2, false)`, is 1/1, mandatory) |""",
         """; an "if that creature is X/Y" is the ability's intervening "if" `TriggeringPermanentMatches` with `PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, …)` (Sigil Captain) |"""),
        ("""| `DestroySubtypeCombatOpponentEffect` | `(CardSubtype requiredSubtype, boolean cannotBeRegenerated)` | equipment combat trigger: when equipped creature blocks or becomes blocked by a creature with the required subtype, destroy that creature. At trigger creation time, the combat opponent's subtype is checked; if it matches, a `DestroyTargetPermanentEffect(cannotBeRegenerated)` is auto-targeted on the opponent and placed on the stack as non-targeting. Use Triggers.blocks() (non-per-blocker) + Triggers.becomesBlocked() (`TriggerOptions.EACH_BLOCKER`). See Wooden Stake |
""", ""),
        ("""| `CopyThisSpellIfConditionEffect` | `(Condition condition)` | trigger descriptor for the `Triggers.youCastThisSpell()` ability (a spell's own "when you cast this spell" ability): "copy it if `condition`. You may choose new targets for the copy." Scanned against the just-cast spell card in `TriggerCollectionService.checkSpellCastTriggers`, which snapshots the spell and queues a triggered ability wrapping `CopyControllerCastSpellEffect` in a `ConditionalEffect` on `condition` (so the copy happens only when the condition holds at resolution). Used by the SOS Infusion copy cycle, e.g. Lumaret's Favor (`new GainedLifeThisTurn()`) |
""", ""),
        ("""`CombatOpponentConditionalEffect` takes a `PermanentPredicate` and a wrapped `CardEffect` for an attached Aura/Equipment combat trigger. `CombatTriggerService` evaluates the predicate at trigger time against the relevant combat opponent(s) and unwraps the effect only on a match. Register it normally in `Triggers.becomesBlocked()` for "one or more" wording so multiple blockers still produce one trigger; Hedron Blade wraps the existing equipped-creature temporary keyword effect this way.""",
         """An attached Aura's or Equipment's "blocks or becomes blocked by a [filter] creature" is the trigger's filter on the creature on the other side of the block (`Triggers.becomesBlocked().withFilter(p)`, CR 509.3f), which `CombatTriggerService` judges as the ability triggers: once for each matching blocker with `TriggerOptions.EACH_BLOCKER` (Wooden Stake), once if one or more blockers match without it (Hedron Blade)."""),
        ("""`CopyThisSpellIfConditionEffect` defaults to one copy and accepts an explicit copy count. An outer `ConditionalEffect` gates the cast trigger, with the descriptor's condition checked again at resolution, for a single intervening conditional trigger that creates multiple copies.""",
         """"When you cast this spell, copy it" is `CopyTriggeringSpellEffect` in the `Triggers.youCastThisSpell()` ability, the spell the entry's triggering spell: "copy it if …" a `ConditionalEffect` around it, "while …" the event condition, "if …" right after the trigger the intervening "if", "copy this spell twice" `.withCopies(2)` (Bygone Marvels)."""),
        ("""`withCopies(n)` ("copy it twice"). `targetingSource()`""",
         """`withCopies(n)` ("copy it twice"). In a `Triggers.youCastThisSpell()` ability it copies this spell (Brass Knuckles, Banish into Fable, Lumaret's Favor). `targetingSource()`"""),
    ],
    DOCS / "CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("""Triggers.blocks() DestroySubtypeCombatOpponentEffect(VAMPIRE, true) + Triggers.becomesBlocked() TriggerOptions.EACH_BLOCKER DestroySubtypeCombatOpponentEffect(VAMPIRE, true) + equip.""",
         """`Triggers.blocks().withFilter(vampire)` `DestroyCombatOpponentEffect(true)` + `Triggers.becomesBlocked().withFilter(vampire)` `TriggerOptions.EACH_BLOCKER` `DestroyCombatOpponentEffect(true)` + equip; the filter is on the creature on the other side of the block."""),
        ("""normal Triggers.becomesBlocked() `CombatOpponentConditionalEffect(PermanentIsColorlessPredicate, BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(0, 0, DEATHTOUCH))` + `EquipActivatedAbility("{2}")`; the trigger-time filter scans all blockers so "one or more" fires once per blocked event""",
         """`Triggers.becomesBlocked().withFilter(new PermanentIsColorlessPredicate())` `BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(0, 0, DEATHTOUCH)` + `EquipActivatedAbility("{2}")`; without `EACH_BLOCKER` the filter is judged against all blockers, so "one or more" fires once per blocked event"""),
    ],
    DOCS / "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("""Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL) EnteringCreatureExactStatsConditionalEffect(1, 1, PutCountersOnEnteringCreatureEffect(2, false)) — intervening-if "is 1/1" gate (exact printed power AND toughness); `optional=false` bakes the mandatory counter placement on the entering creature directly onto the stack (no "you may" prompt) |""",
         """`Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL)` + """ + SIGIL_IF + """ + """ + SIGIL_EFFECT + """ — the intervening "if" "is 1/1" on the entering creature, judged as it enters and again as the ability resolves (CR 603.4); it triggers for itself entering as a 1/1 |"""),
    ],
    DOCS / "ORACLE_TEXT_EFFECT_MAP.md": [
        ("""`CopyThisSpellIfConditionEffect` gets special copy handling, any other effect is queued as a plain triggered ability under the caster |""",
         """an ability that copies this spell (`CopyTriggeringSpellEffect`, behind a "you may" or an "if") is one object of its own with the spell as its triggering spell, any other effect is queued as a plain triggered ability under the caster |"""),
        ("""| `EnteringCreatureExactStatsConditionalEffect(X, Y, new PutCountersOnEnteringCreatureEffect(M, false))` | `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` | Sigil Captain (X=1, Y=1, M=2). Intervening-if "is 1/1" gate = exact printed power AND toughness; `optional=false` makes the counter placement mandatory (baked directly onto the stack, no "you may" prompt), unlike Mighty Emergence's may variant. |""",
         """| """ + SIGIL_EFFECT.replace("new Fixed(2)", "new Fixed(M)") + """ | `Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL)` + `TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentMatches(power and toughness at least / at most X and Y))` | Sigil Captain (X=1, Y=1, M=2). The intervening "if" is judged on the entering creature as it enters and again as the ability resolves (CR 603.4). |"""),
        ("""| normal `Triggers.becomesBlocked()` `CombatOpponentConditionalEffect(filter, BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(0, 0, Keyword.X))` | attached combat trigger | The combat collector checks all blockers at trigger time and unwraps the existing equipped-creature keyword effect once when any blocker matches; Hedron Blade uses `PermanentIsColorlessPredicate` and `DEATHTOUCH` |""",
         """| `BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(0, 0, Keyword.X)` | `Triggers.becomesBlocked().withFilter(filter)` (attached combat trigger) | The filter is on the blockers (CR 509.3f); without `EACH_BLOCKER` the combat collector judges it against all of them and the ability triggers once when any matches. Hedron Blade uses `PermanentIsColorlessPredicate` and `DEATHTOUCH`. "Blocks or becomes blocked by a [filter], destroy that creature" is `Triggers.blocks().withFilter(filter)` and `Triggers.becomesBlocked().withFilter(filter)` with `EACH_BLOCKER`, each `DestroyCombatOpponentEffect` (Wooden Stake) |"""),
        ("""`ChooseCardFromHandAndApplyPerpetualIncorporationEffect(CardAnyOfPredicate(INSTANT, SORCERY), "{U}{R}", CopyNextInstantOrSorceryCastThisTurnEffect())`""",
         """`ChooseCardFromHandAndApplyPerpetualIncorporationEffect(CardAnyOfPredicate(INSTANT, SORCERY), "{U}{R}", new CopyTriggeringSpellEffect())`"""),
    ],
    DOCS / "EFFECT_MERGE_PLAN.md": [
        ("""needs one new toughness predicate (`CardAllOfPredicate` composes it) — still net −2 (MED).
""",
         """needs one new toughness predicate (`CardAllOfPredicate` composes it) — still net −2 (MED).
**19a status**: done another way. Min and Max are gone, and effect-slot-refactor.md step 15 run 17t removed
`ExactStats` and `EnterCreatureConditionalEffect`: Sigil Captain's "if that creature is 1/1" is the ability's
intervening "if" `TriggeringPermanentMatches` on the entering creature, so no wrapper class is left to merge.
"""),
    ],
}

GONE = ["CombatOpponentFilterEffect", "CombatOpponentConditionalEffect", "DestroySubtypeCombatOpponentEffect",
        "EnteringCreatureExactStatsConditionalEffect", "CopyThisSpellIfConditionEffect"]


def main():
    for path, edits in EDITS.items():
        text = path.read_text()
        for old, new in edits:
            if text.count(old) == 1:
                text = text.replace(old, new)
            elif (new and new not in text) or (not new and old in text):
                raise SystemExit(f"{path}: edit neither applies once nor is applied: {old[:100]!r}")
        path.write_text(text)
    left = []
    for path in list(DOCS.glob("*.md")) + list(SKILLS.rglob("*.md")):
        if path.name == "EFFECT_MERGE_PLAN.md":
            continue
        text = path.read_text()
        left += [f"{path}: {name}" for name in GONE if name in text]
    if left:
        raise SystemExit("still named:\n" + "\n".join(left))
    print("docs updated")


if __name__ == "__main__":
    main()
