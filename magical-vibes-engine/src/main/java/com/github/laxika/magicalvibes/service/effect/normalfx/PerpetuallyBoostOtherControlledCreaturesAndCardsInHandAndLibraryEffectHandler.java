package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostOtherControlledCreaturesAndCardsInHandAndLibraryEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a scoped perpetual power/toughness boost for controlled creatures and hand/library cards. */
@Component
@RequiredArgsConstructor
public class PerpetuallyBoostOtherControlledCreaturesAndCardsInHandAndLibraryEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostOtherControlledCreaturesAndCardsInHandAndLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (PerpetuallyBoostOtherControlledCreaturesAndCardsInHandAndLibraryEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();

        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if (permanent.getId().equals(sourcePermanentId)
                    || !gameQueryService.isCreature(gameData, permanent)
                    || !matches(gameData, entry, boost, permanent.getCard(), controllerId)) {
                continue;
            }
            PerpetualCardPowerToughnessSupport.remember(
                    gameData, permanent.getCard(), boost.powerBoost(), boost.toughnessBoost());
        }

        applyToCards(gameData, entry, boost, gameData.playerHands.getOrDefault(controllerId, List.of()), controllerId);
        applyToCards(gameData, entry, boost, gameData.playerDecks.getOrDefault(controllerId, List.of()), controllerId);
    }

    private void applyToCards(GameData gameData, StackEntry entry,
                              PerpetuallyBoostOtherControlledCreaturesAndCardsInHandAndLibraryEffect effect,
                              List<Card> cards, UUID controllerId) {
        for (Card card : cards) {
            if (matches(gameData, entry, effect, card, controllerId)) {
                PerpetualCardPowerToughnessSupport.remember(
                        gameData, card, effect.powerBoost(), effect.toughnessBoost());
            }
        }
    }

    private boolean matches(GameData gameData, StackEntry entry,
                            PerpetuallyBoostOtherControlledCreaturesAndCardsInHandAndLibraryEffect effect,
                            Card card, UUID cardOwnerId) {
        return predicateEvaluationService.matchesCardPredicate(
                card, effect.filter(), entry.getCard().getId(), gameData, cardOwnerId);
    }
}
