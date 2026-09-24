package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConjureCardNamedOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardNamedOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardNamedOntoBattlefieldEffect conjure =
                (ConjureCardNamedOntoBattlefieldEffect) effect;
        Card card = createCardByName(conjure.cardName());
        if (card == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        card.setOwnerId(controllerId);
        card.freeze();
        Permanent permanent = new Permanent(card);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
        gameLogService.append(gameData,
                GameLog.entersBattlefieldUnder(card, gameData.playerIdToName.get(controllerId)));
    }

    private Card createCardByName(String cardName) {
        for (CardSet set : CardSet.values()) {
            for (CardPrinting printing : cardCatalog.getPrintings(set)) {
                Card card = printing.createCard();
                if (cardName.equals(card.getName())) {
                    return card;
                }
            }
        }
        return null;
    }
}
