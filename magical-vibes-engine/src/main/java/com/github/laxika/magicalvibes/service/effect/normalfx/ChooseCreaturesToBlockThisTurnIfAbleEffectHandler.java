package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreaturesToBlockThisTurnIfAbleEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the non-targeted any-number creature choice from Berserker's Frenzy's low branch. */
@Component
@RequiredArgsConstructor
public class ChooseCreaturesToBlockThisTurnIfAbleEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCreaturesToBlockThisTurnIfAbleEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> creatureIds = new ArrayList<>();
        gameData.forEachPermanent((ignoredControllerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)) {
                creatureIds.add(permanent.getId());
            }
        });

        if (creatureIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " controls no creatures to choose to block."));
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, controllerId, creatureIds, creatureIds.size(),
                new MultiPermanentChoiceContext.ChooseCreaturesToBlockThisTurnIfAble(),
                "Choose any number of creatures. They block this combat if able.");
    }
}
