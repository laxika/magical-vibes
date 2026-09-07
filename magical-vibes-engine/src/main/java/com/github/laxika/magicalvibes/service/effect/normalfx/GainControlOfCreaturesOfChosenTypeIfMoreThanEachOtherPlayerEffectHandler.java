package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfCreaturesOfChosenTypeIfMoreThanEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the Peer Pressure control effect after its creature-type choice and count comparison.
 */
@Component
@RequiredArgsConstructor
public class GainControlOfCreaturesOfChosenTypeIfMoreThanEachOtherPlayerEffectHandler
        implements NormalEffectHandlerBean {

    private static final GainControlOfTargetEffect CONTROL_EFFECT =
            new GainControlOfTargetEffect(ControlDuration.PERMANENT);

    private final PlayerInputService playerInputService;
    private final CreatureControlService creatureControlService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfCreaturesOfChosenTypeIfMoreThanEachOtherPlayerEffect.class;
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
        PermanentPredicate creatureTypeFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(chosenSubtype)));

        int controllerCount = countMatching(gameData, controllerId, creatureTypeFilter, filterContext);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId)
                    && controllerCount <= countMatching(gameData, playerId, creatureTypeFilter, filterContext)) {
                return;
            }
        }

        List<Permanent> toSeize = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (!playerId.equals(controllerId)
                    && predicateEvaluationService.matchesPermanentPredicate(
                    permanent, creatureTypeFilter, filterContext)) {
                toSeize.add(permanent);
            }
        });

        for (Permanent permanent : toSeize) {
            creatureControlService.applyControlEffect(gameData, controllerId, permanent,
                    CONTROL_EFFECT, ControlDuration.PERMANENT.toEffectDuration(), null,
                    entry.getCard().getName());
        }
    }

    private int countMatching(GameData gameData, UUID playerId, PermanentPredicate filter,
                              FilterContext filterContext) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return 0;
        }

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(permanent, filter, filterContext)) {
                count++;
            }
        }
        return count;
    }
}
