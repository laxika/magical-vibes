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

                long hitCounterCards = gameData.exiledCards.stream()
                        .filter(card -> damagedPlayerId.equals(card.ownerId()))
                        .filter(card -> gameData.exiledCardHitCounters.getOrDefault(card.card().getId(), 0) > 0)
                        .count();
                if (hitCounterCards >= 3) {
                    targetPlayerLosesGameEffectHandler.resolve(gameData, entry,
                            new TargetPlayerLosesGameEffect(damagedPlayerId));
                }
            }
        }

        shuffleSourceIntoOwnerLibrary(gameData, entry);
        permanentRemovalService.removeOrphanedAuras(gameData);
        log.info("Game {} - {} exiled a creature with a hit counter", gameData.id,
                entry.getCard().getName());
    }

    private void shuffleSourceIntoOwnerLibrary(GameData gameData, StackEntry entry) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        boolean shuffled;
        if (source != null) {
            shuffled = permanentRemovalService.removePermanentToLibraryShuffled(gameData, source);
        } else {
            shuffled = permanentRemovalService.shuffleCardIntoOwnerLibrary(
                    gameData, entry.getCard(), entry.getControllerId());
        }
        if (shuffled) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " is shuffled into its owner's library."));
        }
    }
}
