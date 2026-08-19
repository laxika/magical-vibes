package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link BoostAllCreaturesOfChosenSubtypeEffect} by prompting for a creature type and
 * applying the resulting one-shot modifier to every matching creature on every battlefield.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BoostAllCreaturesOfChosenSubtypeEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostAllCreaturesOfChosenSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostAllCreaturesOfChosenSubtypeEffect) effect;
        UUID controllerId = entry.getControllerId();

        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, controllerId);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosenSubtype = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId);
        PermanentHasSubtypePredicate subtypePredicate = new PermanentHasSubtypePredicate(chosenSubtype);
        int[] affectedCount = {0};

        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && predicateEvaluationService.matchesPermanentPredicate(
                        permanent, subtypePredicate, filterContext)) {
                    permanent.setPowerModifier(permanent.getPowerModifier() + boost.powerBoost());
                    permanent.setToughnessModifier(permanent.getToughnessModifier() + boost.toughnessBoost());
                    affectedCount[0]++;
                }
            }
        });

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(String.format(" gives %+d/%+d to %d creature(s) until end of turn.",
                        boost.powerBoost(), boost.toughnessBoost(), affectedCount[0]))
                .build());
        log.info("Game {} - {} gives {}/{} to {} creatures of chosen subtype",
                gameData.id, entry.getCard().getName(), boost.powerBoost(), boost.toughnessBoost(), affectedCount[0]);
    }
}
