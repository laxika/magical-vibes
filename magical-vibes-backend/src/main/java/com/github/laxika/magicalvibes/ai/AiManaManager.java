package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.List;
import java.util.UUID;
import java.util.function.IntConsumer;

/**
 * Shared mana management logic for AI: virtual mana pool calculation,
 * land tapping, and X-cost spell management.
 */
class AiManaManager {

    private final GameQueryService gameQueryService;

    AiManaManager(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    ManaPool buildVirtualManaPool(GameData gameData, UUID aiPlayerId) {
        ManaPool virtual = new ManaPool();

        ManaPool current = gameData.playerManaPools.get(aiPlayerId);
        if (current != null) {
            for (ManaColor color : ManaColor.values()) {
                for (int i = 0; i < current.get(color); i++) {
                    virtual.add(color);
                }
            }
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayerId);
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (perm.isTapped()) {
                    continue;
                }
                if (gameQueryService.isCreature(gameData, perm) && perm.isSummoningSick()
                        && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) {
                    continue;
                }
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_TAP)) {
                    if (effect instanceof AwardManaEffect manaEffect) {
                        virtual.add(manaEffect.color(), manaEffect.amount());
                    } else if (effect instanceof AwardAnyColorManaEffect) {
                        virtual.add(ManaColor.COLORLESS);
                    }
                }
            }
        }

        return virtual;
    }

    void tapLandsForCost(GameData gameData, UUID aiPlayerId, String manaCostStr, IntConsumer tapPermanent) {
        ManaCost cost = new ManaCost(manaCostStr);
        ManaPool currentPool = gameData.playerManaPools.get(aiPlayerId);

        if (cost.canPay(currentPool)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayerId);
        if (battlefield == null) {
            return;
        }

        for (int i = 0; i < battlefield.size(); i++) {
            Permanent perm = battlefield.get(i);
            if (perm.isTapped()) {
                continue;
            }
            if (gameQueryService.isCreature(gameData, perm) && perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) {
                continue;
            }

            boolean producesMana = perm.getCard().getEffects(EffectSlot.ON_TAP).stream()
                    .anyMatch(e -> e instanceof AwardManaEffect || e instanceof AwardAnyColorManaEffect);
            if (!producesMana) {
                continue;
            }

            tapPermanent.accept(i);

            currentPool = gameData.playerManaPools.get(aiPlayerId);
            if (cost.canPay(currentPool)) {
                return;
            }
        }
    }

    void tapLandsForXSpell(GameData gameData, UUID aiPlayerId, Card card, int xValue, IntConsumer tapPermanent) {
        ManaCost cost = new ManaCost(card.getManaCost());
        ManaPool currentPool = gameData.playerManaPools.get(aiPlayerId);

        boolean alreadyPaid;
        if (card.getXColorRestriction() != null) {
            alreadyPaid = cost.canPay(currentPool, xValue, card.getXColorRestriction(), 0);
        } else {
            alreadyPaid = cost.canPay(currentPool, xValue);
        }
        if (alreadyPaid) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayerId);
        if (battlefield == null) {
            return;
        }

        for (int i = 0; i < battlefield.size(); i++) {
            Permanent perm = battlefield.get(i);
            if (perm.isTapped()) {
                continue;
            }
            if (gameQueryService.isCreature(gameData, perm) && perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) {
                continue;
            }

            boolean producesMana = perm.getCard().getEffects(EffectSlot.ON_TAP).stream()
                    .anyMatch(e -> e instanceof AwardManaEffect || e instanceof AwardAnyColorManaEffect);
            if (!producesMana) {
                continue;
            }

            tapPermanent.accept(i);

            currentPool = gameData.playerManaPools.get(aiPlayerId);
            boolean canPayNow;
            if (card.getXColorRestriction() != null) {
                canPayNow = cost.canPay(currentPool, xValue, card.getXColorRestriction(), 0);
            } else {
                canPayNow = cost.canPay(currentPool, xValue);
            }
            if (canPayNow) {
                return;
            }
        }
    }

    int calculateMaxAffordableX(Card card, ManaPool pool) {
        ManaCost cost = new ManaCost(card.getManaCost());
        if (card.getXColorRestriction() != null) {
            return cost.calculateMaxX(pool, card.getXColorRestriction(), 0);
        }
        return cost.calculateMaxX(pool);
    }

    int calculateSmartX(GameData gameData, Card card, UUID targetId, ManaPool virtualPool) {
        int maxX = calculateMaxAffordableX(card, virtualPool);
        if (maxX <= 0) {
            return 0;
        }

        if (targetId != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null && gameQueryService.isCreature(gameData, target)) {
                int toughness = gameQueryService.getEffectiveToughness(gameData, target);
                return Math.min(toughness, maxX);
            }
        }

        return maxX;
    }
}
