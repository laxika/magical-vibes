package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyRemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Perpetually removes a keyword from the resolving card. The loss is newer than any keyword grant
 * already on the source permanent, so those grants lose the keyword too.
 */
@Component
@RequiredArgsConstructor
public class PerpetuallyRemoveKeywordEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyRemoveKeywordEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var removal = (PerpetuallyRemoveKeywordEffect) effect;
        if (entry.getCard() == null) {
            return;
        }
        gameData.perpetualCardRemovedKeywords
                .computeIfAbsent(entry.getCard().getId(), ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(removal.keyword());

        Permanent permanent = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (permanent != null) {
            removeExistingGrants(gameData, permanent, removal.keyword());
        }
    }

    private void removeExistingGrants(GameData gameData, Permanent permanent, Keyword keyword) {
        permanent.getGrantedKeywords().remove(keyword);
        permanent.getPersistentGrantedKeywords().remove(keyword);
        permanent.getUntilNextTurnKeywords().remove(keyword);
        synchronized (gameData.floatingEffects) {
            for (int i = 0; i < gameData.floatingEffects.size(); i++) {
                FloatingContinuousEffect floating = gameData.floatingEffects.get(i);
                if (!permanent.getId().equals(floating.affectedPermanentId())
                        || !(floating.effect() instanceof GrantKeywordEffect grant)
                        || !grant.keywords().contains(keyword)) {
                    continue;
                }
                Set<Keyword> remaining = new HashSet<>(grant.keywords());
                remaining.remove(keyword);
                if (remaining.isEmpty()) {
                    gameData.floatingEffects.remove(i);
                    i--;
                    continue;
                }
                gameData.floatingEffects.set(i, new FloatingContinuousEffect(
                        floating.id(), floating.sourceCardName(), floating.sourcePermanentId(),
                        floating.controllerId(),
                        new GrantKeywordEffect(remaining, grant.scope(), grant.filter(), grant.duration(),
                                grant.grantCondition()),
                        floating.affectedPermanentId(), floating.affectedPlayerId(), floating.scope(),
                        floating.duration(), floating.timestamp(), floating.expiresWhenScopeNoLongerMatches()));
            }
        }
    }
}
