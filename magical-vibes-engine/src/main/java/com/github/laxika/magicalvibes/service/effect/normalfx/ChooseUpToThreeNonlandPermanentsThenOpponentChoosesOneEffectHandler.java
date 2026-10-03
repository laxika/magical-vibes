package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseUpToThreeNonlandPermanentsThenOpponentChoosesOneEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves My Will Is Irresistible's two-stage permanent choice. */
@Component
@RequiredArgsConstructor
public class ChooseUpToThreeNonlandPermanentsThenOpponentChoosesOneEffectHandler
        implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseUpToThreeNonlandPermanentsThenOpponentChoosesOneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.targetsForEffect(effect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        if (controllerId == null || opponentId == null
                || !gameData.playerIds.contains(opponentId)
                || controllerId.equals(opponentId)) {
            return;
        }

        List<UUID> eligibleIds = nonlandPermanentsNotControlledBy(gameData, controllerId);
        if (eligibleIds.isEmpty()) {
            return;
        }

        String sourceName = entry.getCard().getName();
        playerInputService.beginMultiPermanentChoice(
                gameData, controllerId, eligibleIds, 3,
                new MultiPermanentChoiceContext.ChooseUpToThreeNonlandPermanentsThenOpponentChoosesOne(
                        controllerId, opponentId, List.of(), sourceName, false),
                sourceName + " — choose up to three nonland permanents you don't control.");
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.ChooseUpToThreeNonlandPermanentsThenOpponentChoosesOne context) {
        if (!context.opponentChoosing()) {
            List<UUID> stillPresent = permanentIds.stream()
                    .filter(id -> gameQueryService.findPermanentById(gameData, id) != null)
                    .toList();
            if (stillPresent.isEmpty()) {
                return;
            }
            if (stillPresent.size() == 1) {
                applyControlToRest(gameData, context.controllerId(), stillPresent,
                        stillPresent.getFirst(), context.sourceName());
                return;
            }

            playerInputService.beginMultiPermanentChoice(
                    gameData, context.opponentId(), stillPresent, 1,
                    new MultiPermanentChoiceContext.ChooseUpToThreeNonlandPermanentsThenOpponentChoosesOne(
                            context.controllerId(), context.opponentId(), stillPresent,
                            context.sourceName(), true),
                    context.sourceName() + " — choose one permanent to leave behind.");
            return;
        }

        applyControlToRest(gameData, context.controllerId(), context.chosenPermanentIds(),
                permanentIds.getFirst(), context.sourceName());
    }

    private List<UUID> nonlandPermanentsNotControlledBy(GameData gameData, UUID controllerId) {
        List<UUID> eligibleIds = new ArrayList<>();
        gameData.forEachPermanent((currentControllerId, permanent) -> {
            if (!controllerId.equals(currentControllerId) && !gameQueryService.isLand(gameData, permanent)) {
                eligibleIds.add(permanent.getId());
            }
        });
        return eligibleIds;
    }

    private void applyControlToRest(GameData gameData, UUID controllerId, List<UUID> chosenPermanentIds,
                                    UUID leftBehindId, String sourceName) {
        List<Permanent> toSeize = chosenPermanentIds.stream()
                .filter(id -> !id.equals(leftBehindId))
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(permanent -> permanent != null)
                .toList();
        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        for (Permanent permanent : toSeize) {
            creatureControlService.applyControlEffect(gameData, controllerId, permanent,
                    controlEffect, ControlDuration.PERMANENT.toEffectDuration(), null, sourceName);
        }
    }
}
