package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenPerChosenTypeCountEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves a chosen-type token count by prompting for a creature type, counting matching creatures
 * controlled by the effect controller, and delegating token creation to the normal token handler.
 */
@Component
@RequiredArgsConstructor
public class CreateTokenPerChosenTypeCountEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;
    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenPerChosenTypeCountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenPerChosenTypeCountEffect chosenTypeEffect =
                (CreateTokenPerChosenTypeCountEffect) effect;
        UUID controllerId = entry.getControllerId();

        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, controllerId);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosenSubtype = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(controllerId);
        PermanentHasSubtypePredicate subtypePredicate = new PermanentHasSubtypePredicate(chosenSubtype);
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        int count = 0;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && predicateEvaluationService.matchesPermanentPredicate(
                        permanent, subtypePredicate, filterContext)) {
                    count++;
                }
            }
        }

        if (count > 0) {
            createTokenEffectHandler.resolveForController(
                    gameData, entry, chosenTypeEffect.tokenEffect().withAmount(count), controllerId);
        }
    }
}
