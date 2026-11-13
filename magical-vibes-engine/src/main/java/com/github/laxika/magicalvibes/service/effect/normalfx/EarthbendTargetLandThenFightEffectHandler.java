package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandThenFightEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Earth Rumble's successful Earthbend action and its reflexive fight target choices. */
@Component
@RequiredArgsConstructor
public class EarthbendTargetLandThenFightEffectHandler implements NormalEffectHandlerBean {

    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final EarthbendTargetLandEffectHandler earthbendHandler;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final TargetLegalityService targetLegalityService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EarthbendTargetLandThenFightEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EarthbendTargetLandThenFightEffect earthbend = (EarthbendTargetLandThenFightEffect) effect;
        if (!earthbendHandler.applyEarthbend(
                gameData, entry, new EarthbendTargetLandEffect(earthbend.counterCount()))) {
            return;
        }

        Card sourceCard = entry.getCard();
        UUID controllerId = entry.getControllerId();
        List<UUID> controlledCreatures = validCreatureIds(gameData, controllerId, sourceCard, controllerId);
        List<UUID> opponentCreatures = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .flatMap(playerId -> validCreatureIds(gameData, playerId, sourceCard, controllerId).stream())
                .toList();
        if (opponentCreatures.isEmpty()) {
            return;
        }

        // "When you do" is an ability of its own (CR 603.12): its targets are chosen as it goes on the stack, and its
        // entry records what the resolving entry was about.
        StackEntry ability = ReflexiveAbilitySupport.record(gameData, entry,
                List.of(FightTargetsEffect.ofReflexiveAbilityTargets()));
        if (controlledCreatures.isEmpty()) {
            beginOpponentChoice(gameData, sourceCard, controllerId, opponentCreatures,
                    null, entry.getSourcePermanentId(), ability);
            return;
        }
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.EarthbendThenFightTarget(
                sourceCard, controllerId, entry.getSourcePermanentId(), null, false, ability));
        List<UUID> validPlayerIds = List.of(controllerId);
        playerInputService.beginAnyTargetChoice(gameData, controllerId, controlledCreatures, validPlayerIds,
                sourceCard.getName()
                        + " - Choose up to one creature you control to fight (choose yourself to decline).");
    }

    public void handleTargetChoice(GameData gameData, UUID chosenId,
                                   PermanentChoiceContext.EarthbendThenFightTarget context) {
        if (!context.choosingOpponentTarget()) {
            UUID firstTargetId = chosenId.equals(context.controllerId()) ? null : chosenId;
            List<UUID> opponentCreatures = gameData.orderedPlayerIds.stream()
                    .filter(playerId -> !playerId.equals(context.controllerId()))
                    .flatMap(playerId -> validCreatureIds(gameData, playerId, context.sourceCard(),
                            context.controllerId()).stream())
                    .toList();
            if (opponentCreatures.isEmpty()) {
                finish(gameData);
                return;
            }
            beginOpponentChoice(gameData, context.sourceCard(), context.controllerId(), opponentCreatures,
                    firstTargetId, context.sourcePermanentId(), context.ability());
            return;
        }

        List<UUID> fightTargetIds = context.firstTargetId() == null
                ? List.of(chosenId)
                : List.of(context.firstTargetId(), chosenId);
        StackEntry fightEntry = context.ability();
        fightEntry.chooseTargetIds(fightTargetIds);
        fightEntry.setTargetFilters(context.firstTargetId() == null
                ? List.of(TargetFilters.creatureAnOpponentControls())
                : List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureAnOpponentControls()));
        // Its targets were chosen as it went on the stack (CR 603.3d): the door puts it there with them.
        triggeredAbilityStackService.putMade(gameData, fightEntry);
        triggerCollectionService.checkBecomesTargetOfAbilityTriggers(gameData);
        finish(gameData);
    }

    private void beginOpponentChoice(GameData gameData, Card sourceCard, UUID controllerId,
                                      List<UUID> opponentCreatures, UUID firstTargetId,
                                      UUID sourcePermanentId, StackEntry ability) {
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.EarthbendThenFightTarget(
                sourceCard, controllerId, sourcePermanentId, firstTargetId, true, ability));
        playerInputService.beginPermanentChoice(gameData, controllerId, opponentCreatures,
                sourceCard.getName() + " - Choose target creature an opponent controls.");
    }

    private List<UUID> validCreatureIds(GameData gameData, UUID battlefieldControllerId,
                                        Card sourceCard, UUID abilityControllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(battlefieldControllerId, List.of());
        List<UUID> validIds = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)
                    || targetLegalityService.checkTriggeredPermanentTargetableReason(
                            gameData, permanent, sourceCard, abilityControllerId).isPresent()) {
                continue;
            }
            if (!battlefieldControllerId.equals(abilityControllerId)
                    && gameQueryService.cantBeTargetOfOpponentAbilities(gameData, permanent)) {
                continue;
            }
            validIds.add(permanent.getId());
        }
        return validIds;
    }

    private void finish(GameData gameData) {
        gameData.interaction.clearPermanentChoiceContext();
        gameData.interaction.clearAwaitingInput();
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
