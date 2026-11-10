package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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
                graveyardTargetingSupport, gameLogService, triggerCollectionService, amountEvaluationService);
        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
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
}
