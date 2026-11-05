package com.github.laxika.magicalvibes.service.trigger;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedControllerSacrificesCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeAndDrawEqualToDyingPowerEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTargetForEachLeavingSourceCountersEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEqualToDyingPowerEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;
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

    // ── "when this dies" handlers ──────────────────────────────────────────────












    @Nested
    @DisplayName("handleAllyCreatureMayPay")
    class AllyCreatureMayPay {

        @Test
        @DisplayName("Queues may ability for MayPayManaEffect")
        void queuesMayAbility() {
            Card watcher = createCreature("Pay Watcher", 1, 1);
            var mayPay = new MayPayManaEffect("{1}", new DrawCardEffect(1), "Pay?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 2, 2), PLAYER1_ID, 2, 2);

            svc.handleAllyCreatureMayPay(match(perm, PLAYER1_ID, mayPay), mayPay, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayPayManaEffect.class);
        }
    }

    @Nested
    @DisplayName("handleAllyCreatureMayPayLife")
    class AllyCreatureMayPayLife {

        @Test
        @DisplayName("Adds the optional life payment to the stack")
        void addsLifePaymentToStack() {
            Card watcher = createCreature("Life Pay Watcher", 1, 1);
            var mayPay = new MayPayLifeEffect(2, new DrawCardEffect(1), "Pay 2?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 2, 2), PLAYER1_ID, 2, 2);

            assertThat(svc.handleAllyCreatureMayPayLife(match(perm, PLAYER1_ID, mayPay), mayPay, ctx)).isTrue();

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isEqualTo(mayPay);
        }
    }

    @Nested
    @DisplayName("handleAllyCreatureMay")
    class AllyCreatureMay {

        @Test
        @DisplayName("Queues may ability with sourcePermanentId")
        void queuesMayAbilityWithSourcePermanentId() {
            Card watcher = createCreature("May Watcher", 1, 1);
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 2, 2), PLAYER1_ID, 2, 2);

            svc.handleAllyCreatureMay(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }
    }


    @Nested
    @DisplayName("handleEquippedCreatureDeathDefault")
    class EquippedCreatureDeathDefault {

        @Test
        @DisplayName("Snapshots dying power for the optional life payment and draw")
        void snapshotsDyingPowerForPaymentAndDraw() {
            Card equipment = createEquipment("Mask");
            var effect = new MayPayLifeAndDrawEqualToDyingPowerEffect();
            Permanent perm = new Permanent(equipment);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, null, 4);

            svc.handleEquippedCreatureDeathMayPayLifeAndDrawEqualToPower(
                    match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            var resolved = (MayPayLifeEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.lifeCost()).isEqualTo(4);
            assertThat(resolved.wrapped()).isEqualTo(new DrawCardEffect(4));
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
    @DisplayName("handleEquippedCreatureReturn")
    class EquippedCreatureReturn {

        @Test
        @DisplayName("Gives the entry the dying card and carries the source Equipment as its target")
        void bindsDyingCardAndSource() {
            Card equipment = createEquipment("Oathkeeper");
            Card dying = createCreature("Samurai", 3, 3);
            Permanent perm = new Permanent(equipment);
            var effect = new ReturnDyingCreatureToBattlefieldEffect(false);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, dying);

            svc.handleEquippedCreatureReturn(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getTargetId()).isEqualTo(perm.getId());
            assertThat(gd.stack.get(0).getTriggeringCardId()).isEqualTo(dying.getId());
            assertThat(gd.stack.get(0).getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("Does not fire without a dying card")
        void noDyingCard() {
            Card equipment = createEquipment("Oathkeeper");
            Permanent perm = new Permanent(equipment);
            var effect = new ReturnDyingCreatureToBattlefieldEffect(false);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, null);

            assertThat(svc.handleEquippedCreatureReturn(match(perm, PLAYER1_ID, effect), effect, ctx)).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("handleEquippedCreatureDelayedReturn")
    class EquippedCreatureDelayedReturn {

        @Test
        @DisplayName("Binds the dying card to the delayed return trigger")
        void bindsDyingCard() {
            Card equipment = createEquipment("Resurrection Orb");
            Card dying = createCreature("Dying Creature", 2, 2);
            Permanent perm = new Permanent(equipment);
            var effect = new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                    false, null, 0, null, null, false, true, false);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, dying);

            assertThat(svc.handleEquippedCreatureDelayedReturn(match(perm, PLAYER1_ID, effect), effect, ctx))
                    .isTrue();

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(entry.getTriggeringCardId()).isEqualTo(dying.getId());
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("Does not fire without a dying card")
        void noDyingCard() {
            Card equipment = createEquipment("Resurrection Orb");
            Permanent perm = new Permanent(equipment);
            var effect = new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                    false, null, 0, null, null, false, true, false);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, null);

            assertThat(svc.handleEquippedCreatureDelayedReturn(match(perm, PLAYER1_ID, effect), effect, ctx))
                    .isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }


    // ── ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD handlers ─────────────

    @Nested
    @DisplayName("handleReturnSourceAura")
    class ReturnSourceAura {

        @Test
        @DisplayName("Bakes dying creature's controller ID")
        void bakesControllerId() {
            Card aura = createEnchantment("Necrotic Plague");
            var effect = new ReturnSourceAuraToOpponentCreatureOnDeathEffect();
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER1_ID, null, 0, 0);

            svc.handleReturnSourceAura(match(perm, PLAYER2_ID, effect), effect, ctx);

            var resolved = (ReturnSourceAuraToOpponentCreatureOnDeathEffect) gd.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class).effects().get(0);
            assertThat(resolved.enchantedCreatureControllerId()).isEqualTo(PLAYER1_ID);
        }

        @Test
        @DisplayName("Does not bake controller when null")
        void doesNotBakeWhenNull() {
            Card aura = createEnchantment("Necrotic Plague");
            var effect = new ReturnSourceAuraToOpponentCreatureOnDeathEffect();
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), null, null, 0, 0);

            svc.handleReturnSourceAura(match(perm, PLAYER2_ID, effect), effect, ctx);

            var resolved = (ReturnSourceAuraToOpponentCreatureOnDeathEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.enchantedCreatureControllerId()).isNull();
        }
    }

    @Nested
    @DisplayName("handleReturnEnchantedCreature")
    class ReturnEnchantedCreature {

        @Test
        @DisplayName("Bakes dying creature's card ID")
        void bakesCardId() {
            Card aura = createEnchantment("Demonic Vigor");
            var effect = new ReturnEnchantedCreatureToOwnerHandOnDeathEffect();
            Permanent perm = new Permanent(aura);
            UUID creatureCardId = UUID.randomUUID();
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER1_ID, creatureCardId, 0, 0);

            svc.handleReturnEnchantedCreature(match(perm, PLAYER1_ID, effect), effect, ctx);

            var resolved = (ReturnEnchantedCreatureToOwnerHandOnDeathEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.dyingCreatureCardId()).isEqualTo(creatureCardId);
        }

        @Test
        @DisplayName("Does not bake card ID when null")
        void doesNotBakeWhenNull() {
            Card aura = createEnchantment("Demonic Vigor");
            var effect = new ReturnEnchantedCreatureToOwnerHandOnDeathEffect();
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER1_ID, null, 0, 0);

            svc.handleReturnEnchantedCreature(match(perm, PLAYER1_ID, effect), effect, ctx);

            var resolved = (ReturnEnchantedCreatureToOwnerHandOnDeathEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.dyingCreatureCardId()).isNull();
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
        @DisplayName("Snapshots event value for a dynamic look count")
        void snapshotsEventValueForDynamicLookCount() {
            Card aura = createEnchantment("Necrosynthesis");
            var effect = new LookAtTopCardsEffect(
                    new EventValue(), new Fixed(1), null,
                    LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false);
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), null, null, 3, 3);
            when(amountEvaluationService.referencesEventValue(effect.lookCount())).thenReturn(true);

            svc.handleEnchantedPermanentDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("handleEnchantedCreatureControllerLosesLife")
    class EnchantedCreatureControllerLosesLife {

        @Test
        @DisplayName("Bakes the dying creature's toughness and controller into the effect")
        void bakesToughnessAndController() {
            Card aura = createEnchantment("Banewasp Affliction");
            var effect = new EnchantedCreatureControllerLosesLifeEffect(0);
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER2_ID, null, 0, 4);

            svc.handleEnchantedCreatureControllerLosesLife(match(perm, PLAYER1_ID, effect), effect, ctx);

            var resolved = (EnchantedCreatureControllerLosesLifeEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.amount()).isEqualTo(4);
            assertThat(resolved.affectedPlayerId()).isEqualTo(PLAYER2_ID);
        }

        @Test
        @DisplayName("Clamps negative toughness to zero life loss")
        void clampsNegativeToughness() {
            Card aura = createEnchantment("Banewasp Affliction");
            var effect = new EnchantedCreatureControllerLosesLifeEffect(0);
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER2_ID, null, 0, -1);

            svc.handleEnchantedCreatureControllerLosesLife(match(perm, PLAYER1_ID, effect), effect, ctx);

            var resolved = (EnchantedCreatureControllerLosesLifeEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.amount()).isZero();
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
    @DisplayName("handleEnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughness")
    class EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughness {

        @Test
        @DisplayName("Bakes power loss and toughness gain onto one stack entry")
        void bakesPowerLossAndToughnessGain() {
            Card aura = createEnchantment("Death Watch");
            var effect = new EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect();
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER2_ID, null, 2, 4);

            svc.handleEnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughness(
                    match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            var effects = gd.stack.get(0).getEffectsToResolve();
            assertThat(effects).hasSize(2);
            var loss = (EnchantedCreatureControllerLosesLifeEffect) effects.get(0);
            assertThat(loss.amount()).isEqualTo(2);
            assertThat(loss.affectedPlayerId()).isEqualTo(PLAYER2_ID);
            var gain = (GainLifeEffect) effects.get(1);
            assertThat(gain.amount()).isEqualTo(new Fixed(4));
        }

        @Test
        @DisplayName("Clamps negative power and toughness to zero")
        void clampsNegatives() {
            Card aura = createEnchantment("Death Watch");
            var effect = new EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect();
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER2_ID, null, -3, -1);

            svc.handleEnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughness(
                    match(perm, PLAYER1_ID, effect), effect, ctx);

            var effects = gd.stack.get(0).getEffectsToResolve();
            assertThat(((EnchantedCreatureControllerLosesLifeEffect) effects.get(0)).amount()).isZero();
            assertThat(((GainLifeEffect) effects.get(1)).amount()).isEqualTo(new Fixed(0));
        }
    }

    // ── ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD handlers ─────────────

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
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("enchanted permanent left the battlefield")));
        }
    }

    @Nested
    @DisplayName("handleEnchantedControllerSacrificesCreatureOnLeave")
    class EnchantedControllerSacrificesCreatureOnLeave {

        @Test
        @DisplayName("Bakes the leaving creature's controller, not the Aura controller")
        void bakesLeavingControllerNotAuraController() {
            Card aura = createEnchantment("Funeral March");
            var effect = new EnchantedControllerSacrificesCreatureOnLeaveEffect();
            Permanent auraPerm = new Permanent(aura);
            // Aura controlled by PLAYER1, but the enchanted creature was controlled by PLAYER2.
            var ctx = new TriggerContext.EnchantedPermanentLeaves(
                    new Permanent(createCreature("Leaving", 2, 2)), PLAYER2_ID);

            assertThat(svc.handleEnchantedControllerSacrificesCreatureOnLeave(
                    match(auraPerm, PLAYER1_ID, effect), effect, ctx)).isTrue();

            assertThat(gd.stack).hasSize(1);
            var resolved = (EnchantedControllerSacrificesCreatureOnLeaveEffect)
                    gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.enchantedControllerId()).isEqualTo(PLAYER2_ID);
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

    // ── ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD handlers ───

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
        @DisplayName("Default effect binds the artifact's counter snapshot")
        void artifactDefaultBindsArtifactCounters() {
            Card watcher = createCreature("Donatello, Mutant Mechanic", 3, 5);
            Permanent perm = new Permanent(watcher);
            var effect = new PutCountersOnTargetForEachLeavingSourceCountersEffect(
                            new PermanentIsCreaturePredicate());
            var ctx = new TriggerContext.ArtifactGraveyard(
                    PLAYER1_ID, PLAYER1_ID, null, 0,
                    Map.of(CounterType.PLUS_ONE_PLUS_ONE, 2, CounterType.CHARGE, 3));

            assertThat(svc.handleArtifactGraveyardDefault(
                    match(perm, PLAYER1_ID, effect), effect, ctx)).isTrue();

            var pending = (PermanentChoiceContext.SpellTargetTriggerAnyTarget)
                    gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            var bound = (PutCountersOnTargetForEachLeavingSourceCountersEffect) pending.effects().getFirst();
            assertThat(bound.counters()).containsExactlyInAnyOrderEntriesOf(
                    Map.of(CounterType.PLUS_ONE_PLUS_ONE, 2, CounterType.CHARGE, 3));
        }

        @Test
        @DisplayName("Default effect does not fire when the artifact had no counters")
        void artifactDefaultDoesNotFireWithoutArtifactCounters() {
            Card watcher = createCreature("Donatello, Mutant Mechanic", 3, 5);
            Permanent perm = new Permanent(watcher);
            var effect = new PutCountersOnTargetForEachLeavingSourceCountersEffect(
                            new PermanentIsCreaturePredicate());
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER1_ID, PLAYER1_ID);

            assertThat(svc.handleArtifactGraveyardDefault(
                    match(perm, PLAYER1_ID, effect), effect, ctx)).isFalse();
            assertThat(gd.pendingInteractions).isEmpty();
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

            svc.handleAnyCreatureDeathPutCounters(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("BoostSelfEffect sets sourcePermanentId")
        void boostSelfSetsSourcePermanentId() {
            Card watcher = createCreature("Gristle Grinner", 3, 3);
            var effect = new BoostSelfEffect(2, 2);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.handleAnyCreatureDeathBoostSelf(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("TapPermanentsEffect sets sourcePermanentId")
        void tapSetsSourcePermanentId() {
            Card watcher = createCreature("Fleshmad Steed", 2, 2);
            var effect = new TapPermanentsEffect(TapUntapScope.SELF);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.handleAnyCreatureDeathTap(match(perm, PLAYER1_ID, effect), effect, ctx);

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

            svc.handleAnyCreatureDeathMay(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("PutCountersEqualToDyingPower bakes the dying power into an optional counter ability")
        void putCountersEqualToDyingPowerBakesPower() {
            Card watcher = createCreature("Kresh the Bloodbraided", 3, 3);
            var effect = new PutCountersOnSourceEqualToDyingPowerEffect(1, 1, true);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 4, 4), PLAYER1_ID, 4, 4);

            svc.handleAnyCreatureDeathPutCountersEqualToPower(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
            var may = (MayEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            var counters = (PutCountersOnSourceEffect) may.wrapped();
            assertThat(counters.amount()).isEqualTo(4);
        }

        @Test
        @DisplayName("Targeting MayEffect queues a DeathTriggerTarget interaction (CR 603.3d)")
        void targetingMayQueuesDeathTriggerTarget() {
            Card watcher = createEnchantment("Vicious Shadows");
            var may = new MayEffect(
                    new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER), "Deal damage?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.handleAnyCreatureDeathMay(match(perm, PLAYER1_ID, may), may, ctx);

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

            svc.handleAnyCreatureDeathDamageController(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
        }

        @Test
        @DisplayName("BecomeCopyOfDyingCreature queues a pay-mana may ability with the dying card baked in")
        void becomeCopyQueuesMayPayAbility() {
            Card puca = createCreature("Cemetery Puca", 1, 2);
            Card dying = createCreature("Dead Creature", 2, 2);
            var copyEffect = new BecomeCopyOfDyingCreatureEffect();
            var rawMayPay = new MayPayManaEffect("{1}", copyEffect, "Pay {1}?");
            Permanent perm = new Permanent(puca);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 2, 2);

            assertThat(svc.handleAnyCreatureDeathBecomeCopy(match(perm, PLAYER1_ID, rawMayPay), copyEffect, ctx)).isTrue();
            assertThat(gd.pendingMayAbilities).hasSize(1);
            assertThat(gd.pendingMayAbilities.get(0).manaCost()).isEqualTo("{1}");
            var baked = (BecomeCopyOfDyingCreatureEffect) gd.pendingMayAbilities.get(0).effects().get(0);
            assertThat(baked.dyingCardId()).isEqualTo(dying.getId());
        }

        @Test
        @DisplayName("BecomeCopyOfDyingCreature does not fire when the dying card is null")
        void becomeCopyNoFireWithoutDyingCard() {
            Card puca = createCreature("Cemetery Puca", 1, 2);
            var copyEffect = new BecomeCopyOfDyingCreatureEffect();
            var rawMayPay = new MayPayManaEffect("{1}", copyEffect, "Pay {1}?");
            Permanent perm = new Permanent(puca);
            var ctx = new TriggerContext.CreatureDeath(null, PLAYER1_ID, 0, 0);

            assertThat(svc.handleAnyCreatureDeathBecomeCopy(match(perm, PLAYER1_ID, rawMayPay), copyEffect, ctx)).isFalse();
            assertThat(gd.pendingMayAbilities).isEmpty();
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

            assertThat(svc.handleAnyCreatureDeathCreateTokenCopy(
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
    @DisplayName("Ally nontoken creature death handlers")
    class AllyNontokenCreatureDeathHandlers {

        @Test
        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card watcher = createCreature("May Ally", 1, 1);
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 2, 2), PLAYER1_ID, 2, 2);

            svc.handleAllyNontokenMay(match(perm, PLAYER1_ID, may), may, ctx);

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

            svc.handleAllyNontokenMayPay(match(perm, PLAYER1_ID, mayPay), mayPay, ctx);

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
        @DisplayName("Imprint goes on the stack with its may and the dying card as its triggering card (CR 603.5)")
        void imprintBakesDyingCardId() {
            Card dying = createCreature("Dying Nontoken", 3, 3);
            Card watcher = createCreature("Mimic Vat", 0, 0);
            var imprint = new ImprintDyingCreatureEffect();
            var rawMay = new MayEffect(imprint, "Exile and imprint?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 3, 3);

            svc.handleImprintDyingCreature(match(perm, PLAYER1_ID, rawMay), imprint, ctx);

            assertThat(gd.pendingMayAbilities).isEmpty();
            assertThat(gd.stack).hasSize(1);
            var may = (MayEffect) gd.stack.getFirst().getEffectsToResolve().getFirst();
            assertThat(may.prompt()).isEqualTo("Exile and imprint?");
            assertThat(may.wrapped()).isEqualTo(imprint);
            assertThat(gd.stack.getFirst().getTriggeringCardId()).isEqualTo(dying.getId());
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(perm.getId());
            assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        }

        @Test
        @DisplayName("ReturnDyingCreature fires when dying card is in controller's graveyard")
        void returnTriggerFiresWhenInGraveyard() {
            Card dying = createCreature("Dead Creature", 2, 2);
            Card deathmantle = createEquipment("Nim Deathmantle");
            var returnEffect = new ReturnDyingCreatureToBattlefieldEffect(true);
            var rawMayPay = new MayPayManaEffect("{4}", returnEffect, "Pay 4?");
            Permanent perm = new Permanent(deathmantle);
            gd.playerGraveyards.get(PLAYER1_ID).add(dying);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 2, 2);

            assertThat(svc.handleReturnDyingCreatureMayPay(match(perm, PLAYER1_ID, rawMayPay), returnEffect, ctx)).isTrue();
            assertThat(gd.stack).hasSize(1);
            var onStack = (MayPayManaEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(onStack.manaCost()).isEqualTo("{4}");
        }

        @Test
        @DisplayName("ReturnDyingCreature does not fire when dying card is NOT in graveyard")
        void returnTriggerDoesNotFireWhenNotInGraveyard() {
            Card dying = createCreature("Dead Creature", 2, 2);
            Card deathmantle = createEquipment("Nim Deathmantle");
            var returnEffect = new ReturnDyingCreatureToBattlefieldEffect(true);
            var rawMayPay = new MayPayManaEffect("{4}", returnEffect, "Pay 4?");
            Permanent perm = new Permanent(deathmantle);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 2, 2);

            assertThat(svc.handleReturnDyingCreatureMayPay(match(perm, PLAYER1_ID, rawMayPay), returnEffect, ctx)).isFalse();
        }

        @Test
        @DisplayName("ReturnDyingCreature does not fire when graveyard is null")
        void returnTriggerNullGraveyard() {
            Card dying = createCreature("Dead Creature", 2, 2);
            Card deathmantle = createEquipment("Nim Deathmantle");
            var returnEffect = new ReturnDyingCreatureToBattlefieldEffect(true);
            var rawMayPay = new MayPayManaEffect("{4}", returnEffect, "Pay 4?");
            Permanent perm = new Permanent(deathmantle);
            gd.playerGraveyards.remove(PLAYER1_ID);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 2, 2);

            assertThat(svc.handleReturnDyingCreatureMayPay(match(perm, PLAYER1_ID, rawMayPay), returnEffect, ctx)).isFalse();
        }
    }


    @Nested
    @DisplayName("Opponent creature death handlers")
    class OpponentCreatureDeathHandlers {

        @Test
        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card watcher = createCreature("Optional Vulture", 1, 1);
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(null, PLAYER1_ID, 0, 0);

            svc.handleOpponentCreatureDeathMay(match(perm, PLAYER2_ID, may), may, ctx);

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

    // ── "when this leaves the battlefield" handlers ────────────────────────────


    // ── ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD handler ──

    @Nested
    @DisplayName("handleRegisterDelayedReturn")
    class RegisterDelayedReturn {

        @Test
        @DisplayName("Queues the optional delayed return with the dying card as the entry's triggering card")
        void queuesMayEffectWithBakedCardId() {
            Card dyingAura = createEnchantment("Dying Aura");
            Card tiana = createCreature("Tiana", 3, 3);
            var effect = new RegisterDelayedReturnCardFromGraveyardToHandEffect();
            Permanent perm = new Permanent(tiana);
            var ctx = new TriggerContext.AllyAuraOrEquipmentGraveyard(dyingAura, PLAYER1_ID);

            svc.handleRegisterDelayedReturn(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve())
                    .containsExactly(new RegisterDelayedReturnCardFromGraveyardToHandEffect(true));
            assertThat(gd.stack.get(0).getTriggeringCardId()).isEqualTo(dyingAura.getId());
        }

        @Test
        @DisplayName("May prompt includes dying card name")
        void promptIncludesDyingCardName() {
            Card dyingEquip = createEquipment("Lost Sword");
            Card tiana = createCreature("Tiana", 3, 3);
            var effect = new RegisterDelayedReturnCardFromGraveyardToHandEffect();
            Permanent perm = new Permanent(tiana);
            var ctx = new TriggerContext.AllyAuraOrEquipmentGraveyard(dyingEquip, PLAYER1_ID);

            svc.handleRegisterDelayedReturn(match(perm, PLAYER1_ID, effect), effect, ctx);

            MayEffect may = (MayEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(may.prompt()).contains("Lost Sword");
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card dyingAura = createEnchantment("Fallen Aura");
            Card tiana = createCreature("Tiana Ship's Caretaker", 3, 3);
            var effect = new RegisterDelayedReturnCardFromGraveyardToHandEffect();
            Permanent perm = new Permanent(tiana);
            var ctx = new TriggerContext.AllyAuraOrEquipmentGraveyard(dyingAura, PLAYER1_ID);

            svc.handleRegisterDelayedReturn(match(perm, PLAYER1_ID, effect), effect, ctx);

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("Tiana Ship's Caretaker") && logEntry.plainText().contains("Fallen Aura")));
        }
    }
}
