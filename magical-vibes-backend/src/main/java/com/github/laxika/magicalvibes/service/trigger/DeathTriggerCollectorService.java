package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameOnLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToBlockedAttackersOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentLeavesConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToOpponentCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Trigger collectors for death and leaves-battlefield events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeathTriggerCollectorService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    // ── ON_DEATH (dying card's own death triggers) ─────────────────────

    @CollectsTrigger(value = DealDamageToBlockedAttackersOnDeathEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handleDealDamageToBlockedAttackers(TriggerMatchContext match,
            DealDamageToBlockedAttackersOnDeathEffect deathDmg, TriggerContext ctx) {
        TriggerContext.SelfDeath sd = (TriggerContext.SelfDeath) ctx;
        Permanent dyingPermanent = sd.dyingPermanent();
        TurnStep step = match.gameData().currentStep;
        if (dyingPermanent == null || step == null
                || step.ordinal() < TurnStep.BEGINNING_OF_COMBAT.ordinal()
                || step.ordinal() > TurnStep.END_OF_COMBAT.ordinal()
                || dyingPermanent.getBlockingTargetIds().isEmpty()) {
            return false;
        }
        List<UUID> targetIds = new ArrayList<>(dyingPermanent.getBlockingTargetIds());
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sd.dyingCard(),
                sd.controllerId(),
                sd.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(deathDmg)),
                0,
                targetIds
        ));
        return true;
    }

    @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handleDeathMayPayMana(TriggerMatchContext match,
            MayPayManaEffect mayPay, TriggerContext ctx) {
        TriggerContext.SelfDeath sd = (TriggerContext.SelfDeath) ctx;
        match.gameData().queueMayAbility(sd.dyingCard(), sd.controllerId(), mayPay, null);
        return true;
    }

    @CollectsTrigger(value = TargetPlayerLosesLifeEqualToPowerEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handleLosesLifeEqualToPower(TriggerMatchContext match,
            TargetPlayerLosesLifeEqualToPowerEffect effect, TriggerContext ctx) {
        TriggerContext.SelfDeath sd = (TriggerContext.SelfDeath) ctx;
        Permanent dyingPermanent = sd.dyingPermanent();
        int power = dyingPermanent != null ? dyingPermanent.getEffectivePower()
                : (sd.dyingCard().getPower() != null ? sd.dyingCard().getPower() : 0);
        CardEffect resolved = new TargetPlayerLosesLifeEffect(Math.max(0, power));
        match.gameData().pendingDeathTriggerTargets.add(new PermanentChoiceContext.DeathTriggerTarget(
                sd.dyingCard(), sd.controllerId(), new ArrayList<>(List.of(resolved))
        ));
        return true;
    }

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handleDeathMayEffect(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.SelfDeath sd = (TriggerContext.SelfDeath) ctx;
        // CR 603.3d: targeted "may" abilities need the target chosen when stacking
        if (may.canTargetPermanent() || may.canTargetPlayer()) {
            match.gameData().pendingDeathTriggerTargets.add(new PermanentChoiceContext.DeathTriggerTarget(
                    sd.dyingCard(), sd.controllerId(), new ArrayList<>(List.of(may))
            ));
        } else {
            match.gameData().queueMayAbility(sd.dyingCard(), sd.controllerId(), may);
        }
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handleDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.SelfDeath sd = (TriggerContext.SelfDeath) ctx;
        if (effect.canTargetPermanent() || effect.canTargetPlayer()) {
            match.gameData().pendingDeathTriggerTargets.add(new PermanentChoiceContext.DeathTriggerTarget(
                    sd.dyingCard(), sd.controllerId(), new ArrayList<>(List.of(effect))
            ));
        } else {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sd.dyingCard(),
                    sd.controllerId(),
                    sd.dyingCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect))
            ));
        }
        return true;
    }

    // ── ON_ALLY_CREATURE_DIES (may effects only — non-may effects are batched by orchestrator) ──

    @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_ALLY_CREATURE_DIES)
    boolean handleAllyCreatureMayPay(TriggerMatchContext match,
            MayPayManaEffect mayPay, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(), mayPay, null);
        return true;
    }

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ALLY_CREATURE_DIES)
    boolean handleAllyCreatureMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(), may, null, match.permanent().getId());
        return true;
    }

    // ── ON_EQUIPPED_CREATURE_DIES ──────────────────────────────────────

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_EQUIPPED_CREATURE_DIES)
    boolean handleEquippedCreatureDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        GameData gameData = match.gameData();
        if (effect.canTargetPermanent() || effect.canTargetPlayer()) {
            gameData.pendingDeathTriggerTargets.add(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect))
            ));
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect))
            ));
        }
        String triggerLog = match.permanent().getCard().getName() + "'s ability triggers (equipped creature died).";
        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
        log.info("Game {} - {} triggers (equipped creature died)", gameData.id, match.permanent().getCard().getName());
        return true;
    }

    // ── ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD ──────────────────────

    @CollectsTrigger(value = ReturnSourceAuraToOpponentCreatureOnDeathEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleReturnSourceAura(TriggerMatchContext match,
            ReturnSourceAuraToOpponentCreatureOnDeathEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        CardEffect effectForStack = epd.dyingPermanentControllerId() != null
                ? new ReturnSourceAuraToOpponentCreatureOnDeathEffect(epd.dyingPermanentControllerId())
                : effect;
        addEnchantedPermanentDeathEntry(match, effectForStack);
        return true;
    }

    @CollectsTrigger(value = ReturnEnchantedCreatureToOwnerHandOnDeathEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleReturnEnchantedCreature(TriggerMatchContext match,
            ReturnEnchantedCreatureToOwnerHandOnDeathEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        CardEffect effectForStack = epd.dyingCreatureCardId() != null
                ? new ReturnEnchantedCreatureToOwnerHandOnDeathEffect(epd.dyingCreatureCardId())
                : effect;
        addEnchantedPermanentDeathEntry(match, effectForStack);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedPermanentDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        addEnchantedPermanentDeathEntry(match, effect);
        return true;
    }

    private void addEnchantedPermanentDeathEntry(TriggerMatchContext match, CardEffect effect) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect))
        ));
        String triggerLog = match.permanent().getCard().getName() + "'s ability triggers (enchanted permanent put into graveyard).";
        gameBroadcastService.logAndBroadcast(match.gameData(), triggerLog);
        log.info("Game {} - {} triggers (enchanted permanent put into graveyard)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD ──────────────────────

    @CollectsTrigger(value = EnchantedPermanentLeavesConditionalEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD)
    boolean handleEnchantedPermanentLeavesConditional(TriggerMatchContext match,
            EnchantedPermanentLeavesConditionalEffect conditional, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentLeaves epl = (TriggerContext.EnchantedPermanentLeaves) ctx;
        if (conditional.permanentFilter() != null
                && !gameQueryService.matchesCardPredicate(epl.leavingPermanent().getCard(), conditional.permanentFilter(), null)) {
            return false;
        }
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(conditional.resolvedEffects())
        ));
        logEnchantedPermanentLTB(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD)
    boolean handleEnchantedPermanentLeavesDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect))
        ));
        logEnchantedPermanentLTB(match);
        return true;
    }

    private void logEnchantedPermanentLTB(TriggerMatchContext match) {
        String triggerLog = match.permanent().getCard().getName() + "'s ability triggers (enchanted permanent left the battlefield).";
        gameBroadcastService.logAndBroadcast(match.gameData(), triggerLog);
        log.info("Game {} - {} triggers (enchanted permanent left the battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD ────────────

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleArtifactGraveyardMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may);
        logArtifactGraveyard(match);
        return true;
    }

    @CollectsTrigger(value = DealDamageToTriggeringPermanentControllerEffect.class, slot = EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleArtifactGraveyardDamageController(TriggerMatchContext match,
            DealDamageToTriggeringPermanentControllerEffect effect, TriggerContext ctx) {
        TriggerContext.ArtifactGraveyard ag = (TriggerContext.ArtifactGraveyard) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                ag.artifactControllerId(),
                match.permanent().getId()
        ));
        logArtifactGraveyard(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleArtifactGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logArtifactGraveyard(match);
        return true;
    }

    private void logArtifactGraveyard(TriggerMatchContext match) {
        String triggerLog = match.permanent().getCard().getName() + "'s ability triggers.";
        gameBroadcastService.logAndBroadcast(match.gameData(), triggerLog);
        log.info("Game {} - {} triggers (artifact put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD ───────

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentArtifactGraveyardMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may);
        logOpponentArtifactGraveyard(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentArtifactGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logOpponentArtifactGraveyard(match);
        return true;
    }

    private void logOpponentArtifactGraveyard(TriggerMatchContext match) {
        String triggerLog = match.permanent().getCard().getName() + "'s ability triggers.";
        gameBroadcastService.logAndBroadcast(match.gameData(), triggerLog);
        log.info("Game {} - {} triggers (opponent artifact put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ANY_CREATURE_DIES ───────────────────────────────────────────

    @CollectsTrigger(value = PutCountersOnSourceEffect.class, slot = EffectSlot.ON_ANY_CREATURE_DIES)
    boolean handleAnyCreatureDeathPutCounters(TriggerMatchContext match,
            PutCountersOnSourceEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ANY_CREATURE_DIES)
    boolean handleAnyCreatureDeathMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may);
        logAnyCreatureDeath(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ANY_CREATURE_DIES)
    boolean handleAnyCreatureDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        GameData gameData = match.gameData();
        if (effect.canTargetPermanent() || effect.canTargetPlayer()) {
            gameData.pendingDeathTriggerTargets.add(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect))
            ));
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect))
            ));
        }
        logAnyCreatureDeath(match);
        return true;
    }

    private void logAnyCreatureDeath(TriggerMatchContext match) {
        String triggerLog = match.permanent().getCard().getName() + "'s ability triggers.";
        gameBroadcastService.logAndBroadcast(match.gameData(), triggerLog);
        log.info("Game {} - {} triggers (any creature died)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ALLY_NONTOKEN_CREATURE_DIES ─────────────────────────────────

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES)
    boolean handleAllyNontokenMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(), may);
        logAllyNontokenCreatureDeath(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES)
    boolean handleAllyNontokenDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                cd.dyingCreatureControllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAllyNontokenCreatureDeath(match);
        return true;
    }

    private void logAllyNontokenCreatureDeath(TriggerMatchContext match) {
        String triggerLog = match.permanent().getCard().getName() + "'s ability triggers.";
        gameBroadcastService.logAndBroadcast(match.gameData(), triggerLog);
        log.info("Game {} - {} triggers (ally nontoken creature died)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ANY_NONTOKEN_CREATURE_DIES ──────────────────────────────────

    @CollectsTrigger(value = ImprintDyingCreatureEffect.class, slot = EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES)
    boolean handleImprintDyingCreature(TriggerMatchContext match,
            ImprintDyingCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        ImprintDyingCreatureEffect imprintEffect = new ImprintDyingCreatureEffect(cd.dyingCard().getId());
        MayEffect rawMay = (MayEffect) match.rawEffect();
        match.gameData().pendingMayAbilities.add(new PendingMayAbility(
                match.permanent().getCard(),
                match.controllerId(),
                List.of(imprintEffect),
                match.permanent().getCard().getName() + " — " + rawMay.prompt()
        ));
        String triggerLog = match.permanent().getCard().getName() + "'s imprint ability triggers.";
        gameBroadcastService.logAndBroadcast(match.gameData(), triggerLog);
        log.info("Game {} - {} imprint triggers (nontoken creature died)", match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = ReturnDyingCreatureToBattlefieldAndAttachSourceEffect.class, slot = EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES)
    boolean handleReturnDyingCreatureMayPay(TriggerMatchContext match,
            ReturnDyingCreatureToBattlefieldAndAttachSourceEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        GameData gameData = match.gameData();
        List<Card> playerGraveyard = gameData.playerGraveyards.get(match.controllerId());
        if (playerGraveyard == null || playerGraveyard.stream().noneMatch(c -> c.getId().equals(cd.dyingCard().getId()))) {
            return false;
        }
        MayPayManaEffect rawMayPay = (MayPayManaEffect) match.rawEffect();
        var returnEffect = new ReturnDyingCreatureToBattlefieldAndAttachSourceEffect(cd.dyingCard().getId());
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                match.permanent().getCard(),
                match.controllerId(),
                List.of(returnEffect),
                match.permanent().getCard().getName() + " — Pay " + rawMayPay.manaCost() + " to return " + cd.dyingCard().getName() + " to the battlefield?",
                cd.dyingCard().getId(),
                rawMayPay.manaCost()
        ));
        String triggerLog = match.permanent().getCard().getName() + "'s ability triggers (" + cd.dyingCard().getName() + " died).";
        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
        log.info("Game {} - {} return trigger fires (nontoken creature {} died)", gameData.id, match.permanent().getCard().getName(), cd.dyingCard().getName());
        return true;
    }

    // ── ON_OPPONENT_CREATURE_DIES ──────────────────────────────────────

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_OPPONENT_CREATURE_DIES)
    boolean handleOpponentCreatureDeathMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may);
        logOpponentCreatureDeath(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_OPPONENT_CREATURE_DIES)
    boolean handleOpponentCreatureDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                cd.dyingCreatureControllerId(),
                match.permanent().getId()
        ));
        logOpponentCreatureDeath(match);
        return true;
    }

    private void logOpponentCreatureDeath(TriggerMatchContext match) {
        String triggerLog = match.permanent().getCard().getName() + "'s ability triggers.";
        gameBroadcastService.logAndBroadcast(match.gameData(), triggerLog);
        log.info("Game {} - {} triggers (opponent creature died)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_SELF_LEAVES_BATTLEFIELD ─────────────────────────────────────

    @CollectsTrigger(value = ControllerLosesGameOnLeavesEffect.class, slot = EffectSlot.ON_SELF_LEAVES_BATTLEFIELD)
    boolean handleControllerLosesGameOnLeaves(TriggerMatchContext match,
            ControllerLosesGameOnLeavesEffect effect, TriggerContext ctx) {
        TriggerContext.SelfLeaves sl = (TriggerContext.SelfLeaves) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                sl.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new TargetPlayerLosesGameEffect(sl.controllerId())))
        ));
        logSelfLeaves(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_SELF_LEAVES_BATTLEFIELD)
    boolean handleSelfLeavesDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.SelfLeaves sl = (TriggerContext.SelfLeaves) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                sl.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect))
        ));
        logSelfLeaves(match);
        return true;
    }

    private void logSelfLeaves(TriggerMatchContext match) {
        String triggerLog = match.permanent().getCard().getName() + "'s ability triggers (left the battlefield).";
        gameBroadcastService.logAndBroadcast(match.gameData(), triggerLog);
        log.info("Game {} - {} triggers (left the battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD ──

    @CollectsTrigger(value = RegisterDelayedReturnCardFromGraveyardToHandEffect.class, slot = EffectSlot.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleRegisterDelayedReturn(TriggerMatchContext match,
            RegisterDelayedReturnCardFromGraveyardToHandEffect effect, TriggerContext ctx) {
        TriggerContext.AllyAuraOrEquipmentGraveyard aaeg = (TriggerContext.AllyAuraOrEquipmentGraveyard) ctx;
        Card dyingCard = aaeg.dyingCard();
        RegisterDelayedReturnCardFromGraveyardToHandEffect delayedEffect =
                new RegisterDelayedReturnCardFromGraveyardToHandEffect(dyingCard.getId());
        MayEffect may = new MayEffect(delayedEffect,
                "Return " + dyingCard.getName() + " to its owner's hand at the beginning of the next end step?");
        match.gameData().queueMayAbility(match.permanent().getCard(), aaeg.controllerId(), may);

        String triggerLog = match.permanent().getCard().getName() + "'s ability triggers (" + dyingCard.getName()
                + " was put into a graveyard from the battlefield).";
        gameBroadcastService.logAndBroadcast(match.gameData(), triggerLog);
        log.info("Game {} - {} triggers (ally Aura/Equipment {} put into graveyard from battlefield)",
                match.gameData().id, match.permanent().getCard().getName(), dyingCard.getName());
        return true;
    }
}
