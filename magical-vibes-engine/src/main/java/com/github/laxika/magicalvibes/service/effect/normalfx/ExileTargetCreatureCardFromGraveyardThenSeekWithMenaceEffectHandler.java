package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardThenSeekWithMenaceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Puppet Raiser-style graveyard exile and perpetual menace seek. */
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureCardFromGraveyardThenSeekWithMenaceEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureCardFromGraveyardThenSeekWithMenaceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effectToResolve) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && entry.getTargetCardIds() != null
                && !entry.getTargetCardIds().isEmpty()) {
            targetCardId = entry.getTargetCardIds().getFirst();
        }
        if (targetCardId == null) {
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
        if (targetCard == null || !entry.getControllerId().equals(graveyardOwnerId)
                || !targetCard.hasType(CardType.CREATURE)) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer legal)."));
            return;
        }

        int soughtManaValue = targetCard.getManaValue() + 1;
        if (!graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, targetCardId, targetCard)) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer in a graveyard)."));
            return;
        }

        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(entry.getControllerId()) + " exiles ", targetCard,
                " from their graveyard."));

        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (library == null || hand == null) {
            return;
        }

        List<Card> matchingCards = new ArrayList<>(library.stream()
                .filter(card -> !card.isToken())
                .filter(card -> card.hasType(CardType.CREATURE))
                .filter(card -> card.getManaValue() == soughtManaValue)
                .toList());
        Collections.shuffle(matchingCards);
        if (matchingCards.isEmpty()) {
            return;
        }

        Card soughtCard = matchingCards.getFirst();
        library.remove(soughtCard);
        hand.add(soughtCard);
        gameData.perpetualKeywords.merge(soughtCard.getId(), Set.of(Keyword.MENACE), (existing, added) -> {
            Set<Keyword> merged = EnumSet.noneOf(Keyword.class);
            merged.addAll(existing);
            merged.addAll(added);
            return Set.copyOf(merged);
        });
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(entry.getControllerId()) + " seeks a card."));
    }
}
