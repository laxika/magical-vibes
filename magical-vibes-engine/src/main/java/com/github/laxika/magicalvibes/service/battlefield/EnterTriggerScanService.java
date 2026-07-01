package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMaxPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMinPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintedCardNameMatchesEnteringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentEnteredThisTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnterTriggerScanService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    public void checkEntersFromGraveyardTriggers(GameData gameData, UUID enteringControllerId, Card enteringCreature) {
        if (enteringCreature.getToughness() == null) return;

        Permanent enteringPermanent = null;
        List<Permanent> controllerBf = gameData.playerBattlefields.get(enteringControllerId);
        if (controllerBf != null) {
            for (Permanent p : controllerBf) {
                if (p.getCard() == enteringCreature) {
                    enteringPermanent = p;
                    break;
                }
            }
        }
        if (enteringPermanent == null || enteringPermanent.getEnteredFromGraveyardOwnerId() == null) {
            return;
        }

        UUID graveyardOwnerId = enteringPermanent.getEnteredFromGraveyardOwnerId();
        UUID enteringPermanentId = enteringPermanent.getId();

        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(graveyardOwnerId)) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_CREATURE_ENTERS_FROM_GRAVEYARD);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                gameData.pendingEntersFromGraveyardTriggerTargets.add(
                        new PermanentChoiceContext.EntersFromGraveyardTriggerTarget(
                                perm.getCard(), playerId, new ArrayList<>(List.of(effect)), enteringPermanentId));
                String triggerLog = perm.getCard().getName() + "'s ability triggers ("
                        + enteringCreature.getName() + " entered from a graveyard).";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers ({} entered from graveyard)",
                        gameData.id, perm.getCard().getName(), enteringCreature.getName());
            }
        });
    }

    public void checkAllyCreatureEntersTriggers(GameData gameData, UUID controllerId, Card enteringCreature, int extraWizardTriggers) {
        if (enteringCreature.getToughness() == null) return;

        int stackSizeBefore = gameData.stack.size();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCreature) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                if (effect instanceof EnteringCreatureMinPowerConditionalEffect conditional) {
                    if (enteringCreature.getPower() == null || enteringCreature.getPower() < conditional.minPower()) {
                        continue;
                    }
                    CardEffect innerEffect = conditional.wrapped();
                    if (innerEffect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), controllerId, may);
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for {} entering (power {} >= {})",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName(),
                                enteringCreature.getPower(), conditional.minPower());
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                controllerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(innerEffect))
                        ));
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for {} entering (power {} >= {})",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName(),
                                enteringCreature.getPower(), conditional.minPower());
                    }
                } else if (effect instanceof EnteringCreatureMaxPowerConditionalEffect conditional) {
                    if (enteringCreature.getPower() == null || enteringCreature.getPower() > conditional.maxPower()) {
                        continue;
                    }
                    CardEffect innerEffect = conditional.wrapped();
                    if (innerEffect instanceof MayPayManaEffect mayPay) {
                        gameData.queueMayAbility(perm.getCard(), controllerId, mayPay, null);
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for {} entering (power {} <= {})",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName(),
                                enteringCreature.getPower(), conditional.maxPower());
                    } else if (innerEffect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), controllerId, may);
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for {} entering (power {} <= {})",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName(),
                                enteringCreature.getPower(), conditional.maxPower());
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                controllerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(innerEffect))
                        ));
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for {} entering (power {} <= {})",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName(),
                                enteringCreature.getPower(), conditional.maxPower());
                    }
                } else if (effect instanceof TriggeringCardConditionalEffect conditional) {
                    if (!gameQueryService.matchesCardPredicate(enteringCreature, conditional.predicate(), null,
                            gameData, controllerId)) {
                        continue;
                    }
                    CardEffect innerEffect = conditional.wrapped();
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(innerEffect)),
                            null,
                            perm.getId()
                    ));
                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for {} entering (card predicate {})",
                            gameData.id, perm.getCard().getName(), enteringCreature.getName(),
                            conditional.predicate());
                } else if (effect instanceof GainLifeEqualToToughnessEffect) {
                    int toughness = enteringCreature.getToughness();
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            List.of(new GainLifeEffect(toughness))
                    ));
                    String triggerLog = perm.getCard().getName() + " triggers — " +
                            gameData.playerIdToName.get(controllerId) + " will gain " + toughness + " life.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for {} entering (toughness={})",
                            gameData.id, perm.getCard().getName(), enteringCreature.getName(), toughness);
                } else if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(perm.getCard(), controllerId, may);
                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for {} entering (may effect)",
                            gameData.id, perm.getCard().getName(), enteringCreature.getName());
                }
            }
        }

        if (extraWizardTriggers > 0) {
            List<StackEntry> newEntries = new ArrayList<>(gameData.stack.subList(stackSizeBefore, gameData.stack.size()));
            for (int i = 0; i < extraWizardTriggers; i++) {
                for (StackEntry entry : newEntries) {
                    gameData.stack.add(new StackEntry(entry));
                }
            }
        }
    }

    public void checkAllyArtifactEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        if (!enteringCard.hasType(CardType.ARTIFACT)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers for {} entering (ally artifact entered)",
                        gameData.id, perm.getCard().getName(), enteringCard.getName());
            }
        }
    }

    public void checkAllyEquipmentEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        if (!enteringCard.getSubtypes().contains(CardSubtype.EQUIPMENT)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers for {} entering (ally equipment entered)",
                        gameData.id, perm.getCard().getName(), enteringCard.getName());
            }
        }
    }

    public void checkAllyNontokenArtifactEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        if (enteringCard.isToken()) return;
        if (!enteringCard.hasType(CardType.ARTIFACT)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);

        UUID enteringPermanentId = null;
        for (Permanent p : battlefield) {
            if (p.getCard() == enteringCard) {
                enteringPermanentId = p.getId();
                break;
            }
        }

        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                if (effect instanceof MayPayManaEffect mayPay) {
                    gameData.queueMayAbility(perm.getCard(), controllerId, mayPay, enteringPermanentId);
                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for nontoken artifact {} entering",
                            gameData.id, perm.getCard().getName(), enteringCard.getName());
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect)),
                            null,
                            perm.getId()
                    ));
                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for nontoken artifact {} entering",
                            gameData.id, perm.getCard().getName(), enteringCard.getName());
                }
            }
        }
    }

    public void checkOpponentLandEntersTriggers(GameData gameData, UUID landControllerId, Card enteringLand) {
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(landControllerId)) return;

            for (Permanent perm : battlefield) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    CardEffect effectToResolve = effect;

                    if (effect instanceof PermanentEnteredThisTurnConditionalEffect conditional) {
                        List<Card> entered = gameData.permanentsEnteredBattlefieldThisTurn
                                .getOrDefault(landControllerId, List.of());
                        long matchCount = entered.stream()
                                .filter(c -> gameQueryService.matchesCardPredicate(c, conditional.predicate(), null))
                                .count();
                        if (matchCount < conditional.minCount()) continue;
                        effectToResolve = conditional.wrapped();
                    }

                    if (effect instanceof ImprintedCardNameMatchesEnteringPermanentConditionalEffect conditional) {
                        Card imprintedCard = perm.getCard().getImprintedCard();
                        if (imprintedCard == null || !imprintedCard.getName().equals(enteringLand.getName())) continue;
                        effectToResolve = conditional.wrapped();
                    }

                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effectToResolve)),
                            landControllerId,
                            perm.getId()
                    ));

                    String logEntry = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} triggers on opponent land entering", gameData.id, perm.getCard().getName());
                }
            }
        });
    }

    public void checkAllyLandEntersTriggers(GameData gameData, UUID landControllerId, Card enteringLand) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(landControllerId);
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringLand) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    landControllerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    null,
                    perm.getId()
            ));

            String logEntry = perm.getCard().getName() + "'s ability triggers.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} triggers on ally land entering", gameData.id, perm.getCard().getName());
        }
    }

    public void checkOpponentCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId, Card enteringCreature) {
        if (enteringCreature.getToughness() == null) return;

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(enteringCreatureControllerId)) return;

            for (Permanent perm : battlefield) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), playerId, may, enteringCreatureControllerId, perm.getId());
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for opponent creature {} entering",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName());
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(effect)),
                                enteringCreatureControllerId,
                                perm.getId()
                        ));
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for opponent creature {} entering",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName());
                    }
                }
            }
        });
    }

    public void checkAnyCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId, Card enteringCreature) {
        if (enteringCreature.getToughness() == null) return;

        int extraWizardTriggers = gameQueryService.countETBExtraTriggers(gameData, enteringCreatureControllerId, enteringCreature);

        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) return;

            if (perm.getCard() == enteringCreature) return;

            int extraTriggers = playerId.equals(enteringCreatureControllerId) ? extraWizardTriggers : 0;

            for (CardEffect effect : effects) {
                if (effect instanceof GainLifeEffect gainLife) {
                    for (int t = 0; t < 1 + extraTriggers; t++) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                List.of(new GainLifeEffect(gainLife.amount()))
                        ));
                    }
                    String triggerLog = perm.getCard().getName() + " triggers — " +
                            gameData.playerIdToName.get(playerId) + " will gain " + gainLife.amount() + " life.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for {} entering (gain {} life)",
                            gameData.id, perm.getCard().getName(), enteringCreature.getName(), gainLife.amount());
                } else if (effect instanceof DealDamageToTargetPlayerEffect damageEffect) {
                    for (int t = 0; t < 1 + extraTriggers; t++) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(new DealDamageToTargetPlayerEffect(damageEffect.damage()))),
                                enteringCreatureControllerId,
                                perm.getId()
                        ));
                    }
                    String triggerLog = perm.getCard().getName() + " triggers — deals " + damageEffect.damage() +
                            " damage to " + gameData.playerIdToName.get(enteringCreatureControllerId) + ".";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for {} entering (deal {} damage to controller)",
                            gameData.id, perm.getCard().getName(), enteringCreature.getName(), damageEffect.damage());
                } else if (!effect.canTargetPlayer()
                        && !effect.canTargetPermanent()
                        && !effect.canTargetSpell()
                        && !effect.canTargetGraveyard()
                        && !effect.canTargetAnyGraveyard()) {
                    for (int t = 0; t < 1 + extraTriggers; t++) {
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
                    String triggerLog = perm.getCard().getName() + " triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for {} entering",
                            gameData.id, perm.getCard().getName(), enteringCreature.getName());
                }
            }
        });
    }
}
