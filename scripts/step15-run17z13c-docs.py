#!/usr/bin/env python3
"""Step 15, runs 17z13c and 17z13d: the agent-docs name the cards' own effects and conditions.

Whole-line edits in `EFFECTS_INDEX.md`, `EFFECTS_QUICK_REFERENCE.md`, `ORACLE_TEXT_EFFECT_MAP.md`,
`TRIGGER_SLOT_TARGETING.md`, `EFFECT_MERGE_PLAN.md` and the `CARD_PATTERNS_*` files: the removed markers
(`ReturnTappedLandToHandEffect`, `RelicBindTapEffect`, `HullbreakerHorrorTriggerEffect`,
`DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect`) and the suspend effect's `unlessCastFromExile` factory
get "Gone" lines naming the forms that replace them. Fails if a doc or skill still names a removed form outside a
"Gone" line. Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"
SKILLS = ROOT / ".claude" / "skills"
GONE_NOTE = "**Gone** (effect-slot-refactor.md step 15, run 17z13c)"
GONE_NOTE_D = "**Gone** (effect-slot-refactor.md step 15, run 17z13d)"

HULL_FORM = ("`new ChooseOneEffect(List.of(new ChooseOneOption(\"Return target spell you don't control to its owner's "
             "hand\", new ReturnTargetSpellToHandEffect(), new StackEntryPredicateTargetFilter(new StackEntryNotPredicate("
             "new StackEntryControlledByPredicate()), …)), new ChooseOneOption(\"Return target nonland permanent to its "
             "owner's hand\", ReturnToHandEffect.target(), new PermanentPredicateTargetFilter(new PermanentNotPredicate("
             "new PermanentIsLandPredicate()), …))), true, 0, 1)`")
RELIC_FORM = ("`new ChooseOneEffect(List.of(new ChooseOneOption(\"This Aura deals 1 damage to target player or "
              "planeswalker\", new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)), new ChooseOneOption(\"Target player "
              "gains 1 life\", new TargetPlayerGainsLifeEffect(1))))`")
OOZE_FORM = "`new DelayedEndOfCombatEffect(new DestroyAttachmentsOnTargetCreatureEffect(false, true))`"
RORY_FORM = ("`TriggerOptions.DEFAULT.withEventCondition(new NotCondition(new CastFromZone(Zone.EXILE)))` + "
             "`new ExileTriggeringSpellWithSuspendCountersEffect(3, CreateTokenEffect.ofClueToken(1))`")

EDITS = {
    "EFFECTS_INDEX.md": [
        ("| `RelicBindTapEffect` | `()` |",
         f"| `RelicBindTapEffect` | — | {GONE_NOTE}: Relic Bind writes {RELIC_FORM} on "
         "`Triggers.enchantedPermanentBecomesTapped()`; the enchanted-tap watch's `ChooseOneEffect` route queues the "
         "card's modal as a `TriggeredModalTrigger` (mode and target chosen as the ability goes on the stack, CR 603.3c), "
         "the tapped permanent as its triggering permanent. |"),
        ("| `HullbreakerHorrorTriggerEffect` | `()` |",
         f"| `HullbreakerHorrorTriggerEffect` | — | {GONE_NOTE}: Hullbreaker Horror writes {HULL_FORM} on "
         "`Triggers.youCastSpell().anySpell()`; the cast collector queues the optional modal's trigger-time choice, a "
         "mode with no legal target isn't offered and \"Choose no modes\" removes the ability (CR 603.3c). |"),
        ("| `DestroyAttachmentsOnTargetCreatureEffect` |",
         "| `DestroyAttachmentsOnTargetCreatureEffect` | `(boolean auras, boolean equipment[, TargetPredicate "
         "declaredTarget])` | destroy the Auras and/or Equipment attached to the target creature, all at once, by default "
         "(`(true, true)` = both, Strip Bare; `(false, true)` = Equipment only, Turn to Slag). The optional category "
         "supports other permanent targets such as `TargetPredicates.land()` for Street Sweeper. Declares its target "
         "category and honours protection. The card declares no target for it, so it acts on \"that creature\", the "
         "creature on the other side of the block, which the collector records as the entry's non-targeting `targetId`. "
         "Once that permanent has left the battlefield (destroyed by an earlier effect of the same spell, Eaten by "
         "Spiders; gone before a delayed ability resolves, Corrosive Ooze) the Auras and Equipment attached to it just "
         "before it left are destroyed (CR 608.2h), from the entry's record "
         "(`StackEntry.lastKnownPermanentAttachments`, recorded by the departure). \"Destroy all Equipment attached to "
         f"that creature at end of combat\" is {OOZE_FORM} (Corrosive Ooze). Lethal damage destruction is deferred "
         "until all effects on the stack entry resolve. Resolved by `DestroyAttachmentsOnTargetCreatureEffectHandler` |"),
        ("| `DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect` |",
         f"| `DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect` | — | {GONE_NOTE}: Corrosive Ooze writes "
         f"{OOZE_FORM} on `Triggers.blocks().withFilter(new PermanentIsEquippedPredicate())` and "
         "`Triggers.becomesBlocked().withFilter(…)` (`TriggerOptions.EACH_BLOCKER`). |"),
        ("| `ExileTriggeringSpellWithSuspendCountersEffect` | `(int counters)` / `(int counters, CardEffect followUp)` / "
         "`unlessCastFromExile(",
         "| `ExileTriggeringSpellWithSuspendCountersEffect` | `(int counters)` / `(int counters, CardEffect followUp)` | "
         "spell-cast trigger effect that removes the triggering spell from the stack, exiles it under its owner's "
         "identity, and records suspend counters; the follow-up still resolves when the spell has left the stack. \"When "
         f"you cast this spell from anywhere other than exile\" is the ability's event condition: {RORY_FORM} (Rory "
         f"Williams; `unlessCastFromExile(...)` is {GONE_NOTE_D}) |"),
        ("| `DelayedEndOfCombatEffect` | `(CardEffect effect)` |",
         "| `DelayedEndOfCombatEffect` | `(CardEffect effect)` | schedules the wrapped non-targeting ability to trigger at "
         "the beginning of the end-of-combat step. The delayed trigger preserves the source card and controller even if "
         "the source permanent leaves the battlefield. The scheduling ability's non-targeting permanent (\"that "
         "creature\", the creature on the other side of a block) is the delayed ability's non-targeting target, with what "
         "was attached to it just before it left the battlefield if it has (CR 608.2h). Use Triggers.attacks() / "
         "Triggers.blocks() for delayed instructions such as Fickle Efreet's end-of-combat coin flip, or Corrosive Ooze's "
         f"{OOZE_FORM} |"),
        ("| `ReturnTappedLandToHandEffect` | `()` |",
         f"| `ReturnTappedLandToHandEffect` | — | {GONE_NOTE}: Storm Cauldron writes `ReturnToHandEffect.triggering()` "
         "on `Triggers.playerTapsLandForMana()`; the land-tap default records the tapped land as the triggering "
         "permanent (a land that left as it was tapped is returned by nothing, the ability still triggers). |"),
        ("`DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect` carries a scheduling flag",
         "A departing permanent's Auras and Equipment are recorded on every stack entry that targets it or names it as its "
         "non-targeting \"that creature\" (`StackEntry.lastKnownPermanentAttachments`) and on every delayed end-of-combat "
         "ability that affects it (`DelayedEndOfCombatTrigger.affectedPermanentAttachments`, copied to the delayed "
         "entry), whatever their effects are (effect-slot-refactor.md step 15, run 17z13c)."),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `ExileTriggeringSpellWithSuspendCountersEffect(int counters)` — removes the triggering spell from the stack, "
         "exiles it under its owner's identity, and tracks its time counters; `(int, CardEffect)` adds a follow-up ",
         "- `ExileTriggeringSpellWithSuspendCountersEffect(int counters)` — removes the triggering spell from the stack, "
         "exiles it under its owner's identity, and tracks its time counters; `(int, CardEffect)` adds a follow-up that "
         "resolves even if the spell has left the stack. \"From anywhere other than exile\" is the ability's event "
         "condition, `TriggerOptions.DEFAULT.withEventCondition(new NotCondition(new CastFromZone(Zone.EXILE)))` (Rory "
         "Williams; the `unlessCastFromExile` factory is gone, run 17z13d)"),
        ("- `ReturnTappedLandToHandEffect()` —",
         f"- `ReturnTappedLandToHandEffect()` — {GONE_NOTE}: write `ReturnToHandEffect.triggering()` on "
         "Triggers.playerTapsLandForMana() (Storm Cauldron)"),
        ("- `DelayedEndOfCombatEffect(CardEffect effect)` —",
         "- `DelayedEndOfCombatEffect(CardEffect effect)` — schedule the wrapped non-targeting ability to trigger at the "
         "beginning of the end-of-combat step. The delayed trigger carries the source card and controller, so it still "
         "triggers if the source permanent has left the battlefield, and the scheduling ability's \"that creature\" (the "
         "creature on the other side of a block) as its non-targeting target. Put on Triggers.attacks() / "
         "Triggers.blocks() for wording such as \"flip a coin at end of combat\" (Fickle Efreet) or \"destroy all "
         f"Equipment attached to that creature at end of combat\" ({OOZE_FORM}, Corrosive Ooze)"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"Partner with Amy Pond\" + \"The Last Centurion —",
         "| \"Partner with Amy Pond\" + \"The Last Centurion — When you cast this spell from anywhere other than exile, "
         "exile it with three time counters on it. It gains suspend. Then investigate.\" | `Triggers.enters("
         "ObjectRelation.THIS) MayEffect(SearchTargetLibraryEffect(1, CardNamedPredicate(\\\"Amy Pond\\\"), HAND, true, "
         f"TARGET_PLAYER))` + `Triggers.youCastThisSpell()` {RORY_FORM} | `Triggers.enters(ObjectRelation.THIS)` + "
         "`Triggers.youCastThisSpell()` | Rory Williams (WHO 153); \"from anywhere other than exile\" is a trigger "
         "condition (CR 603.2), so cast from exile it doesn't trigger; the Clue follow-up resolves even if the spell has "
         "left the stack |"),
        ("| \"whenever a land is tapped for mana, return it to its owner's hand\" |",
         "| \"whenever a land is tapped for mana, return it to its owner's hand\" | `ReturnToHandEffect.triggering()` | "
         "Triggers.playerTapsLandForMana() | Storm Cauldron. Symmetric — bounces the tapped land (the entry's triggering "
         "permanent); the mana it produced stays in the pool; a land sacrificed as it was tapped still triggers it, and "
         "nothing returns |"),
        ("| \"Whenever enchanted artifact becomes tapped, choose one —",
         "| \"Whenever enchanted artifact becomes tapped, choose one — 1 damage to target player/planeswalker; or target "
         f"player gains 1 life\" | `Triggers.enchantedPermanentBecomesTapped()` {RELIC_FORM} | Trigger | Relic Bind. The "
         "card's modal goes to the trigger-time mode choice as written (`TriggeredModalTrigger`, CR 603.3c), then the "
         "chosen mode's target |"),
        ("| \"Whenever you cast a spell, choose up to one — return target spell you don't control to hand;",
         "| \"Whenever you cast a spell, choose up to one — return target spell you don't control to hand; or return target "
         f"nonland permanent to hand\" | `.anySpell()` on the cast trigger with {HULL_FORM} | `Triggers.youCastSpell()` | "
         "Hullbreaker Horror. The optional modal's mode is chosen as the ability goes on the stack (CR 603.3c): a mode "
         "with no legal target isn't offered, a spell mode's target is chosen among the spells its "
         "`StackEntryPredicateTargetFilter` admits, \"Choose no modes\" removes the ability |"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| Spell cast modal choose-up-to-one bounce | `h/HullbreakerHorror.java` |",
         "| Spell cast modal choose-up-to-one bounce | `h/HullbreakerHorror.java` | STATIC CantBeCounteredEffect + "
         "Triggers.youCastSpell().anySpell() with an optional `ChooseOneEffect(options, true, 0, 1)`: a spell mode "
         "(`ReturnTargetSpellToHandEffect` + `StackEntryPredicateTargetFilter(StackEntryNotPredicate("
         "StackEntryControlledByPredicate))`) and a nonland permanent mode — modes chosen as the ability goes on the "
         "stack |"),
    ],
    "CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("| Enchant opponent's artifact: modal tap trigger | `r/RelicBind.java` |",
         "| Enchant opponent's artifact: modal tap trigger | `r/RelicBind.java` | target(PermanentAllOf("
         "PermanentIsArtifactPredicate, PermanentNot(PermanentControlledBySourceControllerPredicate))) + "
         f"Triggers.enchantedPermanentBecomesTapped() {RELIC_FORM}. The card's modal goes to the trigger-time mode "
         "choice (`TriggeredModalTrigger`, CR 603.3c) through the enchanted-tap watch's `ChooseOneEffect` route |"),
    ],
    "CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("| Symmetric extra land play + land-tap bounce | `s/StormCauldron.java` |",
         "| Symmetric extra land play + land-tap bounce | `s/StormCauldron.java` | STATIC EachPlayerPlaysAdditionalLandEffect "
         "+ Triggers.playerTapsLandForMana() ReturnToHandEffect.triggering() — every player gets one extra land play per "
         "turn; any land tapped for mana returns to its owner's hand (mana kept) |"),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("| `Triggers.becomesTargetOfSpell()` / `becomesTargetOfSpellOrAbility()` / "
         "`becomesTargetOfOpponentSpellOrAbility()` / `becomesTargetOfOpponentSpell()` |",
         "| `Triggers.becomesTargetOfSpell()` / `becomesTargetOfSpellOrAbility()` / "
         "`becomesTargetOfOpponentSpellOrAbility()` / `becomesTargetOfOpponentSpell()` | "
         "`TriggerCollectionService.checkBecomesTargetOfSpell*` | Spell-target. \"A spell an opponent controls\" "
         "(Reality Smasher's discard counter) is `becomesTargetOfOpponentSpell()`, which no ability triggers; ward's "
         "\"spell or ability\" is `becomesTargetOfOpponentSpellOrAbility()` |"),
    ],
    "EFFECT_MERGE_PLAN.md": [
        ("- `ChoiceHandlerService:696` (Relic Bind's damage mode) and",
         "- Relic Bind's damage mode (its own `ChooseOneEffect` mode since effect-slot-refactor.md step 15, run 17z13c) and"),
        ("  **construct** the survivor at runtime; the `(int)` / `(DynamicAmount)` sugar keeps them `ANY`,",
         "  **construct** the survivor; the `(int)` / `(DynamicAmount)` sugar keeps them `ANY`,"),
    ],
}

MODAL_OLD = ("Optional, multi-mode, multi-target and\n"
             "graveyard/exile/spell-target modals are not announced there and still pick their mode on resolution via\n"
             "`ChooseOneEffectHandler` (`ChoiceContext.ChooseModeChoice`); use `ChooseOneAtTriggerTimeEffect` for those.\n")
MODAL_NEW = ("Optional, multi-mode, multi-target and\n"
             "graveyard/exile/spell-target modals are not announced there and still pick their mode on resolution via\n"
             "`ChooseOneEffectHandler` (`ChoiceContext.ChooseModeChoice`); use `ChooseOneAtTriggerTimeEffect` for those.\n"
             "A trigger-time modal (`TriggeredModalTrigger`: a cast trigger's or an enchanted-permanent tap trigger's bare\n"
             "`ChooseOneEffect`, any `ChooseOneAtTriggerTimeEffect`) offers a mode that targets a spell only while a spell its\n"
             "`StackEntryPredicateTargetFilter` admits is on the stack, and chooses that spell as the ability goes on the stack\n"
             "(`ETBSpellTargetTrigger`, CR 603.3c, 603.3d): Hullbreaker Horror's \"return target spell you don't control\"\n"
             "(effect-slot-refactor.md step 15, run 17z13c).\n")

REMOVED = ["ReturnTappedLandToHandEffect", "RelicBindTapEffect", "HullbreakerHorrorTriggerEffect",
           "DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect", "unlessCastFromExile", "skipIfCastFromExile",
           "RelicBindModeChoice", "HullbreakerHorrorModeChoice"]


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        lines = path.read_text().split("\n")
        changed = False
        for prefix, replacement in edits:
            hits = [i for i, line in enumerate(lines) if line.startswith(prefix)]
            if not hits:
                if any(line == replacement for line in lines):
                    continue
                sys.exit(f"{name}: no line starts with {prefix!r}")
            if len(hits) > 1:
                sys.exit(f"{name}: several lines start with {prefix!r}")
            if lines[hits[0]] != replacement:
                lines[hits[0]] = replacement
                changed = True
        text = "\n".join(lines)
        if name == "EFFECTS_INDEX.md" and MODAL_NEW not in text:
            if text.count(MODAL_OLD) != 1:
                sys.exit("EFFECTS_INDEX.md: the modal-trigger paragraph moved")
            text = text.replace(MODAL_OLD, MODAL_NEW)
            changed = True
        if changed:
            path.write_text(text)
            print("updated", name)
    bad = []
    for path in sorted([*DOCS.glob("*.md"), *SKILLS.rglob("*.md")]):
        for n, line in enumerate(path.read_text().split("\n"), 1):
            if "**Gone**" in line or "is gone" in line or "are gone" in line:
                continue
            for removed in REMOVED:
                if removed in line:
                    bad.append(f"{path.relative_to(ROOT)}:{n}: {removed}")
    if bad:
        sys.exit("still named:\n" + "\n".join(bad))


if __name__ == "__main__":
    main()
