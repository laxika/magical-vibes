package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferReturnCreatureFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Tempt with Immortality's sequential opponent reanimation choices. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemptingOfferReturnCreatureFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TemptingOfferReturnCreatureFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var offer = (TemptingOfferReturnCreatureFromGraveyardEffect) effect;
        UUID controllerId = offer.abilityControllerId() != null
                ? offer.abilityControllerId() : entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        List<UUID> opponents = offer.remainingOpponentIds() == null
                ? new ArrayList<>(AnyOpponentMayTakeDamageSacrificeSourceEffectHandler
                        .apnapOpponents(gameData, controllerId))
                : new ArrayList<>(offer.remainingOpponentIds());
        opponents.removeIf(id -> !gameData.playerIds.contains(id) || !hasCreatureCard(gameData, id));
        if (!opponents.isEmpty()) {
            promptNext(gameData, entry.getCard(), new TemptingOfferReturnCreatureFromGraveyardEffect(
                    List.copyOf(opponents), controllerId));
        }
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           TemptingOfferReturnCreatureFromGraveyardEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                "Return a creature card from your graveyard to the battlefield? If you do, "
                        + sourceCard.getName() + "'s controller returns another creature card."));
        log.info("Game {} - offering {} the {} reanimation choice", gameData.id,
                gameData.playerIdToName.get(opponentId), sourceCard.getName());
    }

    public void completeChoice(GameData gameData, PendingMayAbility ability,
                               TemptingOfferReturnCreatureFromGraveyardEffect effect,
                               boolean accepted) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            return;
        }

        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(ability.controllerId());
        remaining.removeIf(id -> !gameData.playerIds.contains(id));

        List<CardEffect> followUps = new ArrayList<>();
        if (accepted) {
            gameData.pendingGraveyardReturnQueue.addFirst(new PendingGraveyardReturnChoice(
                    ability.controllerId(),
                    1,
                    new CardTypePredicate(CardType.CREATURE),
                    GraveyardChoiceDestination.BATTLEFIELD,
                    false,
                    true,
                    false));
            followUps.add(ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.BATTLEFIELD)
                    .filter(new CardTypePredicate(CardType.CREATURE))
                    .mandatory(true)
                    .build());
        }
        if (!remaining.isEmpty()) {
            followUps.add(new TemptingOfferReturnCreatureFromGraveyardEffect(
                    List.copyOf(remaining), effect.abilityControllerId()));
        }
        if (!followUps.isEmpty()) {
            entry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, followUps);
        }
        if (accepted) {
            graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
        }
    }

    private boolean hasCreatureCard(GameData gameData, UUID playerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        return graveyard != null && graveyard.stream()
                .anyMatch(card -> predicateEvaluationService.matchesCardPredicate(
                        card, new CardTypePredicate(CardType.CREATURE), null));
    }
}
