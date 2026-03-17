package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.List;
import java.util.UUID;

/**
 * Handles the {@link SacrificeCreatureCost} as a permanent-choice cost when the ability
 * also has other effects that need targeting (e.g. Equip — Sacrifice a creature).
 * Filters the battlefield to creatures only and delegates the actual sacrifice to the provided action.
 */
public class CreatureSacrificeCostHandler implements PermanentChoiceCostHandler {

    private final SacrificeCreatureCost cost;
    private final GameQueryService gameQueryService;
    private final PermanentSacrificeAction sacrificeAction;
    private final UUID sourcePermanentId;

    public CreatureSacrificeCostHandler(SacrificeCreatureCost cost, GameQueryService gameQueryService, PermanentSacrificeAction sacrificeAction) {
        this(cost, gameQueryService, sacrificeAction, null);
    }

    public CreatureSacrificeCostHandler(SacrificeCreatureCost cost, GameQueryService gameQueryService, PermanentSacrificeAction sacrificeAction, UUID sourcePermanentId) {
        this.cost = cost;
        this.gameQueryService = gameQueryService;
        this.sacrificeAction = sacrificeAction;
        this.sourcePermanentId = sourcePermanentId;
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return 1; }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (getValidChoiceIds(gameData, playerId).isEmpty()) {
            throw new IllegalStateException("Must choose a creature to sacrifice");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .filter(p -> !cost.excludeSelf() || !p.getId().equals(sourcePermanentId))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (!gameQueryService.isCreature(gameData, chosen)) {
            throw new IllegalStateException("Must sacrifice a creature");
        }
        if (cost.excludeSelf() && chosen.getId().equals(sourcePermanentId)) {
            throw new IllegalStateException("Cannot sacrifice this permanent to its own ability");
        }
        sacrificeAction.sacrifice(gameData, player, chosen);
    }

    @Override
    public String getPromptMessage(int remaining) {
        return cost.excludeSelf() ? "Choose another creature to sacrifice." : "Choose a creature to sacrifice.";
    }
}
