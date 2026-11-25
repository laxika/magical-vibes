package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.service.trigger.WatchedAbilities;
import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.rules.LibraryEvent;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The library, draw and discard matcher ({@link LibraryTriggers}): each ability its event triggers goes to the stack
 * door whole, with the record of the event whatever its effects are; the ability's own trigger decides whether it
 * triggers (its player relation, filter, narrower event and count).
 */
@ExtendWith(MockitoExtension.class)
class LibraryTriggersTest {

    private static final CardPredicate CREATURE = new CardTypePredicate(CardType.CREATURE);
    private static final CardPredicate NONLAND = new CardNotPredicate(new CardTypePredicate(CardType.LAND));

    @Mock
    private TriggerIndex triggerIndex;

    @Mock
    private TriggeredAbilityStackService stackService;

    @Mock
    private PredicateEvaluationService predicateEvaluationService;

    @Mock
    private ConditionEvaluationService conditionEvaluationService;

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameOutcomeService gameOutcomeService;

    @Mock
    private GameLogService gameLogService;

    @Mock
    private PlayerInputService playerInputService;

    @Mock
    private StateTriggerService stateTriggerService;

    private LibraryTriggers sut;

    /** The requests handed to the stack door, in order. */
    private final List<TriggeredAbilityRequest> doorRequests = new ArrayList<>();

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        // A query scope runs its queries; these tests have no layered pass to share.
        lenient().when(gameQueryService.withQueryScope(any(GameData.class), any()))
                .thenAnswer(invocation -> ((java.util.function.Supplier<?>) invocation.getArgument(1)).get());
        // The index, as far as these abilities need it: a permanent's card abilities and those granted it, a graveyard
        // card's own.
        lenient().when(triggerIndex.permanentAbilities(any(), any())).thenAnswer(invocation -> {
            Permanent permanent = invocation.getArgument(1);
            List<TriggeredAbility> abilities = new ArrayList<>(permanent.getCard().getTriggeredAbilities());
            abilities.addAll(WatchedAbilities.grantedByEffects(gd, permanent));
            return abilities;
        });
        lenient().when(triggerIndex.graveyardAbilities(any(), any()))
                .thenAnswer(invocation -> ((Card) invocation.getArgument(1)).getTriggeredAbilities());
        lenient().when(conditionEvaluationService.eventConditionMet(any(), any(), any()))
                .thenAnswer(invocation -> ((TriggerOptions) invocation.getArgument(1)).eventCondition() == null);
        // The stack door, as far as these abilities rely on it: it judges and marks the ability's limit and puts one
        // entry of its effects as written, stamped with the record, on the stack.
        lenient().when(stackService.put(any(), any())).thenAnswer(invocation -> {
            GameData game = invocation.getArgument(0);
            TriggeredAbilityRequest request = invocation.getArgument(1);
            doorRequests.add(request);
            TriggeredAbility ability = request.ability();
            UUID limitSource = request.sourcePermanent() != null ? request.sourcePermanentId()
                    : request.sourceCard().getId();
            if (TriggeredAbilityStackService.limitReached(game, ability.options().limit(), limitSource,
                    ability.limitKey())) {
                return false;
            }
            StackEntry entry = TestStackDoor.triggered(request.sourceCard(), request.controllerId(),
                    request.description(),
                    new ArrayList<>(ability.effects().stream().map(EffectRegistration::effect).toList()), null,
                    request.sourcePermanentId());
            request.record().stamp(entry);
            game.stack.add(entry);
            TriggeredAbilityStackService.markLimit(game, ability.options().limit(), limitSource, ability.limitKey());
            return true;
        });
        EventAbilitySupport abilities = new EventAbilitySupport(triggerIndex, stackService, conditionEvaluationService,
                gameLogService);
        sut = new LibraryTriggers(abilities, triggerIndex, stackService, predicateEvaluationService,
                conditionEvaluationService, gameQueryService, gameOutcomeService, gameLogService, playerInputService,
                stateTriggerService);
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
    }

    // Effects must be added before wrapping in a Permanent: the Permanent constructor freezes the card.
    private static Card createCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    private void draw(UUID playerId, Card drawn) {
        int count = gd.cardsDrawnThisTurn.merge(playerId, 1, Integer::sum);
        sut.trigger(gd, new LibraryEvent.Drew(playerId, drawn, false, count));
    }

    @Nested
    @DisplayName("draws")
    class Draws {

        @Test
        @DisplayName("A draw's abilities are found in one query scope")
        void drawAbilitiesFoundInOneQueryScope() {
            draw(player1Id, createCard("Grizzly Bears", CardType.CREATURE));

            verify(gameQueryService).withQueryScope(same(gd), any());
        }

        @Test
        @DisplayName("an ability of two effects is one stack entry recording the draw")
        void abilityOfTwoEffectsIsOneEntry() {
            Card card = createCard("Gixian Puppeteer", CardType.CREATURE);
            LoseLifeEffect loss = new LoseLifeEffect(2, Subjects.eachOpponent());
            GainLifeEffect gain = new GainLifeEffect(2);
            card.addTrigger(Triggers.youDrawCard(), loss, gain);
            Permanent puppeteer = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(puppeteer);
            Card drawn = createCard("Island", CardType.LAND);

            draw(player1Id, drawn);

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(loss, gain);
                assertThat(entry.getSourcePermanentId()).isEqualTo(puppeteer.getId());
                assertThat(entry.getTargetId()).isEqualTo(player1Id);
                assertThat(entry.isNonTargeting()).isTrue();
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player1Id);
                assertThat(entry.getTriggeringCards()).containsExactly(drawn);
                assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
            });
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) ->
                    e.plainText().equals("Gixian Puppeteer's ability triggers.")));
        }

        @Test
        @DisplayName("a \"you may\" goes on the stack as written")
        void mayAbilityGoesOnTheStackAsWritten() {
            Card card = createCard("Hoofprints of the Stag", CardType.ENCHANTMENT);
            MayEffect may = new MayEffect(new BoostSelfEffect(1, 1), "Do it?");
            card.addTrigger(Triggers.youDrawCard(), may);
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            draw(player1Id, null);

            assertThat(gd.stack).singleElement()
                    .satisfies(entry -> assertThat(entry.getEffectsToResolve()).containsExactly(may));
        }

        @Test
        @DisplayName("hands the door the draw as the event the additional-trigger effects of draws judge")
        void handsTheDoorTheDrawEvent() {
            Card crawlerCard = createCard("Psychosis Crawler", CardType.CREATURE);
            crawlerCard.addTrigger(Triggers.youDrawCard(), new BoostSelfEffect(1, 1));
            Permanent crawler = new Permanent(crawlerCard);
            gd.playerBattlefields.get(player1Id).add(crawler);

            draw(player1Id, null);

            assertThat(doorRequests).singleElement().satisfies(request -> {
                assertThat(request.sourcePermanent()).isSameAs(crawler);
                assertThat(request.cause()).isEqualTo(new TriggerCause.Drew());
            });
        }

        @Test
        @DisplayName("an ability granted for a duration triggers too, as the layers leave the permanent")
        void aGrantedAbilityTriggers() {
            Permanent bears = new Permanent(createCard("Grizzly Bears", CardType.CREATURE));
            PermanentGrants.abilityUntilEndOfTurn(gd, bears,
                    new TriggeredAbility(Triggers.youDrawCard(), new BoostSelfEffect(1, 1)));
            gd.playerBattlefields.get(player1Id).add(bears);

            draw(player1Id, null);

            assertThat(gd.stack).singleElement()
                    .satisfies(entry -> assertThat(entry.getSourcePermanentId()).isEqualTo(bears.getId()));
        }

        @Test
        @DisplayName("judges the filter on the drawn card and records the card as the triggering card")
        void judgesTheFilterOnTheDrawnCard() {
            Card whelpCard = createCard("Whelp", CardType.CREATURE);
            whelpCard.addTrigger(Triggers.youDrawCard().withFilter(CREATURE), new BoostSelfEffect(1, 1));
            Permanent whelp = new Permanent(whelpCard);
            gd.playerBattlefields.get(player1Id).add(whelp);
            Card bears = createCard("Grizzly Bears", CardType.CREATURE);
            Card forest = createCard("Forest", CardType.LAND);
            lenient().when(predicateEvaluationService.matchesCardPredicate(eq(bears), eq(CREATURE),
                    eq(whelpCard.getId()), eq(gd), eq(player1Id), eq(whelp.getId()), any(), any(), eq(whelp)))
                    .thenReturn(true);

            draw(player1Id, forest);
            draw(player1Id, null);
            assertThat(gd.stack).isEmpty();

            draw(player1Id, bears);

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getTriggeringCards()).containsExactly(bears);
                assertThat(entry.getTriggeringCardId()).isEqualTo(bears.getId());
            });
        }

        @Test
        @DisplayName("\"your second card each turn\" triggers on the player's second draw only")
        void theSecondCardEachTurn() {
            Card card = createCard("Mantle of Tides", CardType.ARTIFACT);
            card.target(TargetFilters.creatureYouControl())
                    .addTrigger(Triggers.youDrawSecondCardEachTurn(), new AttachSourceEquipmentToTargetCreatureEffect());
            Permanent equipment = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(equipment);

            draw(player1Id, null);
            assertThat(doorRequests).isEmpty();
            draw(player1Id, null);
            draw(player1Id, null);

            assertThat(doorRequests).singleElement().satisfies(request -> {
                assertThat(request.ability()).isEqualTo(card.getTriggeredAbilities().getFirst());
                assertThat(request.sourcePermanent()).isSameAs(equipment);
            });
        }

        @Test
        @DisplayName("the draw step's first draw is told apart (Orcish Bowmasters' \"except the first\")")
        void theDrawStepsFirstDrawIsToldApart() {
            Card card = createCard("Orcish Bowmasters", CardType.CREATURE);
            card.addTrigger(Triggers.opponentDrawsCardExceptFirstInDrawStep(), new BoostSelfEffect(1, 1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.trigger(gd, new LibraryEvent.Drew(player2Id, null, true, 1));
            assertThat(gd.stack).isEmpty();

            sut.trigger(gd, new LibraryEvent.Drew(player2Id, null, false, 2));
            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("an opponent's draw names the drawing player as the non-targeting \"that player\"")
        void opponentDrawNamesTheDrawingPlayer() {
            Card card = createCard("Fate Unraveler", CardType.ENCHANTMENT);
            card.addTrigger(Triggers.opponentDrawsCard(), new BoostSelfEffect(1, 1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            draw(player1Id, null);
            assertThat(gd.stack).isEmpty();
            draw(player2Id, null);

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getControllerId()).isEqualTo(player1Id);
                assertThat(entry.getTargetId()).isEqualTo(player2Id);
                assertThat(entry.isNonTargeting()).isTrue();
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2Id);
            });
        }

        @Test
        @DisplayName("an opponent-draw ability honors its once-each-turn limit")
        void opponentDrawTriggerHonorsItsOncePerTurnLimit() {
            Card card = createCard("Tataru Taru", CardType.CREATURE);
            card.addTrigger(Triggers.opponentDrawsCard(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                    new BoostSelfEffect(1, 1));
            Permanent source = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(source);

            draw(player2Id, null);
            draw(player2Id, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.keyedOncePerTurnTriggersFiredThisTurn.get(source.getId()))
                    .containsExactly(card.getTriggeredAbilities().getFirst().limitKey());
        }

        @Test
        @DisplayName("a graveyard \"your second card each turn\" ability records its graveyard incarnation")
        void graveyardSecondDrawAbility() {
            Card wolfbat = createCard("Wolfbat", CardType.CREATURE);
            wolfbat.addTrigger(new TriggerCondition(Triggers.youDrawSecondCardEachTurn(), AbilityZones.GRAVEYARD),
                    TriggerOptions.DEFAULT, UnlessPaysEffect.youMayPay("{B}", new BoostSelfEffect(1, 1)));
            gd.playerGraveyards.put(player1Id, new ArrayList<>(List.of(wolfbat)));

            draw(player1Id, null);
            draw(player1Id, null);

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
                assertThat(entry.getCard()).isEqualTo(wolfbat);
                assertThat(entry.getTriggeringCardId()).isEqualTo(wolfbat.getId());
                assertThat(entry.getEffectsToResolve()).singleElement().isInstanceOf(UnlessPaysEffect.class);
            });
        }

        @Test
        @DisplayName("a graveyard \"their second card each turn\" ability is its owner's")
        void graveyardOpponentSecondDrawAbility() {
            Card detective = createCard("Dogged Detective", CardType.CREATURE);
            detective.addTrigger(new TriggerCondition(Triggers.opponentDrawsSecondCardEachTurn(),
                    AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT, new MayEffect(new BoostSelfEffect(1, 1), "?"));
            gd.playerGraveyards.put(player1Id, new ArrayList<>(List.of(detective)));

            draw(player2Id, null);
            draw(player2Id, null);

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getCard()).isEqualTo(detective);
                assertThat(entry.getControllerId()).isEqualTo(player1Id);
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2Id);
            });
        }

        @Test
        @DisplayName("an emblem's \"whenever a player draws a card\" triggers on every player's draw")
        void emblemAnyPlayerDraw() {
            Card source = createCard("Ob Nixilis Reignited", CardType.PLANESWALKER);
            gd.emblems.add(new Emblem(player1Id, List.of(), source, List.of(), 0L,
                    List.of(new TriggeredAbility(Triggers.playerDrawsCard(),
                            new LoseLifeEffect(2, Subjects.you())))));

            draw(player2Id, null);
            draw(player1Id, null);

            assertThat(gd.stack).hasSize(2).allSatisfy(entry -> {
                assertThat(entry.getControllerId()).isEqualTo(player1Id);
                assertThat(entry.isNonTargeting()).isTrue();
                assertThat(entry.isMadeByResolution()).isTrue();
            });
            assertThat(gd.stack).extracting(StackEntry::getTriggeringPlayerId).containsExactly(player2Id, player1Id);
        }

        @Test
        @DisplayName("an emblem's targeted draw ability goes to the door with its own target filter")
        void emblemTargetedDrawAbility() {
            Card source = createCard("Teferi, Hero of Dominaria", CardType.PLANESWALKER);
            TargetFilter opponentsPermanent = TargetFilters.permanentAnOpponentControls();
            TriggeredAbility ability = new TriggeredAbility(Triggers.youDrawCard(), new ExileTargetPermanentEffect())
                    .withTargetFilter(opponentsPermanent);
            gd.emblems.add(new Emblem(player1Id, List.of(), source, List.of(), 0L, List.of(ability)));

            draw(player2Id, null);
            assertThat(doorRequests).isEmpty();
            draw(player1Id, null);

            assertThat(doorRequests).singleElement().satisfies(request -> {
                assertThat(request.ability()).isEqualTo(ability);
                assertThat(request.sourceCard()).isEqualTo(source);
                assertThat(request.sourcePermanent()).isNull();
                assertThat(request.emblemAbility()).isTrue();
            });
        }

        @Test
        @DisplayName("a reveal ability triggers for its own source's reveal by its controller, on a matching card")
        void aRevealedCardTriggersTheSourcesRevealAbilities() {
            CardPredicate land = new CardTypePredicate(CardType.LAND);
            Card rowenCard = createCard("Rowen", CardType.ENCHANTMENT);
            rowenCard.addTrigger(Triggers.youRevealCardThisWay().withFilter(land), new DrawCardEffect(1));
            Permanent rowen = new Permanent(rowenCard);
            gd.playerBattlefields.get(player1Id).add(rowen);
            Card forest = createCard("Forest", CardType.LAND);
            Card bears = createCard("Grizzly Bears", CardType.CREATURE);
            when(gameQueryService.findPermanentController(gd, rowen.getId())).thenReturn(player1Id);
            lenient().when(predicateEvaluationService.matchesCardPredicate(eq(forest), eq(land),
                    eq(rowenCard.getId()), eq(gd), eq(player1Id), eq(rowen.getId()), any(), any(), eq(rowen)))
                    .thenReturn(true);

            sut.trigger(gd, new LibraryEvent.RevealedDrawnCard(player1Id, rowen, bears));
            sut.trigger(gd, new LibraryEvent.RevealedDrawnCard(player2Id, rowen, forest));
            assertThat(gd.stack).isEmpty();

            sut.trigger(gd, new LibraryEvent.RevealedDrawnCard(player1Id, rowen, forest));

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(new DrawCardEffect(1));
                assertThat(entry.getTriggeringCards()).containsExactly(forest);
            });
        }
    }

    @Nested
    @DisplayName("discards and cycling")
    class Discards {

        @Test
        @DisplayName("an opponent's discard names the discarding player and the card, with its graveyard stay")
        void opponentDiscardRecordsThePlayerAndTheCard() {
            Card card = createCard("Megrim", CardType.ENCHANTMENT);
            card.addTrigger(Triggers.opponentDiscardsCard(), new BoostSelfEffect(1, 1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            Card discarded = createCard("Forest", CardType.LAND);

            sut.trigger(gd, new LibraryEvent.Discarded(player1Id, discarded, false, false, false));
            assertThat(gd.stack).isEmpty();
            sut.trigger(gd, new LibraryEvent.Discarded(player2Id, discarded, false, false, false));

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getTargetId()).isEqualTo(player2Id);
                assertThat(entry.isNonTargeting()).isTrue();
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2Id);
                assertThat(entry.getTriggeringCards()).containsExactly(discarded);
                assertThat(entry.getTriggeringCardId()).isEqualTo(discarded.getId());
            });
        }

        @Test
        @DisplayName("\"whenever you cycle a card\" triggers on a cycling discard only")
        void youCycleACard() {
            Card card = createCard("Drake Haven", CardType.ENCHANTMENT);
            card.addTrigger(Triggers.youCycleCard(), new BoostSelfEffect(1, 1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            Card discarded = createCard("Forest", CardType.LAND);

            sut.trigger(gd, new LibraryEvent.Discarded(player1Id, discarded, false, false, false));
            assertThat(gd.stack).isEmpty();
            sut.trigger(gd, new LibraryEvent.Discarded(player1Id, discarded, true, false, false));

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("\"your first nonland discard each turn\" counts the turn's nonland discards")
        void theFirstNonlandDiscardEachTurn() {
            Card card = createCard("Veronica, Dissident Scribe", CardType.CREATURE);
            card.addTrigger(Triggers.youDiscardCard().withFilter(NONLAND).firstTimeEachTurn(),
                    new BoostSelfEffect(1, 1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            Card bears = createCard("Grizzly Bears", CardType.CREATURE);
            when(predicateEvaluationService.matchesCardPredicate(eq(bears), eq(NONLAND), any(), eq(gd),
                    eq(player1Id))).thenReturn(true);
            gd.playerGraveyards.put(player1Id, new ArrayList<>(List.of(bears)));

            gd.nonlandCardsDiscardedThisTurn.put(player1Id, 1);
            sut.trigger(gd, new LibraryEvent.Discarded(player1Id, bears, false, false, false));
            gd.nonlandCardsDiscardedThisTurn.put(player1Id, 2);
            sut.trigger(gd, new LibraryEvent.Discarded(player1Id, bears, false, false, false));

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("a discard event of its own triggers \"one or more cards\" with their number as its value")
        void aLoneDiscardIsADiscardEvent() {
            Card card = createCard("Archfiend of the Dross", CardType.CREATURE);
            card.addTrigger(Triggers.youDiscardOneOrMoreCards(), new BoostSelfEffect(1, 1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            Card first = createCard("First", CardType.CREATURE);
            Card second = createCard("Second", CardType.CREATURE);

            sut.trigger(gd, new LibraryEvent.Discarded(player1Id, first, false, false, false));
            assertThat(gd.stack).isEmpty();
            sut.trigger(gd, new LibraryEvent.Discarded(player1Id, first, false, false, true));
            sut.trigger(gd, new LibraryEvent.DiscardedCards(player1Id, List.of(first, second)));

            assertThat(gd.stack).extracting(StackEntry::getEventValue).containsExactly(1, 2);
            assertThat(gd.stack.getLast().getTriggeringCards()).containsExactly(first, second);
            verify(stateTriggerService).checkStateTriggers(gd);
        }

        @Test
        @DisplayName("the discarded card's own abilities trigger from its graveyard, by an opponent's discard too")
        void theDiscardedCardsOwnAbilities() {
            Card card = createCard("Obstinate Baloth", CardType.CREATURE);
            card.addTrigger(Triggers.youDiscardThisCard(), new DrawCardEffect(1));
            card.addTrigger(Triggers.opponentCausesYouToDiscardThisCard(), new GainLifeEffect(4));
            gd.playerGraveyards.put(player1Id, new ArrayList<>(List.of(card)));

            sut.trigger(gd, new LibraryEvent.Discarded(player1Id, card, false, false, false));
            assertThat(gd.stack).singleElement()
                    .satisfies(entry -> assertThat(entry.getEffectsToResolve()).containsExactly(new DrawCardEffect(1)));

            sut.trigger(gd, new LibraryEvent.Discarded(player1Id, card, false, true, false));
            assertThat(gd.stack).hasSize(3);
            assertThat(gd.stack.getLast().getTriggeringCardId()).isEqualTo(card.getId());
        }

        @Test
        @DisplayName("a cycled card's \"when you cycle this card\" takes the cycling cost's X")
        void theCycledCardsOwnAbility() {
            Card card = createCard("Shark Typhoon", CardType.ENCHANTMENT);
            card.addTrigger(Triggers.youCycleThisCard(), new DrawCardEffect(1));
            gd.playerGraveyards.put(player1Id, new ArrayList<>(List.of(card)));

            sut.trigger(gd, new LibraryEvent.Cycled(player1Id, card, 3, null, null));

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getXValue()).isEqualTo(3);
                assertThat(entry.isNonTargeting()).isTrue();
            });
        }
    }

    @Nested
    @DisplayName("scry, surveil, search and shuffle")
    class LibraryActions {

        @Test
        @DisplayName("a scry records the player, the cards put on the bottom and the cards looked at")
        void scry() {
            Card own = createCard("Flamekin Village", CardType.ENCHANTMENT);
            own.addTrigger(Triggers.youScry(), new BoostSelfEffect(1, 1));
            Card theirs = createCard("Opponent Watcher", CardType.ENCHANTMENT);
            theirs.addTrigger(Triggers.opponentScries(), new BoostSelfEffect(1, 1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(own));
            gd.playerBattlefields.get(player2Id).add(new Permanent(theirs));

            sut.trigger(gd, new LibraryEvent.Scried(player1Id, 1, 2));

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack.getFirst()).satisfies(entry -> {
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player1Id);
                assertThat(entry.getEventValue()).isEqualTo(1);
                assertThat(entry.getCardsLookedAtWhileScrying()).isEqualTo(2);
                assertThat(entry.getTargetId()).isNull();
            });
            assertThat(gd.stack.getLast().getTargetId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("\"whenever you surveil for the first time each turn\" triggers on the first surveil only")
        void surveilFirstTimeEachTurn() {
            Card card = createCard("Sinister Snitch", CardType.CREATURE);
            card.addTrigger(Triggers.youSurveil().firstTimeEachTurn(), new BoostSelfEffect(1, 1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.trigger(gd, new LibraryEvent.Surveilled(player1Id, true));
            sut.trigger(gd, new LibraryEvent.Surveilled(player1Id, false));

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("an opponent's search names the searching player as \"that player\"")
        void opponentSearch() {
            Card card = createCard("Ob Nixilis, Unshackled", CardType.CREATURE);
            card.addTrigger(Triggers.opponentSearchesTheirLibrary(), new BoostSelfEffect(1, 1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.trigger(gd, new LibraryEvent.Searched(player1Id));
            assertThat(gd.stack).isEmpty();
            sut.trigger(gd, new LibraryEvent.Searched(player2Id));

            assertThat(gd.stack).singleElement()
                    .satisfies(entry -> assertThat(entry.getTargetId()).isEqualTo(player2Id));
        }

        @Test
        @DisplayName("\"a spell or ability causes its controller to shuffle\" needs the shuffler's own spell or ability")
        void shuffleCausedByItsController() {
            Card card = createCard("Cosi's Trickster", CardType.CREATURE);
            card.addTrigger(Triggers.spellOrAbilityCausesItsControllerToShuffle(), new BoostSelfEffect(1, 1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.trigger(gd, new LibraryEvent.Shuffled(player2Id, player1Id));
            assertThat(gd.stack).isEmpty();
            sut.trigger(gd, new LibraryEvent.Shuffled(player2Id, player2Id));

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getTargetId()).isEqualTo(player2Id);
                assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
            });
        }
    }
}
