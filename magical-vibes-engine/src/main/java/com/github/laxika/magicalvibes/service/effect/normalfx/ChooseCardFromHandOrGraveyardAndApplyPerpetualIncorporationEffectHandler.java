package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandOrGraveyardAndApplyPerpetualIncorporationEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChooseCardFromHandOrGraveyardAndApplyPerpetualIncorporationEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardFromHandOrGraveyardAndApplyPerpetualIncorporationEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var incorporation = (ChooseCardFromHandOrGraveyardAndApplyPerpetualIncorporationEffect) effect;
        List<Card> choices = new ArrayList<>();
        addMatchingCards(gameData.playerHands.getOrDefault(entry.getControllerId(), List.of()), choices,
                incorporation, gameData, entry.getControllerId());
        addMatchingCards(gameData.playerGraveyards.getOrDefault(entry.getControllerId(), List.of()), choices,
                incorporation, gameData, entry.getControllerId());
        if (choices.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PerpetualHandOrGraveyardCardChoice(
                entry.getControllerId(), choices,
                "Choose an instant or sorcery card in your hand or graveyard. It perpetually incorporates "
                        + incorporation.manaCost() + " and gains a draw ability.",
                incorporation.manaCost(), incorporation.selfCastAbility()));
    }

    private void addMatchingCards(List<Card> source, List<Card> choices,
                                  ChooseCardFromHandOrGraveyardAndApplyPerpetualIncorporationEffect incorporation,
                                  GameData gameData, java.util.UUID controllerId) {
        for (Card card : source) {
            if (predicateEvaluationService.matchesCardPredicate(
                    card, incorporation.cardFilter(), null, gameData, controllerId)) {
                choices.add(card);
            }
        }
    }
}
