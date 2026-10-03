package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Orzhov Advokist's sequential optional player choices. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect counterEffect =
                (EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect) effect;
        List<UUID> players = counterEffect.remainingPlayerIds().isEmpty()
                ? apnapPlayers(gameData)
                : counterEffect.remainingPlayerIds();
        promptNext(gameData, entry.getCard(), new EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect(
                counterEffect.counterType(), counterEffect.count(), players, entry.getControllerId(), counterEffect.acceptedFollowUp()),
                entry.getSourcePermanentId());
    }

    public List<UUID> creatureIds(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    /** Applies the accepted counter placement and its resulting attack restriction. */
    public void accept(GameData gameData, PendingMayAbility ability, UUID permanentId) {
        EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect effect = effect(ability);
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null || !gameQueryService.isCreature(gameData, target)
                || !ability.controllerId().equals(gameQueryService.findPermanentController(gameData, permanentId))) {
            return;
        }

        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            return;
        }
        StackEntry placementEntry = new StackEntry(pendingEntry);
        placementEntry.setControllerId(ability.controllerId());
        int placed = permanentCounterSupport.placeCounterOnPermanent(
                gameData, placementEntry, target, effect.counterType(), effect.count());
        if (placed > 0 && effect.acceptedFollowUp() instanceof com.github.laxika.magicalvibes.model.effect.GoadTriggeringCreatureUntilNextTurnEffect) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), ability.sourceCard().getName(), ability.sourcePermanentId(),
                    effect.sourceControllerId(), effect.acceptedFollowUp(), target.getId(), null, null,
                    EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));
        } else if (placed > 0) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), ability.sourceCard().getName(), ability.sourcePermanentId(),
                    effect.sourceControllerId(),
                    new CreaturesCantAttackControllerUnlessPredicateEffect(
                            new PermanentNotPredicate(new PermanentTruePredicate()), true,
                            ability.controllerId()),
                    null, effect.sourceControllerId(), null,
                    EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));
        }
    }

    /** Advances to the next player after the current optional choice. */
    public void advance(GameData gameData, Card sourceCard,
                        EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect effect,
                        UUID sourcePermanentId) {
        List<UUID> remaining = effect.remainingPlayerIds().size() <= 1
                ? List.of()
                : effect.remainingPlayerIds().subList(1, effect.remainingPlayerIds().size());
        promptNext(gameData, sourceCard,
                new EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect(
                        effect.counterType(), effect.count(), remaining, effect.sourceControllerId(), effect.acceptedFollowUp()),
                sourcePermanentId);
    }

    public EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect effect(PendingMayAbility ability) {
        return ability.effects().stream()
                .filter(EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect.class::isInstance)
                .map(EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect.class::cast)
                .findFirst()
                .orElseThrow();
    }

    public void completeCreatureChoice(GameData gameData, UUID permanentId,
                                       PermanentChoiceContext.OrzhovAdvokistCreatureChoice context) {
        PendingMayAbility ability = context.ability();
        accept(gameData, ability, permanentId);
        advance(gameData, ability.sourceCard(), context.effect(), ability.sourcePermanentId());
    }

    private void promptNext(GameData gameData, Card sourceCard,
                            EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect effect,
                            UUID sourcePermanentId) {
        List<UUID> remaining = new ArrayList<>(effect.remainingPlayerIds());
        while (!remaining.isEmpty()) {
            UUID playerId = remaining.getFirst();
            if (!gameData.playerIds.contains(playerId) || creatureIds(gameData, playerId).isEmpty()) {
                remaining.removeFirst();
                continue;
            }

            EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect current =
                    new EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect(
                            effect.counterType(), effect.count(), remaining, effect.sourceControllerId(), effect.acceptedFollowUp());
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    sourceCard, playerId, List.of(current),
                    sourceCard.getName() + " - You may put " + effect.count() + " "
                            + effect.counterType().name().toLowerCase() + " counters on a creature you control.",
                    null, null, sourcePermanentId));
            return;
        }
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
            return rotated;
        }
        return ordered;
    }
}
