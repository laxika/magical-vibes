package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects triggers caused by lore counters being put on controlled Sagas. */
@Service
public class SagaCounterTriggerCollectorService {

    private final GameLogService gameLogService;

    public SagaCounterTriggerCollectorService(GameLogService gameLogService) {
        this.gameLogService = gameLogService;
    }

    @CollectsTrigger(value = PutCounterOnTargetPermanentEffect.class,
            slot = EffectSlot.ON_YOU_PUT_LORE_COUNTERS_ON_SAGA)
    private boolean handleLoreCounterPlaced(TriggerMatchContext match,
                                            PutCounterOnTargetPermanentEffect effect,
                                            TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.LoreCounterPlaced loreCounterPlaced)
                || match.permanent() == null || match.controllerId() == null) {
            return false;
        }

        Permanent source = match.permanent();
        Card sourceCard = source.getCard();
        PermanentPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSpecificPermanentPredicate(source.getId()))
        ));
        TargetFilter targetFilter = new PermanentPredicateTargetFilter(
                targetPredicate, "Target must be another creature");

        match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                sourceCard,
                match.controllerId(),
                new ArrayList<>(List.of(effect)),
                false,
                targetFilter,
                0,
                source.getId(),
                new Permanent(source),
                true,
                loreCounterPlaced.saga().getId(),
                null,
                match.controllerId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        return true;
    }
}
