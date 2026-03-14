package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveEnchantedPermanentControllerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillOpponentOnLifeLossEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEqualToLifeGainedEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Trigger collectors for sacrifice, enchanted-permanent-tap, life-loss, and life-gain events.
 */
@Slf4j
@Service
public class MiscTriggerCollectorService {

    private final GameBroadcastService gameBroadcastService;
    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;

    public MiscTriggerCollectorService(GameBroadcastService gameBroadcastService,
                                       @Lazy GraveyardService graveyardService,
                                       GameQueryService gameQueryService) {
        this.gameBroadcastService = gameBroadcastService;
        this.graveyardService = graveyardService;
        this.gameQueryService = gameQueryService;
    }

    // ── ON_ALLY_PERMANENT_SACRIFICED ───────────────────────────────────

    @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_ALLY_PERMANENT_SACRIFICED)
    private boolean handleSacrificeMayPay(TriggerMatchContext match, MayPayManaEffect mayPay, TriggerContext ctx) {
        TriggerContext.AllySacrificed as = (TriggerContext.AllySacrificed) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), as.sacrificingPlayerId(), mayPay, null);
        return true;
    }

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ALLY_PERMANENT_SACRIFICED)
    private boolean handleSacrificeMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {
        TriggerContext.AllySacrificed as = (TriggerContext.AllySacrificed) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), as.sacrificingPlayerId(), may);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ALLY_PERMANENT_SACRIFICED)
    private boolean handleSacrificeDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.AllySacrificed as = (TriggerContext.AllySacrificed) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                as.sacrificingPlayerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect))
        ));
        return true;
    }

    // ── ON_ENCHANTED_PERMANENT_TAPPED ──────────────────────────────────

    @CollectsTrigger(value = GiveEnchantedPermanentControllerPoisonCountersEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapPoison(TriggerMatchContext match,
            GiveEnchantedPermanentControllerPoisonCountersEffect e, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentTap ept = (TriggerContext.EnchantedPermanentTap) ctx;
        GiveEnchantedPermanentControllerPoisonCountersEffect resolved =
                new GiveEnchantedPermanentControllerPoisonCountersEffect(e.amount(), ept.tappedPermanentControllerId());
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(resolved)),
                null,
                match.permanent().getId()
        ));
        String triggerLog = match.permanent().getCard().getName() + "'s ability triggers.";
        gameBroadcastService.logAndBroadcast(match.gameData(), triggerLog);
        log.info("Game {} - {} triggers on enchanted permanent tap ({})",
                match.gameData().id, match.permanent().getCard().getName(),
                ept.tappedPermanent().getCard().getName());
        return true;
    }

    // ── ON_OPPONENT_LOSES_LIFE ─────────────────────────────────────────

    @CollectsTrigger(value = MillOpponentOnLifeLossEffect.class, slot = EffectSlot.ON_OPPONENT_LOSES_LIFE)
    private boolean handleMillOnLifeLoss(TriggerMatchContext match,
            MillOpponentOnLifeLossEffect trigger, TriggerContext ctx) {
        TriggerContext.LifeLoss ll = (TriggerContext.LifeLoss) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();
        String playerName = gameData.playerIdToName.get(ll.losingPlayerId());
        int amount = ll.lifeLostAmount();

        String logEntry = cardName + " triggers — " + playerName + " mills " + amount
                + " card" + (amount != 1 ? "s" : "") + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} triggers on life loss, milling {} for {} cards",
                gameData.id, cardName, playerName, amount);

        graveyardService.resolveMillPlayer(gameData, ll.losingPlayerId(), amount);
        return true;
    }

    // ── ON_CONTROLLER_GAINS_LIFE ────────────────────────────────────────

    @CollectsTrigger(value = PutCountersOnSourceEffect.class, slot = EffectSlot.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainPutCounters(TriggerMatchContext match,
            PutCountersOnSourceEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));

        String triggerLog = cardName + "'s ability triggers.";
        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
        log.info("Game {} - {} triggers on life gain", gameData.id, cardName);
        return true;
    }

    @CollectsTrigger(value = TargetPlayerLosesLifeEqualToLifeGainedEffect.class, slot = EffectSlot.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainTargetPlayerLosesLife(TriggerMatchContext match,
            TargetPlayerLosesLifeEqualToLifeGainedEffect effect, TriggerContext ctx) {
        TriggerContext.LifeGain lg = (TriggerContext.LifeGain) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();
        UUID opponentId = gameQueryService.getOpponentId(gameData, match.controllerId());

        TargetPlayerLosesLifeEffect resolved = new TargetPlayerLosesLifeEffect(lg.lifeGainedAmount());
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(resolved)),
                opponentId,
                match.permanent().getId()
        ));

        String triggerLog = cardName + "'s ability triggers.";
        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
        log.info("Game {} - {} triggers on life gain ({} life), target opponent loses that much",
                gameData.id, cardName, lg.lifeGainedAmount());
        return true;
    }

    // ── ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE ──────────────────────────────

    @CollectsTrigger(value = BoostSelfEffect.class, slot = EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE)
    private boolean handleNoncombatDamageBoostSelf(TriggerMatchContext match,
            BoostSelfEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));

        String triggerLog = cardName + "'s ability triggers.";
        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
        log.info("Game {} - {} triggers on noncombat damage to opponent", gameData.id, cardName);
        return true;
    }
}
