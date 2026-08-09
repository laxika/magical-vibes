package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOneOfTwoTargetCreaturesThenPutCountersOnOtherEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Cannibalize's choice between two targeted creatures. */
@Component
@RequiredArgsConstructor
public class ExileOneOfTwoTargetCreaturesThenPutCountersOnOtherEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOneOfTwoTargetCreaturesThenPutCountersOnOtherEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> legalTargets = entry.getTargetIds().stream()
                .filter(targetId -> gameQueryService.findPermanentById(gameData, targetId) != null)
                .toList();

        if (legalTargets.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " fizzles (no legal targets)."));
            return;
        }

        if (legalTargets.size() == 1) {
            Permanent only = gameQueryService.findPermanentById(gameData, legalTargets.getFirst());
            exileSupport.exilePermanentAndLog(gameData, only, entry.getCard().getName());
            return;
        }

        if (legalTargets.size() != 2) {
            return;
        }

        UUID firstController = gameQueryService.findPermanentController(gameData, legalTargets.getFirst());
        UUID secondController = gameQueryService.findPermanentController(gameData, legalTargets.get(1));
        if (firstController == null || !firstController.equals(secondController)) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " fizzles (targets are not controlled by the same player)."));
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.CannibalizeChoice(
                entry.getCard(), entry.getControllerId(), legalTargets.getFirst(), legalTargets.get(1)));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), legalTargets,
                "Choose a creature to exile. The other gets two +1/+1 counters.");
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                + " chooses which creature to exile for " + entry.getCard().getName() + "."));
    }
}
