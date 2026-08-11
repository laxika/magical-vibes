package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOneOfTwoTargetCreaturesThenReturnOtherToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Barrin's Spite's controller-choice sacrifice and subsequent bounce. */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeOneOfTwoTargetCreaturesThenReturnOtherToHandEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeOneOfTwoTargetCreaturesThenReturnOtherToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> legal = new ArrayList<>();
        List<UUID> targets = entry.getTargetIds();
        if (targets != null) {
            for (UUID targetId : targets) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
                if (permanent != null && gameQueryService.isCreature(gameData, permanent)) {
                    legal.add(targetId);
                }
            }
        }

        if (legal.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " fizzles (no legal targets)."));
            return;
        }

        UUID sacrificingPlayerId = gameQueryService.findPermanentController(gameData, legal.getFirst());
        if (legal.size() > 1 && legal.stream()
                .anyMatch(targetId -> !Objects.equals(sacrificingPlayerId,
                        gameQueryService.findPermanentController(gameData, targetId)))) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " fizzles (targets are no longer controlled by the same player)."));
            return;
        }

        if (legal.size() == 1) {
            Permanent only = gameQueryService.findPermanentById(gameData, legal.getFirst());
            destructionSupport.sacrificeAndLog(gameData, only, sacrificingPlayerId);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SacrificeOneOfTwoThenReturnOtherToHand(
                sacrificingPlayerId, entry.getCard(), entry.getControllerId(), legal.get(0), legal.get(1)));
        playerInputService.beginPermanentChoice(gameData, sacrificingPlayerId, legal,
                "Choose a creature to sacrifice. Return the other to its owner's hand.");
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(sacrificingPlayerId)
                + " must sacrifice one of the two creatures targeted by " + entry.getCard().getName() + "."));
    }
}
