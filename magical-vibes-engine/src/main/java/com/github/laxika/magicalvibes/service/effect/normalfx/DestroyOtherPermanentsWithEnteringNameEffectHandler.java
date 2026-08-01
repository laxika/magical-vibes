package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyOtherPermanentsWithEnteringNameEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Destroys every permanent sharing the entering permanent's name except the entering permanent
 * itself. Can't be regenerated. Name is read from the live permanent when present, otherwise from
 * last-known card info via {@code triggeringCardId}.
 */
@Component
@RequiredArgsConstructor
public class DestroyOtherPermanentsWithEnteringNameEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyOtherPermanentsWithEnteringNameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID enteringId = entry.getTriggeringPermanentId();
        String name = resolveEnteringName(gameData, entry, enteringId);
        if (name == null) {
            return;
        }

        List<Permanent> toDestroy = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (enteringId != null && perm.getId().equals(enteringId)) {
                    continue;
                }
                if (name.equals(perm.getCard().getName())) {
                    toDestroy.add(perm);
                }
            }
        });

        destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(), true);
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private String resolveEnteringName(GameData gameData, StackEntry entry, UUID enteringId) {
        if (enteringId != null) {
            Permanent entering = gameQueryService.findPermanentById(gameData, enteringId);
            if (entering != null) {
                return entering.getCard().getName();
            }
        }
        UUID cardId = entry.getTriggeringCardId();
        if (cardId == null) {
            return null;
        }
        Card found = findCardById(gameData, cardId);
        return found != null ? found.getName() : null;
    }

    private static Card findCardById(GameData gameData, UUID cardId) {
        for (var battlefield : gameData.playerBattlefields.values()) {
            for (Permanent perm : battlefield) {
                if (perm.getCard().getId().equals(cardId)) {
                    return perm.getCard();
                }
            }
        }
        for (var graveyard : gameData.playerGraveyards.values()) {
            for (Card card : graveyard) {
                if (card.getId().equals(cardId)) {
                    return card;
                }
            }
        }
        for (var exile : gameData.exiledCards) {
            if (exile.card().getId().equals(cardId)) {
                return exile.card();
            }
        }
        return null;
    }
}
