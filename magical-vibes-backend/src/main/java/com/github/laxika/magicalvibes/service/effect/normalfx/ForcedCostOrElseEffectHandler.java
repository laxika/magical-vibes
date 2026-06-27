package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ForcedCostOrElseEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ForcedCostOrElseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ForcedCostOrElseEffect) effect;
        if (!(e.forcedCost() instanceof SacrificePermanentCost sacrificePermanent)) {
                    log.warn("Game {} - Unsupported forced cost: {}", gameData.id, e.forcedCost().getClass().getSimpleName());
                    destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                    return;
                }

                UUID controllerId = entry.getControllerId();

                List<UUID> matchingPermanentIds = destructionSupport.collectPermanentIds(gameData, controllerId,
                        p -> gameQueryService.matchesPermanentPredicate(gameData, p, sacrificePermanent.filter()));

                if (matchingPermanentIds.isEmpty()) {
                    destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                    return;
                }

                if (matchingPermanentIds.size() == 1) {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, matchingPermanentIds.getFirst());
                    if (permanent != null) {
                        destructionSupport.sacrificeAndLog(gameData, permanent, controllerId);
                    } else {
                        destructionSupport.resolveForcedCostElseEffects(gameData, entry, e);
                    }
                    return;
                }

                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.ForcedCostOrElse(
                                controllerId, entry.getSourcePermanentId(), entry.getCard(), e));
                playerInputService.beginPermanentChoice(gameData, controllerId, matchingPermanentIds,
                        "Choose a permanent to sacrifice (" + sacrificePermanent.description() + ").");
    }
}
