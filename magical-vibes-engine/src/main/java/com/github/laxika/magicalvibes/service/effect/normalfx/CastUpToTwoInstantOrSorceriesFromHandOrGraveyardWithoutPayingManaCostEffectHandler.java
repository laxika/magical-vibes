package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastUpToTwoInstantOrSorceriesFromHandOrGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Resolves Invoke Calamity's single hand-and-graveyard spell selection. */
@Component
@RequiredArgsConstructor
public class CastUpToTwoInstantOrSorceriesFromHandOrGraveyardWithoutPayingManaCostEffectHandler
        implements NormalEffectHandlerBean {

    public static final int MAX_TOTAL_MANA_VALUE = 6;

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastUpToTwoInstantOrSorceriesFromHandOrGraveyardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> eligible = new ArrayList<>();
        addEligible(eligible, gameData.playerHands.get(controllerId), true);
        addEligible(eligible, gameData.playerGraveyards.get(controllerId), false);
        if (eligible.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.InvokeCalamityCastChoice(
                        controllerId, eligible.stream().map(Card::getId).toList()));
    }

    private static void addEligible(List<Card> eligible, List<Card> cards, boolean fromHand) {
        if (cards == null) {
            return;
        }
        for (Card card : cards) {
            if (isEligible(card) && (!fromHand || !card.isCastOnlyFromGraveyard())) {
                eligible.add(card);
            }
        }
    }

    public static boolean isEligible(Card card) {
        return (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                && largestSpellManaValue(card) <= MAX_TOTAL_MANA_VALUE;
    }

    /**
     * The largest mana value the chosen spell can have once cast. Only the half of a split card
     * being cast counts (CR 709.3a), but these free casts choose the half on resolution, so every
     * half, and the fused spell, must fit within the limit.
     */
    public static int largestSpellManaValue(Card card) {
        if (card.getSplitHalves().size() < 2) {
            return card.getManaValue();
        }
        return card.getEffects(EffectSlot.SPELL).stream()
                .filter(ChooseOneEffect.class::isInstance)
                .map(ChooseOneEffect.class::cast)
                .flatMap(modal -> modal.options().stream())
                .map(ChooseOneEffect.ChooseOneOption::manaCost)
                .filter(Objects::nonNull)
                .mapToInt(cost -> new ManaCost(cost).getManaValue())
                .max()
                .orElse(card.getManaValue());
    }
}
