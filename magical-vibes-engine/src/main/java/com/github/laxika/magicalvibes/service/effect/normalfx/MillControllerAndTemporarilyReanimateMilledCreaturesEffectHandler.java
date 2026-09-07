package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndTemporarilyReanimateMilledCreaturesEffect;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves the mill-and-temporary-reanimation effect. */
@Component
@RequiredArgsConstructor
public class MillControllerAndTemporarilyReanimateMilledCreaturesEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndTemporarilyReanimateMilledCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var millEffect = (MillControllerAndTemporarilyReanimateMilledCreaturesEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> milled = graveyardService.resolveMillPlayer(gameData, controllerId, millEffect.count());
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) {
            return;
        }

        List<Card> creatureCards = milled.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .filter(graveyard::contains)
                .filter(card -> !graveyardReturnSupport.isCardBlockedFromEnteringFromZone(
                        gameData, card, Zone.GRAVEYARD))
                .toList();
        if (creatureCards.isEmpty()) {
            return;
        }

        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            graveyard.removeAll(creatureCards);
            graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, creatureCards);
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<Permanent> enteredPermanents = new ArrayList<>();
        for (Card card : creatureCards) {
            Permanent permanent = new Permanent(card);
            permanent.getGrantedKeywords().add(Keyword.HASTE);
            permanent.setEnteredFromGraveyardOwnerId(controllerId);
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered);
            if (gameQueryService.findPermanentById(gameData, permanent.getId()) != null) {
                simultaneouslyEntered.add(permanent);
                enteredPermanents.add(permanent);
            }
        }

        for (Permanent permanent : enteredPermanents) {
            graveyardReturnSupport.handleCreatureEtbAndLegendRule(
                    gameData, controllerId, permanent, permanent.getCard());
            if (gameQueryService.findPermanentById(gameData, permanent.getId()) != null) {
                gameData.queueDelayedAction(new DelayedPermanentAction(
                        permanent.getId(), DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP));
            }
        }
    }
}
