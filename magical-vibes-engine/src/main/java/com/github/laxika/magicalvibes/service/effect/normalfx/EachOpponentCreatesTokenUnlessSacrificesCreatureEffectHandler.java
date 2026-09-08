package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatesTokenUnlessSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Acererak-style per-opponent sacrifice-or-token choices in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachOpponentCreatesTokenUnlessSacrificesCreatureEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentCreatesTokenUnlessSacrificesCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachOpponentCreatesTokenUnlessSacrificesCreatureEffect tokenEffect =
                (EachOpponentCreatesTokenUnlessSacrificesCreatureEffect) effect;
        beginNextOpponent(gameData, entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(),
                entry.getSourcePermanentSnapshot(), tokenEffect.token(), apnapOpponents(gameData, entry.getControllerId()),
                entry);
    }

    /** Completes the may choice after an opponent decides whether to sacrifice a creature. */
    public void resolveMayChoice(GameData gameData, PendingMayAbility ability, boolean accepted,
                                 EachOpponentCreatesTokenUnlessSacrificesCreatureEffect effect) {
        UUID sacrificingPlayerId = ability.controllerId();
        UUID sourceControllerId = sourceControllerId(gameData, ability);
        List<UUID> remainingOpponentIds = remainingOpponents(gameData, sourceControllerId, sacrificingPlayerId);
        List<UUID> creatureIds = eligibleCreatureIds(gameData, sacrificingPlayerId, sourceControllerId);

        if (accepted && !creatureIds.isEmpty()) {
            if (creatureIds.size() == 1) {
                Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
                if (creature != null) {
                    destructionSupport.sacrificeAndLog(gameData, creature, sacrificingPlayerId);
                    continueWithOpponents(gameData, sourceControllerId, ability.sourceCard(),
                            ability.sourcePermanentId(), ability.sourcePermanentSnapshot(), effect.token(),
                            remainingOpponentIds, null);
                    return;
                }
            } else {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.EachOpponentCreatesTokenUnlessSacrificesCreature(
                                sacrificingPlayerId, sourceControllerId, ability.sourceCard(),
                                ability.sourcePermanentId(), ability.sourcePermanentSnapshot(), effect.token(),
                                remainingOpponentIds));
                playerInputService.beginPermanentChoice(gameData, sacrificingPlayerId, creatureIds,
                        ability.sourceCard().getName() + " - Choose a creature to sacrifice.");
                return;
            }
        }

        createToken(gameData, sourceEntry(ability.sourceCard(), sourceControllerId, ability.sourcePermanentId(),
                ability.sourcePermanentSnapshot()), effect.token());
        continueWithOpponents(gameData, sourceControllerId, ability.sourceCard(), ability.sourcePermanentId(),
                ability.sourcePermanentSnapshot(), effect.token(), remainingOpponentIds, null);
    }

    /** Completes the creature choice and continues with the remaining opponents. */
    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.EachOpponentCreatesTokenUnlessSacrificesCreature context) {
        Permanent creature = gameQueryService.findPermanentById(gameData, permanentId);
        boolean canSacrifice = creature != null
                && context.sacrificingPlayerId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                && gameQueryService.isCreature(gameData, creature)
                && !gameQueryService.cantBeSacrificed(gameData, creature)
                && gameQueryService.canEffectCauseSacrifice(gameData, context.sacrificingPlayerId(),
                context.sourceControllerId());
        if (canSacrifice) {
            destructionSupport.sacrificeAndLog(gameData, creature, context.sacrificingPlayerId());
        } else {
            createToken(gameData, sourceEntry(context.sourceCard(), context.sourceControllerId(),
                    context.sourcePermanentId(), context.sourcePermanentSnapshot()), context.token());
        }
        continueWithOpponents(gameData, context.sourceControllerId(), context.sourceCard(),
                context.sourcePermanentId(), context.sourcePermanentSnapshot(), context.token(),
                context.remainingOpponentIds(), null);
    }

    private void beginNextOpponent(GameData gameData, UUID sourceControllerId, Card sourceCard,
                                   UUID sourcePermanentId, Permanent sourcePermanentSnapshot,
                                   CreateTokenEffect token, List<UUID> remainingOpponentIds,
                                   StackEntry tokenSourceEntry) {
        continueWithOpponents(gameData, sourceControllerId, sourceCard, sourcePermanentId,
                sourcePermanentSnapshot, token, remainingOpponentIds, tokenSourceEntry);
    }

    private void continueWithOpponents(GameData gameData, UUID sourceControllerId, Card sourceCard,
                                       UUID sourcePermanentId, Permanent sourcePermanentSnapshot,
                                       CreateTokenEffect token, List<UUID> remainingOpponentIds,
                                       StackEntry tokenSourceEntry) {
        List<UUID> remaining = new ArrayList<>(remainingOpponentIds);
        StackEntry activeTokenSource = tokenSourceEntry;
        while (!remaining.isEmpty()) {
            UUID opponentId = remaining.removeFirst();
            List<UUID> creatureIds = eligibleCreatureIds(gameData, opponentId, sourceControllerId);
            if (creatureIds.isEmpty()) {
                if (activeTokenSource == null) {
                    activeTokenSource = sourceEntry(sourceCard, sourceControllerId, sourcePermanentId,
                            sourcePermanentSnapshot);
                }
                createToken(gameData, activeTokenSource, token);
                continue;
            }

            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    sourceCard, opponentId,
                    List.of(new EachOpponentCreatesTokenUnlessSacrificesCreatureEffect(token)),
                    sourceCard.getName() + " - Sacrifice a creature?", null, null, sourcePermanentId,
                    null, 0, 0, null, null, null, sourcePermanentSnapshot, sourceControllerId));
            return;
        }
    }

    private List<UUID> eligibleCreatureIds(GameData gameData, UUID playerId, UUID sourceControllerId) {
        if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, sourceControllerId)) {
            return List.of();
        }
        return destructionSupport.collectCreatureIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent));
    }

    private UUID sourceControllerId(GameData gameData, PendingMayAbility ability) {
        if (ability.sourceControllerId() != null) {
            return ability.sourceControllerId();
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, ability.sourcePermanentId());
        return controllerId != null ? controllerId : ability.controllerId();
    }

    private List<UUID> remainingOpponents(GameData gameData, UUID sourceControllerId, UUID currentOpponentId) {
        List<UUID> opponents = apnapOpponents(gameData, sourceControllerId);
        int currentIndex = opponents.indexOf(currentOpponentId);
        return currentIndex < 0 ? List.of() : opponents.subList(currentIndex + 1, opponents.size());
    }

    private StackEntry sourceEntry(Card sourceCard, UUID sourceControllerId, UUID sourcePermanentId,
                                   Permanent sourcePermanentSnapshot) {
        StackEntry sourceEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard, sourceControllerId,
                sourceCard.getName() + "'s ability", new ArrayList<>(), 0, sourcePermanentId);
        sourceEntry.setSourcePermanentSnapshot(sourcePermanentSnapshot);
        return sourceEntry;
    }

    private void createToken(GameData gameData, StackEntry sourceEntry, CreateTokenEffect token) {
        createTokenEffectHandler.resolve(gameData, sourceEntry, token);
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID sourceControllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
            ordered = rotated;
        }
        return ordered.stream().filter(playerId -> !playerId.equals(sourceControllerId)).toList();
    }
}
