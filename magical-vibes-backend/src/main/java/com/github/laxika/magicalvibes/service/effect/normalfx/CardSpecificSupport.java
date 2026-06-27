package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Shared helpers used by card-specific reveal-until-type handlers.
 */
@Component
public class CardSpecificSupport {

    public boolean cardMatchesAnyType(Card card, Set<CardType> types) {
        if (types.contains(card.getType())) return true;
        for (CardType additionalType : card.getAdditionalTypes()) {
            if (types.contains(additionalType)) return true;
        }
        return false;
    }
}
