#!/usr/bin/env python3
"""Step 15, run 17z12d: the "this card" effects read the source card's graveyard stay
(`StackEntry.sourceCardGraveyardEntryVersion()`), not the triggering card's.

With --probe, each converted site also logs (Step15Probe) where the old reading differs from the new one.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
FX = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/effect/normalfx"
PROBE = "--probe" in sys.argv
P = "com.github.laxika.magicalvibes.service.Step15Probe"


def probe(indent, name, old_expr):
    if not PROBE:
        return ""
    return f"{indent}{P}.cmp(\"{name}\", entry, {old_expr}); // PROBE17z12d\n"


def edit(file, old, new):
    path = FX / file
    src = path.read_text()
    if src.count(old) != 1:
        sys.exit(f"{file}: expected one match of\n{old}")
    path.write_text(src.replace(old, new))


TRIG = "entry.getTriggeringCardGraveyardEntryVersion()"

# GraveyardReturnSupport: "exile it" of a return effect, and the self return.
edit("GraveyardReturnSupport.java",
     """        // A card that left the graveyard and came back is a new object the trigger cannot exile.
        if (entry != null && entry.getTriggeringCardGraveyardEntryVersion() >= 0
                && gameData.graveyardEntryVersion(sourceCardId) != entry.getTriggeringCardGraveyardEntryVersion()) {
""",
     (probe("        ", "GRS.exileSource", f"entry == null ? -1 : {TRIG}").replace("        com", "        if (entry != null) com")) +
     """        // A card that left the graveyard and came back is a new object the trigger cannot exile.
        if (entry != null && entry.sourceCardGraveyardEntryVersion() >= 0
                && gameData.graveyardEntryVersion(sourceCardId) != entry.sourceCardGraveyardEntryVersion()) {
""")
edit("GraveyardReturnSupport.java",
     """        if (effect.filter() instanceof com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate
                && entry != null && entry.getTriggeringCardGraveyardEntryVersion() >= 0
                && entry.getCard() != null && entry.getCard().getId().equals(entry.getTriggeringCardId())
                && gameData.graveyardEntryVersion(entry.getCard().getId())
                != entry.getTriggeringCardGraveyardEntryVersion()) {
""",
     (probe("        ", "GRS.self",
            f"entry.getCard().getId().equals(entry.getTriggeringCardId()) ? {TRIG} : -1")
      .replace("        com", "        if (entry != null && entry.getCard() != null && effect.filter() instanceof "
               "com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate) com")) +
     """        if (effect.filter() instanceof com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate
                && entry != null && entry.getCard() != null && entry.sourceCardGraveyardEntryVersion() >= 0
                && gameData.graveyardEntryVersion(entry.getCard().getId())
                != entry.sourceCardGraveyardEntryVersion()) {
""")
edit("GraveyardReturnSupport.java",
     """    private static void keepOwnAbilityBoundToMovedSource(GameData gameData, StackEntry entry, Card movedCard) {
        if (entry != null && entry.getTriggeringCardGraveyardEntryVersion() >= 0
                && entry.getCard() != null && entry.getCard().getId().equals(movedCard.getId())) {
            entry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(movedCard.getId()));
        }
    }
""",
     """    private static void keepOwnAbilityBoundToMovedSource(GameData gameData, StackEntry entry, Card movedCard) {
        if (entry == null || entry.getCard() == null || !entry.getCard().getId().equals(movedCard.getId())
                || entry.sourceCardGraveyardEntryVersion() < 0) {
            return;
        }
        long movedVersion = gameData.graveyardEntryVersion(movedCard.getId());
        if (movedCard.getId().equals(entry.getTriggeringCardId())
                && entry.getTriggeringCardGraveyardEntryVersion() >= 0) {
            entry.setTriggeringCardGraveyardEntryVersion(movedVersion);
        }
        entry.setSourceGraveyardEntryVersion(movedVersion);
    }
""")

# Plain readers: `entry.getTriggeringCardGraveyardEntryVersion()` of the source card becomes the source's stay.
plain = {
    "ExileSourceCardFromGraveyardAndCollectEvidenceEffectHandler.java": "        // CR 400.7: a card that left",
    "RegisterDelayedSelfReturnFromGraveyardEffectHandler.java": "        long graveyardEntryVersion =",
    "ShuffleSelfFromGraveyardIntoLibraryEffectHandler.java": "        boolean sameGraveyardObject =",
    "PutSourceCardFromGraveyardIntoLibraryNFromTopEffectHandler.java": "        long expectedEntryVersion =",
    "PutAuraFromHandOntoSelfWithinXManaValueOrExileEffectHandler.java": "        if (entry.getTriggeringCardGraveyardEntryVersion() >= 0",
    "PersistReturnEffectHandler.java": "        if (entry.getTriggeringCardGraveyardEntryVersion() >= 0",
    "ExileSourceCardFromGraveyardEffectHandler.java": "        long expectedEntryVersion =",
    "ExileSourceCardFromGraveyardAndShuffleTopCardsEffectHandler.java": "        if (entry.getTriggeringCardGraveyardEntryVersion() >= 0",
    "AttachOneOfControlledEquipmentToSourceEffectHandler.java": "        if (entry.getTriggeringCardGraveyardEntryVersion() >= 0) {",
    "RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffectHandler.java": "        gameData.queueDelayedAction(",
    "ReturnSourceCardFromGraveyardToBattlefieldEffectHandler.java": "        if (entry.getTriggeringCardGraveyardEntryVersion() >= 0",
    "ReturnCardFromGraveyardEffectHandler.java": "        if (e.filter() instanceof CardIsSelfPredicate",
}
for file, anchor in plain.items():
    path = FX / file
    src = path.read_text()
    if src.count(anchor) != 1:
        sys.exit(f"{file}: anchor not unique: {anchor}")
    name = file.replace("EffectHandler.java", "")
    src = src.replace(anchor, probe("        ", name, TRIG) + anchor)
    head, tail = src.split(probe("        ", name, TRIG) + anchor if PROBE else anchor, 1)
    tail = tail.replace(TRIG, "entry.sourceCardGraveyardEntryVersion()")
    src = head + (probe("        ", name, TRIG) if PROBE else "") + anchor.replace(
        TRIG, "entry.sourceCardGraveyardEntryVersion()") + tail
    if "TriggeringCardGraveyardEntryVersion()" in tail:
        sys.exit(f"{file}: reader left")
    path.write_text(src)

# Readers that read the triggering version only when it "belongs to the source".
edit("ForcedCostOrElseEffectHandler.java",
     """        UUID sourceCardId = entry.getCard().getId();
        boolean versionBelongsToSource = entry.getTriggeringCardId() == null
                || entry.getTriggeringCardId().equals(sourceCardId);
        if (versionBelongsToSource
                && entry.getTriggeringCardGraveyardEntryVersion() >= 0
                && gameQueryService.findCardInGraveyardById(gameData, sourceCardId) != null
                && gameData.graveyardEntryVersion(sourceCardId) != entry.getTriggeringCardGraveyardEntryVersion()) {
""",
     """        UUID sourceCardId = entry.getCard().getId();
""" + probe("        ", "ForcedCostOrElse",
            f"entry.getTriggeringCardId() == null || entry.getTriggeringCardId().equals(sourceCardId) ? {TRIG} : -1") +
     """        if (entry.sourceCardGraveyardEntryVersion() >= 0
                && gameQueryService.findCardInGraveyardById(gameData, sourceCardId) != null
                && gameData.graveyardEntryVersion(sourceCardId) != entry.sourceCardGraveyardEntryVersion()) {
""")
edit("ReturnSourceCardFromGraveyardToOwnerHandEffectHandler.java",
     """        // The entry's graveyard version belongs to the source only when no other card is the
        // trigger's subject; a discard follow-up records the discarded card's version instead.
        boolean versionBelongsToSource = entry.getTriggeringCardId() == null
                || entry.getTriggeringCardId().equals(cardId);
        if (sourceCard == null || (versionBelongsToSource
                && entry.getTriggeringCardGraveyardEntryVersion() >= 0
                && gameData.graveyardEntryVersion(cardId) != entry.getTriggeringCardGraveyardEntryVersion())) {
""",
     probe("        ", "ReturnSourceCardFromGraveyardToOwnerHand",
           f"entry.getTriggeringCardId() == null || entry.getTriggeringCardId().equals(cardId) ? {TRIG} : -1") +
     """        // CR 400.7: a card that left the graveyard and came back is a new object, not "this card".
        if (sourceCard == null || (entry.sourceCardGraveyardEntryVersion() >= 0
                && gameData.graveyardEntryVersion(cardId) != entry.sourceCardGraveyardEntryVersion())) {
""")
edit("ReturnSourceTransformedFromGraveyardEffectHandler.java",
     """        if (ownerId == null || card.getId().equals(entry.getTriggeringCardId())
                && entry.getTriggeringCardGraveyardEntryVersion() >= 0
                && gameData.graveyardEntryVersion(card.getId()) != entry.getTriggeringCardGraveyardEntryVersion()) {
""",
     probe("        ", "ReturnSourceTransformed",
           f"card.getId().equals(entry.getTriggeringCardId()) ? {TRIG} : -1") +
     """        if (ownerId == null || entry.sourceCardGraveyardEntryVersion() >= 0
                && gameData.graveyardEntryVersion(card.getId()) != entry.sourceCardGraveyardEntryVersion()) {
""")
print("converted the source readers" + (" with the probe" if PROBE else ""))
