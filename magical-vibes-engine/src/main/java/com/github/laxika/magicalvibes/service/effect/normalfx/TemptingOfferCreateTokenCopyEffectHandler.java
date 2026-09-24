package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Tempt with Reflections' sequential opponent copy choices. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemptingOfferCreateTokenCopyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TemptingOfferCreateTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TemptingOfferCreateTokenCopyEffect offer = (TemptingOfferCreateTokenCopyEffect) effect;
        UUID targetId = offer.targetId() != null ? offer.targetId() : targetId(entry, offer);
        UUID controllerId = offer.abilityControllerId() != null
                ? offer.abilityControllerId() : entry.getControllerId();
        if (targetId == null || controllerId == null) {
            return;
        }

        createTokenCopy(gameData, entry, offer, targetId, controllerId);

        List<UUID> opponents = offer.remainingOpponentIds() == null
                ? new ArrayList<>(AnyOpponentMayTakeDamageSacrificeSourceEffectHandler
                        .apnapOpponents(gameData, controllerId))
                : new ArrayList<>(offer.remainingOpponentIds());
        opponents.removeIf(id -> !gameData.playerIds.contains(id));
        if (!opponents.isEmpty()) {
            promptNext(gameData, entry.getCard(), new TemptingOfferCreateTokenCopyEffect(
                    offer.tokenCopyEffect(), List.copyOf(opponents), controllerId, targetId));
        }
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           TemptingOfferCreateTokenCopyEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                "Create a token that's a copy of that creature? If you do, "
                        + sourceCard.getName() + "'s controller creates another one."));
        log.info("Game {} - offering {} the {} token-copy choice", gameData.id,
                gameData.playerIdToName.get(opponentId), sourceCard.getName());
    }

    public void completeChoice(GameData gameData, PendingMayAbility ability,
                               TemptingOfferCreateTokenCopyEffect effect, boolean accepted) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (accepted && entry != null) {
            createTokenCopy(gameData, entry, effect, effect.targetId(), ability.controllerId());
            createTokenCopy(gameData, entry, effect, effect.targetId(), effect.abilityControllerId());
        }

        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(ability.controllerId());
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (!remaining.isEmpty()) {
            promptNext(gameData, ability.sourceCard(), new TemptingOfferCreateTokenCopyEffect(
                    effect.tokenCopyEffect(), List.copyOf(remaining), effect.abilityControllerId(),
                    effect.targetId()));
        }
    }

    private UUID targetId(StackEntry entry, TemptingOfferCreateTokenCopyEffect effect) {
        List<UUID> targetIds = entry.targetsForBoundEffectGroup(effect);
        if (targetIds != null && !targetIds.isEmpty()) {
            return targetIds.getFirst();
        }
        if (entry.getTargetId() != null) {
            return entry.getTargetId();
        }
        return entry.getTargetIds().isEmpty() ? null : entry.getTargetIds().getFirst();
    }

    private void createTokenCopy(GameData gameData, StackEntry entry,
                                 TemptingOfferCreateTokenCopyEffect offer,
                                 UUID targetId, UUID tokenControllerId) {
        if (targetId == null || tokenControllerId == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }
        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        tokenCopySupport.createTokenCopies(gameData, entry, List.of(target.getCard()), sourcePermanent,
                tokenControllerId, offer.tokenCopyEffect());
    }
}
