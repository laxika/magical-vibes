package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Clackbridge Troll's APNAP sacrifice trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AnyOpponentMaySacrificeCreatureSupport anyOpponentMaySacrificeCreatureSupport;
    private final LifeSupport lifeSupport;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffect.class;
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

        promptNext(gameData, entry.getCard(), new AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffect(
                List.copyOf(opponents), controllerId, entry.getSourcePermanentId(), false));
    }

    public List<UUID> creatureIds(GameData gameData, UUID playerId) {
        return anyOpponentMaySacrificeCreatureSupport.creatureIds(gameData, playerId);
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        String prompt = "Sacrifice a creature? If you do, " + sourceCard.getName()
                + " becomes tapped, you gain 3 life, and you draw a card.";
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

    public void sacrifice(GameData gameData, UUID sacrificingPlayerId, UUID permanentId) {
        anyOpponentMaySacrificeCreatureSupport.sacrifice(gameData, sacrificingPlayerId, permanentId);
    }

    public void advance(GameData gameData, Card sourceCard,
                        AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffect effect,
                        UUID chooserId, boolean anyAccepted) {
        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(chooserId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        remaining.removeIf(id -> creatureIds(gameData, id).isEmpty());

        if (!remaining.isEmpty()) {
            promptNext(gameData, sourceCard, new AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffect(
                    List.copyOf(remaining), effect.abilityControllerId(), effect.sourcePermanentId(), anyAccepted));
            return;
        }

        if (anyAccepted) {
            tapAndRewardSource(gameData, sourceCard, effect.abilityControllerId(), effect.sourcePermanentId());
        }
    }

    private void tapAndRewardSource(GameData gameData, Card sourceCard, UUID controllerId,
                                     UUID sourcePermanentId) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source != null && !source.isTapped()) {
            source.tap();
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " is tapped."));
        }
        lifeSupport.applyGainLife(gameData, controllerId, 3, sourceCard.getName());
        playerInteractionSupport.applyDrawCards(gameData, controllerId, 1);
    }
}
