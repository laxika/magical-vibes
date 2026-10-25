package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifePerSubtypeEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrawAndLoseLifePerSubtypeEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawAndLoseLifePerSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DrawAndLoseLifePerSubtypeEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        int count = 0;
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm)
                        && gameQueryService.hasEffectiveSubtype(gameData, perm, e.subtype())) {
                    count++;
                }
            }
        }

        if (count == 0) {
            String logEntry = playerName + " controls no " + e.subtype().getDisplayName() + "s — draws nothing and loses no life.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} controls no {}s for draw/life loss", gameData.id, playerName, e.subtype().getDisplayName());
            return;
        }

        for (int i = 0; i < count; i++) {
            drawService.resolveDrawCard(gameData, controllerId);
        }

        gameLogService.append(gameData, GameLog.builder().text(playerName + " draws " + count + " card" + (count != 1 ? "s" : "") + " (").card(entry.getCard()).text(").").build());
        // The life loss is a step of its own, so a death replacement question its Lich's Duel Mastery sacrifice
        // asks (CR 616.1f) reruns only the loss, not the draws.
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                List.of(new LoseLifeEffect(count, LoseLifeRecipient.CONTROLLER)));
    }
}
