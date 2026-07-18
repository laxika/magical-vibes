package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedPermanentUnlessPaysManaOrLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "Destroy [the enchanted permanent] unless that player pays {M} or N life." (Erosion.) The paying
 * player is the enchanted permanent's controller — baked onto the trigger's {@code targetId} by
 * {@code StepTriggerService}. When the player can pay neither resource the permanent is destroyed
 * immediately; otherwise the choice is offered via the may-ability system.
 */
@Component
@RequiredArgsConstructor
public class DestroyEnchantedPermanentUnlessPaysManaOrLifeEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyEnchantedPermanentUnlessPaysManaOrLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyEnchantedPermanentUnlessPaysManaOrLifeEffect) effect;

        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || !aura.isAttached()) {
            // The Aura (and thus the permanent it was on) is already gone — nothing to do.
            return;
        }
        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchanted == null) {
            return;
        }

        UUID payerId = entry.getTargetId();
        ManaCost cost = new ManaCost(e.manaCost());
        ManaPool pool = gameData.playerManaPools.get(payerId);
        boolean canPayMana = cost.canPay(pool);
        boolean canPayLife = gameQueryService.canPlayerLifeChange(gameData, payerId)
                && gameData.getLife(payerId) >= e.lifeCost();

        if (!canPayMana && !canPayLife) {
            // Can't pay either — destroy the enchanted permanent now.
            destructionSupport.tryDestroyAndLog(gameData, enchanted, entry.getCard().getName());
            return;
        }

        // Can pay at least one resource — ask the player via the may-ability system.
        String prompt = "Pay " + e.manaCost() + " or " + e.lifeCost() + " life? If you don't, "
                + enchanted.getCard().getName() + " is destroyed. (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), payerId, List.of(e), prompt, null, e.manaCost(), entry.getSourcePermanentId()));
    }
}
