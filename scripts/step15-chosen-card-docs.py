#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 13a: exact agent-docs edits for the chosen card, the accepted players and the
produced mana colors read from the stack entry.

Idempotent: each edit is skipped when its new text is already there. Run from the repository root:

    python3 scripts/step15-chosen-card-docs.py
"""
import pathlib

EDITS = {
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md": [
        ("PerpetuallyGrantCardCharacteristicsEffect(ARTIFACT, FOOD, ActivatedAbility({2}, SacrificeSelfCost + "
         "GainLifeEffect(3))))` | Triggers.enters(ObjectRelation.THIS) | Hinterland Chef (YMID 50). The drafted card's "
         "identity carries the type, subtype, and activated ability through later zone changes. |",
         "PerpetuallyGrantChosenCardCharacteristicsEffect(Set.of(ARTIFACT), Set.of(FOOD), "
         "List.of(ActivatedAbility({2}, SacrificeSelfCost + GainLifeEffect(3)))))` | "
         "Triggers.enters(ObjectRelation.THIS) | Hinterland Chef (YMID 50). The draft sets the drafted card as the "
         "stack entry's chosen card; its identity carries the type, subtype, and activated ability through later "
         "zone changes. |"),
    ],
    "agent-docs/EFFECTS_INDEX.md": [
        ("`PerpetuallyGrantCardCharacteristicsEffect` records card types, subtypes, and activated abilities on a card "
         "identity and reapplies them when that card returns to the battlefield.",
         "`PerpetuallyGrantCardCharacteristicsEffect` records card types, subtypes, and activated abilities on the "
         "source's own card identity and reapplies them when that card returns to the battlefield; for the card a "
         "choice selected, use `PerpetuallyGrantChosenCardCharacteristicsEffect`."),
        ("Records perpetual card-type, subtype, and activated-ability grants on the card selected by a library search. "
         "| Woodland Investigation (YMKM 21) |",
         "Records perpetual card-type, subtype, and activated-ability grants on the card a preceding library search or "
         "spellbook draft selected (the stack entry's chosen card). | Woodland Investigation (YMKM 21), Hinterland "
         "Chef (YMID 50) |"),
        ("| `PutChosenCardFromHandOntoBattlefieldEffect` | `(UUID cardId)` | Puts the card selected by a preceding hand "
         "choice onto the battlefield. |",
         "| `PutChosenCardFromHandOntoBattlefieldEffect` | `()` / `(boolean enterTapped)` | Puts the card a preceding "
         "hand or library choice selected (the stack entry's chosen card) onto the battlefield, if it is still in "
         "hand. |"),
        ("| `EachPlayerMayDiscardHandThenDrawEffect` | `(int/DynamicAmount cardsToDraw[, AcceptedPlayersAwareEffect "
         "followUp])` | Each player chooses independently in APNAP order whether to discard their hand and draw the "
         "evaluated number; all choices complete before accepted players discard and draw, then an optional "
         "follow-up is bound to the accepted players. |",
         "| `EachPlayerMayDiscardHandThenDrawEffect` | `(int/DynamicAmount cardsToDraw[, CardEffect followUp])` | Each "
         "player chooses independently in APNAP order whether to discard their hand and draw the evaluated number; "
         "all choices complete before accepted players discard and draw, then an optional follow-up resolves with "
         "the accepted players as the stack entry's accepted players. |"),
        ("| `DealDamageToPlayersEffect.selectedOpponents` | `(DynamicAmount/int)` | Follow-up damage to the opponents "
         "who accepted an `EachPlayerMayDiscardHandThenDrawEffect` choice. |",
         "| `DealDamageToPlayersEffect.selectedOpponents` | `(DynamicAmount/int)` | Follow-up damage to the opponents "
         "who accepted an `EachPlayerMayDiscardHandThenDrawEffect` choice (the stack entry's accepted players). |"),
        ("| `HoldDraftedSpellbookCardEffect` | `(Card)` |",
         "| `HoldDraftedSpellbookCardEffect` | `()` |"),
    ],
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        ("- `PerpetuallyGrantCardCharacteristicsEffect(CardType, CardSubtype, ActivatedAbility)` — records "
         "type/subtype and an activated ability on a card identity, applying the grants immediately and when the "
         "card returns to the battlefield; used by Hinterland Chef (YMID 50).",
         "- `PerpetuallyGrantCardCharacteristicsEffect(Set<CardType>, Set<CardSubtype>, List<ActivatedAbility>)` — "
         "records types, subtypes and activated abilities on the source's own card identity, applying the grants "
         "immediately and when the card returns to the battlefield; for the card a choice selected use "
         "`PerpetuallyGrantChosenCardCharacteristicsEffect`."),
        ("- `PerpetuallyGrantChosenCardCharacteristicsEffect(Set<CardType>, Set<CardSubtype>, List<ActivatedAbility>)` "
         "— records perpetual type, subtype, and activated-ability grants on the card selected by a library search; "
         "used by Woodland Investigation (YMKM 21).",
         "- `PerpetuallyGrantChosenCardCharacteristicsEffect(Set<CardType>, Set<CardSubtype>, List<ActivatedAbility>)` "
         "— records perpetual type, subtype, and activated-ability grants on the card a preceding library search or "
         "spellbook draft selected (the stack entry's chosen card); used by Woodland Investigation (YMKM 21) and, as "
         "a draft's follow-up, Hinterland Chef (YMID 50)."),
        ("- `EachPlayerMayDiscardHandThenDrawEffect(int/DynamicAmount cardsToDraw[, AcceptedPlayersAwareEffect "
         "followUp])`",
         "- `EachPlayerMayDiscardHandThenDrawEffect(int/DynamicAmount cardsToDraw[, CardEffect followUp])`"),
        ("then an optional follow-up is bound to the accepted players",
         "then an optional follow-up resolves with the accepted players as the stack entry's accepted players"),
        ("choice; use with its optional `AcceptedPlayersAwareEffect` follow-up",
         "choice (the stack entry's accepted players); use as its optional follow-up"),
    ],
    "agent-docs/CARD_PATTERN_INDEX.md": [
        ("| `h/HinterlandChef.java` + `PerpetuallyGrantCardCharacteristicsEffect` |",
         "| `h/HinterlandChef.java` + `PerpetuallyGrantChosenCardCharacteristicsEffect` |"),
    ],
}


def main():
    for name, edits in EDITS.items():
        path = pathlib.Path(name)
        text = path.read_text()
        for old, new in edits:
            if new and new in text:
                continue
            if old not in text:
                if not new:
                    continue
                raise SystemExit(f"{name}: neither the old nor the new form of {old[:80]!r}")
            if text.count(old) != 1:
                raise SystemExit(f"{name}: {old[:80]!r} is not unique")
            text = text.replace(old, new)
        path.write_text(text)


if __name__ == "__main__":
    main()
