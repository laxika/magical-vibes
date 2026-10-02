package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureCardInHandOrCreatureYouControlAndApplyPerpetualPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChooseCreatureCardInHandOrCreatureYouControlAndApplyPerpetualPowerToughnessEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCreatureCardInHandOrCreatureYouControlAndApplyPerpetualPowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (ChooseCreatureCardInHandOrCreatureYouControlAndApplyPerpetualPowerToughnessEffect) effect;
        UUID playerId = entry.getControllerId();
        List<UUID> validCardIds = matchingObjectIds(gameData, playerId);
        if (validCardIds.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PerpetualCreatureCardOrPermanentChoice(
                playerId, validCardIds,
                "Choose a creature card in your hand or a creature you control. It perpetually gets +"
                        + boost.powerBoost() + "/+" + boost.toughnessBoost() + ".",
                boost.powerBoost(), boost.toughnessBoost()));
    }

    public void completeChoice(GameData gameData, UUID chosenCardId,
                               PendingInteraction.PerpetualCreatureCardOrPermanentChoice interaction) {
        UUID playerId = interaction.playerId();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            if (permanent.getCard().getId().equals(chosenCardId)
                    && gameQueryService.isCreature(gameData, permanent)) {
                PerpetualCardPowerToughnessSupport.remember(
                        gameData, permanent.getCard(), interaction.powerBoost(), interaction.toughnessBoost());
                return;
            }
        }

        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        for (Card card : gameData.playerHands.getOrDefault(playerId, List.of())) {
            if (card.getId().equals(chosenCardId)
                    && predicateEvaluationService.matchesCardPredicate(
                    card, creature, card.getId(), gameData, playerId)) {
                PerpetualCardPowerToughnessSupport.remember(
                        gameData, card, interaction.powerBoost(), interaction.toughnessBoost());
                return;
            }
        }
    }

    private List<UUID> matchingObjectIds(GameData gameData, UUID playerId) {
        List<UUID> ids = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                ids.add(permanent.getCard().getId());
            }
        }

        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        for (Card card : gameData.playerHands.getOrDefault(playerId, List.of())) {
            if (predicateEvaluationService.matchesCardPredicate(
                    card, creature, card.getId(), gameData, playerId)) {
                ids.add(card.getId());
            }
        }
        return ids;
    }
}
