package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGiveLandExtraManaEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Vigorous Farming's perpetual land-card ability. */
@Component
public class PerpetuallyGiveLandExtraManaEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGiveLandExtraManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetuallyGiveLandExtraManaEffect grant = (PerpetuallyGiveLandExtraManaEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null) {
            return;
        }

        for (int i = 0; i < library.size(); i++) {
            Card card = library.get(i);
            if (!card.hasType(CardType.LAND)) {
                continue;
            }

            Card modifiedCard = card.createRuntimeCopy();
            modifiedCard.addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                    new AddManaWhenLandTappedForManaEffect(grant.color(), false, true));
            library.set(i, modifiedCard);
            return;
        }
    }
}
