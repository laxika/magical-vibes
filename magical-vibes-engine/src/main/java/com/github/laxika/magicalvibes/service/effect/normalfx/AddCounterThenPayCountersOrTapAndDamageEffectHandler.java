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
        if (self == null) {
            return;
        }

        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, self, e.counterType(), 1);

        int x = self.getCounterCount(e.counterType());
        if (x <= 0) {
            // Paying {0} would succeed trivially, so there is no penalty either — nothing to do.
            return;
        }

        // "You may pay {X}; if you don't, tap this creature and it deals X damage to you."
        ForcedCostOrElseEffect punish = new ForcedCostOrElseEffect(
                new PayManaCost("{" + x + "}"),
                List.of(new TapPermanentsEffect(TapUntapScope.SELF),
                        new DealDamageToPlayersEffect(x, DamageRecipient.CONTROLLER)),
                true);
        forcedCostOrElseEffectHandler.resolve(gameData, entry, punish);
    }
}
