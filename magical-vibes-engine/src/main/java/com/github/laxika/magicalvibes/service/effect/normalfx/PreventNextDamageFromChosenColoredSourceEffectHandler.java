package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageFromChosenColoredSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreventNextDamageFromChosenColoredSourceEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventNextDamageFromChosenColoredSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PreventNextDamageFromChosenColoredSourceEffect) effect;
        UUID controllerId = entry.getControllerId();
        CardColor color = e.color();

        List<UUID> validIds = collectValidSourceIds(gameData, color);
        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.PreventNextDamageFromColoredSourceChoice(controllerId, color));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                "Choose a " + color.name().toLowerCase() + " source. The next time it would deal damage to you this turn, prevent that damage.");
    }

    private List<UUID> collectValidSourceIds(GameData gameData, CardColor color) {
        PermanentColorInPredicate colorPredicate = new PermanentColorInPredicate(Set.of(color));
        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, perm) -> {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, perm, colorPredicate)) {
                validIds.add(perm.getId());
            }
        });
        return validIds;
    }
}
