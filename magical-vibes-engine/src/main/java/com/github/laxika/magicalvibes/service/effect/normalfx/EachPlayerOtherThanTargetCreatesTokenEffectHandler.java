package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerOtherThanTargetCreatesTokenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachPlayerOtherThanTargetCreatesTokenEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerOtherThanTargetCreatesTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerOtherThanTargetCreatesTokenEffect) effect;
        UUID excludedPlayerId = entry.getTargetId();
        if (excludedPlayerId == null || !gameData.playerIds.contains(excludedPlayerId)) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext base = AmountContext.forStackEntry(entry, source);

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(excludedPlayerId)) {
                continue;
            }
            AmountContext playerContext = new AmountContext(playerId, source, base.targetPermanentId(),
                    base.xValue(), base.eventValue());
            int amount = amountEvaluationService.evaluate(gameData, e.token().amount(), playerContext);
            if (amount <= 0) {
                continue;
            }
            if (e.token().subtypes().contains(CardSubtype.CLUE)) {
                triggerCollectionService.checkInvestigateTriggers(gameData, playerId);
            }
            int power = amountEvaluationService.evaluate(gameData, e.token().power(), playerContext);
            int toughness = amountEvaluationService.evaluate(gameData, e.token().toughness(), playerContext);
            List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                    gameData, playerId, e.token(), amount, entry.getCard().getSetCode(), power, toughness);
            entry.getCreatedPermanentIds().addAll(createdIds);
        }
    }
}
