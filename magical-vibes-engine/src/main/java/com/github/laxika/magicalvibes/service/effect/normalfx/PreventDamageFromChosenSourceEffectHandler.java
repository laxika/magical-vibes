package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PreventDamageFromChosenSourceEffect}: collects the legal source choices
 * (optionally restricted by the effect's source filter), then starts a permanent choice whose
 * context installs the scope-appropriate prevention shield once the source is picked (see
 * {@code PermanentChoiceBattlefieldHandlerService}).
 */
@Component
@RequiredArgsConstructor
public class PreventDamageFromChosenSourceEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventDamageFromChosenSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PreventDamageFromChosenSourceEffect) effect;
        UUID controllerId = entry.getControllerId();

        List<UUID> validIds = collectValidSourceIds(gameData, e.sourceFilter());
        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
            return;
        }

        String label = e.sourceLabel() == null ? "" : e.sourceLabel() + " ";
        PermanentChoiceContext context;
        String prompt;
        switch (e.scope()) {
            case NEXT_DAMAGE_TO_CONTROLLER -> {
                context = new PermanentChoiceContext.PreventNextDamageFromSourceChoice(controllerId, e.gainLife());
                prompt = "Choose a " + label
                        + "source. The next time it would deal damage to you this turn, prevent that damage"
                        + (e.gainLife() ? " and gain that much life." : ".");
            }
            case NEXT_DAMAGE_TO_ANY_TARGET -> {
                context = new PermanentChoiceContext.PreventNextDamageFromSourceToAnyTargetChoice(controllerId);
                prompt = "Choose a source. The next time it would deal damage to any target this turn, prevent that damage.";
            }
            case ALL_DAMAGE_THIS_TURN -> {
                context = new PermanentChoiceContext.PreventDamageSourceChoice(controllerId, e.controllerOnly());
                prompt = e.controllerOnly()
                        ? "Choose a source. Prevent all damage it would deal to you this turn."
                        : "Choose a " + label + "source. Prevent all damage it would deal this turn.";
            }
            default -> throw new IllegalStateException("Unknown chosen-source prevention scope: " + e.scope());
        }

        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds, prompt);
    }

    private List<UUID> collectValidSourceIds(GameData gameData, PermanentPredicate sourceFilter) {
        if (sourceFilter == null) {
            return preventionSupport.collectAllBattlefieldPermanentIds(gameData);
        }
        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, perm) -> {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, perm, sourceFilter)) {
                validIds.add(perm.getId());
            }
        });
        return validIds;
    }
}
