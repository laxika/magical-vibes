package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapOtherCreaturesForUnblockableEffect;
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
public class TapOtherCreaturesForUnblockableEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapOtherCreaturesForUnblockableEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var tapEffect = (TapOtherCreaturesForUnblockableEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        List<UUID> eligibleIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (!permanent.isTapped()
                        && !permanent.getId().equals(sourcePermanentId)
                        && gameQueryService.isCreature(gameData, permanent)) {
                    eligibleIds.add(permanent.getId());
                }
            }
        }

        if (eligibleIds.size() < tapEffect.creatureCount()) {
            gameLogService.append(gameData, GameLog.builder()
                    .card(entry.getCard())
                    .text(" resolves, but there are not enough other untapped creatures to tap.")
                    .build());
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                controllerId,
                eligibleIds,
                tapEffect.creatureCount(),
                new MultiPermanentChoiceContext.TapOtherCreaturesForUnblockable(
                        sourcePermanentId, tapEffect.creatureCount()),
                "You may tap " + tapEffect.creatureCount() + " other untapped creatures you control.");
    }
}
