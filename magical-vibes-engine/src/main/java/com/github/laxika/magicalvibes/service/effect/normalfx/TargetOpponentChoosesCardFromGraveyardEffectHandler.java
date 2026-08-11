package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentChoosesCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetOpponentChoosesCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetOpponentChoosesCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetOpponentChoosesCardFromGraveyardEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.getTargetId();
        List<Card> matchingCards = matchingCards(gameData, opponentId, e.filter(), entry.getCard().getId());

        UUID chosenCardId = gameData.graveyardTargetOperation.scroungeChosenCardId;
        gameData.graveyardTargetOperation.scroungeChosenCardId = null;
        if (chosenCardId != null) {
            gameData.rerunCurrentEffectAfterInteraction = false;
            matchingCards.stream()
                    .filter(card -> card.getId().equals(chosenCardId))
                    .findFirst()
                    .ifPresent(card -> putUnderControllerControl(gameData, entry, card, opponentId));
            return;
        }

        if (opponentId == null || matchingCards.isEmpty()) {
            return;
        }
        if (matchingCards.size() == 1) {
            putUnderControllerControl(gameData, entry, matchingCards.getFirst(), opponentId);
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeScroungeResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        List<Integer> indices = IntStream.range(0, matchingCards.size()).boxed().toList();
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(opponentId, indices, GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        entry.getCard().getName() + " — choose a " + CardPredicateUtils.describeFilter(e.filter())
                                + " in your graveyard to put onto the battlefield.")
                .cardPool(new ArrayList<>(matchingCards))
                .mandatory(true)
                .build());
    }

    private List<Card> matchingCards(GameData gameData, UUID opponentId,
                                     CardPredicate filter,
                                     UUID sourceCardId) {
        if (opponentId == null) {
            return List.of();
        }
        List<Card> graveyard = gameData.playerGraveyards.get(opponentId);
        if (graveyard == null) {
            return List.of();
        }
        return graveyard.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, filter, sourceCardId))
                .toList();
    }

    private void putUnderControllerControl(GameData gameData, StackEntry entry, Card card, UUID opponentId) {
        UUID controllerId = entry.getControllerId();
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (!opponentId.equals(graveyardOwnerId)) {
            return;
        }
        if (graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
            gameLogService.append(gameData, GameLog.textCardText(
                    entry.getDescription() + " can't put ", card,
                    " onto the battlefield from a graveyard."));
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
        Permanent permanent = new Permanent(card);
        permanent.setEnteredFromGraveyardOwnerId(graveyardOwnerId);
        Set<CardType> enterTappedTypes =
                battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);
        graveyardReturnSupport.trackStolenCreature(gameData, permanent.getId(), controllerId, graveyardOwnerId);

        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(controllerId) + " puts ", card,
                " onto the battlefield under their control."));
        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, card);
    }
}
