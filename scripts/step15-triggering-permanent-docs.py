#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 15: docs for the effects that read the triggering permanent from the stack entry.

Idempotent: python3 scripts/step15-triggering-permanent-docs.py

Exact edits in agent-docs/ (EFFECTS_INDEX.md, EFFECTS_QUICK_REFERENCE.md, ORACLE_TEXT_EFFECT_MAP.md,
TRIGGER_SLOT_TARGETING.md): UntapTriggeringPermanentEffect is gone (UntapPermanentsEffect(TapUntapScope.TRIGGERING)),
DealDamageToTriggeringAttackerEffect lost its attacker condition (the trigger's filter), the land and artifact
returns lost their bound card ids, Mutable Pupa's effect lost its bound keywords, Mirror of Life Trapping's "if it was
cast" is a filter, and the triggering-permanent readers name the entry instead of a bound or baked id. Fails if a
removed constructor form is still named.
"""
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("with `ConjureDuplicateOfTriggeringCreatureToHandEffect()` | Triggers.youCastSpell() | Ace Flockbringer (YBLB 20). The handler copies the triggering creature spell into hand and adds flying to that runtime card permanently. |",
         "with `ConjureDuplicateOfTriggeringCreatureToHandEffect()` | Triggers.youCastSpell() | Ace Flockbringer (YBLB 20). The handler copies the triggering creature spell (the stack entry's triggering spell, as it last existed on the stack once it has left) into hand and adds flying to that runtime card permanently. |"),
        ("| Haunted One (SLD 2200). The static grant uses all battlefield creatures plus ownership, while the resolution effect limits the boost to the ability controller's creatures. |",
         "| Haunted One (SLD 2200). The static grant uses all battlefield creatures plus ownership, while the resolution effect limits the boost to the ability controller's creatures. \"It\" is the stack entry's triggering permanent, its last-known snapshot once it has left. |"),
        ("| Mutable Pupa (YEOE 20). The trigger snapshots the entering creature's supported keywords for last-known information, then rechecks them at resolution while that creature remains on the battlefield. |",
         "| Mutable Pupa (YEOE 20). The keywords are read as the ability resolves from the stack entry's triggering permanent, or from its keywords as it left the battlefield (last-known information). |"),
        ("| \"Whenever a creature with flying attacks you or a planeswalker you control, this creature deals damage equal to its power to that creature\" | `DealDamageToTriggeringAttackerEffect(new SourcePower(), new PermanentHasKeywordPredicate(FLYING))` | `Triggers.creatureAttacksYouOrYourPlaneswalker()` | The attacker predicate is checked at declaration; the source power is evaluated when the trigger resolves. Used by Palazzo Archers (ACR 42) |",
         "| \"Whenever a creature with flying attacks you or a planeswalker you control, this creature deals damage equal to its power to that creature\" | `DealDamageToTriggeringAttackerEffect(new SourcePower())` | `Triggers.creatureAttacksYouOrYourPlaneswalker().withFilter(new PermanentHasKeywordPredicate(FLYING))` | The trigger's filter is judged on the attacker at declaration; the source power is evaluated when the trigger resolves. Used by Palazzo Archers (ACR 42) |"),
        ("+ `ReturnTriggeringLandFromGraveyardToBattlefieldEffect(null)`. The engine reads",
         "+ `ReturnTriggeringLandFromGraveyardToBattlefieldEffect()` (\"that card\" is the stack entry's triggering card). The engine reads"),
        ("| \"Whenever a creature with flying attacks you, this deals N damage to it\" | `DealDamageToTriggeringAttackerEffect(N, new PermanentHasKeywordPredicate(FLYING))` | `Triggers.creatureAttacksYouOrYourPlaneswalker()` | The predicate restricts which attackers fire the trigger (checked at attack declaration in `CombatAttackService`); damage is dealt to the attacking creature (non-targeting `targetId`) on resolution. Used by Raking Canopy (4 damage) |",
         "| \"Whenever a creature with flying attacks you, this deals N damage to it\" | `DealDamageToTriggeringAttackerEffect(N)` | `Triggers.creatureAttacksYou().withFilter(new PermanentHasKeywordPredicate(FLYING))` | The trigger's filter restricts which attackers fire it (judged at attack declaration in `CombatAttackService`); damage is dealt to the attacking creature (the stack entry's triggering permanent, not targeted) on resolution. Used by Raking Canopy (4 damage) |"),
        ("+ `ReturnTriggeringLandFromGraveyardToBattlefieldEffect(null, true)` on `Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN)` | trigger | Hedge Shredder (DSK 183). The library trigger preserves the cards from the one library-to-graveyard event and returns only its land cards, tapped |",
         "+ `ReturnTriggeringLandFromGraveyardToBattlefieldEffect(true)` on `Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN)` with `TriggerOptions.ONE_OR_MORE` | trigger | Hedge Shredder (DSK 183). The stack entry's event cards are the land cards of the one library-to-graveyard event; they return tapped |"),
        ("| `ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect()` in `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER)` | TRIGGER | Mirror of Life Trapping. The entering creature is bound by permanent identity, only cast entries trigger, and the newly exiled creature is excluded from the immediate return |",
         "| `ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect()` in `Triggers.enters(new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentWasCastPredicate())), ObjectRelation.ANOTHER)` | TRIGGER | Mirror of Life Trapping. \"If it was cast\" can't change, so it is the trigger's filter; the entering creature is the stack entry's triggering permanent, and the newly exiled creature is excluded from the immediate return |"),
        ("| Fractured Loyalty. The trigger carries the targeting spell or activated ability as an internal stack reference and re-derives the enchanted creature from the Aura at resolution |",
         "| Fractured Loyalty. \"That creature\" is the stack entry's triggering permanent (the enchanted creature that became the target); \"that spell or ability's controller\" is read from the spell or ability on the stack, or the controller recorded as the ability triggered |"),
        ("| Pia's Revolution. The artifact card id is bound from the graveyard event; token artifacts are filtered out.",
         "| Pia's Revolution. \"That card\" is the stack entry's triggering card; token artifacts are filtered out."),
        ("`ChooseOneAtTriggerTimeEffect(ChooseOneEffect(UntapTriggeringPermanentEffect(), TapPermanentsEffect(TapUntapScope.TARGET)",
         "`ChooseOneAtTriggerTimeEffect(ChooseOneEffect(UntapPermanentsEffect(TapUntapScope.TRIGGERING), TapPermanentsEffect(TapUntapScope.TARGET)"),
        ("| `trigger.withFilter(PermanentHasKeywordPredicate(REACH)), SequenceEffect.of(UntapTriggeringPermanentEffect(), MakeCreatureBlockableOnlyByFilterEffect(PermanentPowerAtMostSourcePowerPredicate(), ..., UNTIL_END_OF_COMBAT))` |",
         "| `trigger.withFilter(PermanentAllOfPredicate(creature, PermanentHasKeywordPredicate(REACH))), SequenceEffect.of(UntapPermanentsEffect(TapUntapScope.TARGET), GrantStaticEffectToTargetUntilEndOfCombatEffect(CantBeBlockedByCreaturesMatchingPredicateEffect(PermanentNotPredicate(PermanentPowerAtMostSourcePowerPredicate()))))` |"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `PerpetuallyGainKeywordsOfTriggeringCreatureEffect()` — on an ally-creature-enter trigger, records the supported keywords of the entering creature and perpetually grants them to the source card (Mutable Pupa, YEOE 20).",
         "- `PerpetuallyGainKeywordsOfTriggeringCreatureEffect()` — on an ally-creature-enter trigger, perpetually grants the source card the supported keywords the stack entry's triggering permanent has as the ability resolves, or had as it left the battlefield (Mutable Pupa, YEOE 20)."),
        ("- Palazzo Archers uses `DealDamageToTriggeringAttackerEffect(new SourcePower(), new PermanentHasKeywordPredicate(FLYING))`;",
         "- Palazzo Archers uses `Triggers.creatureAttacksYouOrYourPlaneswalker().withFilter(new PermanentHasKeywordPredicate(FLYING))` with `DealDamageToTriggeringAttackerEffect(new SourcePower())`;"),
        ("- `ConjureDuplicateOfTriggeringCreatureToHandEffect()` - creates a non-token copy of the creature spell that caused the trigger in the resolving controller's hand;",
         "- `ConjureDuplicateOfTriggeringCreatureToHandEffect()` - creates a non-token copy of the creature spell that caused the trigger (the stack entry's triggering spell, as it last existed on the stack once it has left) in the resolving controller's hand;"),
        ("- `DealDamageToTriggeringAttackerEffect(DynamicAmount damage, PermanentPredicate attackerCondition)`; convenience `(int, PermanentPredicate)` — `Triggers.creatureAttacksYouOrYourPlaneswalker()` or `Triggers.creatureAttacksYou()`: evaluated damage to the attacking creature; `attackerCondition` restricts which attackers trigger it (Raking Canopy: flyers, 4; Barbed Foliage: direct nonflyers, 1; Storm, Queen of Wakanda: flyers, `SourcePower()`)",
         "- `DealDamageToTriggeringAttackerEffect(DynamicAmount damage)`; convenience `(int)` — `Triggers.creatureAttacksYouOrYourPlaneswalker()` or `Triggers.creatureAttacksYou()`: evaluated damage to the attacking creature (the stack entry's triggering permanent); the trigger's `withFilter(...)` restricts which attackers trigger it (Raking Canopy: flyers, 4; Barbed Foliage: direct nonflyers, 1; Storm, Queen of Wakanda: flyers, `SourcePower()`)"),
        ("; the triggering object is carried on the stack and the host is re-derived from the Aura (Fractured Loyalty)",
         "; the enchanted creature that became the target is the stack entry's triggering permanent (Fractured Loyalty)"),
        ("The artifact id is bound from the graveyard event and the target is chosen as the trigger is put on the stack (Pia's Revolution)",
         "The artifact card is the stack entry's triggering card and the target is chosen as the trigger is put on the stack (Pia's Revolution)"),
        ("- `ReturnTriggeringLandFromGraveyardToBattlefieldEffect(UUID landCardId[, boolean enterTapped])` — Sacred Ground's trigger effect: return the identified land from the graveyard to the battlefield under its owner's control, optionally tapped. Register the template with `null` on the `Triggers.dies(Triggers.LAND, ObjectRelation.YOU_OWN).causedBy(PlayerRelation.OPPONENT)` slot, or on `Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN)` for Hedge Shredder; the collector stamps the real card id.",
         "- `ReturnTriggeringLandFromGraveyardToBattlefieldEffect([boolean enterTapped])` — returns the land cards the trigger event was about (the stack entry's event cards, else its triggering card, each only if still the same graveyard object) to the battlefield under their owner's control, optionally tapped. On `Triggers.dies(Triggers.LAND, ObjectRelation.YOU_OWN).causedBy(PlayerRelation.OPPONENT)` (Sacred Ground) or `Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN)` (Hedge Shredder)."),
        ("- `ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect()` — non-targeting global creature-entry trigger: for a creature that entered by being cast, binds and exiles it with the source,",
         "- `ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect()` — non-targeting global creature-entry trigger (\"if it was cast\" is the trigger's `PermanentWasCastPredicate` filter): exiles the stack entry's triggering permanent with the source,"),
        ("- `UntapTriggeringPermanentEffect()` — non-targeting trigger effect: untaps the permanent whose event caused the resolving ability (Tadeas, Juniper Ascendant)\n",
         ""),
    ],
    "EFFECTS_INDEX.md": [
        ("| `PerpetuallyGainKeywordsOfTriggeringCreatureEffect` | `()` | on an ally-creature-enter trigger, records the supported keywords of the entering creature and perpetually grants them to the source card;",
         "| `PerpetuallyGainKeywordsOfTriggeringCreatureEffect` | `()` | on an ally-creature-enter trigger, perpetually grants the source card the supported keywords the stack entry's triggering permanent has as the ability resolves (or had as it left the battlefield);"),
        ("| `DealDamageToTriggeringAttackerEffect` dynamic amount | `(DynamicAmount damage, PermanentPredicate attackerCondition)` | extends the attack-trigger damage effect to evaluate source-relative amounts such as `SourcePower`; the attacker condition is still checked when attackers are declared |",
         "| `DealDamageToTriggeringAttackerEffect` dynamic amount | `(DynamicAmount damage)` | extends the attack-trigger damage effect to evaluate source-relative amounts such as `SourcePower`; the trigger's filter on the attacker is checked when attackers are declared |"),
        ("and preserves the source Aura id. `TriggeringObjectControllerGainsControlOfEnchantedPermanentEffect` uses both references for Fractured Loyalty.",
         "and preserves the source Aura id; the permanent that became the target is the entry's triggering permanent. `TriggeringObjectControllerGainsControlOfEnchantedPermanentEffect` uses both references for Fractured Loyalty."),
        ("| `DealDamageToTriggeringAttackerEffect` | `(DynamicAmount damage, PermanentPredicate attackerCondition)`; convenience `(int, PermanentPredicate)` | deal the evaluated damage to the attacking creature that caused this `Triggers.creatureAttacksYouOrYourPlaneswalker()` trigger (`StackEntry.targetId`, non-targeting). The `attackerCondition` restricts which attackers fire the trigger",
         "| `DealDamageToTriggeringAttackerEffect` | `(DynamicAmount damage)`; convenience `(int)` | deal the evaluated damage to the attacking creature that caused this `Triggers.creatureAttacksYouOrYourPlaneswalker()` / `Triggers.creatureAttacksYou()` trigger (the stack entry's triggering permanent, not targeted). The trigger's `withFilter(...)` restricts which attackers fire the trigger"),
        ("`CombatAttackService.declareAttackers` evaluates it against the attacker at declaration time, so the damage is dealt unconditionally on resolution.",
         "`CombatAttackService.declareAttackers` judges it on the attacker at declaration time, so the damage is dealt unconditionally on resolution."),
        ("| `ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect` | `()` | non-targeting global creature-entry trigger: when a cast creature enters, binds and exiles it with the source,",
         "| `ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect` | `()` | non-targeting global creature-entry trigger (\"if it was cast\" is the trigger's `PermanentWasCastPredicate` filter): exiles the stack entry's triggering permanent with the source,"),
        ("adds the triggering attacker (the stack entry's non-targeting `targetId`) to `mustBlockIds`",
         "adds the triggering attacker (the stack entry's triggering permanent) to `mustBlockIds`"),
        ("| `ReturnTriggeringLandFromGraveyardToBattlefieldEffect` | `(UUID landCardId[, boolean enterTapped])` | immediately returns the identified land from the graveyard to the battlefield under the ability controller's (= graveyard owner's) control; optionally tapped; fizzles if it is no longer in a graveyard. Register the template with `null`; the `Triggers.dies(Triggers.LAND, ObjectRelation.YOU_OWN).causedBy(PlayerRelation.OPPONENT)` or `Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN)` collector stamps the concrete land card id. Used by Sacred Ground and Hedge Shredder |",
         "| `ReturnTriggeringLandFromGraveyardToBattlefieldEffect` | `([boolean enterTapped])` | returns the land cards the trigger event was about — the stack entry's event cards, else its triggering card, each only if it is still the graveyard object the event named — to the battlefield under the ability controller's (= graveyard owner's) control; optionally tapped. On `Triggers.dies(Triggers.LAND, ObjectRelation.YOU_OWN).causedBy(PlayerRelation.OPPONENT)` or `Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN)`. Used by Sacred Ground and Hedge Shredder |"),
        ("The trigger carries the triggering object in the `STACK` zone and the Aura as `sourcePermanentId`; the host is re-derived from `attachedTo` at resolution (Fractured Loyalty) |",
         "The trigger carries the triggering object in the `STACK` zone, the Aura as `sourcePermanentId` and the enchanted creature that became the target as its triggering permanent (Fractured Loyalty) |"),
        ("| `UntapTriggeringPermanentEffect` | `()` | non-targeting trigger effect: untaps the permanent whose event caused the resolving ability (Tadeas, Juniper Ascendant) |\n",
         ""),
        ("`UntapPermanentsEffect(TapUntapScope.TRIGGERING)` untaps the permanent whose event caused the\ntrigger, and",
         "`UntapPermanentsEffect(TapUntapScope.TRIGGERING)` untaps the permanent whose event caused the\ntrigger (\"untap that land\", Tiller Engine; \"untap it\", Raggadragga, Goreguts Boss), and"),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("the resolving spell/ability — is an opponent of the graveyard owner; the collector stamps the dying\nland card id onto a fresh `ReturnTriggeringLandFromGraveyardToBattlefieldEffect`),",
         "the resolving spell/ability — is an opponent of the graveyard owner; the collector gives the stack\nentry the dying land as its triggering card, which `ReturnTriggeringLandFromGraveyardToBattlefieldEffect` reads),"),
    ],
}

GONE = (
    "UntapTriggeringPermanentEffect",
    "ReturnTriggeringLandFromGraveyardToBattlefieldEffect(null",
    "ReturnTriggeringLandFromGraveyardToBattlefieldEffect(UUID",
    "PermanentPredicate attackerCondition",
)


def apply(path, edits):
    text = path.read_text()
    original = text
    for old, new in edits:
        if new and new in text:
            continue
        if old not in text:
            if not new:
                continue
            raise SystemExit(f"{path.name}: neither the old nor the new form of {old[:90]!r}")
        text = text.replace(old, new)
    if text != original:
        path.write_text(text)
        print(f"edited {path.relative_to(ROOT)}")


def main():
    for name, edits in EDITS.items():
        apply(DOCS / name, edits)
    left = [f"{p.name}: {gone}" for p in DOCS.glob("*.md") for gone in GONE if gone in p.read_text()]
    if left:
        raise SystemExit("removed forms still named: " + ", ".join(left))


if __name__ == "__main__":
    main()
