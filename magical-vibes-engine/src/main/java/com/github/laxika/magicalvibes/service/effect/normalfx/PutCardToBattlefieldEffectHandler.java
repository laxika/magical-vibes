package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutCardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCardToBattlefieldEffect) effect;

        // For "…and attach this Equipment to it" (Deathrender), the source Equipment is the trigger's card.
        java.util.UUID sourceEquipmentCardId = e.attachSourceEquipment() ? entry.getCard().getId() : null;
        // "With mana value N or less": the bound as the effect resolves (the event's value, or the triggering
        // permanent's mana value as it is now or last existed, CR 608.2h).
        Integer maxManaValue = e.maxManaValue() == null ? null : amountEvaluationService.evaluate(gameData,
                e.maxManaValue(), AmountContext.forStackEntry(entry, null));
        playerInteractionSupport.applyPutCardToBattlefield(gameData, entry.getControllerId(), e, entry.getXValue(),
                maxManaValue, sourceEquipmentCardId, entry.getCard() == null ? null : entry.getCard().getId(),
                null, null, e.enterBlocking() ? (entry.getTriggeringPermanentId() != null
                        ? entry.getTriggeringPermanentId() : entry.getTargetId()) : null,
                ignored -> true, entry.getSourcePermanentId(), entry.getSourcePermanentSnapshot());

    }
}
