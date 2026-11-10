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
import com.github.laxika.magicalvibes.model.filter.CardManaValueLessThanXPredicate;
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
    void enchantedTapDamagePreservesTriggeringPermanent() {
        Permanent aura = createPermanent("Damage aura");
        Permanent land = createPermanent("Tapped land");
        var effect = new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER);

        registry.dispatch(match(aura, player1Id, effect), TapWatch.ON_ENCHANTED_PERMANENT_TAPPED,
                new TriggerContext.EnchantedPermanentTap(land, player2Id));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTriggeringPermanentId()).isEqualTo(land.getId());
        assertThat(gd.stack.getFirst().getTriggeringPermanentControllerId()).isEqualTo(player2Id);
    }

    @Test
    void cardsLeavingGraveyardQueueTargetChoiceBeforeTrigger() {
        Permanent source = createPermanent("Hardened Academic");
        var effect = new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);

        boolean collected = registry.dispatch(match(source, player1Id, effect),
                ZoneChangeWatch.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD,
                new TriggerContext.ControllerCardsLeaveGraveyard(player1Id));

        assertThat(collected).isTrue();
        assertThat(gd.stack).isEmpty();
        var choice = gd.pollPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class);
        assertThat(choice).isNotNull();
        assertThat(choice.controllerId()).isEqualTo(player1Id);
        assertThat(choice.sourcePermanentId()).isEqualTo(source.getId());
    }

    @Test
    @DisplayName("Cards leaving the graveyard are the entry's triggering cards")
    void cardsLeavingGraveyardAreTheEntrysTriggeringCards() {
        Permanent source = createPermanent("Spirit of Resilience");
        var effect = new PutCounterThenMayBecomeCopyOfCardsEffect();
        Card first = createCard("First departed card");
        Card second = createCard("Second departed card");

        boolean collected = registry.dispatch(match(source, player1Id, effect),
                ZoneChangeWatch.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD,
                new TriggerContext.ControllerCardsLeaveGraveyard(player1Id, List.of(first, second)));

        assertThat(collected).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTriggeringCards()).containsExactly(first, second);
        });
    }

    @Test
    @DisplayName("Cards put into the graveyard from the library are the entry's triggering cards")
    void cardsPutIntoGraveyardFromLibraryAreTheEntrysTriggeringCards() {
        Permanent source = createPermanent("Polluted Cistern");
        var effect = new LoseLifeEffect(new CardTypesAmongTriggeringCards(), LoseLifeRecipient.EACH_OPPONENT);
        Card first = createCard("First milled card");
        Card second = createCard("Second milled card");

        boolean collected = registry.dispatch(match(source, player1Id, effect),
                ZoneChangeWatch.ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY,
                new TriggerContext.CardsPutIntoGraveyardFromLibrary(player1Id, 2, List.of(first, second)));

        assertThat(collected).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTriggeringCards()).containsExactly(first, second);
        });
    }

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
    @DisplayName("A card put into an opponent's graveyard is the entry's triggering card, the graveyard's owner \"that player\"")
    void cardPutIntoOpponentGraveyardIsTheEntrysTriggeringCard() {
        Permanent source = createPermanent("Planar Void");
        var effect = new com.github.laxika.magicalvibes.model.effect.ExileTriggeringCardFromGraveyardEffect();
        Card card = createCard("Milled card");
        gd.graveyardEntryVersions.put(card.getId(), 2L);

        boolean collected = registry.dispatch(match(source, player1Id, effect),
                ZoneChangeWatch.ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE,
                new TriggerContext.CardPutIntoGraveyard(card, player2Id));

        assertThat(collected).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTargetId()).isEqualTo(player2Id);
            assertThat(entry.getTriggeringCardId()).isEqualTo(card.getId());
            assertThat(entry.isTriggeringCardIncarnation(2L)).isTrue();
        });
    }

    @Test
    @DisplayName("Kaya's token-target choice carries the exiled creature cards as its triggering cards, the effect unchanged")
    void kayaTargetChoiceCarriesTheExiledCardsAsTriggeringCards() {
        Permanent source = createPermanent("Kaya, Spirits' Justice");
        var effect = new com.github.laxika.magicalvibes.model.effect.KayaSpiritsJusticeEffect();
        Card first = createCard("First exiled creature card");
        Card second = createCard("Second exiled creature card");

        boolean collected = registry.dispatch(match(source, player1Id, effect),
                ZoneChangeWatch.ON_CONTROLLER_CREATURES_OR_CREATURE_CARDS_EXILED,
                new TriggerContext.ControllerCreaturesOrCreatureCardsExiled(player1Id, 2, List.of(first, second)));

        assertThat(collected).isTrue();
        var choice = gd.pollPendingInteraction(PermanentChoiceContext.DrawTriggerPermanentTarget.class);
        assertThat(choice).isNotNull();
        assertThat(choice.effects()).containsExactly(effect);
        assertThat(choice.triggeringCards()).containsExactly(first, second);
    }

    @Test
    @DisplayName("An opponent's mill: the count is the event value, the milling player the non-targeting \"that player\"")
    void opponentMillRecordsCountAndPlayer() {
        Permanent source = createPermanent("Lo and Li, Royal Advisors");
        var effect = new PutCounterOnEachControlledPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1,
                new PermanentHasSubtypePredicate(CardSubtype.ADVISOR));

        boolean collected = registry.dispatch(match(source, player1Id, effect), ZoneChangeWatch.ON_OPPONENT_MILLS,
                new TriggerContext.Mill(player2Id, 3));

        assertThat(collected).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getEventValue()).isEqualTo(3);
            assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2Id);
            assertThat(entry.getTargetId()).isEqualTo(player2Id);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
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

    @Test
    @DisplayName("A creature exiled from the battlefield: \"that card\" with its exile incarnation, the creature as it last existed")
    void exiledCreatureRecordsTheCardAndTheCreature() {
        Permanent source = createPermanent("Athreos, Shroud-Veiled");
        var effect = new com.github.laxika.magicalvibes.model.effect.ReturnTriggeringPermanentToBattlefieldEffect(
                com.github.laxika.magicalvibes.model.Zone.EXILE);
        Permanent exiled = createPermanent("Grizzly Bears");
        gd.exileEntryVersions.put(exiled.getCard().getId(), 4L);

        boolean collected = registry.dispatch(match(source, player1Id, effect),
                ZoneChangeWatch.ON_ANY_CREATURE_EXILED_FROM_BATTLEFIELD,
                new TriggerContext.CreatureExiledFromBattlefield(exiled, player2Id, 2));

        assertThat(collected).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getTriggeringCardId()).isEqualTo(exiled.getCard().getId());
            assertThat(entry.getTriggeringCardExileEntryVersion()).isEqualTo(4L);
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(exiled.getId());
            assertThat(entry.getTriggeringPermanentSnapshot()).isNotNull();
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player2Id);
            assertThat(entry.getEventValue()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Relic Bind's \"choose one\" goes on the stack with the enchanted-tap record, its modes' targets its own")
    void enchantedPermanentTapModalGoesOnTheStackWithTheRecord() {
        Permanent aura = createPermanent("Modal aura");
        Permanent tapped = createPermanent("Artifact");
        var effect = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Gain", new TargetPlayerGainsLifeEffect(1)),
                new ChooseOneEffect.ChooseOneOption("Damage", new DealDamageToTargetPlayerOrPlaneswalkerEffect(1))));

        registry.dispatch(match(aura, player1Id, effect), TapWatch.ON_ENCHANTED_PERMANENT_TAPPED,
                new TriggerContext.EnchantedPermanentTap(tapped, player2Id));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class)).isFalse();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getSourcePermanentId()).isEqualTo(aura.getId());
            assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(tapped.getId());
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player2Id);
            assertThat(entry.getTargetId()).isNull();
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
        TriggerCollectorRegistry.scanBean(new LifeGainTriggerCollectorService(gameLogService), registry);
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

    @Test
    @DisplayName("life-gain pay-X draw trigger snapshots the life gained on the stack entry")
    void lifeGainPayXDrawTriggerSnapshotsLifeGained() {
        Permanent perm = createPermanent("Well of Lost Dreams");
        var effect = new PayXManaDrawXCardsEffect();
        var ctx = new TriggerContext.LifeGain(player1Id, 3);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), LifeWatch.ON_CONTROLLER_GAINS_LIFE, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getLast().getEventValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("life-gain may-pay trigger keeps the optional payment on the stack, with the life gained")
    void lifeGainMayPayTriggerKeepsOptionalPayment() {
        Permanent perm = createPermanent("Dawn of Hope");
        var effect = new MayPayManaEffect("{2}", new DrawCardEffect(1), "Pay {2} to draw a card?");
        var ctx = new TriggerContext.LifeGain(player1Id, 1);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), LifeWatch.ON_CONTROLLER_GAINS_LIFE, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getLast().getEventValue()).isEqualTo(1);
    }

    @Test
    @DisplayName("life-gain \"target opponent loses that much life\" queues its target choice with the life gained")
    void lifeGainTargetOpponentQueuesItsTargetChoice() {
        Card card = createCard("Sanguine Bond");
        var effect = new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER);
        card.target(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"))
                .addTrigger(Triggers.youGainLife(), effect);
        Permanent perm = new Permanent(card);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), LifeWatch.ON_CONTROLLER_GAINS_LIFE,
                new TriggerContext.LifeGain(player1Id, 4));

        assertThat(result).isTrue();
        assertThat(gd.stack).isEmpty();
        var choice = gd.pollPendingInteraction(PermanentChoiceContext.LifeGainTriggerAnyTarget.class);
        assertThat(choice).isNotNull();
        assertThat(choice.effects()).containsExactly(effect);
        assertThat(choice.sourcePermanentId()).isEqualTo(perm.getId());
        assertThat(choice.eventValue()).isEqualTo(4);
    }

    @Test
    @DisplayName("life-gain \"each opponent loses 1 life\" goes on the stack as written, with the life gained")
    void lifeGainEachOpponentLosesLifeGoesOnTheStack() {
        Permanent perm = createPermanent("Cliffhaven Vampire");
        var effect = new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), LifeWatch.ON_CONTROLLER_GAINS_LIFE,
                new TriggerContext.LifeGain(player1Id, 2));

        assertThat(result).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getEventValue()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("targeted life-gain sequence queues target selection")
    void targetedLifeGainSequenceQueuesTargetSelection() {
        Card card = createCard("Spider-Man, Peter Parker");
        var effect = SequenceEffect.of(
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET));
        card.target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.youGainLife(), effect);
        Permanent perm = new Permanent(card);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect),
                LifeWatch.ON_CONTROLLER_GAINS_LIFE,
                new TriggerContext.LifeGain(player1Id, 3));

        assertThat(result).isTrue();
        assertThat(gd.stack).isEmpty();
        var choice = gd.pollPendingInteraction(PermanentChoiceContext.LifeGainTriggerAnyTarget.class);
        assertThat(choice).isNotNull();
        assertThat(choice.effects()).containsExactly(effect);
        assertThat(choice.sourcePermanentId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("targeted life-gain counter trigger preserves the gained amount")
    void targetedLifeGainCounterTriggerPreservesEventValue() {
        Card card = createCard("Treebeard, Gracious Host");
        var effect = new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue());
        card.target(TargetFilters.creature())
                .addTrigger(Triggers.youGainLife(), effect);
        Permanent perm = new Permanent(card);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect),
                LifeWatch.ON_CONTROLLER_GAINS_LIFE,
                new TriggerContext.LifeGain(player1Id, 3));

        assertThat(result).isTrue();
        var choice = gd.pollPendingInteraction(PermanentChoiceContext.LifeGainTriggerAnyTarget.class);
        assertThat(choice).isNotNull();
        assertThat(choice.eventValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("opponent life-loss default records the life lost, the losing player and the source, whatever the effects")
    void opponentLifeLossDefaultRecordsTheEvent() {
        Permanent perm = createPermanent("Ob Nixilis, Captive Kingpin");
        var effect = SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new ExileTopCardsMayPlayUntilNextEndStepEffect(1));

        assertThat(registry.dispatch(
                match(perm, player1Id, effect),
                LifeWatch.ON_OPPONENT_LOSES_LIFE,
                new TriggerContext.LifeLoss(player2Id, 1))).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getEventValue()).isEqualTo(1);
            assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2Id);
            assertThat(entry.getTargetId()).isEqualTo(player2Id);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
        });
    }

    @Test
    @DisplayName("creature card from any library queues the triggered ability")
    void creatureCardFromAnyLibraryQueuesTriggeredAbility() {
        Permanent perm = createPermanent("Dreadhound");
        var effect = new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT);
        var ctx = new TriggerContext.CreatureCardPutIntoGraveyardFromLibrary(
                createCard("Grizzly Bears"), player2Id);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect),
                ZoneChangeWatch.ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("surveil default trigger queues the original effect")
    void surveilDefaultTriggerQueuesOriginalEffect() {
        Permanent perm = createPermanent("Disinformation Campaign");
        var effect = ReturnToHandEffect.self();
        var ctx = new TriggerContext.Surveil(player1Id, true);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), LibraryWatch.ON_CONTROLLER_SURVEILS, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
        assertThat(gd.stack.getLast().getTriggeringPlayerId()).isEqualTo(player1Id);
        assertThat(gd.stack.getLast().getSourcePermanentSnapshot().getId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("an opponent's surveil records that player as the non-targeting target of a player effect")
    void opponentSurveilRecordsThatPlayer() {
        Permanent perm = createPermanent("River Song");
        var effect = new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER);
        var ctx = new TriggerContext.Surveil(player2Id, true);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), LibraryWatch.ON_OPPONENT_SURVEILS, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getTargetId()).isEqualTo(player2Id);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2Id);
        });
    }

    @Test
    @DisplayName("\"whenever you untap one or more permanents during your untap step\" records that many whatever the effect")
    void untapStepDefaultRecordsTheCount() {
        Permanent perm = createPermanent("The Millennium Calendar");
        var effect = new DrawCardEffect(1);

        boolean result = registry.dispatch(match(perm, player1Id, effect),
                TapWatch.ON_CONTROLLER_UNTAPS_DURING_UNTAP_STEP, new TriggerContext.UntapStep(3));

        assertThat(result).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getEventValue()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("crewed trigger queues a targeted effect for target selection")
    void crewedTargetedTriggerQueuesTargetSelection() {
        Card sourceCard = createCard("Mobilizer Mech");
        var effect = new com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect(
                null, null, List.of(), java.util.Set.of(), null, java.util.Set.of(),
                com.github.laxika.magicalvibes.model.effect.GrantScope.TARGET,
                com.github.laxika.magicalvibes.model.effect.EffectDuration.UNTIL_END_OF_TURN, null);
        sourceCard.addTrigger(Triggers.becomesCrewed(), effect);
        Permanent perm = new Permanent(sourceCard);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect),
                MiscWatch.ON_SELF_BECOMES_CREWED,
                new TriggerContext.SelfBecomesCrewed(player1Id));

        assertThat(result).isTrue();
        assertThat(gd.stack).isEmpty();
        PermanentChoiceContext.SelfTriggeredAbilityTarget pending =
                gd.peekPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class);
        assertThat(pending).isNotNull();
        assertThat(pending.sourcePermanentId()).isEqualTo(perm.getId());
        assertThat(pending.sourcePermanentSnapshot()).isNotNull();
        assertThat(pending.effects()).containsExactly(effect);
    }

    @Test
    @DisplayName("surveil mark-on-acceptance trigger keeps its marker on the stack entry")
    void surveilMarkOnAcceptanceTriggerKeepsMarker() {
        Permanent perm = createPermanent("Planetarium of Wan Shi Tong");
        var effect = new DrawCardEffect(1);
        var ctx = new TriggerContext.Surveil(player1Id, true);

        boolean result = registry.dispatch(
                new TriggerMatchContext(gd, perm, player1Id, List.of(effect), perm.getCard(), null, true),
                LibraryWatch.ON_CONTROLLER_SURVEILS, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.isMarkSourceOncePerTurnOnAcceptance()).isTrue();
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        });
    }

    @Test
    @DisplayName("controller life-gain intervening-if trigger queues the conditional, checked again as it resolves")
    void controllerLifeGainConditionalTriggerQueuesTheConditional() {
        Permanent perm = createPermanent("Vampire Scrivener");
        var effect = new ConditionalEffect(new ControllerTurn(), new PutCountersOnSourceEffect(1, 1, 1));

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), LifeWatch.ON_CONTROLLER_GAINS_LIFE,
                new TriggerContext.LifeGain(player1Id, 3));

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getLast().getEventValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("controller life-loss intervening-if trigger queues the conditional, checked again as it resolves")
    void controllerLifeLossConditionalTriggerQueuesTheConditional() {
        Permanent perm = createPermanent("Vampire Scrivener");
        var effect = new ConditionalEffect(new ControllerTurn(), new PutCountersOnSourceEffect(1, 1, 1));

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), LifeWatch.ON_CONTROLLER_LOSES_LIFE,
                new TriggerContext.LifeLoss(player1Id, 2));

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getLast().getEventValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("opponent life-loss default trigger queues the effect with the life lost as its event value")
    void opponentLifeLossDefaultTriggerQueuesTheEffect() {
        Permanent perm = createPermanent("Kefka, Ruler of Ruin");
        var effect = new DrawCardEffect(new EventValue());

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), LifeWatch.ON_OPPONENT_LOSES_LIFE,
                new TriggerContext.LifeLoss(player2Id, 4));

        assertThat(result).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getEventValue()).isEqualTo(4);
            assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
        });
    }

    @Test
    @DisplayName("energy-gain default: a targeted ability's choice carries the player and the energy gained")
    void energyGainDefaultTargetChoiceCarriesTheRecord() {
        Permanent perm = createPermanent("Fabrication Module");
        var effect = new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);
        var ctx = new TriggerContext.EnergyGain(player1Id, 2);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), CounterWatch.ON_CONTROLLER_GETS_ENERGY, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).isEmpty();
        var pending = gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
        assertThat(pending).isNotNull();
        assertThat(pending.effects()).containsExactly(effect);
        assertThat(pending.controllerId()).isEqualTo(player1Id);
        assertThat(pending.sourcePermanentId()).isEqualTo(perm.getId());
        assertThat(pending.sourcePermanentSnapshot()).isNotNull();
        assertThat(pending.triggeringPlayerId()).isEqualTo(player1Id);
        assertThat(pending.eventValue()).isEqualTo(2);
    }

    /**
     * The defaults of run 17z13e7's watches: each records the event whatever the effect is
     * ({@code MiscTriggerCollectorService.queueEventAbility}).
     */
    @Nested
    @DisplayName("Player, counter and control events are recorded whatever the effects are")
    class EventRecords {

        @Test
        @DisplayName("Energy: an untargeted ability records the player and the energy gained")
        void energyGainRecordsThePlayerAndTheAmount() {
            Permanent perm = createPermanent("Territorial Gorger");
            var effect = new BoostSelfEffect(2, 2);

            registry.dispatch(match(perm, player1Id, effect), CounterWatch.ON_CONTROLLER_GETS_ENERGY,
                    new TriggerContext.EnergyGain(player1Id, 3));

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(effect);
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player1Id);
                assertThat(entry.getEventValue()).isEqualTo(3);
                assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
                assertThat(entry.getTargetId()).isNull();
            });
        }

        @Test
        @DisplayName("Collect evidence, the first investigation and a foretell record the acting player")
        void playerActionsRecordThePlayer() {
            Permanent perm = createPermanent("Surveillance Monitor");
            var token = CreateTokenEffect.ofClueToken(1);
            Card foretold = createCard("Foretold card");

            registry.dispatch(match(perm, player1Id, token), ActionWatch.ON_CONTROLLER_COLLECTS_EVIDENCE,
                    new TriggerContext.CollectEvidence(player1Id));
            registry.dispatch(match(perm, player1Id, token), ActionWatch.ON_CONTROLLER_INVESTIGATES,
                    new TriggerContext.Investigate(player1Id));
            var boost = new BoostSelfEffect(2, 0);
            registry.dispatch(match(perm, player1Id, boost), ActionWatch.ON_CONTROLLER_FORETELLS,
                    new TriggerContext.Foretell(player1Id, foretold));

            assertThat(gd.stack).hasSize(3).allSatisfy(entry -> {
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player1Id);
                assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
                assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
            });
            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(boost);
            assertThat(gd.stack.getLast().getTriggeringCardId()).isEqualTo(foretold.getId());
        }

        @Test
        @DisplayName("Tax payment: the paying opponent is \"that player\" and the mana paid the event value")
        void taxPaymentRecordsThePayerAndTheManaPaid() {
            Permanent perm = createPermanent("Tax Taker");
            var effect = CreateTokenEffect.ofTreasureToken(new EventValue());

            registry.dispatch(match(perm, player1Id, effect), ActionWatch.ON_OPPONENT_PAYS_TAX,
                    new TriggerContext.TaxPayment(player2Id, player1Id, 3));

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(effect);
                assertThat(entry.getEventValue()).isEqualTo(3);
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2Id);
                assertThat(entry.getTargetId()).isEqualTo(player2Id);
                assertThat(entry.isNonTargeting()).isTrue();
                assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
            });
        }

        @Test
        @DisplayName("Counters put: the target choice carries the placing player and the count")
        void countersPlacedCarriesTheCountAsTheEventValue() {
            Permanent watcher = createPermanent("All Will Be One");
            var damage = new DealDamageToAnyTargetEffect(new EventValue());

            boolean result = registry.dispatch(match(watcher, player1Id, damage),
                    CounterWatch.ON_YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER,
                    new TriggerContext.CountersPlaced(player1Id, 4));

            assertThat(result).isTrue();
            var pending = gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(pending.effects()).containsExactly(damage);
            assertThat(pending.eventValue()).isEqualTo(4);
            assertThat(pending.triggeringPlayerId()).isEqualTo(player1Id);
            assertThat(pending.sourcePermanentSnapshot()).isNotNull();
        }

        @Test
        @DisplayName("Lore counter: the target choice carries the Saga as the triggering permanent")
        void loreCounterRecordsTheSaga() {
            Permanent sigurd = createPermanent("Sigurd");
            Permanent saga = createPermanent("Saga");
            var effect = new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);

            registry.dispatch(match(sigurd, player1Id, effect), CounterWatch.ON_YOU_PUT_LORE_COUNTERS_ON_SAGA,
                    new TriggerContext.LoreCounterPlaced(saga, player1Id));

            var pending = gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(pending.triggeringPermanentId()).isEqualTo(saga.getId());
            assertThat(pending.triggeringPermanentControllerId()).isEqualTo(player1Id);
            assertThat(pending.triggeringPlayerId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("Loyalty counters removed: \"target opponent or planeswalker\" with the count removed")
        void loyaltyCountersRemovedCarriesTheCountAndTheOpponentFilter() {
            Permanent chandra = createPermanent("Chandra");
            var damage = new DealDamageToTargetPlayerOrPlaneswalkerEffect(new EventValue(), PlayerRelation.OPPONENT);

            registry.dispatch(match(chandra, player1Id, damage), CounterWatch.ON_SELF_LOYALTY_COUNTERS_REMOVED,
                    new TriggerContext.LoyaltyCountersRemoved(chandra, 2));

            var pending = gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(pending.effects()).containsExactly(damage);
            assertThat(pending.eventValue()).isEqualTo(2);
            assertThat(pending.triggeringPermanentId()).isEqualTo(chandra.getId());
            assertThat(pending.targetFilter()).isInstanceOf(AnyTargetPredicateTargetFilter.class);
        }

        @Test
        @DisplayName("Counters removed from a permanent you control: the permanent and the count")
        void countersRemovedRecordsThePermanent() {
            Permanent armorer = createPermanent("Cloudsculpt Armorer");
            Permanent shielded = createPermanent("Shielded creature");
            var seek = new SeekEffect(new CardTypePredicate(CardType.CREATURE));

            registry.dispatch(match(armorer, player1Id, seek), CounterWatch.ON_ALLY_COUNTERS_REMOVED_FROM_PERMANENT,
                    new TriggerContext.CountersRemovedFromPermanent(shielded, 1));

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(seek);
                assertThat(entry.getTriggeringPermanentId()).isEqualTo(shielded.getId());
                assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player1Id);
                assertThat(entry.getEventValue()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("Control change: the permanent and its new controller as \"that player\"")
        void controlChangeRecordsThePermanentAndTheNewController() {
            Permanent zidane = createPermanent("Zidane");
            Permanent stolen = createPermanent("Stolen permanent");
            var effect = CreateTokenEffect.ofTreasureToken(1);

            registry.dispatch(match(zidane, player1Id, effect), MiscWatch.ON_OPPONENT_GAINS_CONTROL_OF_YOUR_PERMANENT,
                    new TriggerContext.PermanentControlChanged(stolen, player1Id, player2Id));

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getTriggeringPermanentId()).isEqualTo(stolen.getId());
                assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player2Id);
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2Id);
                assertThat(entry.getTargetId()).isEqualTo(player2Id);
                assertThat(entry.isNonTargeting()).isTrue();
            });
        }

        @Test
        @DisplayName("Mana ability resolved: the mana produced is the event value")
        void manaAbilityResolvedRecordsTheManaProduced() {
            Permanent creature = createPermanent("Elf");
            var effect = new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue());

            registry.dispatch(match(creature, player1Id, effect), SpellAbilityWatch.ON_SELF_MANA_ABILITY_RESOLVES,
                    new TriggerContext.ManaAbilityResolved(player1Id, 2));

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(effect);
                assertThat(entry.getEventValue()).isEqualTo(2);
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player1Id);
                assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
            });
        }
    }

    @Test
    @DisplayName("proliferate trigger queues the bundled effect")
    void proliferateTriggerQueuesBundledEffect() {
        Permanent perm = createPermanent("Scheming Aspirant");
        var effect = new SequenceEffect(List.of(
                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(2)));
        var ctx = new TriggerContext.Proliferate(player1Id);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), CounterWatch.ON_CONTROLLER_PROLIFERATES, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
    }

    @Test
    void conditionalProliferateTriggerQueuesWithItsInterveningIfToBeCheckedAsItResolves() {
        Permanent perm = createPermanent("Contagion Dispenser");
        var effect = new ConditionalEffect(new ControllerTurn(), new GainLifeEffect(1));

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), CounterWatch.ON_CONTROLLER_PROLIFERATES,
                new TriggerContext.Proliferate(player1Id));

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
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
        @DisplayName("a graveyard target is chosen with the sacrificed permanent's mana value as X, the effect unchanged")
        void graveyardTargetChoiceHasTheSacrificedManaValueAsX() {
            Permanent perm = createPermanent("Riveteers Ascendancy");
            Card sacrificed = createCard("Sacrificed creature");
            sacrificed.setType(CardType.CREATURE);
            sacrificed.setManaCost("{2}{B}");
            var effect = new MayEffect(ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.BATTLEFIELD)
                    .filter(new CardAllOfPredicate(List.of(
                            new CardTypePredicate(CardType.CREATURE), new CardManaValueLessThanXPredicate())))
                    .targetGraveyard(true)
                    .build(), "Return target creature card with lesser mana value?");
            var ctx = new TriggerContext.AllySacrificed(player1Id, sacrificed);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    ZoneChangeWatch.ON_ALLY_PERMANENT_SACRIFICED, ctx);

            assertThat(result).isTrue();
            var pending = gd.peekPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class);
            assertThat(pending.effects()).containsExactly(effect);
            assertThat(pending.xValue()).isEqualTo(3);
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
                    ZoneChangeWatch.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, ctx);

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

    @Nested
    @DisplayName("ON_ENCHANTED_PERMANENT_TAPPED — GivePoisonCountersEffect (TRIGGERING_PERMANENT_CONTROLLER)")
    class EnchantedPermanentTapPoison {

        @Test
        @DisplayName("puts triggered ability on stack with resolved effect containing tapped permanent's controller")
        void putsTriggeredAbilityOnStack() {
            Permanent aura = createPermanent("Relic Putrescence");
            Permanent tappedPerm = createPermanent("Sol Ring");
            var effect = new GivePoisonCountersEffect(1, PoisonRecipient.TRIGGERING_PERMANENT_CONTROLLER);
            var ctx = new TriggerContext.EnchantedPermanentTap(tappedPerm, player2Id);

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    TapWatch.ON_ENCHANTED_PERMANENT_TAPPED, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Relic Putrescence");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(aura.getId());
        }

        @Test
        @DisplayName("the effect goes on the stack as written, the tapped permanent its triggering permanent")
        void effectGoesOnTheStackAsWrittenWithTheTappedPermanent() {
            Permanent aura = createPermanent("Relic Putrescence");
            Permanent tappedPerm = createPermanent("Sol Ring");
            var effect = new GivePoisonCountersEffect(1, PoisonRecipient.TRIGGERING_PERMANENT_CONTROLLER);
            var ctx = new TriggerContext.EnchantedPermanentTap(tappedPerm, player2Id);

            registry.dispatch(
                    match(aura, player1Id, effect),
                    TapWatch.ON_ENCHANTED_PERMANENT_TAPPED, ctx);

            StackEntry entry = gd.stack.getLast();
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTargetId()).isEqualTo(player2Id);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(tappedPerm.getId());
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("broadcasts trigger log message")
        void broadcastsTriggerLog() {
            Permanent aura = createPermanent("Relic Putrescence");
            Permanent tappedPerm = createPermanent("Sol Ring");
            var effect = new GivePoisonCountersEffect(1, PoisonRecipient.TRIGGERING_PERMANENT_CONTROLLER);
            var ctx = new TriggerContext.EnchantedPermanentTap(tappedPerm, player2Id);

            registry.dispatch(
                    match(aura, player1Id, effect),
                    TapWatch.ON_ENCHANTED_PERMANENT_TAPPED, ctx);

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
                    TapWatch.ON_ENCHANTED_PERMANENT_TAPPED, ctx);

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
                    TapWatch.ON_ENCHANTED_PERMANENT_TAPPED, ctx);

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
                    TapWatch.ON_ENCHANTED_PERMANENT_TAPPED, ctx);

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
    @DisplayName("ON_ENCHANTED_PERMANENT_TAPPED — LoseLifeEffect (TRIGGERING_PERMANENT_CONTROLLER)")
    class EnchantedPermanentTapLoseLife {

        @Test
        @DisplayName("records the tapped land and its controller, which the loss reads as it resolves")
        void putsTriggeredAbilityOnStack() {
            Permanent aura = createPermanent("Corrupted Roots");
            Permanent tappedPerm = createPermanent("Forest");
            var effect = new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER);
            var ctx = new TriggerContext.EnchantedPermanentTap(tappedPerm, player2Id);

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    TapWatch.ON_ENCHANTED_PERMANENT_TAPPED, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Corrupted Roots");
            // Ability controlled by the aura's controller (CR 603.3b), not the tapped land's.
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(aura.getId());
            assertThat(stackEntry.getTriggeringPermanentId()).isEqualTo(tappedPerm.getId());
            assertThat(stackEntry.getTriggeringPermanentControllerId()).isEqualTo(player2Id);
            assertThat(stackEntry.getTargetId()).isEqualTo(player2Id);
            assertThat(stackEntry.getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("broadcasts trigger log message")
        void broadcastsTriggerLog() {
            Permanent aura = createPermanent("Corrupted Roots");
            Permanent tappedPerm = createPermanent("Plains");
            var effect = new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER);
            var ctx = new TriggerContext.EnchantedPermanentTap(tappedPerm, player2Id);

            registry.dispatch(
                    match(aura, player1Id, effect),
                    TapWatch.ON_ENCHANTED_PERMANENT_TAPPED, ctx);

            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }
    }

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

        @Test
        @DisplayName("the controller is \"that player\" when dispatched from the controller life-loss watch")
        void millsControllerForLifeLost() {
            Permanent master = createPermanent("The Master of Lake-town");
            var ctx = new TriggerContext.LifeLoss(player1Id, 2);

            gd.playerIdToName.put(player1Id, "Player1");

            boolean result = registry.dispatch(
                    match(master, player1Id, effect),
                    LifeWatch.ON_CONTROLLER_LOSES_LIFE, ctx);

            assertThat(result).isTrue();
            assertMillTriggerQueued(player1Id, 2);
        }

        @Test
        @DisplayName("the opponent who lost life is \"that player\" and the life lost the event value")
        void millsOpponentForLifeLost() {
            Permanent mindcrank = createPermanent("Mindcrank");
            var ctx = new TriggerContext.LifeLoss(player2Id, 3);

            gd.playerIdToName.put(player2Id, "Player2");

            boolean result = registry.dispatch(
                    match(mindcrank, player1Id, effect),
                    LifeWatch.ON_OPPONENT_LOSES_LIFE, ctx);

            assertThat(result).isTrue();
            assertMillTriggerQueued(player2Id, 3);
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
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
                    LifeWatch.ON_CONTROLLER_GAINS_LIFE, ctx);

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
                    LifeWatch.ON_CONTROLLER_GAINS_LIFE, ctx);

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
                    LifeWatch.ON_CONTROLLER_GAINS_LIFE, ctx);

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
                    LifeWatch.ON_CONTROLLER_GAINS_LIFE, ctx);

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
                    LifeWatch.ON_CONTROLLER_GAINS_LIFE, ctx);

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
                    LifeWatch.ON_CONTROLLER_GAINS_LIFE, ctx);

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

            registry.dispatch(
                    match(perm, player1Id, effect),
                    LifeWatch.ON_CONTROLLER_GAINS_LIFE, ctx);

            assertThat(gd.stack.getLast().getEventValue()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_GAINS_LIFE — GrantKeywordEffect")
    class LifeGainGrantKeyword {

        @Test
        @DisplayName("puts keyword-grant trigger on the stack")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Kalastria Nightwatch");
            var effect = new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF);
            var ctx = new TriggerContext.LifeGain(player1Id, 3);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    LifeWatch.ON_CONTROLLER_GAINS_LIFE, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Kalastria Nightwatch");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(stackEntry.getEffectsToResolve()).containsExactly(effect);
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
                    LifeWatch.ON_CONTROLLER_GAINS_LIFE, ctx);

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
                    LifeWatch.ON_CONTROLLER_GAINS_LIFE, ctx);

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

    @Test
    @DisplayName("Nykthos Paragon's \"you may\" goes on the stack as written with the life gained, once per turn on acceptance")
    void nykthosParagonLifeGainTriggerIsOptionalAndOncePerTurn() {
        Permanent perm = createPermanent("Nykthos Paragon");
        var effect = new MayEffect(new PutCounterOnEachControlledPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE,
                new EventValue(), new PermanentIsCreaturePredicate()), "Put that many +1/+1 counters?");
        var ctx = new TriggerContext.LifeGain(player1Id, 3);

        boolean result = registry.dispatch(
                new TriggerMatchContext(gd, perm, player1Id, List.of(effect), perm.getCard(), null, true),
                LifeWatch.ON_CONTROLLER_GAINS_LIFE, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getLast().getEventValue()).isEqualTo(3);
        assertThat(gd.stack.getLast().isMarkSourceOncePerTurnOnAcceptance()).isTrue();
        assertThat(gd.oncePerTurnTriggersFiredThisTurn).doesNotContain(perm.getId());

        assertThat(registry.dispatch(
                new TriggerMatchContext(gd, perm, player1Id, List.of(effect), perm.getCard(), null, true),
                LifeWatch.ON_CONTROLLER_GAINS_LIFE,
                new TriggerContext.LifeGain(player1Id, 4))).isTrue();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEventValue()).isEqualTo(4);
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

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    LifeWatch.ON_CONTROLLER_LOSES_LIFE, ctx);

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
                LifeWatch.ON_CONTROLLER_LOSES_LIFE, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        var entry = gd.stack.getLast();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getControllerId()).isEqualTo(player1Id);
        assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
        assertThat(entry.getEffectsToResolve()).containsExactly(effect);
    }

    @Nested
    @DisplayName("ON_CONTROLLER_LOSES_LIFE — SequenceEffect")
    class ControllerLifeLossSequence {

        @Test
        @DisplayName("puts triggered ability on stack with the life-loss amount")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Marina Vendrell's Grimoire");
            var effect = SequenceEffect.of(
                    new DiscardEffect(new EventValue(), DiscardRecipient.CONTROLLER),
                    new ConditionalEffect(new ControllerHandEmpty(), new ControllerLosesGameEffect()));
            var ctx = new TriggerContext.LifeLoss(player1Id, 3);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    LifeWatch.ON_CONTROLLER_LOSES_LIFE, ctx);

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
    @DisplayName("queues a trigger for a creature card entering a graveyard from a non-battlefield zone")
    void queuesNonBattlefieldCreatureCardGraveyardTrigger() {
        Permanent perm = createPermanent("Syr Konrad, the Grim");
        var effect = new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT);
        var ctx = new TriggerContext.CreatureCardPutIntoGraveyard(
                createCard("Grizzly Bears"), player2Id);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect),
                ZoneChangeWatch.ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
    }

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

    @Test
    @DisplayName("a \"do this only once each turn\" nonland-cards-milled ability is marked as accepted on the stack")
    void nonlandCardsMilledDefaultCarriesTheOncePerTurnMark() {
        Permanent perm = createPermanent("Screeching Scorchbeast");
        var tokens = new CreateTokenEffect(new EventValue(), "Zombie Mutant", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE, CardSubtype.MUTANT), Set.of(), Set.of());
        var effect = new MayEffect(tokens, "Create that many Zombie Mutant tokens?");
        var ctx = new TriggerContext.NonlandCardsMilled(player2Id, 3);

        boolean result = registry.dispatch(
                new TriggerMatchContext(gd, perm, player1Id, List.of(effect), perm.getCard(), null, true),
                ZoneChangeWatch.ON_NONLAND_CARDS_MILLED, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getEventValue()).isEqualTo(3);
            assertThat(entry.isMarkSourceOncePerTurnOnAcceptance()).isTrue();
        });
    }

    @Test
    @DisplayName("queues a suspended card's opponent-graveyard trigger with its intervening \"if\" as the pipeline judged and wrapped it")
    void queuesSuspendedCardOpponentGraveyardTrigger() {
        Card sourceCard = createCard("Nihilith");
        var effect = new ConditionalEffect(
                new SourceCardSuspended(),
                new MayEffect(new RemoveTimeCounterFromExiledCardEffect(sourceCard.getId()),
                        "Remove a time counter from Nihilith?"));
        var ctx = new TriggerContext.CardPutIntoGraveyard(createCard("Shock"), player2Id);

        boolean result = registry.dispatch(
                new TriggerMatchContext(gd, null, player1Id, effect, sourceCard),
                ZoneChangeWatch.ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getCard()).isSameAs(sourceCard);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player1Id);
        assertThat(gd.stack.getLast().getSourcePermanentId()).isNull();
        assertThat(gd.stack.getLast().isNonTargeting()).isTrue();
        assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
    }

    @Test
    @DisplayName("queues a trigger for each creature card leaving the controller's graveyard")
    void queuesCreatureCardLeavesGraveyardTrigger() {
        Permanent perm = createPermanent("Syr Konrad, the Grim");
        var effect = new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT);
        var ctx = new TriggerContext.ControllerCardsLeaveGraveyard(player1Id);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect),
                ZoneChangeWatch.ON_CONTROLLER_CREATURE_CARD_LEAVES_GRAVEYARD, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
    }
}
