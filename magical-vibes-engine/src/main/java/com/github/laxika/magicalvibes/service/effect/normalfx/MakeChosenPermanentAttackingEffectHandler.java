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

import java.util.List;
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
        Permanent permanent = gameQueryService.findPermanentById(gameData, makeAttacking.permanentId());
        if (permanent == null || !gameQueryService.isCreature(gameData, permanent)) {
            return;
        }

        List<UUID> opponentIds = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .toList();
        if (opponentIds.isEmpty()) {
            return;
        }
        List<UUID> planeswalkerIds = opponentIds.stream()
                .flatMap(playerId -> gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream())
                .filter(candidate -> gameQueryService.isPlaneswalker(gameData, candidate))
                .map(Permanent::getId)
                .toList();

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ChosenPermanentAttackTarget(makeAttacking.permanentId()));
        playerInputService.beginAnyTargetChoice(
                gameData,
                entry.getControllerId(),
                planeswalkerIds,
                opponentIds,
                "Choose the player or planeswalker for the Human to attack.");
    }
}
