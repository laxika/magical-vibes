package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldForTargetSpellControllerEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutCardToBattlefieldForTargetSpellControllerEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardToBattlefieldForTargetSpellControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCardToBattlefieldForTargetSpellControllerEffect) effect;
        StackEntry targetSpell = findTargetSpell(gameData, entry.getTargetId());
        if (targetSpell == null) {
            return;
        }

        playerInteractionSupport.applyPutCardToBattlefield(
                gameData,
                targetSpell.getControllerId(),
                new PutCardToBattlefieldEffect(e.predicate(), e.label()),
                entry.getXValue(),
                null,
                entry.getCard() == null ? null : entry.getCard().getId());
    }

    private StackEntry findTargetSpell(GameData gameData, UUID targetCardId) {
        if (targetCardId == null) {
            return null;
        }
        for (StackEntry stackEntry : gameData.stack) {
            Card card = stackEntry.getCard();
            if (card != null && card.getId().equals(targetCardId)) {
                return stackEntry;
            }
        }
        return null;
    }
}
