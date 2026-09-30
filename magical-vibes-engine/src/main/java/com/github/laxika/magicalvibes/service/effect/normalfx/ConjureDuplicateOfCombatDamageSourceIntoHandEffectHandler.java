package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfCombatDamageSourceIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Semblance Scanner's nontoken combat-damage-source duplicate. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfCombatDamageSourceIntoHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfCombatDamageSourceIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Card sourceCard = source != null
                ? source.getCard()
                : entry.getSourcePermanentSnapshot() == null
                        ? null : entry.getSourcePermanentSnapshot().getCard();
        if (sourceCard == null || sourceCard.isToken()) {
            return;
        }

        Card duplicate = sourceCard.createCardCopy();
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.freeze();
        gameData.addCardToHand(entry.getControllerId(), duplicate);
        gameLogService.append(gameData, GameLog.cardThen(duplicate, " is conjured into "
                + gameData.playerIdToName.get(entry.getControllerId()) + "'s hand."));
    }
}
