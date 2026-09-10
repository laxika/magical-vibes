package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.EpicEffect;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
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

    public CopySupport() {
        this.triggerCollectionService = null;
    }

    @Autowired
    public CopySupport(TriggerCollectionService triggerCollectionService) {
        this.triggerCollectionService = triggerCollectionService;
    }

    public void addCopyToStack(GameData gameData, StackEntry copyEntry) {
        gameData.stack.add(copyEntry);
        if (triggerCollectionService != null) {
            triggerCollectionService.checkSpellCopyTriggers(gameData, copyEntry);
        }
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

    private StackEntry createCopyStackEntry(StackEntry source, Card copyCard, UUID controllerId,
                                             UUID targetId, Zone targetZone, List<UUID> targetCardIds) {
        StackEntry copy = new StackEntry(
                source.getEntryType(),
                copyCard,
                controllerId,
                "Copy of " + source.getDescription(),
                new ArrayList<>(source.getEffectsToResolve()),
                source.getXValue(),
                targetId,
                source.getSourcePermanentId(),
                source.getDamageAssignments(),
                targetZone,
                targetCardIds,
                source.getTargetIds() != null ? new ArrayList<>(source.getTargetIds()) : null
        );
        copy.setCopy(true);
        copy.setSourcePlanarObject(source.getSourcePlanarObject() == null ? null : source.getSourcePlanarObject().copy());
        copy.setKicked(source.isKicked());
        copy.setTargetFilters(source.getTargetFilters());
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
        copy.setColor(original.getColor());
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

        return copy;
    }
}
