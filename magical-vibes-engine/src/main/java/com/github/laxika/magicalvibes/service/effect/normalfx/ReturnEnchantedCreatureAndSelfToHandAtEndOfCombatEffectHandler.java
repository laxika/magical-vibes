package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.DelayedEndOfCombatTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveDelayedPermanentActionEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureAndSelfToHandAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves {@link ReturnEnchantedCreatureAndSelfToHandAtEndOfCombatEffect}: schedules the source
 * Aura first and its enchanted creature second, so the Aura is not orphaned before it can return.
 */
@Component
@RequiredArgsConstructor
public class ReturnEnchantedCreatureAndSelfToHandAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnEnchantedCreatureAndSelfToHandAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null) {
            aura = entry.getSourcePermanentSnapshot();
        }
        if (aura == null || aura.getAttachedTo() == null) {
            return;
        }

        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchanted == null) {
            return;
        }

        List<CardEffect> returns = new ArrayList<>();
        returns.add(new ResolveDelayedPermanentActionEffect(new DelayedPermanentAction(aura.getId(),
                DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_OF_COMBAT)));
        returns.add(new ResolveDelayedPermanentActionEffect(new DelayedPermanentAction(enchanted.getId(),
                DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_OF_COMBAT)));
        gameData.queueDelayedAction(new DelayedEndOfCombatTrigger(entry.getControllerId(),
                entry.getCard(), aura.getId(), new SequenceEffect(returns)));
        gameLogService.append(gameData, GameLog.cardThen(enchanted.getCard(),
                " and " + aura.getCard().getName() + " will be returned to their owners' hands at end of combat."));
    }
}
