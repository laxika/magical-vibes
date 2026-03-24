package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.List;
import java.util.UUID;

/**
 * Handles predicate-based permanent sacrifice costs — both {@link SacrificeMultiplePermanentsCost}
 * (e.g. "Sacrifice three artifacts") and {@link SacrificePermanentCost}
 * (e.g. "Sacrifice an artifact or creature"). Filters the battlefield using
 * {@link GameQueryService#matchesPermanentPredicate} and delegates sacrifice to the provided action.
 */
public class MultiplePermanentSacrificeCostHandler implements PermanentChoiceCostHandler {

    private final CardEffect cost;
    private final PermanentPredicate filter;
    private final int count;
    private final String description;
    private final GameQueryService gameQueryService;
    private final PermanentSacrificeAction sacrificeAction;
    private final UUID sourcePermanentId;

    /**
     * Constructor for {@link SacrificeMultiplePermanentsCost} — sacrifice N permanents matching a filter.
     */
    public MultiplePermanentSacrificeCostHandler(SacrificeMultiplePermanentsCost cost, GameQueryService gameQueryService, PermanentSacrificeAction sacrificeAction) {
        this(cost, cost.filter(), cost.count(), null, gameQueryService, sacrificeAction, null);
    }

    /**
     * Constructor for {@link SacrificePermanentCost} — sacrifice one permanent matching a filter.
     */
    public MultiplePermanentSacrificeCostHandler(SacrificePermanentCost cost, GameQueryService gameQueryService,
                                                  PermanentSacrificeAction sacrificeAction, UUID sourcePermanentId) {
        this(cost, cost.filter(), 1, cost.description(), gameQueryService, sacrificeAction, sourcePermanentId);
    }

    private MultiplePermanentSacrificeCostHandler(CardEffect cost, PermanentPredicate filter, int count, String description,
                                                   GameQueryService gameQueryService, PermanentSacrificeAction sacrificeAction,
                                                   UUID sourcePermanentId) {
        this.cost = cost;
        this.filter = filter;
        this.count = count;
        this.description = description;
        this.gameQueryService = gameQueryService;
        this.sacrificeAction = sacrificeAction;
        this.sourcePermanentId = sourcePermanentId;
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return count; }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        List<UUID> validIds = getValidChoiceIds(gameData, playerId);
        if (validIds.size() < count) {
            String message = description != null
                    ? "No permanent to sacrifice matching: " + description
                    : "Not enough permanents to sacrifice (need " + count + ", have " + validIds.size() + ")";
            throw new IllegalStateException(message);
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, filter))
                .filter(p -> sourcePermanentId == null || !p.getId().equals(sourcePermanentId))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (!gameQueryService.matchesPermanentPredicate(gameData, chosen, filter)) {
            String message = description != null
                    ? "Must sacrifice a permanent matching: " + description
                    : "Must sacrifice a matching permanent";
            throw new IllegalStateException(message);
        }
        if (sourcePermanentId != null && chosen.getId().equals(sourcePermanentId)) {
            throw new IllegalStateException("Cannot sacrifice this permanent to its own ability");
        }
        sacrificeAction.sacrifice(gameData, player, chosen);
    }

    @Override
    public String getPromptMessage(int remaining) {
        if (description != null) {
            return "Choose a permanent to sacrifice (" + description + ").";
        }
        return "Choose a permanent to sacrifice (" + remaining + " remaining).";
    }
}
