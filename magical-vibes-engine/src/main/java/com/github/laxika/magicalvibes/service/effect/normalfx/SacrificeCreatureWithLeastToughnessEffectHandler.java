package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureWithLeastToughnessEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SacrificeCreatureWithLeastToughnessEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeCreatureWithLeastToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> creatures = gameData.playerBattlefields
                .getOrDefault(controllerId, List.of())
                .stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent))
                .toList();
        if (creatures.isEmpty()) {
            return;
        }

        int leastToughness = creatures.stream()
                .mapToInt(permanent -> gameQueryService.getEffectiveToughness(gameData, permanent))
                .min()
                .orElseThrow();
        List<Permanent> tied = creatures.stream()
                .filter(permanent -> gameQueryService.getEffectiveToughness(gameData, permanent) == leastToughness)
                .toList();

        if (tied.size() == 1) {
            destructionSupport.sacrificeAndLog(gameData, tied.getFirst(), controllerId);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificeCreature(controllerId));
        playerInputService.beginPermanentChoice(
                gameData,
                controllerId,
                tied.stream().map(Permanent::getId).toList(),
                "Choose a creature with the least toughness to sacrifice.");
    }
}
