package com.github.laxika.magicalvibes.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AbundanceDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DrawFromBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.FirstNonDrawStepDrawFourReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.LivingConundrumDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentDrawTwoOrMoreReplacedEffect;
import com.github.laxika.magicalvibes.model.effect.QuantumRiddlerDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.DredgeSupport;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.mayfx.BreathstealersCryptDrawReplacementHandler;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DrawServiceTest {

    @Test
    void emptyLibraryLossWaitsUntilResolutionFinishes() {
        gd.playerDecks.put(player1Id, new ArrayList<>());
        gd.deferPlayerLossCheck = true;

        sut.performDrawCard(gd, player1Id);

        assertThat(gd.playersAttemptedDrawFromEmptyLibrary).contains(player1Id);
        verify(gameOutcomeService, never()).resolveLoss(any(), any(), any());
        verify(gameOutcomeService, never()).declareWinner(any(), any());
    }

    @Test
    void drawFromBottomReplacementUsesTheControllerBottomOfLibrary() {
        Card lantern = createCard("Lantern of Undersight", CardType.ARTIFACT);
        lantern.addStaticEffect(new DrawFromBottomOfLibraryEffect());
        gd.playerBattlefields.get(player1Id).add(new Permanent(lantern));
        Card top = createCard("Top", CardType.CREATURE);
        Card bottom = createCard("Bottom", CardType.CREATURE);
        gd.playerDecks.put(player1Id, new ArrayList<>(List.of(top, bottom)));
        gd.playerHands.put(player1Id, new ArrayList<>());

        sut.resolveDrawCard(gd, player1Id);

        assertThat(gd.playerHands.get(player1Id)).containsExactly(bottom);
        assertThat(gd.playerDecks.get(player1Id)).containsExactly(top);
    }

    @Test
    void anotherPlayersDrawInstructionWaitsBehindCurrentReplacement() {
        Card abundance = createCard("Abundance", CardType.ENCHANTMENT);
        abundance.addStaticEffect(new AbundanceDrawReplacementEffect());
        gd.playerBattlefields.get(player1Id).add(new Permanent(abundance));
        Card first = createCard("First", CardType.CREATURE);
        Card second = createCard("Second", CardType.CREATURE);
        gd.playerDecks.put(player1Id, new ArrayList<>(List.of(first, second)));
        gd.playerHands.put(player1Id, new ArrayList<>());

        sut.resolveDrawCards(gd, player1Id, 2);
        sut.resolveDrawCards(gd, player2Id, 2);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingCardDraws).containsExactly(player1Id, player2Id, player2Id);
    }

    @Test
    void multipleDrawsWaitForEachReplacementChoice() {
        Card abundance = createCard("Abundance", CardType.ENCHANTMENT);
        abundance.addStaticEffect(new AbundanceDrawReplacementEffect());
        gd.playerBattlefields.get(player1Id).add(new Permanent(abundance));
        Card first = createCard("First", CardType.CREATURE);
        Card second = createCard("Second", CardType.CREATURE);
        gd.playerDecks.put(player1Id, new ArrayList<>(List.of(first, second)));
        gd.playerHands.put(player1Id, new ArrayList<>());

        sut.resolveDrawCards(gd, player1Id, 2);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.playerHands.get(player1Id)).isEmpty();
        gd.pendingMayAbilities.clear();
        sut.resolveDrawCardWithoutStaticReplacementCheck(gd, player1Id);
        sut.resumePendingCardDraws(gd);

        assertThat(gd.playerHands.get(player1Id)).containsExactly(first);
        assertThat(gd.pendingMayAbilities).hasSize(1);
        gd.pendingMayAbilities.clear();
        sut.resolveDrawCardWithoutStaticReplacementCheck(gd, player1Id);
        sut.resumePendingCardDraws(gd);

        assertThat(gd.playerHands.get(player1Id)).containsExactly(first, second);
        assertThat(gd.pendingCardDraws).isEmpty();
    }

    @Test
    void opponentMultiCardDrawIsReplacedWithOneCardForEachPlayer() {
        when(gameQueryService.getOpponentId(gd, player2Id)).thenReturn(player1Id);
        Card almsCollector = createCard("Alms Collector", CardType.CREATURE);
        almsCollector.addStaticEffect(new OpponentDrawTwoOrMoreReplacedEffect());
        gd.playerBattlefields.get(player1Id).add(new Permanent(almsCollector));
        gd.playerHands.put(player1Id, new ArrayList<>());
        gd.playerHands.put(player2Id, new ArrayList<>());
        Card player1Card = createCard("Player 1 card", CardType.CREATURE);
        Card player2Card = createCard("Player 2 card", CardType.CREATURE);
        gd.playerDecks.put(player1Id, new ArrayList<>(List.of(player1Card)));
        gd.playerDecks.put(player2Id, new ArrayList<>(List.of(player2Card)));

        sut.resolveDrawCards(gd, player2Id, 2);

        assertThat(gd.playerHands.get(player1Id)).containsExactly(player1Card);
        assertThat(gd.playerHands.get(player2Id)).containsExactly(player2Card);
        assertThat(gd.playerDecks.get(player1Id)).isEmpty();
        assertThat(gd.playerDecks.get(player2Id)).isEmpty();
    }

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameLogService gameLogService;

    @Mock
    private GameOutcomeService gameOutcomeService;

    @Mock
    private TriggeredAbilityQueueService triggeredAbilityQueueService;

    @Mock
    private InteractionHandlerRegistry interactionHandlerRegistry;

    @Mock
    private BreathstealersCryptDrawReplacementHandler breathstealersCryptDrawReplacementHandler;

    @Mock
    private ConditionEvaluationService conditionEvaluationService;

    @Mock
    private DredgeSupport dredgeSupport;

    @Mock
    private GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;

    @Mock
    private TriggerCollectionService triggerCollectionService;

    @Mock
    private PredicateEvaluationService predicateEvaluationService;

    @InjectMocks
    private DrawService sut;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        // The draw collectors judge each ability's intervening "if" and event condition through the service.
        lenient().when(conditionEvaluationService.triggerConditionsMet(any(), any(), any())).thenAnswer(invocation -> {
            TriggerOptions options = invocation.getArgument(1);
            return options.interveningIf() == null && options.eventCondition() == null;
        });
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

    // ===== Helpers =====

    // Effects must be added before wrapping in a Permanent — the Permanent constructor
    // freezes the card.
    private static Card createCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    @Nested
    @DisplayName("checkControllerDrawTriggers — the abilities as written")
    class ControllerDrawAbilities {

        @Test
        @DisplayName("an ability of two effects is one stack entry recording the draw")
        void abilityOfTwoEffectsIsOneEntry() {
            Card card = createCard("Gixian Puppeteer", CardType.CREATURE);
            LoseLifeEffect loss = new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT);
            GainLifeEffect gain = new GainLifeEffect(2);
            card.addTrigger(Triggers.youDrawCard(), loss, gain);
            Permanent puppeteer = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(puppeteer);
            Card drawn = createCard("Island", CardType.LAND);

            sut.checkControllerDrawTriggers(gd, player1Id, drawn);

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(loss, gain);
                assertThat(entry.getSourcePermanentId()).isEqualTo(puppeteer.getId());
                assertThat(entry.getTargetId()).isEqualTo(player1Id);
                assertThat(entry.isNonTargeting()).isTrue();
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player1Id);
                assertThat(entry.getTriggeringCards()).containsExactly(drawn);
                assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
            });
        }

        @Test
        @DisplayName("a \"you may\" goes on the stack as written with the reader's record")
        void mayAbilityGoesOnTheStackWithTheRecord() {
            Card card = createCard("Hoofprints of the Stag", CardType.ENCHANTMENT);
            MayEffect may = new MayEffect(new BoostSelfEffect(1, 1), "Do it?");
            card.addTrigger(Triggers.youDrawCard(), may);
            Permanent hoofprints = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(hoofprints);

            sut.checkControllerDrawTriggers(gd, player1Id);

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(may);
                assertThat(entry.getSourcePermanentId()).isEqualTo(hoofprints.getId());
                assertThat(entry.getTargetId()).isEqualTo(player1Id);
                assertThat(entry.isNonTargeting()).isTrue();
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player1Id);
                assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
            });
        }

        @Test
        @DisplayName("a draw ability fires whoever else is on the battlefield")
        void drawTriggerFires() {
            Card crawlerCard = createCard("Psychosis Crawler", CardType.CREATURE);
            crawlerCard.addTrigger(Triggers.youDrawCard(), new BoostSelfEffect(1, 1));
            Permanent crawler = new Permanent(crawlerCard);
            gd.playerBattlefields.get(player1Id).add(crawler);

            sut.checkControllerDrawTriggers(gd, player1Id);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(crawler.getId());
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Psychosis Crawler's ability triggers.")));
        }

        @Test
        @DisplayName("duplicates a draw trigger when an additional-trigger effect applies")
        void additionalTriggerEffectDuplicatesDrawTrigger() {
            Card crawlerCard = createCard("Psychosis Crawler", CardType.CREATURE);
            crawlerCard.addTrigger(Triggers.youDrawCard(), new BoostSelfEffect(1, 1));
            Permanent crawler = new Permanent(crawlerCard);
            gd.playerBattlefields.get(player1Id).add(crawler);
            when(gameQueryService.countAdditionalDrawTriggeredAbilityTriggers(gd, player1Id, crawler))
                    .thenReturn(1);

            sut.checkControllerDrawTriggers(gd, player1Id);

            assertThat(gd.stack).hasSize(2);
        }
    }

    @Nested
    @DisplayName("the drawn card's filter and the reveal abilities")
    class DrawnCardFilter {

        @Test
        @DisplayName("judges a draw ability's filter on the drawn card and records the card as the triggering card")
        void judgesTheFilterOnTheDrawnCardAndRecordsIt() {
            CardPredicate creature = new CardTypePredicate(CardType.CREATURE);
            Card whelpCard = createCard("Whelp", CardType.CREATURE);
            whelpCard.addTrigger(Triggers.youDrawCard().withFilter(creature), new BoostSelfEffect(1, 1));
            Permanent whelp = new Permanent(whelpCard);
            gd.playerBattlefields.get(player1Id).add(whelp);
            Card bears = createCard("Grizzly Bears", CardType.CREATURE);
            Card forest = createCard("Forest", CardType.LAND);
            lenient().when(predicateEvaluationService.matchesCardPredicate(eq(bears), eq(creature), eq(whelpCard.getId()),
                    eq(gd), eq(player1Id), eq(whelp.getId()), any(), any(), eq(whelp))).thenReturn(true);

            sut.checkControllerDrawTriggers(gd, player1Id, forest);
            sut.checkControllerDrawTriggers(gd, player1Id);
            assertThat(gd.stack).isEmpty();

            sut.checkControllerDrawTriggers(gd, player1Id, bears);

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getSourcePermanentId()).isEqualTo(whelp.getId());
                assertThat(entry.getTriggeringCards()).containsExactly(bears);
                assertThat(entry.getTriggeringCardId()).isEqualTo(bears.getId());
            });
        }

        @Test
        @DisplayName("a card revealed by a permanent triggers that permanent's matching reveal abilities")
        void aRevealedCardTriggersTheSourcesRevealAbilities() {
            CardPredicate land = new CardTypePredicate(CardType.LAND);
            Card rowenCard = createCard("Rowen", CardType.ENCHANTMENT);
            rowenCard.addTrigger(Triggers.youRevealCardThisWay().withFilter(land), new DrawCardEffect(1));
            Permanent rowen = new Permanent(rowenCard);
            gd.playerBattlefields.get(player1Id).add(rowen);
            Card forest = createCard("Forest", CardType.LAND);
            Card bears = createCard("Grizzly Bears", CardType.CREATURE);
            when(gameQueryService.findPermanentController(gd, rowen.getId())).thenReturn(player1Id);
            lenient().when(predicateEvaluationService.matchesCardPredicate(eq(forest), eq(land), eq(rowenCard.getId()),
                    eq(gd), eq(player1Id), eq(rowen.getId()), any(), any(), eq(rowen))).thenReturn(true);

            sut.revealDrawnCard(gd, rowen, player1Id, bears);
            sut.revealDrawnCard(gd, rowen, player2Id, forest);
            assertThat(gd.stack).isEmpty();

            sut.revealDrawnCard(gd, rowen, player1Id, forest);

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(new DrawCardEffect(1));
                assertThat(entry.getTriggeringCards()).containsExactly(forest);
            });
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) ->
                    e.plainText().equals("Player1 reveals Forest with Rowen.")));
        }
    }

    @Test
    @DisplayName("pushes a graveyard second-draw may-pay trigger onto the stack")
    void graveyardSecondDrawTriggerPushesMayPayAbility() {
        Card wolfbat = createCard("Wolfbat", CardType.CREATURE);
        wolfbat.addTrigger(new TriggerCondition(Triggers.youDrawSecondCardEachTurn(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new MayPayManaEffect("{B}", new BoostSelfEffect(1, 1), "Pay {B}?"));
        gd.playerGraveyards.put(player1Id, new ArrayList<>(List.of(wolfbat)));
        gd.cardsDrawnThisTurn.put(player1Id, 2);

        sut.checkControllerDrawTriggers(gd, player1Id);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isEqualTo(wolfbat);
        assertThat(gd.stack.getFirst().getEffectsToResolve())
                .singleElement().isInstanceOf(MayPayManaEffect.class);
    }

    @Test
    @DisplayName("pushes a graveyard opponent second-draw may trigger onto the stack")
    void graveyardOpponentSecondDrawTriggerPushesMayAbility() {
        Card detective = createCard("Dogged Detective", CardType.CREATURE);
        detective.addTrigger(new TriggerCondition(Triggers.opponentDrawsSecondCardEachTurn(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new MayEffect(new BoostSelfEffect(1, 1), "Do it?"));
        gd.playerGraveyards.put(player1Id, new ArrayList<>(List.of(detective)));
        gd.cardsDrawnThisTurn.put(player2Id, 2);

        sut.checkOpponentDrawTriggers(gd, player2Id);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isEqualTo(detective);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1Id);
        assertThat(gd.stack.getFirst().getEffectsToResolve())
                .singleElement().isInstanceOf(MayEffect.class);
    }

    @Test
    void targetedSecondDrawTriggerQueuesPermanentTargetChoice() {
        Card card = createCard("Mantle of Tides", CardType.ARTIFACT);
        AttachSourceEquipmentToTargetCreatureEffect effect = new AttachSourceEquipmentToTargetCreatureEffect();
        card.target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.youDrawSecondCardEachTurn(), effect);
        Permanent equipment = new Permanent(card);
        gd.playerBattlefields.get(player1Id).add(equipment);
        gd.cardsDrawnThisTurn.put(player1Id, 2);

        sut.checkControllerDrawTriggers(gd, player1Id);

        assertThat(gd.peekPendingInteraction(PermanentChoiceContext.DrawTriggerPermanentTarget.class))
                .isNotNull()
                .satisfies(trigger -> {
                    assertThat(trigger.sourcePermanentId()).isEqualTo(equipment.getId());
                    assertThat(trigger.targetFilter()).isEqualTo(TargetFilters.creatureYouControl());
                });
    }

    @Test
    void targetedPlayerDrawTriggerQueuesTargetChoice() {
        Card card = createCard("Queza, Augur of Agonies", CardType.CREATURE);
        SequenceEffect effect = SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER),
                new GainLifeEffect(1));
        card.target(new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"))
                .addTrigger(Triggers.youDrawCard(), effect);
        Permanent source = new Permanent(card);
        gd.playerBattlefields.get(player1Id).add(source);

        sut.checkControllerDrawTriggers(gd, player1Id);

        assertThat(gd.peekPendingInteraction(PermanentChoiceContext.DrawTriggerAnyTarget.class))
                .isNotNull();
    }

    @Test
    void opponentDrawTriggerHonorsItsOncePerTurnLimit() {
        Card card = createCard("Tataru Taru", CardType.CREATURE);
        card.addTrigger(Triggers.opponentDrawsCard(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new BoostSelfEffect(1, 1));
        Permanent source = new Permanent(card);
        gd.playerBattlefields.get(player1Id).add(source);

        sut.checkOpponentDrawTriggers(gd, player2Id);
        sut.checkOpponentDrawTriggers(gd, player2Id);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve())
                .containsExactly(new BoostSelfEffect(1, 1));
        assertThat(gd.keyedOncePerTurnTriggersFiredThisTurn.get(source.getId()))
                .containsExactly(card.getTriggeredAbilities().getFirst().limitKey());
        assertThat(gd.oncePerTurnTriggersFiredThisTurn).doesNotContain(source.getId());
    }

    @Test
    void emblemAnyPlayerDrawTriggerQueuesLifeLossForItsController() {
        Card source = createCard("Ob Nixilis Reignited", CardType.ENCHANTMENT);
        gd.emblems.add(new Emblem(player1Id, List.of(), source, List.of(), 0L,
                List.of(new TriggeredAbility(Triggers.playerDrawsCard(),
                        new LoseLifeEffect(2, LoseLifeRecipient.CONTROLLER)))));

        sut.checkControllerDrawTriggers(gd, player2Id);
        sut.checkControllerDrawTriggers(gd, player1Id);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allSatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getEffectsToResolve())
                    .containsExactly(new LoseLifeEffect(2, LoseLifeRecipient.CONTROLLER));
        });
        assertThat(gd.stack).extracting(StackEntry::getTriggeringPlayerId).containsExactly(player2Id, player1Id);
    }

    @Test
    void emblemOpponentDrawTriggerIgnoresItsControllersDraw() {
        Card source = createCard("Emblem source", CardType.ENCHANTMENT);
        gd.emblems.add(new Emblem(player1Id, List.of(), source, List.of(), 0L,
                List.of(new TriggeredAbility(Triggers.opponentDrawsCard(),
                        new LoseLifeEffect(1, LoseLifeRecipient.CONTROLLER)))));

        sut.checkControllerDrawTriggers(gd, player1Id);
        assertThat(gd.stack).isEmpty();

        sut.checkControllerDrawTriggers(gd, player2Id);
        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2Id));
    }

    @Test
    void emblemTargetedDrawTriggerQueuesItsTargetChoiceWithTheAbilitysTargetFilter() {
        Card source = createCard("Teferi, Hero of Dominaria", CardType.PLANESWALKER);
        TargetFilter opponentsPermanent = TargetFilters.permanentAnOpponentControls();
        gd.emblems.add(new Emblem(player1Id, List.of(), source, List.of(), 0L,
                List.of(new TriggeredAbility(Triggers.youDrawCard(), new ExileTargetPermanentEffect())
                        .withTargetFilter(opponentsPermanent))));

        sut.checkControllerDrawTriggers(gd, player2Id);
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class)).isFalse();

        sut.checkControllerDrawTriggers(gd, player1Id);

        PermanentChoiceContext.EmblemTriggerTarget choice =
                gd.peekPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class);
        assertThat(choice.controllerId()).isEqualTo(player1Id);
        assertThat(choice.effects()).containsExactly(new ExileTargetPermanentEffect());
        assertThat(choice.targetFilter()).isEqualTo(opponentsPermanent);
        assertThat(gd.stack).isEmpty();
        verify(triggeredAbilityQueueService).processNextEmblemTriggerTarget(gd);
    }

    @Test
    void conditionalDoubleDrawReplacementOnlyAppliesWhenConditionIsMet() {
        Card sourceCard = createCard("Vnwxt, Verbose Host", CardType.CREATURE);
        sourceCard.addStaticEffect(new ConditionalEffect(
                new MaxSpeed(), new DoubleDrawReplacementEffect()));
        gd.playerBattlefields.get(player1Id).add(new Permanent(sourceCard));

        Card firstCard = createCard("First card", CardType.CREATURE);
        Card secondCard = createCard("Second card", CardType.CREATURE);
        gd.playerDecks.put(player1Id, new ArrayList<>(List.of(firstCard, secondCard)));
        gd.playerHands.put(player1Id, new ArrayList<>());
        when(conditionEvaluationService.isMet(eq(gd), any(), any())).thenReturn(true);

        sut.resolveDrawCard(gd, player1Id);

        assertThat(gd.playerHands.get(player1Id)).containsExactly(firstCard, secondCard);
        assertThat(gd.playerDecks.get(player1Id)).isEmpty();
    }

    @Test
    void quantumRiddlerAddsOneCardToTheWholeDrawInstruction() {
        Card sourceCard = createCard("Quantum Riddler", CardType.CREATURE);
        sourceCard.addStaticEffect(new ConditionalEffect(
                new CardsInHandAtMost(1), new QuantumRiddlerDrawReplacementEffect()));
        gd.playerBattlefields.get(player1Id).add(new Permanent(sourceCard));

        Card firstCard = createCard("First card", CardType.CREATURE);
        Card secondCard = createCard("Second card", CardType.CREATURE);
        Card thirdCard = createCard("Third card", CardType.CREATURE);
        gd.playerDecks.put(player1Id, new ArrayList<>(List.of(firstCard, secondCard, thirdCard)));
        gd.playerHands.put(player1Id, new ArrayList<>());
        when(conditionEvaluationService.isMet(eq(gd), any(), any())).thenReturn(true);

        sut.resolveDrawCards(gd, player1Id, 2);

        assertThat(gd.playerHands.get(player1Id)).containsExactly(firstCard, secondCard, thirdCard);
        assertThat(gd.playerDecks.get(player1Id)).isEmpty();
    }

    @Test
    void firstNonDrawStepReplacementAppliesOnlyOncePerTurn() {
        Card sourceCard = createCard("Reed Richards, Smartest Man", CardType.CREATURE);
        sourceCard.addStaticEffect(new FirstNonDrawStepDrawFourReplacementEffect());
        gd.playerBattlefields.get(player1Id).add(new Permanent(sourceCard));

        Card firstCard = createCard("First card", CardType.CREATURE);
        Card secondCard = createCard("Second card", CardType.CREATURE);
        Card thirdCard = createCard("Third card", CardType.CREATURE);
        Card fourthCard = createCard("Fourth card", CardType.CREATURE);
        Card fifthCard = createCard("Fifth card", CardType.CREATURE);
        gd.playerDecks.put(player1Id, new ArrayList<>(List.of(
                firstCard, secondCard, thirdCard, fourthCard, fifthCard)));
        gd.playerHands.put(player1Id, new ArrayList<>());
        gd.currentStep = TurnStep.PRECOMBAT_MAIN;

        sut.resolveDrawCard(gd, player1Id);
        sut.resolveDrawCard(gd, player1Id);

        assertThat(gd.playerHands.get(player1Id)).hasSize(5);
        assertThat(gd.playerDecks.get(player1Id)).isEmpty();
    }

    @Test
    void emptyLibraryDrawReplacementOnlySkipsControllerDraw() {
        Card sourceCard = createCard("Living Conundrum", CardType.CREATURE);
        sourceCard.addStaticEffect(new LivingConundrumDrawReplacementEffect());
        gd.playerBattlefields.get(player1Id).add(new Permanent(sourceCard));
        gd.playerDecks.put(player1Id, new ArrayList<>());
        gd.playerHands.put(player1Id, new ArrayList<>());

        Card opponentCard = createCard("Opponent card", CardType.CREATURE);
        gd.playerDecks.put(player2Id, new ArrayList<>(List.of(opponentCard)));
        gd.playerHands.put(player2Id, new ArrayList<>());

        sut.resolveDrawCard(gd, player1Id);
        sut.resolveDrawCard(gd, player2Id);

        assertThat(gd.playerHands.get(player1Id)).isEmpty();
        assertThat(gd.playersAttemptedDrawFromEmptyLibrary).doesNotContain(player1Id);
        assertThat(gd.playerHands.get(player2Id)).containsExactly(opponentCard);
    }
}
