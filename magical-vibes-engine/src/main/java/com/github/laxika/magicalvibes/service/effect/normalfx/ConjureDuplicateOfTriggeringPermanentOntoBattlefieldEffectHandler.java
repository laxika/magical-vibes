package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTriggeringPermanentOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Mythweaver Poq's duplicate of the triggering land. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfTriggeringPermanentOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfTriggeringPermanentOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent triggeringPermanent = entry.getTriggeringPermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        Card sourceCard = triggeringPermanent == null ? null : triggeringPermanent.getCard();
        if (sourceCard == null || sourceCard.isToken()) return;

        Card conjuredCard = sourceCard.createConjuredCopy();
        conjuredCard.setToken(false);
        conjuredCard.setOwnerId(entry.getControllerId());

        Permanent permanent = new Permanent(conjuredCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), permanent);
        if (gameQueryService.findPermanentById(gameData, permanent.getId()) == null) return;

        entry.getCreatedPermanentIds().add(permanent.getId());
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures a duplicate of " + sourceCard.getName() + " onto the battlefield."));
        if (conjuredCard.hasType(CardType.LAND)) {
            battlefieldEntryService.processLandETBEffects(gameData, entry.getControllerId(), conjuredCard);
        } else {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, entry.getControllerId(), conjuredCard, null, false);
        }
    }
}
