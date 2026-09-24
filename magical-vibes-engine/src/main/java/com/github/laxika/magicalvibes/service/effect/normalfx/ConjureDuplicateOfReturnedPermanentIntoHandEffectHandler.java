package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfReturnedPermanentIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetualAnyManaTypeToCastSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Vesuvan Mist's kicked duplicate conjure from the bounced permanent's LKI. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfReturnedPermanentIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfReturnedPermanentIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card returnedCard = entry.lastKnownPermanentCard(entry.getTargetId());
        if (returnedCard == null) {
            return;
        }

        Card duplicate = returnedCard.createRuntimeCopyWithNewId();
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.setToken(true);
        duplicate.setTokenCard(true);
        duplicate.addEffect(EffectSlot.STATIC, new PerpetualAnyManaTypeToCastSelfEffect());
        duplicate.freeze();

        gameData.addCardToHand(entry.getControllerId(), duplicate);
        gameLogService.append(gameData, GameLog.textCardText(
                "A duplicate of ", duplicate, " is conjured into your hand."));
    }
}
