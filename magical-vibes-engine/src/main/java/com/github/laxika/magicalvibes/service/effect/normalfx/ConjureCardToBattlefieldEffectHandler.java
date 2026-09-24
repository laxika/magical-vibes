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
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a full card copy conjured onto its controller's battlefield. */
@Component
@RequiredArgsConstructor
public class ConjureCardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardToBattlefieldEffect conjure = (ConjureCardToBattlefieldEffect) effect;
        CardSet set = CardSet.findByCode(conjure.setCode());
        if (set == null) {
            throw new IllegalArgumentException("Unknown card set: " + conjure.setCode());
        }

        CardPrinting printing = cardCatalog.findByCollectorNumber(set, conjure.collectorNumber());
        Card conjuredCard = printing.createCard();
        conjuredCard.setOwnerId(entry.getControllerId());
        Permanent permanent = new Permanent(conjuredCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), permanent);
        entry.getCreatedPermanentIds().add(permanent.getId());

        String controllerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData, GameLog.textCardText(
                controllerName + " conjures a ", conjuredCard, " onto the battlefield."));
    }
}
