package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageFromChosenSourceEffect;
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
public class PreventAllDamageFromChosenSourceEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventAllDamageFromChosenSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PreventAllDamageFromChosenSourceEffect) effect;
        UUID controllerId = entry.getControllerId();
        Set<CardColor> colorFilter = e.colorFilter();

        List<UUID> validIds = collectValidSourceIds(gameData, colorFilter);

        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.PreventDamageSourceChoice(controllerId, e.controllerOnly(), colorFilter));
        String prompt = e.controllerOnly()
                ? "Choose a source. Prevent all damage it would deal to you this turn."
                : colorFilter.isEmpty()
                ? "Choose a source. Prevent all damage it would deal this turn."
                : "Choose a red source. Prevent all damage it would deal this turn.";
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds, prompt);
    }

    private List<UUID> collectValidSourceIds(GameData gameData, Set<CardColor> colorFilter) {
        if (colorFilter == null || colorFilter.isEmpty()) {
            return preventionSupport.collectAllBattlefieldPermanentIds(gameData);
        }

        PermanentColorInPredicate colorPredicate = new PermanentColorInPredicate(colorFilter);
        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, perm) -> {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, perm, colorPredicate)) {
                validIds.add(perm.getId());
            }
        });
        return validIds;
    }
}
