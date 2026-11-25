package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeEnchantmentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Restores a self-changing permanent to the card form its {@code BecomeCreatureEffect} replaced. */
@Component
@RequiredArgsConstructor
public class BecomeEnchantmentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeEnchantmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        if (((BecomeEnchantmentEffect) effect).onlyIfCreature()
                && !gameQueryService.isCreature(gameData, source)) {
            return;
        }

        // It returns to the form its "becomes a creature" effect replaced, which for a copy of the card is the copy, not
        // the copier's own card (CR 707.2); one that isn't in that form already has it.
        if (source.getBecomeCreatureBaseCard() != null && source.getBecomeCreatureResultCard() == source.getCard()) {
            source.setCard(source.getBecomeCreatureBaseCard());
            source.setBecomeCreatureBaseCard(null);
            source.setBecomeCreatureResultCard(null);
        }
        source.setTimestamp(gameData.nextTimestamp());

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " becomes an enchantment again."));
    }
}
