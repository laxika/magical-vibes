package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.NihiloorTapAndStealEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostTriggeringPermanentPowerPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Nihiloor's per-opponent tap choice and reflexive control ability. */
@Component
@RequiredArgsConstructor
public class NihiloorTapAndStealEffectHandler implements NormalEffectHandlerBean {

    private static final GainControlOfTargetEffect GAIN_CONTROL = GainControlOfTargetEffect.withTargetPredicate(
            ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD,
            new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentControlledByTriggeringPlayerPredicate(),
                    new PermanentPowerAtMostTriggeringPermanentPowerPredicate())));

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final QueueReflexiveAbilityEffectHandler queueReflexiveAbilityEffectHandler;
    private final TapUntapSupport tapUntapSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return NihiloorTapAndStealEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        NihiloorTapAndStealEffect tapEffect = (NihiloorTapAndStealEffect) effect;
        List<UUID> eligibleIds = untappedCreatures(gameData, entry.getControllerId());
        if (eligibleIds.isEmpty()) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                entry.getControllerId(),
                eligibleIds,
                1,
                new MultiPermanentChoiceContext.NihiloorTapChoice(entry, tapEffect.opponentId()),
                "Nihiloor — choose up to one untapped creature you control to tap.");
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.NihiloorTapChoice context) {
        if (permanentIds.size() == 1) {
            Permanent tappedCreature = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            UUID controllerId = context.resolvingEntry().getControllerId();
            if (tappedCreature != null
                    && !tappedCreature.isTapped()
                    && gameQueryService.isCreature(gameData, tappedCreature)
                    && controllerId.equals(gameQueryService.findPermanentController(gameData, tappedCreature.getId()))
                    && tapUntapSupport.tapPermanent(gameData, tappedCreature)) {
                // "That player" and "the tapped creature" of the reflexive ability are its triggering player and
                // permanent: its target is chosen, and judged again as it resolves, against the tapped creature's
                // power as it is then (CR 608.2h).
                queueReflexiveAbilityEffectHandler.resolve(
                        gameData,
                        context.resolvingEntry(),
                        new QueueReflexiveAbilityEffect(GAIN_CONTROL),
                        record -> {
                            record.setTriggeringPlayerId(context.opponentId());
                            record.setTriggeringPermanentId(tappedCreature.getId());
                            record.setTriggeringPermanent(tappedCreature);
                        });
            }
        }

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }

    private List<UUID> untappedCreatures(GameData gameData, UUID controllerId) {
        List<UUID> result = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if (!permanent.isTapped() && gameQueryService.isCreature(gameData, permanent)
                    && !gameQueryService.cantBeChosen(gameData, permanent)) {
                result.add(permanent.getId());
            }
        }
        return result;
    }
}
