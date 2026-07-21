package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillAndTransformIfTypesEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MillAndTransformIfTypesEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final AnimationSupport animationSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillAndTransformIfTypesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MillAndTransformIfTypesEffect e = (MillAndTransformIfTypesEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        // Peek before milling so the type check still works if a replacement effect
        // redirects the card out of the graveyard (Aberrant Researcher ruling).
        Card milledCard = deck.getFirst();
        graveyardService.resolveMillPlayer(gameData, controllerId, 1);

        boolean matches = e.cardTypes().stream().anyMatch(milledCard::hasType);
        if (!matches) {
            return;
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null || self.isTransformed()) {
            return;
        }
        if (gameQueryService.isTransformPrevented(gameData, self)) {
            log.info("Game {} - {} can't transform (transform prevented)", gameData.id, self.getCard().getName());
            return;
        }

        animationSupport.transformToBackFace(gameData, self);
    }
}
