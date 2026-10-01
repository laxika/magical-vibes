package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureCardsFromGraveyardToBattlefieldFaceDownAsCybermenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves The Moonbase's face-down Cyberman reanimation. */
@Component
@RequiredArgsConstructor
public class ReturnTargetCreatureCardsFromGraveyardToBattlefieldFaceDownAsCybermenEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreatureCardsFromGraveyardToBattlefieldFaceDownAsCybermenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<GraveyardCard> cardsToReturn = new ArrayList<>();
        for (UUID targetCardId : entry.getTargetCardIdsForEffect(effect)) {
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
            Card card = graveyardOwnerId == null
                    ? null
                    : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
            if (graveyardOwnerId != null
                    && !graveyardOwnerId.equals(entry.getControllerId())
                    && card != null
                    && card.hasType(CardType.CREATURE)
                    && !graveyardReturnSupport.isCardBlockedFromEnteringFromZone(
                    gameData, card, Zone.GRAVEYARD)) {
                cardsToReturn.add(new GraveyardCard(graveyardOwnerId, card));
            }
        }
        if (cardsToReturn.isEmpty()) {
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        int returnedCount = 0;
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (GraveyardCard graveyardCard : cardsToReturn) {
                Card card = graveyardCard.card();
                if (gameQueryService.findCardInGraveyardById(gameData, card.getId()) == null) {
                    continue;
                }

                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                Permanent permanent = new Permanent(card);
                permanent.setEnteredFromGraveyardOwnerId(graveyardCard.ownerId());
                permanent.setFaceDown(2, 2,
                        Set.of(CardType.ARTIFACT, CardType.CREATURE),
                        Set.of(CardSubtype.CYBERMAN));
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, entry.getControllerId(), permanent,
                        enterTappedTypes, simultaneouslyEntered);
                simultaneouslyEntered.add(permanent);
                if (!entry.getControllerId().equals(graveyardCard.ownerId())) {
                    graveyardReturnSupport.trackStolenCreature(
                            gameData, permanent.getId(), entry.getControllerId(), graveyardCard.ownerId());
                }
                battlefieldEntryService.processFaceDownCreatureETBTriggers(
                        gameData, entry.getControllerId(), card);
                returnedCount++;
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (returnedCount > 0) {
            entry.setEventValue(returnedCount);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(entry.getControllerId()) + " puts "
                            + returnedCount + " creature card(s) onto the battlefield face down as Cybermen."));
        }
    }

    private record GraveyardCard(UUID ownerId, Card card) {
    }
}
