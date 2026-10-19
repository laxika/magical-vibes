package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPlayerPermanentsIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleTargetPlayerPermanentsIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleTargetPlayerPermanentsIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ShuffleTargetPlayerPermanentsIntoLibraryEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> toShuffle = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield != null) {
            for (Permanent permanent : List.copyOf(battlefield)) {
                if (e.filter() == null || predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, e.filter())) {
                    toShuffle.add(permanent);
                }
            }
        }

        // Each affected library is shuffled once after all its permanents are in it, so a shuffle
        // trigger (Psychogenic Probe) fires once per library rather than once per permanent.
        Set<UUID> shuffledLibraryOwnerIds = new LinkedHashSet<>();
        for (Permanent permanent : toShuffle) {
            String name = permanent.getCard().getName();
            UUID libraryOwnerId = gameData.defaultControllerOf(permanent.getId());
            if (permanentRemovalService.removePermanentToLibraryUnshuffled(gameData, permanent)) {
                shuffledLibraryOwnerIds.add(libraryOwnerId);
                gameLogService.append(gameData, GameLog.text(name + " is shuffled into its owner's library."));
                log.info("Game {} - {} shuffled into owner's library", gameData.id, name);
            }
        }
        for (UUID libraryOwnerId : shuffledLibraryOwnerIds) {
            LibraryShuffleHelper.shuffleLibrary(gameData, libraryOwnerId);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
