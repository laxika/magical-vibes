package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedDamagedCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageThisTurnBySourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Creates the delayed triggered ability "whenever a creature [you control] dealt damage this way (or by that creature)
 * dies this turn" (CR 603.7a), with this turn as its stated duration (CR 603.7b): the damage source is the resolving
 * spell, or the creature it targets, and the dying creature is judged as it last existed (CR 603.10a).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedDamagedCreatureDeathTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedDamagedCreatureDeathTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RegisterDelayedDamagedCreatureDeathTriggerEffect delayed =
                (RegisterDelayedDamagedCreatureDeathTriggerEffect) effect;
        UUID damageSourceId = delayed.damagedByTarget() ? entry.getTargetId() : entry.getCard().getId();
        if (damageSourceId == null) {
            return;
        }
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.thisTurn(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.creatureDies(new PermanentDealtDamageThisTurnBySourcePredicate(
                        damageSourceId), delayed.allControllers() ? ObjectRelation.ANY : ObjectRelation.YOU_CONTROL),
                        delayed.effect()),
                entry.getSourcePermanentId()));
        String watchedScope = delayed.allControllers() ? "a creature" : "a creature you control";
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": whenever " + watchedScope
                        + (delayed.damagedByTarget() ? " dealt damage by that creature" : " dealt damage this way")
                        + " dies this turn, its delayed ability triggers."));
        log.info("Game {} - {} registers a damaged-creature death trigger for the turn",
                gameData.id, entry.getCard().getName());
    }
}
