package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentForTargetingRelayEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Triarch Stalker's non-targeting opponent choice. */
@Component
@RequiredArgsConstructor
public class ChooseOpponentForTargetingRelayEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseOpponentForTargetingRelayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent source = sourcePermanentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }

        List<UUID> opponents = opponentsOf(gameData, entry.getControllerId());
        if (opponents.isEmpty()) {
            return;
        }
        boolean secret = ((ChooseOpponentForTargetingRelayEffect) effect).secret();
        if (opponents.size() == 1) {
            remember(source, opponents.getFirst(), entry.getControllerId(), secret);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ChooseOpponentForTargetingRelay(
                        entry.getControllerId(), sourcePermanentId, entry.getCard().getName(), secret));
        playerInputService.beginPlayerChoice(gameData, entry.getControllerId(), opponents,
                entry.getCard().getName() + " — choose an opponent.");
    }

    public void completeChoice(GameData gameData, UUID chosenOpponentId,
                               PermanentChoiceContext.ChooseOpponentForTargetingRelay context) {
        if (!opponentsOf(gameData, context.controllerId()).contains(chosenOpponentId)) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (source != null) {
            remember(source, chosenOpponentId, context.controllerId(), context.secret());
        }
    }

    private void remember(Permanent source, UUID opponentId, UUID chooserId, boolean secret) {
        source.setRememberedTargetPlayerId(opponentId);
        if (secret) {
            source.getChosenPlayerIds().clear();
            source.getChosenPlayerIds().add(opponentId);
            source.getProtectionFromPlayerIdsPermanently().clear();
            source.getProtectionFromPlayerIdsPermanently().add(opponentId);
            source.setAsEntersPlayerChooserId(chooserId);
        }
    }

    private List<UUID> opponentsOf(GameData gameData, UUID controllerId) {
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
    }
}
