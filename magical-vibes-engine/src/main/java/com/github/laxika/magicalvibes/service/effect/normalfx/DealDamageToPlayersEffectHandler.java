package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DamageSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
 * opponent-source damage-reduction shields keep applying (the source stays the caster). With
 * {@code DamageSource.EVENT_DAMAGE_SOURCE} the damage is dealt through
 * {@link DamageSupport#entryDealtByEventDamageSource}, whose source is the permanent that dealt the trigger event's
 * damage; with {@code DamageSource.TRIGGERING_PERMANENT} through {@link DamageSupport#entryDealtByTriggeringPermanent},
 * whose source is the permanent the trigger event was about; the recipients and the amount are still read from the
 * resolving entry.
 */
@Component
@RequiredArgsConstructor
public class DealDamageToPlayersEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToPlayersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToPlayersEffect) effect;
        // The entry the damage is dealt through: the resolving one, or one whose source is the permanent that dealt
        // the trigger event's damage ("that Archer deals that much damage"); the recipients are always read from the
        // resolving entry.
        StackEntry dealing = switch (e.damageSource()) {
            case EVENT_DAMAGE_SOURCE -> damageSupport.entryDealtByEventDamageSource(gameData, entry);
            case TRIGGERING_PERMANENT -> damageSupport.entryDealtByTriggeringPermanent(gameData, entry);
            case ABILITY_SOURCE -> entry;
        };
        if (dealing == null) {
            return;
        }
        UUID damageSourceId = dealing.getSourcePermanentId() != null
                ? dealing.getSourcePermanentId() : dealing.getCard().getId();
        int damageBefore = gameData.damageDealtThisTurnBySource.getOrDefault(damageSourceId, 0);
        boolean previousUnpreventable = gameData.unpreventableDamageInProgress;
        gameData.unpreventableDamageInProgress = previousUnpreventable || e.unpreventable();
        try {
            switch (e.recipient()) {
                case ENCHANTED_PERMANENT_CONTROLLER -> {
                    // A tapped-enchanted-permanent trigger names the permanent, so its controller is
                    // read as the ability resolves (CR 608.2h) in case control changed meanwhile.
                    if (entry.getTriggeringPermanentId() != null
                            && gameQueryService.findPermanentController(
                            gameData, entry.getTriggeringPermanentId()) != null) {
                        resolveTriggeringPermanentController(gameData, entry, dealing, e);
                    } else {
                        resolveSingleTargetPlayer(gameData, entry, dealing, e);
                    }
                }
                case TARGET_PLAYER, ACTIVE_PLAYER, ENCHANTED_PLAYER,
                     TRIGGERING_PLAYER, CHOSEN_PLAYER ->
                        resolveSingleTargetPlayer(gameData, entry, dealing, e);
                case ENCHANTED_PLAYER_OR_PLANESWALKER -> resolveEnchantedPlayerOrPlaneswalker(gameData, entry, dealing, e);
                case TRIGGERING_PERMANENT_CONTROLLER -> resolveTriggeringPermanentController(gameData, entry, dealing, e);
                case CONTROLLER -> resolveController(gameData, entry, dealing, e);
                case DEFENDING_PLAYER -> resolveDefendingPlayer(gameData, entry, dealing, e);
                case EACH_OPPONENT -> resolveEachPlayer(gameData, entry, dealing, e, true);
                case SELECTED_OPPONENTS -> resolveSelectedOpponents(gameData, entry, dealing, e);
                case EACH_OTHER_OPPONENT -> resolveEachOtherOpponent(gameData, entry, dealing, e);
                case EACH_PLAYER -> resolveEachPlayer(gameData, entry, dealing, e, false);
                case TARGET_PERMANENT_CONTROLLER -> resolveTargetPermanentController(gameData, entry, dealing, e);
                case TARGET_SPELL_CONTROLLER -> resolveTargetSpellController(gameData, entry, dealing, e);
            }

            if (e.recordDamageDealt()) {
                entry.setEventValue(gameData.damageDealtThisTurnBySource.getOrDefault(damageSourceId, 0)
                        - damageBefore);
            }
            gameOutcomeService.checkWinCondition(gameData);
        } finally {
            gameData.unpreventableDamageInProgress = previousUnpreventable;
        }
    }

    private void resolveEnchantedPlayerOrPlaneswalker(GameData gameData, StackEntry entry, StackEntry dealing,
                                                     DealDamageToPlayersEffect effect) {
        UUID enchantedPlayer = entry.getOpponentChosenTargetPlayerId();
        if (enchantedPlayer == null) {
            enchantedPlayer = entry.getTargetId();
            if (enchantedPlayer == null || !gameData.playerIds.contains(enchantedPlayer)) return;
            entry.setOpponentChosenTargetPlayerId(enchantedPlayer);
            List<UUID> planeswalkers = gameData.playerBattlefields.getOrDefault(enchantedPlayer, List.of()).stream()
                    .filter(permanent -> gameQueryService.isPlaneswalker(gameData, permanent))
                    .map(Permanent::getId).toList();
            if (!planeswalkers.isEmpty()) {
                entry.setTargetId(null);
                gameData.rerunCurrentEffectAfterInteraction = true;
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.RandomOpponentDamageChoice(entry.getCard(), entry.getControllerId()));
                playerInputService.beginAnyTargetChoice(gameData, entry.getControllerId(), planeswalkers,
                        List.of(enchantedPlayer), entry.getCard().getName()
                                + " ? Choose the enchanted player or a planeswalker they control.");
                return;
            }
        }
        gameData.rerunCurrentEffectAfterInteraction = false;
        UUID recipient = entry.getTargetId() == null ? enchantedPlayer : entry.getTargetId();
        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, dealing)) {
            int amount = evaluateAmount(gameData, entry, effect, enchantedPlayer);
            damageSupport.resolveAnyTargetDamage(gameData, dealing, recipient,
                    amount, false);
        }
    }

    /** Deals damage to the player already identified by the stack entry's target. */
    private void resolveSingleTargetPlayer(GameData gameData, StackEntry entry, StackEntry dealing, DealDamageToPlayersEffect e) {
        UUID targetId = entry.getTargetId();
        if (targetId == null || !gameData.playerIds.contains(targetId)) return;

        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, dealing)) {
            int amount = evaluateAmount(gameData, entry, e, targetId);
            int rawDamage = amount;
            damageSupport.dealDamageToPlayer(gameData, dealing, targetId, rawDamage);
        }
    }

    private void resolveTriggeringPermanentController(GameData gameData, StackEntry entry, StackEntry dealing,
                                                     DealDamageToPlayersEffect effect) {
        UUID controllerId = entry.getTriggeringPermanentId() == null ? null
                : gameQueryService.findPermanentController(gameData, entry.getTriggeringPermanentId());
        if (controllerId == null) {
            controllerId = entry.getRemovedPermanentControllers().get(entry.getTriggeringPermanentId());
        }
        if (controllerId == null) {
            controllerId = entry.getTriggeringPermanentControllerId();
        }
        if (controllerId == null) {
            controllerId = entry.getTargetId();
        }
        if (controllerId != null && gameData.playerIds.contains(controllerId)
                && !damageSupport.isDamageSourcePreventedWithLog(gameData, dealing)) {
            int amount = evaluateAmount(gameData, entry, effect, controllerId);
            damageSupport.dealDamageToPlayer(gameData, dealing, controllerId,
                    amount);
        }
    }

    /** CONTROLLER: "deals N damage to you". */
    private void resolveController(GameData gameData, StackEntry entry, StackEntry dealing, DealDamageToPlayersEffect e) {
        if (gameQueryService.isDamageFromStackEntryPrevented(gameData, dealing)) {
            gameLogService.append(gameData, GameLog.cardThen(dealing.getCard(), "'s damage to controller is prevented."));
        } else {
            int amount = evaluateAmount(gameData, entry, e, entry.getControllerId());
            int rawDamage = amount;
            damageSupport.dealDamageToPlayer(gameData, dealing, entry.getControllerId(), rawDamage);
        }
    }

    /** DEFENDING_PLAYER: the player attacked by the source, including a planeswalker's controller. */
    private void resolveDefendingPlayer(GameData gameData, StackEntry entry, StackEntry dealing, DealDamageToPlayersEffect e) {
        UUID attackedTargetId = entry.getAttackedTargetId();
        if (attackedTargetId == null) return;

        UUID defendingPlayerId = entry.getDefendingPlayerId() != null
                ? entry.getDefendingPlayerId()
                : gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
        if (defendingPlayerId == null || !gameData.playerIds.contains(defendingPlayerId)
                || damageSupport.isDamageSourcePreventedWithLog(gameData, dealing)) {
            return;
        }

        int amount = evaluateAmount(gameData, entry, e, defendingPlayerId);
        int rawDamage = amount;
        damageSupport.dealDamageToPlayer(gameData, dealing, defendingPlayerId, rawDamage);
    }

    /** EACH_OPPONENT uses one shared amount; EACH_PLAYER evaluates player-relative amounts separately. */
    private void resolveEachPlayer(GameData gameData, StackEntry entry, StackEntry dealing, DealDamageToPlayersEffect e, boolean opponentsOnly) {
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, dealing)) return;

        UUID controllerId = entry.getControllerId();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (opponentsOnly && playerId.equals(controllerId)) continue;
            int evaluated = opponentsOnly
                    ? evaluateAmount(gameData, entry, e, controllerId)
                    : evaluateAmount(gameData, entry, e, playerId);
            int damage = evaluated;
            damageSupport.dealDamageToPlayer(gameData, dealing, playerId, damage);
        }
    }

    private void resolveSelectedOpponents(GameData gameData, StackEntry entry, StackEntry dealing,
                                           DealDamageToPlayersEffect e) {
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, dealing)) return;

        UUID controllerId = entry.getControllerId();
        for (UUID playerId : entry.getAcceptedPlayerIds()) {
            if (!gameData.playerIds.contains(playerId) || playerId.equals(controllerId)) continue;
            int amount = evaluateAmount(gameData, entry, e, controllerId);
            int damage = amount;
            damageSupport.dealDamageToPlayer(gameData, dealing, playerId, damage);
        }
    }

    private void resolveEachOtherOpponent(GameData gameData, StackEntry entry, StackEntry dealing, DealDamageToPlayersEffect e) {
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, dealing)) return;

        UUID controllerId = entry.getControllerId();
        UUID excludedPlayerId = entry.getTargetId();
        int evaluated = evaluateAmount(gameData, entry, e, controllerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId) || playerId.equals(excludedPlayerId)) continue;
            int damage = evaluated;
            damageSupport.dealDamageToPlayer(gameData, dealing, playerId, damage);
        }
    }

    /** TARGET_PERMANENT_CONTROLLER: victim = the controller of the targeted permanent. */
    private void resolveTargetPermanentController(GameData gameData, StackEntry entry, StackEntry dealing, DealDamageToPlayersEffect e) {
        java.util.List<UUID> effectTargets = entry.targetsForEffect(e);
        UUID targetId = effectTargets.isEmpty() ? entry.getTargetId() : effectTargets.getFirst();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        // A trigger that fired for a permanent's controller ("that player") keeps that player even
        // if the permanent has since left or changed controllers.
        UUID controllerId = targetId != null && targetId.equals(entry.getTriggeringPermanentId())
                && entry.getTriggeringPermanentControllerId() != null
                ? entry.getTriggeringPermanentControllerId()
                : target == null ? entry.getRemovedPermanentControllers().get(targetId)
                : gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId == null) return;
        String cardName = dealing.getCard().getName();

        if (gameQueryService.isDamageFromStackEntryPrevented(gameData, dealing)) {
            gameLogService.append(gameData, GameLog.text(cardName + "'s damage to " + gameData.playerIdToName.get(controllerId) + " is prevented."));
        } else {
            int amount = evaluateAmount(gameData, entry, e, controllerId);
            int rawDamage = amount;
            damageSupport.dealDamageToPlayer(gameData, dealing, controllerId, rawDamage);
        }
    }

    /**
     * TARGET_SPELL_CONTROLLER: victim = the controller of the targeted spell. When an earlier effect
     * of this resolution countered the spell, the controller remembered at that counter is used.
     */
    private void resolveTargetSpellController(GameData gameData, StackEntry entry, StackEntry dealing, DealDamageToPlayersEffect e) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetSpell = null;
        for (StackEntry se : gameData.stack) {
            if (se.getTargetableId().equals(targetCardId)) {
                targetSpell = se;
                break;
            }
        }
        UUID victimId = targetSpell != null ? targetSpell.getControllerId() : entry.getCounteredSpellControllerId();
        if (victimId == null) {
            victimId = entry.getRemovedPermanentControllers().get(targetCardId);
        }
        if (victimId == null) return;

        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, dealing)) {
            int amount = evaluateAmount(gameData, entry, e, victimId);
            if (amount <= 0) return;
            int rawDamage = amount;
            damageSupport.dealDamageToPlayer(gameData, dealing, victimId, rawDamage);
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
        AmountContext context = AmountContext.forStackEntry(entry, source);
        if (e.recipient() == DamageRecipient.EACH_PLAYER) {
            context = context.withControllerId(victimId);
        } else if (e.recipient() == DamageRecipient.DEFENDING_PLAYER) {
            context = context.withTargetPermanentId(victimId);
        }
        return amountEvaluationService.evaluate(gameData, e.amount(), context);
    }
}
