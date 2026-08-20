package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantForetellToNonlandCardsInHandEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Supplies Dream Devourer's dynamic foretell costs without changing cards in hand. */
@Component
public class GrantForetellToNonlandCardsInHandEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantForetellToNonlandCardsInHandEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        return 0;
    }

    @Override
    public ManaCost grantedForetellCost(GameData gameData, UUID playerId, Card card,
                                        CardEffect effect, CostModificationSource source) {
        if (!source.controlledBy(playerId)
                || card.hasType(CardType.LAND)
                || card.getParsedManaCost() == null
                || card.getCastingOption(ForetellCast.class).isPresent()) {
            return null;
        }
        return card.getParsedManaCost().reducedBy(new ManaCost("{2}"));
    }
}
