package com.github.laxika.magicalvibes.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.LivingConundrumDrawReplacementEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.DredgeSupport;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.mayfx.BreathstealersCryptDrawReplacementHandler;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
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

@ExtendWith(MockitoExtension.class)
class DrawServiceTest {

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

    @InjectMocks
    private DrawService sut;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
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

    private Permanent addEquipmentWithDrawTrigger(UUID controllerId) {
        Card card = createCard("Diviner's Wand", CardType.ARTIFACT);
        card.addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(1, 1, Keyword.FLYING));
        Permanent equipment = new Permanent(card);
        gd.playerBattlefields.get(controllerId).add(equipment);
        return equipment;
    }

    // ===== checkControllerDrawTriggers — equipment-granted draw trigger =====

    @Nested
    @DisplayName("checkControllerDrawTriggers — equipment-granted draw trigger")
    class EquipmentGrantedDrawTrigger {

        @Test
        @DisplayName("does not trigger while the equipment is unattached")
        void unattachedEquipmentDoesNotTrigger() {
            addEquipmentWithDrawTrigger(player1Id);

            sut.checkControllerDrawTriggers(gd, player1Id);

            assertThat(gd.stack).isEmpty();
            verify(gameLogService, never()).append(eq(gd), any());
        }

        @Test
        @DisplayName("pushes the trigger onto the stack while the equipment is attached")
        void attachedEquipmentTriggers() {
            Permanent creature = new Permanent(createCard("Grizzly Bears", CardType.CREATURE));
            gd.playerBattlefields.get(player1Id).add(creature);
            Permanent equipment = addEquipmentWithDrawTrigger(player1Id);
            equipment.setAttachedTo(creature.getId());

            sut.checkControllerDrawTriggers(gd, player1Id);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(equipment.getId());
            assertThat(gd.stack.getFirst().getEffectsToResolve())
                    .containsExactly(new BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(1, 1, Keyword.FLYING));
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Diviner's Wand's ability triggers.")));
        }

        @Test
        @DisplayName("non-equipment draw trigger still fires while another permanent is unattached")
        void nonEquipmentDrawTriggerUnaffected() {
            addEquipmentWithDrawTrigger(player1Id); // unattached — must stay silent
            Card crawlerCard = createCard("Psychosis Crawler", CardType.CREATURE);
            crawlerCard.addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new BoostSelfEffect(1, 1));
            Permanent crawler = new Permanent(crawlerCard);
            gd.playerBattlefields.get(player1Id).add(crawler);

            sut.checkControllerDrawTriggers(gd, player1Id);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(crawler.getId());
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Psychosis Crawler's ability triggers.")));
        }
    }

    @Test
    @DisplayName("pushes a graveyard second-draw may-pay trigger onto the stack")
    void graveyardSecondDrawTriggerPushesMayPayAbility() {
        Card wolfbat = createCard("Wolfbat", CardType.CREATURE);
        wolfbat.addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_DRAWS_SECOND_CARD,
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
    void targetedSecondDrawTriggerQueuesPermanentTargetChoice() {
        Card card = createCard("Mantle of Tides", CardType.ARTIFACT);
        AttachSourceEquipmentToTargetCreatureEffect effect = new AttachSourceEquipmentToTargetCreatureEffect();
        card.target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD, effect);
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
    void conditionalDoubleDrawReplacementOnlyAppliesWhenConditionIsMet() {
        Card sourceCard = createCard("Vnwxt, Verbose Host", CardType.CREATURE);
        sourceCard.addEffect(EffectSlot.STATIC, new ConditionalEffect(
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
    void emptyLibraryDrawReplacementOnlySkipsControllerDraw() {
        Card sourceCard = createCard("Living Conundrum", CardType.CREATURE);
        sourceCard.addEffect(EffectSlot.STATIC, new LivingConundrumDrawReplacementEffect());
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
