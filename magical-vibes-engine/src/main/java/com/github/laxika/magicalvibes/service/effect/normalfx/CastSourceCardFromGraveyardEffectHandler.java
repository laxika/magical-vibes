package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CastSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Queues a normal-cost cast offer for the resolving source card, reusing the normal graveyard-cast flow.
 */
@Component
@RequiredArgsConstructor
public class CastSourceCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastSourceCardFromGraveyardEffect.class;
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

        CardEffect castEffect = new CastCardFromGraveyardEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                new CardAnyOfPredicate(List.of()),
                false);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                graveyardCard,
                controllerId,
                List.of(castEffect),
                graveyardCard.getName() + " — Cast it from your graveyard?"));
    }
}
