package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureToCreateTokensEqualToToughnessEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeCreatureToCreateTokensEqualToToughnessEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeCreatureToCreateTokensEqualToToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeCreatureToCreateTokensEqualToToughnessEffect) effect;

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p)
                        && predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter())) {
                    validIds.add(p.getId());
                }
            }
        }

        if (validIds.isEmpty()) {
            String logEntry = playerName + " has no creature to sacrifice for " + entry.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no creature to sacrifice for {}",
                    gameData.id, playerName, entry.getCard().getName());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificeCreatureCreateTokensEqualToToughness(
                        controllerId, entry.getCard(), e.tokenTemplate()));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                entry.getCard().getName() + " — Choose a creature to sacrifice.");

        String logEntry = playerName + " is choosing a creature to sacrifice for " + entry.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} choosing a creature to sacrifice for {}",
                gameData.id, playerName, entry.getCard().getName());
    
    }
}
