package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromPlayersEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromChosenPlayerToControllerAndTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;

@Component
@RequiredArgsConstructor
public class GrantProtectionFromChosenPlayerToControllerAndTargetPermanentUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionFromChosenPlayerToControllerAndTargetPermanentUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null && entry.getSourcePermanentSnapshot() != null) {
            source = new Permanent(entry.getSourcePermanentSnapshot());
        }
        if (source == null) {
            return;
        }

        UUID chosenPlayerId = source.getChosenPlayerIds().stream().findFirst().orElse(null);
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (chosenPlayerId == null || target == null
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, target.getId()))) {
            return;
        }

        gameData.addEffectOnPermanent(entry, new GrantEffectEffect(new ProtectionFromPlayersEffect(Set.of(chosenPlayerId), false), Subjects.target()),
                target.getId(), Duration.UNTIL_END_OF_TURN);
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(),
                new GrantProtectionFromChosenPlayerToControllerAndTargetPermanentUntilEndOfTurnEffect(chosenPlayerId),
                entry.getControllerId(), Duration.UNTIL_END_OF_TURN));

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                " and " + gameData.playerIdToName.get(entry.getControllerId())
                        + " gain protection from " + gameData.playerIdToName.get(chosenPlayerId)
                        + " until end of turn."));
    }
}
