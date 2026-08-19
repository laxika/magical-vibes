package com.github.laxika.magicalvibes.service.trigger;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillOpponentOnLifeLossEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaDrawXCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiscTriggerCollectorServiceTest {

    @Mock
    private GameLogService gameLogService;

    @Mock
    private GraveyardService graveyardService;

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private PredicateEvaluationService predicateEvaluationService;

    @Mock
    private ExileService exileService;

    @Mock
    private PermanentControlSupport permanentControlSupport;

    @Mock
    private AmountEvaluationService amountEvaluationService;

    @InjectMocks
    private MiscTriggerCollectorService sut;

    private TriggerCollectorRegistry registry;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");

        registry = new TriggerCollectorRegistry();
        TriggerCollectorRegistry.scanBean(sut, registry);
    }

    // ===== Helpers =====

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        return card;
    }

    private static Permanent createPermanent(String name) {
        return new Permanent(createCard(name));
    }

    private TriggerMatchContext match(Permanent perm, UUID controllerId, CardEffect effect) {
        return new TriggerMatchContext(gd, perm, controllerId, effect);
    }

    @Test
    @DisplayName("life-gain pay-X draw trigger snapshots the life gained on the stack entry")
    void lifeGainPayXDrawTriggerSnapshotsLifeGained() {
        Permanent perm = createPermanent("Well of Lost Dreams");
        var effect = new PayXManaDrawXCardsEffect();
        var ctx = new TriggerContext.LifeGain(player1Id, 3);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getLast().getEventValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("life-gain may-pay trigger keeps the optional payment on the stack")
    void lifeGainMayPayTriggerKeepsOptionalPayment() {
        Permanent perm = createPermanent("Dawn of Hope");
        var effect = new MayPayManaEffect("{2}", new DrawCardEffect(1), "Pay {2} to draw a card?");
        var ctx = new TriggerContext.LifeGain(player1Id, 1);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
    }

    @Test
    @DisplayName("surveil once-per-turn trigger queues its wrapped effect only once")
    void surveilOncePerTurnTriggerQueuesWrappedEffectOnlyOnce() {
        Permanent perm = createPermanent("Whispering Snitch");
        var effect = new OncePerTurnTriggerEffect(SequenceEffect.of(
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                new GainLifeEffect(1)));
        var ctx = new TriggerContext.Surveil(player1Id);

        assertThat(registry.dispatch(
                match(perm, player1Id, effect), EffectSlot.ON_CONTROLLER_SURVEILS, effect, ctx)).isTrue();
        assertThat(registry.dispatch(
                match(perm, player1Id, effect), EffectSlot.ON_CONTROLLER_SURVEILS, effect, ctx)).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect.wrapped());
        assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("surveil default trigger queues the original effect")
    void surveilDefaultTriggerQueuesOriginalEffect() {
        Permanent perm = createPermanent("Disinformation Campaign");
        var effect = ReturnToHandEffect.self();
        var ctx = new TriggerContext.Surveil(player1Id);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), EffectSlot.ON_CONTROLLER_SURVEILS, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("energy-gain default trigger queues the original effect")
    void energyGainDefaultTriggerQueuesEffect() {
        Permanent perm = createPermanent("Fabrication Module");
        var effect = new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);
        var ctx = new TriggerContext.EnergyGain(player1Id, 1);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), EffectSlot.ON_CONTROLLER_GETS_ENERGY, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).isEmpty();
        PermanentChoiceContext.SelfTriggeredAbilityTarget pending =
                gd.peekPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class);
        assertThat(pending).isNotNull();
        assertThat(pending.effects()).containsExactly(effect);
        assertThat(pending.controllerId()).isEqualTo(player1Id);
        assertThat(pending.sourcePermanentId()).isEqualTo(perm.getId());
    }

    // ===== ON_ALLY_PERMANENT_SACRIFICED — MayPayManaEffect =====

    @Nested
    @DisplayName("ON_ALLY_PERMANENT_SACRIFICED — MayPayManaEffect")
    class SacrificeMayPay {

        @Test
        @DisplayName("queues may-pay ability on stack when ally permanent is sacrificed")
        void queuesMayPayAbilityOnSacrifice() {
            Permanent perm = createPermanent("Furnace Celebration");
            var inner = new BoostSelfEffect(1, 1);
            var effect = new MayPayManaEffect("{2}", inner, "Pay {2}?");
            var ctx = new TriggerContext.AllySacrificed(player1Id, null);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("uses sacrificingPlayerId as ability controller, not permanent controller")
        void usesSacrificingPlayerIdAsController() {
            Permanent perm = createPermanent("Furnace Celebration");
            var inner = new BoostSelfEffect(1, 1);
            var effect = new MayPayManaEffect("{2}", inner, "Pay {2}?");
            var ctx = new TriggerContext.AllySacrificed(player2Id, null);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);

            assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2Id);
        }
    }

    // ===== ON_ALLY_PERMANENT_SACRIFICED — MayEffect =====

    @Nested
    @DisplayName("ON_ALLY_PERMANENT_SACRIFICED — MayEffect")
    class SacrificeMay {

        @Test
        @DisplayName("queues may ability on stack when ally permanent is sacrificed")
        void queuesMayAbilityOnSacrifice() {
            Permanent perm = createPermanent("Some Card");
            var inner = new BoostSelfEffect(1, 1);
            var effect = new MayEffect(inner, "Do you want to?");
            var ctx = new TriggerContext.AllySacrificed(player1Id, null);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("uses sacrificingPlayerId as ability controller, not permanent controller")
        void usesSacrificingPlayerIdAsController() {
            Permanent perm = createPermanent("Some Card");
            var inner = new BoostSelfEffect(1, 1);
            var effect = new MayEffect(inner, "Do you want to?");
            var ctx = new TriggerContext.AllySacrificed(player2Id, null);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);

            assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2Id);
        }
    }

    // ===== ON_ALLY_PERMANENT_SACRIFICED — default CardEffect =====

    @Nested
    @DisplayName("ON_ALLY_PERMANENT_SACRIFICED — default CardEffect")
    class SacrificeDefault {

        @Test
        @DisplayName("puts triggered ability on stack for non-may effect")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Blood Artist");
            var effect = new BoostSelfEffect(1, 1);
            var ctx = new TriggerContext.AllySacrificed(player1Id, null);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getLast().getDescription()).contains("Blood Artist");
            assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("stack entry includes the effect")
        void stackEntryIncludesEffect() {
            Permanent perm = createPermanent("Blood Artist");
            var effect = new BoostSelfEffect(2, 2);
            var ctx = new TriggerContext.AllySacrificed(player1Id, null);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);

            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("uses sacrificingPlayerId as ability controller, not permanent controller")
        void usesSacrificingPlayerIdAsController() {
            Permanent perm = createPermanent("Blood Artist");
            var effect = new BoostSelfEffect(1, 1);
            var ctx = new TriggerContext.AllySacrificed(player2Id, null);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);

            assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2Id);
        }
    }

    // ===== ON_ENCHANTED_PERMANENT_TAPPED — GivePoisonCountersEffect (ENCHANTED_PERMANENT_CONTROLLER) =====

    @Nested
    @DisplayName("ON_ALLY_PERMANENT_SACRIFICED - targeted conditional trigger")
    class SacrificeTargetedConditional {

        @Test
        @DisplayName("queues target selection instead of putting an untargeted ability on the stack")
        void queuesTargetSelection() {
            Permanent perm = createPermanent("Fleeting Memories");
            Card clue = createCard("Clue");
            clue.setType(CardType.ARTIFACT);
            clue.setSubtypes(List.of(CardSubtype.CLUE));
            var effect = new TriggeringPermanentConditionalEffect(
                    new PermanentHasSubtypePredicate(CardSubtype.CLUE),
                    new MillEffect(3, MillRecipient.TARGET_PLAYER));
            var ctx = new TriggerContext.AllySacrificed(player1Id, clue);

            when(predicateEvaluationService.matchesPermanentPredicate(
                    any(GameData.class), any(Permanent.class), any(PermanentPredicate.class)))
                    .thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class)).isTrue();
            var pending = gd.peekPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class);
            assertThat(pending.controllerId()).isEqualTo(player1Id);
            assertThat(pending.sourcePermanentId()).isEqualTo(perm.getId());
            assertThat(pending.effects()).containsExactly(effect.wrapped());
        }
    }

    @Nested
    @DisplayName("ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE")
    class PermanentCardPutIntoGraveyard {

        @Test
        @DisplayName("puts the triggered ability on the stack with the source permanent")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Moonshadow");
            var effect = new BoostSelfEffect(1, 1);
            var ctx = new TriggerContext.PermanentCardPutIntoGraveyard(
                    createCard("Spellbook"), player1Id);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("queues target selection for a targeted graveyard trigger")
        void queuesTargetSelectionForTargetedTrigger() {
            Card sourceCard = createCard("Measure of Wickedness");
            var effect = new TargetPlayerGainsControlOfSourceCreatureEffect();
            sourceCard.target(new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                    "Target must be an opponent"
            )).addEffect(EffectSlot.ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, effect);
            Permanent perm = new Permanent(sourceCard);
            var ctx = new TriggerContext.CardPutIntoGraveyard(
                    createCard("Spellbook"), player1Id);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            PermanentChoiceContext.SelfTriggeredAbilityTarget pending =
                    gd.peekPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class);
            assertThat(pending).isNotNull();
            assertThat(pending.effects()).containsExactly(effect);
            assertThat(pending.sourcePermanentId()).isEqualTo(perm.getId());
        }
    }

    @Nested
    @DisplayName("ON_ENCHANTED_PERMANENT_TAPPED — GivePoisonCountersEffect (ENCHANTED_PERMANENT_CONTROLLER)")
    class EnchantedPermanentTapPoison {

        @Test
        @DisplayName("puts triggered ability on stack with resolved effect containing tapped permanent's controller")
        void putsTriggeredAbilityOnStack() {
            Permanent aura = createPermanent("Relic Putrescence");
            Permanent tappedPerm = createPermanent("Sol Ring");
            var effect = new GivePoisonCountersEffect(1, PoisonRecipient.ENCHANTED_PERMANENT_CONTROLLER);
            var ctx = new TriggerContext.EnchantedPermanentTap(tappedPerm, player2Id);

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Relic Putrescence");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(aura.getId());
        }

        @Test
        @DisplayName("resolved effect bakes in the tapped permanent controller's ID")
        void resolvedEffectContainsControllerId() {
            Permanent aura = createPermanent("Relic Putrescence");
            Permanent tappedPerm = createPermanent("Sol Ring");
            var effect = new GivePoisonCountersEffect(1, PoisonRecipient.ENCHANTED_PERMANENT_CONTROLLER);
            var ctx = new TriggerContext.EnchantedPermanentTap(tappedPerm, player2Id);

            registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, effect, ctx);

            var resolved = (GivePoisonCountersEffect) gd.stack.getLast().getEffectsToResolve().getFirst();
            assertThat(resolved.affectedPlayerId()).isEqualTo(player2Id);
            assertThat(resolved.amount()).isEqualTo(1);
        }

        @Test
        @DisplayName("broadcasts trigger log message")
        void broadcastsTriggerLog() {
            Permanent aura = createPermanent("Relic Putrescence");
            Permanent tappedPerm = createPermanent("Sol Ring");
            var effect = new GivePoisonCountersEffect(1, PoisonRecipient.ENCHANTED_PERMANENT_CONTROLLER);
            var ctx = new TriggerContext.EnchantedPermanentTap(tappedPerm, player2Id);

            registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, effect, ctx);

            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }
    }

    // ===== ON_ENCHANTED_PERMANENT_TAPPED — ForcedCostOrElseEffect =====

    @Nested
    @DisplayName("ON_ENCHANTED_PERMANENT_TAPPED — ForcedCostOrElseEffect")
    class EnchantedPermanentTapForcedCost {

        @Test
        @DisplayName("bakes tapped permanent's controller as targetId for pay-or-penalty")
        void bakesControllerAsTargetId() {
            Permanent aura = createPermanent("Seizures");
            Permanent tappedPerm = createPermanent("Grizzly Bears");
            var effect = ForcedCostOrElseEffect.enchantedControllerMayPay(
                    new PayManaCost("{3}"),
                    List.of(new DealDamageToPlayersEffect(3, DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER)));
            var ctx = new TriggerContext.EnchantedPermanentTap(tappedPerm, player2Id);

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getTargetId()).isEqualTo(player2Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(aura.getId());
            assertThat(stackEntry.getEffectsToResolve().getFirst()).isSameAs(effect);
        }
    }

    // ===== ON_ENCHANTED_PERMANENT_TAPPED — DrawCardEffect =====

    @Nested
    @DisplayName("ON_ENCHANTED_PERMANENT_TAPPED — DrawCardEffect")
    class EnchantedPermanentTapDraw {

        @Test
        @DisplayName("puts triggered ability on stack for the aura's controller")
        void putsTriggeredAbilityOnStack() {
            Permanent aura = createPermanent("Betrayal");
            Permanent tappedPerm = createPermanent("Grizzly Bears");
            var effect = new DrawCardEffect(1);
            var ctx = new TriggerContext.EnchantedPermanentTap(tappedPerm, player2Id);

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Betrayal");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(aura.getId());
            assertThat(stackEntry.getEffectsToResolve().getFirst()).isSameAs(effect);
        }
    }

    // ===== ON_ENCHANTED_PERMANENT_TAPPED — PutCounterOnReferencedPermanentEffect =====

    @Nested
    @DisplayName("ON_ENCHANTED_PERMANENT_TAPPED — PutCounterOnReferencedPermanentEffect")
    class EnchantedPermanentTapCounter {

        @Test
        @DisplayName("puts triggered ability on stack carrying the aura as its source permanent")
        void putsTriggeredAbilityOnStack() {
            Permanent aura = createPermanent("Spirit Shackle");
            Permanent tappedPerm = createPermanent("Grizzly Bears");
            var effect = new PutCounterOnReferencedPermanentEffect(CounterType.MINUS_ZERO_MINUS_TWO);
            var ctx = new TriggerContext.EnchantedPermanentTap(tappedPerm, player2Id);

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Spirit Shackle");
            // Ability controlled by the aura's controller (CR 603.3b), not the tapped permanent's.
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(aura.getId());
            assertThat(stackEntry.getEffectsToResolve().getFirst()).isSameAs(effect);
        }
    }

    // ===== ON_ENCHANTED_PERMANENT_TAPPED — LoseLifeEffect (TARGET_PERMANENT_CONTROLLER) =====

    @Nested
    @DisplayName("ON_ENCHANTED_PERMANENT_TAPPED — LoseLifeEffect (TARGET_PERMANENT_CONTROLLER)")
    class EnchantedPermanentTapLoseLife {

        @Test
        @DisplayName("bakes the tapped land into targetId so its controller loses life at resolution")
        void putsTriggeredAbilityOnStack() {
            Permanent aura = createPermanent("Corrupted Roots");
            Permanent tappedPerm = createPermanent("Forest");
            var effect = new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER);
            var ctx = new TriggerContext.EnchantedPermanentTap(tappedPerm, player2Id);

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Corrupted Roots");
            // Ability controlled by the aura's controller (CR 603.3b), not the tapped land's.
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(aura.getId());
            // targetId is the tapped land; TARGET_PERMANENT_CONTROLLER re-derives its controller.
            assertThat(stackEntry.getTargetId()).isEqualTo(tappedPerm.getId());
            assertThat(stackEntry.getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("broadcasts trigger log message")
        void broadcastsTriggerLog() {
            Permanent aura = createPermanent("Corrupted Roots");
            Permanent tappedPerm = createPermanent("Plains");
            var effect = new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER);
            var ctx = new TriggerContext.EnchantedPermanentTap(tappedPerm, player2Id);

            registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, effect, ctx);

            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }
    }

    // ===== ON_OPPONENT_LOSES_LIFE — MillOpponentOnLifeLossEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_LOSES_LIFE — MillOpponentOnLifeLossEffect")
    class LifeLossMill {

        @Test
        @DisplayName("mills opponent for the amount of life lost and returns true")
        void millsOpponentForLifeLost() {
            Permanent mindcrank = createPermanent("Mindcrank");
            var effect = new MillOpponentOnLifeLossEffect();
            var ctx = new TriggerContext.LifeLoss(player2Id, 3);

            gd.playerIdToName.put(player2Id, "Player2");

            boolean result = registry.dispatch(
                    match(mindcrank, player1Id, effect),
                    EffectSlot.ON_OPPONENT_LOSES_LIFE, effect, ctx);

            assertThat(result).isTrue();
            verify(graveyardService).resolveMillPlayer(gd, player2Id, 3);
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }

        @Test
        @DisplayName("mills correct amount for 1 life lost (singular log message)")
        void millsSingularAmount() {
            Permanent mindcrank = createPermanent("Mindcrank");
            var effect = new MillOpponentOnLifeLossEffect();
            var ctx = new TriggerContext.LifeLoss(player2Id, 1);

            gd.playerIdToName.put(player2Id, "Player2");

            registry.dispatch(
                    match(mindcrank, player1Id, effect),
                    EffectSlot.ON_OPPONENT_LOSES_LIFE, effect, ctx);

            verify(graveyardService).resolveMillPlayer(gd, player2Id, 1);
        }

        @Test
        @DisplayName("broadcasts log message with player name and card count")
        void broadcastsLogMessage() {
            Permanent mindcrank = createPermanent("Mindcrank");
            var effect = new MillOpponentOnLifeLossEffect();
            var ctx = new TriggerContext.LifeLoss(player2Id, 5);

            gd.playerIdToName.put(player2Id, "Opponent");

            registry.dispatch(
                    match(mindcrank, player1Id, effect),
                    EffectSlot.ON_OPPONENT_LOSES_LIFE, effect, ctx);

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Mindcrank triggers — Opponent mills 5 cards.")));
        }
    }

    // ===== ON_CONTROLLER_GAINS_LIFE — PutCountersOnSourceEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_GAINS_LIFE — PutCountersOnSourceEffect")
    class LifeGainPutCounters {

        @Test
        @DisplayName("puts triggered ability on stack and returns true")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Ajani's Pridemate");
            var effect = new PutCountersOnSourceEffect(1, 1, 1);
            var ctx = new TriggerContext.LifeGain(player1Id, 3);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Ajani's Pridemate");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("stack entry includes the PutCountersOnSourceEffect")
        void stackEntryIncludesEffect() {
            Permanent perm = createPermanent("Ajani's Pridemate");
            var effect = new PutCountersOnSourceEffect(1, 1, 1);
            var ctx = new TriggerContext.LifeGain(player1Id, 3);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("broadcasts trigger log message")
        void broadcastsTriggerLog() {
            Permanent perm = createPermanent("Ajani's Pridemate");
            var effect = new PutCountersOnSourceEffect(1, 1, 1);
            var ctx = new TriggerContext.LifeGain(player1Id, 3);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_GAINS_LIFE — SurveilEffect")
    class LifeGainSurveil {

        @Test
        @DisplayName("puts surveil trigger on the stack")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Vanguard Seraph");
            var effect = new SurveilEffect(1);
            var ctx = new TriggerContext.LifeGain(player1Id, 3);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
            assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_GAINS_LIFE — CreateTokenEffect")
    class LifeGainCreateToken {

        @Test
        @DisplayName("puts token creation trigger on the stack")
        void putsTokenCreationTriggerOnStack() {
            Permanent perm = createPermanent("Cat Collector");
            var effect = CreateTokenEffect.whiteSoldier(1);
            var ctx = new TriggerContext.LifeGain(player1Id, 3);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        }
    }

    // ===== ON_CONTROLLER_GAINS_LIFE — PutCountersOnSelfEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_GAINS_LIFE — PutCountersOnSelfEffect")
    class LifeGainPutCountersOnSelf {

        @Test
        @DisplayName("puts triggered ability on stack and returns true")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Comforting Counsel");
            var effect = new PutCountersOnSelfEffect(CounterType.GROWTH);
            var ctx = new TriggerContext.LifeGain(player1Id, 3);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Comforting Counsel");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(stackEntry.getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("snapshots life gained for a dynamic counter amount")
        void snapshotsLifeGainedForDynamicAmount() {
            Permanent perm = createPermanent("Light of Promise");
            var effect = new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue());
            var ctx = new TriggerContext.LifeGain(player1Id, 3);

            when(amountEvaluationService.referencesEventValue(effect.amount())).thenReturn(true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            assertThat(gd.stack.getLast().getEventValue()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_GAINS_LIFE — UntapPermanentsEffect")
    class LifeGainUntapSelf {

        @Test
        @DisplayName("puts triggered ability on stack with the source permanent")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Famished Paladin");
            var effect = new UntapPermanentsEffect(TapUntapScope.SELF);
            var ctx = new TriggerContext.LifeGain(player1Id, 3);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEffectsToResolve()).containsExactly(effect);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(perm.getId());
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_GAINS_LIFE — PutCounterOnEachControlledPermanentEffect")
    class LifeGainPutCountersOnMatching {

        @Test
        @DisplayName("puts triggered ability on stack and returns true")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Blech, Loafing Pest");
            var effect = new PutCounterOnEachControlledPermanentEffect(
                    CounterType.PLUS_ONE_PLUS_ONE, 1,
                    new PermanentAnyOfPredicate(List.of(
                            new PermanentHasSubtypePredicate(CardSubtype.PEST),
                            new PermanentHasSubtypePredicate(CardSubtype.SPIDER)
                    )));
            var ctx = new TriggerContext.LifeGain(player1Id, 3);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Blech, Loafing Pest");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(stackEntry.getEffectsToResolve()).containsExactly(effect);
        }
    }

    // ===== ON_CONTROLLER_GAINS_LIFE — LoseLifeEffect(EventValue, TARGET_PLAYER) =====

    @Nested
    @DisplayName("ON_CONTROLLER_GAINS_LIFE — LoseLifeEffect(EventValue, TARGET_PLAYER)")
    class LifeGainOpponentLosesLife {

        @Test
        @DisplayName("puts triggered ability on stack targeting opponent with life loss equal to life gained")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Sanguine Bond");
            var effect = new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER);
            var ctx = new TriggerContext.LifeGain(player1Id, 4);

            when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Sanguine Bond");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getTargetId()).isEqualTo(player2Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("resolved effect keeps EventValue amount and snapshots life gained onto the entry")
        void resolvedEffectHasCorrectAmount() {
            Permanent perm = createPermanent("Sanguine Bond");
            var effect = new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER);
            var ctx = new TriggerContext.LifeGain(player1Id, 7);

            when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);
            when(amountEvaluationService.referencesEventValue(new EventValue())).thenReturn(true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            var entry = gd.stack.getLast();
            var resolved = (LoseLifeEffect) entry.getEffectsToResolve().getFirst();
            assertThat(resolved.amount()).isEqualTo(new EventValue());
            assertThat(entry.getEventValue()).isEqualTo(7);
        }

        @Test
        @DisplayName("broadcasts trigger log message")
        void broadcastsTriggerLog() {
            Permanent perm = createPermanent("Sanguine Bond");
            var effect = new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER);
            var ctx = new TriggerContext.LifeGain(player1Id, 4);

            when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect, ctx);

            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }
    }

    // ===== ON_CONTROLLER_LOSES_LIFE — SacrificePermanentsEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_LOSES_LIFE — SacrificePermanentsEffect")
    class ControllerLifeLossSacrifice {

        @Test
        @DisplayName("puts triggered ability on stack with the life-loss amount")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Lich's Tomb");
            var effect = new SacrificePermanentsEffect(
                    new EventValue(), new PermanentTruePredicate(), SacrificeRecipient.CONTROLLER);
            var ctx = new TriggerContext.LifeLoss(player1Id, 3);

            when(amountEvaluationService.referencesEventValue(new EventValue())).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_LOSES_LIFE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var entry = gd.stack.getLast();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(entry.getEventValue()).isEqualTo(3);
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        }
    }

    @Test
    @DisplayName("queues energy gain on controller life loss")
    void queuesEnergyGainOnControllerLifeLoss() {
        Permanent perm = createPermanent("Gonti's Machinations");
        var effect = new EnergyCountersEffect(1);
        var ctx = new TriggerContext.LifeLoss(player1Id, 2);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect),
                EffectSlot.ON_CONTROLLER_LOSES_LIFE, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        var entry = gd.stack.getLast();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getControllerId()).isEqualTo(player1Id);
        assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
        assertThat(entry.getEffectsToResolve()).containsExactly(effect);
    }

    // ===== ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE — BoostSelfEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE — BoostSelfEffect")
    class NoncombatDamageBoostSelf {

        @Test
        @DisplayName("puts triggered ability on stack and returns true")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Chandra's Spitfire");
            var effect = new BoostSelfEffect(3, 0);
            var ctx = new TriggerContext.NoncombatDamageToOpponent(player2Id);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Chandra's Spitfire");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("stack entry includes the BoostSelfEffect")
        void stackEntryIncludesEffect() {
            Permanent perm = createPermanent("Chandra's Spitfire");
            var effect = new BoostSelfEffect(3, 0);
            var ctx = new TriggerContext.NoncombatDamageToOpponent(player2Id);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE, effect, ctx);

            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("broadcasts trigger log message")
        void broadcastsTriggerLog() {
            Permanent perm = createPermanent("Chandra's Spitfire");
            var effect = new BoostSelfEffect(3, 0);
            var ctx = new TriggerContext.NoncombatDamageToOpponent(player2Id);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE, effect, ctx);

            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }
    }

    @Nested
    @DisplayName("ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE — BoostAllOwnCreaturesEffect")
    class NoncombatDamageBoostAllOwnCreatures {

        @Test
        @DisplayName("puts the team boost trigger on the stack")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Wildfire Elemental");
            var effect = new BoostAllOwnCreaturesEffect(1, 0);
            var ctx = new TriggerContext.NoncombatDamageToOpponent(player2Id);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getEffectsToResolve()).containsExactly(effect);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(perm.getId());
        }
    }

    @Nested
    @DisplayName("ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE — DrawCardEffect")
    class NoncombatDamageDraw {

        @Test
        @DisplayName("snapshots damage from a source controlled by the watcher")
        void snapshotsControlledSourceDamage() {
            Permanent perm = createPermanent("Niv-Mizzet, Visionary");
            var effect = new DrawCardEffect(new EventValue());
            var ctx = new TriggerContext.NoncombatDamageToOpponent(player2Id, player1Id, 2);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEventValue()).isEqualTo(2);
            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("does not trigger for a source controlled by another player")
        void ignoresOtherPlayersSource() {
            Permanent perm = createPermanent("Niv-Mizzet, Visionary");
            var effect = new DrawCardEffect(new EventValue());
            var ctx = new TriggerContext.NoncombatDamageToOpponent(player2Id, player2Id, 2);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }
}
