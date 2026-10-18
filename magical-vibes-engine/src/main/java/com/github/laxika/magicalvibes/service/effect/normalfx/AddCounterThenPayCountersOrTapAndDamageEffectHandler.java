package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddCounterThenPayCountersOrTapAndDamageEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves "put a +1/+1 counter on this, then you may pay {X} (X = counters on it); if you don't,
 * tap this and it deals X damage to you" (Primordial Ooze). Places the counter, snapshots X, then
 * delegates to {@link ForcedCostOrElseEffectHandler} for the "you may pay {X}; if you don't, tap
 * self + deal X damage to controller" prompt.
 */
@Component
@RequiredArgsConstructor
public class AddCounterThenPayCountersOrTapAndDamageEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final ForcedCostOrElseEffectHandler forcedCostOrElseEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddCounterThenPayCountersOrTapAndDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AddCounterThenPayCountersOrTapAndDamageEffect) effect;
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int x;
        if (self != null) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, self, e.counterType(), 1);
            x = self.getCounterCount(e.counterType());
        } else if (entry.getSourcePermanentSnapshot() != null) {
            // The Ooze left the battlefield in response: no counter can be added, and X is the number of
            // counters it had as it last existed on the battlefield.
            x = entry.getSourcePermanentSnapshot().getCounterCount(e.counterType());
        } else {
            return;
        }
        // With no counters (the new one was prevented) X is 0, but the controller still chooses whether
        // to pay {0}; declining still taps the creature.
        // "You may pay {X}; if you don't, tap this creature and it deals X damage to you."
        ForcedCostOrElseEffect punish = new ForcedCostOrElseEffect(
                new PayManaCost("{" + Math.max(0, x) + "}"),
                List.of(new TapPermanentsEffect(TapUntapScope.SELF),
                        new DealDamageToPlayersEffect(Math.max(0, x), DamageRecipient.CONTROLLER)),
                true);
        forcedCostOrElseEffectHandler.resolve(gameData, entry, punish);
    }
}
