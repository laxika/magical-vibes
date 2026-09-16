package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardTargetedRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwardTargetedRestrictedManaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardTargetedRestrictedManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AwardTargetedRestrictedManaEffect manaEffect = (AwardTargetedRestrictedManaEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, manaEffect.amount(),
                AmountContext.forStackEntry(entry, source));
        if (amount <= 0 || entry.getTargetId() == null) {
            return;
        }

        ManaPool pool = gameData.playerManaPools.get(entry.getTargetId());
        if (pool == null) {
            return;
        }
        manaEffect.restriction().applyTo(pool, manaEffect.color(), amount,
                source == null ? null : source.getChosenSubtype());

        String playerName = gameData.playerIdToName.get(entry.getTargetId());
        gameLogService.append(gameData, GameLog.text(playerName + " adds " + amount + " "
                + manaEffect.color().getCode() + " (" + manaEffect.restriction().description() + ")."));
        log.info("Game {} - {} adds {} {} (restricted: {})", gameData.id, playerName, amount,
                manaEffect.color(), manaEffect.restriction().description());
    }
}
