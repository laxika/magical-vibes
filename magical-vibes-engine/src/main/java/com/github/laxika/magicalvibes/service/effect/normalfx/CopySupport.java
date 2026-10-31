package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DeferredSpellCopy;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTargetAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.EpicEffect;
import com.github.laxika.magicalvibes.model.effect.PsychicBattleRetargetEffect;
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

    /**
     * Whether the ability on the stack carries its own "this ability can't be copied" restriction, as
     * opposed to its source card being uncopyable. Gogo, Master of Mimicry's activated ability is the
     * only such ability: it is identified by the {@link CopyTargetAbilityEffect} it resolves.
     */
    public static boolean isAbilityThatCantBeCopied(StackEntry abilityEntry) {
        return abilityEntry.getEffectsToResolve().stream().anyMatch(CopyTargetAbilityEffect.class::isInstance);
    }

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
        addCopyToStack(gameData, copyEntry, applySpellCopyReplacement, null);
    }

    /** {@link #addCopyToStack(GameData, StackEntry, boolean, PendingMayAbility)} with the spell-copy replacement applied. */
    public void addCopyToStack(GameData gameData, StackEntry copyEntry, PendingMayAbility retargetPrompt) {
        addCopyToStack(gameData, copyEntry, true, retargetPrompt);
    }

    /**
     * Creates a copy whose controller may first choose new targets for it (CR 707.10c: once the targets
     * are decided, the copy is put onto the stack with those targets).
     *
     * <p>With a {@code retargetPrompt} — a {@link CopySpellEffect} prompt naming the copy's id, or the
     * first {@code PsychicBattleRetargetEffect} prompt of a per-target choice — the copy is staged in
     * {@link GameData#stagedSpellCopies} instead of entering the stack, and the prompt is queued ahead of
     * other pending may abilities but after the prompts of copies staged earlier, so copies created
     * together are decided in creation order. {@link #commitDecidedStagedCopies} later puts it onto the
     * stack. Without a prompt the copy is put onto the stack at once. Extra copies from global spell-copy
     * replacements each get their own "choose new targets" prompt.</p>
     */
    public void addCopyToStack(GameData gameData, StackEntry copyEntry, boolean applySpellCopyReplacement,
                               PendingMayAbility retargetPrompt) {
        if (retargetPrompt != null) {
            stageCopy(gameData, copyEntry, retargetPrompt);
        } else {
            gameData.stack.add(copyEntry);
            if (triggerCollectionService != null) {
                triggerCollectionService.checkSpellCopyTriggers(gameData, copyEntry);
            }
        }

        if (!applySpellCopyReplacement || !isSpellCopy(copyEntry) || gameQueryService == null) {
            return;
        }

        int additionalCopies = gameQueryService.countAdditionalSpellCopies(gameData, copyEntry.getControllerId());
        for (int i = 0; i < additionalCopies; i++) {
            Card extraCopyCard = createCopyCard(copyEntry.getCard());
            StackEntry extraCopy = createCopyStackEntry(
                    copyEntry, extraCopyCard, copyEntry.getControllerId(), copyEntry.getTargetId());
            PendingMayAbility extraPrompt = hasTargets(extraCopy)
                    ? new PendingMayAbility(
                            extraCopy.getCard(),
                            extraCopy.getControllerId(),
                            List.of(new CopySpellEffect()),
                            "Choose new targets for the additional copy of "
                                    + extraCopy.getCard().getName() + "?",
                            extraCopyCard.getId())
                    : null;
            addCopyToStack(gameData, extraCopy, false, extraPrompt);
        }
    }

    private void stageCopy(GameData gameData, StackEntry copyEntry, PendingMayAbility retargetPrompt) {
        gameData.stagedSpellCopies.add(copyEntry);
        int index = 0;
        while (index < gameData.pendingMayAbilities.size()
                && awaitsStagedCopy(gameData, gameData.pendingMayAbilities.get(index))) {
            index++;
        }
        gameData.pendingMayAbilities.add(index, retargetPrompt);
    }

    private static boolean awaitsStagedCopy(GameData gameData, PendingMayAbility pending) {
        synchronized (gameData.stagedSpellCopies) {
            for (StackEntry staged : gameData.stagedSpellCopies) {
                if (awaitsDecisionFor(pending, staged.getTargetableId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean awaitsDecisionFor(PendingMayAbility pending, UUID copyId) {
        for (CardEffect effect : pending.effects()) {
            if (effect instanceof CopySpellEffect && copyId.equals(pending.targetCardId())) {
                return true;
            }
            if (effect instanceof PsychicBattleRetargetEffect retarget && copyId.equals(retarget.spellCardId())) {
                return true;
            }
        }
        return false;
    }

    /** The staged (not yet on the stack) spell copy with this id, or {@code null}. */
    public static StackEntry findStagedCopy(GameData gameData, UUID copyId) {
        synchronized (gameData.stagedSpellCopies) {
            for (StackEntry staged : gameData.stagedSpellCopies) {
                if (staged.getTargetableId().equals(copyId)) {
                    return staged;
                }
            }
        }
        return null;
    }

    /** The staged spell copy with this id, otherwise the stack entry with this targetable id, or {@code null}. */
    public static StackEntry findStagedOrStackEntry(GameData gameData, UUID id) {
        StackEntry staged = findStagedCopy(gameData, id);
        if (staged != null) {
            return staged;
        }
        synchronized (gameData.stack) {
            for (StackEntry entry : gameData.stack) {
                if (entry.getTargetableId().equals(id)) {
                    return entry;
                }
            }
        }
        return null;
    }

    /** Whether this entry is a spell copy still waiting off the stack for its new targets. */
    public static boolean isStagedCopy(GameData gameData, StackEntry entry) {
        synchronized (gameData.stagedSpellCopies) {
            return gameData.stagedSpellCopies.stream().anyMatch(staged -> staged == entry);
        }
    }

    /**
     * Puts staged spell copies whose targets are decided onto the stack, in creation order, stopping at
     * the first copy whose prompt or per-target choice is still pending. Each copy enters the stack once,
     * with its final targets (CR 707.10c); only then do "whenever you copy a spell" triggers and the
     * becomes-the-target triggers of its final targets (such as ward, CR 702.21a) happen. Called from the
     * input-completion epilogue, so every way a retarget decision can end commits through here.
     */
    public void commitDecidedStagedCopies(GameData gameData) {
        while (!gameData.stagedSpellCopies.isEmpty()) {
            StackEntry next = gameData.stagedSpellCopies.getFirst();
            UUID copyId = next.getTargetableId();
            boolean undecided = gameData.pendingMayAbilities.stream()
                    .anyMatch(pending -> awaitsDecisionFor(pending, copyId));
            if (undecided) {
                return;
            }
            gameData.stagedSpellCopies.removeFirst();
            gameData.stack.add(next);
            if (triggerCollectionService != null) {
                triggerCollectionService.checkSpellCopyTriggers(gameData, next);
                if (next.getTargetId() != null || !next.getTargetIds().isEmpty()) {
                    triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData, next);
                }
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
            int copyCount = adjustedSpellCopyCount(gameData, pending.controllerId(), pending.requestedCopies());
            for (int i = 0; i < copyCount; i++) {
                Card copyCard = createCopyCard(pending.cardToCast());
                StackEntry copyEntry = createCopyStackEntry(
                        pending.original(), copyCard, pending.controllerId(), pending.original().getTargetId());
                addCopyToStack(gameData, copyEntry, false, copyEntry.getTargetId() == null ? null
                        : new PendingMayAbility(
                                pending.cardToCast(), pending.controllerId(), List.of(new CopySpellEffect()),
                                "Choose new targets for the copy of " + pending.cardToCast().getName() + "?",
                                copyCard.getId()));
            }
        }
    }

    /** Adjusts a counted spell-copy event for active global spell-copy replacements. */
    public int adjustedSpellCopyCount(GameData gameData, UUID copyControllerId, int requestedCopies) {
        if (requestedCopies <= 0 || gameQueryService == null) {
            return requestedCopies;
        }
        return requestedCopies + gameQueryService.countAdditionalSpellCopies(gameData, copyControllerId);
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
                copyCard.hasKeyword(Keyword.CONVERGE) || source.isXValueSetByCastTrigger() ? 0 : source.getXValue(),
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
        copy.setSacrificedColorCount(source.getSacrificedColorCount());
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
        // Keywords the spell has through its own text (spliced cards, kicker-granted keywords) are
        // copied with it. Split second from mana or a casting permission is an effect on the original
        // spell, which a copy does not share (CR 707.2).
        source.getGrantedKeywordsWhileOnStack().stream()
                .filter(keyword -> keyword != Keyword.SPLIT_SECOND)
                .forEach(copy.getGrantedKeywordsWhileOnStack()::add);
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

        copy.addCastingCostsAndSpellEffectsFrom(original, effect -> !withoutEpic || !(effect instanceof EpicEffect));
        copy.addStaticAbilitiesFrom(original, effect -> !withoutEpic || !(effect instanceof EpicEffect));
        for (EffectSlot slot : EffectSlot.values()) {
            for (var reg : original.getEffectRegistrations(slot)) {
                if (withoutEpic && reg.effect() instanceof EpicEffect) {
                    continue;
                }
                copy.addEffect(slot, reg.effect(), reg.triggerMode());
            }
        }

        copy.copyTargetingFrom(original);
        // "Cast only from your graveyard" is part of the copiable ability that stops the copy
        // from being cast from any other zone (Haakon, Stromgald Scourge).
        if (original.isCastOnlyFromGraveyard()) {
            original.getCastingOption(GraveyardCast.class).ifPresent(copy::addCastingOption);
        }
        for (var ability : original.getActivatedAbilities()) {
            copy.addActivatedAbility(ability);
        }

        return copy;
    }
}
