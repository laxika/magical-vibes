package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesBoostSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TapCreaturesBoostSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapCreaturesBoostSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> untappedCreatureIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (!permanent.isTapped() && gameQueryService.isCreature(gameData, permanent)) {
                    untappedCreatureIds.add(permanent.getId());
                }
            }
        }

        if (untappedCreatureIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.builder()
                    .card(entry.getCard())
                    .text(" resolves, but " + gameData.playerIdToName.get(controllerId)
                            + " controls no untapped creatures.")
                    .build());
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                controllerId,
                untappedCreatureIds,
                untappedCreatureIds.size(),
                new MultiPermanentChoiceContext.TapCreaturesBoostSelf(entry.getSourcePermanentId()),
                "You may tap any number of untapped creatures you control.");
    }
}
