package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTokensCreatedWithSourceEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyTokensCreatedWithSourceEffect}: destroys every battlefield permanent
 * registered under the source in {@code GameData.sourceCreatedTokens}, no matter who controls it
 * now. The source id comes from the effect when a leaves-the-battlefield collector baked it in,
 * otherwise from the stack entry.
 */
@Component
@RequiredArgsConstructor
public class DestroyTokensCreatedWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTokensCreatedWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var destroy = (DestroyTokensCreatedWithSourceEffect) effect;

        UUID sourceId = destroy.sourcePermanentId() != null
                ? destroy.sourcePermanentId()
                : entry.getSourcePermanentId();
        if (sourceId == null) {
            return;
        }
        Set<UUID> created = gameData.sourceCreatedTokens.get(sourceId);
        if (created == null || created.isEmpty()) {
            return;
        }

        List<Permanent> victims = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent perm : battlefield) {
                if (created.contains(perm.getId())) {
                    victims.add(perm);
                }
            }
        }
        for (Permanent victim : victims) {
            destructionSupport.tryDestroyAndLog(gameData, victim, entry.getCard().getName(),
                    destroy.cannotBeRegenerated());
        }
        created.removeAll(victims.stream().map(Permanent::getId).toList());
    }
}
