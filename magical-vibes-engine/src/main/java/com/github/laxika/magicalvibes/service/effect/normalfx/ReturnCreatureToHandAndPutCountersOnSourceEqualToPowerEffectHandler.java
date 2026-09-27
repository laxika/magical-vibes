package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureToHandAndPutCountersOnSourceEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnCreatureToHandAndPutCountersOnSourceEqualToPowerEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnCreatureToHandAndPutCountersOnSourceEqualToPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnCreatureToHandAndPutCountersOnSourceEqualToPowerEffect) effect;
        UUID controllerId = entry.getControllerId();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(entry.getSourcePermanentId());

        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), filterContext)) {
                    validIds.add(permanent.getId());
                }
            }
        }

        if (!validIds.isEmpty()) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ReturnCreatureToHandAndPutCountersOnSourceEqualToPower(
                            controllerId, entry.getCard(), entry.getSourcePermanentId()));
            playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                    entry.getCard().getName() + " — Choose " + e.permanentDescription()
                            + " to return to its owner's hand.");
        }
    }
}
