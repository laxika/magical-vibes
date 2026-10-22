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
            // Perpetual changes stay with the card in any zone, so a source that already left the
            // battlefield (e.g. returned to hand before this resolved) is remembered by card id. A card
            // that has since re-entered the battlefield is a new object the ability can't change (CR 400.7).
            if (isInGraveyard(gameData, sourceCard)) {
                applyToGraveyardSource(gameData, sourceCard, boost);
            } else if (findBattlefieldSource(gameData, sourceCard) == null) {
                PerpetualCardPowerToughnessSupport.remember(
                        gameData, sourceCard, boost.powerBoost(), boost.toughnessBoost());
            }
        } else {
            PerpetualCardPowerToughnessSupport.remember(
                    gameData, source.getCard(), boost.powerBoost(), boost.toughnessBoost());
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

    private boolean isInGraveyard(GameData gameData, Card sourceCard) {
        return gameData.playerGraveyards.values().stream()
                .anyMatch(graveyard -> graveyard.stream()
                        .anyMatch(card -> card.getId().equals(sourceCard.getId())));
    }

    private void applyToGraveyardSource(GameData gameData, Card sourceCard,
                                        PerpetuallyBoostSourceEffect boost) {
        if (sourceCard == null) {
            return;
        }
        for (List<Card> graveyard : gameData.playerGraveyards.values()) {
            for (int i = 0; i < graveyard.size(); i++) {
                Card card = graveyard.get(i);
                if (card.getId().equals(sourceCard.getId())) {
                    graveyard.set(i, boostedCopy(card, boost));
                    return;
                }
            }
        }
    }
}
