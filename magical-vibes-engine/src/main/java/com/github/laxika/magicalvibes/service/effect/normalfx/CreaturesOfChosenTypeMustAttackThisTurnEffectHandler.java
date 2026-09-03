package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesOfChosenTypeMustAttackThisTurnEffect;
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
 * Resolves {@link CreaturesOfChosenTypeMustAttackThisTurnEffect} by prompting for a creature type
 * and applying the transient must-attack requirement to every matching creature.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreaturesOfChosenTypeMustAttackThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreaturesOfChosenTypeMustAttackThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
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
                    permanent.setMustAttackThisTurn(true);
                    affectedCount[0]++;
                }
            }
        });

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " forces " + affectedCount[0] + " creature(s) of the chosen type to attack this turn if able."));
        log.info("Game {} - {} forces {} creatures of chosen type to attack this turn if able",
                gameData.id, entry.getCard().getName(), affectedCount[0]);
    }
}
