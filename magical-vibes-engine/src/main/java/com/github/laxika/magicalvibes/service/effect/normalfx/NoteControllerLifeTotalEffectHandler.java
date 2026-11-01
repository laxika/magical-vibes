package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.NoteControllerLifeTotalEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Applies the life-total note while the source permanent enters the battlefield. */
@Component
@RequiredArgsConstructor
public class NoteControllerLifeTotalEffectHandler {

    public void applyIfPresent(GameData gameData, UUID controllerId, Permanent enteringPermanent) {
        boolean applies = enteringPermanent.getCard().getStaticEffects().stream()
                .anyMatch(NoteControllerLifeTotalEffect.class::isInstance);
        if (applies) {
            enteringPermanent.setChosenNumber(gameData.getLife(controllerId));
        }
    }
}
