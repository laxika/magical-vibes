package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PerpetualPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardsInLibraryEffect;
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

            gameData.perpetualPowerToughnessModifiers.merge(card.getId(),
                    new PerpetualPowerToughnessModifier(boost.powerBoost(), boost.toughnessBoost()),
                    (previous, added) -> new PerpetualPowerToughnessModifier(
                            previous.power() + added.power(), previous.toughness() + added.toughness()));
        }
    }
}
