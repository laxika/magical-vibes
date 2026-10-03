package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastUpToNSpellsFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a bounded sequence of free spell casts from the controller's hand. */
@Component
@RequiredArgsConstructor
public class CastUpToNSpellsFromHandWithoutPayingManaCostEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastUpToNSpellsFromHandWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CastUpToNSpellsFromHandWithoutPayingManaCostEffect castEffect =
                (CastUpToNSpellsFromHandWithoutPayingManaCostEffect) effect;
        if (castEffect.maxCount() > 0) {
            queueOffers(gameData, entry.getControllerId(), entry.getSourcePermanentId(),
                    castEffect.maxCount(), UUID.randomUUID(), null);
        }
    }

    /** Rebuilds the remaining offers after one card has been accepted. */
    public void queueOffers(GameData gameData, UUID controllerId, UUID sourcePermanentId,
                            int remainingCount, UUID choiceGroupId, UUID excludedCardId) {
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty() || remainingCount <= 0) {
            return;
        }

        for (int i = hand.size() - 1; i >= 0; i--) {
            Card card = hand.get(i);
            if (card.getId().equals(excludedCardId)
                    || card.hasType(CardType.LAND)
                    || card.isCastOnlyFromGraveyard()) {
                continue;
            }
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    controllerId,
                    List.of(
                            new CastUpToNSpellsFromHandWithoutPayingManaCostEffect(
                                    remainingCount, choiceGroupId),
                            new MayCastFromHandWithoutPayingManaCostEffect(
                                    true, choiceGroupId, null)),
                    "Cast " + card.getName() + " without paying its mana cost?",
                    sourcePermanentId,
                    (Integer) null));
        }
    }
}
