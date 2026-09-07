package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.NoteControllerLifeTotalEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Applies the life-total note while the source permanent enters the battlefield. */
@Component
@RequiredArgsConstructor
public class NoteControllerLifeTotalEffectHandler {

    public void applyIfPresent(GameData gameData, UUID controllerId, Permanent enteringPermanent) {
        boolean applies = enteringPermanent.getCard().getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(NoteControllerLifeTotalEffect.class::isInstance);
        if (applies) {
            enteringPermanent.setChosenNumber(gameData.getLife(controllerId));
        }
    }
}
