package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.effect.l.LivingDeathEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Living Death: each player exiles all creature cards from their graveyard, then sacrifices all
 * creatures they control, then puts all cards they exiled this way onto the battlefield.
 *
 * <p>The exile step runs first, so creatures sacrificed by the second step hit their owners'
 * graveyards after the exile snapshot was taken and are not reanimated. The returning cards all
 * enter simultaneously (CR 614.12), so none of them sees the others entering.
 *
 * <p>The sacrifice is a step departure: a CR 616.1 question ends the step before any creature leaves, and the
 * return then follows as its own step. The exiled cards are kept on the entry
 * ({@link StackEntry#getResolutionExiledCardIds}), so the return never depends on state held by this handler.
 */
@Component
@RequiredArgsConstructor
public class LivingDeathEffectHandler implements NormalEffectHandlerBean, ContinuesAfterDeparture {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final StepDepartureSupport stepDepartureSupport;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final com.github.laxika.magicalvibes.service.graveyard.GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LivingDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LivingDeathEffect livingDeathEffect = (LivingDeathEffect) effect;
        List<UUID> exiledCardIds = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Card> graveyard = gameData.playerGraveyards.get(playerId);
                if (graveyard == null) {
                    continue;
                }

                List<Card> affectedCards = graveyard.stream()
                        .filter(card -> card.hasType(livingDeathEffect.cardType()))
                        .toList();
                for (Card card : affectedCards) {
                    if (graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, card.getId(), card)) {
                        exiledCardIds.add(card.getId());
                    }
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        List<UUID> permanentIdsToSacrifice = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (matchesCardType(gameData, permanent, livingDeathEffect.cardType())) {
                permanentIdsToSacrifice.add(permanent.getId());
            }
        });
        entry.setResolutionExiledCardIds(exiledCardIds);
        List<UUID> sacrificeIds = permanentIdsToSacrifice.stream()
                .filter(permanentId -> !gameQueryService.cantBeSacrificed(gameData,
                        gameQueryService.findPermanentById(gameData, permanentId)))
                .toList();
        stepDepartureSupport.departThenContinue(gameData, entry, effect,
                DepartureBatch.of(StepDepartureSupport.sacrifices(sacrificeIds)), this);
    }

    /** "Then puts all cards they exiled this way onto the battlefield", together, under their owners' control. */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        Map<UUID, List<Card>> exiledByPlayer = new LinkedHashMap<>();
        for (UUID cardId : entry.getResolutionExiledCardIds()) {
            ExiledCardEntry exiled = gameData.findExiledCard(cardId);
            if (exiled != null) {
                exiledByPlayer.computeIfAbsent(exiled.ownerId(), id -> new ArrayList<>()).add(exiled.card());
            }
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        Map<Permanent, UUID> controllerByPermanent = new LinkedHashMap<>();
        for (Map.Entry<UUID, List<Card>> playerEntry : exiledByPlayer.entrySet()) {
            UUID playerId = playerEntry.getKey();
            for (Card card : playerEntry.getValue()) {
                if (!gameData.removeFromExile(card.getId())) {
                    continue;
                }
                Permanent permanent = new Permanent(card);
                permanent.setEnteredFromExile(true);
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, playerId, permanent, enterTappedTypes, simultaneouslyEntered);
                simultaneouslyEntered.add(permanent);
                controllerByPermanent.put(permanent, playerId);
            }
        }

        gameData.collectingSimultaneousCreatureEntryTriggers = true;
        gameData.simultaneousCreatureEntryTriggers.clear();
        try {
            for (Map.Entry<Permanent, UUID> permanentEntry : controllerByPermanent.entrySet()) {
                Permanent permanent = permanentEntry.getKey();
                graveyardReturnSupport.handleCreatureEtbAndLegendRule(
                        gameData, permanentEntry.getValue(), permanent, permanent.getCard());
            }
        } finally {
            gameData.collectingSimultaneousCreatureEntryTriggers = false;
            gameData.simultaneousCreatureEntryTriggers.clear();
        }
    }

    private boolean matchesCardType(GameData gameData, Permanent permanent, CardType cardType) {
        return switch (cardType) {
            case ARTIFACT -> gameQueryService.isArtifact(gameData, permanent);
            case CREATURE -> gameQueryService.isCreature(gameData, permanent);
            default -> permanent.getCard().hasType(cardType);
        };
    }
}
