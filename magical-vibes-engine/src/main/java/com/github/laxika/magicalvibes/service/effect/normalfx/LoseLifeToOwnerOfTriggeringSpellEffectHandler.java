package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeToOwnerOfTriggeringSpellEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves life loss by the owner of the spell that caused the trigger. */
@Component
@RequiredArgsConstructor
public class LoseLifeToOwnerOfTriggeringSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeToOwnerOfTriggeringSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LoseLifeToOwnerOfTriggeringSpellEffect lifeLoss =
                (LoseLifeToOwnerOfTriggeringSpellEffect) effect;
        Card triggeringSpell = gameQueryService.findCardById(gameData, entry.getTriggeringCardId());
        if (triggeringSpell == null || triggeringSpell.getOwnerId() == null) {
            return;
        }

        UUID ownerId = triggeringSpell.getOwnerId();
        lifeSupport.applyLifeLoss(gameData, ownerId, lifeLoss.amount(), entry.getCard().getName());
    }
}
