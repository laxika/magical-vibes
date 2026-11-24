package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.library.ZoneToLibraryService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Timetwister-style mass reset: every player's hand and graveyard — plus, for Sway of the Stars,
 * every permanent they own — goes into their library, which is then shuffled.
 *
 * <p>The permanent sweep is a separate first pass over all players so the battlefield empties as
 * one event rather than one player at a time; only after that does each library get drained and
 * shuffled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerShufflesZonesIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ZoneToLibraryService zoneToLibraryService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerShufflesZonesIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var shuffle = (EachPlayerShufflesZonesIntoLibraryEffect) effect;
        String cardName = entry.getCard().getName();
        List<UUID> players = subjectResolutionService.players(gameData, entry, effect, shuffle.players());
        Map<UUID, Integer> shuffledAway = new LinkedHashMap<>();
        if (shuffle.zones().contains(Zone.BATTLEFIELD)) {
            Map<UUID, Integer> moved = zoneToLibraryService.moveOwnedPermanentsIntoLibraries(gameData, players);
            for (UUID playerId : players) {
                int count = moved.getOrDefault(playerId, 0);
                if (count > 0) {
                    shuffledAway.put(playerId, count);
                    gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                            + " shuffles " + LibraryShuffleSupport.pluralPermanents(count)
                            + " they own into their library (" + cardName + ")."));
                }
            }
        }
        // A library an earlier effect left unshuffled for this one is shuffled here, once (Elixir of Immortality).
        Set<UUID> deferred = new LinkedHashSet<>(entry.getDeferredLibraryShuffleOwnerIds());
        entry.getDeferredLibraryShuffleOwnerIds().clear();
        deferred.removeAll(players);
        for (UUID playerId : players) {
            List<String> zoneTexts = new ArrayList<>();
            if (shuffle.zones().contains(Zone.HAND)) {
                int hand = zoneToLibraryService.moveHandIntoLibrary(gameData, playerId);
                shuffledAway.merge(playerId, hand, Integer::sum);
                zoneTexts.add("hand (" + LibraryShuffleSupport.pluralCards(hand) + ")");
            }
            if (shuffle.zones().contains(Zone.GRAVEYARD)) {
                int graveyard = zoneToLibraryService.moveGraveyardIntoLibrary(gameData, playerId);
                shuffledAway.merge(playerId, graveyard, Integer::sum);
                zoneTexts.add("graveyard (" + LibraryShuffleSupport.pluralCards(graveyard) + ")");
            }
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(zoneTexts.isEmpty()
                    ? playerName + "'s library is shuffled."
                    : playerName + " shuffles their " + String.join(" and ", zoneTexts)
                            + " into their library (" + cardName + ")."));
        }
        for (UUID ownerId : deferred) {
            LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
        }
        entry.recordStep(StepKind.SHUFFLE_INTO_LIBRARY, StepOutcome.ofAmounts(shuffledAway));
    }
}
