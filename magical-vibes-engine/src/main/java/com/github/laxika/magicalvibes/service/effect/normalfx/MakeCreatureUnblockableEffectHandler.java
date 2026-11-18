package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MakeCreatureUnblockableEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeCreatureUnblockableEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var unblockable = (MakeCreatureUnblockableEffect) effect;

        if (unblockable.triggeringPermanent()) {
            Permanent triggering = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
            if (unblockable.notBlockableByOpponentsOfController()) {
                makeUnblockableByOpponentsOf(gameData, triggering, entry);
            } else {
                makeUnblockable(gameData, triggering, entry, unblockable);
            }
            return;
        }

        // Multi-target: make each target in the group unblockable (e.g. Open into Wonder's
        // "X target creatures can't be blocked this turn").
        if (entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()) {
            for (UUID targetId : entry.getTargetIds()) {
                makeUnblockable(gameData, gameQueryService.findPermanentById(gameData, targetId), entry, unblockable);
            }
            return;
        }

        // Self-targeting triggers (e.g. Repartee) use sourcePermanentId; targetId may carry
        // separate context about the spell or player that caused the trigger.
        UUID targetId;
        if (unblockable.attachedPermanent()) {
            targetId = entry.getTargetId();
        } else if (unblockable.selfTargeting() && entry.getSourcePermanentId() != null) {
            targetId = entry.getSourcePermanentId();
        } else {
            targetId = entry.getTargetId();
        }
        makeUnblockable(gameData, gameQueryService.findPermanentById(gameData, targetId), entry, unblockable);
    }

    private void makeUnblockable(GameData gameData, Permanent target, StackEntry entry,
                                 MakeCreatureUnblockableEffect effect) {
        if (target == null) return;
        if (Duration.UNTIL_END_OF_TURN.equals(effect.duration())) {
            makeUnblockable(gameData, entry, target);
            return;
        }
        grantCantBeBlocked(gameData, target, entry, effect.duration());
    }

    /**
     * {@code target} gains "this creature can't be blocked" for {@code duration} (Ogre Marauder): an ability (layer 6, CR 613.1f), which a
     * "loses all abilities" with a later timestamp removes (CR 613.7), unlike "it can't be blocked this turn".
     */
    public void grantCantBeBlocked(GameData gameData, Permanent target, StackEntry entry, Duration duration) {
        if (target == null) return;
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.TARGET),
                target.getId(), null, null, duration, 0));
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                Duration.UNTIL_END_OF_COMBAT.equals(duration)
                        ? " can't be blocked this combat." : " can't be blocked."));
    }

    /**
     * The target can't be blocked by creatures controlled by the opponents of the resolving ability's
     * controller this turn. A creature that controller controls is blocked only by those opponents'
     * creatures, so it simply can't be blocked.
     */
    private void makeUnblockableByOpponentsOf(GameData gameData, Permanent target, StackEntry entry) {
        if (target == null) return;
        UUID abilityControllerId = entry.getControllerId();
        if (abilityControllerId.equals(gameQueryService.findPermanentController(gameData, target.getId()))) {
            makeUnblockable(gameData, entry, target);
            return;
        }
        List<PermanentPredicate> opponentControlled = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(abilityControllerId))
                .<PermanentPredicate>map(PermanentControlledByPlayerPredicate::new)
                .toList();
        if (opponentControlled.isEmpty()) return;
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), null, abilityControllerId,
                new GrantEffectEffect(new CantBeBlockedByCreaturesMatchingPredicateEffect(
                        new PermanentAnyOfPredicate(opponentControlled)), GrantScope.TARGET),
                target.getId(), null, null, Duration.UNTIL_END_OF_TURN, 0));
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                " can't be blocked by creatures its opponents control this turn."));
    }

    private void makeUnblockable(GameData gameData, StackEntry entry, Permanent target) {
        makeUnblockableThisTurn(gameData, target, entry.getCard() == null ? null : entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId());
    }

    /** "{@code target} can't be blocked this turn" (CR 611.2a), made by {@code sourceCardName}'s ability. */
    public void makeUnblockableThisTurn(GameData gameData, Permanent target, String sourceCardName,
                                        UUID sourcePermanentId, UUID controllerId) {
        if (target == null) {
            return;
        }

        gameData.addFloatingEffect(FloatingContinuousEffect.onPermanent(sourceCardName, sourcePermanentId, controllerId,
                new CantBeBlockedEffect(), target.getId(), Duration.UNTIL_END_OF_TURN));

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " can't be blocked this turn."));

        log.info("Game {} - {} can't be blocked this turn", gameData.id, target.getCard().getName());
    }
}
