#!/usr/bin/env python3
"""Step 15, run 17v2: agent-docs lines naming the changed, deleted or new forms (the Aura and creature returns that
read the stack entry, Mask of Griselbrand, Faces of the Past, Hateful Eidolon, Kaya's Ghostform). Each rewrite replaces
the whole line that starts with its key. Idempotent; fails if a doc or skill still names a deleted class.

Usage: step15-run17v2-docs.py (run from the repository root)
"""
import pathlib
import sys

DOCS = pathlib.Path("agent-docs")

REWRITES = {
    "EFFECTS_QUICK_REFERENCE.md": {
        "- `TapOrUntapCreaturesSharingDyingCreatureTypeEffect()`":
            "- Faces of the Past's \"tap all untapped creatures that share a creature type with it or untap all tapped "
            "creatures that share a creature type with it\": `ChooseOneAtResolutionEffect` of "
            "`TapPermanentsEffect(TapUntapScope.ALL_CREATURES, f)` / `UntapPermanentsEffect(TapUntapScope.ALL_CREATURES, f)` "
            "with `f = PermanentSharesCreatureTypeWithTriggeringPermanentPredicate()` on `Triggers.creatureDies(ANY)`; the "
            "dying creature is the stack entry's triggering permanent, its last-known creature types (Changeling-aware).",
        "- `MayPayLifeAndDrawEqualToDyingPowerEffect()`":
            "- Mask of Griselbrand's \"you may pay X life, where X is its power. If you do, draw X cards\": "
            "`MayPayLifeEqualToAmountEffect(new TriggeringPermanentPower(), new DrawCardEffect(new TriggeringPermanentPower()), "
            "prompt)` on `Triggers.creatureDies(EQUIPPED)`; the equipped-death entry records the creature's last-known power.",
        "- `ReturnEnchantedPermanentToBattlefieldOnDeathOrExileEffect(":
            "- `ReturnEnchantedPermanentToBattlefieldOnDeathOrExileEffect()` — Kaya's Ghostform on "
            "`Triggers.putIntoGraveyardOrExileFromBattlefield(null, ObjectRelation.ENCHANTED)`: returns the stack entry's "
            "triggering card under the Aura controller's control from the zone it went to; the enchanted-leaves collector "
            "records its graveyard or exile incarnation (CR 400.7).",
        "- `DrawCardForEachAuraAttachedToDyingCreatureEffect()`":
            "- Hateful Eidolon's \"whenever an enchanted creature dies, draw a card for each Aura you controlled that was "
            "attached to it\": `DrawCardEffect(new AurasAttachedToTriggeringPermanent(true))` on "
            "`Triggers.creatureDies(new PermanentIsEnchantedPredicate(), ANY)`; the entry's triggering attachments record "
            "each Aura's controller as the creature died, same-batch Auras included.",
        "- `ReturnEnchantedCreatureToOwnerHandOnDeathEffect()`":
            "- `ReturnEnchantedCreatureToOwnerHandOnDeathEffect()` — aura `Triggers.creatureDies(ObjectRelation.ENCHANTED)` "
            "trigger: return that card (the stack entry's triggering card, in the graveyard stay the collector recorded) to "
            "its owner's hand (Demonic Vigor, the Zendikons)",
    },
    "EFFECTS_INDEX.md": {
        "| `TapOrUntapCreaturesSharingDyingCreatureTypeEffect`":
            "| `PermanentSharesCreatureTypeWithTriggeringPermanentPredicate` filter | `()` | Faces of the Past: "
            "`ChooseOneAtResolutionEffect` of `TapPermanentsEffect` / `UntapPermanentsEffect` (`TapUntapScope.ALL_CREATURES`) "
            "with this filter on `Triggers.creatureDies(ANY)`, the mode chosen as it resolves (not a bulleted modal, so no "
            "CR 603.3c announcement); the dying creature is the entry's triggering permanent (Changeling-aware) |",
        "- `MayPayLifeAndDrawEqualToDyingPowerEffect`":
            "- Mask of Griselbrand: `MayPayLifeEqualToAmountEffect(new TriggeringPermanentPower(), "
            "new DrawCardEffect(new TriggeringPermanentPower()), prompt)` on `Triggers.creatureDies(EQUIPPED)`; the "
            "equipped-death entry records the creature's last-known power (clamped to zero by the amount).",
        "| `ReturnEnchantedPermanentToBattlefieldOnDeathOrExileEffect`":
            "| `ReturnEnchantedPermanentToBattlefieldOnDeathOrExileEffect` | `()` | Kaya's Ghostform on "
            "`Triggers.putIntoGraveyardOrExileFromBattlefield(null, ObjectRelation.ENCHANTED)`: returns the stack entry's "
            "triggering card under the Aura controller's control from the graveyard or exile incarnation the enchanted-leaves "
            "collector recorded (CR 400.7). |",
        "| `ReturnSourceAuraToOpponentCreatureOnDeathEffect`":
            "| `ReturnSourceAuraToOpponentCreatureOnDeathEffect` | `()` | when enchanted creature dies, return this aura from "
            "owner's graveyard to battlefield attached to target creature an opponent of the dying creature's controller "
            "controls; that controller (the entry's triggering permanent's, `targetChosenByTriggeringPermanentController()`) "
            "chooses the target. Use with `Triggers.dies(ObjectRelation.ENCHANTED)` (e.g. Necrotic Plague) |",
        "| `ReturnEnchantedCreatureToOwnerHandOnDeathEffect` | `()` / `(UUID":
            "| `ReturnEnchantedCreatureToOwnerHandOnDeathEffect` | `()` | when enchanted creature dies, return that card (the "
            "stack entry's triggering card) from graveyard to its owner's hand. Use with "
            "`Triggers.creatureDies(ObjectRelation.ENCHANTED)` (e.g. Demonic Vigor) |",
        "| `ReturnEnchantedCreatureToBattlefieldOnDeathEffect` | `()` / `(boolean underAuraControllersControl)` / `(boolean underAuraControllersControl, boolean enterTapped)` / `(UUID":
            "| `ReturnEnchantedCreatureToBattlefieldOnDeathEffect` | `()` / `(boolean underAuraControllersControl)` / "
            "`(boolean underAuraControllersControl, boolean enterTapped)` | when enchanted creature dies, return that card (the "
            "stack entry's triggering cards, each in the graveyard stay the collector recorded) to the **battlefield** "
            "(contrast the hand variant above), under its owner's control by default, or under the Aura controller's control "
            "when `underAuraControllersControl` is true, optionally tapped. Use with `Triggers.dies(ObjectRelation.ENCHANTED)` "
            "(e.g. Abduction, Unhallowed Pact, False Demise, Fungal Fortitude) |",
        "| `ReturnEnchantedCreatureAndReattachAuraOnDeathEffect`":
            "| `ReturnEnchantedCreatureAndReattachAuraOnDeathEffect` | `()` | Gift of Immortality death trigger: returns that "
            "card (the stack entry's triggering card) from its owner's graveyard under its owner's control, then queues the "
            "source Aura to return at the next end step under its owner's control attached to the same returned permanent; "
            "the delayed action requires both the returned permanent and the Aura card to remain in place. |",
        "| `ReturnSourceAuraToSharedTypeCreatureOnDeathEffect`":
            "| `ReturnSourceAuraToSharedTypeCreatureOnDeathEffect` | `()` | return this aura from owner's graveyard to "
            "battlefield attached to a creature that shares a creature type with the creature that died "
            "(Changeling-aware; the creature types of the stack entry's triggering card, the card as it was put into the "
            "graveyard). Write the card's \"you may\" around it: `MayEffect(new ReturnSourceAuraToSharedTypeCreatureOnDeathEffect(), "
            "prompt)` on `Triggers.dies(ObjectRelation.ENCHANTED)` (e.g. Reins of the Vinesteed) |",
        "| `DrawCardForEachAuraAttachedToDyingCreatureEffect`":
            "| `AurasAttachedToTriggeringPermanent` amount | `()` / `(boolean controlledByYou)` | the Auras attached to the "
            "entry's triggering permanent as it left (its triggering attachments, CR 603.10a), or only those the ability's "
            "controller controlled then: Hateful Eidolon's `DrawCardEffect(new AurasAttachedToTriggeringPermanent(true))` on "
            "`Triggers.creatureDies(new PermanentIsEnchantedPredicate(), ANY)` |",
        "| `ReturnEnchantedCreatureToOwnerHandOnDeathEffect` | `(UUID":
            "| `ReturnEnchantedCreatureToOwnerHandOnDeathEffect` | `()` or `(String followUpManaCost, String followUpPrompt)` | "
            "`Triggers.creatureDies(ObjectRelation.ENCHANTED)`: returns that card (the stack entry's triggering card, in the "
            "graveyard stay the enchanted-death collector recorded, CR 400.7) to its owner's hand. The optional follow-up "
            "payment is offered only after the creature was actually returned and, if paid, returns the source Aura from its "
            "owner's graveyard to its hand (the entry's triggering card id is then the Aura's). Used by Demonic Vigor and "
            "Puppet Master. |",
        "| `ReturnEnchantedCreatureToBattlefieldOnDeathEffect` meld handling":
            "| `ReturnEnchantedCreatureToBattlefieldOnDeathEffect` meld handling | the entry's triggering cards | returns "
            "every physical card represented by the enchanted permanent; a melded permanent therefore returns both "
            "components separately |",
    },
    "ORACLE_TEXT_EFFECT_MAP.md": {
        "| \"Whenever a creature dies, tap all untapped creatures that share a creature type with it":
            "| \"Whenever a creature dies, tap all untapped creatures that share a creature type with it or untap all tapped "
            "creatures that share a creature type with it.\" | `ChooseOneAtResolutionEffect` of "
            "`TapPermanentsEffect(ALL_CREATURES, f)` / `UntapPermanentsEffect(ALL_CREATURES, f)`, "
            "`f = PermanentSharesCreatureTypeWithTriggeringPermanentPredicate()` | `Triggers.creatureDies(ANY)` | Faces of the "
            "Past (`SCG` 35). \"It\" is the entry's triggering permanent as it last existed (Changeling-aware); the tap or "
            "untap mode is chosen as the trigger resolves (official ruling, 2004-10-04; the \"or\" is not a bulleted modal, "
            "so CR 603.3c does not apply). |",
        "- \"Whenever equipped creature dies, you may pay X life, where X is its power":
            "- \"Whenever equipped creature dies, you may pay X life, where X is its power. If you do, draw X cards\" -> "
            "`MayPayLifeEqualToAmountEffect(new TriggeringPermanentPower(), new DrawCardEffect(new TriggeringPermanentPower()), "
            "prompt)` on `Triggers.creatureDies(EQUIPPED)`; Mask of Griselbrand. The equipped-death entry records the "
            "creature's last-known power, which both amounts read (clamped to zero) as the ability resolves.",
        "| \"When enchanted permanent dies or is put into exile, return that card":
            "| \"When enchanted permanent dies or is put into exile, return that card to the battlefield under your control\" | "
            "`ReturnEnchantedPermanentToBattlefieldOnDeathOrExileEffect()` | "
            "`Triggers.putIntoGraveyardOrExileFromBattlefield(null, ObjectRelation.ENCHANTED)` | Kaya's Ghostform; the "
            "enchanted-leaves collector records the card as the entry's triggering card with its graveyard or exile "
            "incarnation, so a card that left that zone is not returned |",
        "| \"Whenever an enchanted creature dies, draw a card for each Aura you controlled":
            "| \"Whenever an enchanted creature dies, draw a card for each Aura you controlled that was attached to it\" | "
            "`DrawCardEffect(new AurasAttachedToTriggeringPermanent(true))` | "
            "`Triggers.creatureDies(new PermanentIsEnchantedPredicate(), ObjectRelation.ANY)` | Hateful Eidolon. The "
            "entry's triggering attachments record each Aura's controller as the creature died, Auras destroyed in the same "
            "batch included |",
        "| \"When enchanted creature dies, return that card to its owner's hand. If that card is returned":
            "| \"When enchanted creature dies, return that card to its owner's hand. If that card is returned to its owner's "
            "hand this way, you may pay {U}{U}{U}. If you do, return this card to its owner's hand.\" | "
            "`ReturnEnchantedCreatureToOwnerHandOnDeathEffect(\"{U}{U}{U}\", prompt)` | "
            "`Triggers.creatureDies(ObjectRelation.ENCHANTED)` | Puppet Master. The effect returns the entry's triggering "
            "card and offers the mana payment only when that move succeeded; paying returns the Aura source from its "
            "owner's graveyard. |",
        "| \"When enchanted creature dies, return that card to the battlefield under its owner's control\" |":
            "| \"When enchanted creature dies, return that card to the battlefield under its owner's control\" | "
            "`ReturnEnchantedCreatureToBattlefieldOnDeathEffect()` | Triggers.dies(ObjectRelation.ENCHANTED) | Abduction. "
            "Battlefield sibling of `ReturnEnchantedCreatureToOwnerHandOnDeathEffect` (Demonic Vigor, returns to hand). "
            "\"That card\" is the entry's triggering cards, recorded by the enchanted-death collector |",
        "| \"When enchanted creature dies, return that card to the battlefield\" |":
            "| \"When enchanted creature dies, return that card to the battlefield\" | "
            "`ReturnEnchantedCreatureToBattlefieldOnDeathEffect(...)` | enchanted-permanent death trigger | The entry's "
            "triggering cards are every component card of the dying permanent; when it was melded, both component cards "
            "return separately. |",
    },
    "PREDICATES_REFERENCE.md": {
        "| `PermanentControlledByTriggeringPermanentControllerPredicate`":
            "| `PermanentControlledByTriggeringPermanentControllerPredicate` | `()` | permanents controlled by the controller "
            "of the permanent `triggeringPermanentId` names; once it has left the battlefield, by its last-known controller "
            "`triggeringPermanentControllerId` (nothing when none was given). Negated: Confusion in the Ranks' \"target "
            "permanent another player controls\", chosen by the entering permanent's controller; Necrotic Plague's "
            "\"creature one of their opponents controls\" | `gameData` + `triggeringPermanentId` / "
            "`triggeringPermanentControllerId` |\n"
            "| `PermanentSharesCreatureTypeWithTriggeringPermanentPredicate` | `()` | permanents that share a creature type "
            "with the triggering permanent: the one `triggeringPermanentId` names while it is on the battlefield, otherwise "
            "the snapshot as it last existed (Faces of the Past's \"creatures that share a creature type with it\"); "
            "Changeling-aware | `FilterContext.triggeringPermanentId` / `triggeringPermanentSnapshot` |",
    },
    "CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md": {
        "| Aura death-return transform DFC (Aura to land)":
            "| Aura death-return transform DFC (Aura to land) | `j/JourneyToEternity.java` / `a/AtzalCaveOfEternity.java` | "
            "Front: `Triggers.dies(ObjectRelation.ENCHANTED)` `SequenceEffect.of(new ReturnEnchantedCreatureToBattlefieldOnDeathEffect(true), "
            "new ReturnSourceTransformedFromGraveyardEffect())` — returns the enchanted creature under the Aura controller's "
            "control, then returns the Aura transformed. Back: `{T}` `AwardAnyColorManaEffect()` + `{3}{B}{G},{T}` "
            "targeted creature-card `ReturnCardFromGraveyardEffect` |",
    },
}

DELETED = ["ReturnEnchantedCreatureAndSourceTransformedOnDeathEffect", "MayPayLifeAndDrawEqualToDyingPowerEffect",
           "TapOrUntapCreaturesSharingDyingCreatureTypeEffect", "DrawCardForEachAuraAttachedToDyingCreatureEffect"]


def main():
    for name, rewrites in REWRITES.items():
        path = DOCS / name
        lines = path.read_text().split("\n")
        changed = False
        for key, new in rewrites.items():
            first = new.split("\n")[0]
            hits = [i for i, line in enumerate(lines) if line.startswith(key)]
            for i in hits:
                if lines[i] != first:
                    lines[i] = new
                    changed = True
        if changed:
            path.write_text("\n".join(lines))
            print(f"updated {path}")
    for path in list(DOCS.glob("*.md")) + list(pathlib.Path(".claude/skills").rglob("*.md")):
        text = path.read_text()
        for cls in DELETED:
            if cls in text:
                raise SystemExit(f"{path} still names {cls}")


if __name__ == "__main__":
    main()
