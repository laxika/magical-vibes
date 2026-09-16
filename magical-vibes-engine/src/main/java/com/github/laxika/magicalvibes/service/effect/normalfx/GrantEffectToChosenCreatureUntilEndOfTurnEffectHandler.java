package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantingPermanentAwareEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantEffectToChosenCreatureUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantEffectToChosenCreatureUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantEffectToChosenCreatureUntilEndOfTurnEffect grant =
                (GrantEffectToChosenCreatureUntilEndOfTurnEffect) effect;
        UUID chosenCreatureId = entry.getChosenPermanentId();
        if (chosenCreatureId == null) {
            return;
        }

        Permanent chosenCreature = gameQueryService.findPermanentById(gameData, chosenCreatureId);
        if (chosenCreature == null || !gameQueryService.isCreature(gameData, chosenCreature)) {
            return;
        }

        CardEffect grantedEffect = grant.grantedEffect();
        if (grantedEffect instanceof GrantingPermanentAwareEffect aware
                && entry.getSourcePermanentId() != null) {
            grantedEffect = aware.withGrantingPermanentId(entry.getSourcePermanentId());
        }
        chosenCreature.addTemporaryTriggeredEffect(grant.slot(), grantedEffect);
    }
}
