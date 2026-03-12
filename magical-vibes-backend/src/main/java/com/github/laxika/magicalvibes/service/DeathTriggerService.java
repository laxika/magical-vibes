package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToBlockedAttackersOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToOpponentCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
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
public class DeathTriggerService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    public void collectDeathTrigger(GameData gameData, Card dyingCard, UUID controllerId, boolean wasCreature) {
        collectDeathTrigger(gameData, dyingCard, controllerId, wasCreature, null);
    }

    public void collectDeathTrigger(GameData gameData, Card dyingCard, UUID controllerId, boolean wasCreature, Permanent dyingPermanent) {
        List<CardEffect> deathEffects = dyingCard.getEffects(EffectSlot.ON_DEATH);
        if (deathEffects.isEmpty()) return;

        for (CardEffect effect : deathEffects) {
            if (effect instanceof DealDamageToBlockedAttackersOnDeathEffect deathDmg) {
                // Only triggers during combat and if the creature was blocking
                TurnStep step = gameData.currentStep;
                if (dyingPermanent != null && step != null
                        && step.ordinal() >= TurnStep.BEGINNING_OF_COMBAT.ordinal()
                        && step.ordinal() <= TurnStep.END_OF_COMBAT.ordinal()
                        && !dyingPermanent.getBlockingTargetPermanentIds().isEmpty()) {
                    List<UUID> targetIds = new ArrayList<>(dyingPermanent.getBlockingTargetPermanentIds());
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            dyingCard,
                            controllerId,
                            dyingCard.getName() + "'s ability",
                            new ArrayList<>(List.of(deathDmg)),
                            0,
                            targetIds
                    ));
                }
            } else if (effect instanceof MayPayManaEffect mayPay) {
                gameData.queueMayAbility(dyingCard, controllerId, mayPay, null);
            } else if (effect instanceof MayEffect may) {
                gameData.queueMayAbility(dyingCard, controllerId, may);
            } else if (effect instanceof TargetPlayerLosesLifeEqualToPowerEffect) {
                // Bake the dying creature's last-known power into a concrete TargetPlayerLosesLifeEffect
                int power = dyingPermanent != null ? dyingPermanent.getEffectivePower()
                        : (dyingCard.getPower() != null ? dyingCard.getPower() : 0);
                CardEffect resolved = new TargetPlayerLosesLifeEffect(Math.max(0, power));
                gameData.pendingDeathTriggerTargets.add(new PermanentChoiceContext.DeathTriggerTarget(
                        dyingCard, controllerId, new ArrayList<>(List.of(resolved))
                ));
            } else if (effect.canTargetPermanent() || effect.canTargetPlayer()) {
                // Targeted death trigger — queue for target selection after current action completes
                gameData.pendingDeathTriggerTargets.add(new PermanentChoiceContext.DeathTriggerTarget(
                        dyingCard, controllerId, new ArrayList<>(List.of(effect))
                ));
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        dyingCard,
                        controllerId,
                        dyingCard.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
            }
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

    public void checkAllyCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingCreatureControllerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                if (effect instanceof MayPayManaEffect mayPay) {
                    gameData.queueMayAbility(perm.getCard(), dyingCreatureControllerId, mayPay, null);
                } else if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(perm.getCard(), dyingCreatureControllerId, may);
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            dyingCreatureControllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect))
                    ));
                }
                String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers (ally creature died)", gameData.id, perm.getCard().getName());
            }
        }
    }

    public void checkEquippedCreatureDeathTriggers(GameData gameData, UUID dyingCreatureId, UUID dyingCreatureControllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingCreatureControllerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            if (!dyingCreatureId.equals(perm.getAttachedTo())) continue;
            if (!perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_EQUIPPED_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        dyingCreatureControllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
                String triggerLog = perm.getCard().getName() + "'s ability triggers (equipped creature died).";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers (equipped creature died)", gameData.id, perm.getCard().getName());
            }
        }
    }

    public void checkEnchantedPermanentDeathTriggers(GameData gameData, UUID dyingPermanentId) {
        checkEnchantedPermanentDeathTriggers(gameData, dyingPermanentId, null);
    }

    public void checkEnchantedPermanentDeathTriggers(GameData gameData, UUID dyingPermanentId, UUID dyingPermanentControllerId) {
        gameData.forEachPermanent((playerId, perm) -> {
            if (!dyingPermanentId.equals(perm.getAttachedTo())) return;
            if (perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                // Bake the dying creature's controller into effects that need it
                CardEffect effectForStack = effect;
                if (effect instanceof ReturnSourceAuraToOpponentCreatureOnDeathEffect && dyingPermanentControllerId != null) {
                    effectForStack = new ReturnSourceAuraToOpponentCreatureOnDeathEffect(dyingPermanentControllerId);
                }

                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        playerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effectForStack))
                ));
                String triggerLog = perm.getCard().getName() + "'s ability triggers (enchanted permanent put into graveyard).";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers (enchanted permanent put into graveyard)", gameData.id, perm.getCard().getName());
            }
        });
    }

    public void checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(GameData gameData, UUID graveyardOwnerId, UUID artifactControllerId) {
        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD);
            if (effects != null && !effects.isEmpty()) {
                for (CardEffect effect : effects) {
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), playerId, may);
                    } else {
                        // For effects that damage the artifact's controller, pre-set the target
                        UUID targetId = (effect instanceof DealDamageToTriggeringPermanentControllerEffect) ? artifactControllerId : null;
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(effect)),
                                targetId,
                                perm.getId()
                        ));
                    }
                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers (artifact put into graveyard from battlefield)", gameData.id, perm.getCard().getName());
                }
            }

            // Opponent-specific triggers: only fire if the graveyard owner is an opponent of this permanent's controller
            if (!playerId.equals(graveyardOwnerId)) {
                List<CardEffect> opponentEffects = perm.getCard().getEffects(EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD);
                if (opponentEffects != null && !opponentEffects.isEmpty()) {
                    for (CardEffect effect : opponentEffects) {
                        if (effect instanceof MayEffect may) {
                            gameData.queueMayAbility(perm.getCard(), playerId, may);
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    playerId,
                                    perm.getCard().getName() + "'s ability",
                                    new ArrayList<>(List.of(effect)),
                                    null,
                                    perm.getId()
                            ));
                        }
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers (opponent artifact put into graveyard from battlefield)", gameData.id, perm.getCard().getName());
                    }
                }
            }
        });
    }

    public void checkAnyNontokenCreatureDeathTriggers(GameData gameData, Card dyingCard) {
        if (dyingCard.isToken()) return;

        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                if (effect instanceof MayEffect may && may.wrapped() instanceof ImprintDyingCreatureEffect) {
                    ImprintDyingCreatureEffect imprintEffect = new ImprintDyingCreatureEffect(dyingCard.getId());
                    gameData.pendingMayAbilities.add(new PendingMayAbility(
                            perm.getCard(),
                            playerId,
                            List.of(imprintEffect),
                            perm.getCard().getName() + " — " + may.prompt()
                    ));
                    String triggerLog = perm.getCard().getName() + "'s imprint ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} imprint triggers (nontoken creature died)", gameData.id, perm.getCard().getName());
                } else if (effect instanceof MayPayManaEffect mayPay
                        && mayPay.wrapped() instanceof ReturnDyingCreatureToBattlefieldAndAttachSourceEffect) {
                    // Nim Deathmantle pattern: only trigger for creatures in this player's graveyard
                    List<Card> playerGraveyard = gameData.playerGraveyards.get(playerId);
                    if (playerGraveyard == null || playerGraveyard.stream().noneMatch(c -> c.getId().equals(dyingCard.getId()))) {
                        return;
                    }
                    var returnEffect = new ReturnDyingCreatureToBattlefieldAndAttachSourceEffect(dyingCard.getId());
                    gameData.pendingMayAbilities.add(new PendingMayAbility(
                            perm.getCard(),
                            playerId,
                            List.of(returnEffect),
                            perm.getCard().getName() + " — Pay " + mayPay.manaCost() + " to return " + dyingCard.getName() + " to the battlefield?",
                            dyingCard.getId(),
                            mayPay.manaCost()
                    ));
                    String triggerLog = perm.getCard().getName() + "'s ability triggers (" + dyingCard.getName() + " died).";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} return trigger fires (nontoken creature {} died)", gameData.id, perm.getCard().getName(), dyingCard.getName());
                }
            }
        });
    }

    public void checkOpponentCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId) {
        gameData.forEachPermanent((playerId, perm) -> {
            // Only fire when the dying creature was controlled by an opponent of this permanent's controller
            if (playerId.equals(dyingCreatureControllerId)) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(perm.getCard(), playerId, may);
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect)),
                            dyingCreatureControllerId,
                            perm.getId()
                    ));
                }
                String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers (opponent creature died)", gameData.id, perm.getCard().getName());
            }
        });
    }
}
