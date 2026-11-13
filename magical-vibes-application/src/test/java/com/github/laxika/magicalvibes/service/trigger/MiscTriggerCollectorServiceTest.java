package com.github.laxika.magicalvibes.service.trigger;

import static org.mockito.ArgumentMatchers.eq;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfCreatureCardInOpponentGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTimeCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaDrawXCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongTriggeringCards;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.SourceCardSuspended;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterThenMayBecomeCopyOfCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEqualToAmountEffect;
import com.github.laxika.magicalvibes.model.amount.TriggeringCardManaValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
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
    private ConditionEvaluationService conditionEvaluationService;

    @InjectMocks
    private MiscTriggerCollectorService sut;

    private TriggerCollectorRegistry registry;




    @Test
    @DisplayName("A card put into your graveyard: Planar Void's exile goes through the default, the card and its graveyard incarnation the entry's")
    void cardPutIntoOwnGraveyardIsTheEntrysTriggeringCard() {
        Permanent source = createPermanent("Planar Void");
        var effect = new com.github.laxika.magicalvibes.model.effect.ExileTriggeringCardFromGraveyardEffect();
        Card card = createCard("Discarded card");
        gd.graveyardEntryVersions.put(card.getId(), 3L);

        boolean collected = registry.dispatch(match(source, player1Id, effect),
                ZoneChangeWatch.ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new TriggerContext.CardPutIntoGraveyard(card, player1Id));

        assertThat(collected).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTriggeringCardId()).isEqualTo(card.getId());
            assertThat(entry.isTriggeringCardIncarnation(3L)).isTrue();
            assertThat(entry.isTriggeringCardIncarnation(4L)).isFalse();
        });
    }




    @Test
    @DisplayName("A nonblack card put into a graveyard: the card, its owner \"that player\", from a source with no permanent")
    void nonblackCardRecordsTheCardAndItsOwner() {
        Card plane = createCard("The Dark Barony");
        var effect = new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER);
        Card card = createCard("Grizzly Bears");
        gd.graveyardEntryVersions.put(card.getId(), 5L);

        boolean collected = registry.dispatch(new TriggerMatchContext(gd, null, player1Id, effect, plane),
                ZoneChangeWatch.ON_NONBLACK_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new TriggerContext.CardPutIntoGraveyard(card, player2Id));

        assertThat(collected).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getCard()).isSameAs(plane);
            assertThat(entry.getSourcePermanentId()).isNull();
            assertThat(entry.getTargetId()).isEqualTo(player2Id);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringCardId()).isEqualTo(card.getId());
            assertThat(entry.isTriggeringCardIncarnation(5L)).isTrue();
        });
    }

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
                    ZoneChangeWatch.ON_ALLY_PERMANENT_SACRIFICED, ctx);

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
                    ZoneChangeWatch.ON_ALLY_PERMANENT_SACRIFICED, ctx);

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
                    ZoneChangeWatch.ON_ALLY_PERMANENT_SACRIFICED, ctx);

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
                    ZoneChangeWatch.ON_ALLY_PERMANENT_SACRIFICED, ctx);

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
                    ZoneChangeWatch.ON_ALLY_PERMANENT_SACRIFICED, ctx);

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
                    ZoneChangeWatch.ON_ALLY_PERMANENT_SACRIFICED, ctx);

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
                    ZoneChangeWatch.ON_ALLY_PERMANENT_SACRIFICED, ctx);

            assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2Id);
        }
    }

    // ===== ON_ENCHANTED_PERMANENT_TAPPED — GivePoisonCountersEffect (ENCHANTED_PERMANENT_CONTROLLER) =====

    @Nested
    @DisplayName("ON_ALLY_PERMANENT_SACRIFICED - targeted trigger")
    class SacrificeTargetedConditional {

        @Test
        @DisplayName("queues target selection instead of putting an untargeted ability on the stack")
        void queuesTargetSelection() {
            Permanent perm = createPermanent("Fleeting Memories");
            Card clue = createCard("Clue");
            clue.setType(CardType.ARTIFACT);
            clue.setSubtypes(List.of(CardSubtype.CLUE));
            var effect = new MillEffect(3, MillRecipient.TARGET_PLAYER);
            var ctx = new TriggerContext.AllySacrificed(player1Id, clue);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    ZoneChangeWatch.ON_ALLY_PERMANENT_SACRIFICED, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class)).isTrue();
            var pending = gd.peekPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class);
            assertThat(pending.controllerId()).isEqualTo(player1Id);
            assertThat(pending.sourcePermanentId()).isEqualTo(perm.getId());
            assertThat(pending.effects()).containsExactly(effect);
        }

        @Test
        @DisplayName("a graveyard target is chosen with no X: the sacrificed card is the record's triggering card")
        void graveyardTargetChoiceReadsTheSacrificedCardFromTheRecord() {
            Permanent perm = createPermanent("Riveteers Ascendancy");
            Card sacrificed = createCard("Sacrificed creature");
            sacrificed.setType(CardType.CREATURE);
            sacrificed.setManaCost("{2}{B}");
            var effect = new MayEffect(ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.BATTLEFIELD)
                    .filter(new CardTypePredicate(CardType.CREATURE))
                    .dynamicMaxManaValue(new Sum(new TriggeringCardManaValue(), new Fixed(-1)))
                    .targetGraveyard(true)
                    .build(), "Return target creature card with lesser mana value?");
            var ctx = new TriggerContext.AllySacrificed(player1Id, sacrificed);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    ZoneChangeWatch.ON_ALLY_PERMANENT_SACRIFICED, ctx);

            assertThat(result).isTrue();
            var pending = gd.peekPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class);
            assertThat(pending.effects()).containsExactly(effect);
            assertThat(pending.xValue()).isZero();
            assertThat(gd.queuedTriggerRecord(pending).getXValue()).isZero();
            assertThat(gd.queuedTriggerRecord(pending).getTriggeringCardId()).isEqualTo(sacrificed.getId());
        }
    }

    @Nested
    @DisplayName("ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE")
    class PermanentCardPutIntoGraveyard {


        @Test
        @DisplayName("queues target selection for a targeted graveyard trigger")
        void queuesTargetSelectionForTargetedTrigger() {
            Card sourceCard = createCard("Measure of Wickedness");
            var effect = new TargetPlayerGainsControlOfSourceCreatureEffect();
            sourceCard.target(new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                    "Target must be an opponent"
            )).addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.ANOTHER_YOU_OWN),
                    effect);
            Permanent perm = new Permanent(sourceCard);
            var ctx = new TriggerContext.CardPutIntoGraveyard(
                    createCard("Spellbook"), player1Id);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    ZoneChangeWatch.ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            PermanentChoiceContext.SelfTriggeredAbilityTarget pending =
                    gd.peekPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class);
            assertThat(pending).isNotNull();
            assertThat(pending.effects()).containsExactly(effect);
            assertThat(pending.sourcePermanentId()).isEqualTo(perm.getId());
        }
    }

    // ===== ON_ENCHANTED_PERMANENT_TAPPED — ForcedCostOrElseEffect =====

    // ===== ON_ENCHANTED_PERMANENT_TAPPED — DrawCardEffect =====

    // ===== ON_ENCHANTED_PERMANENT_TAPPED — PutCounterOnReferencedPermanentEffect =====

    // ===== ON_ENCHANTED_PERMANENT_TAPPED — LoseLifeEffect (TARGET_PERMANENT_CONTROLLER) =====

    @Nested
    @DisplayName("ON_OPPONENT_LOSES_LIFE / ON_CONTROLLER_LOSES_LIFE — MillEffect(EventValue, TRIGGERING_PLAYER)")
    class LifeLossMill {

        private final MillEffect effect = new MillEffect(new EventValue(), MillRecipient.TRIGGERING_PLAYER);

        private void assertMillTriggerQueued(UUID millingPlayerId, int amount) {
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getLast();
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTargetId()).isEqualTo(millingPlayerId);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getEventValue()).isEqualTo(amount);
            verify(graveyardService, never()).resolveMillPlayer(any(), any(), anyInt());
        }
    }

    // ===== ON_CONTROLLER_GAINS_LIFE — PutCountersOnSourceEffect =====

    // ===== ON_CONTROLLER_GAINS_LIFE — PutCountersOnSelfEffect =====


    // ===== ON_CONTROLLER_LOSES_LIFE — SacrificePermanentsEffect =====


    @Test
    @DisplayName("gives a creature-card-put-into-an-opponent's-graveyard ability the card as its triggering card")
    void opponentGraveyardCreatureCardDefaultCarriesTheCard() {
        Permanent perm = createPermanent("Lazav, Dimir Mastermind");
        var effect = new MayEffect(new BecomeCopyOfCreatureCardInOpponentGraveyardEffect(),
                "Become a copy of that creature card?");
        Card bears = createCard("Grizzly Bears");
        var ctx = new TriggerContext.CreatureCardPutIntoGraveyard(bears, player2Id);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect),
                ZoneChangeWatch.ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTriggeringCardId()).isEqualTo(bears.getId());
            assertThat(entry.getTriggeringCards()).containsExactly(bears);
        });
    }

    @Test
    @DisplayName("Lorcan's \"pay life equal to its mana value\" goes on the stack as written with the card's graveyard incarnation")
    void opponentGraveyardCreatureCardMayPayLifeGoesOnTheStackAsWritten() {
        Permanent perm = createPermanent("Lorcan, Warlock Collector");
        var effect = new MayPayLifeEqualToAmountEffect(new TriggeringCardManaValue(),
                new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(false, true, null, 0, CardSubtype.WARLOCK),
                "Pay life equal to that card's mana value to return it to the battlefield?");
        Card bears = createCard("Grizzly Bears");
        gd.graveyardEntryVersions.put(bears.getId(), 4L);
        var ctx = new TriggerContext.CreatureCardPutIntoGraveyard(bears, player2Id);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect),
                ZoneChangeWatch.ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTriggeringCardId()).isEqualTo(bears.getId());
            assertThat(entry.getTriggeringCardGraveyardEntryVersion()).isEqualTo(4L);
            assertThat(entry.getTriggeringCards()).containsExactly(bears);
            assertThat(entry.isNonTargeting()).isTrue();
        });
    }



}
