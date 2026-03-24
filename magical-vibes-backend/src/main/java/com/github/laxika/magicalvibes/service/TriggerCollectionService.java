package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.input.PlayerInputService;

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
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetOnControllerSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
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

        // GRAVEYARD_ON_CONTROLLER_CASTS_SPELL — graveyard-resident spell-cast triggers
        // (e.g. Lingering Phantom: "Whenever you cast a historic spell, you may pay {B}. If you do, return ~ to hand.")
        List<Card> castingPlayerGraveyard = gameData.playerGraveyards.get(castingPlayerId);
        if (castingPlayerGraveyard != null) {
            for (Card card : new ArrayList<>(castingPlayerGraveyard)) {
                List<CardEffect> graveyardEffects = card.getEffects(EffectSlot.GRAVEYARD_ON_CONTROLLER_CASTS_SPELL);
                if (graveyardEffects == null || graveyardEffects.isEmpty()) continue;

                for (CardEffect effect : graveyardEffects) {
                    if (effect instanceof SpellCastTriggerEffect trigger) {
                        if (!gameQueryService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)) continue;

                        if (trigger.manaCost() != null) {
                            // "you may pay {X}" pattern — queue MayPayManaEffect on the stack
                            CardEffect resolvedEffect = trigger.resolvedEffects().getFirst();
                            MayPayManaEffect mayPay = new MayPayManaEffect(
                                    trigger.manaCost(),
                                    resolvedEffect,
                                    "Pay " + trigger.manaCost() + " to return " + card.getName()
                                            + " from your graveyard to your hand?"
                            );
                            gameData.queueMayAbility(card, castingPlayerId, mayPay, null);
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    card,
                                    castingPlayerId,
                                    card.getName() + "'s ability",
                                    new ArrayList<>(trigger.resolvedEffects())
                            ));
                        }

                        log.info("Game {} - {} graveyard spell-cast trigger queued",
                                gameData.id, card.getName());
                    }
                }
            }
        }

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

        // Primal Wellspring delayed mana trigger: copy next instant/sorcery (one-shot)
        Integer pendingCopies = gameData.pendingNextInstantSorceryCopyCount.get(castingPlayerId);
        if (pendingCopies != null && pendingCopies > 0
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                StackEntry snapshot = new StackEntry(spellEntry);
                CopyControllerCastSpellEffect copyEffect =
                        new CopyControllerCastSpellEffect(snapshot, castingPlayerId);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        "Copy " + spellCard.getName() + " (Primal Wellspring)",
                        new ArrayList<>(List.of(copyEffect))
                ));
                // Decrement — one-shot trigger
                int remaining = pendingCopies - 1;
                if (remaining <= 0) {
                    gameData.pendingNextInstantSorceryCopyCount.remove(castingPlayerId);
                } else {
                    gameData.pendingNextInstantSorceryCopyCount.put(castingPlayerId, remaining);
                }
                String logMsg = spellCard.getName() + " is copied (Primal Wellspring).";
                gameBroadcastService.logAndBroadcast(gameData, logMsg);
                log.info("Game {} - {} spell-copy trigger queued for {} (Primal Wellspring)",
                        gameData.id, spellCard.getName(), castingPlayerId);
            }
        }

        // "Until end of turn, whenever you cast an instant or sorcery spell, copy it"
        // (e.g. The Mirari Conjecture chapter III)
        if (gameData.playersWithSpellCopyUntilEndOfTurn.contains(castingPlayerId)
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            // Find the spell on the stack to create a snapshot
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                StackEntry snapshot = new StackEntry(spellEntry);
                CopyControllerCastSpellEffect copyEffect =
                        new CopyControllerCastSpellEffect(snapshot, castingPlayerId);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        "Copy " + spellCard.getName(),
                        new ArrayList<>(List.of(copyEffect))
                ));
                String logMsg = spellCard.getName() + " is copied (The Mirari Conjecture).";
                gameBroadcastService.logAndBroadcast(gameData, logMsg);
                log.info("Game {} - {} spell-copy trigger queued for {}",
                        gameData.id, spellCard.getName(), castingPlayerId);
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
        // Skip EnterBattlefieldOnDiscardEffect — it's a replacement effect handled earlier in the discard flow
        if (discardedCard != null && gameData.discardCausedByOpponent) {
            List<CardEffect> selfTriggers = discardedCard.getEffects(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT).stream()
                    .filter(e -> !(e instanceof EnterBattlefieldOnDiscardEffect))
                    .toList();
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

    public void checkAllyPermanentSacrificedTriggers(GameData gameData, UUID sacrificingPlayerId, Card sacrificedCard) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(sacrificingPlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.AllySacrificed(sacrificingPlayerId, sacrificedCard);

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
        if (spellEntry.getTargetId() != null
                && spellEntry.getTargetZone() == null) {
            targetIds.add(spellEntry.getTargetId());
        }
        if (spellEntry.getTargetIds() != null) {
            targetIds.addAll(spellEntry.getTargetIds());
        }

        for (UUID targetId : targetIds) {
            if (gameData.playerIds.contains(targetId)) continue;

            Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPermanent == null) continue;

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());
            if (controllerId == null) continue;

            collectBecomesTargetTriggers(gameData, targetPermanent, controllerId, targetPermanent);
            collectBecomesTargetOfOpponentSpellTriggers(gameData, targetPermanent, controllerId, spellEntry);
            collectAllyCreatureBecomesTargetOfOpponentTriggers(gameData, targetPermanent, controllerId, spellEntry.getControllerId());
            // Check the targeted permanent itself for "when this becomes the target" triggers.
            // Attached permanents (auras/equipment) use the loop below instead — their triggers
            // monitor the enchanted/equipped creature, not themselves.
            if (!targetPermanent.isAttached()) {
                collectBecomesTargetOfSpellOrAbilityTriggers(gameData, targetPermanent, controllerId);
            }

            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) continue;
                for (Permanent attached : battlefield) {
                    if (attached.isAttached()
                            && attached.getAttachedTo().equals(targetPermanent.getId())) {
                        collectBecomesTargetTriggers(gameData, attached, controllerId, targetPermanent);
                        collectBecomesTargetOfOpponentSpellTriggers(gameData, attached, controllerId, spellEntry);
                        // CR 603.3b: triggered ability is controlled by the controller of the
                        // permanent that has it (the aura/equipment), not the enchanted creature.
                        collectBecomesTargetOfSpellOrAbilityTriggers(gameData, attached, playerId);
                    }
                }
            }
        }

        if (!gameData.pendingSpellTargetTriggers.isEmpty()) {
            processNextSpellTargetTrigger(gameData);
        }
    }

    /**
     * Checks becomes-target triggers for activated/triggered abilities that target permanents.
     * Must be called after an ability is pushed onto the stack with a target permanent.
     */
    public void checkBecomesTargetOfAbilityTriggers(GameData gameData) {
        if (gameData.stack.isEmpty()) return;
        StackEntry abilityEntry = gameData.stack.getLast();
        List<UUID> targetIds = new ArrayList<>();
        if (abilityEntry.getTargetId() != null
                && abilityEntry.getTargetZone() == null
                && !abilityEntry.isNonTargeting()) {
            targetIds.add(abilityEntry.getTargetId());
        }
        if (abilityEntry.getTargetIds() != null) {
            targetIds.addAll(abilityEntry.getTargetIds());
        }

        for (UUID targetId : targetIds) {
            if (gameData.playerIds.contains(targetId)) continue;

            Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPermanent == null) continue;

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());

            // Check the targeted permanent itself for "when this becomes the target" triggers.
            // Attached permanents (auras/equipment) use the loop below instead.
            if (!targetPermanent.isAttached() && controllerId != null) {
                collectBecomesTargetOfSpellOrAbilityTriggers(gameData, targetPermanent, controllerId);
            }

            // Check for "whenever a creature you control becomes the target of opponent's spell or ability"
            if (controllerId != null) {
                collectAllyCreatureBecomesTargetOfOpponentTriggers(gameData, targetPermanent, controllerId, abilityEntry.getControllerId());
            }

            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) continue;
                for (Permanent attached : battlefield) {
                    if (attached.isAttached()
                            && attached.getAttachedTo().equals(targetPermanent.getId())) {
                        // CR 603.3b: triggered ability controlled by the aura/equipment's controller
                        collectBecomesTargetOfSpellOrAbilityTriggers(gameData, attached, playerId);
                    }
                }
            }
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

    private void collectBecomesTargetOfSpellOrAbilityTriggers(GameData gameData, Permanent source, UUID controllerId) {
        List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY);
        if (effects.isEmpty()) return;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                controllerId,
                source.getCard().getName() + "'s triggered ability",
                new ArrayList<>(effects),
                null,
                source.getId()
        );
        gameData.stack.add(entry);

        String logEntry = source.getCard().getName() + "'s triggered ability triggers.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} becomes-target-of-spell-or-ability trigger queued", gameData.id, source.getCard().getName());
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

    /**
     * Checks ALL permanents on the targeted creature's controller's battlefield for
     * {@link EffectSlot#ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY}.
     * Only fires when the targeted permanent is a creature and the spell/ability
     * is controlled by an opponent of the creature's controller.
     */
    private void collectAllyCreatureBecomesTargetOfOpponentTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, UUID spellOrAbilityControllerId) {
        // Only trigger for creatures
        if (!targetPermanent.getCard().hasType(CardType.CREATURE)) return;
        // Only trigger if the spell/ability is controlled by an opponent
        if (creatureControllerId.equals(spellOrAbilityControllerId)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            List<CardEffect> effects = source.getCard().getEffects(
                    EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY);
            if (effects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    creatureControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(effects),
                    null,
                    source.getId()
            );
            gameData.stack.add(entry);

            String logEntry = source.getCard().getName() + "'s triggered ability triggers.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} ally-creature-becomes-target-of-opponent trigger queued",
                    gameData.id, source.getCard().getName());
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

    // ── Opponent-creature-dealt-damage triggers ──────────────────────────

    /**
     * Fires ON_OPPONENT_CREATURE_DEALT_DAMAGE triggers on permanents whose controller
     * is different from the damaged creature's controller (i.e. the damaged creature is
     * an opponent's creature from the perspective of the permanent's controller).
     * Called once per damaged creature — each call produces one trigger per listening permanent.
     */
    public void checkOpponentCreatureDealtDamageTriggers(GameData gameData, UUID damagedCreatureControllerId) {
        gameData.forEachPermanent((playerId, perm) -> {
            // Only fire when the damaged creature was controlled by an opponent of this permanent's controller
            if (playerId.equals(damagedCreatureControllerId)) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_CREATURE_DEALT_DAMAGE);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        playerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers (opponent creature dealt damage)", gameData.id, perm.getCard().getName());
            }
        });
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
            if (!perm.isAttached() || !perm.getAttachedTo().equals(tappedPermanent.getId())) {
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

        // Controller-loses-life triggers (e.g. Lich's Mastery)
        // Snapshot: handlers may modify the battlefield (exile permanents)
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (!playerId.equals(losingPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_LOSES_LIFE)) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    if (registry.dispatch(match, EffectSlot.ON_CONTROLLER_LOSES_LIFE, effect, ctx)) {
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
        checkLifeGainTriggers(gameData, gainingPlayerId, lifeGainedAmount, null, null);
    }

    public void checkLifeGainTriggers(GameData gameData, UUID gainingPlayerId, int lifeGainedAmount,
                                       Card sourceCard, StackEntryType sourceEntryType) {
        if (lifeGainedAmount <= 0) return;

        var ctx = new TriggerContext.LifeGain(gainingPlayerId, lifeGainedAmount, sourceCard, sourceEntryType);

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

    // ── Creature-card-milled triggers ─────────────────────────────────

    public void checkCreatureCardMilledTriggers(GameData gameData, UUID milledPlayerId, Card milledCard) {
        var ctx = new TriggerContext.CreatureCardMilled(milledPlayerId, milledCard);

        // Snapshot battlefields: trigger handlers may add tokens to the battlefield
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(milledPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_CREATURE_CARD_MILLED)) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    registry.dispatch(match, EffectSlot.ON_OPPONENT_CREATURE_CARD_MILLED, effect, ctx);
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

    public void processNextSpellGraveyardTargetTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextSpellGraveyardTargetTrigger(gameData);
    }

    public void processNextEmblemTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextEmblemTriggerTarget(gameData);
    }

    public void processNextLifeGainTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextLifeGainTriggerTarget(gameData);
    }

    public void processNextSagaChapterTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSagaChapterTarget(gameData);
    }

    public void processNextSagaChapterGraveyardTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSagaChapterGraveyardTarget(gameData);
    }

    // ── Explore triggers ──────────────────────────────────────────────

    /**
     * Scans the exploring creature's controller's battlefield for permanents
     * with {@link EffectSlot#ON_ALLY_CREATURE_EXPLORES} effects and queues
     * them for target selection or directly onto the stack.
     */
    public void checkExploreTriggers(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_EXPLORES);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                if (effect.canTargetPermanent()) {
                    gameData.pendingExploreTriggerTargets.add(
                            new PermanentChoiceContext.ExploreTriggerTarget(
                                    perm.getCard(), controllerId, new ArrayList<>(List.of(effect)), perm.getId()));
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect))
                    ));
                }
                log.info("Game {} - {} explore trigger queued", gameData.id, perm.getCard().getName());
            }
        }
    }

    public void processNextExploreTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextExploreTriggerTarget(gameData);
    }

    // ── Death / leaves-battlefield triggers ───────────────────────────

    public void collectDeathTrigger(GameData gameData, Card dyingCard, UUID controllerId, boolean wasCreature) {
        collectDeathTrigger(gameData, dyingCard, controllerId, wasCreature, null);
    }

    public void collectDeathTrigger(GameData gameData, Card dyingCard, UUID controllerId, boolean wasCreature, Permanent dyingPermanent) {
        List<CardEffect> deathEffects = dyingCard.getEffects(EffectSlot.ON_DEATH);
        if (deathEffects.isEmpty()) return;

        var ctx = new TriggerContext.SelfDeath(dyingCard, controllerId, wasCreature, dyingPermanent);
        Permanent perm = dyingPermanent != null ? dyingPermanent : new Permanent(dyingCard);
        for (CardEffect effect : deathEffects) {
            var match = new TriggerMatchContext(gameData, perm, controllerId, effect);
            registry.dispatch(match, EffectSlot.ON_DEATH, effect, ctx);
        }
    }

    public void checkAllyCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId, Card dyingCard) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingCreatureControllerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingCreatureControllerId);

        for (Permanent perm : battlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) continue;

            boolean anyEffectFired = false;
            List<CardEffect> stackEffects = new ArrayList<>();

            for (CardEffect effect : effects) {
                CardEffect resolvedEffect = effect;
                if (effect instanceof SubtypeConditionalEffect filtered) {
                    if (!gameQueryService.cardHasSubtype(dyingCard, filtered.subtype(), gameData, dyingCreatureControllerId)) {
                        continue;
                    }
                    resolvedEffect = filtered.wrapped();
                }

                if (resolvedEffect instanceof MayPayManaEffect || resolvedEffect instanceof MayEffect) {
                    var match = new TriggerMatchContext(gameData, perm, dyingCreatureControllerId, resolvedEffect);
                    registry.dispatch(match, EffectSlot.ON_ALLY_CREATURE_DIES, resolvedEffect, ctx);
                    anyEffectFired = true;
                } else {
                    stackEffects.add(resolvedEffect);
                }
            }

            if (!stackEffects.isEmpty()) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        dyingCreatureControllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(stackEffects),
                        null,
                        perm.getId()
                ));
                anyEffectFired = true;
            }

            if (anyEffectFired) {
                String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers (ally creature died)", gameData.id, perm.getCard().getName());
            }
        }
    }

    public void checkEquippedCreatureDeathTriggers(GameData gameData, UUID dyingCreatureId, UUID dyingCreatureControllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingCreatureControllerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.EquippedCreatureDeath(dyingCreatureId, dyingCreatureControllerId);

        for (Permanent perm : battlefield) {
            if (!dyingCreatureId.equals(perm.getAttachedTo())) continue;
            if (!perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_EQUIPPED_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                var match = new TriggerMatchContext(gameData, perm, dyingCreatureControllerId, effect);
                registry.dispatch(match, EffectSlot.ON_EQUIPPED_CREATURE_DIES, effect, ctx);
            }
        }
    }

    public void checkEnchantedPermanentDeathTriggers(GameData gameData, UUID dyingPermanentId) {
        checkEnchantedPermanentDeathTriggers(gameData, dyingPermanentId, null, null);
    }

    public void checkEnchantedPermanentDeathTriggers(GameData gameData, UUID dyingPermanentId, UUID dyingPermanentControllerId) {
        checkEnchantedPermanentDeathTriggers(gameData, dyingPermanentId, dyingPermanentControllerId, null);
    }

    public void checkEnchantedPermanentDeathTriggers(GameData gameData, UUID dyingPermanentId,
                                                      UUID dyingPermanentControllerId, UUID dyingCreatureCardId) {
        var ctx = new TriggerContext.EnchantedPermanentDeath(dyingPermanentId, dyingPermanentControllerId, dyingCreatureCardId);

        gameData.forEachPermanent((playerId, perm) -> {
            if (!dyingPermanentId.equals(perm.getAttachedTo())) return;
            if (perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                registry.dispatch(match, EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, effect, ctx);
            }
        });
    }

    public void checkEnchantedPermanentLTBTriggers(GameData gameData, Permanent leavingPermanent) {
        var ctx = new TriggerContext.EnchantedPermanentLeaves(leavingPermanent);

        gameData.forEachPermanent((playerId, perm) -> {
            if (!leavingPermanent.getId().equals(perm.getAttachedTo())) return;
            if (perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                registry.dispatch(match, EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD, effect, ctx);
            }
        });
    }

    public void checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(GameData gameData, UUID graveyardOwnerId, UUID artifactControllerId) {
        var ctx = new TriggerContext.ArtifactGraveyard(graveyardOwnerId, artifactControllerId);

        gameData.forEachPermanent((playerId, perm) -> {
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);

            if (!playerId.equals(graveyardOwnerId)) {
                dispatchSlot(gameData, perm, playerId, EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD, ctx);
            }
        });
    }

    public void checkAnyCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId, Card dyingCard) {
        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingCreatureControllerId);

        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                CardEffect resolvedEffect = effect;
                if (effect instanceof SubtypeConditionalEffect filtered) {
                    if (!gameQueryService.cardHasSubtype(dyingCard, filtered.subtype(), gameData, dyingCreatureControllerId)) {
                        continue;
                    }
                    resolvedEffect = filtered.wrapped();
                }
                var match = new TriggerMatchContext(gameData, perm, playerId, resolvedEffect);
                registry.dispatch(match, EffectSlot.ON_ANY_CREATURE_DIES, resolvedEffect, ctx);
            }
        });
    }

    public void checkAllyNontokenCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId, Card dyingCard) {
        if (dyingCard.isToken()) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingCreatureControllerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingCreatureControllerId);

        for (Permanent perm : battlefield) {
            dispatchSlot(gameData, perm, dyingCreatureControllerId, EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, ctx);
        }
    }

    public void checkAnyNontokenCreatureDeathTriggers(GameData gameData, Card dyingCard) {
        if (dyingCard.isToken()) return;

        var ctx = new TriggerContext.CreatureDeath(dyingCard, null);

        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                registry.dispatch(match, EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES, effect, ctx);
            }
        });
    }

    public void checkOpponentCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId) {
        var ctx = new TriggerContext.CreatureDeath(null, dyingCreatureControllerId);

        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(dyingCreatureControllerId)) return;
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_OPPONENT_CREATURE_DIES, ctx);
        });
    }

    public void checkSelfLeavesTriggered(GameData gameData, Permanent target, UUID controllerId) {
        List<CardEffect> effects = target.getCard().getEffects(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD);
        if (effects == null || effects.isEmpty()) return;

        var ctx = new TriggerContext.SelfLeaves(controllerId);

        for (CardEffect effect : effects) {
            var match = new TriggerMatchContext(gameData, target, controllerId, effect);
            registry.dispatch(match, EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, effect, ctx);
        }
    }

    public void checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(GameData gameData, Card dyingCard, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.AllyAuraOrEquipmentGraveyard(dyingCard, controllerId);

        for (Permanent perm : battlefield) {
            dispatchSlot(gameData, perm, controllerId, EffectSlot.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);
        }
    }

    public void triggerDelayedPoisonOnDeath(GameData gameData, UUID dyingCreatureCardId, UUID controllerId) {
        Integer poisonAmount = gameData.creatureGivingControllerPoisonOnDeathThisTurn.remove(dyingCreatureCardId);
        if (poisonAmount == null || poisonAmount <= 0) {
            return;
        }

        if (!gameQueryService.canPlayerGetPoisonCounters(gameData, controllerId)) return;

        int currentPoison = gameData.playerPoisonCounters.getOrDefault(controllerId, 0);
        gameData.playerPoisonCounters.put(controllerId, currentPoison + poisonAmount);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " gets " + poisonAmount + " poison counter"
                + (poisonAmount > 1 ? "s" : "") + " (delayed trigger: creature died this turn).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets {} poison counter(s) (delayed trigger: creature died this turn)",
                gameData.id, playerName, poisonAmount);
    }

    // ── Internal dispatch ──────────────────────────────────────────────

    private void dispatchSlot(GameData gameData, Permanent perm, UUID controllerId, EffectSlot slot, TriggerContext ctx) {
        if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return;
        for (CardEffect effect : perm.getCard().getEffects(slot)) {
            var match = new TriggerMatchContext(gameData, perm, controllerId, effect);
            registry.dispatch(match, slot, effect, ctx);
        }
    }
}
