package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenCastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/** Resolves Wishing Well's counter placement and reflexive graveyard-cast trigger. */
@Component
@RequiredArgsConstructor
public class PutCounterOnSelfThenCastTargetInstantOrSorceryFromGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnSelfThenCastTargetInstantOrSorceryFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCounterOnSelfThenCastTargetInstantOrSorceryFromGraveyardEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId() != null
                ? entry.getSourcePermanentId()
                : entry.getTargetId();
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }

        int before = source.getCounterCount(e.counterType());
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, source, e.counterType(), 1);
        int requiredManaValue = source.getCounterCount(e.counterType());
        if (requiredManaValue <= before) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) {
            return;
        }

        List<Card> matchingCards = graveyard.stream()
                .filter(card -> card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                .filter(card -> card.getManaValue() == requiredManaValue)
                .toList();
        if (matchingCards.isEmpty()) {
            return;
        }

        CardEffect castEffect = new CastTargetInstantOrSorceryFromGraveyardEffect(
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                true,
                e.exileInsteadOfGraveyard());
        if (matchingCards.size() == 1) {
            putReflexiveTriggerOnStack(gameData, entry, castEffect, matchingCards.getFirst().getId(), source);
            return;
        }

        List<Integer> validIndices = IntStream.range(0, matchingCards.size()).boxed().toList();
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(controllerId, validIndices, GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        "Choose an instant or sorcery card with mana value " + requiredManaValue
                                + " from your graveyard to target.")
                .cardPool(new ArrayList<>(matchingCards))
                .mayAbilityContext(entry.getCard(), controllerId, List.of(castEffect), sourcePermanentId)
                .build());
    }

    private void putReflexiveTriggerOnStack(GameData gameData, StackEntry entry, CardEffect castEffect,
                                             UUID targetCardId, Permanent source) {
        StackEntry reflexiveTrigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getCard().getName() + "'s ability",
                List.of(castEffect),
                0,
                targetCardId,
                entry.getSourcePermanentId(),
                null,
                Zone.GRAVEYARD,
                null,
                null);
        reflexiveTrigger.setSourcePermanentSnapshot(new Permanent(source));
        gameData.stack.add(reflexiveTrigger);
    }
}
