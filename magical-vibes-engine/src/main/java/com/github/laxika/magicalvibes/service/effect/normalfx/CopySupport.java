package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DeferredSpellCopy;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.EpicEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Shared copy helpers used by every "normal" Copy effect handler.
 *
 * <p>Extracted verbatim from {@code CopyResolutionService}; behavior is identical.
 */
@Component
public class CopySupport {

    private final TriggerCollectionService triggerCollectionService;
    private final GameQueryService gameQueryService;

    public CopySupport() {
        this.triggerCollectionService = null;
        this.gameQueryService = null;
    }

    @Autowired
    public CopySupport(TriggerCollectionService triggerCollectionService, GameQueryService gameQueryService) {
        this.triggerCollectionService = triggerCollectionService;
        this.gameQueryService = gameQueryService;
    }

    public CopySupport(TriggerCollectionService triggerCollectionService) {
        this(triggerCollectionService, null);
    }

    public void addCopyToStack(GameData gameData, StackEntry copyEntry) {
        addCopyToStack(gameData, copyEntry, true);
    }

    /**
     * Adds a copy and applies global spell-copy replacements once to this copy event.
     *
     * <p>The replacement is disabled for the extra copies created by this method, preventing
     * recursive replacement. Counted copy effects use the overload with {@code false} and adjust
     * their event count before creating the copies.</p>
     */
    public void addCopyToStack(GameData gameData, StackEntry copyEntry, boolean applySpellCopyReplacement) {
        gameData.stack.add(copyEntry);
        if (triggerCollectionService != null) {
            triggerCollectionService.checkSpellCopyTriggers(gameData, copyEntry);
        }

        if (!applySpellCopyReplacement || !isSpellCopy(copyEntry) || gameQueryService == null) {
            return;
        }

        int additionalCopies = gameQueryService.countAdditionalSpellCopies(gameData);
        for (int i = 0; i < additionalCopies; i++) {
            Card extraCopyCard = createCopyCard(copyEntry.getCard());
            StackEntry extraCopy = createCopyStackEntry(
                    copyEntry, extraCopyCard, copyEntry.getControllerId(), copyEntry.getTargetId());
            addCopyToStack(gameData, extraCopy, false);
            if (hasTargets(extraCopy)) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        extraCopy.getCard(),
                        extraCopy.getControllerId(),
                        List.of(new CopySpellEffect()),
                        "Choose new targets for the additional copy of "
                                + extraCopy.getCard().getName() + "?",
                        extraCopyCard.getId()));
            }
        }
    }

    /**
     * Copies a spell just cast from a graveyard. When the resolving spell still has other cards to
     * cast with copies pending (Finale of Promise: "copy each of those spells"), the copies wait
     * until the last of them has been cast or declined, so they end up above every original.
     */
    public void copyGraveyardCastSpell(GameData gameData, StackEntry original, Card cardToCast,
                                       UUID controllerId, int requestedCopies) {
        if (requestedCopies > 0) {
            gameData.deferredGraveyardCastCopies.add(
                    new DeferredSpellCopy(original, cardToCast, controllerId, requestedCopies));
        }
        flushDeferredGraveyardCastCopies(gameData);
    }

    private static boolean castsFromGraveyardWithCopies(CardEffect effect) {
        if (effect instanceof ConditionalReplacementEffect replacement) {
            return castsFromGraveyardWithCopies(replacement.upgradedEffect())
                    || castsFromGraveyardWithCopies(replacement.baseEffect());
        }
        return effect instanceof CastTargetInstantOrSorceryFromGraveyardEffect cast && cast.copyCount() > 0;
    }

    /** Creates the held-back copies once no further graveyard cast with copies is pending. */
    public void flushDeferredGraveyardCastCopies(GameData gameData) {
        if (gameData.deferredGraveyardCastCopies.isEmpty()) {
            return;
        }
        boolean moreCastsPending = gameData.pendingMayAbilities.stream().anyMatch(pending ->
                pending.effects().stream().anyMatch(CopySupport::castsFromGraveyardWithCopies));
        StackEntry parked = gameData.pendingEffectResolutionEntry;
        if (!moreCastsPending && parked != null) {
            List<CardEffect> parkedEffects = parked.getEffectsToResolve();
            moreCastsPending = parkedEffects.subList(
                    Math.min(gameData.pendingEffectResolutionIndex, parkedEffects.size()), parkedEffects.size())
                    .stream().anyMatch(CopySupport::castsFromGraveyardWithCopies);
        }
        if (moreCastsPending) {
            return;
        }
        List<DeferredSpellCopy> deferred = new ArrayList<>(gameData.deferredGraveyardCastCopies);
        gameData.deferredGraveyardCastCopies.clear();
        for (DeferredSpellCopy pending : deferred) {
            int copyCount = adjustedSpellCopyCount(gameData, pending.requestedCopies());
            for (int i = 0; i < copyCount; i++) {
                Card copyCard = createCopyCard(pending.cardToCast());
                StackEntry copyEntry = createCopyStackEntry(
                        pending.original(), copyCard, pending.controllerId(), pending.original().getTargetId());
                addCopyToStack(gameData, copyEntry, false);
                if (copyEntry.getTargetId() != null) {
                    gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                            pending.cardToCast(), pending.controllerId(), List.of(new CopySpellEffect()),
                            "Choose new targets for the copy of " + pending.cardToCast().getName() + "?",
                            copyCard.getId()));
                }
            }
        }
    }

    /** Adjusts a counted spell-copy event for active global spell-copy replacements. */
    public int adjustedSpellCopyCount(GameData gameData, int requestedCopies) {
        if (requestedCopies <= 0 || gameQueryService == null) {
            return requestedCopies;
        }
        return requestedCopies + gameQueryService.countAdditionalSpellCopies(gameData);
    }

    private static boolean isSpellCopy(StackEntry entry) {
        return entry != null && entry.isCopy()
                && entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                && entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY;
    }

    private static boolean hasTargets(StackEntry entry) {
        return entry.getTargetId() != null
                || !entry.getTargetIds().isEmpty()
                || !entry.getTargetCardIds().isEmpty();
    }

    public StackEntry createCopyStackEntry(StackEntry source, Card copyCard, UUID controllerId, UUID targetId) {
        return createCopyStackEntry(source, copyCard, controllerId, targetId, source.getTargetZone(),
                source.getTargetCardIds() != null ? new ArrayList<>(source.getTargetCardIds()) : null);
    }

    public StackEntry createCopyStackEntry(StackEntry source, Card copyCard, UUID controllerId,
                                            UUID targetId, Zone targetZone) {
        List<UUID> targetCardIds = targetZone == Zone.GRAVEYARD && targetId != null
                ? List.of(targetId) : List.of();
        return createCopyStackEntry(source, copyCard, controllerId, targetId, targetZone, targetCardIds);
    }

    /**
     * Creates a copy whose divided-damage assignments and target ids are replaced by the given ones, for copies
     * that retarget every damage portion onto a single new permanent.
     */
    public StackEntry createCopyStackEntry(StackEntry source, Card copyCard, UUID controllerId, UUID targetId,
                                            Map<UUID, Integer> damageAssignments, List<UUID> targetIds) {
        return createCopyStackEntry(source, copyCard, controllerId, targetId, source.getTargetZone(),
                List.of(), damageAssignments, targetIds);
    }

    private StackEntry createCopyStackEntry(StackEntry source, Card copyCard, UUID controllerId,
                                             UUID targetId, Zone targetZone, List<UUID> targetCardIds) {
        return createCopyStackEntry(source, copyCard, controllerId, targetId, targetZone, targetCardIds,
                source.getDamageAssignments(),
                source.getTargetIds() != null ? new ArrayList<>(source.getTargetIds()) : null);
    }

    private StackEntry createCopyStackEntry(StackEntry source, Card copyCard, UUID controllerId,
                                             UUID targetId, Zone targetZone, List<UUID> targetCardIds,
                                             Map<UUID, Integer> damageAssignments, List<UUID> sourceTargetIds) {
        StackEntry copy = new StackEntry(
                source.getEntryType(),
                copyCard,
                controllerId,
                "Copy of " + source.getDescription(),
                new ArrayList<>(source.getEffectsToResolve()),
                copyCard.hasKeyword(Keyword.CONVERGE) ? 0 : source.getXValue(),
                targetId,
                source.getSourcePermanentId(),
                damageAssignments,
                targetZone,
                targetCardIds,
                sourceTargetIds
        );
        if (source.getTargetingCard() != source.getCard()) {
            copy.setCastCard(copyCard.createRuntimeCopyWithFace(source.getTargetingCard()));
        }
        copy.setCopy(true);
        copy.setSourcePermanentSnapshot(source.getSourcePermanentSnapshot() == null
                ? null : new com.github.laxika.magicalvibes.model.Permanent(source.getSourcePermanentSnapshot()));
        copy.setSourcePlanarObject(source.getSourcePlanarObject() == null ? null : source.getSourcePlanarObject().copy());
        // CR 707.10: a copy refers to the objects used to pay the original's costs (e.g. a sacrificed permanent).
        copy.setSacrificedPermanentSnapshot(source.getSacrificedPermanentSnapshot() == null
                ? null : new com.github.laxika.magicalvibes.model.Permanent(source.getSacrificedPermanentSnapshot()));
        copy.setKicked(source.isKicked());
        copy.setGiftPromised(source.isGiftPromised());
        copy.setAlternateCost(source.isAlternateCost());
        copy.setWaterbendCostPaid(source.isWaterbendCostPaid());
        copy.setCastForForetell(source.isCastForForetell());
        copy.setRepeatedAdditionalCosts(source.getRepeatedAdditionalCosts());
        copy.setRevealCardFromHandCostPaid(source.isRevealCardFromHandCostPaid());
        copy.setAdditionalEnterCounters(source.getAdditionalEnterCounters());
        copy.setTargetFilters(source.getTargetFilters());
        copy.setOpponentChosenTargetPlayerId(source.getOpponentChosenTargetPlayerId());
        copy.getGrantedKeywordsOnEntry().addAll(source.getGrantedKeywordsOnEntry());
        return copy;
    }

    public Card createCopyCard(Card original) {
        return createCopyCard(original, false);
    }

    public Card createTokenCopyCard(Card original) {
        Card copy = createCopyCard(original, false);
        copy.setToken(true);
        return copy;
    }

    public Card createCopyCardWithoutEpic(Card original) {
        return createCopyCard(original, true);
    }

    public void checkSpellCopyTriggers(GameData gameData, StackEntry copyEntry) {
        if (triggerCollectionService == null || copyEntry == null || !copyEntry.isCopy()) return;
        if (copyEntry.getEntryType() != StackEntryType.INSTANT_SPELL
                && copyEntry.getEntryType() != StackEntryType.SORCERY_SPELL) {
            return;
        }
        triggerCollectionService.checkSpellCopyTriggers(gameData, copyEntry);
    }

    private Card createCopyCard(Card original, boolean withoutEpic) {
        Card copy = new Card();

        copy.setName(original.getName());
        copy.setType(original.getType());
        copy.setManaCost(original.getManaCost());
        copy.setCombinedManaValue(original.getCombinedManaValue());
        copy.setSplitHalves(original.getSplitHalves());
        copy.setColor(original.getColor());
        copy.setColors(original.getColors());
        copy.setAdditionalTypes(original.getAdditionalTypes());
        copy.setSupertypes(original.getSupertypes());
        copy.setSubtypes(original.getSubtypes());
        copy.setCardText(original.getCardText());
        copy.setPower(original.getPower());
        copy.setToughness(original.getToughness());
        if (original.getRoomDoorManaCosts().size() == 2) {
            copy.setRoomDoorManaCosts(original.getRoomDoorManaCosts());
        }
        Set<Keyword> copiedKeywords = original.getKeywords().isEmpty()
                ? EnumSet.noneOf(Keyword.class)
                : EnumSet.copyOf(original.getKeywords());
        if (withoutEpic) {
            copiedKeywords.remove(Keyword.EPIC);
        }
        copy.setKeywords(copiedKeywords);
        copy.setLoyalty(original.getLoyalty());
        copy.setXColorRestrictions(original.getXColorRestrictions());
        copy.setSpellCastTimingRestriction(original.getSpellCastTimingRestriction());
        if (original.getXValueCap() != null) {
            copy.setXValueCap(original.getXValueCap());
        }

        for (EffectSlot slot : EffectSlot.values()) {
            for (var reg : original.getEffectRegistrations(slot)) {
                if (withoutEpic && reg.effect() instanceof EpicEffect) {
                    continue;
                }
                copy.addEffect(slot, reg.effect(), reg.triggerMode());
            }
        }

        copy.copyTargetingFrom(original);
        for (var ability : original.getActivatedAbilities()) {
            copy.addActivatedAbility(ability);
        }

        return copy;
    }
}
