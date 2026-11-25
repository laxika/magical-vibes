package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Asks each opponent who could accept, in turn order, whether they accept the offer, changing nothing while they decide,
 * then has the payload happen for the opponents who accepted and once more for the offer's controller for each of them,
 * as the next steps of the same resolution (C13 tempting offer rulings, CR 101.4, 608.2d).
 */
@Component
@RequiredArgsConstructor
public class TemptingOfferEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TemptingOfferEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var offer = (TemptingOfferEffect) effect;
        List<UUID> opponents = new ArrayList<>(SubjectResolutionService
                .apnapOpponents(gameData, entry.getControllerId()));
        // An opponent who couldn't do what is offered can't accept it, so isn't asked (CR 608.2d).
        List<UUID> couldAccept = subjectResolutionService.players(gameData, entry, effect, offer.offeredTo());
        opponents.removeIf(id -> !gameData.playerIds.contains(id) || !couldAccept.contains(id));
        entry.setAcceptedPlayerIds(List.of());
        if (!opponents.isEmpty()) {
            promptNext(gameData, entry.getCard(), offer.deciding(opponents, List.of()));
        }
    }

    /**
     * Applies one opponent's answer: the next opponent is asked, or, once every opponent has decided, the accepting
     * opponents are recorded and the payload's steps are put next in the parked resolution.
     */
    public void completeChoice(GameData gameData, PendingMayAbility ability, TemptingOfferEffect offer,
                               boolean accepted) {
        UUID playerId = ability.controllerId();
        List<UUID> acceptedIds = new ArrayList<>(offer.acceptedOpponentIds());
        if (accepted) {
            acceptedIds.add(playerId);
        }
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                + (accepted ? " accepts the offer." : " declines the offer.")));

        List<UUID> remaining = new ArrayList<>(offer.remainingOpponentIds());
        remaining.remove(playerId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (!remaining.isEmpty()) {
            promptNext(gameData, ability.sourceCard(), offer.deciding(remaining, acceptedIds));
            return;
        }

        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            return;
        }
        acceptedIds.removeIf(id -> !gameData.playerIds.contains(id));
        entry.setAcceptedPlayerIds(acceptedIds);
        List<CardEffect> steps = offer.stepsFor(acceptedIds);
        if (!steps.isEmpty()) {
            entry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, steps);
        }
    }

    private void promptNext(GameData gameData, Card sourceCard, TemptingOfferEffect offer) {
        UUID opponentId = offer.remainingOpponentIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(sourceCard, opponentId, List.of(offer),
                offer.prompt()));
    }
}
