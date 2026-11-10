package com.github.laxika.magicalvibes.service.trigger;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentPower;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentToughness;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEqualToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTargetForEachLeavingSourceCountersEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringLandFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToOpponentCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

@ExtendWith(MockitoExtension.class)
class DeathTriggerCollectorServiceTest {

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private PredicateEvaluationService predicateEvaluationService;

    @Mock
    private GameLogService gameLogService;
    @Mock
    private AmountEvaluationService amountEvaluationService;

    @Mock
    private com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport graveyardTargetingSupport;

    @InjectMocks
    private DeathTriggerCollectorService svc;

    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Player1");
        gd.playerIdToName.put(PLAYER2_ID, "Player2");
        gd.playerBattlefields.put(PLAYER1_ID, new ArrayList<>());
        gd.playerBattlefields.put(PLAYER2_ID, new ArrayList<>());
        gd.playerGraveyards.put(PLAYER1_ID, new ArrayList<>());
        gd.playerGraveyards.put(PLAYER2_ID, new ArrayList<>());
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

    private Card createArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
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

    private TriggerMatchContext match(Permanent perm, UUID controllerId, CardEffect rawEffect) {
        return new TriggerMatchContext(gd, perm, controllerId, rawEffect);
    }

    @Test
    @DisplayName("Puts an opponent-permanent ability's effect on the stack once the pipeline matched it")
    void putsOpponentPermanentAbilityOnStack() {
        Card watcherCard = createCreature("Watcher", 1, 3);
        Permanent watcher = new Permanent(watcherCard);
        Card dyingCard = createArtifact("Bountied Artifact");
        Permanent dyingPermanent = new Permanent(dyingCard);
        var effect = SequenceEffect.of(new GainLifeEffect(3), new DrawCardEffect());
        var context = new TriggerContext.OpponentPermanentGraveyard(
                dyingCard, PLAYER2_ID, PLAYER2_ID, dyingPermanent);

        assertThat(svc.handleOpponentPermanentGraveyardDefault(
                match(watcher, PLAYER1_ID, effect), effect, context)).isTrue();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
    }

    @Nested
    @DisplayName("handleEquippedCreatureDeathDefault")
    class EquippedCreatureDeathDefault {

        @Test
        @DisplayName("Puts the effect on the stack as written with the dying power recorded")
        void recordsDyingPowerForPaymentAndDraw() {
            Card equipment = createEquipment("Mask");
            var effect = new MayPayLifeEqualToAmountEffect(new TriggeringPermanentPower(),
                    new DrawCardEffect(new TriggeringPermanentPower()), "Pay?");
            Permanent perm = new Permanent(equipment);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, null, 4);

            svc.handleEquippedCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve()).containsExactly(effect);
            assertThat(gd.stack.get(0).getTriggeringPermanentPowerAtTrigger()).isEqualTo(4);
        }

        @Test
        @DisplayName("Non-targeting effect adds to stack")
        void nonTargetingAddsToStack() {
            Card equipment = createEquipment("Death Sword");
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(equipment);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, null);

            svc.handleEquippedCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getCard()).isEqualTo(equipment);
        }

        @Test
        @DisplayName("Targeting effect queues DeathTriggerTarget")
        void targetingQueuesDeathTriggerTarget() {
            Card equipment = createEquipment("Target Sword");
            var effect = new PutCounterOnTargetPermanentEffect(CounterType.CHARGE);
            Permanent perm = new Permanent(equipment);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, null);

            svc.handleEquippedCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card equipment = createEquipment("Trigger Blade");
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(equipment);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, null);

            svc.handleEquippedCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("Trigger Blade") && logEntry.plainText().contains("equipped creature died")));
        }
    }

    @Nested
    @DisplayName("handleEquippedCreatureDeathDefault: a return of the creature that died")
    class EquippedCreatureReturn {

        @Test
        @DisplayName("Gives the entry the dying card and the source Equipment as its source permanent")
        void bindsDyingCardAndSource() {
            Card equipment = createEquipment("Oathkeeper");
            Card dying = createCreature("Samurai", 3, 3);
            Permanent perm = new Permanent(equipment);
            var effect = new ReturnDyingCreatureToBattlefieldEffect(false);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, dying);

            svc.handleEquippedCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(gd.stack.get(0).getTriggeringCardId()).isEqualTo(dying.getId());
            assertThat(gd.stack.get(0).getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("Triggers for a token, whose return finds no card")
        void triggersWithoutDyingCard() {
            Card equipment = createEquipment("Oathkeeper");
            Permanent perm = new Permanent(equipment);
            var effect = new ReturnDyingCreatureToBattlefieldEffect(false);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, null);

            assertThat(svc.handleEquippedCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx)).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getTriggeringCardId()).isNull();
        }
    }

    @Nested
    @DisplayName("handleEquippedCreatureDeathDefault: a delayed return of the creature that died")
    class EquippedCreatureDelayedReturn {

        @Test
        @DisplayName("Gives the delayed return the dying card, the Equipment as its source and the death recorded")
        void bindsDyingCard() {
            Card equipment = createEquipment("Resurrection Orb");
            Card dying = createCreature("Dying Creature", 2, 2);
            Permanent dyingPermanent = new Permanent(dying);
            Permanent perm = new Permanent(equipment);
            var effect = new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                    false, null, 0, null, null, false, true);
            var ctx = new TriggerContext.EquippedCreatureDeath(dyingPermanent.getId(), PLAYER1_ID, dying, 2,
                    dyingPermanent);

            assertThat(svc.dispatchCreatureDeath(CreatureDeathWatch.EQUIPPED, match(perm, PLAYER1_ID, effect),
                    effect, ctx)).isTrue();

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(entry.getTriggeringCardId()).isEqualTo(dying.getId());
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(dyingPermanent.getId());
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(PLAYER1_ID);
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("Triggers for a token, which leaves no card to return (CR 704.5d)")
        void triggersForAToken() {
            Card equipment = createEquipment("Resurrection Orb");
            Permanent perm = new Permanent(equipment);
            var effect = new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                    false, null, 0, null, null, false, true);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, null);

            assertThat(svc.dispatchCreatureDeath(CreatureDeathWatch.EQUIPPED, match(perm, PLAYER1_ID, effect),
                    effect, ctx)).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTriggeringCardId()).isNull();
        }

        @Test
        @DisplayName("A target choice carries the dying creature's power, controller and last known information")
        void targetChoiceCarriesTheDeath() {
            Card equipment = createEquipment("Target Sword");
            Card dying = createCreature("Dying Creature", 3, 3);
            Permanent dyingPermanent = new Permanent(dying);
            Permanent perm = new Permanent(equipment);
            var effect = new PutCounterOnTargetPermanentEffect(CounterType.CHARGE);
            var ctx = new TriggerContext.EquippedCreatureDeath(dyingPermanent.getId(), PLAYER2_ID, dying, 3,
                    dyingPermanent);

            svc.dispatchCreatureDeath(CreatureDeathWatch.EQUIPPED, match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.pendingInteractions).singleElement()
                    .isInstanceOfSatisfying(PermanentChoiceContext.DeathTriggerTarget.class, target -> {
                        assertThat(target.eventValue()).isEqualTo(3);
                        assertThat(target.triggeringCardId()).isEqualTo(dying.getId());
                        assertThat(target.triggeringPermanent().getId()).isEqualTo(dyingPermanent.getId());
                        assertThat(target.triggeringPermanentControllerId()).isEqualTo(PLAYER2_ID);
                        assertThat(target.sourcePermanentSnapshot().getId()).isEqualTo(perm.getId());
                    });
        }
    }

    @Nested
    @DisplayName("enchanted-death returns")
    class EnchantedDeathReturns {

        @Test
        @DisplayName("Necrotic Plague's target choice carries the dying creature's controller")
        void targetChoiceCarriesDyingController() {
            Card aura = createEnchantment("Necrotic Plague");
            var effect = new ReturnSourceAuraToOpponentCreatureOnDeathEffect();
            Permanent perm = new Permanent(aura);
            Card creature = createCreature("Grizzly Bears", 2, 2);
            Permanent dying = new Permanent(creature);
            var ctx = new TriggerContext.EnchantedPermanentDeath(dying.getId(), PLAYER1_ID, creature.getId(), 2, 2,
                    0, true, List.of(creature.getId()), PLAYER1_ID, creature, dying);

            svc.handleEnchantedPermanentDeathDefault(match(perm, PLAYER2_ID, effect), effect, ctx);

            var choice = gd.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);
            assertThat(choice.effects()).containsExactly(effect);
            assertThat(choice.triggeringPermanentControllerId()).isEqualTo(PLAYER1_ID);
        }

        @Test
        @DisplayName("A return goes on the stack as written with the dying card as its triggering card")
        void returnRecordsDyingCard() {
            Card aura = createEnchantment("Demonic Vigor");
            var effect = new ReturnEnchantedCreatureToOwnerHandOnDeathEffect();
            Permanent perm = new Permanent(aura);
            Card creature = createCreature("Grizzly Bears", 2, 2);
            Permanent dying = new Permanent(creature);
            var ctx = new TriggerContext.EnchantedPermanentDeath(dying.getId(), PLAYER1_ID, creature.getId(), 2, 2,
                    0, true, List.of(creature.getId()), PLAYER1_ID, creature, dying);

            svc.handleEnchantedPermanentDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.triggeringCardIds()).containsExactly(creature.getId());
            assertThat(entry.getEventCardGraveyardEntryVersions()).containsKey(creature.getId());
        }
    }

    @Nested
    @DisplayName("handleEnchantedPermanentDeathDefault")
    class EnchantedPermanentDeathDefault {

        @Test
        @DisplayName("Adds to stack and logs")
        void addsToStackAndLogs() {
            Card aura = createEnchantment("Death Aura");
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), null, null, 0, 0);

            svc.handleEnchantedPermanentDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getCard()).isEqualTo(aura);
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("enchanted permanent put into graveyard")));
        }

        @Test
        @DisplayName("Records the dying creature's power as the event value a dynamic look count reads")
        void recordsDyingPowerAsEventValueForDynamicLookCount() {
            Card aura = createEnchantment("Necrosynthesis");
            var effect = new LookAtTopCardsEffect(
                    new EventValue(), new Fixed(1), null,
                    LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false);
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), null, null, 3, 3);

            svc.handleEnchantedPermanentDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("handleEnchantedPermanentDeathDefault records the dying creature")
    class EnchantedPermanentDeathRecordsTriggeringPermanent {

        @Test
        @DisplayName("Puts the effect on the stack as written, the creature's controller, power and toughness on the entry")
        void recordsTriggeringPermanentWithoutRebuilding() {
            Card aura = createEnchantment("Banewasp Affliction");
            var effect = new LoseLifeEffect(new TriggeringPermanentToughness(),
                    LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER);
            Permanent perm = new Permanent(aura);
            UUID dyingId = UUID.randomUUID();
            var ctx = new TriggerContext.EnchantedPermanentDeath(dyingId, PLAYER2_ID, null, 2, 4);

            svc.handleEnchantedPermanentDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            var entry = gd.stack.get(0);
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(dyingId);
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(PLAYER2_ID);
            assertThat(entry.getTriggeringPermanentPowerAtTrigger()).isEqualTo(2);
            assertThat(entry.getTriggeringPermanentToughnessAtTrigger()).isEqualTo(4);
            assertThat(entry.getEventValue()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("handleEnchantedCreatureControllerDrawsCard")
    class EnchantedCreatureControllerDrawsCard {

        @Test
        @DisplayName("Bakes the dying creature's controller as the draw target")
        void bakesControllerId() {
            Card aura = createEnchantment("Fate Foretold");
            var effect = new DrawCardForTargetPlayerEffect(1);
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER2_ID, null, 0, 0);

            svc.handleEnchantedCreatureControllerDrawsCard(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getTargetId()).isEqualTo(PLAYER2_ID);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }
    }

    @Nested
    @DisplayName("handleEnchantedPermanentLeavesDefault")
    class EnchantedPermanentLeavesDefault {

        @Test
        @DisplayName("Non-conditional effect fires unconditionally")
        void firesUnconditionally() {
            Card aura = createEnchantment("LTB Aura");
            var effect = new DrawCardEffect(1);
            Permanent auraPerm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentLeaves(new Permanent(createCreature("Leaving", 2, 2)), PLAYER1_ID);

            svc.handleEnchantedPermanentLeavesDefault(match(auraPerm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getTriggeringPermanentId()).isEqualTo(ctx.leavingPermanent().getId());
            assertThat(gd.stack.get(0).getTriggeringPermanentControllerId()).isEqualTo(PLAYER1_ID);
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("enchanted permanent left the battlefield")));
        }
    }

    @Nested
    @DisplayName("Any permanent graveyard handlers")
    class AnyPermanentGraveyardHandlers {

        @Test
        @DisplayName("Default effect queues a trigger for any permanent, including tokens")
        void defaultQueuesTrigger() {
            Card watcher = createEnchantment("Last Laugh");
            Permanent perm = new Permanent(watcher);
            var effect = new MassDamageEffect(1, true);
            var dying = createArtifact("Treasure");
            dying.setToken(true);
            var ctx = new TriggerContext.AnyPermanentGraveyard(dying, PLAYER2_ID, PLAYER2_ID);

            assertThat(svc.handleAnyPermanentGraveyardDefault(
                    match(perm, PLAYER1_ID, effect), effect, ctx)).isTrue();

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve()).containsExactly(effect);
            assertThat(gd.stack.get(0).getTargetId()).isEqualTo(PLAYER2_ID);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }
    }

    @Nested
    @DisplayName("Artifact graveyard handlers")
    class ArtifactGraveyardHandlers {

        @Test
        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card watcher = createArtifact("Optional Watcher");
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER1_ID, PLAYER1_ID);

            svc.handleArtifactGraveyardMay(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayEffect.class);
        }

        @Test
        @DisplayName("DealDamage effect sets target to artifact controller")
        void dealDamageSetsTarget() {
            Card watcher = createArtifact("Damage Watcher");
            var effect = new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER2_ID, PLAYER2_ID);

            svc.handleArtifactGraveyardDamageController(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getTargetId()).isEqualTo(PLAYER2_ID);
        }

        @Test
        @DisplayName("Default effect has null targetId and sets sourcePermanentId")
        void defaultSetsSourcePermanentId() {
            Card watcher = createArtifact("Plain Watcher");
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER1_ID, PLAYER1_ID);

            svc.handleArtifactGraveyardDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getTargetId()).isNull();
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("Default effect queues a player-targeted trigger with source snapshot")
        void artifactDefaultQueuesTargetedTrigger() {
            Card watcher = createCreature("Marionette Master", 1, 3);
            Permanent perm = new Permanent(watcher);
            var effect = new LoseLifeEffect(new SourcePower(), LoseLifeRecipient.TARGET_PLAYER);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER1_ID, PLAYER1_ID);

            assertThat(svc.handleArtifactGraveyardDefault(
                    match(perm, PLAYER1_ID, effect), effect, ctx)).isTrue();

            var pending = (PermanentChoiceContext.SpellTargetTriggerAnyTarget)
                    gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(pending.playerTargetOnly()).isTrue();
            assertThat(pending.sourcePermanentId()).isEqualTo(perm.getId());
            assertThat(pending.sourcePermanentSnapshot()).isNotNull();
            assertThat(pending.effects()).containsExactly(effect);
        }

        @Test
        @DisplayName("Default effect queues a non-targeted trigger without target choice")
        void artifactDefaultQueuesNonTargetedTrigger() {
            Card watcher = createCreature("Syr Ginger, the Meal Ender", 3, 1);
            Permanent perm = new Permanent(watcher);
            var effect = SequenceEffect.of(
                    new PutCountersOnSourceEffect(1, 1, 1), new ScryEffect(1));
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER1_ID, PLAYER1_ID);

            assertThat(svc.handleArtifactGraveyardDefault(
                    match(perm, PLAYER1_ID, effect), effect, ctx)).isTrue();

            assertThat(gd.pendingInteractions).isEmpty();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetId()).isNull();
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("Default effect carries the artifact as it last existed as the triggering permanent")
        void artifactDefaultCarriesTheArtifactAsTriggeringPermanent() {
            Card watcher = createCreature("Donatello, Mutant Mechanic", 3, 5);
            Permanent perm = new Permanent(watcher);
            var effect = new PutCountersOnTargetForEachLeavingSourceCountersEffect(
                            new PermanentIsCreaturePredicate());
            Permanent artifact = new Permanent(createCreature("Ornithopter", 0, 2));
            artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
            artifact.setCounterCount(CounterType.CHARGE, 3);
            var ctx = new TriggerContext.ArtifactGraveyard(
                    PLAYER1_ID, PLAYER1_ID, artifact.getCard(), 0, artifact, false);

            assertThat(svc.handleArtifactGraveyardDefault(
                    match(perm, PLAYER1_ID, effect), effect, ctx)).isTrue();

            var pending = (PermanentChoiceContext.SpellTargetTriggerAnyTarget)
                    gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(pending.effects()).containsExactly(effect);
            assertThat(pending.departedPermanent().getCounters()).containsExactlyInAnyOrderEntriesOf(
                    Map.of(CounterType.PLUS_ONE_PLUS_ONE, 2, CounterType.CHARGE, 3));
        }
    }

    @Nested
    @DisplayName("Enchantment graveyard handlers")
    class EnchantmentGraveyardHandlers {

        @Test
        @DisplayName("Default effect has null targetId and sets sourcePermanentId")
        void defaultSetsSourcePermanentId() {
            Card watcher = createCreature("Femeref Enchantress", 1, 2);
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.EnchantmentGraveyard(PLAYER1_ID, PLAYER1_ID);

            svc.handleEnchantmentGraveyardDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getTargetId()).isNull();
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }
    }

    @Nested
    @DisplayName("Opponent artifact graveyard handlers")
    class OpponentArtifactGraveyardHandlers {

        @Test
        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card watcher = createArtifact("Opponent May Watcher");
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER2_ID, PLAYER2_ID);

            svc.handleOpponentArtifactGraveyardMay(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Default sets sourcePermanentId")
        void defaultSetsSourcePermanentId() {
            Card watcher = createArtifact("Opponent Tracker");
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER2_ID, PLAYER2_ID);

            svc.handleOpponentArtifactGraveyardDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }
    }

    @Nested
    @DisplayName("Any creature death handlers")
    class AnyCreatureDeathHandlers {

        @Test
        @DisplayName("PutCountersOnSource sets sourcePermanentId")
        void putCountersSetsSourcePermanentId() {
            Card watcher = createCreature("Growing Watcher", 1, 1);
            var effect = new PutCountersOnSourceEffect(1, 1, 1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.dispatchCreatureDeath(CreatureDeathWatch.ANY, match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("BoostSelfEffect sets sourcePermanentId")
        void boostSelfSetsSourcePermanentId() {
            Card watcher = createCreature("Gristle Grinner", 3, 3);
            var effect = new BoostSelfEffect(2, 2);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.dispatchCreatureDeath(CreatureDeathWatch.ANY, match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("TapPermanentsEffect sets sourcePermanentId")
        void tapSetsSourcePermanentId() {
            Card watcher = createCreature("Fleshmad Steed", 2, 2);
            var effect = new TapPermanentsEffect(TapUntapScope.SELF);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.dispatchCreatureDeath(CreatureDeathWatch.ANY, match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card watcher = createCreature("Optional Watcher", 1, 1);
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.dispatchCreatureDeath(CreatureDeathWatch.ANY, match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("MayEffect records the dying creature's power for an amount that reads it, not rebuilt")
        void mayEffectRecordsDyingPower() {
            Card watcher = createCreature("Kresh the Bloodbraided", 3, 3);
            var may = new MayEffect(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE,
                    new TriggeringPermanentPower()), "Put counters?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 4, 4), PLAYER1_ID, 4, 4);

            svc.dispatchCreatureDeath(CreatureDeathWatch.ANY, match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(gd.stack.get(0).getEffectsToResolve()).containsExactly(may);
            assertThat(gd.stack.get(0).getTriggeringPermanentPowerAtTrigger()).isEqualTo(4);
        }

        @Test
        @DisplayName("Targeting MayEffect queues a DeathTriggerTarget interaction (CR 603.3d)")
        void targetingMayQueuesDeathTriggerTarget() {
            Card watcher = createEnchantment("Vicious Shadows");
            var may = new MayEffect(
                    new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER), "Deal damage?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.dispatchCreatureDeath(CreatureDeathWatch.ANY, match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
        }

        @Test
        @DisplayName("Target player damage queues a death-trigger target choice")
        void targetPlayerDamageQueuesDeathTriggerTarget() {
            Card watcher = createCreature("Rage Thrower", 4, 2);
            var effect = new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER2_ID, 1, 1);

            svc.handleAnyCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
        }

        @Test
        @DisplayName("BecomeCopyOfDyingCreature's may-pay goes on the stack unchanged with the dying creature")
        void becomeCopyCarriesTheDyingCreature() {
            Card puca = createCreature("Cemetery Puca", 1, 2);
            Card dying = createCreature("Dead Creature", 2, 2);
            Permanent dyingPermanent = new Permanent(dying);
            var rawMayPay = new MayPayManaEffect("{1}", new BecomeCopyOfDyingCreatureEffect(), "Pay {1}?");
            Permanent perm = new Permanent(puca);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 2, 2, dyingPermanent.getId(),
                    dyingPermanent);

            assertThat(svc.dispatchCreatureDeath(CreatureDeathWatch.ANY, match(perm, PLAYER1_ID, rawMayPay),
                    rawMayPay, ctx)).isTrue();

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEffectsToResolve()).containsExactly(rawMayPay);
            assertThat(entry.getTriggeringCardId()).isEqualTo(dying.getId());
            assertThat(entry.getTriggeringPermanentSnapshot().getId()).isEqualTo(dyingPermanent.getId());
        }

        @Test
        @DisplayName("CreateTokenCopyOfDyingCreature gives the entry the dying permanent and its card")
        void createTokenCopyBindsDyingPermanent() {
            Card watcher = createCreature("Genestealer Patriarch", 4, 4);
            Card dying = createCreature("Dead Creature", 2, 2);
            Permanent perm = new Permanent(watcher);
            Permanent dyingPermanent = new Permanent(dying);
            var copyEffect = new CreateTokenCopyOfDyingCreatureEffect(
                    new CreateTokenCopyOfTargetPermanentEffect(
                            List.of(), java.util.Set.of(), null, null, Map.of()));
            var ctx = new TriggerContext.CreatureDeath(
                    dying, PLAYER2_ID, 2, 2, dyingPermanent.getId(), dyingPermanent);

            assertThat(svc.dispatchCreatureDeath(CreatureDeathWatch.ANY,
                    match(perm, PLAYER1_ID, copyEffect), copyEffect, ctx)).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(gd.stack.getFirst().getTriggeringPermanentId()).isEqualTo(dyingPermanent.getId());
            assertThat(gd.stack.getFirst().getTriggeringCardId()).isEqualTo(dying.getId());
            assertThat(gd.stack.getFirst().lastKnownPermanentCard(dyingPermanent.getId()))
                    .isEqualTo(dying);
        }

        @Test
        @DisplayName("Default non-targeting adds to stack with sourcePermanentId")
        void defaultNonTargetingAddsToStack() {
            Card watcher = createCreature("Death Counter", 1, 1);
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.handleAnyCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("Default records the source as it triggered and the dying creature's controller whatever the effect is")
        void defaultRecordsSourceSnapshotAndDyingController() {
            Card watcher = createCreature("Dingus Watcher", 1, 1);
            var effect = new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER2_ID, 1, 1);

            svc.dispatchCreatureDeath(CreatureDeathWatch.ANY, match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getTargetId()).isNull();
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(PLAYER2_ID);
            assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
            assertThat(entry.getSourcePermanentSnapshot().getId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("Default targeting queues DeathTriggerTarget")
        void defaultTargetingQueuesDeathTriggerTarget() {
            Card watcher = createCreature("Target Watcher", 1, 1);
            var effect = new PutCounterOnTargetPermanentEffect(CounterType.CHARGE);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.handleAnyCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Creature-death defaults record the death whatever the effects are")
    class CreatureDeathRecord {

        @Test
        @DisplayName("A \"you may\" goes on the stack with the creature as it last existed, its controller, power, toughness and mana value")
        void mayRecordsTheDeath() {
            Card watcher = createCreature("Kresh the Bloodbraided", 3, 3);
            Card dying = createCreature("Dying", 4, 5);
            Permanent dyingPermanent = new Permanent(dying);
            var may = new MayEffect(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE,
                    new TriggeringPermanentPower()), "Put counters?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER2_ID, 4, 5, dyingPermanent.getId(),
                    dyingPermanent);

            svc.dispatchCreatureDeath(CreatureDeathWatch.ANY,
                    new TriggerMatchContext(gd, perm, PLAYER1_ID, may, watcher, true), may, ctx);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEffectsToResolve()).containsExactly(may);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringCardId()).isEqualTo(dying.getId());
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(dyingPermanent.getId());
            assertThat(entry.getTriggeringPermanentSnapshot().getId()).isEqualTo(dyingPermanent.getId());
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(PLAYER2_ID);
            assertThat(entry.getTriggeringPermanentPowerAtTrigger()).isEqualTo(4);
            assertThat(entry.getTriggeringPermanentToughnessAtTrigger()).isEqualTo(5);
            assertThat(entry.getEventValue()).isEqualTo(4);
            assertThat(entry.getDyingPermanentManaValue()).isEqualTo(dying.getManaValue());
            assertThat(entry.getSourcePermanentSnapshot().getId()).isEqualTo(perm.getId());
            assertThat(entry.isMarkSourceOncePerTurnOnAcceptance()).isTrue();
        }

        @Test
        @DisplayName("A nontoken creature you control: the same record, the ability controlled by the source's controller")
        void allyNontokenRecordsTheDeath() {
            Card watcher = createCreature("Ally Tracker", 1, 1);
            Card dying = createCreature("Dying", 2, 3);
            Permanent dyingPermanent = new Permanent(dying);
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 2, 3, dyingPermanent.getId(),
                    dyingPermanent);

            svc.dispatchCreatureDeath(CreatureDeathWatch.ALLY_NONTOKEN, match(perm, PLAYER1_ID, effect), effect, ctx);

            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getControllerId()).isEqualTo(PLAYER1_ID);
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(dyingPermanent.getId());
            assertThat(entry.getTriggeringPermanentPowerAtTrigger()).isEqualTo(2);
            assertThat(entry.getTriggeringPermanentToughnessAtTrigger()).isEqualTo(3);
            assertThat(entry.getSourcePermanentSnapshot().getId()).isEqualTo(perm.getId());
        }
    }

    @Nested
    @DisplayName("Ally nontoken creature death handlers")
    class AllyNontokenCreatureDeathHandlers {

        @Test
        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card watcher = createCreature("May Ally", 1, 1);
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 2, 2), PLAYER1_ID, 2, 2);

            svc.dispatchCreatureDeath(CreatureDeathWatch.ALLY_NONTOKEN, match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Default adds to stack with sourcePermanentId")
        void defaultAddsToStackWithSourcePermanentId() {
            Card watcher = createCreature("Ally Tracker", 1, 1);
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 2, 2), PLAYER1_ID, 2, 2);

            svc.handleAllyNontokenDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(gd.stack.get(0).getDyingPermanentManaValue()).isEqualTo(ctx.dyingCard().getManaValue());
        }

        @Test
        @DisplayName("MayPayManaEffect goes on the stack with the dying card as the entry's triggering card")
        void mayPayBindsDyingCardAwareWrappedEffect() {
            Card dying = createCreature("Dead Creature", 2, 2);
            Card watcher = createCreature("Decaying Soil", 0, 0);
            var returnEffect = new ReturnTriggeringCardToOwnerHandEffect();
            var mayPay = new MayPayManaEffect("{1}", returnEffect, "Pay 1?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 2, 2);

            svc.dispatchCreatureDeath(CreatureDeathWatch.ALLY_NONTOKEN, match(perm, PLAYER1_ID, mayPay), mayPay, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(mayPay);
            assertThat(gd.stack.getFirst().getTriggeringCardId()).isEqualTo(dying.getId());
        }
    }

    @Nested
    @DisplayName("Any nontoken creature death handlers")
    class AnyNontokenCreatureDeathHandlers {

        @Test
        @DisplayName("A \"you may\" goes on the stack as written, the dying card its triggering card (CR 603.5)")
        void mayGoesOnTheStackAsWritten() {
            Card dying = createCreature("Dying Nontoken", 3, 3);
            Card watcher = createCreature("Mimic Vat", 0, 0);
            var may = new MayEffect(new ImprintDyingCreatureEffect(), "Exile and imprint?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 3, 3);

            svc.handleAnyNontokenCreatureDeathDefault(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.pendingMayAbilities).isEmpty();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(may);
            assertThat(gd.stack.getFirst().getTriggeringCardId()).isEqualTo(dying.getId());
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("A \"you may pay\" goes on the stack as written, the payment offered as it resolves (CR 603.5)")
        void mayPayGoesOnTheStackAsWritten() {
            Card dying = createCreature("Dead Creature", 2, 2);
            Card deathmantle = createEquipment("Nim Deathmantle");
            var mayPay = new MayPayManaEffect("{4}", new ReturnDyingCreatureToBattlefieldEffect(true), "Pay 4?");
            Permanent perm = new Permanent(deathmantle);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 2, 2);

            assertThat(svc.handleAnyNontokenCreatureDeathDefault(match(perm, PLAYER1_ID, mayPay), mayPay, ctx)).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve()).containsExactly(mayPay);
            assertThat(gd.stack.get(0).getTriggeringCardId()).isEqualTo(dying.getId());
        }
    }

    @Nested
    @DisplayName("Opponent creature death handlers")
    class OpponentCreatureDeathHandlers {

        @Test
        @DisplayName("A graveyard-targeting \"you may\" waits for its target with the \"do this only once each turn\" mark")
        void graveyardTargetMayCarriesTheOncePerTurnMark() {
            Card watcher = createCreature("Glissa, the Traitor", 3, 3);
            Card dying = createCreature("Dead Creature", 2, 2);
            Permanent dyingPermanent = new Permanent(dying);
            var may = new MayEffect(new DrawCardEffect(1), "Return?");
            Permanent perm = new Permanent(watcher);
            when(graveyardTargetingSupport.findTarget(List.of(may))).thenReturn(
                    new com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport.Target(
                            null, GraveyardSearchScope.CONTROLLERS_GRAVEYARD, "to your hand", 1));
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 2, 2, dyingPermanent.getId(),
                    dyingPermanent);

            svc.dispatchCreatureDeath(CreatureDeathWatch.OPPONENT,
                    new TriggerMatchContext(gd, perm, PLAYER2_ID, may, watcher, true), may, ctx);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).singleElement()
                    .isInstanceOfSatisfying(PermanentChoiceContext.DeathTriggerTarget.class, target -> {
                        assertThat(target.markSourceOncePerTurnOnAcceptance()).isTrue();
                        assertThat(target.triggeringCardId()).isEqualTo(dying.getId());
                        assertThat(target.triggeringPermanentControllerId()).isEqualTo(PLAYER1_ID);
                    });
        }

        @Test
        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card watcher = createCreature("Optional Vulture", 1, 1);
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(null, PLAYER1_ID, 0, 0);

            svc.dispatchCreatureDeath(CreatureDeathWatch.OPPONENT, match(perm, PLAYER2_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Default sets targetId to dyingCreatureControllerId and sourcePermanentId")
        void defaultSetsTargetAndSourcePermanentId() {
            Card watcher = createCreature("Vulture", 1, 1);
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(null, PLAYER1_ID, 0, 0);

            svc.handleOpponentCreatureDeathDefault(match(perm, PLAYER2_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getTargetId()).isEqualTo(PLAYER1_ID);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("Default gives the entry the opponent creature that died as its triggering card")
        void defaultBindsDyingCardAwareEffect() {
            Card watcher = createCreature("Vein Patron", 4, 4);
            Card dying = createCreature("Dead Creature", 2, 2);
            var effect = new ExileTriggeringCreatureFromGraveyardEffect();
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 2, 2);

            svc.handleOpponentCreatureDeathDefault(match(perm, PLAYER2_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getEffectsToResolve()).containsExactly(effect);
            assertThat(gd.stack.get(0).getTriggeringCardId()).isEqualTo(dying.getId());
        }
    }

    @Nested
    @DisplayName("handleAllyAuraOrEquipmentGraveyardDefault: a delayed return")
    class RegisterDelayedReturn {

        @Test
        @DisplayName("Puts the optional delayed return on the stack as written, the dying card its triggering card")
        void queuesTheOptionalReturnAsWritten() {
            Card dyingAura = createEnchantment("Dying Aura");
            Card tiana = createCreature("Tiana", 3, 3);
            var effect = new RegisterDelayedReturnCardFromGraveyardToHandEffect(true);
            Permanent perm = new Permanent(tiana);
            var ctx = new TriggerContext.AllyAuraOrEquipmentGraveyard(dyingAura, PLAYER1_ID);

            svc.handleAllyAuraOrEquipmentGraveyardDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve()).containsExactly(effect);
            assertThat(gd.stack.get(0).getTriggeringCardId()).isEqualTo(dyingAura.getId());
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card dyingAura = createEnchantment("Fallen Aura");
            Card tiana = createCreature("Tiana Ship's Caretaker", 3, 3);
            var effect = new RegisterDelayedReturnCardFromGraveyardToHandEffect(true);
            Permanent perm = new Permanent(tiana);
            var ctx = new TriggerContext.AllyAuraOrEquipmentGraveyard(dyingAura, PLAYER1_ID);

            svc.handleAllyAuraOrEquipmentGraveyardDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("Tiana Ship's Caretaker") && logEntry.plainText().contains("Fallen Aura")));
        }
    }

    @Nested
    @DisplayName("Graveyard returns of the card the event was about")
    class GraveyardReturnOfTheTriggeringCard {

        @Test
        @DisplayName("A land's entry has the land as its triggering card, with its graveyard incarnation, and the effect unchanged")
        void landReturnCarriesTheLandAsTriggeringCard() {
            Card land = new Card();
            land.setName("Forest");
            land.setType(CardType.LAND);
            gd.playerGraveyards.get(PLAYER1_ID).add(land);
            gd.graveyardEntryVersions.put(land.getId(), 7L);
            Permanent perm = new Permanent(createEnchantment("Sacred Ground"));
            var effect = new ReturnTriggeringLandFromGraveyardToBattlefieldEffect();
            var ctx = new TriggerContext.LandPutIntoGraveyard(land, PLAYER1_ID, PLAYER2_ID);

            assertThat(svc.handleLandGraveyardReturn(match(perm, PLAYER1_ID, effect), effect, ctx)).isTrue();

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTriggeringCardId()).isEqualTo(land.getId());
            assertThat(entry.isTriggeringCardIncarnation(7L)).isTrue();
            assertThat(entry.isTriggeringCardIncarnation(8L)).isFalse();
        }

        @Test
        @DisplayName("An artifact's target choice carries the artifact as its triggering card and the effect unchanged")
        void artifactReturnCarriesTheArtifactAsTriggeringCard() {
            Card artifact = createArtifact("Ornithopter");
            Permanent perm = new Permanent(createEnchantment("Pia's Revolution"));
            var effect = new ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect(3);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER1_ID, PLAYER1_ID, artifact, 0,
                    new Permanent(artifact), false);

            assertThat(svc.handleArtifactGraveyardReturnUnlessDamage(match(perm, PLAYER1_ID, effect), effect, ctx))
                    .isTrue();

            assertThat(gd.pendingInteractions)
                    .filteredOn(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class::isInstance)
                    .singleElement()
                    .satisfies(choice -> {
                        var targetChoice = (PermanentChoiceContext.SpellTargetTriggerAnyTarget) choice;
                        assertThat(targetChoice.triggeringCardId()).isEqualTo(artifact.getId());
                        assertThat(targetChoice.effects()).containsExactly(effect);
                    });
        }
    }

    @Nested
    @DisplayName("Graveyard defaults give the entry \"that card\"")
    class GraveyardDefaultsCarryTheTriggeringCard {

        private Card dyingInGraveyard(UUID ownerId, long version) {
            Card dying = createCreature("Grizzly Bears", 2, 2);
            gd.playerGraveyards.get(ownerId).add(dying);
            gd.graveyardEntryVersions.put(dying.getId(), version);
            return dying;
        }

        private void assertCarriesTheCardUnchanged(CardEffect effect, Card dying, long version) {
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTriggeringCardId()).isEqualTo(dying.getId());
            assertThat(entry.isTriggeringCardIncarnation(version)).isTrue();
            assertThat(entry.isTriggeringCardIncarnation(version + 1)).isFalse();
        }

        @Test
        @DisplayName("Any permanent: Yomiji's return goes on the stack unchanged, the dying card its triggering card")
        void anyPermanentDefaultCarriesTheDyingCard() {
            Card dying = dyingInGraveyard(PLAYER2_ID, 4L);
            Permanent perm = new Permanent(createCreature("Yomiji, Who Bars the Way", 4, 4));
            var effect = new ReturnTriggeringCardToOwnerHandEffect();
            var ctx = new TriggerContext.AnyPermanentGraveyard(dying, PLAYER2_ID, PLAYER2_ID);

            assertThat(svc.handleAnyPermanentGraveyardDefault(match(perm, PLAYER1_ID, effect), effect, ctx)).isTrue();

            assertCarriesTheCardUnchanged(effect, dying, 4L);
        }

        @Test
        @DisplayName("A creature put into your graveyard: Enduring Renewal's and Mortuary's effects go through the default")
        void ownedCreatureDefaultCarriesTheDyingCard() {
            Card dying = dyingInGraveyard(PLAYER1_ID, 2L);
            Permanent perm = new Permanent(createEnchantment("Enduring Renewal"));
            var effect = new ReturnTriggeringCardToOwnerHandEffect();
            var ctx = new TriggerContext.AnyPermanentGraveyard(dying, PLAYER2_ID, PLAYER1_ID);

            assertThat(svc.handleCreaturePutIntoOwnerGraveyardDefault(match(perm, PLAYER1_ID, effect), effect, ctx))
                    .isTrue();

            assertCarriesTheCardUnchanged(effect, dying, 2L);
        }

        @Test
        @DisplayName("A nontoken permanent you control: the default gives the dying card")
        void allyNontokenDefaultCarriesTheDyingCard() {
            Card dying = dyingInGraveyard(PLAYER1_ID, 3L);
            Permanent perm = new Permanent(createEnchantment("Watcher"));
            var effect = new ReturnTriggeringCardToOwnerHandEffect();
            var ctx = new TriggerContext.AnyPermanentGraveyard(dying, PLAYER1_ID, PLAYER1_ID);

            assertThat(svc.handleAllyNontokenPermanentGraveyardDefault(match(perm, PLAYER1_ID, effect), effect, ctx))
                    .isTrue();

            assertCarriesTheCardUnchanged(effect, dying, 3L);
        }

        @Test
        @DisplayName("Any nontoken permanent: the default gives the dying card")
        void anyNontokenDefaultCarriesTheDyingCard() {
            Card dying = dyingInGraveyard(PLAYER2_ID, 5L);
            Permanent perm = new Permanent(createEnchantment("Watcher"));
            var effect = new ReturnTriggeringCardToOwnerHandEffect();
            var ctx = new TriggerContext.AnyPermanentGraveyard(dying, PLAYER2_ID, PLAYER2_ID);

            assertThat(svc.handleAnyNontokenPermanentGraveyardDefault(match(perm, PLAYER1_ID, effect), effect, ctx))
                    .isTrue();

            assertCarriesTheCardUnchanged(effect, dying, 5L);
        }

        @Test
        @DisplayName("An equipped creature: Sword of the Realms' return goes through the default unchanged")
        void equippedDefaultCarriesTheDyingCard() {
            Card dying = dyingInGraveyard(PLAYER1_ID, 6L);
            Permanent perm = new Permanent(createEquipment("Sword of the Realms"));
            var effect = new ReturnTriggeringCardToOwnerHandEffect();
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, dying);

            svc.handleEquippedCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertCarriesTheCardUnchanged(effect, dying, 6L);
        }

        @Test
        @DisplayName("An Aura or Equipment you control: the default triggers for an Equipment too, the card its triggering card")
        void auraOrEquipmentDefaultCarriesTheCard() {
            Card equipment = createEquipment("Bonesplitter");
            gd.playerGraveyards.get(PLAYER1_ID).add(equipment);
            gd.graveyardEntryVersions.put(equipment.getId(), 1L);
            Permanent perm = new Permanent(createCreature("Watcher", 1, 1));
            var effect = new DrawCardEffect(1);
            var ctx = new TriggerContext.AllyAuraOrEquipmentGraveyard(equipment, PLAYER1_ID);

            assertThat(svc.handleAllyAuraOrEquipmentGraveyardDefault(match(perm, PLAYER1_ID, effect), effect, ctx))
                    .isTrue();

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(effect);
                assertThat(entry.getTriggeringCardId()).isEqualTo(equipment.getId());
                assertThat(entry.isTriggeringCardIncarnation(1L)).isTrue();
            });
        }
    }
}
