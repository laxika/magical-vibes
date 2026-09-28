package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutChosenCardFromHandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the optional Snowborn Simulacra hand-choice follow-up. */
@Component
@RequiredArgsConstructor
public class PutChosenCardFromHandOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutChosenCardFromHandOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutChosenCardFromHandOntoBattlefieldEffect chosenEffect =
                (PutChosenCardFromHandOntoBattlefieldEffect) effect;
        if (chosenEffect.cardId() == null) {
            return;
        }

        var hand = gameData.playerHands.get(entry.getControllerId());
        Card selectedCard = hand == null ? null : hand.stream()
                .filter(card -> card.getId().equals(chosenEffect.cardId()))
                .findFirst()
                .orElse(null);
        if (selectedCard == null) {
            return;
        }

        hand.remove(selectedCard);
        Permanent permanent = new Permanent(selectedCard, Zone.LIBRARY);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), permanent);
        if (selectedCard.hasType(CardType.CREATURE)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, entry.getControllerId(), selectedCard, null, false);
        }
        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(entry.getControllerId()) + " puts ")
                .card(selectedCard)
                .text(" onto the battlefield.")
                .build());
    }
}
