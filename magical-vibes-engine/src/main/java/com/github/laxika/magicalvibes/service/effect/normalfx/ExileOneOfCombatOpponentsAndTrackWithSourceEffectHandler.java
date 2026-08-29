package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOneOfCombatOpponentsAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Godsend's non-targeting choice among the equipped creature's combat opponents. */
@Component
@RequiredArgsConstructor
public class ExileOneOfCombatOpponentsAndTrackWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOneOfCombatOpponentsAndTrackWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> combatOpponentIds = collectCombatOpponentIds(gameData, entry.getTriggeringPermanentId());
        if (combatOpponentIds.isEmpty()) {
            return;
        }

        if (combatOpponentIds.size() == 1) {
            Permanent opponent = gameQueryService.findPermanentById(gameData, combatOpponentIds.getFirst());
            if (opponent != null) {
                exileSupport.exilePermanentAndTrackWithSource(
                        gameData, opponent, entry.getSourcePermanentId(), entry.getCard());
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ExileCombatOpponent(entry.getSourcePermanentId(), entry.getCard()));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), combatOpponentIds,
                "Choose one of those creatures to exile.");
    }

    private List<UUID> collectCombatOpponentIds(GameData gameData, UUID triggeringPermanentId) {
        if (triggeringPermanentId == null) {
            return List.of();
        }

        Set<UUID> candidateIds = new LinkedHashSet<>(
                gameData.combatBlockOpponentIdsThisCombat.getOrDefault(triggeringPermanentId, Set.of()));
        Permanent sourceCreature = gameQueryService.findPermanentById(gameData, triggeringPermanentId);
        if (sourceCreature != null) {
            candidateIds.addAll(sourceCreature.getBlockingTargetIds());
        }
        gameData.forEachPermanent((ownerId, permanent) -> {
            if (permanent.getBlockingTargetIds().contains(triggeringPermanentId)) {
                candidateIds.add(permanent.getId());
            }
        });

        List<UUID> legalIds = new ArrayList<>();
        for (UUID candidateId : candidateIds) {
            Permanent candidate = gameQueryService.findPermanentById(gameData, candidateId);
            if (candidate != null && gameQueryService.isCreature(gameData, candidate)) {
                legalIds.add(candidateId);
            }
        }
        return legalIds;
    }
}
