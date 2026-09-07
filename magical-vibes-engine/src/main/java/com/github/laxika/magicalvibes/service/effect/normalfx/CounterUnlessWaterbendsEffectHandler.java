package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessWaterbendsEffect;
import com.github.laxika.magicalvibes.service.effect.WaterbendPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CounterUnlessWaterbendsEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final WaterbendPaymentService waterbendPaymentService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessWaterbendsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CounterUnlessWaterbendsEffect waterbend = (CounterUnlessWaterbendsEffect) effect;
        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, entry.getTargetId(), entry);
        if (targetEntry == null) {
            return;
        }

        if (!waterbendPaymentService.canPay(gameData, targetEntry.getControllerId(), waterbend.amount())) {
            counterSupport.counterSpell(gameData, entry, targetEntry);
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                targetEntry.getControllerId(),
                List.of(waterbend),
                "Pay {" + waterbend.amount() + "} using waterbend to prevent "
                        + targetEntry.getCard().getName() + " from being countered?",
                targetEntry.getCard().getId(),
                entry.getControllerId()
        ));
    }
}
