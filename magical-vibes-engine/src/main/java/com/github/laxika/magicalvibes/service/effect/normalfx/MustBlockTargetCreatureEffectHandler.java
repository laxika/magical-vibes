package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MustBlockTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MustBlockTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MustBlockTargetCreatureEffect) effect;

        List<UUID> blockerGroup = entry.targetsForGroup(e.blockerTargetGroup());
        List<UUID> blockedGroup = entry.targetsForGroup(e.blockedTargetGroup());
        if (blockerGroup.isEmpty() || blockedGroup.isEmpty()) {
            return;
        }

        Permanent blocker = gameQueryService.findPermanentById(gameData, blockerGroup.getFirst());
        Permanent blocked = gameQueryService.findPermanentById(gameData, blockedGroup.getFirst());
        if (blocker == null || blocked == null) {
            return;
        }

        blocker.getMustBlockIds().add(blocked.getId());

        String logEntry = blocker.getCard().getName() + " must block " + blocked.getCard().getName()
                + " this turn if able.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} must block {} this turn if able", gameData.id,
                blocker.getCard().getName(), blocked.getCard().getName());
    }
}
