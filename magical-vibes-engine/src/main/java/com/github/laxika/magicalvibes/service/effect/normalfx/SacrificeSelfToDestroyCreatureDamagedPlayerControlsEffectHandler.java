package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID defenderId = entry.getTargetId();
                UUID sourcePermanentId = entry.getSourcePermanentId();
                UUID controllerId = entry.getControllerId();

                if (defenderId == null || sourcePermanentId == null) {
                    return;
                }

                // Check source creature is still on the battlefield
                Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                if (source == null) {
                    String logEntry = entry.getCard().getName() + "'s ability fizzles — source no longer on the battlefield.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                    return;
                }

                // Collect valid creature targets from damaged player's battlefield
                List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
                List<UUID> validCreatureIds = new ArrayList<>();
                if (defenderBattlefield != null) {
                    for (Permanent perm : defenderBattlefield) {
                        if (gameQueryService.isCreature(gameData, perm)) {
                            validCreatureIds.add(perm.getId());
                        }
                    }
                }

                if (validCreatureIds.isEmpty()) {
                    String logEntry = entry.getCard().getName() + "'s ability resolves, but "
                            + gameData.playerIdToName.get(defenderId) + " has no creatures.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                    return;
                }

                // Present multi-permanent choice with max 1 to select destruction target
                playerInputService.beginMultiPermanentChoice(gameData, controllerId, validCreatureIds, 1,
                        new MultiPermanentChoiceContext.SacrificeSelfToDestroy(sourcePermanentId),
                        entry.getCard().getName() + "'s ability — Choose a creature "
                                + gameData.playerIdToName.get(defenderId) + " controls to destroy.");
    }
}
