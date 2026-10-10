package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantEnterAbilityToPermanentCardsInGraveyardEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Resolves a perpetual ETB ability grant for permanent cards in a graveyard. */
@Component
public class PerpetuallyGrantEnterAbilityToPermanentCardsInGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantEnterAbilityToPermanentCardsInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantEnterAbilityToPermanentCardsInGraveyardEffect) effect;
        List<Card> graveyard = gameData.playerGraveyards
                .getOrDefault(entry.getControllerId(), List.of());
        for (Card card : graveyard) {
            if (!isPermanent(card)) {
                continue;
            }
            gameData.perpetualEnterEffectsByCardId.compute(card.getId(), (ignored, existing) -> {
                List<CardEffect> updated = new ArrayList<>(existing == null ? List.of() : existing);
                updated.add(grant.enterAbility());
                return List.copyOf(updated);
            });
        }
    }

    private boolean isPermanent(Card card) {
        return Arrays.stream(CardType.values())
                .filter(CardType::isPermanentType)
                .anyMatch(card::hasType);
    }
}
