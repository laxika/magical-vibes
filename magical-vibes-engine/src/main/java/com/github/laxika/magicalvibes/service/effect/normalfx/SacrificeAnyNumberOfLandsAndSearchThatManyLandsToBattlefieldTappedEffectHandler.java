package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfLandsAndSearchThatManyLandsToBattlefieldTappedEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SacrificeAnyNumberOfLandsAndSearchThatManyLandsToBattlefieldTappedEffect}
 * (Scapeshift): prompts the controller to choose any number of the lands they control to
 * sacrifice. The sacrifice and the follow-up "search for that many land cards" are completed in
 * {@code MultiPermanentChoiceHandlerService} once the choice is answered.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeAnyNumberOfLandsAndSearchThatManyLandsToBattlefieldTappedEffectHandler
        implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeAnyNumberOfLandsAndSearchThatManyLandsToBattlefieldTappedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<UUID> landIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (perm.getCard().hasType(CardType.LAND)) {
                    landIds.add(perm.getId());
                }
            }
        }

        if (landIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(controllerId) + " controls no lands to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no lands to sacrifice for {}",
                    gameData.id, gameData.playerIdToName.get(controllerId), entry.getCard().getName());
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, controllerId, landIds, landIds.size(),
                new MultiPermanentChoiceContext.SacrificeLandsSearchLandsToBattlefieldTapped(),
                "Sacrifice any number of lands. You will search your library for that many land cards.");
    }
}
