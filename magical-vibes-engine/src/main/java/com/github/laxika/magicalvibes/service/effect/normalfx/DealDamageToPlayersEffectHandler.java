package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Deals damage to one or more players. Merged handler for every {@link DealDamageToPlayersEffect}
 * recipient. Each recipient computes its victim player id(s) explicitly and passes them to
 * {@link DamageSupport#dealDamageToPlayer}; the stack entry's controller id is never remapped so
 * opponent-source damage-reduction shields keep applying (the source stays the caster).
 */
@Component
@RequiredArgsConstructor
public class DealDamageToPlayersEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToPlayersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToPlayersEffect) effect;

        switch (e.recipient()) {
            case TARGET_PLAYER, ENCHANTED_PLAYER, ENCHANTED_PERMANENT_CONTROLLER, TRIGGERING_PERMANENT_CONTROLLER ->
                    resolveSingleTargetPlayer(gameData, entry, e);
            case CONTROLLER -> resolveController(gameData, entry, e);
            case EACH_OPPONENT -> resolveEachPlayer(gameData, entry, e, true);
            case EACH_PLAYER -> resolveEachPlayer(gameData, entry, e, false);
            case TARGET_PERMANENT_CONTROLLER -> resolveTargetPermanentController(gameData, entry, e);
            case TARGET_SPELL_CONTROLLER -> resolveTargetSpellController(gameData, entry, e);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    /** TARGET_PLAYER / ENCHANTED_PLAYER / ENCHANTED_PERMANENT_CONTROLLER / TRIGGERING_PERMANENT_CONTROLLER: victim = the stack entry's target player. */
    private void resolveSingleTargetPlayer(GameData gameData, StackEntry entry, DealDamageToPlayersEffect e) {
        UUID targetId = entry.getTargetId();
        if (!gameData.playerIds.contains(targetId)) return;

        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            int amount = evaluateAmount(gameData, entry, e, targetId);
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, amount, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }
    }

    /** CONTROLLER: "deals N damage to you". */
    private void resolveController(GameData gameData, StackEntry entry, DealDamageToPlayersEffect e) {
        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), "'s damage to controller is prevented."));
        } else {
            int amount = evaluateAmount(gameData, entry, e, entry.getControllerId());
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, amount, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, entry.getControllerId(), rawDamage);
        }
    }

    /** EACH_OPPONENT ({@code opponentsOnly}) / EACH_PLAYER: same value dealt to every player in scope. */
    private void resolveEachPlayer(GameData gameData, StackEntry entry, DealDamageToPlayersEffect e, boolean opponentsOnly) {
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        int evaluated = evaluateAmount(gameData, entry, e, entry.getControllerId());
        int damage = gameQueryService.applyDamageMultiplier(gameData, evaluated, entry);
        UUID controllerId = entry.getControllerId();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (opponentsOnly && playerId.equals(controllerId)) continue;
            damageSupport.dealDamageToPlayer(gameData, entry, playerId, damage);
        }
    }

    /** TARGET_PERMANENT_CONTROLLER: victim = the controller of the targeted permanent. */
    private void resolveTargetPermanentController(GameData gameData, StackEntry entry, DealDamageToPlayersEffect e) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        String cardName = entry.getCard().getName();

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(cardName + "'s damage to " + gameData.playerIdToName.get(controllerId) + " is prevented."));
        } else {
            int amount = evaluateAmount(gameData, entry, e, controllerId);
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, amount, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, controllerId, rawDamage);
        }
    }

    /** TARGET_SPELL_CONTROLLER: victim = the controller of the targeted spell still on the stack. */
    private void resolveTargetSpellController(GameData gameData, StackEntry entry, DealDamageToPlayersEffect e) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetSpell = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetSpell = se;
                break;
            }
        }
        if (targetSpell == null) return;

        UUID victimId = targetSpell.getControllerId();
        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            int amount = evaluateAmount(gameData, entry, e, victimId);
            if (amount <= 0) return;
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, amount, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, victimId, rawDamage);
        }
    }

    private int evaluateAmount(GameData gameData, StackEntry entry, DealDamageToPlayersEffect e, UUID victimId) {
        // Curse of Thirst: damage equals the number of matching permanents attached to the enchanted player.
        if (e.attachedCountFilter() != null) {
            return damageSupport.countPermanentsAttachedToPlayer(gameData, victimId, e.attachedCountFilter());
        }
        // Source-relative amounts use the live source permanent when it is still on the battlefield,
        // else the last-known snapshot (e.g. sacrificed as an activation cost).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        return amountEvaluationService.evaluate(gameData, e.amount(), AmountContext.forStackEntry(entry, source));
    }
}
