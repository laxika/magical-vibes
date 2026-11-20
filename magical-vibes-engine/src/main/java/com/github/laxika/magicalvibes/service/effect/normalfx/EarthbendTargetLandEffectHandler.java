package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.BendingType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ReturnEarthbendedLandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves the Earthbend keyword action for a targeted land. */
@Component
@RequiredArgsConstructor
public class EarthbendTargetLandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AnimationSupport animationSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EarthbendTargetLandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        applyEarthbend(gameData, entry, (EarthbendTargetLandEffect) effect);
    }

    public boolean applyEarthbend(GameData gameData, StackEntry entry, EarthbendTargetLandEffect earthbend) {
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int counterCount = amountEvaluationService.evaluate(gameData, earthbend.counterCount(),
                AmountContext.forStackEntry(entry, source));
        List<UUID> targetIds = entry.targetsForEffect(earthbend);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        AnimatePermanentsEffect animation = new AnimatePermanentsEffect(
                0, 0, List.of(), Set.of(Keyword.HASTE), null, Set.of(CardType.LAND),
                Subjects.target(), Duration.PERMANENT);
        boolean applied = false;
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isLand(gameData, target)) {
                continue;
            }

            animationSupport.animatePermanently(gameData, target, animation, 0, 0,
                    entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId());
            permanentCounterSupport.applyPlusOnePlusOneCounters(
                    gameData, entry, target, counterCount);
            applied = true;

            // "When that land dies or is put into exile, return it to the battlefield tapped under your control"
            // (CR 701.66a): about the land, with no stated duration (CR 603.7b); it can go to only one of the two
            // zones, so each is its own registration, with the return from that zone.
            UUID returnControllerId = entry.getControllerId();
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(returnControllerId,
                    entry.getCard(), new TriggeredAbility(Triggers.dies(ObjectRelation.THIS),
                            new ReturnEarthbendedLandEffect(returnControllerId, false)),
                    target.getId(), entry.getSourcePermanentId(), false));
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(returnControllerId,
                    entry.getCard(), new TriggeredAbility(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null,
                            ObjectRelation.THIS), new ReturnEarthbendedLandEffect(returnControllerId, true)),
                    target.getId(), entry.getSourcePermanentId(), false));
        }
        if (applied) {
            ActionEvents.bent(gameData, entry.getControllerId(), BendingType.EARTHBEND);
        }
        return applied;
    }
}
