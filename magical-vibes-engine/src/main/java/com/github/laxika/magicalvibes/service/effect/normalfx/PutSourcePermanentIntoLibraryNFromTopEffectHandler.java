package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourcePermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves putting the source permanent into its owner's library at a fixed position. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutSourcePermanentIntoLibraryNFromTopEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutSourcePermanentIntoLibraryNFromTopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int position = Math.max(0, ((PutSourcePermanentIntoLibraryNFromTopEffect) effect).position());
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        if (permanentRemovalService.removePermanentToLibraryPosition(gameData, source, position)) {
            String ordinal = switch (position) {
                case 0 -> "on top of";
                case 1 -> "second from the top of";
                case 2 -> "third from the top of";
                default -> (position + 1) + "th from the top of";
            };
            gameLogService.append(gameData, GameLog.builder().card(source.getCard())
                    .text(" is put " + ordinal + " its owner's library.").build());
            log.info("Game {} - {} put {} its owner's library", gameData.id,
                    source.getCard().getName(), ordinal);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
