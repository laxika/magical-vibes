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
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class EachOtherPlayerDrawsCardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    public EachOtherPlayerDrawsCardEffectHandler(
            PlayerInteractionSupport playerInteractionSupport,
            GameQueryService gameQueryService,
            AmountEvaluationService amountEvaluationService) {
        this.playerInteractionSupport = playerInteractionSupport;
        this.gameQueryService = gameQueryService;
        this.amountEvaluationService = amountEvaluationService;
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

        // The other players draw in APNAP order (CR 101.4), every number fixed before the first draw (CR 608.2h).
        List<UUID> players = SubjectResolutionService.apnapOpponents(gameData, entry.getControllerId()).stream()
                .filter(gameData.playerIds::contains)
                .toList();
        List<Integer> amounts = new ArrayList<>();
        for (UUID playerId : players) {
            AmountContext playerContext = new AmountContext(playerId, source, base.targetPermanentId(),
                    base.xValue(), base.eventValue());
            amounts.add(amountEvaluationService.evaluate(gameData, e.amount(), playerContext));
        }
        for (int i = 0; i < players.size(); i++) {
            playerInteractionSupport.applyDrawCards(gameData, players.get(i), amounts.get(i));
        }
        if (entry.isGiftPromised()
                && entry.getCard().getStaticEffects()
                .stream().anyMatch(GiftEffect.class::isInstance)) {
            ActionEvents.giftGiven(
                    gameData, entry.getControllerId());
        }
    }
}
