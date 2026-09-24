package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapSourceThenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Gitrog, Horror of Zhava's multi-opponent sacrifice choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AnyOpponentMaySacrificeCreatureSupport anyOpponentMaySacrificeCreatureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffect.class;
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

        promptNext(gameData, entry.getCard(), new AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffect(
                ((AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffect) effect).landSearch(),
                List.copyOf(opponents), controllerId, entry.getSourcePermanentId(), false));
    }

    /** The nontoken creatures {@code playerId} controls. */
    public List<UUID> creatureIds(GameData gameData, UUID playerId) {
        return anyOpponentMaySacrificeCreatureSupport.creatureIds(gameData, playerId).stream()
                .filter(id -> {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                    return permanent != null && !permanent.getCard().isToken();
                })
                .toList();
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        String prompt = "Sacrifice a nontoken creature? If you do, " + sourceCard.getName()
                + " becomes tapped, then you seek a land card onto the battlefield tapped.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                prompt,
                effect.abilityControllerId(),
                null,
                effect.sourcePermanentId()));
        log.info("Game {} - offering {} the {} nontoken creature sacrifice choice", gameData.id,
                gameData.playerIdToName.get(opponentId), sourceCard.getName());
    }

    public void sacrifice(GameData gameData, UUID sacrificingPlayerId, UUID permanentId) {
        anyOpponentMaySacrificeCreatureSupport.sacrifice(gameData, sacrificingPlayerId, permanentId);
    }

    public void advance(GameData gameData, Card sourceCard,
                        AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffect effect,
                        UUID chooserId, boolean anyAccepted) {
        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(chooserId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        remaining.removeIf(id -> creatureIds(gameData, id).isEmpty());

        if (!remaining.isEmpty()) {
            promptNext(gameData, sourceCard, new AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffect(
                    effect.landSearch(), List.copyOf(remaining), effect.abilityControllerId(),
                    effect.sourcePermanentId(), anyAccepted));
            return;
        }

        if (anyAccepted) {
            StackEntry followUp = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    effect.abilityControllerId(),
                    sourceCard.getName() + "'s ability",
                    List.of(new TapSourceThenEffect(effect.landSearch())),
                    null,
                    effect.sourcePermanentId());
            gameData.stack.add(followUp);
        }
    }
}
