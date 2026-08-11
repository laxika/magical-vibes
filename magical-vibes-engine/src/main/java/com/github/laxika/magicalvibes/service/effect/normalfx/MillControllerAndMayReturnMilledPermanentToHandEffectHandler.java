package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Mills the controller's library and offers only permanent cards that were milled by this
 * resolution.
 */
@Component
@RequiredArgsConstructor
public class MillControllerAndMayReturnMilledPermanentToHandEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndMayReturnMilledPermanentToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MillControllerAndMayReturnMilledPermanentToHandEffect millEffect =
                (MillControllerAndMayReturnMilledPermanentToHandEffect) effect;
        List<Card> milled = graveyardService.resolveMillPlayer(
                gameData, entry.getControllerId(), millEffect.count());

        List<Card> permanentCards = milled.stream()
                .filter(card -> card.getType().isPermanentType())
                .filter(card -> gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null)
                .toList();
        if (permanentCards.isEmpty()) {
            return;
        }

        UUID groupId = UUID.randomUUID();
        for (int i = permanentCards.size() - 1; i >= 0; i--) {
            Card card = permanentCards.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    entry.getControllerId(),
                    List.of(new ReturnMilledPermanentToHandEffect(groupId)),
                    "Put " + card.getName() + " into your hand?"));
        }
    }
}
