package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeDistinctNamePermanentsCost;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Handles activation costs that require sacrificing matching permanents with different names. */
public class DistinctNamePermanentSacrificeCostHandler implements PermanentChoiceCostHandler {

    private final SacrificeDistinctNamePermanentsCost cost;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentSacrificeAction sacrificeAction;
    private final List<UUID> chosenSoFar;

    public DistinctNamePermanentSacrificeCostHandler(SacrificeDistinctNamePermanentsCost cost,
                                                     PredicateEvaluationService predicateEvaluationService,
                                                     PermanentSacrificeAction sacrificeAction,
                                                     List<UUID> chosenSoFar) {
        this.cost = cost;
        this.predicateEvaluationService = predicateEvaluationService;
        this.sacrificeAction = sacrificeAction;
        this.chosenSoFar = new ArrayList<>(chosenSoFar == null ? List.of() : chosenSoFar);
    }

    @Override
    public CardEffect costEffect() {
        return cost;
    }

    @Override
    public int requiredCount() {
        return cost.count();
    }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (distinctNames(matchingPermanents(gameData, playerId)) < cost.count()) {
            throw new IllegalStateException("Not enough permanents with different names to sacrifice (need "
                    + cost.count() + ")");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        int remaining = cost.count() - chosenSoFar.size();
        if (remaining <= 0) {
            return List.of();
        }

        List<Permanent> candidates = matchingPermanents(gameData, playerId);
        Set<String> chosenNames = chosenNames(gameData, playerId);
        return candidates.stream()
                .filter(permanent -> !chosenSoFar.contains(permanent.getId()))
                .filter(permanent -> !chosenNames.contains(permanent.getCard().getName()))
                .filter(permanent -> distinctNamesAfterChoice(candidates, permanent, chosenNames) >= remaining - 1)
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (!getValidChoiceIds(gameData, player.getId()).contains(chosen.getId())) {
            throw new IllegalStateException("Must sacrifice artifact tokens with different names");
        }

        chosenSoFar.add(chosen.getId());
        if (chosenSoFar.size() < cost.count()) {
            return;
        }

        List<Permanent> selected = chosenSoFar.stream()
                .map(id -> findPermanent(gameData, player.getId(), id))
                .toList();
        if (selected.stream().anyMatch(permanent -> permanent == null)) {
            throw new IllegalStateException("A selected permanent is no longer available to sacrifice");
        }
        for (Permanent permanent : selected) {
            sacrificeAction.sacrifice(gameData, player, permanent);
        }
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose an artifact token with a different name to sacrifice (" + remaining + " remaining).";
    }

    @Override
    public boolean shouldAutoPayAll(GameData gameData, UUID playerId, int remaining) {
        List<UUID> validIds = getValidChoiceIds(gameData, playerId);
        Set<String> validNames = new HashSet<>();
        for (UUID id : validIds) {
            Permanent permanent = findPermanent(gameData, playerId, id);
            if (permanent != null) {
                validNames.add(permanent.getCard().getName());
            }
        }
        return validIds.size() <= remaining && validNames.size() == validIds.size();
    }

    private List<Permanent> matchingPermanents(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, cost.filter()))
                .toList();
    }

    private Set<String> chosenNames(GameData gameData, UUID playerId) {
        Set<String> names = new HashSet<>();
        for (UUID id : chosenSoFar) {
            Permanent permanent = findPermanent(gameData, playerId, id);
            if (permanent != null) {
                names.add(permanent.getCard().getName());
            }
        }
        return names;
    }

    private int distinctNamesAfterChoice(List<Permanent> candidates, Permanent chosen, Set<String> chosenNames) {
        Set<String> names = new HashSet<>();
        for (Permanent candidate : candidates) {
            if (candidate != chosen
                    && !chosenSoFar.contains(candidate.getId())
                    && !chosenNames.contains(candidate.getCard().getName())
                    && !candidate.getCard().getName().equals(chosen.getCard().getName())) {
                names.add(candidate.getCard().getName());
            }
        }
        return names.size();
    }

    private static int distinctNames(List<Permanent> permanents) {
        return (int) permanents.stream()
                .map(permanent -> permanent.getCard().getName())
                .distinct()
                .count();
    }

    private static Permanent findPermanent(GameData gameData, UUID playerId, UUID permanentId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }
        return battlefield.stream()
                .filter(permanent -> permanent.getId().equals(permanentId))
                .findFirst()
                .orElse(null);
    }
}
