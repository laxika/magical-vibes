package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CradleOfVitalityLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToLifeGainedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileForEachLifeLostEffect;
import com.github.laxika.magicalvibes.model.effect.ExileMilledCreatureAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillOpponentOnLifeLossEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RelicBindTapEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnSpellLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    private final PredicateEvaluationService predicateEvaluationService;
    private final ExileService exileService;
    private final DrawService drawService;
    private final AmountEvaluationService amountEvaluationService;
    // @Lazy to break indirect circular dependency:
    // MiscTriggerCollectorService → PermanentControlSupport → TriggerCollectionService → MiscTriggerCollectorService
    private PermanentControlSupport permanentControlSupport;
    private PermanentRemovalService permanentRemovalService;

    public MiscTriggerCollectorService(GameBroadcastService gameBroadcastService,
                                       @Lazy GraveyardService graveyardService,
                                       GameQueryService gameQueryService,
                                       PredicateEvaluationService predicateEvaluationService,
                                       ExileService exileService,
                                       @Lazy DrawService drawService,
                                       AmountEvaluationService amountEvaluationService,
                                       @Lazy PermanentControlSupport permanentControlSupport,
                                       @Lazy PermanentRemovalService permanentRemovalService) {
        this.gameBroadcastService = gameBroadcastService;
        this.graveyardService = graveyardService;
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.exileService = exileService;
        this.drawService = drawService;
        this.amountEvaluationService = amountEvaluationService;
        this.permanentControlSupport = permanentControlSupport;
        this.permanentRemovalService = permanentRemovalService;
    }

    /**
     * Sets the PermanentControlSupport for manual (non-Spring) construction where
     * the circular dependency prevents passing it in the constructor.
     */
    public void setPermanentControlSupport(PermanentControlSupport permanentControlSupport) {
        this.permanentControlSupport = permanentControlSupport;
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

    // ── ON_ANY_CREATURE_SACRIFICED ─────────────────────────────────────

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ANY_CREATURE_SACRIFICED)
    private boolean handleAnyCreatureSacrificedMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {
        // "Whenever a player sacrifices a creature, you may put a +1/+1 counter on this creature"
        // (Thraximundar). The wrapped PutCountersOnSourceEffect resolves onto the source permanent,
        // and the "you may" is offered to the source's controller (not the sacrificing player).
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may,
                null, match.permanent().getId());
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ALLY_PERMANENT_SACRIFICED)
    private boolean handleSacrificeDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.AllySacrificed as = (TriggerContext.AllySacrificed) ctx;
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                as.sacrificingPlayerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect))
        ));
        return true;
    }

    @CollectsTrigger(value = TriggeringPermanentConditionalEffect.class, slot = EffectSlot.ON_ALLY_PERMANENT_SACRIFICED)
    private boolean handleSacrificePermanentConditional(TriggerMatchContext match,
            TriggeringPermanentConditionalEffect conditional, TriggerContext ctx) {
        TriggerContext.AllySacrificed as = (TriggerContext.AllySacrificed) ctx;
        if (as.sacrificedCard() == null
                || !predicateEvaluationService.matchesPermanentPredicate(match.gameData(),
                        new Permanent(as.sacrificedCard()), conditional.predicate())) {
            return false;
        }
        String cardName = match.permanent().getCard().getName();
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                as.sacrificingPlayerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(conditional.wrapped())),
                null,
                match.permanent().getId()
        ));
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on matching permanent sacrifice", match.gameData().id, cardName);
        return true;
    }

    // ── ON_ENCHANTED_PERMANENT_TAPPED ──────────────────────────────────

    @CollectsTrigger(value = GivePoisonCountersEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapPoison(TriggerMatchContext match,
            GivePoisonCountersEffect e, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentTap ept = (TriggerContext.EnchantedPermanentTap) ctx;
        GivePoisonCountersEffect resolved = new GivePoisonCountersEffect(
                e.amount(), PoisonRecipient.ENCHANTED_PERMANENT_CONTROLLER, null, ept.tappedPermanentControllerId());
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(resolved)),
                null,
                match.permanent().getId()
        ));
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on enchanted permanent tap ({})",
                match.gameData().id, match.permanent().getCard().getName(),
                ept.tappedPermanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = DestroyEnchantedPermanentEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapDestroy(TriggerMatchContext match,
            DestroyEnchantedPermanentEffect e, TriggerContext ctx) {
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(e)),
                null,
                match.permanent().getId()
        ));
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers to destroy enchanted permanent",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = PutCounterOnEnchantedCreatureEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapCounter(TriggerMatchContext match,
            PutCounterOnEnchantedCreatureEffect e, TriggerContext ctx) {
        // The effect re-derives the enchanted creature from the source Aura at resolution, so the
        // trigger only needs to carry the Aura as its source permanent (like the destroy variant).
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(e)),
                null,
                match.permanent().getId()
        ));
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers to put a counter on enchanted creature",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = DealDamageToPlayersEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapDamage(TriggerMatchContext match,
            DealDamageToPlayersEffect e, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentTap ept = (TriggerContext.EnchantedPermanentTap) ctx;
        // TRIGGERING_PERMANENT_CONTROLLER reads entry.getTargetId(); bake it to the tapped land's controller.
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(e)),
                ept.tappedPermanentControllerId(),
                match.permanent().getId()
        ));
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers to damage enchanted permanent's controller",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = LoseLifeEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapLoseLife(TriggerMatchContext match,
            LoseLifeEffect e, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentTap ept = (TriggerContext.EnchantedPermanentTap) ctx;
        // TARGET_PERMANENT_CONTROLLER re-derives the losing player from the entry's targetId
        // permanent; bake it to the tapped land so its controller loses life. Life loss (not
        // damage, CR 118.2) — fires "loses life" triggers via LifeSupport.
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(e)),
                ept.tappedPermanent().getId(),
                match.permanent().getId()));
        String triggerLog = match.permanent().getCard().getName() + "'s ability triggers.";
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.text(triggerLog));
        log.info("Game {} - {} triggers, enchanted permanent's controller loses life",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = RelicBindTapEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapRelicBind(TriggerMatchContext match,
            RelicBindTapEffect e, TriggerContext ctx) {
        // Modal, targeted ability — the mode and target are chosen when the ability resolves
        // (RelicBindTapEffectHandler). The trigger goes on the stack non-targeting; targets are
        // free (any player / planeswalker), so the tapped permanent's controller is not needed here.
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(e)),
                null,
                match.permanent().getId()
        ));
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on enchanted permanent tap (modal)",
                match.gameData().id, match.permanent().getCard().getName());
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

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + playerName + " mills " + amount + " card" + (amount != 1 ? "s" : "") + "."));
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

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life gain", gameData.id, cardName);
        return true;
    }

    @CollectsTrigger(value = PutCountersOnSelfEffect.class, slot = EffectSlot.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainPutCountersOnSelf(TriggerMatchContext match,
            PutCountersOnSelfEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life gain (put counter on self)", gameData.id, cardName);
        return true;
    }

    @CollectsTrigger(value = PutCountersOnSelfEffect.class, slot = EffectSlot.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleLandPutIntoGraveyardPutCountersOnSelf(TriggerMatchContext match,
            PutCountersOnSelfEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (land put into graveyard, put counter on self)", gameData.id, cardName);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleCreatureCardPutIntoGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (creature card put into graveyard from anywhere)", gameData.id, cardName);
        return true;
    }

    @CollectsTrigger(value = DrawCardEffect.class, slot = EffectSlot.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainDrawCard(TriggerMatchContext match,
            DrawCardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life gain (draw a card)", gameData.id, cardName);
        return true;
    }

    @CollectsTrigger(value = PutCounterOnEachControlledPermanentEffect.class,
            slot = EffectSlot.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainPutCountersOnMatching(TriggerMatchContext match,
            PutCounterOnEachControlledPermanentEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life gain (put +1/+1 counters)", gameData.id, cardName);
        return true;
    }

    @CollectsTrigger(value = DealDamageOnSpellLifeGainEffect.class, slot = EffectSlot.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainDealDamageOnSpell(TriggerMatchContext match,
            DealDamageOnSpellLifeGainEffect effect, TriggerContext ctx) {
        TriggerContext.LifeGain lg = (TriggerContext.LifeGain) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        // Only triggers when the life gain source is an instant or sorcery spell of the matching color
        if (lg.sourceEntryType() == null) return false;
        if (lg.sourceEntryType() != StackEntryType.INSTANT_SPELL
                && lg.sourceEntryType() != StackEntryType.SORCERY_SPELL) return false;
        if (lg.sourceCard() == null || !lg.sourceCard().getColors().contains(effect.triggeringColor())) return false;

        // Queue for target selection (creature or player)
        gameData.queueInteraction(new PermanentChoiceContext.LifeGainTriggerAnyTarget(
                match.permanent().getCard(),
                match.controllerId(),
                List.of(new DealDamageToAnyTargetEffect(effect.damage())),
                match.permanent().getId()
        ));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on spell life gain (source: {})",
                gameData.id, cardName, lg.sourceCard().getName());
        return true;
    }

    @CollectsTrigger(value = CradleOfVitalityLifeGainEffect.class, slot = EffectSlot.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainPayForCounters(TriggerMatchContext match,
            CradleOfVitalityLifeGainEffect effect, TriggerContext ctx) {
        TriggerContext.LifeGain lg = (TriggerContext.LifeGain) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        int lifeGained = lg.lifeGainedAmount();
        if (lifeGained <= 0) return false;

        // "Target creature" — if no creature is on any battlefield the ability has no legal target
        // and never goes on the stack.
        boolean anyCreature = gameData.orderedPlayerIds.stream()
                .map(gameData.playerBattlefields::get)
                .filter(java.util.Objects::nonNull)
                .flatMap(List::stream)
                .anyMatch(p -> gameQueryService.isCreature(gameData, p));
        if (!anyCreature) return false;

        // The counter count is locked in at trigger time (life gained by this event). The "you may pay"
        // choice happens at resolution via MayPayManaEffect; the +1/+1 counters go on the chosen creature.
        CardEffect payAndCounter = new MayPayManaEffect(effect.manaCost(),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(lifeGained)),
                "Pay " + effect.manaCost() + " to put " + lifeGained + " +1/+1 counter(s) on target creature?");

        gameData.queueInteraction(new PermanentChoiceContext.LifeGainTriggerAnyTarget(
                match.permanent().getCard(),
                match.controllerId(),
                List.of(payAndCounter),
                match.permanent().getId(),
                true));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life gain ({} life), pay for +1/+1 counters on target creature",
                gameData.id, cardName, lifeGained);
        return true;
    }

    @CollectsTrigger(value = LoseLifeEffect.class, slot = EffectSlot.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainTargetPlayerLosesLife(TriggerMatchContext match,
            LoseLifeEffect effect, TriggerContext ctx) {
        TriggerContext.LifeGain lg = (TriggerContext.LifeGain) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();
        UUID opponentId = gameQueryService.getOpponentId(gameData, match.controllerId());

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                opponentId,
                match.permanent().getId());
        // Snapshot the life gained onto the entry's event value — parallel to the spell-mana-spent
        // xValue plumbing — so the effect's EventValue amount ("equal to the life gained") reads it
        // back at resolution.
        if (amountEvaluationService.referencesEventValue(effect.amount())) {
            entry.setEventValue(lg.lifeGainedAmount());
        }
        gameData.enqueueTrigger(entry);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life gain ({} life), target opponent loses that much",
                gameData.id, cardName, lg.lifeGainedAmount());
        return true;
    }

    // ── ON_OPPONENT_CREATURE_CARD_MILLED ────────────────────────────────

    @CollectsTrigger(value = ExileMilledCreatureAndCreateTokenEffect.class, slot = EffectSlot.ON_OPPONENT_CREATURE_CARD_MILLED)
    private boolean handleExileMilledCreatureAndCreateToken(TriggerMatchContext match,
            ExileMilledCreatureAndCreateTokenEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureCardMilled milled = (TriggerContext.CreatureCardMilled) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();
        String milledCardName = milled.milledCard().getName();

        // Exile from graveyard if still there (may already be exiled by another trigger)
        List<Card> graveyard = gameData.playerGraveyards.get(milled.milledPlayerId());
        if (graveyard.remove(milled.milledCard())) {
            exileService.exileCard(gameData, milled.milledPlayerId(), milled.milledCard());
        }

        // Create the token for the controller of the triggering permanent
        CreateTokenEffect tokenEffect = new CreateTokenEffect(
                effect.tokenName(), effect.tokenPower(), effect.tokenToughness(),
                effect.tokenColor(), effect.tokenSubtypes(),
                Set.of(), Set.of()
        );
        permanentControlSupport.applyCreateToken(
                gameData, match.controllerId(), tokenEffect, match.permanent().getCard().getSetCode()
        );

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(match.permanent().getCard(),
                "'s ability triggers — exiling ", milled.milledCard(), " and creating a 2/2 black Zombie creature token."));
        log.info("Game {} - {} triggers on creature card milled: exile {} + create Zombie token",
                gameData.id, cardName, milledCardName);
        return true;
    }

    // ── ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE ──────────────────────────────

    @CollectsTrigger(value = BoostSelfEffect.class, slot = EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE)
    private boolean handleNoncombatDamageBoostSelf(TriggerMatchContext match,
            BoostSelfEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on noncombat damage to opponent", gameData.id, cardName);
        return true;
    }

    // ── ON_CONTROLLER_GAINS_LIFE (draw cards equal to life gained) ────

    @CollectsTrigger(value = DrawCardsEqualToLifeGainedEffect.class, slot = EffectSlot.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleDrawCardsEqualToLifeGained(TriggerMatchContext match,
            DrawCardsEqualToLifeGainedEffect effect, TriggerContext ctx) {
        TriggerContext.LifeGain lg = (TriggerContext.LifeGain) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();
        UUID controllerId = match.controllerId();
        int amount = lg.lifeGainedAmount();

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + gameData.playerIdToName.get(controllerId)
                        + " draws " + amount + " card" + (amount != 1 ? "s" : "") + "."));
        log.info("Game {} - {} triggers on life gain, drawing {} cards",
                gameData.id, cardName, amount);

        for (int i = 0; i < amount; i++) {
            drawService.resolveDrawCard(gameData, controllerId);
        }
        return true;
    }

    // ── ON_CONTROLLER_LOSES_LIFE (exile for each life lost) ──────────

    @CollectsTrigger(value = ExileForEachLifeLostEffect.class, slot = EffectSlot.ON_CONTROLLER_LOSES_LIFE)
    private boolean handleExileForEachLifeLost(TriggerMatchContext match,
            ExileForEachLifeLostEffect effect, TriggerContext ctx) {
        TriggerContext.LifeLoss ll = (TriggerContext.LifeLoss) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();
        UUID controllerId = match.controllerId();
        int amount = ll.lifeLostAmount();

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + gameData.playerIdToName.get(controllerId)
                        + " must exile " + amount + " card" + (amount != 1 ? "s" : "") + "/permanent" + (amount != 1 ? "s" : "") + "."));
        log.info("Game {} - {} triggers on life loss, exiling {} cards/permanents",
                gameData.id, cardName, amount);

        performLichExile(gameData, controllerId, amount, match.permanent());
        return true;
    }

    /**
     * Exiles cards/permanents for Lich's Mastery. Priority: graveyard cards first,
     * then hand cards, then other battlefield permanents (avoiding the source enchantment
     * unless it's the last resort).
     */
    private void performLichExile(GameData gameData, UUID controllerId, int count, Permanent source) {
        int remaining = count;

        // 1. Exile from graveyard first
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            graveyardService.beginGraveyardLeaveBatch(gameData);
            try {
                while (remaining > 0 && !graveyard.isEmpty()) {
                    Card card = graveyard.removeLast();
                    graveyardService.notifyCardsLeftGraveyard(gameData, controllerId);
                    exileService.exileCard(gameData, controllerId, card);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                            gameData.playerIdToName.get(controllerId) + " exiles ", card, " from their graveyard."));
                    remaining--;
                }
            } finally {
                graveyardService.endGraveyardLeaveBatch(gameData);
            }
        }

        // 2. Exile from hand
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand != null) {
            while (remaining > 0 && !hand.isEmpty()) {
                Card card = hand.removeLast();
                exileService.exileCard(gameData, controllerId, card);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(controllerId) + " exiles ", card, " from their hand."));
                remaining--;
            }
        }

        // 3. Exile permanents (skip the source enchantment unless it's the only option)
        if (remaining > 0) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield != null) {
                // Exile non-source permanents first
                while (remaining > 0) {
                    Permanent toExile = null;
                    for (Permanent perm : battlefield) {
                        if (!perm.getId().equals(source.getId())) {
                            toExile = perm;
                            break;
                        }
                    }
                    if (toExile == null) {
                        // Only the source enchantment remains — exile it as last resort
                        if (!battlefield.isEmpty()) {
                            toExile = battlefield.getFirst();
                        }
                    }
                    if (toExile == null) break;

                    Card exiledCard = toExile.getCard();
                    permanentRemovalService.removePermanentToExile(gameData, toExile);
                    permanentRemovalService.removeOrphanedAuras(gameData);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                            gameData.playerIdToName.get(controllerId) + " exiles ", exiledCard, " from the battlefield."));
                    remaining--;
                }
            }
        }

        if (remaining > 0) {
            String logEntry = gameData.playerIdToName.get(controllerId)
                    + " has nothing left to exile (" + remaining + " remaining).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} ran out of things to exile ({} remaining)",
                    gameData.id, gameData.playerIdToName.get(controllerId), remaining);
        }
    }

    // ── ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD ────────────────────────────

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD)
    boolean handleControllerCardsLeaveGraveyard(TriggerMatchContext match,
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
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (cards left graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }
}
