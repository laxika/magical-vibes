package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingAbilityActivation;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

/**
 * Answers the "exile a card from your graveyard as an activation cost" choice with a
 * highest-mana-value heuristic. For abilities that also require paying the selected card's mana
 * cost, the heuristic is restricted to cards payable from the mana already floating for the
 * activation.
 */
@Slf4j
class GraveyardExileCostChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.GraveyardExileCostChoice> {

    @Override
    public Class<PendingInteraction.GraveyardExileCostChoice> handledType() {
        return PendingInteraction.GraveyardExileCostChoice.class;
    }

    @Override
    public void answer(PendingInteraction.GraveyardExileCostChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<Integer> validIndices = interaction.validIndices();
        if (validIndices == null || validIndices.isEmpty()) {
            return;
        }

        final List<Card> gy = ctx.gameData().playerGraveyards.getOrDefault(ctx.aiPlayerId(), List.of());
        List<Integer> candidateIndices = affordableIndices(validIndices, gy, ctx);
        int bestIndex = candidateIndices.stream()
                .max(Comparator.comparingInt(i -> i < gy.size() ? gy.get(i).getManaValue() : 0))
                .orElse(validIndices.iterator().next());

        log.info("AI: Choosing graveyard card at index {} in game {}", bestIndex, ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.GraveyardCardChosen(bestIndex));
    }

    private List<Integer> affordableIndices(
            List<Integer> validIndices, List<Card> graveyard, AiInteractionContext ctx) {
        ExileCardFromGraveyardCost cost = findPayExiledCardManaCost(ctx);
        ManaPool manaPool = ctx.gameData().playerManaPools.get(ctx.aiPlayerId());
        if (cost == null || manaPool == null) {
            return validIndices;
        }

        PendingAbilityActivation pending = ctx.gameData().pendingAbilityActivation;
        Permanent source = ctx.gameQueryService().findPermanentById(ctx.gameData(), pending.sourcePermanentId());
        int additionalGenericCost = ctx.gameActions().getActivatedAbilityAdditionalGenericCost(
                ctx.gameData(), source, pending.abilityIndex(), pending.targetId(), pending.targetIds());
        List<Integer> affordable = validIndices.stream()
                .filter(index -> index >= 0 && index < graveyard.size())
                .filter(index -> isAffordable(graveyard.get(index), manaPool, additionalGenericCost))
                .toList();
        return affordable.isEmpty() ? validIndices : affordable;
    }

    private ExileCardFromGraveyardCost findPayExiledCardManaCost(AiInteractionContext ctx) {
        PendingAbilityActivation pending = ctx.gameData().pendingAbilityActivation;
        if (pending == null) {
            return null;
        }

        Permanent source = ctx.gameQueryService().findPermanentById(ctx.gameData(), pending.sourcePermanentId());
        if (source == null) {
            return null;
        }
        List<ActivatedAbility> abilities = ctx.gameActions().getEffectiveActivatedAbilities(ctx.gameData(), source);
        if (pending.abilityIndex() < 0 || pending.abilityIndex() >= abilities.size()) {
            return null;
        }
        return abilities.get(pending.abilityIndex()).getEffects().stream()
                .filter(ExileCardFromGraveyardCost.class::isInstance)
                .map(ExileCardFromGraveyardCost.class::cast)
                .filter(ExileCardFromGraveyardCost::payExiledCardManaCost)
                .findFirst()
                .orElse(null);
    }

    private boolean isAffordable(Card card, ManaPool manaPool, int additionalGenericCost) {
        return card.getManaCost() == null
                || new ManaCost(card.getManaCost()).canPay(manaPool, additionalGenericCost);
    }
}
