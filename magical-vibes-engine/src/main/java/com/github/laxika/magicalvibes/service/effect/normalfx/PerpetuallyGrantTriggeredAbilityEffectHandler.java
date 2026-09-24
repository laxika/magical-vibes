package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Records a triggered ability on the card identified by the triggering stack entry. */
@Component
public class PerpetuallyGrantTriggeredAbilityEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantTriggeredAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) {
            return;
        }
        var perpetual = (PerpetuallyGrantTriggeredAbilityEffect) effect;
        gameData.perpetualTriggeredAbilities.compute(triggeringCardId, (ignored, existing) -> {
            List<CardEffect> updated = new ArrayList<>(existing == null ? List.of() : existing);
            updated.add(perpetual.grantedEffect());
            return List.copyOf(updated);
        });
    }
}
