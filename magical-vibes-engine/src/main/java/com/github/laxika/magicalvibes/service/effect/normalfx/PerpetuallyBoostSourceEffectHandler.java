package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Applies a perpetual power/toughness change to the ability's own card while it is still the object the ability
 * refers to ({@link SubjectResolutionService#sourceObjectCard}): a source that has changed zones since is a new
 * object and is not changed (CR 400.7).
 */
@Component
@RequiredArgsConstructor
public class PerpetuallyBoostSourceEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetuallyBoostSourceEffect boost = (PerpetuallyBoostSourceEffect) effect;
        var source = subjectResolutionService.sourceObjectCard(gameData, entry, effect);
        if (source == null) {
            return;
        }
        if (source.zone() == Zone.GRAVEYARD) {
            applyToGraveyardSource(gameData, source.card(), boost);
        } else {
            PerpetualCardPowerToughnessSupport.remember(
                    gameData, source.card(), boost.powerBoost(), boost.toughnessBoost());
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

    private void applyToGraveyardSource(GameData gameData, Card sourceCard,
                                        PerpetuallyBoostSourceEffect boost) {
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
