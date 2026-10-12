package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUnlessControllerPaysEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a target creature's controller's pay-or-exile choice. */
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentUnlessControllerPaysEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final ExileTargetPermanentEffectHandler exileTargetPermanentEffectHandler;
    private final PotentialManaService potentialManaService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentUnlessControllerPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTargetPermanentUnlessControllerPaysEffect) effect;
        UUID targetPermanentId = entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetPermanentId);
        if (targetControllerId == null) {
            return;
        }

        // "Unless its controller pays {X}" still offers the choice at X = 0: paying {0} keeps the
        // target, declining exiles it.
        int amount = Math.max(0, amountEvaluationService.evaluate(gameData, e.manaAmount(),
                AmountContext.forStackEntry(entry, null)));

        String manaCost = "{" + amount + "}";
        ManaCost cost = new ManaCost(manaCost);
        ManaPool pool = gameData.playerManaPools.get(targetControllerId);
        // Mana abilities may be activated while the choice is open (the payment is made as the
        // choice resolves), so untapped mana sources count toward what the controller can pay.
        boolean canPayWithSources = potentialManaService
                .buildVirtualManaPool(gameData, targetControllerId).getTotal() >= amount;
        if (!cost.canPay(pool) && !canPayWithSources) {
            exileTargetPermanent(gameData, entry.getCard(), entry.getControllerId(),
                    targetPermanentId, entry.getSourcePermanentId());
            return;
        }

        String prompt = "Pay " + manaCost + "? If you don't, " + target.getCard().getName()
                + " is exiled. (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetControllerId, List.of(e), prompt,
                targetPermanentId, manaCost, entry.getSourcePermanentId(), null,
                0, 0, null, null, null, entry.getSourcePermanentSnapshot(), entry.getControllerId()));
    }

    /** Exiles the target when the controller declines or cannot pay. */
    public void exileTargetPermanent(GameData gameData, Card sourceCard, UUID sourceControllerId,
            UUID targetPermanentId, UUID sourcePermanentId) {
        ExileTargetPermanentEffect exileTarget = new ExileTargetPermanentEffect();
        StackEntry targetEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, sourceCard, sourceControllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(exileTarget)), targetPermanentId, sourcePermanentId);
        exileTargetPermanentEffectHandler.resolve(gameData, targetEntry, exileTarget);
    }
}
