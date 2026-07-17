package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageFromChosenSourceMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreventNextDamageFromChosenSourceMatchingEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventNextDamageFromChosenSourceMatchingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PreventNextDamageFromChosenSourceMatchingEffect) effect;
        UUID controllerId = entry.getControllerId();

        List<UUID> validIds = collectValidSourceIds(gameData, e.sourceFilter());
        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
            return;
        }

        // Post-choice behaviour is identical to any-source prevention with no life gain:
        // the chosen permanent gets a one-shot next-damage shield. Only the legal choices differ.
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.PreventNextDamageFromSourceChoice(controllerId, false));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                "Choose a " + e.sourceLabel() + " source. The next time it would deal damage to you this turn, prevent that damage.");
    }

    private List<UUID> collectValidSourceIds(GameData gameData, PermanentPredicate sourceFilter) {
        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, perm) -> {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, perm, sourceFilter)) {
                validIds.add(perm.getId());
            }
        });
        return validIds;
    }
}
