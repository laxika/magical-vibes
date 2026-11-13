package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.TreasureManaSpentToActivate;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileUpToCombatDamageCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.CountersOnTriggeringPermanent;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithDyingSourcePowerCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToBlockedAttackersOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutImprintedCardIntoOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The event binding of the "when this dies" and "when this leaves the battlefield" abilities: what each effect's
 * handler puts toward the stack for the zone change that triggered it.
 */
@ExtendWith(MockitoExtension.class)
class TriggeredAbilityStackServiceTest {

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private PredicateEvaluationService predicateEvaluationService;

    @Mock
    private com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector triggerTargetCollector;
    @Mock
    private GameLogService gameLogService;
    @Mock
    private AmountEvaluationService amountEvaluationService;
    @Mock
    private ConditionEvaluationService conditionEvaluationService;
    @Mock
    private GraveyardTargetingSupport graveyardTargetingSupport;
    @Mock
    private TriggerCollectionService triggerCollectionService;

    private TriggeredAbilityStackService stackService;

    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    /** The zone change a test triggers an ability for, with the trigger it meets and the permanent that moved. */
    private record Triggering(ZoneChangeTrigger trigger, Card card, Permanent permanent, UUID controllerId,
                              ZoneChangeEvent event) {
    }

    @BeforeEach
    void setUp() {
        stackService = new TriggeredAbilityStackService(conditionEvaluationService, gameQueryService,
                graveyardTargetingSupport, gameLogService, triggerCollectionService, amountEvaluationService,
                predicateEvaluationService, triggerTargetCollector);
        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Player1");
        gd.playerIdToName.put(PLAYER2_ID, "Player2");
        gd.playerBattlefields.put(PLAYER1_ID, new ArrayList<>());
        gd.playerBattlefields.put(PLAYER2_ID, new ArrayList<>());
        gd.playerGraveyards.put(PLAYER1_ID, new ArrayList<>());
        gd.playerGraveyards.put(PLAYER2_ID, new ArrayList<>());
    }

    /** {@code permanent}, the last-known copy of {@code card}, died under {@code controllerId}'s control. */
    private Triggering dies(Card card, UUID controllerId, Permanent permanent) {
        return dies(card, controllerId, permanent, permanent.getEffectivePower());
    }

    private Triggering dies(Card card, UUID controllerId, Permanent permanent, int power) {
        permanent.setLastKnownPower(power);
        ZoneChangeEvent event = new ZoneChangeEvent(card, controllerId, Zone.BATTLEFIELD, Zone.GRAVEYARD,
                ZoneChangeCause.DESTROY, permanent, controllerId);
        return new Triggering(Triggers.dies(ObjectRelation.THIS), card, permanent, controllerId, event);
    }

    /** {@code permanent} left the battlefield for its owner's graveyard. */
    private Triggering leaves(Permanent permanent, UUID controllerId) {
        ZoneChangeEvent event = new ZoneChangeEvent(permanent.getCard(), controllerId, Zone.BATTLEFIELD,
                Zone.GRAVEYARD, ZoneChangeCause.OTHER, permanent, controllerId);
        return new Triggering(Triggers.leavesBattlefield(ObjectRelation.THIS), permanent.getCard(), permanent,
                controllerId, event);
    }

    /** Triggers the ability of {@code effect} the moving object has for its own zone change. */
    private void trigger(Triggering triggering, CardEffect effect) {
        TriggerCandidate candidate = new TriggerCandidate(triggering.card(), triggering.permanent(),
                triggering.controllerId(), Zone.BATTLEFIELD, new TriggeredAbility(triggering.trigger(), effect));
        stackService.trigger(gd, candidate, triggering.event(), triggering.trigger());
    }

    private Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private Card createEnchantment(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost("");
        return card;
    }

    private Card createEquipment(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        card.setManaCost("");
        return card;
    }

    @Test
    @DisplayName("Uses the dying creature's power snapshot for incubate-by-power triggers")
    void usesDyingPowerSnapshotForIncubate() {
        Card card = createCreature("Furnace Gremlin", 1, 2);
        Permanent perm = new Permanent(card);
        var effect = new CreateTokenWithDyingSourcePowerCountersEffect(null);
        var ctx = dies(card, PLAYER1_ID, perm, 2);

        trigger(ctx, effect);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Blocked-attacker damage goes on the stack unchanged, with the dying blocker as its triggering permanent")
    void blockedAttackerDamageCarriesTheDyingBlocker() {
        Card card = createCreature("Blocker", 1, 1);
        var deathDmg = new DealDamageToBlockedAttackersOnDeathEffect(2);
        Permanent perm = new Permanent(card);
        UUID attackerId = UUID.randomUUID();
        perm.addBlockingTargetId(attackerId);
        gd.currentStep = TurnStep.COMBAT_DAMAGE;
        var ctx = dies(card, PLAYER1_ID, perm);

        trigger(ctx, deathDmg);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEffectsToResolve()).containsExactly(deathDmg);
        assertThat(entry.getTargetIds()).isEmpty();
        assertThat(entry.getTriggeringPermanentSnapshot().getBlockedAttackerIdsThisCombat())
                .containsExactly(attackerId);
    }

    @Test
    @DisplayName("A \"you may\" counter distribution goes on the stack unchanged, with the dying creature as its triggering permanent")
    void mayDistributionGoesOnTheStackWithTheDyingCreature() {
        Card card = createCreature("Hydra", 0, 0);
        var effect = new MayEffect(new DistributeCountersAmongControlledCreaturesEffect(CounterType.PLUS_ONE_PLUS_ONE,
                new CountersOnTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE)), "Distribute the counters?");
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        var ctx = dies(card, PLAYER1_ID, perm);

        trigger(ctx, effect);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        assertThat(entry.triggeringPermanentCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Modular's \"you may\" waits for its target unchanged, with the dying creature as the triggering permanent")
    void modularWaitsForItsTargetWithTheDyingCreature() {
        Card card = createCreature("Arcbound Bruiser", 0, 0);
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        var effect = new MayEffect(PutCounterOnTargetForEachDyingSourceCounterEffect.modularAbility(),
                PutCounterOnTargetForEachDyingSourceCounterEffect.MODULAR_PROMPT);
        var ctx = dies(card, PLAYER1_ID, perm);

        trigger(ctx, effect);

        var pending = gd.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);
        assertThat(pending.effects()).containsExactly(effect);
        assertThat(pending.triggeringPermanent().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counters equal to its power wait for their target unchanged, with the dying creature as it last existed")
    void powerCountersCarryTheDyingCreature() {
        Card card = createCreature("Galuf's target", 3, 2);
        Permanent perm = new Permanent(card);
        var effect = new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, new SourcePower(), null,
                new PermanentIsCreaturePredicate(), false, null, true);
        var ctx = dies(card, PLAYER1_ID, perm);

        trigger(ctx, effect);

        var pending = gd.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);
        assertThat(pending.effects()).containsExactly(effect);
        assertThat(pending.sourcePermanentSnapshot().getLastKnownPower()).isEqualTo(3);
        assertThat(pending.triggeringPermanent().getId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("Queues may ability with mana cost")
    void queuesMayAbility() {
        Card card = createCreature("Mana Dude", 2, 2);
        var mayPay = new MayPayManaEffect("{2}", new DrawCardEffect(1), "Pay 2?");
        Permanent perm = new Permanent(card);
        var ctx = dies(card, PLAYER1_ID, perm);

        trigger(ctx, mayPay);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayPayManaEffect.class);
    }

    @Test
    @DisplayName("Adds the optional life payment to the stack")
    void addsLifePaymentToStack() {
        Card card = createCreature("Life Watcher", 2, 2);
        var mayPay = new MayPayLifeEffect(2, new DrawCardEffect(1), "Pay 2?");
        Permanent perm = new Permanent(card);
        var ctx = dies(card, PLAYER1_ID, perm);

        trigger(ctx, mayPay);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isEqualTo(mayPay);
    }

    @Test
    @DisplayName("Queues a triggered modal choice")
    void queuesTriggeredModalChoice() {
        Card card = createCreature("Shambling Ghast", 1, 1);
        Permanent perm = new Permanent(card);
        var choice = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw", new DrawCardEffect(1)),
                new ChooseOneEffect.ChooseOneOption("Gain life", new GainLifeEffect(1))));
        var effect = new ChooseOneAtTriggerTimeEffect(choice);
        var ctx = dies(card, PLAYER1_ID, perm);

        trigger(ctx, effect);

        var pending = gd.peekPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class);
        assertThat(pending.sourceCard()).isEqualTo(card);
        assertThat(pending.controllerId()).isEqualTo(PLAYER1_ID);
        assertThat(pending.effect()).isEqualTo(choice);
        assertThat(pending.sourcePermanentId()).isEqualTo(perm.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Non-targeting MayEffect queues may ability")
    void nonTargetingQueuesMayAbility() {
        Card card = createCreature("Optional", 1, 1);
        var may = new MayEffect(new DrawCardEffect(1), "Draw?");
        Permanent perm = new Permanent(card);
        var ctx = dies(card, PLAYER1_ID, perm);

        trigger(ctx, may);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayEffect.class);
    }

    @Test
    @DisplayName("Targeting MayEffect queues a DeathTriggerTarget interaction (CR 603.3d)")
    void targetingGoesToPendingTargets() {
        Card card = createCreature("Targeted May", 2, 2);
        var may = new MayEffect(new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PLAYER), "Drain?");
        Permanent perm = new Permanent(card);
        var ctx = dies(card, PLAYER1_ID, perm);

        trigger(ctx, may);

        assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Non-targeting effect adds to stack")
    void nonTargetingAddsToStack() {
        Card card = createCreature("Dying Dude", 2, 2);
        var effect = new DrawCardEffect(1);
        Permanent perm = new Permanent(card);
        var ctx = dies(card, PLAYER1_ID, perm);

        trigger(ctx, effect);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.get(0);
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getControllerId()).isEqualTo(PLAYER1_ID);
        assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
        assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(DrawCardEffect.class);
    }

    @Test
    @DisplayName("A death entry keeps the card the source had exiled as its linked exiled card")
    void deathEntryKeepsTheLinkedExiledCard() {
        Card card = createCreature("Hoarding Dragon", 4, 4);
        Card exiled = createEquipment("Hoarded Equipment");
        gd.setImprintedCard(card, exiled);
        var effect = new MayEffect(new PutImprintedCardIntoOwnersHandEffect(), "Put the exiled card into its owner's hand?");

        trigger(dies(card, PLAYER1_ID, new Permanent(card)), effect);
        gd.setImprintedCard(card, createEquipment("Later Equipment"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getFirst().getLinkedExiledCardId()).isEqualTo(exiled.getId());
    }

    @Test
    void transformedDeathGivesThePhysicalCardAsTheTriggeringCard() {
        Card front = createCreature("Front", 2, 2);
        Card back = createCreature("Back", 4, 4);
        Permanent permanent = new Permanent(front);
        permanent.setCard(back);
        permanent.setTransformed(true);
        var effect = new PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect();
        var context = dies(back, PLAYER1_ID, permanent);

        trigger(context, effect);

        assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getFirst().getTriggeringCardId()).isEqualTo(front.getId());
    }

    @Test
    void unchangedDeathSequencePreservesItsTargetBinding() {
        Card card = createCreature("Optional death trigger", 2, 2);
        CardEffect effect = SequenceEffect.of(
                new MayEffect(new DrawCardEffect(1), "Draw?"),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
        card.target(0, 1).addTrigger(Triggers.dies(ObjectRelation.THIS), effect);
        Permanent permanent = new Permanent(card);

        trigger(dies(card, PLAYER1_ID, permanent), effect);

        var pending = gd.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);
        assertThat(pending).isNotNull();
        assertThat(pending.dyingCard().isEffectBoundToTargetGroup(pending.effects().getFirst(), 0)).isTrue();
    }

    @Test
    @DisplayName("Preserves alternate-cost state for a conditional death trigger")
    void preservesAlternateCostState() {
        Card card = createCreature("Blitz Creature", 2, 2);
        var effect = new ConditionalEffect(new CastForAlternateCost(), new DrawCardEffect(1));
        Permanent perm = new Permanent(card);
        perm.setAlternateCost(true);
        var ctx = dies(card, PLAYER1_ID, perm);

        trigger(ctx, effect);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isAlternateCost()).isTrue();
    }

    @Test
    @DisplayName("Targeting effect queues DeathTriggerTarget")
    void targetingQueuesDeathTriggerTarget() {
        Card card = createCreature("Targeting Dude", 3, 3);
        var effect = new PutCounterOnTargetPermanentEffect(CounterType.CHARGE);
        Permanent perm = new Permanent(card);
        var ctx = dies(card, PLAYER1_ID, perm);

        trigger(ctx, effect);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
    }

    @Test
    @DisplayName("Graveyard-targeting effect queues DeathTriggerTarget (Ruin Rat)")
    void graveyardTargetingQueuesDeathTriggerTarget() {
        Card card = createCreature("Ruin Rat", 1, 1);
        var effect = new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD);
        Permanent perm = new Permanent(card);
        var ctx = dies(card, PLAYER1_ID, perm);

        trigger(ctx, effect);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
    }

    @Test
    @DisplayName("Multi-target graveyard return queues DeathTriggerTarget")
    void multiTargetGraveyardReturnQueuesDeathTriggerTarget() {
        Card card = createCreature("Forked-Branch Garami", 4, 4);
        var effect = new ReturnTargetCardsFromGraveyardToHandEffect(null, 2);
        Permanent perm = new Permanent(card);
        var ctx = dies(card, PLAYER1_ID, perm);
        when(graveyardTargetingSupport.findTarget(List.of(effect))).thenReturn(
                new com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport.Target(
                        null, GraveyardSearchScope.CONTROLLERS_GRAVEYARD, "to your hand", 2));

        trigger(ctx, effect);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
    }

    @Test
    @DisplayName("Carries the Equipment's last-known attachment as the triggering permanent")
    void carriesLastKnownAttachment() {
        Card equipment = createEquipment("Oathkeeper");
        Permanent perm = new Permanent(equipment);
        UUID equippedId = UUID.randomUUID();
        perm.setAttachedTo(equippedId);
        var effect = new ExileEquippedCreatureEffect();
        var ctx = dies(equipment, PLAYER1_ID, perm);

        trigger(ctx, effect);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.get(0).getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.get(0).getTriggeringPermanentSnapshot().getAttachedTo()).isEqualTo(equippedId);
    }

    @Test
    @DisplayName("Still triggers when the Equipment was unattached (no intervening if, CR 603.2)")
    void unattachedStillTriggers() {
        Card equipment = createEquipment("Oathkeeper");
        Permanent perm = new Permanent(equipment);
        var effect = new ExileEquippedCreatureEffect();
        var ctx = dies(equipment, PLAYER1_ID, perm);

        trigger(ctx, effect);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("\"You lose the game\" goes on the stack as written, controlled by the leaving permanent's controller")
    void controllerLosesGameGoesOnTheStackAsWritten() {
        Card card = createCreature("Pact Creature", 5, 5);
        var effect = new ControllerLosesGameEffect();
        Permanent perm = new Permanent(card);
        var ctx = leaves(perm, PLAYER1_ID);

        trigger(ctx, effect);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.get(0).getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.get(0).getControllerId()).isEqualTo(PLAYER1_ID);
    }

    @Test
    @DisplayName("Generic effect adds to stack")
    void genericEffectAddsToStack() {
        Card card = createCreature("Leaving Dude", 2, 2);
        var effect = new DrawCardEffect(1);
        Permanent perm = new Permanent(card);
        var ctx = leaves(perm, PLAYER1_ID);

        trigger(ctx, effect);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(DrawCardEffect.class);
    }

    @Test
    @DisplayName("Leaving-source counter trigger carries the leaving permanent and queues target selection")
    void leavingSourceCounterTriggerCarriesTheLeavingPermanent() {
        Card card = createCreature("Counter Captain", 1, 1);
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        var effect = new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE,
                new CountersOnTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE));
        var ctx = leaves(perm, PLAYER1_ID);

        trigger(ctx, effect);

        var pending = gd.peekPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class);
        assertThat(pending.effects()).containsExactly(effect);
        assertThat(pending.triggeringPermanent().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("DestroyEnchantedCreatureOnLeaveEffect goes on the stack as written, the Aura as it last existed its source")
    void destroyEnchantedCreatureKeepsTheAuraAsItLastExisted() {
        Permanent aura = new Permanent(createEnchantment("Leaving Aura"));
        Permanent creature = new Permanent(createCreature("Enchanted Creature", 2, 2));
        aura.setAttachedTo(creature.getId());
        var effect = new DestroyEnchantedCreatureOnLeaveEffect();
        var ctx = leaves(aura, PLAYER1_ID);

        trigger(ctx, effect);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.get(0);
        assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        assertThat(entry.getSourcePermanentId()).isEqualTo(aura.getId());
        assertThat(entry.getSourcePermanentSnapshot().getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("ReturnAllCardsExiledWithSourceEffect preserves the leaving permanent ID")
    void returnAllCardsExiledWithSourcePreservesSourceId() {
        Permanent perm = new Permanent(createCreature("Leaving Angel", 6, 6));
        var effect = new ReturnAllCardsExiledWithSourceEffect();
        var ctx = leaves(perm, PLAYER1_ID);

        trigger(ctx, effect);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.get(0);
        assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
        assertThat(entry.getEffectsToResolve()).containsExactly(effect);
    }

    @Test
    @DisplayName("Logs trigger message")
    void logsMessage() {
        Card card = createCreature("Logged Leaver", 2, 2);
        var effect = new DrawCardEffect(1);
        Permanent perm = new Permanent(card);
        var ctx = leaves(perm, PLAYER1_ID);

        trigger(ctx, effect);

        verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("Logged Leaver")
                && logEntry.plainText().contains("triggers")));
    }

    @Test
    @DisplayName("A \"choose one\" death ability's mode is chosen as it goes on the stack (CR 603.3c)")
    void chooseOneModeIsChosenAsItTriggers() {
        Card card = createCreature("Ao, the Dawn Sky", 5, 4);
        Permanent perm = new Permanent(card);
        var choice = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw", new DrawCardEffect(1)),
                new ChooseOneEffect.ChooseOneOption("Gain life", new GainLifeEffect(1))));

        trigger(dies(card, PLAYER1_ID, perm), choice);

        var pending = gd.peekPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class);
        assertThat(pending.effect()).isEqualTo(choice);
        assertThat(pending.sourcePermanentId()).isEqualTo(perm.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A leaves-the-battlefield entry records the permanent as it last existed whatever the effect is")
    void leavesEntryRecordsTheLeavingPermanent() {
        Card card = createCreature("Leaving Dude", 3, 3);
        Permanent perm = new Permanent(card);
        perm.setLastKnownPower(3);
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        trigger(leaves(perm, PLAYER1_ID), new DrawCardEffect(1));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.isNonTargeting()).isTrue();
        assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
        assertThat(entry.getEventValue()).isEqualTo(3);
        assertThat(entry.getTriggeringPermanentPowerAtTrigger()).isEqualTo(3);
        assertThat(entry.triggeringPermanentCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(entry.getTriggeringCardId()).isEqualTo(card.getId());
        assertThat(entry.getDyingPermanentManaValue()).isEqualTo(card.getManaValue());
    }

    @Test
    @DisplayName("The ability Dance of Many gives its token is controlled by the enchantment's controller")
    void danceOfManyTokenAbilityIsTheEnchantments() {
        Card token = createCreature("Token Copy", 2, 2);
        Permanent tokenPermanent = new Permanent(token);
        UUID enchantmentId = UUID.randomUUID();
        tokenPermanent.setChosenPermanentId(enchantmentId);
        when(gameQueryService.findPermanentController(gd, enchantmentId)).thenReturn(PLAYER2_ID);
        var effect = new RemoveLinkedPermanentEffect(RemoveLinkedPermanentEffect.Mode.SACRIFICE_PARTNER);

        trigger(leaves(tokenPermanent, PLAYER1_ID), effect);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(PLAYER2_ID);
        assertThat(gd.stack.getFirst().getSourcePermanentSnapshot().getChosenPermanentId()).isEqualTo(enchantmentId);
    }

    /** A blocker's ability handed to the stack door, with the attacker it blocked as "that creature". */
    private TriggeredAbilityRequest blockRequest(TriggeredAbility ability, Card card, Permanent blocker,
                                                 Permanent attacker) {
        return new TriggeredAbilityRequest(ability, card, blocker, PLAYER1_ID, card.getName() + "'s block trigger",
                ConditionContext.forPermanent(blocker, PLAYER1_ID), entry -> {
                    entry.setTargetId(attacker.getId());
                    entry.setNonTargeting(true);
                    entry.setTriggeringPermanentId(attacker.getId());
                });
    }

    @Test
    @DisplayName("The door puts an ability that chooses nothing on the stack with its record, whatever its effects")
    void doorPutsAnAbilityThatChoosesNothingWithItsRecord() {
        Card card = createCreature("Blocker", 1, 1);
        Permanent blocker = new Permanent(card);
        Permanent attacker = new Permanent(createCreature("Attacker", 2, 2));
        var effect = new GainLifeEffect(1);

        boolean triggered = stackService.put(gd, blockRequest(new TriggeredAbility(Triggers.blocks(), effect),
                card, blocker, attacker));

        assertThat(triggered).isTrue();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
        assertThat(entry.isNonTargeting()).isTrue();
        assertThat(entry.getTriggeringPermanentId()).isEqualTo(attacker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(blocker.getId());
        assertThat(entry.isParkedAtStackDoor()).isFalse();
    }

    @Test
    @DisplayName("The door judges the intervening \"if\" as the ability triggers and wraps it for resolution")
    void doorJudgesAndWrapsTheInterveningIf() {
        Card card = createCreature("Blocker", 1, 1);
        Permanent blocker = new Permanent(card);
        Permanent attacker = new Permanent(createCreature("Attacker", 2, 2));
        var condition = new CastForAlternateCost();
        var ability = new TriggeredAbility(Triggers.blocks(), TriggerOptions.DEFAULT.withInterveningIf(condition),
                new GainLifeEffect(1));
        when(conditionEvaluationService.isMet(eq(gd), eq(condition), any(ConditionContext.class)))
                .thenReturn(false, true);

        assertThat(stackService.put(gd, blockRequest(ability, card, blocker, attacker))).isFalse();
        assertThat(gd.stack).isEmpty();

        assertThat(stackService.put(gd, blockRequest(ability, card, blocker, attacker))).isTrue();
        assertThat(gd.stack.getFirst().getEffectsToResolve())
                .containsExactly(new ConditionalEffect(condition, new GainLifeEffect(1)));
    }

    @Test
    @DisplayName("The door keeps an ability limited to once each turn from triggering again")
    void doorMarksTheLimit() {
        Card card = createCreature("Blocker", 1, 1);
        Permanent blocker = new Permanent(card);
        Permanent attacker = new Permanent(createCreature("Attacker", 2, 2));
        var ability = new TriggeredAbility(Triggers.blocks(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), new GainLifeEffect(1));

        assertThat(stackService.put(gd, blockRequest(ability, card, blocker, attacker))).isTrue();
        assertThat(stackService.put(gd, blockRequest(ability, card, blocker, attacker))).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("\"Once each turn for each creature\" is judged and marked by the creature the event names")
    void doorMarksTheLimitForEachCreature() {
        Card card = createCreature("Blocker", 1, 1);
        Permanent blocker = new Permanent(card);
        Permanent attacker = new Permanent(createCreature("Attacker", 2, 2));
        Permanent otherAttacker = new Permanent(createCreature("Other attacker", 2, 2));
        var ability = new TriggeredAbility(Triggers.blocks(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE),
                new GainLifeEffect(1));

        assertThat(stackService.put(gd, blockRequest(ability, card, blocker, attacker)
                .withLimitCreature(attacker.getId()))).isTrue();
        assertThat(stackService.put(gd, blockRequest(ability, card, blocker, attacker)
                .withLimitCreature(attacker.getId()))).isFalse();
        assertThat(stackService.put(gd, blockRequest(ability, card, blocker, otherAttacker)
                .withLimitCreature(otherAttacker.getId()))).isTrue();
        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Each additional time the ability triggers is another stack object")
    void doorAddsAStackObjectForEachAdditionalTrigger() {
        Card card = createCreature("Blocker", 1, 1);
        Permanent blocker = new Permanent(card);
        Permanent attacker = new Permanent(createCreature("Attacker", 2, 2));
        when(gameQueryService.countAdditionalTriggeredAbilityTriggers(gd, PLAYER1_ID, blocker, false, null))
                .thenReturn(1);

        stackService.put(gd, blockRequest(new TriggeredAbility(Triggers.blocks(), new GainLifeEffect(1)),
                card, blocker, attacker));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.get(0)).isNotSameAs(gd.stack.get(1));
    }

    @Test
    @DisplayName("An effect whose target the card doesn't declare acts on the creature the event names, without a choice")
    void undeclaredTargetIsTheObjectTheEventNames() {
        Card card = createCreature("Blocker", 1, 1);
        Permanent blocker = new Permanent(card);
        Permanent attacker = new Permanent(createCreature("Attacker", 2, 2));
        var effect = new DealDamageToTargetCreatureEffect(1);

        stackService.put(gd, blockRequest(new TriggeredAbility(Triggers.blocks(), effect), card, blocker, attacker));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(attacker.getId());
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("A target the card declares is chosen as the ability goes on the stack, with the record parked")
    void declaredTargetParksTheAbilityForItsChoice() {
        Card card = createCreature("Javelineer", 2, 2);
        var effect = new DealDamageToTargetCreatureEffect(1);
        card.target(1, 1).addTrigger(Triggers.blocks(), effect);
        Permanent blocker = new Permanent(card);
        Permanent attacker = new Permanent(createCreature("Attacker", 2, 2));

        stackService.put(gd, blockRequest(card.getTriggeredAbilities().getFirst(), card, blocker, attacker));

        assertThat(gd.stack).isEmpty();
        var choice = gd.peekPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class);
        assertThat(choice.effects()).containsExactly(effect);
        assertThat(choice.triggerRecord().isParkedAtStackDoor()).isTrue();
        assertThat(choice.triggerRecord().getTriggeringPermanentId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("A mode chosen as the ability triggers parks it for the mode choice")
    void modalAbilityParksForItsMode() {
        Card card = createCreature("Blocker", 1, 1);
        Permanent blocker = new Permanent(card);
        Permanent attacker = new Permanent(createCreature("Attacker", 2, 2));
        var choice = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw", new DrawCardEffect(1)),
                new ChooseOneEffect.ChooseOneOption("Gain life", new GainLifeEffect(1))));

        stackService.put(gd, blockRequest(new TriggeredAbility(Triggers.blocks(),
                new ChooseOneAtTriggerTimeEffect(choice)), card, blocker, attacker));

        assertThat(gd.stack).isEmpty();
        var modal = gd.peekPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class);
        assertThat(modal.effect()).isEqualTo(choice);
        assertThat(modal.triggerRecord().isParkedAtStackDoor()).isTrue();
    }

    @Test
    @DisplayName("The continuation puts a new stack object of the parked record and the choice on the stack")
    void finishParkedPutsTheRecordWithTheChoice() {
        Card card = createCreature("Javelineer", 2, 2);
        StackEntry record = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, PLAYER1_ID, "record",
                List.of(new GainLifeEffect(1)), null, UUID.randomUUID());
        UUID triggeringId = UUID.randomUUID();
        record.setTriggeringPermanentId(triggeringId);
        UUID targetId = UUID.randomUUID();

        assertThatThrownBy(() -> stackService.finishParked(gd, record, entry -> {
        })).isInstanceOf(IllegalStateException.class);
        record.setParkedAtStackDoor(true);
        StackEntry finished = stackService.finishParked(gd, record, entry -> entry.setTargetId(targetId));

        assertThat(finished).isNotSameAs(record);
        assertThat(gd.stack).containsExactly(finished);
        assertThat(finished.isParkedAtStackDoor()).isFalse();
        assertThat(finished.getTargetId()).isEqualTo(targetId);
        assertThat(finished.getTriggeringPermanentId()).isEqualTo(triggeringId);
        assertThat(finished.getTargetableId()).isNotEqualTo(record.getTargetableId());
    }

    @Test
    @DisplayName("A graveyard target is chosen as the ability goes on the stack, with the record parked")
    void graveyardTargetParksTheAbilityWithItsRecord() {
        Card card = createCreature("Blocked", 2, 2);
        Permanent attacker = new Permanent(card);
        Permanent blocker = new Permanent(createCreature("Blocker", 1, 1));
        var effect = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .targetGraveyard(true)
                .build();

        stackService.put(gd, blockRequest(new TriggeredAbility(Triggers.becomesBlocked(), effect), card, attacker,
                blocker));

        assertThat(gd.stack).isEmpty();
        var choice = gd.peekPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class);
        assertThat(choice.effects()).containsExactly(effect);
        assertThat(choice.triggerRecord().isParkedAtStackDoor()).isTrue();
        assertThat(choice.triggerRecord().getTriggeringPermanentId()).isEqualTo(blocker.getId());
    }

    @Test
    @DisplayName("A parked ability that targets graveyard cards checks the abilities its targets trigger")
    void finishParkedWithGraveyardTargetsChecksTargetTriggers() {
        Card card = createCreature("Javelineer", 2, 2);
        StackEntry record = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, PLAYER1_ID, "record",
                List.of(new GainLifeEffect(1)), null, UUID.randomUUID());
        record.setParkedAtStackDoor(true);
        UUID graveyardCardId = UUID.randomUUID();

        StackEntry finished = stackService.finishParked(gd, record,
                entry -> entry.setTargetCardIds(List.of(graveyardCardId)));

        verify(triggerCollectionService).checkBecomesTargetOfAbilityTriggers(gd);
        verify(triggerCollectionService).checkCrimeTriggers(gd, finished);
    }

    /** A combat damage record naming the damaged player PLAYER2 as "that player", as the combat collector stamps it. */
    private TriggeredAbilityRequest damageRequest(TriggeredAbility ability, Card card, Permanent creature) {
        return new TriggeredAbilityRequest(ability, card, creature, PLAYER1_ID, card.getName() + "'s ability",
                ConditionContext.forPermanent(creature, PLAYER1_ID), entry -> {
                    entry.setTargetId(PLAYER2_ID);
                    entry.setNonTargeting(true);
                    entry.setTriggeringPlayerId(PLAYER2_ID);
                    entry.setEventValue(3);
                });
    }

    @Test
    @DisplayName("A player-only effect acts on the player the event names, without a choice")
    void playerOnlyEffectIsThePlayerTheEventNames() {
        Card card = createCreature("Raider", 2, 2);
        Permanent creature = new Permanent(card);

        stackService.put(gd, damageRequest(new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)), card, creature));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(PLAYER2_ID);
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("An \"any target\" effect chooses its target although the event names a player")
    void anyTargetEffectChoosesItsTargetBesideThePlayerTheEventNames() {
        Card card = createCreature("Raider", 2, 2);
        Permanent creature = new Permanent(card);

        stackService.put(gd, damageRequest(new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                new DealDamageToAnyTargetEffect(1)), card, creature));

        assertThat(gd.stack).isEmpty();
        var choice = gd.peekPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class);
        assertThat(choice.choosingPlayerId()).isEqualTo(PLAYER1_ID);
        assertThat(choice.triggeringPlayerId()).isEqualTo(PLAYER2_ID);
        assertThat(choice.eventValue()).isEqualTo(3);
        assertThat(choice.triggerRecord().isParkedAtStackDoor()).isTrue();
    }

    @Test
    @DisplayName("The defending player chooses the target of a card whose combat ability says so")
    void defendingPlayerChoosesTheTargetWhenTheCardSaysSo() {
        Card card = createCreature("Erithizon", 4, 4);
        card.setCombatTriggerTargetChosenByDefendingPlayer(true);
        Permanent creature = new Permanent(card);
        var ability = new TriggeredAbility(Triggers.attacks(), new PutCounterOnTargetPermanentEffect(
                com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE));

        stackService.put(gd, new TriggeredAbilityRequest(ability, card, creature, PLAYER1_ID, "attack",
                ConditionContext.forPermanent(creature, PLAYER1_ID), entry -> {
                    entry.setAttackedTargetId(PLAYER2_ID);
                    entry.setDefendingPlayerId(PLAYER2_ID);
                }).causedBySourceAttacking());

        var choice = gd.peekPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class);
        assertThat(choice.choosingPlayerId()).isEqualTo(PLAYER2_ID);
        assertThat(choice.attackedTargetId()).isEqualTo(PLAYER2_ID);
    }

    @Test
    @DisplayName("Targets in two groups are chosen group by group; one group of two targets in one choice")
    void groupsDecideHowTargetsAreChosen() {
        Card twoGroups = createCreature("Two groups", 2, 2);
        CardEffect destroy = new DealDamageToTargetCreatureEffect(1);
        CardEffect boost = new PutCounterOnTargetPermanentEffect(
                com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE);
        twoGroups.target(1, 1).bindEffect(destroy);
        twoGroups.target(1, 1).bindEffect(boost);
        stackService.put(gd, new TriggeredAbilityRequest(new TriggeredAbility(Triggers.attacks(), destroy, boost),
                twoGroups, new Permanent(twoGroups), PLAYER1_ID, "attack",
                ConditionContext.forCard(twoGroups, PLAYER1_ID), TriggeredAbilityRequest.TriggerRecord.NONE));

        Card oneGroup = createCreature("One group", 2, 2);
        CardEffect damage = new DealDamageToTargetCreatureEffect(1);
        oneGroup.target(2, 2).bindEffect(damage);
        stackService.put(gd, new TriggeredAbilityRequest(new TriggeredAbility(Triggers.attacks(), damage),
                oneGroup, new Permanent(oneGroup), PLAYER1_ID, "attack",
                ConditionContext.forCard(oneGroup, PLAYER1_ID), TriggeredAbilityRequest.TriggerRecord.NONE));

        assertThat(gd.peekPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class).sourceCard())
                .isSameAs(twoGroups);
        assertThat(gd.peekPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class).sourceCard())
                .isSameAs(oneGroup);
    }

    @Test
    @DisplayName("An opponent's graveyard of an ability about one player is that player's")
    void opponentGraveyardOfAnAbilityAboutOnePlayerIsThatPlayers() {
        Card card = createCreature("Raider", 2, 2);
        var effect = new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD, null,
                false, false, false, null, false, false);

        stackService.put(gd, damageRequest(new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(), effect), card,
                new Permanent(card)));
        stackService.put(gd, new TriggeredAbilityRequest(new TriggeredAbility(Triggers.attacks(), effect), card,
                new Permanent(card), PLAYER1_ID, "attack", ConditionContext.forCard(card, PLAYER1_ID),
                entry -> entry.setDefendingPlayerId(PLAYER2_ID)));

        var choices = gd.pendingInteractions.stream()
                .filter(PermanentChoiceContext.SpellGraveyardTargetTrigger.class::isInstance)
                .map(PermanentChoiceContext.SpellGraveyardTargetTrigger.class::cast).toList();
        assertThat(choices).extracting(PermanentChoiceContext.SpellGraveyardTargetTrigger::graveyardOwnerId)
                .containsExactly(PLAYER2_ID, null);
    }

    @Test
    @DisplayName("Graveyard cards up to the event's amount are chosen from the graveyard of the player it names")
    void graveyardChoiceReadsTheEventsAmountAndPlayer() {
        Card card = createCreature("Froghemoth", 4, 4);

        stackService.put(gd, damageRequest(new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                new ExileUpToCombatDamageCardsFromGraveyardEffect()), card, new Permanent(card)));

        var choice = gd.peekPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class);
        assertThat(choice.graveyardOwnerId()).isEqualTo(PLAYER2_ID);
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.triggerRecord().isParkedAtStackDoor()).isTrue();
    }

    @Test
    @DisplayName("The doublers judge an attack's ability as attack-caused, with the event they read")
    void doublersReadTheAttackAndTheEvent() {
        Card card = createCreature("Raider", 2, 2);
        Permanent creature = new Permanent(card);
        var event = new com.github.laxika.magicalvibes.service.trigger.TriggerContext.SourceDealsCombatDamage(
                card, PLAYER1_ID, creature.getId(), 3, 3);

        stackService.put(gd, damageRequest(new TriggeredAbility(Triggers.attacks(), new GainLifeEffect(1)), card,
                creature).causedByAttack().withEvent(event));

        verify(gameQueryService).countAdditionalTriggeredAbilityTriggers(gd, PLAYER1_ID, creature, true, event);
    }

    @Test
    @DisplayName("The entry names the permanent that granted the ability")
    void entryNamesTheGrantingPermanent() {
        Card card = createCreature("Equipped", 2, 2);
        UUID grantingId = UUID.randomUUID();
        var ability = new TriggeredAbility(Triggers.attacks(), new GainLifeEffect(1))
                .withGrantingPermanentId(grantingId);

        stackService.put(gd, new TriggeredAbilityRequest(ability, card, new Permanent(card), PLAYER1_ID, "attack",
                ConditionContext.forCard(card, PLAYER1_ID), TriggeredAbilityRequest.TriggerRecord.NONE));

        assertThat(gd.stack.getFirst().getGrantingPermanentId()).isEqualTo(grantingId);
    }

    @Test
    @DisplayName("An ability its attacking source's attack caused is reported as it reaches the stack")
    void abilityOfTheAttackingSourceIsReported() {
        Card card = createCreature("Attacker", 2, 2);
        Permanent attacker = new Permanent(card);
        attacker.setAttacking(true);
        when(gameQueryService.findPermanentById(gd, attacker.getId())).thenReturn(attacker);

        stackService.put(gd, new TriggeredAbilityRequest(new TriggeredAbility(Triggers.attacks(),
                new GainLifeEffect(1)), card, attacker, PLAYER1_ID, "attack",
                ConditionContext.forPermanent(attacker, PLAYER1_ID), TriggeredAbilityRequest.TriggerRecord.NONE)
                .causedBySourceAttacking());

        assertThat(gd.stack.getFirst().isTriggeredBySourceAttacking()).isTrue();
        verify(triggerCollectionService).checkAttackingCreatureTriggeredAbilityTriggers(gd, attacker,
                gd.stack.getFirst());
    }

    @Test
    @DisplayName("An effect that acts only on a spell is aimed at the spell the event was about, without targeting it")
    void spellOnlyEffectIsAimedAtTheTriggeringSpell() {
        Card card = createCreature("Warded", 2, 2);
        Permanent warded = new Permanent(card);
        StackEntry spell = new StackEntry(StackEntryType.INSTANT_SPELL, createCreature("Bolt", 0, 0), PLAYER2_ID,
                "Bolt", List.of());

        stackService.put(gd, new TriggeredAbilityRequest(new TriggeredAbility(
                Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2)), card, warded,
                PLAYER1_ID, "ward", ConditionContext.forPermanent(warded, PLAYER1_ID), entry -> {
                    entry.setTargetId(PLAYER2_ID);
                    entry.setNonTargeting(true);
                    entry.setTriggeringSpell(spell);
                }));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(spell.getTargetableId());
        assertThat(entry.getTargetZone()).isEqualTo(Zone.STACK);
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("A permanent the record names keeps the entry aimed at it beside an effect that acts on a spell")
    void namedPermanentIsNotReplacedByTheTriggeringSpell() {
        Card card = createCreature("Watcher", 2, 2);
        Permanent watcher = new Permanent(card);
        Permanent named = new Permanent(createCreature("Named", 1, 1));
        StackEntry spell = new StackEntry(StackEntryType.INSTANT_SPELL, createCreature("Bolt", 0, 0), PLAYER2_ID,
                "Bolt", List.of());

        stackService.put(gd, new TriggeredAbilityRequest(new TriggeredAbility(
                Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2)), card, watcher,
                PLAYER1_ID, "watch", ConditionContext.forPermanent(watcher, PLAYER1_ID), entry -> {
                    entry.setTargetId(named.getId());
                    entry.setNonTargeting(true);
                    entry.setTriggeringSpell(spell);
                }));

        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(named.getId());
    }

    @Test
    @DisplayName("An intervening \"if\" that is a fact of the event is judged as the ability triggers, not again")
    void eventFactInterveningIfIsJudgedOnce() {
        Card card = createCreature("Negotiator", 3, 3);
        Permanent source = new Permanent(card);
        var condition = new TreasureManaSpentToActivate();
        when(conditionEvaluationService.isMet(eq(gd), eq(condition), any(ConditionContext.class))).thenReturn(true);

        stackService.put(gd, new TriggeredAbilityRequest(new TriggeredAbility(Triggers.opponentActivatesAbility(),
                TriggerOptions.DEFAULT.withInterveningIf(condition), new GainLifeEffect(1)), card, source,
                PLAYER1_ID, "activation", ConditionContext.forPermanent(source, PLAYER1_ID),
                TriggeredAbilityRequest.TriggerRecord.NONE));

        assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(new GainLifeEffect(1));
    }

    @Test
    @DisplayName("\"Choose up to X\" reads X as the ability goes on the stack; with none to choose it is removed")
    void chooseUpToXReadsXAsItGoesOnTheStack() {
        Card card = createCreature("Riku", 2, 2);
        Permanent source = new Permanent(card);
        var modes = ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw", new DrawCardEffect(1)),
                new ChooseOneEffect.ChooseOneOption("Gain life", new GainLifeEffect(1))));
        var maximum = new Fixed(1);
        var ability = new TriggeredAbility(Triggers.youCastSpell(), new ChooseOneAtTriggerTimeEffect(modes, maximum));
        when(amountEvaluationService.evaluate(eq(gd), eq(maximum), any())).thenReturn(0, 1);

        stackService.put(gd, new TriggeredAbilityRequest(ability, card, source, PLAYER1_ID, "cast",
                ConditionContext.forPermanent(source, PLAYER1_ID), TriggeredAbilityRequest.TriggerRecord.NONE));
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class)).isFalse();
        assertThat(gd.stack).isEmpty();

        stackService.put(gd, new TriggeredAbilityRequest(ability, card, source, PLAYER1_ID, "cast",
                ConditionContext.forPermanent(source, PLAYER1_ID), TriggeredAbilityRequest.TriggerRecord.NONE));
        var modeChoice = gd.peekPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class);
        assertThat(modeChoice.modeCount()).isEqualTo(
                new PermanentChoiceContext.TriggeredModalTrigger.ModeCount(0, 1));
    }

    @Test
    @DisplayName("A \"you may pay\" beside another effect has the target of what it offers chosen as it goes on the stack")
    void youMayPayBesideAnotherEffectChoosesItsTarget() {
        Card card = createCreature("Talisman", 0, 0);
        Permanent source = new Permanent(card);
        var pay = new MayPayManaEffect("{1}", new DealDamageToTargetCreatureEffect(1), "Pay {1}?");

        stackService.put(gd, new TriggeredAbilityRequest(new TriggeredAbility(Triggers.youCastSpell(), pay,
                new GainLifeEffect(1)), card, source, PLAYER1_ID, "cast",
                ConditionContext.forPermanent(source, PLAYER1_ID), TriggeredAbilityRequest.TriggerRecord.NONE));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.peekPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class).effects())
                .containsExactly(pay, new GainLifeEffect(1));
    }

    @Test
    @DisplayName("An ability with a target filter of its own chooses its target beside the player the event names")
    void abilityTargetFilterDeclaresItsTarget() {
        Card card = createCreature("Emblem", 0, 0);
        Permanent source = new Permanent(card);
        var ability = new TriggeredAbility(Triggers.youCastSpell(),
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER))
                .withTargetFilter(new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent"));

        stackService.put(gd, new TriggeredAbilityRequest(ability, card, source, PLAYER1_ID, "cast",
                ConditionContext.forPermanent(source, PLAYER1_ID), entry -> {
                    entry.setTargetId(PLAYER1_ID);
                    entry.setNonTargeting(true);
                }));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isTrue();
    }

    @Test
    @DisplayName("The doublers judge a becomes-the-target ability with its event")
    void doublersReadTheTargetEvent() {
        Card card = createCreature("Watcher", 2, 2);
        Permanent watcher = new Permanent(card);
        var event = new com.github.laxika.magicalvibes.service.trigger.TriggerContext.BecomesTarget(watcher,
                PLAYER1_ID);

        stackService.put(gd, new TriggeredAbilityRequest(new TriggeredAbility(
                Triggers.becomesTargetOfSpellOrAbility(), new GainLifeEffect(1)), card, watcher, PLAYER1_ID,
                "watch", ConditionContext.forPermanent(watcher, PLAYER1_ID),
                TriggeredAbilityRequest.TriggerRecord.NONE).withEvent(event));

        verify(gameQueryService).countAdditionalTriggeredAbilityTriggers(gd, PLAYER1_ID, watcher, false, event);
    }
}
