package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreserveOneLoyaltyCounterForChosenPlaneswalkerTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.LoyaltyDamageReplacementHandler;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PreserveOneLoyaltyCounterForChosenPlaneswalkerTypeHandler
        implements LoyaltyDamageReplacementHandler {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreserveOneLoyaltyCounterForChosenPlaneswalkerTypeEffect.class;
    }

    @Override
    public int apply(GameData gameData, Permanent source, Permanent target, int damage) {
        if (damage <= 0 || !target.getCard().hasType(CardType.PLANESWALKER)) return damage;

        CardSubtype chosenSubtype = source.getChosenSubtype();
        if (chosenSubtype == null) return damage;

        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, source.getId());
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (sourceControllerId == null || !Objects.equals(sourceControllerId, targetControllerId)) return damage;

        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null || battlefield.stream().noneMatch(permanent -> gameQueryService.isCreature(gameData, permanent))) {
            return damage;
        }
        if (!predicateEvaluationService.matchesPermanentPredicate(
                gameData, target, new PermanentHasSubtypePredicate(chosenSubtype))) {
            return damage;
        }

        int loyalty = target.getCounterCount(CounterType.LOYALTY);
        return damage >= loyalty ? Math.max(0, loyalty - 1) : damage;
    }
}
