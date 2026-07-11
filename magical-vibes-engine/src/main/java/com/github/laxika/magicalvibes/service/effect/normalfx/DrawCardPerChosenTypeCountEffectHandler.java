package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardPerChosenTypeCountEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DrawCardPerChosenTypeCountEffect} (Distant Melody): prompts the controller to
 * choose a creature type, then draws a card for each permanent the controller controls of that type
 * (Changeling-aware).
 *
 * <p>Two-phase: the first resolution begins the creature-type choice and pauses (re-running once the
 * choice completes via {@code rerunCurrentEffectAfterInteraction}); the re-entry reads and clears
 * {@code GameData.chosenSpellSubtype}, counts, and draws.</p>
 */
@Component
@RequiredArgsConstructor
public class DrawCardPerChosenTypeCountEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardPerChosenTypeCountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        if (gameData.chosenSpellSubtype == null) {
            // First pass: choose a creature type, then re-run this effect once the choice completes.
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, controllerId);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosen = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(controllerId);
        PermanentHasSubtypePredicate predicate = new PermanentHasSubtypePredicate(chosen);
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        int count = 0;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, predicate, filterContext)) {
                    count++;
                }
            }
        }

        playerInteractionSupport.applyDrawCards(gameData, controllerId, count);
    }
}
