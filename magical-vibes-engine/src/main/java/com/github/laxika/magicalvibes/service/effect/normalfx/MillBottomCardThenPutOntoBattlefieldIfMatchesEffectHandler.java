package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillBottomCardThenPutOntoBattlefieldIfMatchesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MillBottomCardThenPutOntoBattlefieldIfMatchesEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillBottomCardThenPutOntoBattlefieldIfMatchesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillBottomCardThenPutOntoBattlefieldIfMatchesEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + "'s library is empty — "
                            + entry.getCard().getName() + "'s ability does nothing."));
            return;
        }

        Card bottomCard = library.removeLast();
        boolean enteredGraveyard = graveyardService.addCardToGraveyard(gameData, controllerId, bottomCard, Zone.LIBRARY);
        if (enteredGraveyard) {
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(controllerId) + " puts ", bottomCard,
                    " from the bottom of their library into their graveyard."));
        } else if (gameData.findExiledCard(bottomCard.getId()) == null) {
            return;
        }

        Integer sourcePower = entry.getTriggeringPermanentPowerAtTrigger();
        if (sourcePower == null && entry.getSourcePermanentSnapshot() != null) {
            sourcePower = entry.getSourcePermanentSnapshot().getEffectivePower();
        }
        boolean matches = predicateEvaluationService.matchesCardPredicate(
                bottomCard,
                e.filter(),
                entry.getCard().getId(),
                gameData,
                controllerId,
                entry.getSourcePermanentId(),
                sourcePower,
                entry.getXValue(),
                entry.getSourcePermanentSnapshot());
        if (!matches) {
            return;
        }
        if (enteredGraveyard) {
            graveyardReturnSupport.reanimateTargetedCard(gameData, controllerId, bottomCard);
        } else {
            returnExiledCardToBattlefield(gameData, bottomCard);
        }
    }

    /**
     * Puts a card that a replacement effect exiled instead of letting it reach the graveyard onto the
     * battlefield under its owner's control. The ability still checks the card's characteristics
     * after the exile, so the card returns from exile (Grenzo, Dungeon Warden with Leyline of the Void).
     */
    private void returnExiledCardToBattlefield(GameData gameData, Card card) {
        ExiledCardEntry exiled = gameData.findExiledCard(card.getId());
        if (exiled == null || !gameData.removeFromExile(card.getId())) {
            return;
        }
        UUID ownerId = exiled.ownerId();
        Permanent permanent = new Permanent(card);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, permanent);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(ownerId) + " returns ", card,
                " from exile to the battlefield."));
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, ownerId, card, null, false);
    }
}
