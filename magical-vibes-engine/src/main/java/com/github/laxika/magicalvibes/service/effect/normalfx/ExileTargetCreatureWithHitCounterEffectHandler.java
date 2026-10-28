package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureWithHitCounterEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfIntoOwnerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureWithHitCounterEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final TargetPlayerLosesGameEffectHandler targetPlayerLosesGameEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureWithHitCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID damagedPlayerId = entry.getAttackedTargetId();
        UUID targetId = entry.targetsForEffect(effect).stream().findFirst().orElse(entry.getTargetId());
        if (damagedPlayerId == null || targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null
                || !gameQueryService.isCreature(gameData, target)
                || !damagedPlayerId.equals(gameQueryService.findPermanentController(gameData, targetId))) {
            return;
        }

        boolean token = target.getCard().isToken();
        if (!permanentRemovalService.removePermanentToExile(gameData, target)) {
            return;
        }

        if (!token) {
            ExiledCardEntry exiled = gameData.findExiledCard(target.getCard().getId());
            if (exiled != null) {
                UUID cardId = exiled.card().getId();
                gameData.exiledCardHitCounters.merge(cardId, 1, Integer::sum);
                gameLogService.append(gameData,
                        GameLog.cardThen(target.getCard(), " is exiled with a hit counter."));
            }
        } else {
            // A token in exile ceases to exist (CR 111.7). It is removed here rather than by the state-based
            // action, which a lost game would skip before the loss check below ends the game.
            gameData.removeFromExile(target.getCard().getId());
        }

        // The loss check reads every card the player owns in exile with a hit counter, so it applies
        // even when the exiled creature was a token that adds no counter (Etrata ruling, 2018-10-05).
        long hitCounterCards = gameData.exiledCards.stream()
                .filter(card -> damagedPlayerId.equals(card.ownerId()))
                .filter(card -> gameData.exiledCardHitCounters.getOrDefault(card.card().getId(), 0) > 0)
                .count();
        // Etrata is shuffled away before the loss is declared, since a lost game stops further processing.
        shuffleSourceIntoOwnerLibrary(gameData, entry);
        if (hitCounterCards >= 3) {
            targetPlayerLosesGameEffectHandler.resolve(gameData, entry,
                    new TargetPlayerLosesGameEffect(damagedPlayerId));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        log.info("Game {} - {} exiled a creature with a hit counter", gameData.id,
                entry.getCard().getName());
    }

    private void shuffleSourceIntoOwnerLibrary(GameData gameData, StackEntry entry) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null) {
            boolean shuffled = permanentRemovalService.removePermanentToLibraryShuffled(gameData, source);
            if (shuffled) {
                gameLogService.append(gameData,
                        GameLog.cardThen(entry.getCard(), " is shuffled into its owner's library."));
            }
            return;
        }
        // If Etrata left the battlefield while its ability was on the stack, it stays in its new zone and
        // only its owner's library is shuffled (Etrata ruling, 2018-10-05).
        UUID ownerId = entry.getCard().getOwnerId() != null
                ? entry.getCard().getOwnerId() : entry.getControllerId();
        if (gameData.playerDecks.get(ownerId) != null) {
            LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
            gameLogService.append(gameData,
                    GameLog.text(gameData.playerIdToName.get(ownerId) + "'s library is shuffled."));
        }
    }
}
