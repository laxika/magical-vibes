package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayForArtifactsOrCreaturesCantAttackThisCombatEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MayPayForArtifactsOrCreaturesCantAttackThisCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayForArtifactsOrCreaturesCantAttackThisCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.resolvedMayAccepted != null) {
            boolean accepted = gameData.resolvedMayAccepted;
            gameData.resolvedMayAccepted = null;
            if (!accepted) {
                gameData.creaturesCantAttackThisCombat = true;
            }
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null || source.getAttachedTo() == null
                || !gameData.playerIds.contains(source.getAttachedTo())) {
            return;
        }

        UUID enchantedPlayerId = source.getAttachedTo();
        int artifactCount = gameData.playerBattlefields.getOrDefault(enchantedPlayerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isArtifact(gameData, permanent))
                .mapToInt(permanent -> 1)
                .sum();
        String manaCost = artifactCount == 0
                ? "{0}"
                : "{1}".repeat(artifactCount);

        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                enchantedPlayerId,
                List.of(),
                entry.getCard().getName() + " - Pay " + manaCost + "?",
                entry.getTargetId(),
                manaCost,
                entry.getSourcePermanentId(),
                null,
                0,
                0,
                entry.getAttackedTargetId(),
                entry.getActivePlayerId(),
                null,
                entry.getSourcePermanentSnapshot(),
                entry.getControllerId(),
                entry.getTriggeringCardId(),
                entry.getEventValue()));
    }
}
