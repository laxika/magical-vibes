package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantGraveyardAbilityToTargetCardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Records a graveyard ability grant on the targeted card identity. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantGraveyardAbilityToTargetCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantGraveyardAbilityToTargetCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var perpetual = (PerpetuallyGrantGraveyardAbilityToTargetCardEffect) effect;
        UUID targetCardId = perpetual.triggeringCard() ? entry.getTriggeringCardId()
                : !entry.getTargetCardIds().isEmpty() ? entry.getTargetCardIds().getFirst()
                : entry.getTargetId();
        if (targetCardId == null || gameQueryService.findCardInGraveyardById(gameData, targetCardId) == null) {
            return;
        }
        // The trigger event's card is the one it became in the graveyard; once it has left and returned it is a new
        // object (CR 400.7).
        if (perpetual.triggeringCard() && entry.getTriggeringCardGraveyardEntryVersion() > 0
                && gameData.graveyardEntryVersion(targetCardId) != entry.getTriggeringCardGraveyardEntryVersion()) {
            return;
        }

        gameData.perpetualGraveyardAbilities.compute(targetCardId, (ignored, existing) -> {
            java.util.List<ActivatedAbility> updated =
                    new java.util.ArrayList<>(existing == null ? java.util.List.of() : existing);
            updated.add(perpetual.ability());
            return java.util.List.copyOf(updated);
        });
    }
}
