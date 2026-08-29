package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.BendingType;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves mana produced by a firebending-style attack trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AwardManaUntilEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardManaUntilEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var manaEffect = (AwardManaUntilEndOfCombatEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }

        int amount = amountEvaluationService.evaluate(gameData, manaEffect.amount(),
                AmountContext.forStackEntry(entry, source));
        triggerCollectionService.checkBendingTriggers(gameData, entry.getControllerId(), BendingType.FIREBEND);
        if (amount <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        ManaColor effectiveColor = ManaProductionSupport.effectiveColor(gameData, controllerId,
                manaEffect.color());
        pool.addManaUntilEndOfCombat(effectiveColor, amount);
        if (source != null && gameQueryService.isCreature(gameData, source)) {
            pool.addCreatureMana(effectiveColor, amount);
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(
                playerName + " adds " + amount + " " + effectiveColor.getCode()
                        + " until end of combat."));
        log.info("Game {} - {} adds {} {} until end of combat", gameData.id, playerName,
                amount, effectiveColor);
    }
}
