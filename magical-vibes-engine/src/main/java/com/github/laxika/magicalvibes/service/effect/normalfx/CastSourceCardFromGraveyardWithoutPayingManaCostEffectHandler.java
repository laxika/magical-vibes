package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastSourceCardFromGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Queues a free-cast offer for the resolving source card, reusing the normal graveyard-cast flow.
 */
@Component
@RequiredArgsConstructor
public class CastSourceCardFromGraveyardWithoutPayingManaCostEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastSourceCardFromGraveyardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card sourceCard = entry.getCard();
        if (sourceCard == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, sourceCard.getId());
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, sourceCard.getId());
        if (graveyardCard == null || !controllerId.equals(graveyardOwnerId)) {
            return;
        }

        CastTargetInstantOrSorceryFromGraveyardEffect castEffect =
                new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD, true);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                graveyardCard,
                controllerId,
                List.of(castEffect),
                graveyardCard.getName() + " — Cast it without paying its mana cost?"));
    }
}
