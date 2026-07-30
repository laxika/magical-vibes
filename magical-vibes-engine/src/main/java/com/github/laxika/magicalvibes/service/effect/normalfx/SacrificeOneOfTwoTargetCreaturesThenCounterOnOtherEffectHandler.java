package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOneOfTwoTargetCreaturesThenCounterOnOtherEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SacrificeOneOfTwoTargetCreaturesThenCounterOnOtherEffect}: the opponent who
 * controls the two targeted creatures chooses one of them and sacrifices it, and the other gets a
 * -1/-1 counter.
 *
 * <p>Targets that are no longer on the battlefield, or that the targeting player has since gained
 * control of, drop out of the choice. With a single target left the ruling forces that one to be
 * sacrificed, so no choice is asked for and nothing receives a counter.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeOneOfTwoTargetCreaturesThenCounterOnOtherEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeOneOfTwoTargetCreaturesThenCounterOnOtherEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> legal = new ArrayList<>();
        List<UUID> targets = entry.getTargetIds();
        if (targets != null) {
            for (UUID targetId : targets) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
                if (permanent == null) {
                    continue;
                }
                UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
                if (controllerId == null || controllerId.equals(entry.getControllerId())) {
                    continue;
                }
                legal.add(targetId);
            }
        }

        if (legal.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " fizzles (no legal targets)."));
            return;
        }

        UUID sacrificingPlayerId = gameQueryService.findPermanentController(gameData, legal.getFirst());

        if (legal.size() == 1) {
            Permanent only = gameQueryService.findPermanentById(gameData, legal.getFirst());
            destructionSupport.sacrificeAndLog(gameData, only, sacrificingPlayerId);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SacrificeOneOfTwoThenCounterOnOther(
                sacrificingPlayerId, entry.getCard(), entry.getControllerId(), legal.get(0), legal.get(1)));
        playerInputService.beginPermanentChoice(gameData, sacrificingPlayerId, legal,
                "Choose a creature to sacrifice. The other gets a -1/-1 counter.");
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(sacrificingPlayerId)
                + " must sacrifice one of the two creatures targeted by " + entry.getCard().getName() + "."));
    }
}
