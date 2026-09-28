package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentsThenEachControllerMayCastEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Guff Rewrites History's per-controller shuffle and free-cast effect. */
@Component
@RequiredArgsConstructor
public class ShuffleTargetPermanentsThenEachControllerMayCastEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleTargetPermanentsThenEachControllerMayCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        Set<UUID> affectedControllers = new HashSet<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (controllerId == null) {
                continue;
            }

            String name = target.getCard().getName();
            if (permanentRemovalService.removePermanentToLibraryShuffled(gameData, target)) {
                affectedControllers.add(controllerId);
                gameLogService.append(gameData,
                        GameLog.text(name + " is shuffled into its owner's library."));
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        for (UUID playerId : EachPlayerMayScryEffectHandler.apnapPlayers(gameData)) {
            if (!affectedControllers.contains(playerId)) {
                continue;
            }
            exileUntilNonlandAndOfferCast(gameData, entry, playerId);
        }
    }

    private void exileUntilNonlandAndOfferCast(GameData gameData, StackEntry entry, UUID playerId) {
        List<Card> library = gameData.playerDecks.get(playerId);
        List<Card> landsExiled = new ArrayList<>();
        String playerName = gameData.playerIdToName.get(playerId);
        while (library != null && !library.isEmpty()) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, playerId, card);
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " exiles ")
                    .card(card)
                    .text(" from the top of their library (" + entry.getCard().getName() + ").")
                    .build());

            if (card.hasType(CardType.LAND)) {
                landsExiled.add(card);
            } else {
                putLandsOnBottom(gameData, playerId, landsExiled);
                gameData.pendingMayAbilities.add(new PendingMayAbility(
                        entry.getCard(),
                        playerId,
                        List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                        "Cast " + card.getName() + " without paying its mana cost?",
                        card.getId()));
                return;
            }
        }
        putLandsOnBottom(gameData, playerId, landsExiled);
    }

    private void putLandsOnBottom(GameData gameData, UUID playerId, List<Card> lands) {
        if (lands.isEmpty()) {
            return;
        }
        for (Card land : lands) {
            gameData.removeFromExile(land.getId());
        }
        Collections.shuffle(lands);
        gameData.playerDecks.get(playerId).addAll(lands);
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                + " puts the exiled lands on the bottom of their library in a random order."));
    }
}
