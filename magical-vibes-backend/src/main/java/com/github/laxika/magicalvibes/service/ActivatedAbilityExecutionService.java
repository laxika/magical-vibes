package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DoubleManaPoolEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect.Scope;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivatedAbilityExecutionService {

    private final GameHelper gameHelper;
    private final StateBasedActionService stateBasedActionService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final SessionManager sessionManager;

    public void completeActivationAfterCosts(GameData gameData,
                                             Player player,
                                             Permanent permanent,
                                             ActivatedAbility ability,
                                             List<CardEffect> abilityEffects,
                                             int effectiveXValue,
                                             UUID targetPermanentId,
                                             Zone targetZone,
                                             boolean markAsNonTargetingForSacCreatureCost) {
        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            throw new IllegalStateException("Invalid battlefield");
        }

        UUID effectiveTargetId = targetPermanentId;
        if (effectiveTargetId == null) {
            boolean needsSelfTarget = abilityEffects.stream().anyMatch(e ->
                    e instanceof RegenerateEffect || e instanceof BoostSelfEffect || e instanceof UntapSelfEffect
                            || e instanceof AnimateSelfEffect
                            || (e instanceof GrantKeywordEffect grant && grant.scope() == Scope.SELF));
            if (needsSelfTarget) {
                effectiveTargetId = permanent.getId();
            }
        }

        if (ability.isRequiresTap()) {
            permanent.tap();
        }

        boolean shouldSacrifice = abilityEffects.stream().anyMatch(e -> e instanceof SacrificeSelfCost);
        if (shouldSacrifice) {
            boolean wasCreature = gameQueryService.isCreature(gameData, permanent);
            battlefield.remove(permanent);
            gameHelper.addCardToGraveyard(gameData, playerId, permanent.getCard(), Zone.BATTLEFIELD);
            gameHelper.collectDeathTrigger(gameData, permanent.getCard(), playerId, wasCreature);
            if (wasCreature) {
                gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
            }
        }

        String logEntry = player.getUsername() + " activates " + permanent.getCard().getName() + "'s ability.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} activates {}'s ability", gameData.id, player.getUsername(), permanent.getCard().getName());

        List<CardEffect> snapshotEffects = snapshotEffects(abilityEffects, permanent);
        boolean isManaAbility = !ability.isNeedsTarget() && !ability.isNeedsSpellTarget()
                && ability.getLoyaltyCost() == null
                && !snapshotEffects.isEmpty()
                && snapshotEffects.stream().allMatch(e ->
                e instanceof AwardManaEffect || e instanceof AwardAnyColorManaEffect || e instanceof DoubleManaPoolEffect);

        if (isManaAbility) {
            resolveManaAbility(gameData, playerId, player, snapshotEffects);
            return;
        }

        pushAbilityOnStack(gameData, playerId, permanent, ability, snapshotEffects, effectiveXValue, effectiveTargetId, targetZone);
        if (markAsNonTargetingForSacCreatureCost && !gameData.stack.isEmpty()) {
            gameData.stack.getLast().setNonTargeting(true);
        }
    }

    private List<CardEffect> snapshotEffects(List<CardEffect> abilityEffects, Permanent permanent) {
        List<CardEffect> snapshotEffects = new ArrayList<>();
        for (CardEffect effect : abilityEffects) {
            if (effect instanceof SacrificeSelfCost
                    || effect instanceof SacrificeCreatureCost
                    || effect instanceof SacrificeSubtypeCreatureCost
                    || effect instanceof DiscardCardTypeCost) {
                continue;
            }
            if (effect instanceof CantBlockSourceEffect) {
                snapshotEffects.add(new CantBlockSourceEffect(permanent.getId()));
            } else if (effect instanceof PreventNextColorDamageToControllerEffect && permanent.getChosenColor() != null) {
                snapshotEffects.add(new PreventNextColorDamageToControllerEffect(permanent.getChosenColor()));
            } else {
                snapshotEffects.add(effect);
            }
        }
        return snapshotEffects;
    }

    private void resolveManaAbility(GameData gameData, UUID playerId, Player player, List<CardEffect> snapshotEffects) {
        for (CardEffect effect : snapshotEffects) {
            if (effect instanceof AwardManaEffect award) {
                gameData.playerManaPools.get(playerId).add(award.color());
            } else if (effect instanceof DoubleManaPoolEffect) {
                ManaPool pool = gameData.playerManaPools.get(playerId);
                for (ManaColor color : ManaColor.values()) {
                    int current = pool.get(color);
                    for (int i = 0; i < current; i++) {
                        pool.add(color);
                    }
                }
            } else if (effect instanceof AwardAnyColorManaEffect) {
                ColorChoiceContext.ManaColorChoice choiceContext = new ColorChoiceContext.ManaColorChoice(playerId);
                gameData.interaction.beginColorChoice(playerId, null, null, choiceContext);
                List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                sessionManager.sendToPlayer(playerId, new ChooseColorMessage(colors, "Choose a color of mana to add."));
                log.info("Game {} - Awaiting {} to choose a mana color", gameData.id, player.getUsername());
            }
        }
        stateBasedActionService.performStateBasedActions(gameData);
        gameData.priorityPassedBy.clear();
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingDeathTriggerTargets.isEmpty()) {
            gameHelper.processNextDeathTriggerTarget(gameData);
        }
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
        gameBroadcastService.broadcastGameState(gameData);
    }

    private void pushAbilityOnStack(GameData gameData,
                                    UUID playerId,
                                    Permanent permanent,
                                    ActivatedAbility ability,
                                    List<CardEffect> snapshotEffects,
                                    int effectiveXValue,
                                    UUID effectiveTargetId,
                                    Zone targetZone) {
        Zone effectiveTargetZone = targetZone;
        StackEntry stackEntry;
        if (ability.isNeedsSpellTarget()) {
            effectiveTargetZone = Zone.STACK;
        }
        if (effectiveTargetZone != null && effectiveTargetZone != Zone.BATTLEFIELD) {
            stackEntry = new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY,
                    permanent.getCard(),
                    playerId,
                    permanent.getCard().getName() + "'s ability",
                    snapshotEffects,
                    effectiveXValue,
                    effectiveTargetId,
                    permanent.getId(),
                    Map.of(),
                    effectiveTargetZone,
                    List.of(),
                    List.of()
            );
        } else {
            stackEntry = new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY,
                    permanent.getCard(),
                    playerId,
                    permanent.getCard().getName() + "'s ability",
                    snapshotEffects,
                    effectiveXValue,
                    effectiveTargetId,
                    permanent.getId(),
                    Map.of(),
                    null,
                    List.of(),
                    List.of()
            );
        }
        stackEntry.setTargetFilter(ability.getTargetFilter());
        gameData.stack.add(stackEntry);
        stateBasedActionService.performStateBasedActions(gameData);
        gameData.priorityPassedBy.clear();
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingDeathTriggerTargets.isEmpty()) {
            gameHelper.processNextDeathTriggerTarget(gameData);
        }
        gameBroadcastService.broadcastGameState(gameData);
    }
}

