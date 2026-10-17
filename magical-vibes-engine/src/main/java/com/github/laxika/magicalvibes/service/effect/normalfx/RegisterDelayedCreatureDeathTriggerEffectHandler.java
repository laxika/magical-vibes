package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Registers a turn-scoped trigger for each creature that dies. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedCreatureDeathTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedCreatureDeathTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RegisterDelayedCreatureDeathTriggerEffect delayed = (RegisterDelayedCreatureDeathTriggerEffect) effect;
        gameData.creatureDeathTriggerWatchers.add(
                new CreatureDeathTriggerWatcher(entry.getControllerId(), entry.getCard(),
                        boundToSourceObject(delayed.effect(), entry),
                        delayed.onlyOthers() ? entry.getSourcePermanentId() : null));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": whenever a creature dies this turn, its delayed ability triggers."));
        log.info("Game {} - {} registers a creature-death trigger for the turn",
                gameData.id, entry.getCard().getName());
    }

    /**
     * A delayed ability's "other than this creature" refers to the object that created it, not to any
     * later object with the same card: when the source returns as a new object it is not excluded.
     */
    private static CardEffect boundToSourceObject(CardEffect effect, StackEntry entry) {
        if (entry.getSourcePermanentId() != null
                && effect instanceof BoostAllCreaturesEffect boost
                && boost.filter() instanceof PermanentNotPredicate not
                && not.predicate() instanceof PermanentIsSourceCardPredicate) {
            return new BoostAllCreaturesEffect(boost.powerBoost(), boost.toughnessBoost(),
                    new PermanentNotPredicate(new PermanentIsSpecificPermanentPredicate(entry.getSourcePermanentId())),
                    boost.scope(), boost.duration());
        }
        return effect;
    }
}
