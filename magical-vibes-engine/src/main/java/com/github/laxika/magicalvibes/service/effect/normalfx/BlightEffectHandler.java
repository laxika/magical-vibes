package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the blight action after its optional may choice has been accepted. */
@Component
@RequiredArgsConstructor
public class BlightEffectHandler implements NormalEffectHandlerBean {

    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PermanentCounterSupport permanentCounterSupport;
    @Autowired
    @Lazy
    private EffectResolutionService effectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BlightEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        BlightEffect blight = (BlightEffect) effect;
        resolveForPlayer(gameData, entry, blight, entry.getControllerId());
    }

    public void resolveForPlayer(GameData gameData, StackEntry entry, BlightEffect blight,
                                 UUID controllerId) {
        List<UUID> creatureIds = controlledCreatureIds(gameData, controllerId);

        if (creatureIds.isEmpty()) {
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                placeCountersAndQueueThen(gameData, entry, creature, blight);
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.BlightCreatureChoice(
                controllerId, entry.getCard(), entry.getSourcePermanentId(), blight));
        playerInputService.beginPermanentChoice(gameData, controllerId, creatureIds,
                entry.getCard().getName() + " - Choose a creature to blight.");
    }

    public void placeCountersAndQueueThen(GameData gameData, StackEntry entry, Permanent creature,
                                          BlightEffect blight) {
        if (gameQueryService.cantHaveCounters(gameData, creature)
                || gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, creature)) {
            return;
        }
        permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, creature, CounterType.MINUS_ONE_MINUS_ONE, blight.count());
        if (blight.thenEffect() == null) {
            return;
        }

        if (blight.thenEffectTargets()) {
            queueTargetedReflexiveAbility(gameData, entry, blight.thenEffect());
            return;
        }

        StackEntry followUp = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getDescription(),
                new ArrayList<>(List.of(blight.thenEffect())),
                creature.getId(),
                entry.getSourcePermanentId());
        followUp.setNonTargeting(true);
        followUp.setTriggeringPermanentId(creature.getId());
        followUp.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        effectResolutionService.resolveEffects(gameData, followUp);
    }

    private void queueTargetedReflexiveAbility(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        // "When you do" is an ability of its own (CR 603.12): the stack door chooses its target as it goes on the stack,
        // after the resolution and the state-based actions (CR 704.3), so a creature the blight put to zero toughness
        // can't be chosen.
        triggeredAbilityStackService.putMade(gameData, ReflexiveAbilitySupport.record(gameData, entry, List.of(thenEffect)));
    }

    private List<UUID> controlledCreatureIds(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> !gameQueryService.cantHaveCounters(gameData, permanent))
                .filter(permanent -> !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent))
                .filter(permanent -> !gameQueryService.cantBeChosen(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }
}
