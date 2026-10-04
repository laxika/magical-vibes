package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerDrawsCardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDrawsCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerDrawsCardEffect) effect;

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext base = AmountContext.forStackEntry(entry, source);

        List<UUID> drawingPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = drawingPlayers.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) Collections.rotate(drawingPlayers, -activeIndex);
        for (UUID playerId : drawingPlayers) {
            // Evaluate the amount relative to each drawing player so player-relative amounts
            // (e.g. Nature's Resurgence: "a card for each creature card in their graveyard")
            // count that player's own objects. Player-invariant amounts (fixed, X) are unaffected.
            AmountContext playerContext = new AmountContext(playerId, source, base.targetPermanentId(),
                    base.xValue(), base.eventValue());
            int amount = amountEvaluationService.evaluate(gameData, e.amount(), playerContext);
            playerInteractionSupport.applyDrawCards(gameData, playerId, amount);
        }
    }
}
