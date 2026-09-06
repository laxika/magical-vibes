package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrawCardForTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardForTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DrawCardForTargetPlayerEffect) effect;

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;

        // Recheck the untapped condition against the live source or its last known state.
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (e.requireSourceUntapped() && source != null && source.isTapped()) {
            log.info("Game {} - {}'s draw trigger does nothing (source is tapped)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));

        // Prefer the effect's bound target group so fuse / multi-group spells only draw for this
        // half's player(s). Unbound multi-target ("any number of target players each draw…") still
        // fans over the flat list via targetsForEffect; single-target casts fall back to targetId.
        List<UUID> targetPlayerIds = e.targetGroup() >= 0
                ? entry.targetsForGroup(e.targetGroup())
                : entry.targetsForEffect(effect);
        if (e.targetGroup() < 0 && targetPlayerIds.isEmpty() && entry.getTargetId() != null) {
            targetPlayerIds = Collections.singletonList(entry.getTargetId());
        }
        for (UUID targetPlayerId : targetPlayerIds) {
            if (!gameData.playerIds.contains(targetPlayerId)) {
                continue;
            }
            for (int i = 0; i < amount; i++) {
                drawService.resolveDrawCard(gameData, targetPlayerId);
            }
        }
    }
}
