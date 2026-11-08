#!/usr/bin/env python3
"""Step 15, run 17x3b: the agent-docs rows of the enter family's "it / that creature" markers.

Each edit replaces (or, with ``None``, drops) the one line of a doc that starts with the given fragment, or appends a
sentence to the line that holds an anchor. Idempotent: an edit whose result is already there is skipped. Fails if a
doc or skill still names one of the removed markers.

Usage: step15-run17x3b-docs.py
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent

REMOVED = [
    "UntapEnteringPermanentEffect", "TransformEnteringCreatureEffect", "PutCountersOnEnteringCreatureEffect`",
    "PutCountersOnEnteringCreatureEffect(", "BoostEnteringCreatureEffect", "AttachSourceAuraToEnteringCreatureEffect",
    "AttachSourceEquipmentToEnteringCreatureEffect", "BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect",
]

COUNTERS_IT = ("`PutCounterOnTargetPermanentEffect.onTriggeringPermanent(CounterType, DynamicAmount | int)`")

# doc -> [(line prefix, replacement line or None)]
LINES = {
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        ("- ~~`EnteringCreatureHasCountersConditionalEffect`~~ (removed",
         "- ~~`EnteringCreatureHasCountersConditionalEffect`~~ (removed: Ichorplate Golem's \"if it has one or more oil "
         "counters on it\" is the intervening \"if\" `TriggeringPermanentMatches(PermanentHasCountersPredicate(OIL))`)"),
        ("- `PutCountersOnEnteringCreatureEffect(",
         f"- {COUNTERS_IT} — \"put N counters on it / that creature\" of an enter trigger (or any trigger whose stack "
         "entry records its triggering permanent): not targeted, placed on the entry's triggering permanent wherever it "
         "is as the ability resolves; the count is evaluated against the source, and the controller orders counter "
         "replacements as for a target. Good Fortune Unicorn `(PLUS_ONE_PLUS_ONE, 1)`, Railway Brawler's \"X is its "
         "power\" `new TriggeringPermanentPower()`; \"you may\" is a `MayEffect` (Mighty Emergence); \"and it gains "
         "haste\" a `SequenceEffect.of(…, new GrantKeywordEffect(Keyword.HASTE, GrantScope.TRIGGERING_PERMANENT))` "
         "(Wartime Protestors, Surrak and Goreclaw); a condition on the entering creature is the trigger's filter or, "
         "for \"if it has …\", an intervening \"if\" `TriggeringPermanentMatches` (Ichorplate Golem, Sigil Captain)"),
        ("- `BoostEnteringCreatureEffect(",
         "- \"that creature gets +X/+Y [and gains haste] until end of turn\" of an enter trigger: "
         "`BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, X, Y)` (Primal Forcemage), with the keyword "
         "`new GrantKeywordEffect(Keyword.HASTE, GrantScope.TRIGGERING_PERMANENT[, GrantDuration])` in one "
         "`SequenceEffect.of(…)` (Ogre Battledriver), or the grant alone for \"it gains haste\" (Rapid Augmenter; "
         "Chainer's `UNTIL_YOUR_NEXT_TURN`); both read the stack entry's triggering permanent as it resolves"),
        ("- `UntapEnteringPermanentEffect()`",
         "- \"Whenever a permanent you control enters tapped, untap it\": `Triggers.enters(new "
         "PermanentIsTappedPredicate(), ObjectRelation.YOU_CONTROL)` with `UntapPermanentsEffect(TapUntapScope."
         "TRIGGERING)` (Amulet of Vigor); \"enters tapped\" is the trigger's filter, judged on the permanent as it "
         "entered"),
        ("- `TransformEnteringCreatureEffect([boolean optional])` — Triggers.enters(", None),
        ("- `TransformTargetPermanentEffect()` — transform the permanent referenced by stack `targetId`",
         "- `TransformTargetPermanentEffect()` — transform the permanent referenced by stack `targetId` (other face); "
         "`TransformTargetPermanentEffect.ofTriggeringPermanent()` the stack entry's triggering permanent, not "
         "targeted (Vildin-Pack Alpha's \"you may transform it\": `MayEffect(TransformTargetPermanentEffect"
         ".ofTriggeringPermanent(), …)`); no-op if no back face / transform prevented"),
        ("- `AttachSourceEquipmentToEnteringCreatureEffect(", None),
        ("- `AttachSourceEquipmentToTargetCreatureEffect([CardEffect thenEffect, boolean thenEffectOptionalTarget])`",
         "- `AttachSourceEquipmentToTargetCreatureEffect([CardEffect thenEffect, boolean thenEffectOptionalTarget])` — "
         "attaches the source Equipment to target creature; when `thenEffect` is present, queues it as a reflexive "
         "ability only after the attachment succeeds. Used by Novel Nunchaku for its attach-then-fight rider. "
         "`forTriggeringCreature()` reads the triggering permanent without targeting: \"whenever a [subtype] creature "
         "enters, you may attach this Equipment to it\" is `MayEffect(AttachSourceEquipmentToTargetCreatureEffect"
         ".forTriggeringCreature(), …)` (Cloak and Dagger), the mandatory form bare (Ronin Warclub); combine with "
         "`QueueReflexiveAbilityEffect` for attach-after-payment triggers."),
        ("- `AttachSourceAuraToEnteringCreatureEffect(",
         "- `AttachSourceAuraToTargetCreatureEffect()` — moves the source Aura (already on the battlefield) onto "
         "target creature; `AttachSourceAuraToTargetCreatureEffect.ofTriggeringPermanent()` onto the stack entry's "
         "triggering permanent, not targeted: \"whenever a creature [an opponent controls] enters, you may attach this "
         "Aura to that creature\" is `MayEffect(AttachSourceAuraToTargetCreatureEffect.ofTriggeringPermanent(), …)` "
         "(Shielded by Faith, Prison Term), the mandatory form bare (Illusory Gains)"),
        ("- `BecomeCopyOfEnteringCreatureEffect()` — source permanent becomes a copy",
         "- `BecomeCopyOfEnteringCreatureEffect()` — source permanent becomes a copy of the stack entry's triggering "
         "permanent (the creature that entered, as it is, else as it last existed on the battlefield), retaining its "
         "own enters-copy trigger (\"except it has this ability\"). Unstable Shapeshifter. Place in "
         "`Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER)`, mandatory"),
        ("- `BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect()`",
         "- \"You may have this creature become a copy of that creature until end of turn\" of an enter trigger: "
         "`MayEffect(new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect([nameOverride, supertypes]).ofTriggeringPermanent(), "
         "…)` — copies the stack entry's triggering permanent, not targeted (as it is, else as it last existed on the "
         "battlefield); the copy does not retain the trigger and reverts at cleanup (Renegade Doppelganger, Sarkhan, "
         "Soul Aflame)"),
        ("- `TransformEnteringCreatureEffect([boolean optional])` / `TransformTargetPermanentEffect()`",
         "- `MayEffect(TransformTargetPermanentEffect.ofTriggeringPermanent(), …)` — may transform the entering "
         "creature (Vildin-Pack Alpha); see counters section above"),
    ],
    "agent-docs/EFFECTS_INDEX.md": [
        ("| ~~`EnteringCreatureHasCountersConditionalEffect`~~ (removed)",
         "| ~~`EnteringCreatureHasCountersConditionalEffect`~~ (removed) | | Use the trigger's filter, or for \"if it "
         "has …\" the intervening \"if\" `TriggeringPermanentMatches(PermanentHasCountersPredicate(counterType))` with "
         "`PutCounterOnTargetPermanentEffect.onTriggeringPermanent(counterType, 1)` (Ichorplate Golem) |"),
        ("| `PutCountersOnEnteringCreatureEffect` | `(CounterType counterType, int count, boolean optional", None),
        ("| `PutCountersOnEnteringCreatureEffect` | `(int count[, boolean optional])`",
         "| `PutCounterOnTargetPermanentEffect.onTriggeringPermanent` | `(CounterType counterType, DynamicAmount "
         "amount)` or `(CounterType, int count)` | \"put N counters on it / that creature\": the stack entry's "
         "triggering permanent (the creature that entered), not targeted, wherever it is as the ability resolves; the "
         "count is evaluated against the source and the controller orders counter replacements. \"You may\" a "
         "`MayEffect` (Mighty Emergence), \"and it gains haste\" a `SequenceEffect` with `GrantKeywordEffect(HASTE, "
         "TRIGGERING_PERMANENT)` (Wartime Protestors), \"if it has …\" the intervening \"if\" `TriggeringPermanentMatches` "
         "(Ichorplate Golem) |"),
        ("| `BoostEnteringCreatureEffect` |",
         "| `BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, X, Y)` + `GrantKeywordEffect(keywords, "
         "GrantScope.TRIGGERING_PERMANENT[, duration])` | | \"that creature gets +X/+Y and gains [keywords]\" of an "
         "enter trigger, one `SequenceEffect.of(…)` (Ogre Battledriver `(2, 0)` + haste), the boost alone (Primal "
         "Forcemage) or the grant alone (Rapid Augmenter; Chainer `UNTIL_YOUR_NEXT_TURN`), reading the entry's "
         "triggering permanent |"),
        ("| `UntapEnteringPermanentEffect` |",
         "| `UntapPermanentsEffect(TapUntapScope.TRIGGERING)` on `Triggers.enters(new PermanentIsTappedPredicate(), "
         "ObjectRelation.YOU_CONTROL)` | `()` | \"whenever a permanent you control enters tapped, untap it\": the "
         "tapped entry is the trigger's filter, \"it\" the entry's triggering permanent. Amulet of Vigor |"),
        ("| `TransformEnteringCreatureEffect` |", None),
        ("| `TransformTargetPermanentEffect` | `()` |",
         "| `TransformTargetPermanentEffect` | `()` or `.ofTriggeringPermanent()` | Transform the permanent referenced "
         "by stack `targetId` (flip to other face), or the entry's triggering permanent, not targeted (Vildin-Pack "
         "Alpha's \"you may transform it\": `MayEffect(TransformTargetPermanentEffect.ofTriggeringPermanent(), …)`). "
         "No-op if the permanent has no back face or transform is prevented |"),
        ("| `BecomeCopyOfEnteringCreatureEffect` |",
         "| `BecomeCopyOfEnteringCreatureEffect` | `()` | source permanent becomes a copy of the stack entry's "
         "triggering permanent (the creature that entered, as it is, else as it last existed on the battlefield), "
         "re-granting the source's own `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER)` ability (\"except "
         "it has this ability\"). Mandatory (no `MayEffect`). Used by Unstable Shapeshifter |"),
        ("| `BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect` |",
         "| `BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(…).ofTriggeringPermanent()` | | \"you may have this "
         "creature become a copy of that creature until end of turn\" of an enter trigger, in a `MayEffect`: copies the "
         "stack entry's triggering permanent, not targeted (as it is, else as it last existed on the battlefield); the "
         "temporary copy does not retain the trigger and reverts at cleanup. Renegade Doppelganger, Sarkhan, Soul "
         "Aflame |"),
        ("| `AttachSourceEquipmentToEnteringCreatureEffect` |",
         "| `AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature()` | `()` | attach this Equipment to "
         "the stack entry's triggering permanent, not targeted: \"whenever a [subtype] creature enters, you may attach "
         "this Equipment to it\" is `MayEffect(….forTriggeringCreature(), …)` (Cloak and Dagger), the mandatory form "
         "bare (Ronin Warclub) |"),
        ("| `AttachSourceAuraToTargetCreatureEffect` | `()` |",
         "| `AttachSourceAuraToTargetCreatureEffect` | `()` or `.ofTriggeringPermanent()` | move the source Aura "
         "(already on the battlefield) onto target creature, or onto the stack entry's triggering permanent, not "
         "targeted. Reads targetId (or the triggering permanent) as creature, sourcePermanentId as the Aura; reattaches "
         "and gives a new timestamp (CR 613.7e). \"You may attach this Aura to that creature\" is "
         "`MayEffect(AttachSourceAuraToTargetCreatureEffect.ofTriggeringPermanent(), …)` (Prison Term, Shielded by "
         "Faith), the mandatory form bare (Illusory Gains) |"),
        ("| `AttachSourceAuraToEnteringCreatureEffect` |", None),
    ],
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"Whenever a creature you control enters, if it has one or more oil counters on it, put an oil counter on it\" |",
         "| \"Whenever a creature you control enters, if it has one or more oil counters on it, put an oil counter on "
         "it\" | `PutCounterOnTargetPermanentEffect.onTriggeringPermanent(OIL, 1)` | `Triggers.enters(Triggers.CREATURE, "
         "ObjectRelation.YOU_CONTROL)` with `TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentMatches("
         "new PermanentHasCountersPredicate(OIL)))` | Ichorplate Golem; the intervening \"if\" is judged as the creature "
         "enters and again as the ability resolves (CR 603.4), and the counter is placed on the entering creature |"),
        ("| \"Whenever another creature you control enters, that creature gets +X/+Y and gains [keyword] until end of turn\" |",
         "| \"Whenever another creature you control enters, that creature gets +X/+Y and gains [keyword] until end of "
         "turn\" | `SequenceEffect.of(new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, X, Y), new "
         "GrantKeywordEffect(Keyword.…, GrantScope.TRIGGERING_PERMANENT))` | `Triggers.enters(Triggers.CREATURE, "
         "ObjectRelation.ANOTHER_YOU_CONTROL)` | Ogre Battledriver. Non-targeting: both effects read the stack entry's "
         "triggering permanent, the creature that entered. The relation already excludes the source itself |"),
        ("| \"Whenever another creature you control with base power 1 enters, it gains haste until end of turn\" |",
         "| \"Whenever another creature you control with base power 1 enters, it gains haste until end of turn\" | "
         "`new GrantKeywordEffect(Keyword.HASTE, GrantScope.TRIGGERING_PERMANENT)` | `Triggers.enters("
         "PermanentAllOfPredicate(Triggers.CREATURE, PermanentBasePowerEqualsPredicate(1)), ObjectRelation."
         "ANOTHER_YOU_CONTROL)` | Rapid Augmenter (BLC 38). The base-power predicate is the trigger's filter, judged on "
         "the entering permanent |"),
        ("| \"Whenever a nontoken creature you control enters, if you didn't cast it from your hand, it gains haste until your next turn\" |",
         "| \"Whenever a nontoken creature you control enters, if you didn't cast it from your hand, it gains haste "
         "until your next turn\" | `new GrantKeywordEffect(Keyword.HASTE, GrantScope.TRIGGERING_PERMANENT, "
         "GrantDuration.UNTIL_YOUR_NEXT_TURN)` | `Triggers.enters(PermanentAllOfPredicate(Triggers.CREATURE, "
         "Triggers.NONTOKEN, PermanentNotPredicate(PermanentCastFromZonePredicate(Zone.HAND))), ObjectRelation."
         "YOU_CONTROL)` | Chainer, Nightmare Adept. The filter checks the entering permanent's cast flag and source "
         "zone and excludes tokens |"),
        ("| \"Whenever a permanent you control enters tapped, untap it.\" |",
         "| \"Whenever a permanent you control enters tapped, untap it.\" | `UntapPermanentsEffect(TapUntapScope."
         "TRIGGERING)` | `Triggers.enters(new PermanentIsTappedPredicate(), ObjectRelation.YOU_CONTROL)` | Amulet of "
         "Vigor. \"Enters tapped\" is the trigger's filter, judged on the permanent as it entered; \"it\" is the stack "
         "entry's triggering permanent, not targeted. |"),
        ("| \"Whenever a [subtype] creature enters, you may attach this Equipment to it\" |",
         "| \"Whenever a [subtype] creature enters, you may attach this Equipment to it\" | `new MayEffect("
         "AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature(), \"Attach this Equipment to it?\")` | "
         "`Triggers.enters(PermanentAllOfPredicate(Triggers.CREATURE, PermanentHasSubtypePredicate(subtype)), "
         "ObjectRelation.ANOTHER)` | Cloak and Dagger (Rogue). Non-targeting \"may attach\" to the entering creature "
         "(any player's), the stack entry's triggering permanent. |"),
        ("| \"Whenever a creature you control enters, attach this Equipment to that creature\" |",
         "| \"Whenever a creature you control enters, attach this Equipment to that creature\" | "
         "`AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature()` | `Triggers.enters(Triggers.CREATURE, "
         "ObjectRelation.ANOTHER_YOU_CONTROL)` | Ronin Warclub. Mandatory non-targeting attachment to the stack entry's "
         "triggering permanent. |"),
        ("| \"Whenever a creature an opponent controls enters, you may attach this Aura to that creature\" |",
         "| \"Whenever a creature an opponent controls enters, you may attach this Aura to that creature\" | `new "
         "MayEffect(AttachSourceAuraToTargetCreatureEffect.ofTriggeringPermanent(), \"Attach this Aura to that "
         "creature?\")` | `Triggers.enters(Triggers.CREATURE, ObjectRelation.OPPONENT_CONTROLS)` | Prison Term (Aura). "
         "Non-targeting \"may attach\" moving the Aura onto the entering opponent creature, the stack entry's "
         "triggering permanent. Pair with the STATIC lockdown effects. |"),
        ("| \"Whenever a creature an opponent controls enters, attach this Aura to that creature\" |",
         "| \"Whenever a creature an opponent controls enters, attach this Aura to that creature\" | "
         "`target(TargetFilters.creature())` + `STATIC ControlEnchantedCreatureEffect()` + `Triggers.enters("
         "Triggers.CREATURE, ObjectRelation.OPPONENT_CONTROLS) AttachSourceAuraToTargetCreatureEffect"
         ".ofTriggeringPermanent()` | Aura | Illusory Gains. Mandatory non-targeting attachment moves the Aura onto the "
         "entering opponent creature, and the static control effect follows the Aura. |"),
        ("| \"Whenever a [Subtype] you control enters, you may transform it\" |",
         "| \"Whenever a [Subtype] you control enters, you may transform it\" | `new MayEffect("
         "TransformTargetPermanentEffect.ofTriggeringPermanent(), \"Transform it?\")` | `Triggers.enters("
         "PermanentAllOfPredicate(Triggers.CREATURE, PermanentHasSubtypePredicate(subtype)), ObjectRelation."
         "YOU_CONTROL)` | Vildin-Pack Alpha (Werewolf). Non-targeting \"it\": the stack entry's triggering permanent. "
         "No-op on non-DFCs / transform-prevented. Says \"a [Subtype] you control\" rather than \"another\", so the "
         "relation is `YOU_CONTROL` and the source's own entry is judged by the filter too |"),
        ("| \"Whenever a creature you control with power N or greater enters, you may put M +1/+1 counters on it\" |",
         "| \"Whenever a creature you control with power N or greater enters, you may put M +1/+1 counters on it\" | "
         "`new MayEffect(PutCounterOnTargetPermanentEffect.onTriggeringPermanent(PLUS_ONE_PLUS_ONE, M), …)` | "
         "`Triggers.enters(PermanentAllOfPredicate(Triggers.CREATURE, PermanentPowerAtLeastPredicate(N)), "
         "ObjectRelation.YOU_CONTROL)` | Mighty Emergence (N=5, M=2). Counters go on the entering creature (\"it\", the "
         "stack entry's triggering permanent), not the source. Drop the power filter (plain `Triggers.CREATURE`) for an "
         "unconditional \"whenever a creature you control enters\" variant. |"),
        ("| \"Whenever another creature enters, this creature becomes a copy of that creature, except it has this ability\" |",
         "| \"Whenever another creature enters, this creature becomes a copy of that creature, except it has this "
         "ability\" | `BecomeCopyOfEnteringCreatureEffect()` | `Triggers.enters(Triggers.CREATURE, ObjectRelation."
         "ANOTHER)` | Copies the stack entry's triggering permanent as it is (else as it last existed on the "
         "battlefield) and re-grants this trigger. Unstable Shapeshifter |"),
        ("| \"Whenever another creature you control enters, you may have this creature become a copy of that creature until end of turn\" |",
         "| \"Whenever another creature you control enters, you may have this creature become a copy of that creature "
         "until end of turn\" | `new MayEffect(new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect()"
         ".ofTriggeringPermanent(), …)` | `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` | "
         "Copies the stack entry's triggering permanent, not targeted, as it is or as it last existed on the "
         "battlefield. Renegade Doppelganger |"),
        ("| \"Whenever a Dragon you control enters, you may have Sarkhan become a copy of it until end of turn,",
         "| \"Whenever a Dragon you control enters, you may have Sarkhan become a copy of it until end of turn, except "
         "its name is Sarkhan, Soul Aflame and it's legendary in addition to its other types.\" | `new MayEffect(new "
         "BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(\"Sarkhan, Soul Aflame\", Set.of(LEGENDARY))"
         ".ofTriggeringPermanent(), …)` | `Triggers.enters(PermanentAllOfPredicate(Triggers.CREATURE, "
         "PermanentHasSubtypePredicate(DRAGON)), ObjectRelation.ANOTHER_YOU_CONTROL)` | The trigger's filter restricts "
         "it to entering Dragons; the temporary copy effect carries the retained name and added legendary supertype, "
         "then reverts at cleanup. Sarkhan, Soul Aflame (MAT 46) |"),
    ],
    "agent-docs/CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("| Boost + shroud + attach-to-entering-subtype equip |",
         "| Boost + shroud + attach-to-entering-subtype equip | `c/CloakAndDagger.java` | STATIC StaticBoostEffect(2, 0, "
         "EQUIPPED_CREATURE) + GrantKeywordEffect(SHROUD, EQUIPPED_CREATURE) + Triggers.enters(PermanentAllOfPredicate("
         "Triggers.CREATURE, PermanentHasSubtypePredicate(ROGUE)), ObjectRelation.ANOTHER) MayEffect("
         "AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature()) + equip. \"Whenever a Rogue creature "
         "enters, you may attach this Equipment to it\" — non-targeting may-attach to the entering creature (any "
         "controller), the stack entry's triggering permanent |"),
    ],
    "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| Another ally creature enters (pump it + haste) |",
         "| Another ally creature enters (pump it + haste) | `o/OgreBattledriver.java` | Triggers.enters("
         "Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL) SequenceEffect.of(BoostReferencedPermanentEffect("
         "PermanentReference.TRIGGERING, 2, 0), GrantKeywordEffect(HASTE, GrantScope.TRIGGERING_PERMANENT)) — \"that "
         "creature gets +2/+0 and gains haste until end of turn\"; mandatory, reading the stack entry's triggering "
         "permanent |"),
        ("| Ally subtype enters (may transform it) |",
         "| Ally subtype enters (may transform it) | `v/VildinPackAlpha.java` (front `g/GeierReachBandit.java`) | "
         "Triggers.enters(PermanentAllOfPredicate(Triggers.CREATURE, PermanentHasSubtypePredicate(WEREWOLF)), "
         "ObjectRelation.YOU_CONTROL) MayEffect(TransformTargetPermanentEffect.ofTriggeringPermanent()) — may transform "
         "the entering Werewolf (the stack entry's triggering permanent), not the source. Front: EACH_UPKEEP "
         "NoSpellsCastLastTurn→TransformSelf; back also EACH_UPKEEP TwoOrMoreSpellsCastLastTurn→TransformSelf |"),
        ("| Creature you control with power N+ enters (may put M +1/+1 counters on it) |",
         "| Creature you control with power N+ enters (may put M +1/+1 counters on it) | `m/MightyEmergence.java` | "
         "Triggers.enters(PermanentAllOfPredicate(Triggers.CREATURE, PermanentPowerAtLeastPredicate(5)), "
         "ObjectRelation.YOU_CONTROL) MayEffect(PutCounterOnTargetPermanentEffect.onTriggeringPermanent("
         "PLUS_ONE_PLUS_ONE, 2)) — counters go on the entering creature (the stack entry's triggering permanent), not "
         "the source. Enchantment, not a creature |"),
    ],
    "agent-docs/CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("| Oil-counter trigger plus counter-based anthem |",
         "| Oil-counter trigger plus counter-based anthem | `i/IchorplateGolem.java` | Triggers.enters(Triggers.CREATURE, "
         "ObjectRelation.YOU_CONTROL) with the intervening \"if\" `TriggeringPermanentMatches(PermanentHasCounters"
         "Predicate(OIL))` and `PutCounterOnTargetPermanentEffect.onTriggeringPermanent(OIL, 1)` plus STATIC "
         "`StaticBoostEffect(1, 1, ALL_OWN_CREATURES, PermanentHasCountersPredicate(OIL))` |"),
        ("| Lockdown + jump-to-entering-opponent-creature |",
         "| Lockdown + jump-to-entering-opponent-creature | `p/PrisonTerm.java` | STATIC "
         "EnchantedCreatureCantAttackOrBlockEffect + EnchantedCreatureCantActivateAbilitiesEffect + Triggers.enters("
         "Triggers.CREATURE, ObjectRelation.OPPONENT_CONTROLS) MayEffect(AttachSourceAuraToTargetCreatureEffect"
         ".ofTriggeringPermanent()) — \"you may attach this Aura to that creature\" moves the Aura onto each opponent "
         "creature that enters |"),
    ],
}

# doc -> [(anchor text in a line, old fragment in that line, new fragment)]
FRAGMENTS = {
    "agent-docs/CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("`t/ThornbiteStaff.java`", "ObjectRelation.ANOTHER) AttachSourceEquipmentToEnteringCreatureEffect",
         "ObjectRelation.ANOTHER) MayEffect(AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature())"),
        ("`d/DivinersWand.java`", "ObjectRelation.ANOTHER) AttachSourceEquipmentToEnteringCreatureEffect",
         "ObjectRelation.ANOTHER) MayEffect(AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature())"),
        ("`v/VeteransArmaments.java`", "ObjectRelation.ANOTHER) AttachSourceEquipmentToEnteringCreatureEffect",
         "ObjectRelation.ANOTHER) MayEffect(AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature())"),
    ],
    "agent-docs/EFFECTS_INDEX.md": [
        ("| `BecomeCopyOfTargetCreatureUntilEndOfTurnEffect` | `()` or `(nameOverride, additionalSupertypesOverride)` | source",
         "Place in Triggers.attacks() or a may-trigger path with a creature target.",
         "Place in Triggers.attacks() or a may-trigger path with a creature target; `.ofTriggeringPermanent()` "
         "copies the stack entry's triggering permanent instead, not targeted (Sarkhan, Soul Aflame, Renegade "
         "Doppelganger)."),
    ],
    "agent-docs/EFFECT_MERGE_PLAN.md": [
        ("`AttachSourceEquipmentToTargetCreatureEffect` (3).", "`AttachSourceEquipmentToTargetCreatureEffect` (3).",
         "`AttachSourceEquipmentToTargetCreatureEffect` (3). Both have a non-targeting form reading the stack entry's "
         "triggering permanent (`AttachSourceAuraToTargetCreatureEffect.ofTriggeringPermanent()`, "
         "`AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature()`, step 15 run 17x3b) that the merged "
         "effect keeps."),
    ],
    "agent-docs/ARCHITECTURE.md": [
        ("never in a rebuilt effect (Archon of Redemption, Hamletback Goliath, Cream of the Crop).",
         "never in a rebuilt effect (Archon of Redemption, Hamletback Goliath, Cream of the Crop).",
         "never in a rebuilt effect (Archon of Redemption, Hamletback Goliath, Cream of the Crop). Likewise an enter "
         "trigger's \"put counters on it\", \"it gets +X/+Y\", \"transform it\", \"attach this to it\" and \"become a copy "
         "of that creature\" are effects the card writes reading the entry's triggering permanent "
         "(`PutCounterOnTargetPermanentEffect.onTriggeringPermanent`, `PermanentReference.TRIGGERING`, "
         "`GrantScope.TRIGGERING_PERMANENT`, the `ofTriggeringPermanent()` forms); the enter collector puts them on the "
         "stack as written."),
    ],
}


def read(path):
    return path.read_text(encoding="utf-8", errors="surrogateescape")


def write(path, text):
    path.write_text(text, encoding="utf-8", errors="surrogateescape")


def main():
    for doc, edits in LINES.items():
        path = ROOT / doc
        lines = read(path).split("\n")
        changed = False
        for prefix, new in edits:
            matches = [i for i, line in enumerate(lines) if line.startswith(prefix)]
            if new is not None and new in lines and not any(lines[i] != new for i in matches):
                continue
            if new is None and not matches:
                continue
            if len(matches) != 1:
                raise SystemExit(f"{doc}: {len(matches)} lines start with {prefix!r}")
            if new is None:
                del lines[matches[0]]
            else:
                lines[matches[0]] = new
            changed = True
        if changed:
            write(path, "\n".join(lines))
            print(f"updated {doc}")
    for doc, edits in FRAGMENTS.items():
        path = ROOT / doc
        lines = read(path).split("\n")
        changed = False
        for anchor, old, new in edits:
            matches = [i for i, line in enumerate(lines) if anchor in line]
            if len(matches) != 1:
                raise SystemExit(f"{doc}: {len(matches)} lines hold {anchor!r}")
            line = lines[matches[0]]
            if new in line:
                continue
            if old not in line:
                raise SystemExit(f"{doc}: {old!r} not in the line holding {anchor!r}")
            lines[matches[0]] = line.replace(old, new, 1)
            changed = True
        if changed:
            write(path, "\n".join(lines))
            print(f"updated {doc}")
    stale = []
    for path in list((ROOT / "agent-docs").glob("*.md")) + list(ROOT.glob(".claude/skills/**/*.md")) \
            + list(ROOT.glob(".codex/skills/**/*.md")):
        text = read(path)
        for name in REMOVED:
            if name in text:
                stale.append(f"{path.relative_to(ROOT)}: {name}")
    if stale:
        raise SystemExit("still named:\n" + "\n".join(stale))


if __name__ == "__main__":
    sys.exit(main())
