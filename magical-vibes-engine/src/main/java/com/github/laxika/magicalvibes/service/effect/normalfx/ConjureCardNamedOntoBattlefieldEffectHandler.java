package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
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
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a named-card conjure by creating the requested printing and entering it normally. */
@Component
@RequiredArgsConstructor
public class ConjureCardNamedOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardNamedOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var conjure = (ConjureCardNamedOntoBattlefieldEffect) effect;
        CardSet set = CardSet.findByCode(conjure.setCode());
        if (set == null) {
            throw new IllegalArgumentException("Unknown card set code: " + conjure.setCode());
        }

        Card card = cardCatalog.findByCollectorNumber(set, conjure.collectorNumber()).createCard();
        Permanent permanent = new Permanent(card);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), permanent);
        if (gameQueryService.findPermanentById(gameData, permanent.getId()) == null) {
            return;
        }

        graveyardReturnSupport.handleCreatureEtbAndLegendRule(
                gameData, entry.getControllerId(), permanent, card);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(entry.getControllerId()) + " conjures ",
                card, " onto the battlefield."));
    }
}
