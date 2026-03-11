package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetOnControllerSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectorRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerContext;
import com.github.laxika.magicalvibes.service.trigger.TriggerMatchContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Thin orchestrator that detects trigger events, iterates permanents/effect-slots,
 * and delegates per-effect handling to the {@link TriggerCollectorRegistry}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TriggerCollectionService {

    private final TriggerCollectorRegistry registry;
    private final GameOutcomeService gameOutcomeService;
    private final PlayerInputService playerInputService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    // ── Spell-cast triggers ────────────────────────────────────────────

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId, true);
    }

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId, boolean castFromHand) {
        var ctx = new TriggerContext.SpellCast(spellCard, castingPlayerId, castFromHand);

        // Opening hand reveal delayed triggers (Chancellor cycle)
        if (!gameData.openingHandRevealTriggers.isEmpty()
                && !gameData.playersWhoCastFirstSpellInGame.contains(castingPlayerId)) {
            gameData.playersWhoCastFirstSpellInGame.add(castingPlayerId);
            for (OpeningHandRevealTrigger trigger : gameData.openingHandRevealTriggers) {
                if (!trigger.revealingPlayerId().equals(castingPlayerId)
                        && trigger.effect() instanceof CounterUnlessPaysEffect counterEffect) {
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            trigger.sourceCard(),
                            trigger.revealingPlayerId(),
                            trigger.sourceCard().getName() + "'s ability",
                            new ArrayList<>(List.of(counterEffect)),
                            spellCard.getId(),
                            Zone.STACK
                    );
                    gameData.stack.add(entry);
                }
            }
        }

        // ON_ANY_PLAYER_CASTS_SPELL
        gameData.forEachPermanent((playerId, perm) -> {
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, ctx);
        });

        // ON_CONTROLLER_CASTS_SPELL (only controller's own spells)
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(castingPlayerId)) return;
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_CONTROLLER_CASTS_SPELL, ctx);
        });

        // ON_OPPONENT_CASTS_SPELL (only opponents' permanents)
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(castingPlayerId)) return;
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_OPPONENT_CASTS_SPELL, ctx);
        });

        // Emblem spell cast triggers (e.g. Venser's emblem)
        for (Emblem emblem : gameData.emblems) {
            if (!emblem.controllerId().equals(castingPlayerId)) continue;
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof ExileTargetOnControllerSpellCastEffect) {
                    gameData.pendingEmblemTriggerTargets.add(new PermanentChoiceContext.EmblemTriggerTarget(
                            "Venser's emblem",
                            emblem.controllerId(),
                            List.of(new ExileTargetPermanentEffect()),
                            emblem.sourceCard()
                    ));
                }
            }
        }

        if (!gameData.pendingEmblemTriggerTargets.isEmpty()) {
            triggeredAbilityQueueService.processNextEmblemTriggerTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        playerInputService.processNextMayAbility(gameData);
    }

    // ── Discard triggers ───────────────────────────────────────────────

    public void checkDiscardTriggers(GameData gameData, UUID discardingPlayerId, Card discardedCard) {
        boolean[] anyTriggered = {false};
        var ctx = new TriggerContext.Discard(discardingPlayerId, discardedCard);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(discardingPlayerId)) return;

            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_DISCARDS)) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    if (registry.dispatch(match, EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        });

        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }

        // Process any pending may abilities added by discard triggers
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }

        // Check the discarded card itself for self-discard triggers (e.g. Guerrilla Tactics)
        if (discardedCard != null && gameData.discardCausedByOpponent) {
            List<CardEffect> selfTriggers = discardedCard.getEffects(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT);
            if (!selfTriggers.isEmpty()) {
                gameData.pendingDiscardSelfTriggers.add(new PermanentChoiceContext.DiscardTriggerAnyTarget(
                        discardedCard, discardingPlayerId, new ArrayList<>(selfTriggers)
                ));
                String logEntry = discardedCard.getName() + " was discarded by an opponent's effect — its ability triggers!";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} self-discard trigger queued", gameData.id, discardedCard.getName());
            }
        }
    }

    // ── Damage-dealt-to-controller triggers ────────────────────────────

    public void checkDamageDealtToControllerTriggers(GameData gameData, UUID damagedPlayerId, UUID sourcePermanentId, boolean isCombatDamage) {
        if (sourcePermanentId == null) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (damagedPlayerBattlefield == null) return;

        boolean hasTrigger = false;
        for (Permanent perm : damagedPlayerBattlefield) {
            if (!perm.getCard().getEffects(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU).isEmpty()) {
                hasTrigger = true;
                break;
            }
        }
        if (!hasTrigger) return;

        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent == null) return;

        var ctx = new TriggerContext.DamageToController(damagedPlayerId, sourcePermanentId, isCombatDamage);

        for (Permanent perm : new ArrayList<>(damagedPlayerBattlefield)) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU)) {
                var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                boolean triggered = registry.dispatch(match, EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

                // If the source was bounced, stop processing all triggers
                if (triggered && gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
                    return;
                }
            }
        }
    }

    // ── Land-tap triggers ──────────────────────────────────────────────

    public void checkLandTapTriggers(GameData gameData, UUID tappingPlayerId, UUID tappedLandId) {
        boolean[] anyTriggered = {false};
        var ctx = new TriggerContext.LandTap(tappingPlayerId, tappedLandId);

        gameData.forEachPermanent((playerId, perm) -> {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_PLAYER_TAPS_LAND)) {
                var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                if (registry.dispatch(match, EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx)) {
                    anyTriggered[0] = true;
                }
            }
        });

        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    // ── Ally-permanent-sacrificed triggers ──────────────────────────────

    public void checkAllyPermanentSacrificedTriggers(GameData gameData, UUID sacrificingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(sacrificingPlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.AllySacrificed(sacrificingPlayerId);

        for (Permanent perm : battlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                var match = new TriggerMatchContext(gameData, perm, sacrificingPlayerId, effect);
                registry.dispatch(match, EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);
            }
        }

        playerInputService.processNextMayAbility(gameData);
    }

    // ── Becomes-target-of-spell triggers ───────────────────────────────

    public void checkBecomesTargetOfSpellTriggers(GameData gameData) {
        if (gameData.stack.isEmpty()) return;
        checkBecomesTargetOfSpellTriggers(gameData, gameData.stack.getLast());
    }

    public void checkBecomesTargetOfSpellTriggers(GameData gameData, StackEntry spellEntry) {
        List<UUID> targetIds = new ArrayList<>();
        if (spellEntry.getTargetPermanentId() != null
                && spellEntry.getTargetZone() == null) {
            targetIds.add(spellEntry.getTargetPermanentId());
        }
        if (spellEntry.getTargetPermanentIds() != null) {
            targetIds.addAll(spellEntry.getTargetPermanentIds());
        }

        for (UUID targetId : targetIds) {
            if (gameData.playerIds.contains(targetId)) continue;

            Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPermanent == null) continue;

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());
            if (controllerId == null) continue;

            collectBecomesTargetTriggers(gameData, targetPermanent, controllerId, targetPermanent);
            collectBecomesTargetOfOpponentSpellTriggers(gameData, targetPermanent, controllerId, spellEntry);

            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) continue;
                for (Permanent attached : battlefield) {
                    if (attached.getAttachedTo() != null
                            && attached.getAttachedTo().equals(targetPermanent.getId())) {
                        collectBecomesTargetTriggers(gameData, attached, controllerId, targetPermanent);
                        collectBecomesTargetOfOpponentSpellTriggers(gameData, attached, controllerId, spellEntry);
                    }
                }
            }
        }

        if (!gameData.pendingSpellTargetTriggers.isEmpty()) {
            processNextSpellTargetTrigger(gameData);
        }
    }

    private void collectBecomesTargetTriggers(GameData gameData, Permanent source, UUID controllerId, Permanent targetedCreature) {
        List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL);
        if (effects.isEmpty()) return;

        gameData.pendingSpellTargetTriggers.add(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                targetedCreature.getCard(), controllerId, new ArrayList<>(effects)
        ));

        String logEntry = targetedCreature.getCard().getName() + "'s triggered ability triggers — choose a target for damage.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} becomes-target-of-spell trigger queued", gameData.id, targetedCreature.getCard().getName());
    }

    private void collectBecomesTargetOfOpponentSpellTriggers(GameData gameData, Permanent source, UUID controllerId, StackEntry spellEntry) {
        // Only trigger if the spell/ability is controlled by an opponent
        if (controllerId.equals(spellEntry.getControllerId())) return;

        List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL);
        if (effects.isEmpty()) return;

        for (CardEffect effect : effects) {
            if (effect instanceof CounterUnlessPaysEffect counterEffect) {
                // Put counter-unless-pays directly on the stack targeting the spell
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        controllerId,
                        source.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(counterEffect)),
                        spellEntry.getCard().getId(),
                        Zone.STACK
                );
                gameData.stack.add(entry);

                String logEntry = source.getCard().getName() + "'s triggered ability triggers — counter unless controller pays {" + counterEffect.amount() + "}.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} becomes-target-of-opponent-spell counter trigger queued", gameData.id, source.getCard().getName());
            }
        }
    }

    // ── Dealt-damage-to-creature triggers ──────────────────────────────

    public void checkDealtDamageToCreatureTriggers(GameData gameData, Permanent damagedCreature) {
        checkDealtDamageToCreatureTriggers(gameData, damagedCreature, 0, null);
    }

    public void checkDealtDamageToCreatureTriggers(GameData gameData, Permanent damagedCreature, int damageDealt, UUID damageSourceControllerId) {
        List<CardEffect> effects = damagedCreature.getCard().getEffects(EffectSlot.ON_DEALT_DAMAGE);
        if (effects.isEmpty()) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        if (controllerId == null) return;

        var ctx = new TriggerContext.DamageToCreature(damagedCreature, damageDealt, damageSourceControllerId);

        for (CardEffect effect : effects) {
            var match = new TriggerMatchContext(gameData, damagedCreature, controllerId, effect);
            registry.dispatch(match, EffectSlot.ON_DEALT_DAMAGE, effect, ctx);
        }
    }

    // ── Enchanted-permanent-tap triggers ───────────────────────────────

    public void checkEnchantedPermanentTapTriggers(GameData gameData, Permanent tappedPermanent) {
        UUID tappedPermanentControllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(tappedPermanent)) {
                tappedPermanentControllerId = pid;
                break;
            }
        }
        if (tappedPermanentControllerId == null) return;

        var ctx = new TriggerContext.EnchantedPermanentTap(tappedPermanent, tappedPermanentControllerId);

        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (perm.getAttachedTo() == null || !perm.getAttachedTo().equals(tappedPermanent.getId())) {
                return;
            }
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED)) {
                var match = new TriggerMatchContext(gameData, perm, auraOwnerId, effect);
                registry.dispatch(match, EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, effect, ctx);
            }
        });
    }

    // ── Life-loss triggers ─────────────────────────────────────────────

    public void checkLifeLossTriggers(GameData gameData, UUID losingPlayerId, int lifeLostAmount) {
        if (lifeLostAmount <= 0) return;

        boolean[] anyTriggered = {false};
        var ctx = new TriggerContext.LifeLoss(losingPlayerId, lifeLostAmount);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(losingPlayerId)) return;

            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_LOSES_LIFE)) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    if (registry.dispatch(match, EffectSlot.ON_OPPONENT_LOSES_LIFE, effect, ctx)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        });

        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    // ── Life-gain triggers ──────────────────────────────────────────────

    public void checkLifeGainTriggers(GameData gameData, UUID gainingPlayerId, int lifeGainedAmount) {
        if (lifeGainedAmount <= 0) return;

        var ctx = new TriggerContext.LifeGain(gainingPlayerId, lifeGainedAmount);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (!playerId.equals(gainingPlayerId)) return;

            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_GAINS_LIFE)) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    registry.dispatch(match, EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);
                }
            }
        });
    }

    // ── Noncombat-damage-to-opponent triggers ──────────────────────────

    public void checkNoncombatDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId) {
        var ctx = new TriggerContext.NoncombatDamageToOpponent(damagedPlayerId);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(damagedPlayerId)) return;

            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE)) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    registry.dispatch(match, EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE, effect, ctx);
                }
            }
        });
    }

    // ── Queue-processing delegates ─────────────────────────────────────

    public void processNextDeathTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextDeathTriggerTarget(gameData);
    }

    public void processNextAttackTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextAttackTriggerTarget(gameData);
    }

    public void processNextDiscardSelfTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextDiscardSelfTrigger(gameData);
    }

    public void processNextSpellTargetTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextSpellTargetTrigger(gameData);
    }

    public void processNextEmblemTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextEmblemTriggerTarget(gameData);
    }

    // ── Internal dispatch ──────────────────────────────────────────────

    private void dispatchSlot(GameData gameData, Permanent perm, UUID controllerId, EffectSlot slot, TriggerContext ctx) {
        for (CardEffect effect : perm.getCard().getEffects(slot)) {
            var match = new TriggerMatchContext(gameData, perm, controllerId, effect);
            registry.dispatch(match, slot, effect, ctx);
        }
    }
}
