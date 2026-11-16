package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureCardFromTargetOwnerGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResolveEffectOnTargetDeathThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ResolveEffectOnTargetDeathThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ResolveEffectOnTargetDeathThisTurnEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            log.info("Game {} - Target permanent no longer on battlefield, delayed death trigger not registered", gameData.id);
            return;
        }

        if (e.onlyIfDealtDamageBySource()) {
            UUID damageSourceId = entry.getSourcePermanentId() != null
                    ? entry.getSourcePermanentId() : entry.getCard().getId();
            if (gameData.damageDealtToPermanentsBySourceThisTurn
                    .getOrDefault(target.getId(), java.util.Map.of()).getOrDefault(damageSourceId, 0) <= 0) {
                return;
            }
        }

        CardEffect delayedEffect = e.effect();
        if (delayedEffect instanceof ReturnCreatureCardFromTargetOwnerGraveyardEffect returnEffect) {
            UUID ownerId = target.getCard().getOwnerId();
            if (ownerId == null) {
                ownerId = gameQueryService.findPermanentController(gameData, target.getId());
            }
            delayedEffect = returnEffect.bindOwner(ownerId);
        }

        // "When that creature dies this turn, …": about the creature, with this turn as its stated duration
        // (CR 603.7b) and its source this ability's (CR 603.7e). An "if" after the trigger condition is checked as it
        // triggers and as it resolves (CR 603.4).
        TriggerOptions options = e.interveningIf() == null ? TriggerOptions.DEFAULT
                : TriggerOptions.DEFAULT.withInterveningIf(e.interveningIf());
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.dies(ObjectRelation.THIS), options, delayedEffect),
                target.getId(), entry.getSourcePermanentId(), true));

        log.info("Game {} - Delayed trigger registered: if {} dies this turn, {} triggers",
                gameData.id, target.getCard().getName(), entry.getCard().getName());
    }
}
