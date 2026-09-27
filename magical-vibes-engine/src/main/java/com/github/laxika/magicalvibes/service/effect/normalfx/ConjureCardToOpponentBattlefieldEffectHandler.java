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
import com.github.laxika.magicalvibes.model.effect.ConjureCardToOpponentBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a Gift that conjures a registered card printing for the opponent. */
@Component
public class ConjureCardToOpponentBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    @Lazy
    private final TriggerCollectionService triggerCollectionService;

    public ConjureCardToOpponentBattlefieldEffectHandler(
            CardCatalog cardCatalog,
            BattlefieldEntryService battlefieldEntryService,
            GameQueryService gameQueryService,
            GameLogService gameLogService,
            @Lazy TriggerCollectionService triggerCollectionService
    ) {
        this.cardCatalog = cardCatalog;
        this.battlefieldEntryService = battlefieldEntryService;
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.triggerCollectionService = triggerCollectionService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardToOpponentBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardToOpponentBattlefieldEffect conjure =
                (ConjureCardToOpponentBattlefieldEffect) effect;
        UUID opponentId = gameQueryService.getOpponentId(gameData, entry.getControllerId());
        if (opponentId == null) {
            return;
        }

        CardSet set = CardSet.findByCode(conjure.setCode());
        if (set == null) {
            throw new IllegalArgumentException("Unknown card set: " + conjure.setCode());
        }
        CardPrinting printing = cardCatalog.findByCollectorNumber(set, conjure.collectorNumber());
        Card conjuredCard = printing.createCard();
        conjuredCard.setOwnerId(opponentId);
        Permanent permanent = new Permanent(conjuredCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, opponentId, permanent);
        entry.getCreatedPermanentIds().add(permanent.getId());

        String opponentName = gameData.playerIdToName.get(opponentId);
        gameLogService.append(gameData, GameLog.textCardText(
                opponentName + " receives a conjured ", conjuredCard, "."));
        if (conjure.gift()) {
            triggerCollectionService.checkControllerGivesGiftTriggers(gameData, entry.getControllerId());
        }
    }
}
