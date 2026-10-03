package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.action.EchoAtNextUpkeep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterEchoAtNextUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    /** Registers an intrinsic echo ability when its permanent enters, without an entry trigger. */
    public void registerOnEntry(GameData gameData, Permanent permanent) {
        if (permanent.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, permanent)) return;
        for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof RegisterEchoAtNextUpkeepEffect echo) {
                gameData.queueDelayedAction(new EchoAtNextUpkeep(permanent.getId(), echo.manaCost(),
                        echo.dynamicManaCost(), echo.handCardCost(), echo.cost(), echo.paidEffects(),
                        permanent.getCard()));
            }
        }
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterEchoAtNextUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null || gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
            return;
        }

        var e = (RegisterEchoAtNextUpkeepEffect) effect;
        gameData.queueDelayedAction(new EchoAtNextUpkeep(
                sourcePermanentId, e.manaCost(), e.dynamicManaCost(), e.handCardCost(), e.cost(), e.paidEffects(),
                entry.getCard()));
    }
}
