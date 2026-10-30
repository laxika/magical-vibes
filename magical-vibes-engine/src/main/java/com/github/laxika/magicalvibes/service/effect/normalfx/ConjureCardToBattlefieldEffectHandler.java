package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
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
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves a full card copy conjured onto its controller's battlefield. */
@Component
@RequiredArgsConstructor
public class ConjureCardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardToBattlefieldEffect conjure = (ConjureCardToBattlefieldEffect) effect;
        Card conjuredCard = findPrinting(conjure.setCode(), conjure.collectorNumber());
        conjuredCard.setOwnerId(entry.getControllerId());

        Permanent permanent = new Permanent(conjuredCard);
        if (MakeCreatedPermanentsAttackingEffectHandler.entersTappedAndAttacking(entry)) {
            permanent.tap();
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), permanent);
        triggerCollectionService.checkConjureTriggers(
                gameData, entry.getControllerId(), conjure.conjureEventCardCount());

        if (gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of()).stream()
                .anyMatch(candidate -> candidate.getId().equals(permanent.getId()))) {
            entry.getCreatedPermanentIds().add(permanent.getId());
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " conjures " + conjuredCard.getName() + " onto the battlefield."));
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, entry.getControllerId(), conjuredCard, null, false);
        }
    }

    private Card findPrinting(String setCode, String collectorNumber) {
        CardSet set = CardSet.findByCode(setCode);
        if (set == null) {
            throw new IllegalArgumentException("Unknown card set: " + setCode);
        }
        Card conjured = cardCatalog.findByCollectorNumber(set, collectorNumber).createCard();
        conjured.setConjured(true);
        return conjured;
    }
}
