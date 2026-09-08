package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TapAndChooseOpponentGainsControlOfSourceAndMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

/**
 * Resolves the non-targeting opponent choice for a tap-and-control effect.
 */
@Component
@RequiredArgsConstructor
public class TapAndChooseOpponentGainsControlOfSourceAndMatchingPermanentsEffectHandler
        implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapAndChooseOpponentGainsControlOfSourceAndMatchingPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var tapAndControl = (TapAndChooseOpponentGainsControlOfSourceAndMatchingPermanentsEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent source = sourcePermanentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, sourcePermanentId);

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());
        LinkedHashSet<UUID> affectedPermanentIds = new LinkedHashSet<>();
        if (source != null) {
            affectedPermanentIds.add(source.getId());
        }
        gameData.forEachPermanent((playerId, permanent) -> {
            if (predicateEvaluationService.matchesPermanentPredicate(
                    permanent, tapAndControl.filter(), filterContext)) {
                affectedPermanentIds.add(permanent.getId());
            }
        });

        if (affectedPermanentIds.isEmpty()) {
            return;
        }

        for (UUID affectedPermanentId : affectedPermanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, affectedPermanentId);
            if (permanent != null) {
                tapUntapSupport.tapPermanent(gameData, permanent);
            }
        }

        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .toList();
        if (opponents.isEmpty()) {
            return;
        }

        if (opponents.size() == 1) {
            completeChoice(gameData, opponents.getFirst(), new PermanentChoiceContext
                    .ChooseOpponentGainsControlOfSourceAndMatchingPermanents(
                            entry.getControllerId(), entry.getCard().getName(), new ArrayList<>(affectedPermanentIds)));
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext
                .ChooseOpponentGainsControlOfSourceAndMatchingPermanents(
                        entry.getControllerId(), entry.getCard().getName(), new ArrayList<>(affectedPermanentIds)));
        playerInputService.beginAnyTargetChoice(
                gameData,
                entry.getControllerId(),
                List.of(),
                opponents,
                entry.getCard().getName() + " — choose an opponent.");
    }

    public void completeChoice(GameData gameData, UUID chosenOpponentId,
                               PermanentChoiceContext.ChooseOpponentGainsControlOfSourceAndMatchingPermanents context) {
        if (!gameData.playerIds.contains(chosenOpponentId)
                || chosenOpponentId.equals(context.choosingPlayerId())) {
            return;
        }

        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        for (UUID permanentId : context.affectedPermanentIds()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null) {
                creatureControlService.applyControlEffect(
                        gameData,
                        chosenOpponentId,
                        permanent,
                        controlEffect,
                        ControlDuration.PERMANENT.toEffectDuration(),
                        null,
                        context.sourceCardName());
            }
        }
    }
}
