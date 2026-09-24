package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.EquipSupport;

import java.util.List;
import java.util.UUID;

/** Pays "unattach an Equipment from this permanent" and remembers its mana value as X. */
public class UnattachEquipmentFromSourceCostHandler implements PermanentChoiceCostHandler {

    private final CostEffect cost;
    private final GameQueryService gameQueryService;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;
    private final UUID sourcePermanentId;
    private Integer lastPaymentValue;

    public UnattachEquipmentFromSourceCostHandler(CostEffect cost,
                                                   GameQueryService gameQueryService,
                                                   EquipSupport equipSupport,
                                                   GameLogService gameLogService,
                                                   UUID sourcePermanentId) {
        this.cost = cost;
        this.gameQueryService = gameQueryService;
        this.equipSupport = equipSupport;
        this.gameLogService = gameLogService;
        this.sourcePermanentId = sourcePermanentId;
    }

    @Override
    public CardEffect costEffect() {
        return cost;
    }

    @Override
    public int requiredCount() {
        return 1;
    }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (getValidChoiceIds(gameData, playerId).isEmpty()) {
            throw new IllegalStateException("No Equipment is attached to this permanent");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return List.of();
        }
        return gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(equipment -> GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT))
                .filter(equipment -> sourcePermanentId.equals(equipment.getAttachedTo()))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null || !sourcePermanentId.equals(chosen.getAttachedTo())
                || !GameQueryService.permanentHasSubtype(chosen, CardSubtype.EQUIPMENT)) {
            throw new IllegalStateException("Must unattach an Equipment from this permanent");
        }

        lastPaymentValue = chosen.getCard().getManaValue();
        UUID oldAttachedTo = chosen.getAttachedTo();
        chosen.setAttachedTo(null);
        gameData.expireFloatingEffectsForUnattachedSource(chosen.getId());
        equipSupport.expireAttachedCopyEffects(gameData, chosen);
        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, chosen, oldAttachedTo, null);
        gameLogService.append(gameData, GameLog.builder()
                .card(source.getCard())
                .text(" unattaches ")
                .card(chosen.getCard())
                .text(" as a cost.")
                .build());
    }

    @Override
    public Integer lastPaymentValue() {
        return lastPaymentValue;
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose an Equipment attached to this permanent to unattach.";
    }
}
