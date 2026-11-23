package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
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
public class SacrificeCreatureWithLeastToughnessEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

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
                .toList();
        if (creatures.isEmpty()) {
            return;
        }

        // The least toughness is found among every creature you control; a creature with it that can't be sacrificed
        // is not sacrificed, and no creature with more toughness is sacrificed instead (CR 101.3).
        int leastToughness = creatures.stream()
                .mapToInt(permanent -> gameQueryService.getEffectiveToughness(gameData, permanent))
                .min()
                .orElseThrow();
        List<Permanent> tied = creatures.stream()
                .filter(permanent -> gameQueryService.getEffectiveToughness(gameData, permanent) == leastToughness)
                .filter(permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent))
                .filter(permanent -> !gameQueryService.cantBeChosen(gameData, permanent))
                .toList();

        if (tied.isEmpty()) {
            return;
        }
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
