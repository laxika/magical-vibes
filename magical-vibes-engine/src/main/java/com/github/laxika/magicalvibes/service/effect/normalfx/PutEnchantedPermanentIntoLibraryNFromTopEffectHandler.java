package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutEnchantedPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutEnchantedPermanentIntoLibraryNFromTopEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutEnchantedPermanentIntoLibraryNFromTopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent enchanted = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (enchanted == null) {
            return;
        }

        int position = Math.max(0, ((PutEnchantedPermanentIntoLibraryNFromTopEffect) effect).position());
        if (permanentRemovalService.removePermanentToLibraryPosition(gameData, enchanted, position)) {
            String ordinal = switch (position) {
                case 0 -> "on top of";
                case 1 -> "second from the top of";
                case 2 -> "third from the top of";
                default -> (position + 1) + "th from the top of";
            };
            gameLogService.append(gameData, GameLog.builder().card(enchanted.getCard())
                    .text(" is put " + ordinal + " its owner's library.").build());
            log.info("Game {} - {} put {} its owner's library", gameData.id,
                    enchanted.getCard().getName(), ordinal);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
