package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardsInLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a perpetual power/toughness boost for creature cards in a library. */
@Component
public class PerpetuallyBoostCreatureCardsInLibraryEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostCreatureCardsInLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (PerpetuallyBoostCreatureCardsInLibraryEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null) {
            return;
        }

        for (int i = 0; i < library.size(); i++) {
            Card card = library.get(i);
            if (!card.hasType(CardType.CREATURE)) {
                continue;
            }

            Card modifiedCard = card.createRuntimeCopy();
            modifiedCard.addEffect(EffectSlot.STATIC,
                    new StaticBoostEffect(boost.powerBoost(), boost.toughnessBoost(), GrantScope.SELF));
            library.set(i, modifiedCard);
        }
    }
}
