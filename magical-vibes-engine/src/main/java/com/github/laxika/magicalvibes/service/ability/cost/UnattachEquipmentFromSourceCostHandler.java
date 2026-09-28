package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentFromSourceCost;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.EquipSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UnattachEquipmentFromSourceCostHandler implements PermanentChoiceCostHandler {

    private final UnattachEquipmentFromSourceCost cost;
    private final UUID sourcePermanentId;
    private final GameQueryService gameQueryService;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;

    public UnattachEquipmentFromSourceCostHandler(UnattachEquipmentFromSourceCost cost,
                                                   UUID sourcePermanentId,
                                                   GameQueryService gameQueryService,
                                                   EquipSupport equipSupport,
                                                   GameLogService gameLogService) {
        this.cost = cost;
        this.sourcePermanentId = sourcePermanentId;
        this.gameQueryService = gameQueryService;
        this.equipSupport = equipSupport;
        this.gameLogService = gameLogService;
    }

    @Override
    public CardEffect costEffect() {
        return cost;
    }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (getValidChoiceIds(gameData, playerId).isEmpty()) {
            throw new IllegalStateException("No Equipment is attached to the source");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<UUID> validIds = new ArrayList<>();
        Permanent source = sourcePermanentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return validIds;
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (GameQueryService.permanentHasSubtype(permanent, CardSubtype.EQUIPMENT)
                        && source.getId().equals(permanent.getAttachedTo())) {
                    validIds.add(permanent.getId());
                }
            }
        }
        return validIds;
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        Permanent source = sourcePermanentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (!GameQueryService.permanentHasSubtype(chosen, CardSubtype.EQUIPMENT)
                || source == null
                || !source.getId().equals(chosen.getAttachedTo())) {
            throw new IllegalStateException("Choose an Equipment attached to the source");
        }

        UUID oldAttachedTo = chosen.getAttachedTo();
        chosen.setAttachedTo(null);
        gameData.expireFloatingEffectsForUnattachedSource(chosen.getId());
        equipSupport.expireAttachedCopyEffects(gameData, chosen);
        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, chosen, oldAttachedTo, null);
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " unattaches ", chosen.getCard(), " as a cost."));
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose an Equipment attached to the source.";
    }

    @Override
    public int requiredCount() {
        return 1;
    }
}
