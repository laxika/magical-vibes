package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGiveSpellCastLifeLossToDefendingHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Putrefying Rotboar's perpetual hand-card ability. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGiveSpellCastLifeLossToDefendingHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGiveSpellCastLifeLossToDefendingHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID attackedTargetId = entry.getAttackedTargetId();
        if (attackedTargetId == null) {
            return;
        }

        UUID defendingPlayerId = gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
        if (defendingPlayerId == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(defendingPlayerId);
        if (hand == null) {
            return;
        }

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.hasType(CardType.LAND)) {
                continue;
            }

            Card modifiedCard = card.createRuntimeCopy();
            modifiedCard.addEffect(EffectSlot.ON_SELF_CAST, new LoseLifeEffect(1));
            hand.set(i, modifiedCard);
        }
    }
}
