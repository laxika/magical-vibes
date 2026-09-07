package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BecomeDayAsEntersEffect;
import com.github.laxika.magicalvibes.service.turn.DayNightService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BecomeDayAsEntersEffectHandler {

    private final DayNightService dayNightService;

    public void applyDayboundEntryFace(GameData gameData, Permanent enteringPermanent) {
        dayNightService.applyDayboundEntryRules(gameData, enteringPermanent);
    }

    public void applyIfPresent(GameData gameData, Permanent enteringPermanent) {
        boolean applies = enteringPermanent.getCard().getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(BecomeDayAsEntersEffect.class::isInstance);
        if (applies && gameData.dayNight == DayNight.NEITHER) {
            dayNightService.becomeDay(gameData);
        }
    }
}
