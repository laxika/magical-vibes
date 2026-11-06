package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameOnLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForEmergeSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForImprintedCardOwnerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForExiledCardsWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyMerfolkTappedForSourceAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTokensCreatedWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongCreaturesOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTokensCreatedWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutImprintedCardIntoOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryAndReturnExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.RememberTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceToOwnerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnLinkedCardToOwnerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardAndShuffleTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledWhileActivatingCraftAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIdSetPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Puts the pilot families' triggered abilities on the stack with the event data their effects need: the moved
 * object's last-known power, counters, attachments and links, its graveyard entry version, the spell it was
 * sacrificed for. These are the old slot collectors' {@code @CollectsTrigger} handlers, moved here when their
 * families left {@code EffectSlot} (effect-slot-refactor.md step 10b) and now reading the {@link ZoneChangeEvent}
 * instead of a {@code TriggerContext}. Like the collectors, they bind that data into the effects, choose how the
 * ability waits for its targets or its mode, and decline an ability whose bound effect would do nothing or whose
 * handler checks an intervening "if" of its own.
 *
 * <p>{@link TriggeredAbilityStackService} calls it for an ability of one effect, after the ability's options let it
 * trigger. A handler is chosen by the effect's class
 * as the collectors chose one: the class itself, then the class of the effect a {@link MayEffect},
 * {@link MayPayManaEffect} or {@link MayPayTapPermanentsEffect} wraps, then the event's default. Step 15 of the
 * refactor replaces the binding with effects that read the event stored on the stack entry, and removes this
 * class.
 */
@Slf4j
@Service
public class LegacyTriggerBindings {

    /**
     * Which kind of the source's own zone change triggered the ability, which picks its handlers. The constants are
     * in the order the old collectors put one event's abilities on the stack.
     */
    enum Kind {
        /** Left the battlefield for any zone (CR 603.6c). */
        LEAVES,
        /** Arrived in a zone other than the battlefield: a graveyard from anywhere, a hand, a library, the stack. */
        ARRIVES,
        /** Put into a graveyard from the battlefield (CR 700.4). */
        DIES,
        /** Sacrificed (CR 701.21a). */
        SACRIFICED
    }

    /**
     * One triggered ability's event, as its handler reads it.
     *
     * @param gameData  the game
     * @param candidate the ability and its source as the event sees them
     * @param event     the zone change that triggered it
     */
    record Ctx(GameData gameData, TriggerCandidate candidate, ZoneChangeEvent event) {

        /** The source's card; for a departure, the effective card it left as. */
        Card dyingCard() {
            return candidate.sourceCard();
        }

        UUID controllerId() {
            return candidate.controllerId();
        }

        /** The permanent's last-known copy for a departure, or {@code null}. */
        Permanent dyingPermanent() {
            return event.permanent();
        }

        /** The source as a permanent: the last-known copy, or a stand-in for a card in another zone. */
        Permanent permanent() {
            return candidate.sourceAsPermanent();
        }

        /** The last-known power of the departed permanent, or the card's printed power. */
        int dyingPower() {
            Permanent lastKnown = event.permanent();
            if (lastKnown != null) {
                return lastKnown.getLastKnownPower() != null
                        ? lastKnown.getLastKnownPower() : lastKnown.getEffectivePower();
            }
            Card card = candidate.sourceCard();
            return card.getPower() != null ? card.getPower() : 0;
        }

        /** The spell whose cost the permanent was sacrificed to pay, or {@code null}. */
        Card castingSpell() {
            return event.costOf();
        }

        Zone destination() {
            return event.to();
        }

        boolean exiledWhileActivatingCraftAbility() {
            return event.cause() == ZoneChangeCause.CRAFT;
        }

        boolean wasCreature() {
            return lastKnownCard().hasType(CardType.CREATURE);
        }

        boolean wasLand() {
            return lastKnownCard().hasType(CardType.LAND);
        }

        private Card lastKnownCard() {
            return event.permanent() != null ? event.permanent().getCard() : event.card();
        }
    }

    @FunctionalInterface
    private interface Handler {
        boolean handle(Ctx c, CardEffect effect);
    }

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GraveyardTargetingSupport graveyardTargetingSupport;
    private final TargetLegalityService targetLegalityService;
    private final GraveyardTargetingService graveyardTargetingService;
    private final TriggerCollectionService triggerCollectionService;

    private final Map<Class<? extends CardEffect>, Handler> deathHandlers = new HashMap<>();
    private final Map<Class<? extends CardEffect>, Handler> leaveHandlers = new HashMap<>();

    public LegacyTriggerBindings(@Lazy GameQueryService gameQueryService,
                                 @Lazy PredicateEvaluationService predicateEvaluationService,
                                 @Lazy GameLogService gameLogService,
                                 @Lazy AmountEvaluationService amountEvaluationService,
                                 @Lazy ConditionEvaluationService conditionEvaluationService,
                                 @Lazy GraveyardTargetingSupport graveyardTargetingSupport,
                                 @Lazy TargetLegalityService targetLegalityService,
                                 @Lazy GraveyardTargetingService graveyardTargetingService,
                                 @Lazy TriggerCollectionService triggerCollectionService) {
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameLogService = gameLogService;
        this.amountEvaluationService = amountEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.graveyardTargetingSupport = graveyardTargetingSupport;
        this.targetLegalityService = targetLegalityService;
        this.graveyardTargetingService = graveyardTargetingService;
        this.triggerCollectionService = triggerCollectionService;
        registerDeathHandlers();
        registerLeaveHandlers();
    }

    /** The kind of zone change {@code trigger} is, as this class handles it. */
    static Kind kindOf(ZoneChangeTrigger trigger) {
        if (trigger.causes().contains(ZoneChangeCause.SACRIFICE)) {
            return Kind.SACRIFICED;
        }
        if (trigger.from().equals(Set.of(Zone.BATTLEFIELD))) {
            return trigger.to() == Zone.GRAVEYARD ? Kind.DIES : Kind.LEAVES;
        }
        return Kind.ARRIVES;
    }

    /**
     * Puts {@code effect}, the one effect of {@code candidate}'s ability, on the stack for {@code event}, as the
     * collector of {@code kind} did. Returns whether it triggered.
     */
    public boolean trigger(GameData gameData, TriggerCandidate candidate, ZoneChangeEvent event, Kind kind,
                           CardEffect effect) {
        Ctx c = new Ctx(gameData, candidate, event);
        return switch (kind) {
            case DIES -> dies(c, effect);
            case SACRIFICED -> sacrificed(c, effect);
            case LEAVES -> leaves(c, effect);
            case ARRIVES -> arrives(c, effect);
        };
    }

    /** "When this dies": the dying card's graveyard incarnation is bound first. */
    private boolean dies(Ctx c, CardEffect effect) {
        if (effect instanceof ExileSourceCardFromGraveyardAndShuffleTopCardsEffect
                && !c.controllerId().equals(gameQueryService.findGraveyardOwnerById(c.gameData(),
                c.dyingPermanent() != null ? c.dyingPermanent().getOriginalCard().getId() : c.dyingCard().getId()))) {
            // Triumph of Saint Katherine exiles itself and cards from its controller's library into one pile.
            return false;
        }
        CardEffect bound = triggerCollectionService.bindSelfDeathEffect(c.gameData(), c.dyingCard(),
                c.dyingPermanent(), effect);
        return dispatch(deathHandlers, c, bound, this::handleDeathDefault);
    }

    /** "When you sacrifice this": the death handlers. */
    private boolean sacrificed(Ctx c, CardEffect effect) {
        return dispatch(deathHandlers, c, effect, this::handleDeathDefault);
    }

    /**
     * "When this leaves the battlefield". An ability of "when this is put into exile from the battlefield" refers to the
     * card as the exile object it just became (CR 400.7), so a later exile of it is another object.
     */
    private boolean leaves(Ctx c, CardEffect effect) {
        int stackSize = c.gameData().stack.size();
        if (!dispatch(leaveHandlers, c, effect, this::handleSelfLeavesDefault)) {
            return false;
        }
        List<StackEntry> stack = c.gameData().stack;
        if (c.destination() == Zone.EXILE && exileOnly(c.candidate().ability()) && stack.size() > stackSize) {
            StackEntry queued = stack.getLast();
            if (c.permanent().getId().equals(queued.getSourcePermanentId())) {
                queued.setTriggeringCardExileEntryVersion(c.gameData().exileEntryVersions
                        .getOrDefault(c.dyingCard().getId(), 0L));
            }
        }
        return true;
    }

    /** Whether every leaves-the-battlefield condition of {@code ability} is a move to exile. */
    private static boolean exileOnly(TriggeredAbility ability) {
        return ability.conditions().stream().map(TriggerCondition::trigger)
                .filter(ZoneChangeTrigger.class::isInstance).map(ZoneChangeTrigger.class::cast)
                .filter(ZoneChangeTrigger::leavesBattlefield)
                .allMatch(trigger -> trigger.to() == Zone.EXILE);
    }

    /**
     * "When this is put into a graveyard from anywhere", "from your hand", "from your library", "is milled", and
     * haunt on an instant or sorcery: the card is in its new zone and controlled by its owner. An ability that
     * targets waits for its targets as it goes on the stack (CR 603.3d).
     */
    private boolean arrives(Ctx c, CardEffect effect) {
        Card card = c.dyingCard();
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            c.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    card, c.controllerId(), new ArrayList<>(List.of(effect))));
            return true;
        }
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, c.controllerId(),
                card.getName() + "'s ability", new ArrayList<>(List.of(effect)), null, (UUID) null);
        if (c.destination() == Zone.GRAVEYARD) {
            // A graveyard card that leaves and comes back before this resolves is a new object (CR 400.7).
            entry.setTriggeringCardId(card.getId());
            entry.setTriggeringCardGraveyardEntryVersion(c.gameData().graveyardEntryVersion(card.getId()));
        }
        c.gameData().enqueueTrigger(entry);
        gameLogService.append(c.gameData(), GameLog.abilityTriggers(card));
        return true;
    }

    /** Picks the handler for {@code effect} as the collectors' registry did. */
    private static boolean dispatch(Map<Class<? extends CardEffect>, Handler> handlers, Ctx c, CardEffect effect,
                                    Handler fallback) {
        Handler handler = handlers.get(effect.getClass());
        if (handler != null) {
            return handler.handle(c, effect);
        }
        CardEffect inner = switch (effect) {
            case MayEffect may -> may.wrapped();
            case MayPayManaEffect mayPay -> mayPay.wrapped();
            case MayPayTapPermanentsEffect mayPayTap -> mayPayTap.wrapped();
            default -> null;
        };
        if (inner != null) {
            handler = handlers.get(inner.getClass());
            if (handler != null) {
                return handler.handle(c, inner);
            }
        }
        return fallback.handle(c, effect);
    }

    @SuppressWarnings("unchecked")
    private static <T extends CardEffect> void register(Map<Class<? extends CardEffect>, Handler> handlers,
                                                        Class<T> type, BiPredicate<Ctx, T> handler) {
        handlers.put(type, (c, effect) -> handler.test(c, (T) effect));
    }

    private void registerDeathHandlers() {
        Map<Class<? extends CardEffect>, Handler> h = deathHandlers;
        register(h, DistributeCountersAmongCreaturesOnDeathEffect.class,
                this::handleDistributeCountersAmongCreaturesOnDeath);
        register(h, MayPayManaEffect.class, this::handleDeathMayPayMana);
        register(h, MayPayLifeEffect.class, this::handleDeathMayPayLife);
        register(h, ChooseOneAtTriggerTimeEffect.class, this::handleDeathModalAtTriggerTime);
        register(h, MayEffect.class, this::handleDeathMayEffect);
        register(h, ReturnAllCardsExiledWithSourceEffect.class, this::handleReturnAllCardsExiledWithSource);
        register(h, PutSelfOnBottomOfOwnersLibraryAndReturnExiledCardsEffect.class,
                this::handlePutSelfOnBottomAndReturnExiledCards);
        register(h, CreateTokenForEmergeSacrificeEffect.class, this::handleCreateTokenForEmergeSacrifice);
        register(h, DestroyMerfolkTappedForSourceAbilitiesEffect.class,
                this::handleDestroyMerfolkTappedForSourceAbilities);
        register(h, ChooseOneEffect.class, this::handleModalDeathTrigger);
    }

    private void registerLeaveHandlers() {
        Map<Class<? extends CardEffect>, Handler> h = leaveHandlers;
        register(h, ChooseOneAtTriggerTimeEffect.class, this::handleModalSelfLeavesTrigger);
        register(h, ReturnLinkedCardToOwnerGraveyardEffect.class, this::handleReturnLinkedCardToOwnerGraveyard);
        register(h, ReturnAllCardsExiledWithSourceToOwnerGraveyardEffect.class,
                this::handleReturnAllCardsExiledWithSourceToOwnerGraveyard);
        register(h, ReturnTargetCardsFromGraveyardToBattlefieldEffect.class,
                this::handleReturnTargetCardsFromGraveyardSelfLeaves);
        register(h, SelfExiledWhileActivatingCraftAbilityEffect.class,
                this::handleSelfExiledWhileActivatingCraftAbility);
        register(h, ControllerLosesGameOnLeavesEffect.class, this::handleControllerLosesGameOnLeaves);
        register(h, RemoveLinkedPermanentEffect.class, this::handleRemoveLinkedPermanent);
        register(h, DestroyTokensCreatedWithSourceEffect.class, this::handleDestroyTokensCreatedWithSourceOnLeave);
        register(h, ExileTokensCreatedWithSourceEffect.class, this::handleExileTokensCreatedWithSourceOnLeave);
        register(h, MayCastCardsExiledWithSourceEffect.class, this::handleMayCastCardsExiledWithSourceOnLeave);
        register(h, SacrificeEnchantedCreatureOnLeaveEffect.class, this::handleSacrificeEnchantedCreatureOnLeave);
        register(h, DestroyEnchantedCreatureOnLeaveEffect.class, this::handleDestroyEnchantedCreatureOnLeave);
        register(h, ReturnAllCardsExiledWithSourceEffect.class, this::handleReturnAllCardsExiledWithSourceOnLeave);
        register(h, CreateTokenForImprintedCardOwnerEffect.class,
                this::handleSelfLeavesCreateTokenForImprintedCardOwner);
        register(h, CreateTokensForExiledCardsWithSourceEffect.class,
                this::handleSelfLeavesCreateTokensForExiledCardsWithSource);
        register(h, TargetPlayerGainsLifeEffect.class, this::handleSelfLeavesTargetPlayerGainsLife);
    }

    private boolean handleDistributeCountersAmongCreaturesOnDeath(Ctx c, DistributeCountersAmongCreaturesOnDeathEffect effect) {
        Permanent dyingPermanent = c.dyingPermanent();
        if (dyingPermanent == null) {
            return false;
        }
        Card dyingCard = c.dyingCard();
        UUID controllerId = c.controllerId();
        int counters = effect.countFromSourcePower()
                ? Math.max(0, dyingPermanent.getEffectivePower())
                : effect.countFromSourceCounters()
                        ? dyingPermanent.getCounterCount(effect.counterType())
                        : effect.count();
        // The dying-source form snapshots at death — the permanent is gone by resolution. The "you
        // may" and the division (pendingETBDamageAssignments) both happen when the trigger resolves.
        CardEffect baked = effect.anyCreature() || effect.targeted()
                ? new DistributeCountersAmongCreaturesOnDeathEffect(effect.counterType(), counters,
                        effect.countFromSourceCounters(), effect.anyCreature(), false, effect.optional(),
                        effect.targeted())
                : new DistributeCountersAmongControlledCreaturesEffect(effect.counterType(), counters);
        CardEffect queued = effect.optional()
                ? new MayEffect(baked, "distribute " + counters + " counter(s) among "
                        + (effect.anyCreature() ? "any number of creatures?" : "creatures you control?"))
                : baked;
        Map<UUID, Integer> announced = effect.anyCreature() || effect.targeted()
                ? announceStagedDistribution(c.gameData(), dyingCard, controllerId, effect, counters)
                : Map.of();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                dyingCard,
                controllerId,
                dyingCard.getName() + "'s ability",
                new ArrayList<>(List.of(queued))
        );
        if (!announced.isEmpty()) {
            entry.announceDamageAssignments(announced);
        }
        c.gameData().stack.add(entry);
        return true;
    }
    private boolean handleDeathMayPayMana(Ctx c, MayPayManaEffect mayPay) {
        // CR 603.3d: the ability's target is chosen as the trigger goes on the stack even though
        // the "you may pay" happens on resolution (Drainpipe Vermin). Untargeted may-pay death
        // triggers (the Spellbomb cycle) skip the stack and prompt directly.
        var wrappedSpec = mayPay.wrapped().targetSpec();
        if (wrappedSpec.admits(TargetPredicate.Kind.PERMANENT) || wrappedSpec.admits(TargetPredicate.Kind.PLAYER)) {
            c.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    c.dyingCard(), c.controllerId(), new ArrayList<>(List.of(mayPay))
            ).withTriggeringPermanent(c.dyingPermanent(), c.event().attachments()));
        } else if (wrappedSpec.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            c.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    c.dyingCard(), c.controllerId(), List.of(mayPay)));
        } else {
            // Carry the dying permanent's id and last-known state so the paid effect can read its
            // damage and combat history after the card leaves the graveyard (Giant Albatross).
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    c.dyingCard(),
                    c.controllerId(),
                    c.dyingCard().getName() + "'s ability",
                    new ArrayList<>(List.of(mayPay)),
                    null,
                    c.permanent().getId()
            );
            entry.setSourcePermanentSnapshot(new Permanent(c.permanent()));
            // CR 400.7: the trigger refers to this stay in the graveyard; if the card leaves and dies
            // again before it resolves, a return of the card itself no longer finds that object.
            entry.setTriggeringCardId(c.dyingCard().getId());
            entry.setTriggeringCardGraveyardEntryVersion(
                    c.gameData().graveyardEntryVersion(c.dyingCard().getId()));
            entry.setTriggeringPermanent(c.dyingPermanent(), c.event().attachments());
            c.gameData().stack.add(entry);
        }
        return true;
    }
    private boolean handleDeathMayPayLife(Ctx c, MayPayLifeEffect mayPay) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(mayPay)));
        markDyingCard(c, entry);
        c.gameData().stack.add(entry);
        return true;
    }
    private boolean handleDeathModalAtTriggerTime(Ctx c, ChooseOneAtTriggerTimeEffect effect) {
        c.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                c.dyingCard(), c.controllerId(), effect.choice(), c.permanent().getId()));
        return true;
    }
    private boolean handleDeathMayEffect(Ctx c, MayEffect may) {
        MayEffect resolvedMay = (MayEffect) snapshotDynamicMaxManaValue(c, may);
        // CR 603.3d: targeted "may" abilities need the target chosen when stacking — including one
        // whose targets are cards in a graveyard (Iname, Life Aspect's "you may exile it. If you do,
        // return any number of target Spirit cards from your graveyard to your hand").
        boolean graveyardTarget = graveyardTargetingSupport.findTarget(List.of(resolvedMay)) != null;
        if (resolvedMay.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || resolvedMay.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || graveyardTarget) {
            c.gameData().queueInteraction(withDyingCard(c, new PermanentChoiceContext.DeathTriggerTarget(
                    c.dyingCard(), c.controllerId(), new ArrayList<>(List.of(resolvedMay)), c.dyingPower(),
                    new Permanent(c.permanent()))));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    c.dyingCard(),
                    c.controllerId(),
                    c.dyingCard().getName() + "'s ability",
                    new ArrayList<>(List.of(resolvedMay))
            );
            entry.setSourcePermanentSnapshot(new Permanent(c.permanent()));
            entry.setTriggeringPermanentPowerAtTrigger(Math.max(0, c.dyingPower()));
            entry.setTriggeringCardGraveyardEntryVersion(c.gameData().graveyardEntryVersion(c.dyingCard().getId()));
            markDyingCard(c, entry);
            c.gameData().stack.add(entry);
        }
        return true;
    }
    private boolean handleReturnAllCardsExiledWithSource(Ctx c, ReturnAllCardsExiledWithSourceEffect effect) {
        Permanent dyingPermanent = c.dyingPermanent();
        if (dyingPermanent == null) {
            return false;
        }
        // Carry the dying permanent's ID so resolution can look up cards exiled with it.
        c.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                dyingPermanent.getId()
        ));
        return true;
    }
    private boolean handlePutSelfOnBottomAndReturnExiledCards(Ctx c, PutSelfOnBottomOfOwnersLibraryAndReturnExiledCardsEffect effect) {
        Permanent dyingPermanent = c.dyingPermanent();
        if (dyingPermanent == null) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                dyingPermanent.getId()
        );
        entry.setSourcePermanentSnapshot(new Permanent(dyingPermanent));
        c.gameData().stack.add(entry);
        return true;
    }
    private boolean handleCreateTokenForEmergeSacrifice(Ctx c, CreateTokenForEmergeSacrificeEffect effect) {
        Card castingSpell = c.castingSpell();
        boolean isEmergeSpell = castingSpell != null
                && castingSpell.getCastingOption(AlternateHandCast.class)
                .map(AlternateHandCast::reduceManaBySacrificedManaValue)
                .orElse(false);
        if (!isEmergeSpell) {
            return false;
        }

        c.gameData().pendingSpellCastCostTriggers.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect.tokenEffect()))
        ));
        return true;
    }
    private boolean handleDestroyMerfolkTappedForSourceAbilities(Ctx c, DestroyMerfolkTappedForSourceAbilitiesEffect effect) {
        Permanent dyingPermanent = c.dyingPermanent() != null ? c.dyingPermanent() : c.permanent();
        if (dyingPermanent == null) {
            return false;
        }

        CardEffect resolved = new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.MERFOLK),
                new PermanentAnyOfPredicate(dyingPermanent.getTappedPermanentsForAbilityThisTurn().stream()
                        .map(id -> (PermanentPredicate) new PermanentIsSpecificPermanentPredicate(id))
                        .toList())
        )));
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(resolved)),
                null,
                dyingPermanent.getId()
        );
        entry.setSourcePermanentSnapshot(new Permanent(dyingPermanent));
        c.gameData().stack.add(entry);
        return true;
    }
    private boolean handleModalDeathTrigger(Ctx c, ChooseOneEffect effect) {
        c.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                c.dyingCard(), c.controllerId(), effect, c.permanent().getId()));
        return true;
    }
    private boolean handleDeathDefault(Ctx c, CardEffect effect) {
        if (effect instanceof com.github.laxika.magicalvibes.model.effect.RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffect
                && !c.controllerId().equals(gameQueryService.findGraveyardOwnerById(c.gameData(), c.dyingCard().getId()))) {
            return true;
        }
        CardEffect triggerEffect = snapshotDynamicMaxManaValue(c, effect);
        // "Another target card" excludes this card: for a copy (Phantasmal Image copying Gixian
        // Puppeteer) that is the copy's own physical card, not the copied card.
        if (triggerEffect instanceof ReturnCardFromGraveyardEffect returnEffect && returnEffect.filter() != null
                && c.dyingPermanent() != null
                && !c.dyingPermanent().getOriginalCard().getId().equals(c.dyingCard().getId())) {
            triggerEffect = returnEffect.toBuilder()
                    .filter(bindSelfToCard(returnEffect.filter(), c.dyingPermanent().getOriginalCard().getId()))
                    .build();
        }
        // Graveyard-targeting death triggers (e.g. Ruin Rat: "exile target card from an opponent's
        // graveyard") also queue as a DeathTriggerTarget; processNextDeathTriggerTarget routes them
        // to a graveyard card choice made as the trigger goes on the stack.
        if (triggerEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || triggerEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || triggerEffect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                || graveyardTargetingSupport.findTarget(List.of(triggerEffect)) != null) {
            c.gameData().queueInteraction(withDyingCard(c, new PermanentChoiceContext.DeathTriggerTarget(
                    c.dyingCard(), c.controllerId(), new ArrayList<>(List.of(triggerEffect)), c.dyingPower(),
                    new Permanent(c.permanent())
            )));
        } else {
            // A ConditionalEffect's intervening-"if" may be about the dying permanent itself
            // (Fyndhorn Druid's "if it was blocked this turn"), so carry its id even though the
            // permanent has already left the battlefield — turn-scoped trackers are keyed by id.
            UUID sourcePermanentId = c.permanent().getId();
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    c.dyingCard(),
                    c.controllerId(),
                    c.dyingCard().getName() + "'s ability",
                    new ArrayList<>(List.of(triggerEffect)),
                    null,
                    sourcePermanentId
            );
            entry.setSourcePermanentSnapshot(new Permanent(c.permanent()));
            // Preserve cast-mode state for conditional abilities that resolve after the permanent
            // has left the battlefield, such as a blitz-only death trigger.
            entry.setAlternateCost(c.permanent().isAlternateCost());
            entry.setTriggeringPermanentPowerAtTrigger(Math.max(0, c.dyingPower()));
            entry.setEventValue(c.dyingPower());
            entry.setDyingPermanentManaValue(c.dyingPermanent() != null
                    ? c.dyingPermanent().getCard().getManaValue()
                    : c.dyingCard().getManaValue());
            entry.setTriggeringCardGraveyardEntryVersion(c.gameData().graveyardEntryVersion(graveyardCardIdOf(c)));
            markDyingCard(c, entry);
            c.gameData().stack.add(entry);
        }
        return true;
    }
    private boolean handleModalSelfLeavesTrigger(Ctx c, ChooseOneAtTriggerTimeEffect effect) {
        c.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                c.dyingCard(), c.controllerId(), effect.choice(), c.permanent().getId()));
        logSelfLeaves(c);
        return true;
    }
    private boolean handleReturnLinkedCardToOwnerGraveyard(Ctx c, ReturnLinkedCardToOwnerGraveyardEffect effect) {
        UUID linkedCardId = c.permanent().getChosenPermanentId();
        if (linkedCardId == null) {
            return false;
        }
        c.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(new ReturnLinkedCardToOwnerGraveyardEffect(linkedCardId)))
        ));
        logSelfLeaves(c);
        return true;
    }
    private boolean handleReturnAllCardsExiledWithSourceToOwnerGraveyard(Ctx c, ReturnAllCardsExiledWithSourceToOwnerGraveyardEffect effect) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(new ReturnAllCardsExiledWithSourceToOwnerGraveyardEffect())),
                null,
                c.permanent().getId());
        c.gameData().stack.add(entry);
        logSelfLeaves(c);
        return true;
    }
    private boolean handleReturnTargetCardsFromGraveyardSelfLeaves(Ctx c, ReturnTargetCardsFromGraveyardToBattlefieldEffect effect) {
        graveyardTargetingService.handleReturnToBattlefieldETBTargeting(c.gameData(), c.controllerId(),
                c.dyingCard(), List.of(effect), effect, effect.maxTargets());
        logSelfLeaves(c);
        return true;
    }
    private boolean handleSelfExiledWhileActivatingCraftAbility(Ctx c, SelfExiledWhileActivatingCraftAbilityEffect effect) {
        if (c.destination() != com.github.laxika.magicalvibes.model.Zone.EXILE
                || !c.exiledWhileActivatingCraftAbility()) {
            return false;
        }
        return handleSelfLeavesDefault(c, effect.wrapped());
    }

    private boolean handleControllerLosesGameOnLeaves(Ctx c, ControllerLosesGameOnLeavesEffect effect) {
        c.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(new TargetPlayerLosesGameEffect(c.controllerId())))
        ));
        logSelfLeaves(c);
        return true;
    }

    private boolean handleRemoveLinkedPermanent(Ctx c, RemoveLinkedPermanentEffect effect) {
        // The partner id is stored on the leaving permanent; if the bond was never forged (no token was
        // created) or was already broken, the trigger simply does nothing (CR 603.4-style no-op).
        UUID linkedId = c.permanent().getChosenPermanentId();
        if (linkedId == null) {
            return false;
        }
        Permanent linked = gameQueryService.findPermanentById(c.gameData(), linkedId);
        UUID linkedCardId = linked == null ? null : linked.getCard().getId();
        // SACRIFICE_PARTNER: the sacrifice ability belongs to the partner (e.g. "when the token leaves, sacrifice
        // this enchantment"), so it is controlled by the partner's controller, which can differ from the leaving
        // token's controller.
        UUID abilityControllerId = c.controllerId();
        if (effect.mode() == RemoveLinkedPermanentEffect.Mode.SACRIFICE_PARTNER && linked != null) {
            UUID partnerControllerId = gameQueryService.findPermanentController(c.gameData(), linkedId);
            if (partnerControllerId != null) {
                abilityControllerId = partnerControllerId;
            }
        }
        // Bake the linked id into the effect (not the stack entry's targetId, which resolution would
        // validate against the source card's spell target filter and fizzle — Dance of Many's ETB filter
        // is "nontoken creature", which the token itself fails).
        c.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                abilityControllerId,
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(new RemoveLinkedPermanentEffect(effect.mode(), linkedId, linkedCardId)))
        ));
        logSelfLeaves(c);
        return true;
    }

    private boolean handleDestroyTokensCreatedWithSourceOnLeave(Ctx c, DestroyTokensCreatedWithSourceEffect effect) {
        // The source has already left, so its id is baked into the effect — the stack entry cannot
        // carry a sourcePermanentId that still resolves to a battlefield permanent.
        c.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(new DestroyTokensCreatedWithSourceEffect(
                        effect.cannotBeRegenerated(), c.permanent().getId())))
        ));
        logSelfLeaves(c);
        return true;
    }

    private boolean handleExileTokensCreatedWithSourceOnLeave(Ctx c, ExileTokensCreatedWithSourceEffect effect) {
        c.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(new ExileTokensCreatedWithSourceEffect(
                        c.permanent().getId(), effect.atNextEndStep())))
        ));
        logSelfLeaves(c);
        return true;
    }

    private boolean handleMayCastCardsExiledWithSourceOnLeave(Ctx c, MayCastCardsExiledWithSourceEffect effect) {
        // The source has already left, so the id the exiled cards are tracked under is baked into
        // the effect — nothing on the stack can resolve it back to a battlefield permanent.
        c.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(new MayCastCardsExiledWithSourceEffect(c.permanent().getId())))
        ));
        logSelfLeaves(c);
        return true;
    }

    private boolean handleSacrificeEnchantedCreatureOnLeave(Ctx c, SacrificeEnchantedCreatureOnLeaveEffect effect) {
        // Capture the enchanted creature's permanent id before the Aura is gone. An Aura that could not
        // attach keeps the reanimated creature as its chosen permanent, so that creature is still sacrificed.
        UUID enchantedId = c.permanent().getAttachedTo() != null
                ? c.permanent().getAttachedTo()
                : c.permanent().getChosenPermanentId();
        if (enchantedId == null) {
            return false;
        }
        c.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(new SacrificeEnchantedCreatureOnLeaveEffect(enchantedId)))
        ));
        logSelfLeaves(c);
        return true;
    }

    private boolean handleDestroyEnchantedCreatureOnLeave(Ctx c, DestroyEnchantedCreatureOnLeaveEffect effect) {
        UUID enchantedId = c.permanent().getAttachedTo();
        if (enchantedId == null) {
            return false;
        }
        c.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(new DestroyEnchantedCreatureOnLeaveEffect(
                        enchantedId, effect.cannotBeRegenerated())))
        ));
        logSelfLeaves(c);
        return true;
    }

    private boolean handleReturnAllCardsExiledWithSourceOnLeave(Ctx c, ReturnAllCardsExiledWithSourceEffect effect) {
        UUID sourcePermanentId = effect.useLinkedSource()
                ? c.permanent().getChosenPermanentId()
                : c.permanent().getId();
        if (sourcePermanentId == null) {
            return false;
        }
        c.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                sourcePermanentId,
                List.of()
        ));
        logSelfLeaves(c);
        return true;
    }

    /**
     * "When this permanent leaves the battlefield, the imprinted card's owner creates an X/X token."
     * The source is already gone by the time the trigger resolves, so the blueprint's dynamic
     * power/toughness is evaluated here against the leaving permanent and frozen into the queued effect.
     */
    private boolean handleSelfLeavesCreateTokenForImprintedCardOwner(Ctx c, CreateTokenForImprintedCardOwnerEffect effect) {
        Card imprintedCard = c.gameData().getImprintedCard(c.dyingCard());
        if (imprintedCard == null) {
            return false;
        }

        CreateTokenEffect blueprint = effect.tokenEffect();
        AmountContext amountContext = new AmountContext(c.controllerId(), c.permanent(), null, 0, 0);
        CreateTokenEffect frozen = blueprint.withPowerToughness(
                amountEvaluationService.evaluate(c.gameData(), blueprint.power(), amountContext),
                amountEvaluationService.evaluate(c.gameData(), blueprint.toughness(), amountContext));
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(new CreateTokenForImprintedCardOwnerEffect(frozen))),
                c.permanent().getId(),
                List.of()
        );
        entry.setSourcePermanentSnapshot(new Permanent(c.permanent()));
        entry.setNonTargeting(true);
        c.gameData().stack.add(entry);
        logSelfLeaves(c);
        return true;
    }

    private boolean handleSelfLeavesCreateTokensForExiledCardsWithSource(Ctx c, CreateTokensForExiledCardsWithSourceEffect effect) {
        boolean hasExiledCard;
        synchronized (c.gameData().exiledCards) {
            hasExiledCard = c.gameData().exiledCards.stream()
                    .anyMatch(exiled -> c.permanent().getId().equals(exiled.sourcePermanentId()));
        }
        if (!hasExiledCard) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(new CreateTokensForExiledCardsWithSourceEffect(
                        effect.tokenEffect(), c.permanent().getId()))),
                c.permanent().getId(),
                List.of()
        );
        entry.setSourcePermanentSnapshot(new Permanent(c.permanent()));
        entry.setNonTargeting(true);
        c.gameData().stack.add(entry);
        logSelfLeaves(c);
        return true;
    }

    private boolean handleSelfLeavesTargetPlayerGainsLife(Ctx c, TargetPlayerGainsLifeEffect effect) {
        UUID targetPlayerId = c.permanent().getRememberedTargetPlayerId();
        if (targetPlayerId == null) {
            targetPlayerId = findPendingEnterTarget(c.gameData(), c.permanent());
        }
        if (targetPlayerId == null || !c.gameData().playerIds.contains(targetPlayerId)) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                c.dyingCard(),
                c.controllerId(),
                c.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                targetPlayerId,
                c.permanent().getId());
        entry.setSourcePermanentSnapshot(new Permanent(c.permanent()));
        entry.setTriggeringPermanent(c.dyingPermanent());
        entry.setNonTargeting(true);
        c.gameData().stack.add(entry);
        logSelfLeaves(c);
        return true;
    }

    /**
     * The id of the card that lands in the graveyard when this permanent dies. For a transformed
     * permanent that is the front face it was cast as, not the back face currently on the battlefield.
     */
    private static UUID graveyardCardIdOf(Ctx c) {
        return c.dyingPermanent() != null
                ? c.dyingPermanent().getOriginalCard().getId()
                : c.dyingCard().getId();
    }

    /**
     * Gives {@code entry} the card that went to the graveyard ("return it", "exile that card") as its triggering card,
     * with the graveyard incarnation it has now (CR 400.7), and the permanent as it last existed on the battlefield
     * with what was attached to it ("its counters", "that creature", CR 603.10a) as its triggering permanent, for the
     * effects that read them as they resolve.
     */
    private static void markDyingCard(Ctx c, StackEntry entry) {
        if (c.destination() == Zone.GRAVEYARD) {
            UUID cardId = graveyardCardIdOf(c);
            entry.setTriggeringCard(cardId, c.gameData().graveyardEntryVersion(cardId));
        }
        if (c.dyingPermanent() != null) {
            entry.setTriggeringPermanent(c.dyingPermanent(), c.event().attachments());
        }
    }

    /** {@code target} with the triggering card and permanent {@link #markDyingCard} gives an entry. */
    private static PermanentChoiceContext.DeathTriggerTarget withDyingCard(
            Ctx c, PermanentChoiceContext.DeathTriggerTarget target) {
        PermanentChoiceContext.DeathTriggerTarget withPermanent = c.dyingPermanent() == null ? target
                : target.withTriggeringPermanent(c.dyingPermanent(), c.event().attachments());
        if (c.destination() != Zone.GRAVEYARD) {
            return withPermanent;
        }
        UUID cardId = graveyardCardIdOf(c);
        return withPermanent.withTriggeringCard(cardId, c.gameData().graveyardEntryVersion(cardId));
    }


    /**
     * Takes the division staged in {@code pendingETBDamageAssignments} as the division announced for
     * a targeted distribution trigger going on the stack (CR 601.2d, CR 603.3d), so it is fixed before
     * any player receives priority and cannot be changed at resolution. Every recipient must be a
     * legal target when announced and the amounts must total {@code total}; otherwise the
     * announcement is rejected.
     *
     * @return the announced division, or an empty map when nothing was staged
     */
    private Map<UUID, Integer> announceStagedDistribution(GameData gameData, Card source, UUID controllerId,
            DistributeCountersAmongCreaturesOnDeathEffect effect, int total) {
        Map<UUID, Integer> staged = gameData.pendingETBDamageAssignments;
        gameData.pendingETBDamageAssignments = Map.of();
        if (staged.isEmpty()) {
            return Map.of();
        }
        int announcedTotal = 0;
        for (Map.Entry<UUID, Integer> assignment : staged.entrySet()) {
            Integer amount = assignment.getValue();
            if (amount == null || amount <= 0) {
                throw new IllegalStateException("Each target must receive at least 1 counter");
            }
            Permanent target = gameQueryService.findPermanentById(gameData, assignment.getKey());
            if (target == null || !gameQueryService.isCreature(gameData, target)
                    || (!effect.anyCreature()
                            && !controllerId.equals(gameQueryService.findPermanentController(gameData, target.getId())))) {
                throw new IllegalStateException("Counters can only be distributed among legal creature targets");
            }
            if (targetLegalityService.checkTriggeredPermanentTargetableReason(
                    gameData, target, source, controllerId).isPresent()) {
                throw new IllegalStateException("Illegal target for distributed counters");
            }
            announcedTotal += amount;
        }
        if (announcedTotal != total) {
            throw new IllegalStateException("Counters must be distributed in a total of " + total);
        }
        return staged;
    }

    private static CardPredicate bindSelfToCard(CardPredicate predicate, UUID cardId) {
        return switch (predicate) {
            case CardIsSelfPredicate ignored -> new CardIdSetPredicate(Set.of(cardId));
            case CardNotPredicate not -> new CardNotPredicate(bindSelfToCard(not.predicate(), cardId));
            case CardAllOfPredicate all -> new CardAllOfPredicate(all.predicates().stream()
                    .map(p -> bindSelfToCard(p, cardId)).toList());
            case CardAnyOfPredicate any -> new CardAnyOfPredicate(any.predicates().stream()
                    .map(p -> bindSelfToCard(p, cardId)).toList());
            default -> predicate;
        };
    }

    private CardEffect snapshotDynamicMaxManaValue(Ctx c, CardEffect effect) {
        if (effect instanceof MayEffect may) {
            CardEffect wrapped = snapshotDynamicMaxManaValue(c, may.wrapped());
            return wrapped == may.wrapped() ? may : new MayEffect(wrapped, may.prompt(), may.elseEffect());
        }
        if (effect instanceof PutImprintedCardIntoOwnersHandEffect put && put.boundCardId() == null) {
            Card imprinted = c.gameData().getImprintedCard(c.dyingCard());
            return imprinted == null ? effect : new PutImprintedCardIntoOwnersHandEffect(imprinted.getId());
        }
        if (effect instanceof ExileSourceCardFromGraveyardThenEffect exileThen) {
            CardEffect snapshotted = snapshotDynamicMaxManaValue(c, exileThen.thenEffect());
            return snapshotted == exileThen.thenEffect() ? exileThen
                    : new ExileSourceCardFromGraveyardThenEffect(snapshotted, exileThen.reflexive());
        }
        if (!(effect instanceof ReturnCardFromGraveyardEffect returnEffect)
                || returnEffect.dynamicMaxManaValue() == null) {
            return effect;
        }

        Permanent dyingPermanent = c.dyingPermanent() != null
                ? c.dyingPermanent() : c.permanent();
        int maxManaValue = amountEvaluationService.evaluateAtDeath(
                c.gameData(), returnEffect.dynamicMaxManaValue(), c.controllerId(), dyingPermanent);
        CardPredicate manaValueFilter = new CardMaxManaValuePredicate(maxManaValue);
        CardPredicate filter = returnEffect.filter() == null
                ? manaValueFilter
                : new CardAllOfPredicate(List.of(returnEffect.filter(), manaValueFilter));
        return returnEffect.toBuilder()
                .filter(filter)
                .dynamicMaxManaValue(null)
                .build();
    }


    private UUID findPendingEnterTarget(GameData gameData, Permanent source) {
        return gameData.stack.stream()
                .filter(entry -> source.getId().equals(entry.getSourcePermanentId()))
                .filter(entry -> entry.getEffectsToResolve().stream()
                        .anyMatch(RememberTargetPlayerEffect.class::isInstance))
                .map(StackEntry::getTargetId)
                .filter(gameData.playerIds::contains)
                .findFirst()
                .orElse(null);
    }

    private boolean handleSelfLeavesDefault(Ctx c, CardEffect effect) {
        // Graveyard-targeting self-leaves triggers (e.g. Offalsnout) also queue as a
        // The queue processor routes graveyard targets to a card choice.
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                || effect instanceof ReturnTargetCardsFromGraveyardToHandEffect) {
            c.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    c.dyingCard(), c.controllerId(), new ArrayList<>(List.of(effect)),
                    "leaves-the-battlefield", c.permanent().getId(), new Permanent(c.permanent())
            ).withTriggeringPermanent(c.dyingPermanent()));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    c.dyingCard(),
                    c.controllerId(),
                    c.dyingCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    c.permanent().getId(),
                    List.of()
            );
            entry.setSourcePermanentSnapshot(new Permanent(c.permanent()));
            entry.setTriggeringPermanent(c.dyingPermanent());
            c.gameData().stack.add(entry);
        }
        logSelfLeaves(c);
        return true;
    }

    private void logSelfLeaves(Ctx c) {
        gameLogService.append(c.gameData(), GameLog.cardThen(c.dyingCard(),
                "'s ability triggers (left the battlefield)."));
        log.info("Game {} - {} triggers (left the battlefield)", c.gameData().id, c.dyingCard().getName());
    }
}
