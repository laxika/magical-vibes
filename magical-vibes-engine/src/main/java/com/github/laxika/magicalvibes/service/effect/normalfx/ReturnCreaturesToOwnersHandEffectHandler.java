package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnCreaturesToOwnersHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnCreaturesToOwnersHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var bounce = (ReturnCreaturesToOwnersHandEffect) effect;
        List<Permanent> creaturesToReturn = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) ->
                creaturesToReturn.addAll(battlefield.stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .filter(p -> gameQueryService.matchesFilters(
                                p,
                                bounce.filters(),
                                FilterContext.of(gameData)
                                        .withSourceCardId(entry.getCard().getId())
                                        .withSourceControllerId(entry.getControllerId())))
                        .toList()));

        for (Permanent creature : creaturesToReturn) {
            permanentRemovalService.removePermanentToHand(gameData, creature);

            String logEntry = creature.getCard().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned to owner's hand by {}", gameData.id, creature.getCard().getName(), entry.getCard().getName());
        }

        if (!creaturesToReturn.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }
}
