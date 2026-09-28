package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CastDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CastDiscardedCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastDiscardedCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID discardedCardId = entry.getTriggeringCardId();
        if (discardedCardId == null) {
            return;
        }

        Card discardedCard = gameQueryService.findCardInGraveyardById(gameData, discardedCardId);
        UUID graveyardOwnerId = discardedCard == null
                ? null : gameQueryService.findGraveyardOwnerById(gameData, discardedCardId);
        if (discardedCard == null || !entry.getControllerId().equals(graveyardOwnerId)
                || discardedCard.hasType(CardType.LAND)) {
            return;
        }

        long expectedEntryVersion = entry.getTriggeringCardGraveyardEntryVersion();
        if (expectedEntryVersion != 0
                && gameData.graveyardEntryVersion(discardedCardId) != expectedEntryVersion) {
            return;
        }

        CardEffect castEffect = new CastCardFromGraveyardEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                new CardAnyOfPredicate(List.of()),
                true);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                discardedCard,
                entry.getControllerId(),
                List.of(castEffect),
                entry.getCard().getName() + " — Cast " + discardedCard.getName()
                        + " from your graveyard?"));
    }
}
