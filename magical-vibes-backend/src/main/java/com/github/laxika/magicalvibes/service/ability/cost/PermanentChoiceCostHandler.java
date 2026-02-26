package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

public interface PermanentChoiceCostHandler {
    CardEffect costEffect();
    void validateCanPay(GameData gameData, UUID playerId);
    List<UUID> getValidChoiceIds(GameData gameData, UUID playerId);
    void validateAndPay(GameData gameData, Player player, Permanent chosen);
    String getPromptMessage(int remaining);
    int requiredCount();
}
