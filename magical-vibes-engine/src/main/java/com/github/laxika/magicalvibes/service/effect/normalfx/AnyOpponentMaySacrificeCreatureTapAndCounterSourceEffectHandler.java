package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffect} (Desecration Demon).
 * Each opponent in turn order that controls a creature is offered the sacrifice; the source is
 * tapped and gets a single +1/+1 counter once every opponent has chosen, if at least one sacrificed.
 * Accept/decline lives in {@code mayfx/AnyOpponentMaySacrificeCreatureTapAndCounterSourceHandler}
 * and the "which creature" pick in {@code PermanentChoiceBattlefieldHandlerService}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final MaySacrificeForCounterSupport maySacrificeForCounterSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> opponents = new ArrayList<>(
                AnyOpponentMayTakeDamageSacrificeSourceEffectHandler.apnapOpponents(gameData, controllerId));
        opponents.removeIf(id -> creatureIds(gameData, id).isEmpty());
        if (opponents.isEmpty()) {
            return;
        }

        promptNext(gameData, entry.getCard(), new AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffect(
                List.copyOf(opponents), controllerId, entry.getSourcePermanentId(), false));
    }

    /** The creatures {@code playerId} controls, i.e. what they could sacrifice. */
    public List<UUID> creatureIds(GameData gameData, UUID playerId) {
        return maySacrificeForCounterSupport.matchingPermanentIds(gameData, playerId, new PermanentIsCreaturePredicate());
    }

    /**
     * Enqueues the may prompt for the first remaining opponent. Callers must pass an effect whose
     * {@code remainingOpponentIds} is non-empty.
     */
    public void promptNext(GameData gameData, Card sourceCard,
            AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        String prompt = "Sacrifice a creature? If you do, " + sourceCard.getName()
                + " becomes tapped and gets a +1/+1 counter.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                prompt,
                effect.abilityControllerId(),
                null,
                effect.sourcePermanentId()));
        log.info("Game {} - offering {} the {} sacrifice choice", gameData.id,
                gameData.playerIdToName.get(opponentId), sourceCard.getName());
    }

    /** Sacrifices {@code permanentId} for {@code sacrificingPlayerId}. */
    public void sacrifice(GameData gameData, UUID sacrificingPlayerId, UUID permanentId) {
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (toSacrifice == null) {
            return;
        }
        destructionSupport.sacrificeAndLog(gameData, toSacrifice, sacrificingPlayerId);
    }

    /**
     * Moves on to the next opponent that still controls a creature, or finishes the resolution once
     * nobody is left: if anyone sacrificed, the source is tapped and gets one +1/+1 counter.
     */
    public void advance(GameData gameData, Card sourceCard,
            AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffect effect, UUID chooserId, boolean anyAccepted) {
        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(chooserId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        remaining.removeIf(id -> creatureIds(gameData, id).isEmpty());

        if (!remaining.isEmpty()) {
            promptNext(gameData, sourceCard, new AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffect(
                    List.copyOf(remaining), effect.abilityControllerId(), effect.sourcePermanentId(), anyAccepted));
            return;
        }

        if (anyAccepted) {
            tapAndCounterSource(gameData, effect.sourcePermanentId());
        }
    }

    /** "Tap this creature and put a +1/+1 counter on it" — once, however many creatures were sacrificed. */
    private void tapAndCounterSource(GameData gameData, UUID sourcePermanentId) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }
        if (!source.isTapped()) {
            source.tap();
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " is tapped."));
        }
        permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, null, source, 1);
    }
}
