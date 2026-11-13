package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedPermanentControllerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves damage dealt by an Aura to the controller of its enchanted permanent. */
@Component
@RequiredArgsConstructor
public class DealDamageToEnchantedPermanentControllerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEnchantedPermanentControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToEnchantedPermanentControllerEffect) effect;
        Card enchantedCard = entry.getTriggeringPermanentSnapshot() != null
                ? entry.getTriggeringPermanentSnapshot().getCard()
                : entry.getTriggeringCards().isEmpty() ? null : entry.getTriggeringCards().getFirst();
        if (!e.enchantedPermanentIsSource() || enchantedCard == null) {
            dealDamage(gameData, entry, e);
            return;
        }
        // "It deals 1 damage to its controller": the enchanted permanent the trigger event was about deals the damage,
        // as it last existed (CR 608.2h), so its lifelink and its controller apply to that damage, not the Aura's.
        Card auraDamageSourceCard = entry.getDamageSourceCard();
        UUID auraDamageSourceControllerId = entry.getDamageSourceControllerId();
        entry.setDamageSourceCard(enchantedCard);
        entry.setDamageSourceControllerId(entry.getTriggeringPermanentControllerId());
        try {
            dealDamage(gameData, entry, e);
        } finally {
            entry.setDamageSourceCard(auraDamageSourceCard);
            entry.setDamageSourceControllerId(auraDamageSourceControllerId);
        }
    }

    private void dealDamage(GameData gameData, StackEntry entry, DealDamageToEnchantedPermanentControllerEffect e) {
        UUID controllerId = findEnchantedPermanentController(gameData, entry);
        if (controllerId == null || damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(), AmountContext.forStackEntry(entry, source));
        int rawDamage = amount;
        damageSupport.dealDamageToPlayer(gameData, entry, controllerId, rawDamage);
        gameOutcomeService.checkWinCondition(gameData);
    }

    private UUID findEnchantedPermanentController(GameData gameData, StackEntry entry) {
        Permanent aura = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null) {
            aura = entry.getSourcePermanentSnapshot();
        }
        Permanent enchanted = aura == null || aura.getAttachedTo() == null
                ? null : gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchanted != null) {
            return gameQueryService.findPermanentController(gameData, enchanted.getId());
        }

        // The enchanted permanent is gone: its last-known controller (CR 608.2h), recorded on the entry as it left
        // (the Aura's abilities already on the stack), or when the entry records the permanent the Aura was attached
        // to, or the enchanted permanent its death trigger records (CR 603.10a). An orphaned Aura's death trigger has the
        // controller captured when the permanent died as its targetId.
        UUID departedControllerId = aura == null ? null : entry.lastKnownPermanentController(aura.getAttachedTo());
        if (departedControllerId != null) {
            return departedControllerId;
        }
        if ((entry.getAttachedPermanentSnapshot() != null || entry.getTriggeringPermanentSnapshot() != null)
                && entry.getTriggeringPermanentControllerId() != null) {
            return entry.getTriggeringPermanentControllerId();
        }
        UUID capturedControllerId = entry.getTargetId();
        return capturedControllerId != null && gameData.playerIds.contains(capturedControllerId)
                ? capturedControllerId : null;
    }
}
