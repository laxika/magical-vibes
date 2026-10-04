package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeToOwnerOfTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves life loss by the owner of the permanent that caused the trigger. */
@Component
@RequiredArgsConstructor
public class LoseLifeToOwnerOfTriggeringPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeToOwnerOfTriggeringPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        Card triggeringCard = permanent == null
                ? entry.lastKnownPermanentCard(entry.getTriggeringPermanentId()) : permanent.getCard();
        if (triggeringCard == null) {
            triggeringCard = entry.getTriggeringCardSnapshot();
        }
        UUID ownerId = entry.getTriggeringPermanentOwnerId();
        if (ownerId == null && permanent != null) {
            ownerId = permanent.getOriginalCard().getOwnerId();
        }
        if (ownerId == null && triggeringCard != null) {
            ownerId = triggeringCard.getOwnerId();
        }
        if (ownerId == null) {
            return;
        }
        int manaValue = triggeringCard == null ? entry.getEventValue() : triggeringCard.getManaValue();
        lifeSupport.applyLifeLoss(gameData, ownerId, manaValue, entry.getCard().getName());
    }
}
