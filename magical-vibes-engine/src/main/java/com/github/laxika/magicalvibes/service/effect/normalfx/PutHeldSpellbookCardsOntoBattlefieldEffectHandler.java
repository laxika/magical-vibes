package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutHeldSpellbookCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutHeldSpellbookCardsOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutHeldSpellbookCardsOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> cards;
        synchronized (gameData.heldSpellbookCards) {
            cards = new ArrayList<>(gameData.heldSpellbookCards);
            gameData.heldSpellbookCards.clear();
        }
        UUID controllerId = entry.getControllerId();
        for (Card card : cards) {
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, new Permanent(card));
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(controllerId) + " puts ", card, " onto the battlefield."));
        }
        for (Card card : cards) {
            if (card.hasType(CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
            }
        }
    }
}
