package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesGainLifePerCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "Tap any number of untapped creatures you control. You gain N life for each creature tapped
 * this way." Gathers the controller's untapped creatures and prompts a multi-permanent choice;
 * the tapping and life gain happen when the choice is answered (see
 * {@code MultiPermanentChoiceHandlerService}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TapCreaturesGainLifePerCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapCreaturesGainLifePerCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TapCreaturesGainLifePerCreatureEffect) effect;
        UUID controllerId = entry.getControllerId();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<UUID> untappedCreatureIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (!perm.isTapped() && gameQueryService.isCreature(gameData, perm)) {
                    untappedCreatureIds.add(perm.getId());
                }
            }
        }

        if (untappedCreatureIds.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getCard().getName()
                    + " resolves, but " + gameData.playerIdToName.get(controllerId)
                    + " controls no untapped creatures."));
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, controllerId, untappedCreatureIds,
                untappedCreatureIds.size(),
                new MultiPermanentChoiceContext.TapCreaturesGainLife(e.lifePerCreature()),
                "Tap any number of untapped creatures you control.");
    }
}
