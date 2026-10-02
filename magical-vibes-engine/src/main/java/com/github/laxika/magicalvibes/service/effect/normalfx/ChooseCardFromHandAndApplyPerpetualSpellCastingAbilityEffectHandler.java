package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualSpellCastingAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Begins a hand-card choice for a perpetual valued spell-casting ability. */
@Component
@RequiredArgsConstructor
public class ChooseCardFromHandAndApplyPerpetualSpellCastingAbilityEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardFromHandAndApplyPerpetualSpellCastingAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choice = (ChooseCardFromHandAndApplyPerpetualSpellCastingAbilityEffect) effect;
        List<Card> hand = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of());
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(
                    hand.get(i), choice.cardFilter(), null, gameData, entry.getControllerId())) {
                validIndices.add(i);
            }
        }
        if (validIndices.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PerpetualPowerToughnessChoice(
                entry.getControllerId(), validIndices,
                "Choose a card in your hand. It perpetually gains "
                + choice.grantedAbility().name().toLowerCase().replace('_', ' ')
                        + " " + choice.abilityValue() + ".",
                new GrantSpellCastingAbilityToSpellsEffect(
                        choice.grantedAbility(), choice.abilityValue(), new CardTruePredicate())));
    }
}
