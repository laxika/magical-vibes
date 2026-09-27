package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Applies a perpetual power/toughness change to the source card's runtime copy. */
@Component
@RequiredArgsConstructor
public class PerpetuallyBoostSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetuallyBoostSourceEffect boost = (PerpetuallyBoostSourceEffect) effect;
        Card sourceCard = entry.getCard();
        Permanent source = entry.getSourcePermanentId() == null
                ? findBattlefieldSource(gameData, sourceCard)
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            applyToGraveyardSource(gameData, entry, sourceCard, boost);
        } else {
            Card copy = boostedCopy(source.getCard(), boost);
            source.exchangeCard(copy);
        }
    }

    private Card boostedCopy(Card source, PerpetuallyBoostSourceEffect boost) {
        var copy = source.createRuntimeCopy();
        if (copy.getPower() != null) {
            copy.setPower(copy.getPower() + boost.powerBoost());
        }
        if (copy.getToughness() != null) {
            copy.setToughness(copy.getToughness() + boost.toughnessBoost());
        }
        copy.freeze();
        return copy;
    }

    private Permanent findBattlefieldSource(GameData gameData, Card sourceCard) {
        if (sourceCard == null) {
            return null;
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(sourceCard.getId())
                        || permanent.getOriginalCard().getId().equals(sourceCard.getId())) {
                    return permanent;
                }
            }
        }
        return null;
    }

    private void applyToGraveyardSource(GameData gameData, StackEntry entry, Card sourceCard,
                                        PerpetuallyBoostSourceEffect boost) {
        if (sourceCard == null || entry.getControllerId() == null) {
            return;
        }
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (graveyard == null) {
            return;
        }
        for (int i = 0; i < graveyard.size(); i++) {
            Card card = graveyard.get(i);
            if (card.getId().equals(sourceCard.getId())) {
                graveyard.set(i, boostedCopy(card, boost));
                return;
            }
        }
    }
}
