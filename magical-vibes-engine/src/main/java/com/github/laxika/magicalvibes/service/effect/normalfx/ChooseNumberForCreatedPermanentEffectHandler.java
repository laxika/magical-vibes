package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberForCreatedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link ChooseNumberForCreatedPermanentEffect}: prompts the resolving entry's controller to
 * choose a number for the first matching permanent created earlier in the same resolution. The answer
 * is stored on that permanent and the interrupted resolution then continues with the next effect.
 */
@Component
@RequiredArgsConstructor
public class ChooseNumberForCreatedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseNumberForCreatedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choose = (ChooseNumberForCreatedPermanentEffect) effect;
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId());
        for (UUID permanentId : entry.getCreatedPermanentIds()) {
            Permanent created = gameQueryService.findPermanentById(gameData, permanentId);
            if (created != null && (choose.filter() == null
                    || predicateEvaluationService.matchesPermanentPredicate(created, choose.filter(), filterContext))) {
                playerInputService.beginNumberChoice(gameData, entry.getControllerId(), created.getId(),
                        choose.minNumber(), choose.maxNumber());
                return;
            }
        }
    }
}
