package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeChosenPermanentAttackingEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MakeChosenPermanentAttackingEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeChosenPermanentAttackingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MakeChosenPermanentAttackingEffect makeAttacking = (MakeChosenPermanentAttackingEffect) effect;
        UUID permanentId = makeAttacking.permanentId() == null
                ? entry.getSourcePermanentId() : makeAttacking.permanentId();
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent == null || !gameQueryService.isCreature(gameData, permanent)) {
            return;
        }

        UUID requiredAttackingPlayerId = makeAttacking.onlyTargetsAttackedByTriggeringPlayer()
                ? entry.getTargetId() : null;
        List<UUID> opponentIds = requiredAttackingPlayerId == null
                ? gameData.orderedPlayerIds.stream()
                        .filter(playerId -> !playerId.equals(entry.getControllerId()))
                        .toList()
                : List.of();
        Set<UUID> attackTargetIds = requiredAttackingPlayerId == null
                ? Set.of()
                : currentAttackTargetIds(gameData, requiredAttackingPlayerId);
        if (requiredAttackingPlayerId != null && attackTargetIds.isEmpty()) {
            return;
        }
        List<UUID> validPlayerIds = requiredAttackingPlayerId == null
                ? opponentIds
                : attackTargetIds.stream().filter(gameData.playerIds::contains).toList();
        List<UUID> planeswalkerIds = requiredAttackingPlayerId == null
                ? opponentIds.stream()
                        .flatMap(playerId -> gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream())
                        .filter(candidate -> gameQueryService.isPlaneswalker(gameData, candidate))
                        .map(Permanent::getId)
                        .toList()
                : attackTargetIds.stream()
                        .map(id -> gameQueryService.findPermanentById(gameData, id))
                        .filter(candidate -> candidate != null && gameQueryService.isPlaneswalker(gameData, candidate))
                        .map(Permanent::getId)
                        .toList();

        List<UUID> permanentTargetIds = new java.util.ArrayList<>(planeswalkerIds);
        gameData.forEachPermanent((playerId, candidate) -> {
            if (gameQueryService.isBattle(gameData, candidate)
                    && !entry.getControllerId().equals(candidate.getProtectorPlayerId())
                    && (requiredAttackingPlayerId == null || attackTargetIds.contains(candidate.getId()))) {
                permanentTargetIds.add(candidate.getId());
            }
        });

        if (validPlayerIds.size() + permanentTargetIds.size() == 1) {
            permanent.enterAttacking(true);
            permanent.setAttackedOrBlockedSinceLastUpkeep(true);
            permanent.setAttackTarget(validPlayerIds.isEmpty() ? permanentTargetIds.getFirst() : validPlayerIds.getFirst());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ChosenPermanentAttackTarget(
                        permanent.getId(), requiredAttackingPlayerId));
        playerInputService.beginAnyTargetChoice(
                gameData,
                entry.getControllerId(),
                permanentTargetIds,
                validPlayerIds,
                "Choose the player, planeswalker, or battle for " + permanent.getCard().getName() + " to attack.");
    }

    private Set<UUID> currentAttackTargetIds(GameData gameData, UUID attackingPlayerId) {
        Set<UUID> attackTargetIds = new LinkedHashSet<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(attackingPlayerId, List.of())) {
            if (permanent.isAttacking() && permanent.getAttackTarget() != null) {
                attackTargetIds.add(permanent.getAttackTarget());
            }
        }
        return attackTargetIds;
    }
}
