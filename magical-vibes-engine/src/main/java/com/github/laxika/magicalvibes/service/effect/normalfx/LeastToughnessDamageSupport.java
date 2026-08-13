package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LeastToughnessDamageSupport {

    private final GameQueryService gameQueryService;
    private final DamageSupport damageSupport;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    public void resolve(GameData gameData, StackEntry entry, int damage) {
        List<Permanent> creatures = collectCreatures(gameData);
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

        if (tied.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.LeastToughnessDamageChoice(entry.getCard(), damage));
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(),
                    tied.stream().map(Permanent::getId).toList(),
                    "Choose a creature with the least toughness to deal damage to ("
                            + entry.getCard().getName() + ").");
            return;
        }

        damageSupport.dealCreatureDamage(gameData, entry, tied.getFirst(), damage);
    }

    public void handleChoice(GameData gameData, UUID permanentId,
                             PermanentChoiceContext.LeastToughnessDamageChoice context) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (entry != null && target != null) {
            damageSupport.dealCreatureDamage(gameData, entry, target, context.damage());
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private List<Permanent> collectCreatures(GameData gameData) {
        List<Permanent> creatures = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)) {
                creatures.add(permanent);
            }
        });
        return creatures;
    }
}
