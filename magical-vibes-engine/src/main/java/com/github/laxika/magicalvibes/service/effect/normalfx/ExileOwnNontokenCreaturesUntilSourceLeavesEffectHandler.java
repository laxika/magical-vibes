package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnNontokenCreaturesUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the controller's optional non-targeting creature exile choice. */
@Component
@RequiredArgsConstructor
public class ExileOwnNontokenCreaturesUntilSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOwnNontokenCreaturesUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null
                || gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
            return;
        }

        List<UUID> eligibleIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (permanent.getId().equals(sourcePermanentId)
                        || permanent.getCard().isToken()
                        || !gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                eligibleIds.add(permanent.getId());
            }
        }

        if (!eligibleIds.isEmpty()) {
            playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligibleIds, eligibleIds.size(),
                    new MultiPermanentChoiceContext.ExileOwnNontokenCreaturesUntilSourceLeaves(sourcePermanentId),
                    "You may exile any number of other nontoken creatures you control.");
        }
    }
}
