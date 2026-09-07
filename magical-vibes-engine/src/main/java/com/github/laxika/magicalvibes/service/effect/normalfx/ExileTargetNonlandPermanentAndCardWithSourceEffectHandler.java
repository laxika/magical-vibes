package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetNonlandPermanentAndCardWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetNonlandPermanentAndCardWithSourceEffectHandler implements NormalEffectHandlerBean {

    private static final PermanentNotPredicate NONLAND_PERMANENT =
            new PermanentNotPredicate(new PermanentIsLandPredicate());
    private static final CardAllOfPredicate NONLAND_PERMANENT_CARD = new CardAllOfPredicate(List.of(
            new CardIsPermanentPredicate(),
            new CardNotPredicate(new CardTypePredicate(CardType.LAND))));

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetNonlandPermanentAndCardWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        for (UUID cardId : entry.getTargetCardIds()) {
            Permanent permanent = findPermanentByCardId(gameData, cardId);
            if (permanent != null) {
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, NONLAND_PERMANENT)) {
                    exilePermanent(gameData, entry, permanent, sourcePermanentId);
                }
                continue;
            }

            Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, cardId);
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
            if (graveyardCard != null && ownerId != null
                    && predicateEvaluationService.matchesCardPredicate(
                    graveyardCard, NONLAND_PERMANENT_CARD, entry.getCard().getId(), gameData, ownerId)) {
                permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, cardId);
                exileService.exileCard(gameData, ownerId, graveyardCard, sourcePermanentId);
                gameLogService.append(gameData, GameLog.cardTextCard(graveyardCard,
                        " is exiled from a graveyard by ", entry.getCard(), "."));
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private void exilePermanent(GameData gameData, StackEntry entry, Permanent permanent,
                                UUID sourcePermanentId) {
        Card card = permanent.getOriginalCard();
        boolean token = permanent.getCard().isToken();

        if (!permanentRemovalService.removePermanentToExile(gameData, permanent)) {
            return;
        }
        if (!token) {
            if (sourcePermanentId != null) {
                gameData.associateExiledCardWithSource(card.getId(), sourcePermanentId);
            }
        } else {
            gameData.removeFromExile(card.getId());
        }
        gameLogService.append(gameData, GameLog.cardTextCard(card, " is exiled by ", entry.getCard(), "."));
    }

    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(cardId)) {
                    return permanent;
                }
            }
        }
        return null;
    }
}
