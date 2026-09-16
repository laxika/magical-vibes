package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutDiscardedCardOnTopOfLibraryThenBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPermanentToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutDiscardedCardOnTopOfLibraryThenBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final RevealTopCardPermanentToBattlefieldEffectHandler revealTopCardHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutDiscardedCardOnTopOfLibraryThenBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID discardedCardId = entry.getTriggeringCardId();
        UUID ownerId = entry.getControllerId();
        if (discardedCardId == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(ownerId);
        if (library == null) {
            return;
        }

        if (library.isEmpty() || !library.getFirst().getId().equals(discardedCardId)) {
            Card discardedCard = findCardInGraveyard(gameData, discardedCardId);
            if (discardedCard == null) {
                return;
            }
            permanentRemovalService.removeCardFromGraveyardById(gameData, discardedCardId);
            library.addFirst(discardedCard);
        }

        if (!library.isEmpty() && library.getFirst().getId().equals(discardedCardId)) {
            revealTopCardHandler.resolve(gameData, entry, new RevealTopCardPermanentToBattlefieldEffect());
        }
    }

    private Card findCardInGraveyard(GameData gameData, UUID cardId) {
        for (List<Card> graveyard : gameData.playerGraveyards.values()) {
            if (graveyard == null) {
                continue;
            }
            for (Card card : graveyard) {
                if (card.getId().equals(cardId)) {
                    return card;
                }
            }
        }
        return null;
    }
}
