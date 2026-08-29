package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfTopLibraryCardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GainActivatedAbilitiesOfTopLibraryCardSelfEffectHandler implements StaticEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAbilitiesOfTopLibraryCardEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        GameData gameData = context.gameData();
        UUID controllerId = context.sourceControllerId();
        if (controllerId == null) return;
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) return;
        Card topCard = deck.getFirst();
        GainActivatedAbilitiesOfTopLibraryCardEffect gain =
                (GainActivatedAbilitiesOfTopLibraryCardEffect) effect;
        if (!predicateEvaluationService.matchesCardPredicate(topCard, gain.filter(), null)) return;
        for (var ability : topCard.getActivatedAbilities()) {
            accumulator.addActivatedAbility(ability);
        }
    }
}
