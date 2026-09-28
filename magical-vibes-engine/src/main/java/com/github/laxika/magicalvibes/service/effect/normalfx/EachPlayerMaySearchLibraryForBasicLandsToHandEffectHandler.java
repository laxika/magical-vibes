package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMaySearchLibraryForBasicLandsToHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Enigma Ridges' land-count-difference search through the shared library flow. */
@Component
@RequiredArgsConstructor
public class EachPlayerMaySearchLibraryForBasicLandsToHandEffectHandler implements NormalEffectHandlerBean {

    private final BasicLandSearchQueueSupport basicLandSearchQueueSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMaySearchLibraryForBasicLandsToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> players = basicLandSearchQueueSupport.apnapOrder(gameData);
        int greatestLandCount = players.stream()
                .mapToInt(playerId -> countLands(gameData, playerId))
                .max()
                .orElse(0);
        List<LibrarySearchFollowUp.BasicLandsPick> picks = players.stream()
                .map(playerId -> new LibrarySearchFollowUp.BasicLandsPick(
                        playerId, greatestLandCount - countLands(gameData, playerId), false))
                .filter(pick -> pick.count() > 0)
                .toList();

        if (!picks.isEmpty()) {
            basicLandSearchQueueSupport.advance(
                    gameData, LibrarySearchFollowUp.basicLandSearchesToHand(picks));
        }
    }

    private int countLands(GameData gameData, UUID playerId) {
        return (int) gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isLand(gameData, permanent))
                .count();
    }
}
