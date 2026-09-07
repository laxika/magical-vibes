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
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class TargetPlayerChoosesCardFromGraveyardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerChoosesCardFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerChoosesCardFromGraveyardToBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        List<Card> matchingCards = matchingCards(gameData, targetPlayerId, e.filter(), entry.getCard().getId());

        if (targetPlayerId == null || matchingCards.isEmpty()) {
            return;
        }
        if (matchingCards.size() == 1) {
            putUnderControllerControl(gameData, entry, matchingCards.getFirst(), targetPlayerId, e);
            return;
        }

        List<Integer> indices = IntStream.range(0, matchingCards.size()).boxed().toList();
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(controllerId, indices, GraveyardChoiceDestination.BATTLEFIELD,
                        entry.getCard().getName() + " — choose a " + CardPredicateUtils.describeFilter(e.filter())
                                + " in the targeted player's graveyard to put onto the battlefield.")
                .cardPool(new ArrayList<>(matchingCards))
                .enterTapped(e.enterTapped())
                .exileIfLeavesBattlefield(e.exileIfLeavesBattlefield())
                .mandatory(true)
                .build());
    }

    private List<Card> matchingCards(GameData gameData, UUID targetPlayerId,
                                     CardPredicate filter, UUID sourceCardId) {
        if (targetPlayerId == null) {
            return List.of();
        }
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        if (graveyard == null) {
            return List.of();
        }
        return graveyard.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, filter, sourceCardId))
                .toList();
    }

    private void putUnderControllerControl(GameData gameData, StackEntry entry, Card card,
                                           UUID targetPlayerId,
                                           TargetPlayerChoosesCardFromGraveyardToBattlefieldEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (!targetPlayerId.equals(graveyardOwnerId)) {
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
        permanent.setExileIfLeavesBattlefield(effect.exileIfLeavesBattlefield());
        if (effect.enterTapped()) {
            permanent.tap();
        }
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);
        graveyardReturnSupport.trackStolenCreature(gameData, permanent.getId(), controllerId, graveyardOwnerId);

        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(controllerId) + " puts ", card,
                " onto the battlefield under their control."));
        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, card);
    }
}
