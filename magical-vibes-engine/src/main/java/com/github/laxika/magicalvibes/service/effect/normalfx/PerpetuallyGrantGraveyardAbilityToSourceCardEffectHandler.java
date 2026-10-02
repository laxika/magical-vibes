package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantGraveyardAbilityToSourceCardEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Records a graveyard ability grant on the source card identity. */
@Component
public class PerpetuallyGrantGraveyardAbilityToSourceCardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantGraveyardAbilityToSourceCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getCard() == null) {
            return;
        }

        ActivatedAbility ability = ((PerpetuallyGrantGraveyardAbilityToSourceCardEffect) effect).ability();
        gameData.perpetualGraveyardAbilities.compute(entry.getCard().getId(), (ignored, existing) -> {
            List<ActivatedAbility> updated = new ArrayList<>(existing == null ? List.of() : existing);
            if (!updated.contains(ability)) {
                updated.add(ability);
            }
            return List.copyOf(updated);
        });
    }
}
