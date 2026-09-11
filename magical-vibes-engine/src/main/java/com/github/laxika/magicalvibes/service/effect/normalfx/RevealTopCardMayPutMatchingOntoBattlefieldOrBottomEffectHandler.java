package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect typed =
                (RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect) effect;
        if (typed.stage() != RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect.Stage.MAY_REVEAL) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(entry.getControllerId());
        if (deck == null || deck.isEmpty()) {
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), entry.getControllerId(), List.of(typed),
                entry.getCard().getName() + " — Reveal the top card of your library?"));
    }
}
