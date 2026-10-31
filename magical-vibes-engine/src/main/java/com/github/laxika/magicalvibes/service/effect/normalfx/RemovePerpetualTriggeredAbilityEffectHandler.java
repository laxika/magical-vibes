package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemovePerpetualTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Removes a previously granted triggered ability from the source card and permanent. */
@Component
@RequiredArgsConstructor
public class RemovePerpetualTriggeredAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemovePerpetualTriggeredAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var removal = (RemovePerpetualTriggeredAbilityEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID cardId = source != null && source.getOriginalCard() != null
                ? source.getOriginalCard().getId() : entry.getCard().getId();

        gameData.removePerpetualAbilitiesIf(cardId, removal.triggeredAbilitySlot(),
                removal.triggeredAbilityType()::isInstance);

        if (source != null) {
            source.removePersistentTriggeredEffectsIf(removal.triggeredAbilitySlot(),
                    removal.triggeredAbilityType()::isInstance);
        }
    }
}
