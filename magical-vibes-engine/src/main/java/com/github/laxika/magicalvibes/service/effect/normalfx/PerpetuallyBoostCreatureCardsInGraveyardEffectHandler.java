package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PerpetualPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardsInGraveyardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerpetuallyBoostCreatureCardsInGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostCreatureCardsInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (PerpetuallyBoostCreatureCardsInGraveyardEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(controllerId, List.of());
        int permanentCount = boost.usePermanentCardCount()
                ? (int) graveyard.stream().filter(this::isPermanentCard).count()
                : 0;
        for (Card card : graveyard) {
            if (card != null && gameQueryService.cardHasType(
                    card, CardType.CREATURE, gameData, controllerId)) {
                if (boost.usePermanentCardCount()) {
                    PerpetualCardPowerToughnessSupport.remember(
                            gameData, card, permanentCount, permanentCount);
                } else {
                    gameData.perpetualPowerToughnessModifiers.merge(
                            card.getId(),
                            new PerpetualPowerToughnessModifier(boost.powerBoost(), boost.toughnessBoost()),
                            PerpetualPowerToughnessModifier::add);
                }
            }
        }
    }

    private boolean isPermanentCard(Card card) {
        return card != null && !card.isToken()
                && ((card.getType() != null && card.getType().isPermanentType())
                || card.getAdditionalTypes().stream().anyMatch(CardType::isPermanentType));
    }
}
