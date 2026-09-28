package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DanseMacabreEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Danse Macabre's caster-first simultaneous sacrifice and return branches. */
@Component
@RequiredArgsConstructor
public class DanseMacabreEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DanseMacabreEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DanseMacabreEffect danseMacabre = (DanseMacabreEffect) effect;
        if (!danseMacabre.sacrificePhase()) {
            resolveReturnChoice(gameData, entry, danseMacabre);
            return;
        }

        List<PendingForcedSacrifice> choosers = new ArrayList<>();
        for (UUID playerId : casterFirstPlayerOrder(gameData, entry.getControllerId())) {
            if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, entry.getControllerId())) {
                continue;
            }
            List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, playerId,
                    permanent -> !permanent.getCard().isToken()
                            && !gameQueryService.cantBeSacrificed(gameData, permanent));
            if (!creatureIds.isEmpty()) {
                choosers.add(new PendingForcedSacrifice(playerId, 1, creatureIds));
            }
        }

        if (choosers.stream().allMatch(choice -> choice.validPermanentIds().size() <= choice.count())) {
            completeAfterChoices(gameData, entry, choosers.stream()
                    .map(PendingForcedSacrifice::validPermanentIds)
                    .flatMap(List::stream)
                    .toList());
            return;
        }

        beginNextChoice(gameData, choosers, List.of(), entry);
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.DanseMacabreSacrifice context) {
        List<UUID> allChoices = new ArrayList<>(context.accumulatedSacrificeIds());
        allChoices.addAll(permanentIds);
        if (!context.remainingChoosers().isEmpty()) {
            beginNextChoice(gameData, context.remainingChoosers(), allChoices, context.resolvingEntry());
            return;
        }
        completeAfterChoices(gameData, context.resolvingEntry(), allChoices);
    }

    private void beginNextChoice(GameData gameData, List<PendingForcedSacrifice> choosers,
                                 List<UUID> accumulatedChoices, StackEntry entry) {
        PendingForcedSacrifice next = choosers.getFirst();
        playerInputService.beginMultiPermanentChoice(gameData, next.playerId(), next.validPermanentIds(),
                next.count(),
                new MultiPermanentChoiceContext.DanseMacabreSacrifice(
                        List.copyOf(choosers.subList(1, choosers.size())), accumulatedChoices, entry),
                "Choose a nontoken creature to sacrifice.");
    }

    private void completeAfterChoices(GameData gameData, StackEntry entry, List<UUID> permanentIds) {
        int controllerToughness = 0;
        List<UUID> sacrificedCardIds = new ArrayList<>();
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null) {
                continue;
            }
            sacrificedCardIds.add(permanent.getCard().getId());
            if (entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, permanentId))) {
                controllerToughness = gameQueryService.getEffectiveToughness(gameData, permanent);
            }
        }

        destructionSupport.performSimultaneousSacrifice(gameData, permanentIds);
        permanentRemovalService.removeOrphanedAuras(gameData);
        entry.setEventCardIds(sacrificedCardIds.stream()
                .filter(cardId -> gameQueryService.findCardInGraveyardById(gameData, cardId) != null)
                .toList());

        RollD20Effect roll = RollD20Effect.withAddedAmount(
                new Fixed(controllerToughness),
                new DanseMacabreEffect(1, true),
                new DanseMacabreEffect(2, false));
        int currentIndex = Math.max(0, entry.getResolvingEffectIndex());
        entry.insertEffectsToResolve(currentIndex + 1, List.of(roll));
    }

    private void resolveReturnChoice(GameData gameData, StackEntry entry, DanseMacabreEffect effect) {
        Set<UUID> sacrificedCardIds = new HashSet<>(entry.getEventCardIds());
        List<Card> candidates = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Card card : gameData.playerGraveyards.getOrDefault(playerId, List.of())) {
                if (sacrificedCardIds.contains(card.getId()) && card.hasType(CardType.CREATURE)) {
                    candidates.add(card);
                }
            }
        }
        if (candidates.isEmpty()) {
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeReturnCardsToBattlefieldResume = true;
        playerInputService.beginMultiGraveyardChoice(gameData, entry.getControllerId(), candidates,
                effect.maxReturnCount(), effect.mandatoryReturn() ? 1 : 0,
                effect.mandatoryReturn()
                        ? "Choose a creature card to return to the battlefield under your control."
                        : "Choose up to two creature cards to return to the battlefield under your control.");
    }

    private List<UUID> casterFirstPlayerOrder(GameData gameData, UUID casterId) {
        List<UUID> orderedPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int casterIndex = orderedPlayers.indexOf(casterId);
        if (casterIndex <= 0) {
            return orderedPlayers;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayers.subList(casterIndex, orderedPlayers.size()));
        rotated.addAll(orderedPlayers.subList(0, casterIndex));
        return rotated;
    }
}
