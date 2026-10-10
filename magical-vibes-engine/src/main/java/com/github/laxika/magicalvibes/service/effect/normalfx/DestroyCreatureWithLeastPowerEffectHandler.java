package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureWithLeastPowerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves {@link DestroyCreatureWithLeastPowerEffect} by finding the current least effective
 * power and letting the effect controller choose among tied creatures.
 */
@Component
@RequiredArgsConstructor
public class DestroyCreatureWithLeastPowerEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyCreatureWithLeastPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DestroyCreatureWithLeastPowerEffect leastPowerEffect =
                (DestroyCreatureWithLeastPowerEffect) effect;
        List<Permanent> creatures = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)) {
                creatures.add(permanent);
            }
        });

        if (creatures.isEmpty()) {
            return;
        }

        int leastPower = creatures.stream()
                .mapToInt(permanent -> gameQueryService.getEffectivePower(gameData, permanent))
                .min()
                .orElseThrow();
        List<Permanent> tied = creatures.stream()
                .filter(permanent -> gameQueryService.getEffectivePower(gameData, permanent) == leastPower)
                .toList();

        // Ruling (Drop of Honey, 2021): an indestructible creature tied for least power can't be chosen
        // while a destroyable tied creature exists; if every tied creature is indestructible, none is destroyed.
        tied = tied.stream()
                .filter(permanent -> !gameQueryService.hasKeyword(gameData, permanent, Keyword.INDESTRUCTIBLE))
                .toList();
        if (tied.isEmpty()) {
            return;
        }

        if (tied.size() == 1) {
            destructionSupport.tryDestroyAndLog(gameData, tied.getFirst(), entry.getCard().getName(),
                    leastPowerEffect.cannotBeRegenerated());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.DestroyChosenCreature(
                        entry.getControllerId(), entry.getCard().getName(), false,
                        leastPowerEffect.cannotBeRegenerated()));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(),
                tied.stream().map(Permanent::getId).toList(),
                "Choose a creature with the least power to destroy.");
    }
}
