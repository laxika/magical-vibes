package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastAllCardsExiledWithSourceWithoutPayingEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Lets the controller choose which matching cards exiled with the source to cast for free now. */
@Component
@RequiredArgsConstructor
public class CastAllCardsExiledWithSourceWithoutPayingEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastAllCardsExiledWithSourceWithoutPayingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return;

        var cast = (CastAllCardsExiledWithSourceWithoutPayingEffect) effect;
        List<UUID> castableIds = gameData.getCardsExiledByPermanent(sourcePermanentId).stream()
                .filter(card -> cast.filter() == null
                        || predicateEvaluationService.matchesCardPredicate(card, cast.filter(), null))
                .map(Card::getId)
                .filter(id -> gameData.findExiledCard(id) != null)
                .toList();
        if (castableIds.isEmpty()) return;

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ImprovisationCapstoneCastChoice(
                entry.getControllerId(), castableIds, castableIds.size()));
    }
}
