package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromReferencedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves counter removal from an Aura's attached permanent or another referenced permanent. */
@Component
@RequiredArgsConstructor
public class RemoveCounterFromReferencedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromReferencedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (RemoveCounterFromReferencedPermanentEffect) effect;
        Permanent referenced = switch (typedEffect.reference()) {
            case ATTACHED -> findAttached(gameData, entry);
            case TRIGGERING -> findPermanent(gameData, entry.getTriggeringPermanentId());
            case RETURNED -> findPermanentByCardId(gameData, entry.getTargetId());
            case SOURCE -> throw new IllegalStateException("SOURCE counters belong on RemoveCounterFromSourceEffect");
        };
        if (referenced == null) {
            return;
        }

        permanentCounterSupport.removeCounterFromPermanent(
                gameData, referenced, typedEffect.counterType(), typedEffect.amount());
    }

    private Permanent findAttached(GameData gameData, StackEntry entry) {
        Permanent source = findPermanent(gameData, entry.getSourcePermanentId());
        if (source == null || !source.isAttached()) {
            return null;
        }
        return findPermanent(gameData, source.getAttachedTo());
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        return permanentId == null ? null : gameQueryService.findPermanentById(gameData, permanentId);
    }

    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        if (cardId == null) {
            return null;
        }
        return gameData.playerBattlefields.values().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(java.util.Collection::stream)
                .filter(permanent -> cardId.equals(permanent.getCard().getId())
                        || (permanent.getOriginalCard() != null
                        && cardId.equals(permanent.getOriginalCard().getId())))
                .findFirst()
                .orElse(null);
    }
}
