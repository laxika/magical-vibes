package com.github.laxika.magicalvibes.service.effect.normalfx;

import java.util.UUID;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves vanishing-style upkeep counter removal and its last-counter sacrifice trigger. */
@Component
@RequiredArgsConstructor
public class RemoveCounterAndSacrificeSelfOnLastEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterAndSacrificeSelfOnLastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterAndSacrificeSelfOnLastEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        int current = source.getCounterCount(e.counterType());
        if (current <= 0) {
            return;
        }

        permanentCounterSupport.removeCounterFromPermanent(gameData, source, e.counterType(), 1);
        if (e.counterType() == CounterType.TIME && gameQueryService.hasKeyword(gameData, source,
                com.github.laxika.magicalvibes.model.Keyword.VANISHING)) return;

        if (current != 1) {
            return;
        }

        // "When the last is removed this way, sacrifice it" (CR 603.12).
        triggeredAbilityStackService.putMade(gameData, TriggeredAbilityStackService.madeAbility(entry.getCard(),
                entry.getControllerId(), entry.getCard().getName() + "'s ability", List.of(new SacrificeSelfEffect()),
                (UUID) null, source.getId()));
    }
}
