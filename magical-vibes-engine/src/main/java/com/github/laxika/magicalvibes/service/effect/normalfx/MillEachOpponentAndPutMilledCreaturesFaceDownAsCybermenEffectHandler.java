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
import com.github.laxika.magicalvibes.model.effect.MillEachOpponentAndPutMilledCreaturesFaceDownAsCybermenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves The Cyber-Controller's opponent mill and face-down Cyberman creation. */
@Component
@RequiredArgsConstructor
public class MillEachOpponentAndPutMilledCreaturesFaceDownAsCybermenEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillEachOpponentAndPutMilledCreaturesFaceDownAsCybermenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var cybermanEffect = (MillEachOpponentAndPutMilledCreaturesFaceDownAsCybermenEffect) effect;
        int count = Math.max(0, amountEvaluationService.evaluate(
                gameData, cybermanEffect.count(),
                AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot())));

        List<MilledCreature> milledCreatures = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(entry.getControllerId())) {
                continue;
            }
            for (Card card : graveyardService.resolveMillPlayer(gameData, playerId, count)) {
                if (card.hasType(CardType.CREATURE)) {
                    milledCreatures.add(new MilledCreature(playerId, card));
                }
            }
        }
        if (milledCreatures.isEmpty()) {
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        int returnedCount = 0;
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (MilledCreature milledCreature : milledCreatures) {
                Card card = milledCreature.card();
                if (gameQueryService.findCardInGraveyardById(gameData, card.getId()) == null
                        || graveyardReturnSupport.isCardBlockedFromEnteringFromZone(
                        gameData, card, Zone.GRAVEYARD)) {
                    continue;
                }

                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                Permanent permanent = new Permanent(card);
                permanent.setEnteredFromGraveyardOwnerId(milledCreature.ownerId());
                permanent.setFaceDown(2, 2,
                        Set.of(CardType.ARTIFACT, CardType.CREATURE),
                        Set.of(CardSubtype.CYBERMAN));
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, entry.getControllerId(), permanent,
                        enterTappedTypes, simultaneouslyEntered);
                simultaneouslyEntered.add(permanent);
                if (!entry.getControllerId().equals(milledCreature.ownerId())) {
                    graveyardReturnSupport.trackStolenCreature(
                            gameData, permanent.getId(), entry.getControllerId(), milledCreature.ownerId());
                }
                battlefieldEntryService.processFaceDownCreatureETBTriggers(
                        gameData, entry.getControllerId(), card);
                returnedCount++;
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (returnedCount > 0) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(entry.getControllerId()) + " puts "
                            + returnedCount + " creature card(s) onto the battlefield face down as Cybermen."));
        }
    }

    private record MilledCreature(UUID ownerId, Card card) {
    }
}
