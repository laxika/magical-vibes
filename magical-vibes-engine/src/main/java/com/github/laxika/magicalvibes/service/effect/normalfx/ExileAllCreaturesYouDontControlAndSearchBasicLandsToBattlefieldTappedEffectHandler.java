package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllCreaturesYouDontControlAndSearchBasicLandsToBattlefieldTappedEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExileAllCreaturesYouDontControlAndSearchBasicLandsToBattlefieldTappedEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final BasicLandSearchQueueSupport basicLandSearchQueueSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllCreaturesYouDontControlAndSearchBasicLandsToBattlefieldTappedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Map<UUID, List<Permanent>> creaturesByController = new LinkedHashMap<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(entry.getControllerId())) {
                return;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().hasType(CardType.CREATURE)) {
                    creaturesByController.computeIfAbsent(playerId, ignored -> new ArrayList<>()).add(permanent);
                }
            }
        });

        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            for (List<Permanent> creatures : creaturesByController.values()) {
                for (Permanent creature : creatures) {
                    if (!permanentRemovalService.removePermanentToExile(gameData, creature)) {
                        continue;
                    }
                    gameLogService.append(gameData, GameLog.cardThen(creature.getCard(), " is exiled."));
                    log.info("Game {} - {} is exiled by {}",
                            gameData.id, creature.getCard().getName(), entry.getCard().getName());
                }
            }
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        List<LibrarySearchFollowUp.BasicLandsPick> picks = new ArrayList<>();
        for (UUID playerId : basicLandSearchQueueSupport.apnapOrder(gameData)) {
            List<Permanent> creatures = creaturesByController.get(playerId);
            if (creatures != null && !creatures.isEmpty()) {
                picks.add(new LibrarySearchFollowUp.BasicLandsPick(
                        playerId, creatures.size(), true));
            }
        }
        basicLandSearchQueueSupport.advance(gameData,
                LibrarySearchFollowUp.basicLandSearches(picks, List.of()));
    }
}
