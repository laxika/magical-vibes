package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Shared copy helpers used by every "normal" Copy effect handler.
 *
 * <p>Extracted verbatim from {@code CopyResolutionService}; behavior is identical.
 */
@Component
public class CopySupport {

    public StackEntry createCopyStackEntry(StackEntry source, Card copyCard, UUID controllerId, UUID targetId) {
        StackEntry copy = new StackEntry(
                source.getEntryType(),
                copyCard,
                controllerId,
                "Copy of " + source.getCard().getName(),
                new ArrayList<>(source.getEffectsToResolve()),
                source.getXValue(),
                targetId,
                source.getSourcePermanentId(),
                source.getDamageAssignments(),
                source.getTargetZone(),
                source.getTargetCardIds() != null ? new ArrayList<>(source.getTargetCardIds()) : null,
                source.getTargetIds() != null ? new ArrayList<>(source.getTargetIds()) : null
        );
        copy.setCopy(true);
        return copy;
    }

    public Card createCopyCard(Card original) {
        Card copy = new Card();

        copy.setName(original.getName());
        copy.setType(original.getType());
        copy.setManaCost(original.getManaCost());
        copy.setColor(original.getColor());
        copy.setSupertypes(original.getSupertypes());
        copy.setSubtypes(original.getSubtypes());
        copy.setCardText(original.getCardText());
        copy.setPower(original.getPower());
        copy.setToughness(original.getToughness());
        copy.setKeywords(original.getKeywords());
        copy.setLoyalty(original.getLoyalty());
        copy.setXColorRestriction(original.getXColorRestriction());

        for (EffectSlot slot : EffectSlot.values()) {
            for (var reg : original.getEffectRegistrations(slot)) {
                copy.addEffect(slot, reg.effect(), reg.triggerMode());
            }
        }

        copy.copyTargetingFrom(original);

        return copy;
    }
}
