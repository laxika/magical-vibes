package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutPermanentCardsOfTypesFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutPermanentCardsOfTypesFromHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutPermanentCardsOfTypesFromHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutPermanentCardsOfTypesFromHandEffect typedEffect =
                (PutPermanentCardsOfTypesFromHandEffect) effect;
        List<CardType> cardTypes = typedEffect.resolveTypesFromOpponents()
                ? findOpponentsPermanentTypes(gameData, entry.getControllerId())
                : typedEffect.cardTypes();
        if (cardTypes.isEmpty()) {
            return;
        }

        CardType cardType = cardTypes.getFirst();
        CardPredicate predicate = permanentCardOfType(cardType);
        List<CardEffect> nextEffects = new ArrayList<>();
        if (hasMatchingCardInHand(gameData, entry, predicate)) {
            String label = cardType.getDisplayName().toLowerCase(Locale.ROOT);
            nextEffects.add(new MayEffect(
                    new PutCardToBattlefieldEffect(predicate, label),
                    "Put a " + label + " permanent card from your hand onto the battlefield?"));
        }
        if (cardTypes.size() > 1) {
            nextEffects.add(new PutPermanentCardsOfTypesFromHandEffect(cardTypes.subList(1, cardTypes.size())));
        }
        if (!nextEffects.isEmpty()) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, nextEffects);
        }
    }

    private boolean hasMatchingCardInHand(GameData gameData, StackEntry entry, CardPredicate predicate) {
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        return hand != null && hand.stream().anyMatch(card -> predicateEvaluationService.matchesCardPredicate(
                card, predicate, entry.getCard().getId(), gameData, entry.getControllerId()));
    }

    private List<CardType> findOpponentsPermanentTypes(GameData gameData, UUID controllerId) {
        Set<CardType> cardTypes = EnumSet.noneOf(CardType.class);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                gameQueryService.getEffectiveCardTypes(gameData, permanent).stream()
                        .filter(CardType::isPermanentType)
                        .forEach(cardTypes::add);
            }
        }
        return new ArrayList<>(cardTypes);
    }

    private static CardPredicate permanentCardOfType(CardType cardType) {
        return new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardTypePredicate(cardType)));
    }
}
