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
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerContext;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Scans the battlefield for "when a permanent enters" triggered abilities and puts the matching
 * triggers onto the stack. The per-effect placement logic (conditional gating, "may" queueing,
 * value materialisation) lives in {@link EnterTriggerHandlerRegistry} rather than in
 * {@code instanceof} chains here; each scan builds an {@link EnterTriggerContext} that captures
 * the differences between scans and hands every candidate effect to the registry.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnterTriggerScanService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final EnterTriggerHandlerRegistry enterTriggerHandlerRegistry;

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

            EnterTriggerContext context = baseContext(gameData, perm, controllerId, enteringCreature, controllerId).build();
            for (CardEffect effect : effects) {
                enterTriggerHandlerRegistry.dispatch(context, effect);
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

            EnterTriggerContext context = baseContext(gameData, perm, controllerId, enteringCard, controllerId)
                    .mayPayTargetCardId(enteringPermanentId)
                    .build();
            for (CardEffect effect : effects) {
                enterTriggerHandlerRegistry.dispatch(context, effect);
            }
        }
    }

    public void checkOpponentLandEntersTriggers(GameData gameData, UUID landControllerId, Card enteringLand) {
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(landControllerId)) return;

            for (Permanent perm : battlefield) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                EnterTriggerContext context = baseContext(gameData, perm, playerId, enteringLand, landControllerId)
                        .defaultTargetPlayerId(landControllerId)
                        .build();
                for (CardEffect effect : effects) {
                    enterTriggerHandlerRegistry.dispatch(context, effect);
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

                EnterTriggerContext context = baseContext(gameData, perm, playerId, enteringCreature, enteringCreatureControllerId)
                        .defaultTargetPlayerId(enteringCreatureControllerId)
                        .build();
                for (CardEffect effect : effects) {
                    enterTriggerHandlerRegistry.dispatch(context, effect);
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

            EnterTriggerContext context = baseContext(gameData, perm, playerId, enteringCreature, enteringCreatureControllerId)
                    .perEffectTriggerCount(1 + extraTriggers)
                    .defaultSkipsTargetingEffects(true)
                    .build();
            for (CardEffect effect : effects) {
                enterTriggerHandlerRegistry.dispatch(context, effect);
            }
        });
    }

    private EnterTriggerContext.EnterTriggerContextBuilder baseContext(GameData gameData, Permanent sourcePermanent,
                                                                       UUID abilityControllerId, Card enteringCard,
                                                                       UUID enteringControllerId) {
        return EnterTriggerContext.builder()
                .gameData(gameData)
                .sourcePermanent(sourcePermanent)
                .abilityControllerId(abilityControllerId)
                .enteringCard(enteringCard)
                .enteringControllerId(enteringControllerId)
                .perEffectTriggerCount(1)
                .gameBroadcastService(gameBroadcastService)
                .gameQueryService(gameQueryService)
                .registry(enterTriggerHandlerRegistry);
    }
}
