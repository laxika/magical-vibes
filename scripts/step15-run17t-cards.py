#!/usr/bin/env python3
"""Step 15, run 17t: the trigger conditions written as wrapper effects, as trigger filters, options and effect "if"s.

- Wooden Stake (ISD): "whenever equipped creature blocks or becomes blocked by a Vampire, destroy that creature" is
  the blocks / becomes-blocked trigger filtered on the creature on the other side of the block (CR 509.3f) with the
  existing `DestroyCombatOpponentEffect(true)`, as Mirror Shield writes it, not `DestroySubtypeCombatOpponentEffect`,
  which the attached combat collector rebuilt as an auto-targeted `DestroyTargetPermanentEffect`.
- Hedron Blade (BFZ): "becomes blocked by one or more colorless creatures" is the becomes-blocked trigger's filter (it
  triggers once if a blocker matches), not the `CombatOpponentConditionalEffect` wrapper the collector unwrapped.
- Sigil Captain (ARB): "if that creature is 1/1" is the ability's intervening "if" (its rulings say so),
  `TriggeringPermanentMatches` on the entering creature, with the existing
  `PutCounterOnReferencedPermanentEffect(TRIGGERING, ...)`, not `EnteringCreatureExactStatsConditionalEffect`.
- "When you cast this spell, copy it": the existing `CopyTriggeringSpellEffect` (the stack entry's triggering spell is
  this spell), not `CopyThisSpellIfConditionEffect`:
  - "copy it if [condition]" is an effect "if" checked as the ability resolves, a `ConditionalEffect` (Mentor's
    Guidance STX, Lumaret's Favor SOS);
  - "when you cast this spell while [condition]" is the event condition (Social Snub SOS, Ancestral Communion MSC);
  - "when you cast this spell, if [condition]" is the intervening "if" (Malicious Affliction EMA, Sage of the Skies
    TDM, Bygone Marvels LCC, whose condition was written both as the intervening "if" and inside the wrapper);
  - "copy it" (Brass Knuckles SNC; Inspiring Easel YSOS's granted ability);
  - Banish into Fable (ELD): "when you cast this spell from your hand" is the event condition, and "copy it if you
    control an artifact, then copy it if you control an enchantment" one ability of two effect "if"s (it was two).
  "You may copy" stays a `MayEffect`, asked as the ability resolves (CR 603.5); "(The copy becomes a token.)" is
  `new CopyTriggeringSpellEffect(true)`; "copy this spell twice" `withCopies(2)`.

Idempotent.

Usage: step15-run17t-cards.py <letter module dir>...
"""
import pathlib
import sys

P = "import com.github.laxika.magicalvibes.model."

EDITS = {
    "WoodenStake": [
        (P + "effect.DestroySubtypeCombatOpponentEffect;\n",
         P + "effect.DestroyCombatOpponentEffect;\n"),
        (P + "effect.StaticBoostEffect;\n",
         P + "effect.StaticBoostEffect;\n" + P + "filter.PermanentHasSubtypePredicate;\n"),
        ("""        // Whenever equipped creature blocks or becomes blocked by a Vampire,
        // destroy that creature. It can't be regenerated.
        addTrigger(Triggers.blocks(), new DestroySubtypeCombatOpponentEffect(CardSubtype.VAMPIRE, true));
        addTrigger(Triggers.becomesBlocked(),
                TriggerOptions.EACH_BLOCKER,
                new DestroySubtypeCombatOpponentEffect(CardSubtype.VAMPIRE, true));
""",
         """        // Whenever equipped creature blocks or becomes blocked by a Vampire, destroy that creature. It can't be
        // regenerated. The filter is on the creature on the other side of the block.
        PermanentHasSubtypePredicate vampire = new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE);
        addTrigger(Triggers.blocks().withFilter(vampire), new DestroyCombatOpponentEffect(true));
        addTrigger(Triggers.becomesBlocked().withFilter(vampire),
                TriggerOptions.EACH_BLOCKER,
                new DestroyCombatOpponentEffect(true));
"""),
    ],
    "HedronBlade": [
        (P + "effect.CombatOpponentConditionalEffect;\n", ""),
        ("""        addTrigger(Triggers.becomesBlocked(),
                new CombatOpponentConditionalEffect(
                        new PermanentIsColorlessPredicate(),
                        new BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(0, 0, Keyword.DEATHTOUCH)));
""",
         """        // "Becomes blocked by one or more colorless creatures": once, if a blocker matches the filter.
        addTrigger(Triggers.becomesBlocked().withFilter(new PermanentIsColorlessPredicate()),
                new BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(0, 0, Keyword.DEATHTOUCH));
"""),
    ],
    "SigilCaptain": [
        (P + "Card;\n",
         P + "Card;\n" + P + "CounterType;\n" + P + "amount.Fixed;\n"
         + P + "condition.TriggeringPermanentMatches;\n"),
        (P + "effect.EnteringCreatureExactStatsConditionalEffect;\n"
         + P + "effect.PutCountersOnEnteringCreatureEffect;\n",
         P + "effect.PermanentReference;\n" + P + "effect.PutCounterOnReferencedPermanentEffect;\n"
         + P + "filter.PermanentAllOfPredicate;\n" + P + "filter.PermanentPowerAtLeastPredicate;\n"
         + P + "filter.PermanentPowerAtMostPredicate;\n" + P + "filter.PermanentToughnessAtLeastPredicate;\n"
         + P + "filter.PermanentToughnessAtMostPredicate;\n"),
        (P + "trigger.ObjectRelation;\n",
         P + "trigger.ObjectRelation;\n" + P + "trigger.TriggerOptions;\n"),
        (P + "trigger.Triggers;\n",
         P + "trigger.Triggers;\n\nimport java.util.List;\n"),
        ("""        // Whenever a creature you control enters, if that creature is 1/1, put two +1/+1 counters on it.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                new EnteringCreatureExactStatsConditionalEffect(1, 1,
                        new PutCountersOnEnteringCreatureEffect(2, false)));
""",
         """        // Whenever a creature you control enters, if that creature is 1/1, put two +1/+1 counters on it. The
        // intervening "if" is judged as the creature enters and again as the ability resolves (CR 603.4).
        TriggeringPermanentMatches oneOne = new TriggeringPermanentMatches(new PermanentAllOfPredicate(List.of(
                new PermanentPowerAtLeastPredicate(1), new PermanentPowerAtMostPredicate(1),
                new PermanentToughnessAtLeastPredicate(1), new PermanentToughnessAtMostPredicate(1))));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(oneOne),
                new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING,
                        CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2)));
"""),
    ],
    "SocialSnub": [
        (P + "effect.CopyThisSpellIfConditionEffect;\n",
         P + "effect.CopyTriggeringSpellEffect;\n"),
        (P + "effect.LoseLifeRecipient;\n",
         P + "effect.LoseLifeRecipient;\n" + P + "effect.MayEffect;\n"),
        (P + "trigger.Triggers;\n",
         P + "trigger.TriggerOptions;\n" + P + "trigger.Triggers;\n"),
        ("""        addTrigger(Triggers.youCastThisSpell(), CopyThisSpellIfConditionEffect.whenCastWhile(
                new ControlsPermanent(new PermanentIsCreaturePredicate()), true));
""",
         """        // "When you cast this spell while you control a creature": the event condition.
        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withEventCondition(new ControlsPermanent(new PermanentIsCreaturePredicate())),
                new MayEffect(new CopyTriggeringSpellEffect(), "Copy Social Snub?"));
"""),
    ],
    "SageOfTheSkies": [
        (P + "effect.CopyThisSpellIfConditionEffect;\n",
         P + "effect.CopyTriggeringSpellEffect;\n"),
        (P + "trigger.Triggers;\n",
         P + "trigger.TriggerOptions;\n" + P + "trigger.Triggers;\n"),
        ("""        addTrigger(Triggers.youCastThisSpell(), new CopyThisSpellIfConditionEffect(
                new ControllerCastAnotherSpellThisTurn(new CardTruePredicate()), true, false, true));
""",
         """        // "When you cast this spell, if you've cast another spell this turn": the intervening "if".
        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerCastAnotherSpellThisTurn(new CardTruePredicate())),
                new CopyTriggeringSpellEffect(true));
"""),
    ],
    "MentorsGuidance": [
        (P + "effect.CopyThisSpellIfConditionEffect;\n",
         P + "effect.ConditionalEffect;\n" + P + "effect.CopyTriggeringSpellEffect;\n"),
        ("""        addTrigger(Triggers.youCastThisSpell(), new CopyThisSpellIfConditionEffect(
                new ControlsPermanent(new PermanentAnyOfPredicate(List.of(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentHasAnySubtypePredicate(Set.of(
                                CardSubtype.CLERIC,
                                CardSubtype.DRUID,
                                CardSubtype.SHAMAN,
                                CardSubtype.WARLOCK,
                                CardSubtype.WIZARD
                        ))
                )))
        ));
""",
         """        // "Copy it if you control …" is checked as the ability resolves.
        addTrigger(Triggers.youCastThisSpell(), new ConditionalEffect(
                new ControlsPermanent(new PermanentAnyOfPredicate(List.of(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentHasAnySubtypePredicate(Set.of(
                                CardSubtype.CLERIC,
                                CardSubtype.DRUID,
                                CardSubtype.SHAMAN,
                                CardSubtype.WARLOCK,
                                CardSubtype.WIZARD
                        ))
                ))),
                new CopyTriggeringSpellEffect()
        ));
"""),
    ],
    "MaliciousAffliction": [
        (P + "effect.CopyThisSpellIfConditionEffect;\n",
         P + "effect.CopyTriggeringSpellEffect;\n"),
        (P + "effect.DestroyTargetPermanentEffect;\n",
         P + "effect.DestroyTargetPermanentEffect;\n" + P + "effect.MayEffect;\n"),
        (P + "trigger.Triggers;\n",
         P + "trigger.TriggerOptions;\n" + P + "trigger.Triggers;\n"),
        ("""        addTrigger(Triggers.youCastThisSpell(), CopyThisSpellIfConditionEffect.whenCastWhile(new Morbid(), true));
""",
         """        // Morbid: "When you cast this spell, if a creature died this turn": the intervening "if".
        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new Morbid()),
                new MayEffect(new CopyTriggeringSpellEffect(), "Copy Malicious Affliction?"));
"""),
    ],
    "LumaretsFavor": [
        (P + "effect.CopyThisSpellIfConditionEffect;\n",
         P + "effect.ConditionalEffect;\n" + P + "effect.CopyTriggeringSpellEffect;\n"),
        ("""        addTrigger(Triggers.youCastThisSpell(), new CopyThisSpellIfConditionEffect(new GainedLifeThisTurn()));
""",
         """        addTrigger(Triggers.youCastThisSpell(),
                new ConditionalEffect(new GainedLifeThisTurn(), new CopyTriggeringSpellEffect()));
"""),
    ],
    "InspiringEasel": [
        (P + "condition.AllOf;\n" + P + "effect.CopyThisSpellIfConditionEffect;\n",
         P + "effect.CopyTriggeringSpellEffect;\n"),
        ("""                        new CopyThisSpellIfConditionEffect(new AllOf(List.of())))),
""",
         """                        new CopyTriggeringSpellEffect())),
"""),
    ],
    "BygoneMarvels": [
        (P + "effect.CopyThisSpellIfConditionEffect;\n",
         P + "effect.CopyTriggeringSpellEffect;\n"),
        ("""                new CopyThisSpellIfConditionEffect(descendEight, false, false, false, 2));
""",
         """                new CopyTriggeringSpellEffect().withCopies(2));
"""),
    ],
    "BrassKnuckles": [
        (P + "condition.AllOf;\n", ""),
        (P + "effect.CopyThisSpellIfConditionEffect;\n",
         P + "effect.CopyTriggeringSpellEffect;\n"),
        ("""        addTrigger(Triggers.youCastThisSpell(),
                new CopyThisSpellIfConditionEffect(new AllOf(List.of()), true));
""",
         """        addTrigger(Triggers.youCastThisSpell(), new CopyTriggeringSpellEffect(true));
"""),
        ("\nimport java.util.List;\n\n", "\n"),
    ],
    "BanishIntoFable": [
        (P + "condition.AllOf;\n", ""),
        (P + "effect.CopyThisSpellIfConditionEffect;\n",
         P + "effect.ConditionalEffect;\n" + P + "effect.CopyTriggeringSpellEffect;\n"),
        (P + "effect.ReturnToHandEffect;\n",
         P + "effect.ReturnToHandEffect;\n" + P + "effect.SequenceEffect;\n"),
        (P + "trigger.Triggers;\n",
         P + "trigger.TriggerOptions;\n" + P + "trigger.Triggers;\n"),
        ("""        addTrigger(Triggers.youCastThisSpell(), new CopyThisSpellIfConditionEffect(new AllOf(List.of(
                new CastFromZone(Zone.HAND),
                new ControlsPermanent(new PermanentIsArtifactPredicate())))));
        addTrigger(Triggers.youCastThisSpell(), new CopyThisSpellIfConditionEffect(new AllOf(List.of(
                new CastFromZone(Zone.HAND),
                new ControlsPermanent(new PermanentIsEnchantmentPredicate())))));
""",
         """        // "When you cast this spell from your hand" is the event condition; "copy it if you control an artifact,
        // then copy it if you control an enchantment" is one ability whose two "if"s are checked as it resolves.
        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withEventCondition(new CastFromZone(Zone.HAND)),
                SequenceEffect.of(
                        new ConditionalEffect(new ControlsPermanent(new PermanentIsArtifactPredicate()),
                                new CopyTriggeringSpellEffect()),
                        new ConditionalEffect(new ControlsPermanent(new PermanentIsEnchantmentPredicate()),
                                new CopyTriggeringSpellEffect())));
"""),
    ],
    "AncestralCommunion": [
        (P + "effect.CopyThisSpellIfConditionEffect;\n",
         P + "effect.CopyTriggeringSpellEffect;\n"),
        (P + "trigger.Triggers;\n",
         P + "trigger.TriggerOptions;\n" + P + "trigger.Triggers;\n"),
        ("""        addTrigger(Triggers.youCastThisSpell(),
                CopyThisSpellIfConditionEffect.whenCastWhile(new ControlledCommanderAsCast(), false));
""",
         """        // "When you cast this spell while you control your commander": the event condition.
        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withEventCondition(new ControlledCommanderAsCast()),
                new CopyTriggeringSpellEffect());
"""),
    ],
}

DONE_MARKERS = {
    "WoodenStake": "new DestroyCombatOpponentEffect(true)",
    "HedronBlade": "becomesBlocked().withFilter(new PermanentIsColorlessPredicate())",
    "SigilCaptain": "withInterveningIf(oneOne)",
    "SocialSnub": "CopyTriggeringSpellEffect",
    "SageOfTheSkies": "CopyTriggeringSpellEffect",
    "MentorsGuidance": "CopyTriggeringSpellEffect",
    "MaliciousAffliction": "CopyTriggeringSpellEffect",
    "LumaretsFavor": "CopyTriggeringSpellEffect",
    "InspiringEasel": "CopyTriggeringSpellEffect",
    "BygoneMarvels": "CopyTriggeringSpellEffect",
    "BrassKnuckles": "CopyTriggeringSpellEffect",
    "BanishIntoFable": "CopyTriggeringSpellEffect",
    "AncestralCommunion": "CopyTriggeringSpellEffect",
}


def main():
    changed = []
    seen = set()
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).glob("src/main/java/**/*.java")):
            edits = EDITS.get(path.stem)
            if edits is None:
                continue
            seen.add(path.stem)
            source = path.read_text()
            if DONE_MARKERS[path.stem] in source:
                continue
            result = source
            for old, new in edits:
                if result.count(old) != 1:
                    raise SystemExit(f"{path}: expected one occurrence of {old!r}")
                result = result.replace(old, new)
            path.write_text(result)
            changed.append(str(path))
    for path in changed:
        print("converted", path)
    print(f"{len(changed)} converted, {len(seen)} seen")


if __name__ == "__main__":
    main()
