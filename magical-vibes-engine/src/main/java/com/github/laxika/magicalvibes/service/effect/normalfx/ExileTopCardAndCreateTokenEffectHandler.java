package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

/** Resolves Ugin's top-card exile and Spirit-token creation as one linked operation. */
@Component
@RequiredArgsConstructor
public class ExileTopCardAndCreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardAndCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardAndCreateTokenEffect exileAndCreate = (ExileTopCardAndCreateTokenEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        Card exiledCard = deck == null || deck.isEmpty() ? null : deck.removeFirst();

        if (exiledCard != null) {
            gameData.addToExile(controllerId, exiledCard, null, true);
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability exiles the top card of its controller's library face down."));
        }

        var token = exileAndCreate.token();
        if (exiledCard != null) {
            var tokenEffects = new EnumMap<EffectSlot, CardEffect>(EffectSlot.class);
            if (token.tokenEffects() != null) {
                tokenEffects.putAll(token.tokenEffects());
            }
            CardEffect returnEffect = new ReturnExiledCardToHandEffect(exiledCard.getId(), controllerId);
            CardEffect existingLeaveEffect = tokenEffects.get(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD);
            tokenEffects.put(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                    existingLeaveEffect == null ? returnEffect
                            : SequenceEffect.of(existingLeaveEffect, returnEffect));
            token = token.withTokenEffects(tokenEffects);
        }

        createTokenEffectHandler.resolve(gameData, entry, token);
    }
}
