#!/usr/bin/env python3
"""Step 15, runs 17z11c2 and 17z11d: the agent-docs rows of the forms the two runs changed.

Idempotent; fails if a doc or skill still names a removed form outside a "Gone" line. Exact line edits in
`ORACLE_TEXT_EFFECT_MAP.md`, `EFFECTS_QUICK_REFERENCE.md`, `EFFECTS_INDEX.md`, `ACTIVATED_ABILITY_GUIDE.md`,
`CARD_PATTERNS_CREATURES_TRIGGERED.md`, `CARD_PATTERNS_PERMANENTS_STATIC.md`, `CARD_PATTERN_INDEX.md`,
`PREDICATES_REFERENCE.md` and `TRIGGER_SLOT_TARGETING.md`. `EFFECT_MERGE_PLAN.md` is history and is left.
"""
import pathlib
import re
import sys

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else '.')
docs = root / 'agent-docs'

GONE_17Z11C2 = '**Gone** (effect-slot-refactor.md step 15, run 17z11c2)'
GONE_17Z11D = '**Gone** (effect-slot-refactor.md step 15, run 17z11d)'

EDITS = {
    'ORACLE_TEXT_EFFECT_MAP.md': [
        ("`Triggers.youAttack() BoostCreaturesInAttackingBandsEffect()` | STATIC + Triggers.youAttack() |",
         "`Triggers.youAttack()` with `TriggerOptions.DEFAULT.withMultiplicity(TriggerMultiplicity.EACH_BAND)` + "
         "`BoostCreaturesInAttackingBandsEffect()` | STATIC + Triggers.youAttack() |"),
        ("| `ChooseModeNotChosenDuringLastCombatEffect(List.of(CreateTokenEffect.ofPowerstoneToken(...), "
         "SeekLibraryEffect(..., new ManaValueBound(powerstones, true, 0))))` | Triggers.attacks() | Yotian Courier "
         "(YBRO 25). The attack collector removes the source permanent's remembered last mode, then the normal "
         "triggered-modal flow remembers the newly selected label for the next combat; the Seek bound is evaluated at "
         "resolution |",
         "| `ChooseOneAtTriggerTimeEffect(ChooseOneEffect.notChosenDuringLastCombat(List.of(CreateTokenEffect"
         ".ofPowerstoneToken(...), SeekLibraryEffect(..., new ManaValueBound(powerstones, true, 0)))))` | "
         "Triggers.attacks() | Yotian Courier (YBRO 25). The modal's restriction leaves out the source permanent's "
         "remembered last mode as the mode is chosen, and the chosen label is remembered for the next combat; the Seek "
         "bound is evaluated at resolution |"),
        ("| `BoostBlockerWhenAttackerMatchesEffect(new PermanentColorInPredicate(Set.of(BLACK, RED)), 1, 1)` | "
         "`Triggers.creatureBecomesBlocked()` | Righteous Indignation. The attacker predicate is evaluated when "
         "blockers are declared; the trigger materializes a non-targeting boost for the blocker once per qualifying "
         "attacker/blocker pair |",
         "| `BoostTargetCreatureEffect(1, 1)` with no declared target (\"that creature\", the blocker) | "
         "`Triggers.creatureBecomesBlocked().withFilter(new PermanentColorInPredicate(Set.of(BLACK, RED)))` with "
         "`TriggerOptions.EACH_BLOCKER` | Righteous Indignation. Once per declared attacker/blocker pair whose attacker "
         "matches the filter as blockers are declared; the entry's triggering permanent is the attacker and its "
         "non-targeting target the blocker |"),
        ("| `BoostSelfEffect(new CreaturesBlockingSource(), new CreaturesBlockingSource())` | "
         "Triggers.creatureBecomesBlocked().withFilter(PermanentHasSubtypePredicate(SUBTYPE)) | Spined Sliver; once "
         "per matching blocked attacker across all controllers |",
         "| `BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, new CreaturesBlockingSource(true), new "
         "CreaturesBlockingSource(true))` | Triggers.creatureBecomesBlocked().withFilter(PermanentHasSubtypePredicate("
         "SUBTYPE)) | Spined Sliver; once per matching blocked attacker across all controllers, the watcher the "
         "ability's source and the blocked creature its triggering permanent |"),
        ("| `BoostSelfEffect(N, M)` (self-scoped to \"it\"; `.withFilter(...)` on the trigger filters the blocked "
         "creature, Tattered Ratter) | Triggers.creatureYouControlBecomesBlocked() | Unstoppable Ash. Fires once per "
         "blocked attacker; the blocked creature is the non-targeting sourcePermanentId |",
         "| `BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, N, M)` (`.withFilter(...)` on the trigger "
         "filters the blocked creature, Tattered Ratter) | Triggers.creatureYouControlBecomesBlocked() | Unstoppable "
         "Ash. Fires once per blocked attacker; the watcher is the source and \"it\" the entry's triggering permanent "
         "(`ReturnToHandEffect.triggering()`, `UntapPermanentsEffect(TapUntapScope.TRIGGERING)`, "
         "`GrantKeywordEffect(..., GrantScope.TRIGGERING_PERMANENT)`, `PutCounterOnReferencedPermanentEffect(TRIGGERING"
         ", ...)`, \"blocking it\" `PermanentBlockingSourcePredicate.ofTriggeringPermanent()`) |"),
        ("`GrantEffectToSourceOfChosenColorUntilEndOfTurnEffect(Triggers.becomesBlocked(), new "
         "DestroyCreatureBlockingThisEffect())` | activated ability | Zombie Boa.",
         "`GrantEffectToSourceOfChosenColorUntilEndOfTurnEffect(new TriggeredAbility(Triggers.becomesBlocked(), "
         "TriggerOptions.EACH_BLOCKER, new DestroyCreatureBlockingThisEffect()))` | activated ability | Zombie Boa."),
    ],
    'EFFECTS_QUICK_REFERENCE.md': [
        ("- `BoostCreaturesInAttackingBandsEffect()` - `Triggers.youAttack()` emits a separate trigger for each "
         "declared band of at least two creatures. Captured member IDs preserve the triggering band; surviving "
         "attacking members receive +N/+N, where N is the number still in the band at resolution (Chatzuk, Mighty "
         "Guitarist).",
         "- `BoostCreaturesInAttackingBandsEffect()` - on `Triggers.youAttack()` with "
         "`TriggerOptions.DEFAULT.withMultiplicity(TriggerMultiplicity.EACH_BAND)` (\"whenever two or more creatures "
         "you control attack in a band\"), an ability of its own for each declared band of at least two creatures, "
         "whose attacking creatures are the band's; the ones still attacking in a band receive +N/+N, where N is "
         "their number at resolution (Chatzuk, Mighty Guitarist)."),
        ("- `BoostBlockerWhenAttackerMatchesEffect(PermanentPredicate, int, int)` — `Triggers.creatureBecomesBlocked()` "
         "trigger marker; evaluates the attacker predicate at block declaration and materializes a non-targeting "
         "+X/+Y boost for the blocker once per matching attacker/blocker pair (Righteous Indignation)",
         "- `BoostBlockerWhenAttackerMatchesEffect` — " + GONE_17Z11C2 + ": write "
         "`Triggers.creatureBecomesBlocked().withFilter(attacker filter)` with `TriggerOptions.EACH_BLOCKER` and "
         "`BoostTargetCreatureEffect(X, Y)` with no declared target (\"the blocking creature\", Righteous Indignation)"),
        ("- `ChooseModeNotChosenDuringLastCombatEffect(List<ChooseOneOption>)` — Triggers.attacks() marker for "
         "\"choose one that wasn't chosen during your last combat\"; the attack collector filters the source "
         "permanent's remembered `chosenMode` and remembers the newly selected label for the next combat.",
         "- `ChooseModeNotChosenDuringLastCombatEffect` — " + GONE_17Z11D + ": write "
         "`ChooseOneAtTriggerTimeEffect(ChooseOneEffect.notChosenDuringLastCombat(options))` (Yotian Courier)."),
        ("(4th arg `onlyDeclaredAttackers` damages only the creatures an `Triggers.playerAttacks()` trigger recorded "
         "when it triggered, Lightmine Field) — damage each matching permanent across `ALL_PLAYERS`/`TARGET_PLAYER`",
         "(4th arg `onlyDeclaredAttackers` damages only the creatures an `Triggers.playerAttacks()` trigger recorded "
         "when it triggered, Lightmine Field; `.dealtByTriggeringPermanent()` has the permanent the trigger event was "
         "about deal it, the predicate judged with it as the source: \"sacrifice it. If you do, it deals 4 damage to "
         "each creature blocking it\", Ib Halfheart) — damage each matching permanent across "
         "`ALL_PLAYERS`/`TARGET_PLAYER`"),
        ("(the entry's attacking snapshots, its declared `GrantScope.TARGETS` recipients and its triggering "
         "permanents). \"They gain [keyword]\" = `GrantKeywordEffect(keyword, GrantScope.TARGETS)` (Tyvar the "
         "Bellicose),",
         "(the entry's attacking snapshots and its triggering permanents). \"They gain [keyword]\" = "
         "`GrantKeywordEffect(keyword, GrantScope.DECLARED_ATTACKERS)` (Tyvar the Bellicose; unfiltered, the declared "
         "attackers: Angelic Guardian; \"one or more creatures attack one of your opponents or a planeswalker they "
         "control\" is the filter `PermanentIsAttackingOpponentOrTheirPlaneswalkerPredicate`, Frontier Warmonger),"),
    ],
    'EFFECTS_INDEX.md': [
        ("| `BoostCreaturesInAttackingBandsEffect` | `()` | each creature in every attacking band of at least two "
         "creatures gets +N/+N until end of turn, where N is that band's size; resolves from `Triggers.youAttack()` "
         "and reads declared `Permanent.bandId` groups |",
         "| `BoostCreaturesInAttackingBandsEffect` | `()` | each creature in that band gets +N/+N until end of turn, "
         "where N is the number of the band's creatures still attacking in a band; on `Triggers.youAttack()` with "
         "`TriggerMultiplicity.EACH_BAND` (one ability for each declared band of two or more), the band the entry's "
         "attacking creatures |"),
        ("Non-targeting effects use the blocked creature as the non-targeting `sourcePermanentId` so `BoostSelfEffect` "
         "applies to \"it\"; permanent/player-targeting effects use the shared `AttackTriggerTarget` pipeline and the "
         "watching permanent as `sourcePermanentId`. Used by `UnstoppableAsh` and `CloseQuarters`.",
         "The watching permanent is the source; the entry records the blocked creature as its triggering permanent "
         "and non-targeting target, so \"it\" is `BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, ...)` "
         "and the like; permanent/player-targeting effects use the shared `AttackTriggerTarget` pipeline with the "
         "blocked creature as the choice's triggering permanent. Used by `UnstoppableAsh` and `CloseQuarters`. "
         "\"Creatures you control have 'whenever this creature becomes blocked by a creature, …'\" (Retaliation) is "
         "`GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, "
         "…), GrantScope.ALL_OWN_CREATURES)`."),
        ("use `Triggers.creatureBecomesBlocked()` — fires once per attacker/blocker pair on every permanent with the "
         "slot across all battlefields. Its effects must implement `BlockPairConditionalEffect`, whose "
         "`firesForPair(attackerPower, blockerPower)` is evaluated as a trigger *condition* when blockers are declared "
         "(never re-checked on resolution) and whose `actsOn()` participant becomes the stack entry's non-targeting "
         "`targetId`. Used by `NoQuarter`.",
         "use `Triggers.creatureBecomesBlocked()` on every battlefield: without `EACH_BLOCKER` once per blocked "
         "attacker (below), with `TriggerOptions.EACH_BLOCKER` (\"becomes blocked by a creature\", \"a creature blocks "
         "a creature\") once per declared attacker/blocker pair, the attacker the entry's triggering permanent and the "
         "blocker its non-targeting target. A comparison of the pair is the event condition, judged as blockers are "
         "declared (`BlockPairLesserPower(BlockParticipant)`, No Quarter: \"destroy the blocking creature\" = "
         "`DestroyCombatOpponentEffect(false)`, \"destroy the attacking creature\" = "
         "`DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING)`)."),
        ("the combat collector judges the filter on each blocked attacker and queues the wrapped effect with the "
         "blocked attacker as both source and non-targeting target. Spined Sliver uses this with "
         "`CardSubtypePredicate(SLIVER)` and `BoostSelfEffect(CreaturesBlockingSource(), CreaturesBlockingSource())`.",
         "the combat collector judges the filter (and the ability's event condition) on each blocked attacker and "
         "queues the ability with the watcher as its source and the blocked attacker as its triggering permanent and "
         "non-targeting target. Spined Sliver uses this with `PermanentHasSubtypePredicate(SLIVER)` and "
         "`BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, new CreaturesBlockingSource(true), new "
         "CreaturesBlockingSource(true))`."),
        ("| `ChooseModeNotChosenDuringLastCombatEffect` | `(List<ChooseOneOption> options)` | Triggers.attacks() marker "
         "for \"choose one that wasn't chosen during your last combat.\" The attack collector removes the source "
         "permanent's remembered `chosenMode`, then the standard triggered-modal choice remembers the new label for "
         "the next combat. |",
         "| `ChooseModeNotChosenDuringLastCombatEffect` | — | " + GONE_17Z11D + ": write "
         "`ChooseOneAtTriggerTimeEffect(ChooseOneEffect.notChosenDuringLastCombat(options))` (Yotian Courier). |"),
        ("| `DestroyWeakerBlockParticipantEffect` | `(BlockParticipant participant)` |",
         "| `DestroyWeakerBlockParticipantEffect` | — | " + GONE_17Z11C2 + ": write `Triggers.creatureBecomesBlocked()` "
         "with `TriggerOptions.EACH_BLOCKER.withEventCondition(new BlockPairLesserPower(participant))` and "
         "`DestroyCombatOpponentEffect(false)` (the blocker) or `DestroyReferencedPermanentEffect(PermanentReference"
         ".TRIGGERING)` (the attacker), No Quarter. Was:"),
        ("| `BoostBlockerWhenAttackerMatchesEffect` | `(PermanentPredicate attackerPredicate, int powerBoost, int "
         "toughnessBoost)` |",
         "| `BoostBlockerWhenAttackerMatchesEffect` | — | " + GONE_17Z11C2 + ": write "
         "`Triggers.creatureBecomesBlocked().withFilter(attackerPredicate)` with `TriggerOptions.EACH_BLOCKER` and "
         "`BoostTargetCreatureEffect(p, t)`, Righteous Indignation. Was:"),
    ],
    'ACTIVATED_ABILITY_GUIDE.md': [
        ("its entry's attacking snapshots, declared `GrantScope.TARGETS` recipients and triggering permanents are the "
         "matching attackers |",
         "its entry's attacking snapshots (\"they\", `GrantScope.DECLARED_ATTACKERS`) and triggering permanents are "
         "the matching attackers. `TriggerOptions.DEFAULT.withMultiplicity(TriggerMultiplicity.EACH_BAND)` is "
         "\"whenever two or more creatures you control attack in a band\": an ability of its own for each declared "
         "band of two or more, whose attacking creatures are the band's |"),
        ("Non-targeting effects use the blocked creature as `sourcePermanentId`, so self-scoped effects like "
         "`BoostSelfEffect` apply to \"it\". Effects with permanent/player targets use the shared `AttackTriggerTarget` "
         "pipeline and the watching permanent as `sourcePermanentId`.",
         "The watching permanent is the source; the entry records the blocked creature as its triggering permanent and "
         "non-targeting target, so \"it\" is a triggering-permanent reference "
         "(`BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, ...)`). Effects with permanent/player "
         "targets use the shared `AttackTriggerTarget` pipeline."),
        ("Fires once per attacker/blocker pair on every permanent with this slot across all battlefields. Effects "
         "must implement `BlockPairConditionalEffect`; `CombatBlockService` evaluates `firesForPair(attackerPower, "
         "blockerPower)` at trigger time and bakes the `actsOn()` participant in as the non-targeting `targetId` "
         "(attacker as `sourcePermanentId`). Used by No Quarter |",
         "Once per blocked attacker (its filter on the attacker, Spined Sliver), or with `TriggerOptions.EACH_BLOCKER` "
         "once per declared attacker/blocker pair, on every permanent with this slot across all battlefields. The "
         "watcher is the source, the attacker the entry's triggering permanent and, for a pair, the blocker its "
         "non-targeting target; a pair's comparison is the event condition (`BlockPairLesserPower`). Used by No "
         "Quarter and Righteous Indignation |"),
    ],
    'CARD_PATTERNS_CREATURES_TRIGGERED.md': [
        ("| `Triggers.youAttack()` + `BoostCreaturesInAttackingBandsEffect()` - groups the controller's declared "
         "attacking bands and gives every creature in each band of at least two creatures +N/+N, where N is that "
         "band's size |",
         "| `Triggers.youAttack()` with `TriggerMultiplicity.EACH_BAND` + `BoostCreaturesInAttackingBandsEffect()` - "
         "one ability for each declared band of at least two creatures, whose attacking creatures are the band's; "
         "each gets +N/+N, where N is the band's size |"),
        ("Triggers.creatureYouControlBecomesBlocked() `BoostSelfEffect(0, 5)` — \"whenever a creature you control "
         "becomes blocked, it gets +0/+5 until end of turn\". Fires once per blocked attacker; \"it\" = the blocked "
         "creature via non-targeting sourcePermanentId.",
         "Triggers.creatureYouControlBecomesBlocked() `BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, "
         "0, 5)` — \"whenever a creature you control becomes blocked, it gets +0/+5 until end of turn\". Fires once "
         "per blocked attacker; \"it\" = the entry's triggering permanent."),
    ],
    'CARD_PATTERNS_PERMANENTS_STATIC.md': [
        ("| Two Triggers.creatureBecomesBlocked() registrations of `DestroyWeakerBlockParticipantEffect` "
         "(`BlockParticipant.BLOCKER` and `BlockParticipant.ATTACKER`) — \"whenever a creature becomes blocked by a "
         "creature with lesser power, destroy the blocking creature\" plus the mirrored attacker clause.",
         "| Two Triggers.creatureBecomesBlocked() abilities with `TriggerOptions.EACH_BLOCKER.withEventCondition(new "
         "BlockPairLesserPower(BLOCKER / ATTACKER))`, `DestroyCombatOpponentEffect(false)` (the blocker) and "
         "`DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING)` (the attacker) — \"whenever a creature "
         "becomes blocked by a creature with lesser power, destroy the blocking creature\" plus the mirrored attacker "
         "clause."),
        ("| Triggers.creatureBecomesBlocked() `BoostBlockerWhenAttackerMatchesEffect(PermanentColorInPredicate(BLACK, "
         "RED), 1, 1)` — evaluates the attacking creature's colors at declaration and boosts the blocking creature "
         "once per matching attacker/blocker pair |",
         "| Triggers.creatureBecomesBlocked().withFilter(PermanentColorInPredicate(BLACK, RED)) with "
         "`TriggerOptions.EACH_BLOCKER` and `BoostTargetCreatureEffect(1, 1)` (no declared target: the blocker) — "
         "judges the attacking creature's colors at declaration and boosts the blocking creature once per matching "
         "attacker/blocker pair |"),
    ],
    'CARD_PATTERN_INDEX.md': [
        ("| `c/ChatzukMightyGuitarist.java` + `ReduceCastCostForMatchingSpellsEffect` + "
         "`BoostCreaturesInAttackingBandsEffect` |",
         "| `c/ChatzukMightyGuitarist.java` + `ReduceCastCostForMatchingSpellsEffect` + `TriggerMultiplicity.EACH_BAND` "
         "+ `BoostCreaturesInAttackingBandsEffect` |"),
        ("| `y/YotianCourier.java` + `ChooseModeNotChosenDuringLastCombatEffect` + `Permanent.chosenMode` |",
         "| `y/YotianCourier.java` + `ChooseOneAtTriggerTimeEffect(ChooseOneEffect.notChosenDuringLastCombat(...))` + "
         "`Permanent.chosenMode` |"),
    ],
    'PREDICATES_REFERENCE.md': [
        ("| `PermanentBlockingSourcePredicate` | `()` | creatures blocking the source permanent",
         "| `PermanentBlockingSourcePredicate` | `()` / `.ofTriggeringPermanent()` | `.ofTriggeringPermanent()`: "
         "creatures blocking the permanent the trigger event was about, the filter context's triggering permanent or "
         "its last-known information (\"a creature that's blocking it\", Labyrinth Raptor). `()`: creatures blocking "
         "the source permanent"),
    ],
    'ARCHITECTURE.md': [
        ("No effect tells the collectors to do this (the `CombatOpponentReferencingEffect` interface is gone).",
         "No effect tells the collectors to do this (the `CombatOpponentReferencingEffect` interface is gone). A "
         "\"whenever a [filter] creature (you control) becomes blocked\" watcher (`CombatBlockService`'s ally and "
         "any-creature readers) is the ability's source (CR 113.7) and records the blocked creature as its triggering "
         "permanent (id, controller, a last-known copy) and non-targeting target whatever the effects are, so \"it\" is "
         "a triggering-permanent reference, never a self effect with a rebound source; \"becomes blocked by a "
         "creature\" there (`EACH_BLOCKER`, No Quarter, Righteous Indignation) is once per declared pair, the blocker "
         "its target and the pair's comparison its event condition. A \"whenever you attack\" entry records its "
         "attacking creatures whatever the effects are (the band's for `TriggerMultiplicity.EACH_BAND`), and \"they "
         "gain …\" is `GrantScope.DECLARED_ATTACKERS`; an Equipment's own \"whenever equipped creature attacks, choose "
         "one\" (Pip-Boy 3000) is its controller's, an ability granted to the creature its controller's (CR 113.8)."),
    ],
    'TRIGGER_SLOT_TARGETING.md': [
        ("the watching permanent is the source for damage and other source-dependent effects. Non-targeting effects "
         "retain the blocked creature as `sourcePermanentId`. |",
         "the watching permanent is the source for every entry, and the blocked creature the entry's (and the "
         "choice's) triggering permanent. |"),
    ],
}

REMOVED = ['BlockPairConditionalEffect', 'DestroyWeakerBlockParticipantEffect', 'BoostBlockerWhenAttackerMatchesEffect',
           'ChooseModeNotChosenDuringLastCombatEffect']

failed = False
for name, edits in EDITS.items():
    path = docs / name
    text = path.read_text()
    original = text
    for old, new in edits:
        if new in text:
            continue
        if old not in text:
            print(f'{name}: expected text not found: {old[:120]!r}')
            failed = True
            continue
        text = text.replace(old, new, 1)
    if text != original:
        path.write_text(text)
        print(f'updated {name}')

for path in list(docs.glob('*.md')) + list((root / '.claude/skills').rglob('*.md')):
    if path.name == 'EFFECT_MERGE_PLAN.md':
        continue
    for number, line in enumerate(path.read_text().splitlines(), 1):
        for removed in REMOVED:
            if re.search(rf'\b{removed}\b', line) and '**Gone**' not in line:
                print(f'{path.relative_to(root)}:{number} still names {removed}')
                failed = True
if failed:
    sys.exit(1)
