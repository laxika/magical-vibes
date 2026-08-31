package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.BendingType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Shared payment logic for waterbend costs that are resolved outside spell casting or ability activation. */
@Component
public class WaterbendPaymentService {

    private final GameQueryService gameQueryService;
    private final TriggerCollectionService triggerCollectionService;
    private final GameLogService gameLogService;

    public WaterbendPaymentService(GameQueryService gameQueryService,
                                   @Lazy TriggerCollectionService triggerCollectionService,
                                   GameLogService gameLogService) {
        this.gameQueryService = gameQueryService;
        this.triggerCollectionService = triggerCollectionService;
        this.gameLogService = gameLogService;
    }

    public boolean canPay(GameData gameData, UUID playerId, int amount) {
        ManaPool manaPool = gameData.playerManaPools.get(playerId);
        int availableMana = manaPool == null ? 0 : manaPool.getTotal();
        if (availableMana >= amount) {
            return true;
        }
        return eligiblePermanents(gameData, playerId).size() >= amount - availableMana;
    }

    public void pay(GameData gameData, UUID playerId, int amount, Card sourceCard) {
        pay(gameData, playerId, amount, sourceCard, List.of());
    }

    public void pay(GameData gameData, UUID playerId, int amount, Card sourceCard,
                    List<UUID> selectedPermanentIds) {
        ManaPool manaPool = gameData.playerManaPools.get(playerId);
        if (!canPay(gameData, playerId, amount) || manaPool == null) {
            throw new IllegalStateException("Not enough mana or untapped artifacts and creatures to pay waterbend cost");
        }

        List<UUID> ids = selectedPermanentIds == null ? List.of() : selectedPermanentIds;
        List<Permanent> toTap;
        if (ids.isEmpty()) {
            int tapCount = Math.max(0, amount - manaPool.getTotal());
            toTap = eligiblePermanents(gameData, playerId).subList(0, tapCount);
        } else {
            if (ids.size() > amount || ids.stream().distinct().count() != ids.size()) {
                throw new IllegalStateException("Invalid permanents chosen for the waterbend cost");
            }
            toTap = ids.stream().map(id -> validateSelectedPermanent(gameData, playerId, id)).toList();
            if (manaPool.getTotal() < amount - toTap.size()) {
                throw new IllegalStateException("Not enough mana to pay the remaining waterbend cost");
            }
        }

        for (Permanent permanent : toTap) {
            permanent.tap();
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, permanent);
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(playerId) + " taps ")
                    .card(permanent.getCard())
                    .text(" as a waterbend cost for ")
                    .card(sourceCard)
                    .text(".")
                    .build());
        }

        int remainingMana = amount - toTap.size();
        if (remainingMana > 0) {
            new ManaCost("{" + remainingMana + "}").payAsGeneric(manaPool);
        }
        triggerCollectionService.checkBendingTriggers(gameData, playerId, BendingType.WATERBEND);
    }

    private Permanent validateSelectedPermanent(GameData gameData, UUID playerId, UUID permanentId) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent == null || !playerId.equals(gameQueryService.findPermanentController(gameData, permanentId))) {
            throw new IllegalStateException("Can only tap permanents you control for the waterbend cost");
        }
        if (permanent.isTapped()) {
            throw new IllegalStateException("Cannot tap an already tapped permanent for the waterbend cost");
        }
        if (!gameQueryService.isArtifact(gameData, permanent) && !gameQueryService.isCreature(gameData, permanent)) {
            throw new IllegalStateException("Waterbend can tap only artifacts or creatures");
        }
        return permanent;
    }

    private List<Permanent> eligiblePermanents(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> !permanent.isTapped())
                .filter(permanent -> gameQueryService.isArtifact(gameData, permanent)
                        || gameQueryService.isCreature(gameData, permanent))
                .toList();
    }
}
