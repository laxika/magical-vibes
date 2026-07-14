package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect} (Reprocess): prompts
 * the controller to choose any number of the permanents they control matching the effect's filter
 * to sacrifice. The sacrifice and the follow-up "draw a card for each permanent sacrificed" are
 * completed in {@code MultiPermanentChoiceHandlerService} once the choice is answered.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffectHandler
        implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect) effect;
        UUID controllerId = entry.getControllerId();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<UUID> eligibleIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, perm, e.filter())) {
                    eligibleIds.add(perm.getId());
                }
            }
        }

        if (eligibleIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(controllerId)
                    + " has no permanents to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no permanents to sacrifice for {}",
                    gameData.id, gameData.playerIdToName.get(controllerId), entry.getCard().getName());
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligibleIds, eligibleIds.size(),
                new MultiPermanentChoiceContext.SacrificePermanentsDrawPerSacrificed(),
                "Sacrifice any number of artifacts, creatures, and/or lands. "
                        + "You will draw a card for each permanent sacrificed this way.");
    }
}
