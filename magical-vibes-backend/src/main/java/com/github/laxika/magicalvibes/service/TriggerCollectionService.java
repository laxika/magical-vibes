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
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenOnOwnSpellCastWithCostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetOnArtifactCastEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfOnArtifactCastEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDiscardingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeOnOwnSpellCastWithCostEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDamageSourcePermanentToHandEffect;
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

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
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
                        && (!trigger.onlyOwnSpells() || playerId.equals(castingPlayerId))) {
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
                        && spellCard.getType() == CardType.ARTIFACT
                        && playerId.equals(castingPlayerId)) {
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
                } else if (inner instanceof GainLifeOnOwnSpellCastWithCostEffect trigger
                        && gameQueryService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)
                        && playerId.equals(castingPlayerId)) {
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
                        && gameQueryService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)
                        && playerId.equals(castingPlayerId)) {
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
                        && spellCard.getType() == CardType.ARTIFACT
                        && playerId.equals(castingPlayerId)) {
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
                } else if (inner instanceof GiveTargetPlayerPoisonCountersEffect trigger
                        && trigger.spellFilter() != null
                        && gameQueryService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)
                        && playerId.equals(castingPlayerId)) {
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
                } else if (inner instanceof ProliferateEffect
                        && playerId.equals(castingPlayerId)) {
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
                                && !gameHelper.applyColorDamagePreventionForPlayer(gameData, discardingPlayerId, perm.getEffectiveColor())) {
                            int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, discardingPlayerId, damage);
                            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, discardingPlayerId, effectiveDamage, cardName);
                            int currentLife = gameData.playerLifeTotals.getOrDefault(discardingPlayerId, 20);
                            gameData.playerLifeTotals.put(discardingPlayerId, currentLife - effectiveDamage);
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

    public void checkDamageDealtToControllerTriggers(GameData gameData, UUID damagedPlayerId, UUID sourcePermanentId) {
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
                            && !gameHelper.applyColorDamagePreventionForPlayer(gameData, tappingPlayerId, perm.getEffectiveColor())) {
                        int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, tappingPlayerId, damage);
                        effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, tappingPlayerId, effectiveDamage, cardName);
                        int currentLife = gameData.playerLifeTotals.getOrDefault(tappingPlayerId, 20);
                        gameData.playerLifeTotals.put(tappingPlayerId, currentLife - effectiveDamage);
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
}
