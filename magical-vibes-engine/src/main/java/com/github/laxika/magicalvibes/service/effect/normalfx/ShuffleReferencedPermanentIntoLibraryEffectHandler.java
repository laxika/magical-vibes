package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.ShuffleReferencedPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleReferencedPermanentIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleReferencedPermanentIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ShuffleReferencedPermanentIntoLibraryEffect shuffleEffect =
                (ShuffleReferencedPermanentIntoLibraryEffect) effect;
        Permanent referenced = findReferencedPermanent(gameData, entry, shuffleEffect.reference());
        if (referenced == null) {
            return;
        }

        String name = referenced.getCard().getName();
        if (permanentRemovalService.removePermanentToLibraryShuffled(gameData, referenced)) {
            gameLogService.append(gameData, GameLog.text(name + " is shuffled into its owner's library."));
            log.info("Game {} - {} shuffled into owner's library", gameData.id, name);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private Permanent findReferencedPermanent(GameData gameData, StackEntry entry,
                                              PermanentReference reference) {
        return switch (reference) {
            case SOURCE -> findPermanent(gameData, entry.getSourcePermanentId());
            case TRIGGERING -> findPermanent(gameData, entry.getTriggeringPermanentId());
            case ATTACHED -> {
                Permanent source = findPermanent(gameData, entry.getSourcePermanentId());
                yield source == null || !source.isAttached()
                        ? null
                        : findPermanent(gameData, source.getAttachedTo());
            }
            case RETURNED -> findPermanentByCardId(gameData, entry.getTargetId());
        };
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        return permanentId == null ? null : gameQueryService.findPermanentById(gameData, permanentId);
    }

    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        if (cardId == null) {
            return null;
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (cardId.equals(permanent.getCard().getId())
                        || (permanent.getOriginalCard() != null
                        && cardId.equals(permanent.getOriginalCard().getId()))) {
                    return permanent;
                }
            }
        }
        return null;
    }
}
