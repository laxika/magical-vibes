package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffect.Replacement;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final RevealUntilCardPredicateRestOnBottomRandomEffectHandler revealHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typed = (ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffect) effect;
        List<Replacement> remaining = typed.remaining() == null
                ? exileTargets(gameData, entry, effect) : new ArrayList<>(typed.remaining());
        if (remaining.isEmpty()) return;

        int index = entry.getResolvingEffectIndex();
        UUID controllerId = remaining.getFirst().controllerId();
        List<Replacement> options = remaining.stream()
                .filter(item -> item.controllerId().equals(controllerId)).toList();
        if (typed.selectedPermanentId() == null && options.size() > 1) {
            entry.replaceEffectToResolve(index,
                    new ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffect(remaining, null));
            List<String> labels = IntStream.range(0, options.size())
                    .mapToObj(i -> (i + 1) + ": " + options.get(i).name()).toList();
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    controllerId, null, null,
                    new ChoiceContext.PermanentReplacementOrder(
                            options.stream().map(Replacement::permanentId).toList()),
                    labels, "Choose the next permanent to replace."));
            gameData.rerunCurrentEffectAfterInteraction = true;
            return;
        }

        Replacement next = typed.selectedPermanentId() == null ? options.getFirst()
                : options.stream().filter(item -> item.permanentId().equals(typed.selectedPermanentId()))
                .findFirst().orElseThrow();
        remaining.remove(next);
        if (!remaining.isEmpty()) {
            entry.insertEffectsToResolve(index + 1, List.of(
                    new ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffect(remaining, null)));
        }
        StackEntry revealEntry = new StackEntry(entry);
        revealEntry.setControllerId(next.controllerId());
        List<CardPredicate> predicates = next.cardTypes().stream()
                .map(type -> (CardPredicate) new CardTypePredicate(type)).toList();
        revealHandler.resolve(gameData, revealEntry,
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardAnyOfPredicate(predicates), LibrarySearchDestination.BATTLEFIELD), true);
    }

    private List<Replacement> exileTargets(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        List<Permanent> targets = new ArrayList<>();
        List<Replacement> snapshots = new ArrayList<>();
        for (UUID targetId : new LinkedHashSet<>(targetIds)) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (target == null || controllerId == null) continue;
            targets.add(target);
            snapshots.add(new Replacement(targetId, controllerId,
                    gameQueryService.getEffectiveName(gameData, target),
                    gameQueryService.getEffectiveCardTypes(gameData, target)));
        }
        List<Replacement> exiled = new ArrayList<>();
        permanentRemovalService.performSimultaneousRemovals(gameData, targets, () -> {
            for (int i = 0; i < targets.size(); i++) {
                Permanent target = targets.get(i);
                if (permanentRemovalService.removePermanentToExile(gameData, target)) {
                    exiled.add(snapshots.get(i));
                    gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));
                }
            }
        });
        permanentRemovalService.removeOrphanedAuras(gameData);
        List<UUID> playerOrder = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = playerOrder.indexOf(gameData.activePlayerId);
        if (activeIndex >= 0) java.util.Collections.rotate(playerOrder, -activeIndex);
        exiled.sort(Comparator.comparingInt(item -> playerOrder.indexOf(item.controllerId())));
        return exiled;
    }
}
