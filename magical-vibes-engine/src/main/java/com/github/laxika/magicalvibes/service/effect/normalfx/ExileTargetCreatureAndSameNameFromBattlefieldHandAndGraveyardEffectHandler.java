package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndSameNameFromBattlefieldHandAndGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureAndSameNameFromBattlefieldHandAndGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardService graveyardService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureAndSameNameFromBattlefieldHandAndGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        String targetName = target.getCard().getName();
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        List<Permanent> sameNameCreatures = controllerId == null
                ? List.of()
                : collectSameNameCreatures(gameData, controllerId, target, targetName);

        permanentRemovalService.removePermanentToExile(gameData, target);
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));
        for (Permanent creature : sameNameCreatures) {
            permanentRemovalService.removePermanentToExile(gameData, creature);
            gameLogService.append(gameData, GameLog.cardThen(creature.getCard(), " is exiled."));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (controllerId == null) {
            return;
        }

        playerInteractionSupport.resolveRevealHand(gameData, controllerId);
        List<Card> exiledFromHand = exileMatching(gameData, controllerId,
                gameData.playerHands.get(controllerId), targetName);
        List<Card> exiledFromGraveyard = exileMatching(gameData, controllerId,
                gameData.playerGraveyards.get(controllerId), targetName);
        if (!exiledFromGraveyard.isEmpty()) {
            graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, exiledFromGraveyard);
        }

        int total = 1 + sameNameCreatures.size() + exiledFromHand.size() + exiledFromGraveyard.size();
        String controllerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(entry.getCard().getName() + " exiles " + total
                + " card" + (total != 1 ? "s" : "") + " named " + targetName + " from "
                + controllerName + "'s battlefield, hand, and graveyard."));
        log.info("Game {} - {} exiled {} cards named {} from {}'s battlefield, hand, and graveyard",
                gameData.id, entry.getCard().getName(), total, targetName, controllerName);
    }

    private List<Permanent> collectSameNameCreatures(GameData gameData, UUID controllerId,
                                                       Permanent target, String targetName) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return List.of();
        }
        List<Permanent> matches = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (!permanent.getId().equals(target.getId())
                    && gameQueryService.isCreature(gameData, permanent)
                    && permanent.getCard().getName().equals(targetName)) {
                matches.add(permanent);
            }
        }
        return List.copyOf(matches);
    }

    private List<Card> exileMatching(GameData gameData, UUID playerId, List<Card> zone, String name) {
        if (zone == null) {
            return List.of();
        }
        List<Card> matches = new ArrayList<>();
        for (Card card : zone) {
            if (card.getName().equals(name)) {
                matches.add(card);
            }
        }
        zone.removeAll(matches);
        for (Card card : matches) {
            gameData.addToExile(playerId, card);
        }
        return matches;
    }
}
