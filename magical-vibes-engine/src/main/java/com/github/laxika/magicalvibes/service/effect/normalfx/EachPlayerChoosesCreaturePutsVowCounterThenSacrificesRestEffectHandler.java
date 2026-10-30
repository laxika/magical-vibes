package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreaturePutsVowCounterThenSacrificesRestEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Promise of Loyalty's per-player keep, counter, and sacrifice sequence. */
@Component
@RequiredArgsConstructor
@Slf4j
public class EachPlayerChoosesCreaturePutsVowCounterThenSacrificesRestEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesCreaturePutsVowCounterThenSacrificesRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        step(gameData, entry, effect, apnapPlayers(gameData), 0, List.of());
    }

    /** Continues the APNAP keep choices after one player selects a creature. */
    public void completeChoice(GameData gameData, List<UUID> chosenIds,
            MultiPermanentChoiceContext.EachPlayerChoosesCreaturePutsVowCounterChoice context) {
        List<UUID> keptIds = new ArrayList<>(context.keptIds());
        keptIds.addAll(chosenIds);
        step(gameData, context.resolvingEntry(), null, context.playerIds(), context.playerIndex() + 1, keptIds);
    }

    /** @param effect the resolving step when no choice has been asked yet, {@code null} from a choice completion */
    private void step(GameData gameData, StackEntry entry, CardEffect effect, List<UUID> playerIds, int playerIndex,
            List<UUID> keptIds) {
        List<UUID> choices = new ArrayList<>(keptIds);
        int index = playerIndex;
        while (index < playerIds.size()) {
            UUID playerId = playerIds.get(index);
            List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, playerId,
                    permanent -> !gameQueryService.cantBeChosen(gameData, permanent));
            if (creatureIds.isEmpty()) {
                index++;
                continue;
            }
            if (creatureIds.size() == 1) {
                choices.add(creatureIds.getFirst());
                index++;
                continue;
            }

            playerInputService.beginMultiPermanentChoice(gameData, playerId, creatureIds, 1,
                    new MultiPermanentChoiceContext.EachPlayerChoosesCreaturePutsVowCounterChoice(
                            entry, playerIds, index, choices, entry.getCard().getName()),
                    entry.getCard().getName() + " - choose a creature to receive a vow counter and keep.");
            return;
        }

        resolveChoices(gameData, entry, effect, playerIds, choices);
    }

    private void resolveChoices(GameData gameData, StackEntry entry, CardEffect effect, List<UUID> playerIds,
            List<UUID> keptIds) {
        Set<UUID> kept = new HashSet<>(keptIds);
        for (UUID keptId : keptIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, keptId);
            if (permanent != null) {
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, entry, permanent, CounterType.VOW, 1);
                restrictWhileVowCounterRemains(gameData, entry, permanent);
            }
        }

        List<UUID> toSacrifice = new ArrayList<>();
        for (UUID playerId : playerIds) {
            if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, entry.getControllerId())) {
                continue;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (!kept.contains(permanent.getId())
                        && gameQueryService.isCreature(gameData, permanent)
                        && !gameQueryService.cantBeSacrificed(gameData, permanent)) {
                    toSacrifice.add(permanent.getId());
                }
            }
        }

        if (toSacrifice.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getCard().getName() + " resolves but no creatures are sacrificed."));
            return;
        }
        // The vow counters are already on, so the sacrifice is a step of its own (this step, or the step the parked
        // resolution resumes with) and a CR 616.1 question is asked before any creature leaves.
        List<DepartureIntent> intents = StepDepartureSupport.sacrifices(toSacrifice);
        if (effect != null) {
            stepDepartureSupport.depart(gameData, entry, effect, DepartureBatch.of(intents));
        } else if (!stepDepartureSupport.departAfterChoice(gameData, intents, List.of())) {
            destructionSupport.performSimultaneousSacrifice(gameData, toSacrifice);
        }
        log.info("Game {} - {} sacrifices {} creatures", gameData.id, entry.getCard().getName(),
                toSacrifice.size());
    }

    /**
     * "Each of those creatures can't attack you or planeswalkers you control for as long as it has a vow
     * counter on it": bound to this creature and to the counter just placed, so the restriction ends for
     * good when that counter is removed and never passes to another creature that later gets a vow counter.
     */
    private void restrictWhileVowCounterRemains(GameData gameData, StackEntry entry, Permanent permanent) {
        if (permanent.getCounterCount(CounterType.VOW) <= 0) {
            return;
        }
        PermanentPredicate scope = new PermanentAllOfPredicate(List.of(
                new PermanentIsSpecificPermanentPredicate(permanent.getId()),
                new PermanentHasCountersPredicate(CounterType.VOW,
                        permanent.getLastCounterRemovalVersions().getOrDefault(CounterType.VOW, 0L))));
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), entry.getCard().getName(), null,
                entry.getControllerId(),
                new CreaturesCantAttackControllerUnlessPredicateEffect(
                        new PermanentNotPredicate(new PermanentTruePredicate()), true),
                null, null, scope, EffectDuration.PERMANENT, 0, true));
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> orderedPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayers.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return orderedPlayers;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayers.subList(activeIndex, orderedPlayers.size()));
        rotated.addAll(orderedPlayers.subList(0, activeIndex));
        return rotated;
    }
}
