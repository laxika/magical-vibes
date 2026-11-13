package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TrainEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.trigger.CounterAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.CounterWatch;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Training (CR 702.149a): puts a +1/+1 counter on the creature, which then "trains"; each of its "whenever this
 * creature trains" abilities triggers and goes on the stack through the stack door, which makes its choices as it goes
 * there (Savior of Ollenbock's "up to one other target creature or creature card from a graveyard").
 */
@Component
@RequiredArgsConstructor
public class TrainEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameLogService gameLogService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;

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
        UUID controllerId = entry.getControllerId();
        for (TriggeredAbility ability : CounterAbilityReader.printed(card, CounterWatch.ON_SELF_TRAINS)) {
            if (triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, card, source,
                    controllerId, card.getName() + "'s training ability",
                    ConditionContext.forPermanent(source, controllerId),
                    TriggeredAbilityRequest.TriggerRecord.NONE))) {
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
            }
        }
    }
}
