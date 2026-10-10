package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetCreatureBecomesSubtypeUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureBecomesSubtypeUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetCreatureBecomesSubtypeUntilEndOfTurnEffect) effect;
        if (e.scope() == GrantScope.TARGET_PLAYERS_CREATURES) {
            UUID targetPlayerId = entry.getTargetId();
            if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
                return;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
            int count = 0;
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (gameQueryService.isCreature(gameData, permanent)) {
                        setCreatureTypeOverride(gameData, entry, permanent, e.subtypes());
                        count++;
                    }
                }
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" makes " + count + " creature(s) into " + e.subtype().getDisplayName()
                            + "s until end of turn.").build());
            return;
        }

        if (e.scope() == GrantScope.OWN_CREATURES) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
            int count = 0;
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (gameQueryService.isCreature(gameData, permanent)) {
                        setCreatureTypeOverride(gameData, entry, permanent, e.subtypes());
                        count++;
                    }
                }
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" makes " + count + " creature(s) into " + e.subtype().getDisplayName()
                            + "s until end of turn.").build());
            return;
        }

        List<UUID> targetIds = entry.targetsForEffect(effect);
        UUID targetId = entry.getTargetId() != null ? entry.getTargetId()
                : targetIds.isEmpty() ? null : targetIds.getFirst();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }
        setCreatureTypeOverride(gameData, entry, target, e.subtypes());
        gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(" becomes a "
                + subtypeNames(e.subtypes()) + " until end of turn.").build());
    }

    /**
     * Sets the legacy override fields (read by direct {@code Permanent} readers) and registers the
     * same change as timestamped floating layer-4 effects (CR 613.1d, 613.7), so a type-adding
     * effect that begins later (Xenograft) is not stripped by this one.
     */
    private void setCreatureTypeOverride(GameData gameData, StackEntry entry, Permanent permanent,
                                         List<CardSubtype> subtypes) {
        if (subtypes.size() == 1) {
            permanent.setTransientCreatureTypeOverride(subtypes.getFirst());
            permanent.getTransientCreatureTypeOverrides().clear();
        } else {
            permanent.setTransientCreatureTypeOverride(null);
            permanent.getTransientCreatureTypeOverrides().clear();
            permanent.getTransientCreatureTypeOverrides().addAll(subtypes);
        }
        for (int i = 0; i < subtypes.size(); i++) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                    entry.getControllerId(),
                    new GrantSubtypeEffect(subtypes.get(i), GrantScope.TARGET, i == 0),
                    permanent.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
        }
    }

    private String subtypeNames(List<CardSubtype> subtypes) {
        return subtypes.stream()
                .map(CardSubtype::getDisplayName)
                .reduce((left, right) -> left + " " + right)
                .orElseThrow();
    }
}
