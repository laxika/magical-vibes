package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EachOtherPlayerDrawsCardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final TriggerCollectionService triggerCollectionService;

    public EachOtherPlayerDrawsCardEffectHandler(
            PlayerInteractionSupport playerInteractionSupport,
            GameQueryService gameQueryService,
            AmountEvaluationService amountEvaluationService,
            @Lazy TriggerCollectionService triggerCollectionService) {
        this.playerInteractionSupport = playerInteractionSupport;
        this.gameQueryService = gameQueryService;
        this.amountEvaluationService = amountEvaluationService;
        this.triggerCollectionService = triggerCollectionService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOtherPlayerDrawsCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachOtherPlayerDrawsCardEffect) effect;

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext base = AmountContext.forStackEntry(entry, source);

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(entry.getControllerId())) {
                continue;
            }
            AmountContext playerContext = new AmountContext(playerId, source, base.targetPermanentId(),
                    base.xValue(), base.eventValue());
            int amount = amountEvaluationService.evaluate(gameData, e.amount(), playerContext);
            playerInteractionSupport.applyDrawCards(gameData, playerId, amount);
        }
        if (entry.isGiftPromised()
                && entry.getCard().getEffects(com.github.laxika.magicalvibes.model.EffectSlot.STATIC)
                .stream().anyMatch(GiftEffect.class::isInstance)) {
            triggerCollectionService.checkControllerGivesGiftTriggers(
                    gameData, entry.getControllerId());
        }
    }
}
