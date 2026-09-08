package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessGetsPoisonCountersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a poison-counter ward cost. */
@Component
@RequiredArgsConstructor
public class CounterUnlessGetsPoisonCountersEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessGetsPoisonCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CounterUnlessGetsPoisonCountersEffect poisonEffect =
                (CounterUnlessGetsPoisonCountersEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        UUID targetControllerId = targetEntry.getControllerId();
        if (!gameQueryService.canPlayerGetPoisonCounters(gameData, targetControllerId)) {
            counterSupport.counterSpell(gameData, entry, targetEntry);
            return;
        }

        Card targetCard = targetEntry.getCard();
        String prompt = "Get " + poisonEffect.amount() + " poison counters to prevent "
                + targetCard.getName() + " from being countered?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetControllerId, List.of(poisonEffect), prompt,
                targetCardId, entry.getControllerId()));
    }
}
