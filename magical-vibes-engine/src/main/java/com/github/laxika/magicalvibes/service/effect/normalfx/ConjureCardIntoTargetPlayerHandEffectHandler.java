package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a named card conjured into the player targeted by the preceding effect. */
@Component
@RequiredArgsConstructor
public class ConjureCardIntoTargetPlayerHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardIntoTargetPlayerHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardIntoTargetPlayerHandEffect conjureEffect =
                (ConjureCardIntoTargetPlayerHandEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || conjureEffect.card() == null) {
            return;
        }

        Card conjured = conjureEffect.card().createRuntimeCopyWithNewId();
        conjured.setOwnerId(targetPlayerId);
        conjured.setToken(true);
        conjured.setTokenCard(true);
        conjured.freeze();
        gameData.addCardToHand(targetPlayerId, conjured);
        gameLogService.append(gameData, GameLog.textCardText(
                "A card named ", conjured, " is conjured into their hand."));
    }
}
