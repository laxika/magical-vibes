package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardOnOwnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveEnchantedPermanentControllerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenOnOwnSpellCastWithCostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAndGrantKeywordOnOwnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetOnArtifactCastEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfOnArtifactCastEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDiscardingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetOnControllerSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeOnOwnSpellCastWithCostEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolExileAndCastEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGainsControlOfThisPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDamageSourcePermanentToHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TriggerCollectionService {

    private final GameHelper gameHelper;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;
    private final CreatureControlService creatureControlService;

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId, true);
    }

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId, boolean castFromHand) {
        gameData.forEachPermanent((playerId, perm) -> {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)) {
                CardEffect inner = effect instanceof MayEffect m ? m.wrapped() : effect;

                if (inner instanceof GainLifeOnSpellCastEffect trigger
                        && gameQueryService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)) {
                    List<CardEffect> resolvedEffects = List.of(new GainLifeEffect(trigger.amount()));

                    if (effect instanceof MayEffect may) {
                        gameData.pendingMayAbilities.add(new PendingMayAbility(
                                perm.getCard(),
                                playerId,
                                resolvedEffects,
                                perm.getCard().getName() + " — " + may.prompt()
                        ));
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(resolvedEffects)
                        ));
                    }
                } else if (inner instanceof PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect trigger
                        && spellCard.getColor() != null
                        && trigger.triggerColors().contains(spellCard.getColor())
                        && !trigger.onlyOwnSpells()) {
                    List<CardEffect> resolvedEffects = List.of(new PutCountersOnSourceEffect(1, 1, trigger.amount()));

                    if (effect instanceof MayEffect may) {
                        gameData.pendingMayAbilities.add(new PendingMayAbility(
                                perm.getCard(),
                                playerId,
                                resolvedEffects,
                                perm.getCard().getName() + " — " + may.prompt()
                        ));
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(resolvedEffects),
                                null,
                                perm.getId()
                        ));
                    }
                } else if (inner instanceof KnowledgePoolCastTriggerEffect && castFromHand) {
                    // Knowledge Pool: only triggers when a spell is cast from hand (prevents infinite loops)
                    // Trigger is controlled by KP's controller (CR 603.3a); caster tracked in effect
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(new KnowledgePoolExileAndCastEffect(spellCard.getId(), perm.getId(), castingPlayerId)))
                    ));
                } else if (inner instanceof CopySpellForEachOtherSubtypePermanentEffect trigger
                        && trigger.spellSnapshot() == null) {
                    // Only triggers for instant or sorcery spells
                    if (spellCard.getType() != CardType.INSTANT && spellCard.getType() != CardType.SORCERY) continue;

                    // Find the spell on the stack
                    StackEntry spellEntry = null;
                    for (StackEntry se : gameData.stack) {
                        if (se.getCard().getId().equals(spellCard.getId())) {
                            spellEntry = se;
                            break;
                        }
                    }
                    if (spellEntry == null) continue;

                    // Determine the single unique target (if any).
                    // Per CR: triggers when spell "targets only a single Golem and no other object or player".
                    // A spell with multiple target slots all pointing at the same Golem still qualifies.
                    UUID singleTargetId = null;

                    if (spellEntry.getTargetPermanentId() != null
                            && spellEntry.getTargetZone() == null
                            && spellEntry.getTargetPermanentIds().isEmpty()) {
                        // Single-target spell targeting a battlefield permanent
                        singleTargetId = spellEntry.getTargetPermanentId();
                    } else if (spellEntry.getTargetPermanentId() == null
                            && !spellEntry.getTargetPermanentIds().isEmpty()
                            && spellEntry.getTargetPermanentIds().stream().distinct().count() == 1) {
                        // Multi-target spell where every slot targets the same permanent
                        singleTargetId = spellEntry.getTargetPermanentIds().getFirst();
                    }

                    if (singleTargetId == null) continue;
                    if (gameData.playerIds.contains(singleTargetId)) continue;

                    // The targeted permanent must have the matching subtype
                    Permanent targetPerm = gameQueryService.findPermanentById(gameData, singleTargetId);
                    if (targetPerm == null) continue;
                    if (!targetPerm.getCard().getSubtypes().contains(trigger.subtype())) continue;

                    // Snapshot the spell so copies can be created even if the original is countered
                    StackEntry snapshot = new StackEntry(spellEntry);

                    CopySpellForEachOtherSubtypePermanentEffect resolutionEffect =
                            new CopySpellForEachOtherSubtypePermanentEffect(
                                    trigger.subtype(), snapshot, castingPlayerId, singleTargetId);

                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(resolutionEffect))
                    ));
                }
            }
        });

        // Check ON_CONTROLLER_CASTS_SPELL effects (only fire for controller's own spells)
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(castingPlayerId)) return;

            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)) {
                CardEffect inner = effect instanceof MayEffect m ? m.wrapped() : effect;

                if (inner instanceof PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect trigger
                        && spellCard.getColor() != null
                        && trigger.triggerColors().contains(spellCard.getColor())) {
                    List<CardEffect> resolvedEffects = List.of(new PutCountersOnSourceEffect(1, 1, trigger.amount()));

                    if (effect instanceof MayEffect may) {
                        gameData.pendingMayAbilities.add(new PendingMayAbility(
                                perm.getCard(),
                                playerId,
                                resolvedEffects,
                                perm.getCard().getName() + " — " + may.prompt()
                        ));
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(resolvedEffects),
                                null,
                                perm.getId()
                        ));
                    }
                } else if (inner instanceof DealDamageToAnyTargetOnArtifactCastEffect trigger
                        && spellCard.getType() == CardType.ARTIFACT) {
                    List<CardEffect> resolvedEffects = List.of(new DealDamageToAnyTargetEffect(trigger.damage()));

                    if (effect instanceof MayEffect may) {
                        gameData.pendingMayAbilities.add(new PendingMayAbility(
                                perm.getCard(),
                                playerId,
                                resolvedEffects,
                                perm.getCard().getName() + " — " + may.prompt(),
                                null,
                                "{" + trigger.manaCost() + "}"
                        ));
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(resolvedEffects)
                        ));
                    }
                } else if (inner instanceof BoostAndGrantKeywordOnOwnSpellCastEffect trigger
                        && gameQueryService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)) {
                    List<CardEffect> resolvedEffects = List.of(
                            new BoostTargetCreatureEffect(trigger.powerBoost(), trigger.toughnessBoost()),
                            new GrantKeywordEffect(trigger.keyword(), GrantScope.TARGET)
                    );

                    if (effect instanceof MayEffect may) {
                        gameData.pendingMayAbilities.add(new PendingMayAbility(
                                perm.getCard(),
                                playerId,
                                resolvedEffects,
                                perm.getCard().getName() + " — " + may.prompt()
                        ));
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(resolvedEffects)
                        ));
                    }
                } else if (inner instanceof GainLifeOnOwnSpellCastWithCostEffect trigger
                        && gameQueryService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)) {
                    List<CardEffect> resolvedEffects = List.of(new GainLifeEffect(trigger.amount()));

                    if (effect instanceof MayEffect may) {
                        gameData.pendingMayAbilities.add(new PendingMayAbility(
                                perm.getCard(),
                                playerId,
                                resolvedEffects,
                                perm.getCard().getName() + " — " + may.prompt(),
                                null,
                                "{" + trigger.manaCost() + "}"
                        ));
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(resolvedEffects)
                        ));
                    }
                } else if (inner instanceof CreateTokenOnOwnSpellCastWithCostEffect trigger
                        && gameQueryService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)) {
                    List<CardEffect> resolvedEffects = List.of(trigger.tokenEffect());

                    if (effect instanceof MayEffect may) {
                        gameData.pendingMayAbilities.add(new PendingMayAbility(
                                perm.getCard(),
                                playerId,
                                resolvedEffects,
                                perm.getCard().getName() + " — " + may.prompt(),
                                null,
                                "{" + trigger.manaCost() + "}"
                        ));
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(resolvedEffects)
                        ));
                    }
                } else if (inner instanceof PutChargeCounterOnSelfOnArtifactCastEffect
                        && spellCard.getType() == CardType.ARTIFACT) {
                    List<CardEffect> resolvedEffects = List.of(new PutChargeCounterOnSelfEffect());

                    if (effect instanceof MayEffect may) {
                        gameData.pendingMayAbilities.add(new PendingMayAbility(
                                perm.getCard(),
                                playerId,
                                resolvedEffects,
                                perm.getCard().getName() + " — " + may.prompt()
                        ));
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(resolvedEffects),
                                null,
                                perm.getId()
                        ));
                    }
                } else if (inner instanceof DrawAndDiscardOnOwnSpellCastEffect trigger
                        && gameQueryService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)) {
                    List<CardEffect> resolvedEffects = List.of(new DrawCardEffect(), new DiscardCardEffect());

                    if (effect instanceof MayEffect may) {
                        gameData.pendingMayAbilities.add(new PendingMayAbility(
                                perm.getCard(),
                                playerId,
                                resolvedEffects,
                                perm.getCard().getName() + " — " + may.prompt()
                        ));
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(resolvedEffects)
                        ));
                    }
                } else if (inner instanceof GiveTargetPlayerPoisonCountersEffect trigger
                        && trigger.spellFilter() != null
                        && gameQueryService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)) {
                    // Find the opponent to auto-target
                    UUID opponentId = gameData.orderedPlayerIds.stream()
                            .filter(id -> !id.equals(playerId))
                            .findFirst().orElse(null);
                    if (opponentId != null) {
                        List<CardEffect> resolvedEffects = List.of(new GiveTargetPlayerPoisonCountersEffect(trigger.amount()));
                        StackEntry entry = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(resolvedEffects)
                        );
                        entry.setTargetPermanentId(opponentId);
                        gameData.stack.add(entry);
                    }
                } else if (inner instanceof ProliferateEffect) {
                    List<CardEffect> resolvedEffects = List.of(new ProliferateEffect());
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(resolvedEffects)
                    ));
                }
            }
        });

        // Check ON_OPPONENT_CASTS_SPELL effects (only fire for opponents' spells)
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(castingPlayerId)) return;

            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_CASTS_SPELL)) {
                if (effect instanceof LoseLifeUnlessDiscardEffect trigger) {
                    List<CardEffect> resolvedEffects = List.of(trigger);
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(resolvedEffects)
                    );
                    entry.setTargetPermanentId(castingPlayerId);
                    gameData.stack.add(entry);
                }
            }
        });

        // Check emblem spell cast triggers (e.g. Venser's emblem)
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

    public void checkDiscardTriggers(GameData gameData, UUID discardingPlayerId, Card discardedCard) {
        boolean[] anyTriggered = {false};

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(discardingPlayerId)) return;

            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_DISCARDS)) {
                    if (effect instanceof DealDamageToDiscardingPlayerEffect trigger) {
                        String cardName = perm.getCard().getName();
                        int damage = trigger.damage();

                        String logEntry = cardName + " triggers — deals " + damage + " damage to " + gameData.playerIdToName.get(discardingPlayerId) + ".";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} triggers on discard, dealing {} damage to {}",
                                gameData.id, cardName, damage, gameData.playerIdToName.get(discardingPlayerId));

                        if (!gameQueryService.isDamageFromSourcePrevented(gameData, perm.getEffectiveColor())
                                && !gameHelper.isSourceDamagePreventedForPlayer(gameData, discardingPlayerId, perm.getId())
                                && !gameData.permanentsPreventedFromDealingDamage.contains(perm.getId())
                                && !gameHelper.applyColorDamagePreventionForPlayer(gameData, discardingPlayerId, perm.getEffectiveColor())) {
                            int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, discardingPlayerId, damage);
                            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, discardingPlayerId, effectiveDamage, cardName);
                            if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, discardingPlayerId)) {
                                gameBroadcastService.logAndBroadcast(gameData,
                                        gameData.playerIdToName.get(discardingPlayerId) + "'s life total can't change.");
                            } else {
                                int currentLife = gameData.playerLifeTotals.getOrDefault(discardingPlayerId, 20);
                                gameData.playerLifeTotals.put(discardingPlayerId, currentLife - effectiveDamage);
                            }
                        }

                        anyTriggered[0] = true;
                    }
                }
            }
        });

        if (anyTriggered[0]) {
            gameHelper.checkWinCondition(gameData);
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

        // Find the source permanent on the battlefield
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent == null) return;

        for (Permanent perm : new ArrayList<>(damagedPlayerBattlefield)) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU)) {
                if (effect instanceof ReturnDamageSourcePermanentToHandEffect) {
                    // Re-check source is still on the battlefield (may have been bounced by a prior trigger)
                    Permanent currentSource = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                    if (currentSource == null) return;

                    // Find which player controls the source and bounce it
                    for (UUID playerId : gameData.orderedPlayerIds) {
                        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                        if (battlefield != null && battlefield.remove(currentSource)) {
                            permanentRemovalService.removeOrphanedAuras(gameData);
                            UUID ownerId = gameData.stolenCreatures.getOrDefault(currentSource.getId(), playerId);
                            gameData.stolenCreatures.remove(currentSource.getId());
                            List<Card> hand = gameData.playerHands.get(ownerId);
                            hand.add(currentSource.getOriginalCard());

                            String logEntry = perm.getCard().getName() + " triggers — " + currentSource.getCard().getName() + " is returned to its owner's hand.";
                            gameBroadcastService.logAndBroadcast(gameData, logEntry);
                            log.info("Game {} - {} triggers, bouncing {} to owner's hand",
                                    gameData.id, perm.getCard().getName(), currentSource.getCard().getName());
                            break;
                        }
                    }
                    return; // Source already bounced, no need to process more triggers
                }

                if (effect instanceof DamageSourceControllerGainsControlOfThisPermanentEffect controlEffect) {
                    if (controlEffect.combatOnly() && !isCombatDamage) continue;
                    if (controlEffect.creatureOnly() && !gameQueryService.isCreature(gameData, sourcePermanent)) continue;

                    // Find who controls the damage source
                    UUID sourceControllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
                    if (sourceControllerId == null || sourceControllerId.equals(damagedPlayerId)) continue;

                    // Transfer control of this permanent (perm) to the source's controller
                    creatureControlService.stealPermanent(gameData, sourceControllerId, perm);
                    gameData.permanentControlStolenCreatures.add(perm.getId());

                    log.info("Game {} - {} triggers, {} gains control of {}",
                            gameData.id, perm.getCard().getName(),
                            gameData.playerIdToName.get(sourceControllerId), perm.getCard().getName());
                }
            }
        }
    }

    public void checkLandTapTriggers(GameData gameData, UUID tappingPlayerId, UUID tappedLandId) {
        boolean[] anyTriggered = {false};

        gameData.forEachPermanent((playerId, perm) -> {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_PLAYER_TAPS_LAND)) {
                if (effect instanceof DealDamageOnLandTapEffect trigger) {
                    String cardName = perm.getCard().getName();
                    int damage = gameQueryService.applyDamageMultiplier(gameData, trigger.damage());

                    String logEntry = cardName + " triggers — deals " + damage + " damage to " + gameData.playerIdToName.get(tappingPlayerId) + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} triggers on land tap, dealing {} damage to {}",
                            gameData.id, cardName, damage, gameData.playerIdToName.get(tappingPlayerId));

                    if (!gameQueryService.isDamageFromSourcePrevented(gameData, perm.getEffectiveColor())
                            && !gameHelper.isSourceDamagePreventedForPlayer(gameData, tappingPlayerId, perm.getId())
                            && !gameData.permanentsPreventedFromDealingDamage.contains(perm.getId())
                            && !gameHelper.applyColorDamagePreventionForPlayer(gameData, tappingPlayerId, perm.getEffectiveColor())) {
                        int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, tappingPlayerId, damage);
                        effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, tappingPlayerId, effectiveDamage, cardName);
                        if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, tappingPlayerId)) {
                            gameBroadcastService.logAndBroadcast(gameData,
                                    gameData.playerIdToName.get(tappingPlayerId) + "'s life total can't change.");
                        } else {
                            int currentLife = gameData.playerLifeTotals.getOrDefault(tappingPlayerId, 20);
                            gameData.playerLifeTotals.put(tappingPlayerId, currentLife - effectiveDamage);
                        }
                    }

                    anyTriggered[0] = true;
                } else if (effect instanceof AddManaOnEnchantedLandTapEffect trigger) {
                    if (perm.getAttachedTo() == null || !perm.getAttachedTo().equals(tappedLandId)) {
                        continue;
                    }

                    ManaPool pool = gameData.playerManaPools.get(tappingPlayerId);
                    for (int i = 0; i < trigger.amount(); i++) {
                        pool.add(trigger.color());
                    }

                    String logEntry = perm.getCard().getName() + " triggers - " + gameData.playerIdToName.get(tappingPlayerId)
                            + " adds " + trigger.amount() + " " + trigger.color().name().toLowerCase() + " mana.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    anyTriggered[0] = true;
                }
            }
        });

        if (anyTriggered[0]) {
            gameHelper.checkWinCondition(gameData);
        }
    }

    public void checkAllyPermanentSacrificedTriggers(GameData gameData, UUID sacrificingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(sacrificingPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                if (effect instanceof MayPayManaEffect mayPay) {
                    gameData.pendingMayAbilities.add(new PendingMayAbility(
                            perm.getCard(),
                            sacrificingPlayerId,
                            List.of(mayPay.wrapped()),
                            perm.getCard().getName() + " — " + mayPay.prompt(),
                            null,
                            mayPay.manaCost()
                    ));
                } else if (effect instanceof MayEffect may) {
                    gameData.pendingMayAbilities.add(new PendingMayAbility(
                            perm.getCard(),
                            sacrificingPlayerId,
                            List.of(may.wrapped()),
                            perm.getCard().getName() + " — " + may.prompt()
                    ));
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            sacrificingPlayerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect))
                    ));
                }
            }
        }

        playerInputService.processNextMayAbility(gameData);
    }

    public void processNextDeathTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextDeathTriggerTarget(gameData);
    }

    public void processNextAttackTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextAttackTriggerTarget(gameData);
    }

    public void processNextDiscardSelfTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextDiscardSelfTrigger(gameData);
    }

    public void checkBecomesTargetOfSpellTriggers(GameData gameData) {
        if (gameData.stack.isEmpty()) return;
        checkBecomesTargetOfSpellTriggers(gameData, gameData.stack.getLast());
    }

    public void checkBecomesTargetOfSpellTriggers(GameData gameData, StackEntry spellEntry) {
        // Collect all targeted permanent IDs from the spell
        List<UUID> targetIds = new ArrayList<>();
        if (spellEntry.getTargetPermanentId() != null
                && spellEntry.getTargetZone() == null) {
            targetIds.add(spellEntry.getTargetPermanentId());
        }
        if (spellEntry.getTargetPermanentIds() != null) {
            targetIds.addAll(spellEntry.getTargetPermanentIds());
        }

        for (UUID targetId : targetIds) {
            // Skip player targets
            if (gameData.playerIds.contains(targetId)) continue;

            Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPermanent == null) continue;

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());
            if (controllerId == null) continue;

            // Check effects on the targeted permanent itself
            collectBecomesTargetTriggers(gameData, targetPermanent, controllerId, targetPermanent);

            // Check effects on equipment/auras attached to the targeted permanent
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) continue;
                for (Permanent attached : battlefield) {
                    if (attached.getAttachedTo() != null
                            && attached.getAttachedTo().equals(targetPermanent.getId())) {
                        collectBecomesTargetTriggers(gameData, attached, controllerId, targetPermanent);
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

        // Use the targeted creature's card as source (since "this creature deals damage")
        gameData.pendingSpellTargetTriggers.add(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                targetedCreature.getCard(), controllerId, new ArrayList<>(effects)
        ));

        String logEntry = targetedCreature.getCard().getName() + "'s triggered ability triggers — choose a target for damage.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} becomes-target-of-spell trigger queued", gameData.id, targetedCreature.getCard().getName());
    }

    public void processNextSpellTargetTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextSpellTargetTrigger(gameData);
    }

    public void processNextEmblemTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextEmblemTriggerTarget(gameData);
    }

    public void checkEnchantedPermanentTapTriggers(GameData gameData, Permanent tappedPermanent) {
        // Find the controller of the tapped permanent
        UUID tappedPermanentControllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(tappedPermanent)) {
                tappedPermanentControllerId = pid;
                break;
            }
        }
        if (tappedPermanentControllerId == null) return;
        final UUID controllerId = tappedPermanentControllerId;

        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (perm.getAttachedTo() == null || !perm.getAttachedTo().equals(tappedPermanent.getId())) {
                return;
            }
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED)) {
                if (effect instanceof GiveEnchantedPermanentControllerPoisonCountersEffect e) {
                    GiveEnchantedPermanentControllerPoisonCountersEffect resolved =
                            new GiveEnchantedPermanentControllerPoisonCountersEffect(e.amount(), controllerId);
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            auraOwnerId,
                            perm.getCard().getName() + "'s triggered ability",
                            new ArrayList<>(List.of(resolved)),
                            null,
                            perm.getId()
                    ));
                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers on enchanted permanent tap ({})",
                            gameData.id, perm.getCard().getName(), tappedPermanent.getCard().getName());
                }
            }
        });
    }
}
