package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnOpponentCreaturesFromGraveyardAsFoodEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnOpponentCreaturesFromGraveyardAsFoodEffectHandler implements NormalEffectHandlerBean {

    private static final ActivatedAbility FOOD_ABILITY = new ActivatedAbility(
            true,
            "{2}",
            List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
            "{2}, {T}, Sacrifice this artifact: You gain 3 life."
    );

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnOpponentCreaturesFromGraveyardAsFoodEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<ReturnedCard> cardsToReturn = collectAndRemoveCards(gameData, controllerId);
        if (cardsToReturn.isEmpty()) {
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<EnteredFood> enteredFoods = new ArrayList<>();
        for (ReturnedCard returnedCard : cardsToReturn) {
            Card card = returnedCard.card();
            Card foodCard = card.createRuntimeCopy();
            foodCard.setType(CardType.ARTIFACT);
            foodCard.setAdditionalTypes(Set.of());
            foodCard.setSubtypes(List.of(CardSubtype.FOOD));
            foodCard.freeze();

            Permanent permanent = new Permanent(card);
            permanent.setCard(foodCard);
            permanent.setEnteredFromGraveyardOwnerId(returnedCard.graveyardOwnerId());
            addFoodAbilityEffect(gameData, entry, permanent);

            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(permanent);
            graveyardReturnSupport.trackStolenCreature(
                    gameData, permanent.getId(), controllerId, returnedCard.graveyardOwnerId());
            enteredFoods.add(new EnteredFood(permanent, foodCard));
        }

        for (EnteredFood enteredFood : enteredFoods) {
            graveyardReturnSupport.handleCreatureEtbAndLegendRule(
                    gameData, controllerId, enteredFood.permanent(), enteredFood.card());
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        for (ReturnedCard returnedCard : cardsToReturn) {
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " returns ", returnedCard.card(),
                    " to the battlefield under their control as a Food artifact."));
        }
    }

    private List<ReturnedCard> collectAndRemoveCards(GameData gameData, UUID controllerId) {
        List<ReturnedCard> cardsToReturn = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (UUID graveyardOwnerId : gameData.orderedPlayerIds) {
                if (graveyardOwnerId.equals(controllerId)) {
                    continue;
                }
                List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
                if (graveyard == null || graveyard.isEmpty()) {
                    continue;
                }

                Set<UUID> trackedIds = gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn
                        .getOrDefault(graveyardOwnerId, Set.of());
                for (Card card : new ArrayList<>(graveyard)) {
                    if (!card.hasType(CardType.CREATURE)
                            || !trackedIds.contains(card.getId())
                            || graveyardReturnSupport.isCardBlockedFromEnteringFromZone(
                            gameData, card, Zone.GRAVEYARD)
                            || !graveyard.remove(card)) {
                        continue;
                    }
                    graveyardService.notifyCardsLeftGraveyard(gameData, graveyardOwnerId, card);
                    cardsToReturn.add(new ReturnedCard(card, graveyardOwnerId));
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
        return cardsToReturn;
    }

    private void addFoodAbilityEffect(GameData gameData, StackEntry entry, Permanent permanent) {
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                new GrantActivatedAbilityEffect(FOOD_ABILITY, GrantScope.TARGET),
                permanent.getId(), null, null, EffectDuration.PERMANENT, 0));
    }

    private record ReturnedCard(Card card, UUID graveyardOwnerId) {
    }

    private record EnteredFood(Permanent permanent, Card card) {
    }
}
