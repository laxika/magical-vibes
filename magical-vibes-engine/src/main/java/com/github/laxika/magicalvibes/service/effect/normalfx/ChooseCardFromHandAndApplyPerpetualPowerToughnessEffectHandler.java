package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChooseCardFromHandAndApplyPerpetualPowerToughnessEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect) effect;
        List<Card> hand = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of());
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(
                    hand.get(i), e.cardFilter(), null, gameData, entry.getControllerId())) {
                validIndices.add(i);
            }
        }
        if (validIndices.isEmpty()) {
            return;
        }

        List<String> changes = new ArrayList<>();
        if (e.manaCost() != null) {
            changes.add("incorporates " + e.manaCost());
        }
        if (e.power() != 0 || e.toughness() != 0) {
            changes.add("gets +" + e.power() + "/+" + e.toughness());
        }
        for (Keyword keyword : e.keywords()) {
            changes.add("gains " + keyword.name().toLowerCase().replace('_', ' '));
        }
        if (e.selfCastAbility() != null) {
            changes.add("gains an ability that triggers when you cast it");
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PerpetualPowerToughnessChoice(
                entry.getControllerId(), validIndices,
                "Choose a " + CardPredicateUtils.describeFilter(e.cardFilter()) + " in your hand. It perpetually "
                        + String.join(" and ", changes) + ".",
                e.power(), e.toughness(), e.keywords(), 0, 0, e.manaCost(), e.selfCastAbility()));
    }
}
