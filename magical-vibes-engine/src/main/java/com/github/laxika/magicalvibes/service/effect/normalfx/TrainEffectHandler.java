package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BattlefieldAndGraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TrainEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GraveyardTargetingService graveyardTargetingService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TrainEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        int placed = permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, source, CounterType.PLUS_ONE_PLUS_ONE, 1);
        if (placed <= 0) {
            return;
        }

        Card card = source.getCard();
        List<CardEffect> trainTriggers = card.getEffects(EffectSlot.ON_SELF_TRAINS);
        if (trainTriggers.isEmpty()) {
            return;
        }

        BattlefieldAndGraveyardCardChoosingEffect mixedZoneTrigger = trainTriggers.stream()
                .filter(BattlefieldAndGraveyardCardChoosingEffect.class::isInstance)
                .map(BattlefieldAndGraveyardCardChoosingEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (mixedZoneTrigger != null) {
            graveyardTargetingService.handleBattlefieldAndGraveyardExileTargeting(
                    gameData, entry.getControllerId(), card, trainTriggers, source.getId(),
                    mixedZoneTrigger, card.getName() + "'s training ability");
            return;
        }

        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                entry.getControllerId(),
                card.getName() + "'s training ability",
                new ArrayList<>(trainTriggers),
                0,
                null,
                source.getId(),
                Map.of(),
                null,
                List.of(),
                List.of()
        ));
    }
}
