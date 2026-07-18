package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachTargetAuraToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTargetAuraToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.size() < 2) {
            fizzle(gameData, entry, "invalid targets");
            return;
        }

        Permanent aura = gameQueryService.findPermanentById(gameData, targets.get(0));
        if (aura == null) {
            fizzle(gameData, entry, "Aura no longer on the battlefield");
            return;
        }

        Permanent creature = gameQueryService.findPermanentById(gameData, targets.get(1));
        if (creature == null) {
            fizzle(gameData, entry, "target creature no longer on the battlefield");
            return;
        }

        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(creature.getId());
        // CR 613.7e: an Aura receives a new timestamp each time it becomes attached.
        aura.setTimestamp(gameData.nextTimestamp());

        String logEntry = aura.getCard().getName() + " is now attached to " + creature.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(aura.getCard(), " is now attached to ", creature.getCard(), "."));
        log.info("Game {} - {} attached to {} via {}", gameData.id, aura.getCard().getName(), creature.getCard().getName(), entry.getCard().getName());
    }

    private void fizzle(GameData gameData, StackEntry entry, String reason) {
        String logEntry = entry.getCard().getName() + "'s ability fizzles (" + reason + ").";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(entry.getCard()).text("'s ability fizzles (" + reason + ").").build());
        log.info("Game {} - Crown of the Ages ability fizzles: {}", gameData.id, reason);
    }
}
