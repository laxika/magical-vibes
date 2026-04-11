package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyOneOfTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.DidntAttackConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromOwnGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayRevealSubtypeFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.NoOtherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.NotKickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameIfCreaturesInGraveyardEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StepTriggerServiceTest {

    @Mock
    private DrawService drawService;

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @Mock
    private PlayerInputService playerInputService;

    @Mock
    private PermanentRemovalService permanentRemovalService;

    @Mock
    private BattlefieldEntryService battlefieldEntryService;

    @Mock
    private TriggerCollectionService triggerCollectionService;

    private StepTriggerService sut;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        // Build the SUT manually so we can pass a REAL TriggerTargetCollector. The collector's
        // opponent-filter / valid-target logic is exercised by several tests in this class, so a
        // mock would silently return nulls and break them.
        TriggerTargetCollector triggerTargetCollector = new TriggerTargetCollector(gameQueryService);
        sut = new StepTriggerService(
                drawService,
                gameQueryService,
                gameBroadcastService,
                playerInputService,
                permanentRemovalService,
                battlefieldEntryService,
                triggerCollectionService,
                triggerTargetCollector);

        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.status = GameStatus.RUNNING;
        gd.activePlayerId = player1Id;
        gd.playerBattlefields.put(player1Id, new ArrayList<>());
        gd.playerBattlefields.put(player2Id, new ArrayList<>());
        gd.playerHands.put(player1Id, new ArrayList<>());
        gd.playerHands.put(player2Id, new ArrayList<>());
        gd.playerGraveyards.put(player1Id, new ArrayList<>());
        gd.playerGraveyards.put(player2Id, new ArrayList<>());
    }

    @Nested
    @DisplayName("handleDrawStep")
    class HandleDrawStep {

        @Test
        @DisplayName("Starting player skips draw on turn 1")
        void startingPlayerSkipsDrawOnTurn1() {
            gd.turnNumber = 1;
            gd.startingPlayerId = player1Id;

            sut.handleDrawStep(gd);

            verify(drawService, never()).resolveDrawCard(gd, player1Id);
            verify(gameBroadcastService).logAndBroadcast(gd, "Player1 skips the draw (first turn).");
        }

        @Test
        @DisplayName("Active player draws a card on turn 2+")
        void activePlayerDrawsCardOnTurn2() {
            gd.turnNumber = 2;

            sut.handleDrawStep(gd);

            verify(drawService).resolveDrawCard(gd, player1Id);
        }

        @Test
        @DisplayName("Non-starting player draws on turn 1")
        void nonStartingPlayerDrawsOnTurn1() {
            gd.activePlayerId = player2Id;
            gd.turnNumber = 1;
            gd.startingPlayerId = player1Id;

            sut.handleDrawStep(gd);

            verify(drawService).resolveDrawCard(gd, player2Id);
        }

        @Test
        @DisplayName("DRAW_TRIGGERED effect on active player's permanent pushes trigger onto stack")
        void drawTriggeredEffectPushesToStack() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Temple Bell");
            card.addEffect(EffectSlot.DRAW_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleDrawStep(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Temple Bell");
        }

        @Test
        @DisplayName("DRAW_TRIGGERED MayEffect is queued onto the stack via queueMayAbility")
        void drawTriggeredMayEffectQueuedOntoStack() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Mystic Card");
            card.addEffect(EffectSlot.DRAW_TRIGGERED, new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleDrawStep(gd);

            // queueMayAbility adds a StackEntry containing the MayEffect
            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Mystic Card");
        }

        @Test
        @DisplayName("EACH_DRAW_TRIGGERED fires for all players' draw steps")
        void eachDrawTriggeredFiresForAllPlayers() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Howling Mine");
            card.addEffect(EffectSlot.EACH_DRAW_TRIGGERED, new DrawCardForTargetPlayerEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handleDrawStep(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Howling Mine");
        }

        @Test
        @DisplayName("EACH_DRAW_TRIGGERED skips when source requires untapped and is tapped")
        void eachDrawTriggeredSkipsWhenSourceTapped() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Howling Mine");
            card.addEffect(EffectSlot.EACH_DRAW_TRIGGERED, new DrawCardForTargetPlayerEffect(1, true));
            Permanent perm = new Permanent(card);
            perm.tap();
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handleDrawStep(gd);

            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("handleUpkeepTriggers")
    class HandleUpkeepTriggers {

        @Test
        @DisplayName("Permanent with upkeep effect pushes trigger onto stack")
        void permanentWithUpkeepEffectPushesTrigger() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Venser's Journal");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Venser's Journal");
        }

        @Test
        @DisplayName("No triggers pushed when no permanents with upkeep effects")
        void noTriggersWhenNoPermanentsWithUpkeepEffects() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Opponent's upkeep-triggered permanent does not trigger during active player's upkeep")
        void opponentUpkeepTriggeredDoesNotFireForActivePlayer() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Venser's Journal");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("MayEffect queues may ability instead of pushing to stack")
        void mayEffectQueuesMayAbility() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Optional Card");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            // MayEffect goes through queueMayAbility which adds to stack
            assertThat(gd.stack).isNotEmpty();
            verify(playerInputService).processNextMayAbility(gd);
        }

        @Test
        @DisplayName("Player-targeting upkeep effect triggers target selection")
        void playerTargetingEffectTriggersTargetSelection() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Bloodgift Demon");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new DrawCardForTargetPlayerEffect(1, false, true));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            // processNextUpkeepPlayerTarget consumes the pending trigger and asks for target selection
            verify(playerInputService).beginAnyTargetChoice(eq(gd), eq(player1Id), any(), any(), any());
        }

        @Test
        @DisplayName("BecomeCopyOfTargetCreatureEffect triggers copy target selection when valid targets exist")
        void copyEffectTriggersCopyTargetSelection() {
            gd.turnNumber = 2;
            Card sourceCard = createCardWithName("Clone Shell");
            sourceCard.addEffect(EffectSlot.UPKEEP_TRIGGERED, new BecomeCopyOfTargetCreatureEffect());
            Permanent sourcePerm = new Permanent(sourceCard);
            gd.playerBattlefields.get(player1Id).add(sourcePerm);

            Card targetCard = createCardWithName("Target Creature");
            Permanent targetPerm = new Permanent(targetCard);
            gd.playerBattlefields.get(player2Id).add(targetPerm);

            when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenAnswer(inv -> {
                Permanent p = inv.getArgument(1);
                return p.getId().equals(targetPerm.getId());
            });

            sut.handleUpkeepTriggers(gd);

            // processNextUpkeepCopyTarget consumes the trigger and asks for target selection
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id), any(), any());
        }

        @Test
        @DisplayName("BecomeCopyOfTargetCreatureEffect skipped when no valid creature targets")
        void copyEffectSkippedWhenNoTargets() {
            gd.turnNumber = 2;
            Card sourceCard = createCardWithName("Clone Shell");
            sourceCard.addEffect(EffectSlot.UPKEEP_TRIGGERED, new BecomeCopyOfTargetCreatureEffect());
            gd.playerBattlefields.get(player1Id).add(new Permanent(sourceCard));
            // No other permanents on any battlefield — source is skipped, so isCreature is never called

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.pendingUpkeepCopyTargets).isEmpty();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("DestroyOneOfTargetsAtRandomEffect triggers own-target selection")
        void destroyAtRandomTriggersOwnTargetSelection() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Capricious Efreet");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new DestroyOneOfTargetsAtRandomEffect());
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            // processNextCapriciousEfreetTarget consumes the trigger and asks for permanent choice
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id), any(), any());
        }

        @Test
        @DisplayName("NoOtherSubtypeConditionalEffect triggers when no other permanents share the subtype")
        void noOtherSubtypeTriggersWhenConditionMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Tribal Card");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new NoOtherSubtypeConditionalEffect(CardSubtype.HUMAN, new GainLifeEffect(1)));
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);
            // Only one permanent — no other permanents to match, so condition is met

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Tribal Card");
        }

        @Test
        @DisplayName("NoOtherSubtypeConditionalEffect does not trigger when other permanents share the subtype")
        void noOtherSubtypeDoesNotTriggerWhenOtherExists() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Tribal Card");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new NoOtherSubtypeConditionalEffect(CardSubtype.HUMAN, new GainLifeEffect(1)));
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            Card otherCard = createCardWithName("Other Human");
            Permanent otherPerm = new Permanent(otherCard);
            gd.playerBattlefields.get(player1Id).add(otherPerm);

            when(gameQueryService.matchesPermanentPredicate(eq(gd), eq(otherPerm), any())).thenReturn(true);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("WinGameIfCreaturesInGraveyardEffect triggers when threshold met")
        void winGameTriggersWhenThresholdMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Mortal Combat");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new WinGameIfCreaturesInGraveyardEffect(20));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            // Add 20 creature cards to graveyard
            for (int i = 0; i < 20; i++) {
                Card creature = new Card();
                creature.setType(CardType.CREATURE);
                gd.playerGraveyards.get(player1Id).add(creature);
            }

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Mortal Combat");
        }

        @Test
        @DisplayName("WinGameIfCreaturesInGraveyardEffect does not trigger when threshold not met")
        void winGameDoesNotTriggerWhenThresholdNotMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Mortal Combat");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new WinGameIfCreaturesInGraveyardEffect(20));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            // Add only 5 creatures
            for (int i = 0; i < 5; i++) {
                Card creature = new Card();
                creature.setType(CardType.CREATURE);
                gd.playerGraveyards.get(player1Id).add(creature);
            }

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("GRAVEYARD_UPKEEP_TRIGGERED pushes trigger from graveyard card")
        void graveyardUpkeepTriggeredPushesToStack() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Graveyard Card");
            card.addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerGraveyards.get(player1Id).add(card);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Graveyard Card");
        }

        @Test
        @DisplayName("GRAVEYARD_UPKEEP_TRIGGERED with MayPayManaEffect queues onto stack")
        void graveyardUpkeepMayPayQueuesOntoStack() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Graveyard May Card");
            card.addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                    new MayPayManaEffect("{2}", new GainLifeEffect(1), "Pay {2} to gain life?"));
            gd.playerGraveyards.get(player1Id).add(card);

            sut.handleUpkeepTriggers(gd);

            // MayPayManaEffect goes through queueMayAbility, which adds to stack
            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Graveyard May Card");
        }

        @Test
        @DisplayName("GRAVEYARD_UPKEEP_TRIGGERED with MayEffect queues may ability")
        void graveyardUpkeepMayEffectQueuesAbility() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Graveyard May Card");
            card.addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                    new MayEffect(new GainLifeEffect(1), "Gain life?"));
            gd.playerGraveyards.get(player1Id).add(card);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
        }

        @Test
        @DisplayName("GRAVEYARD_UPKEEP_TRIGGERED with MetalcraftConditionalEffect skips when metalcraft not met")
        void graveyardMetalcraftSkipsWhenNotMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Metalcraft Card");
            card.addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                    new MetalcraftConditionalEffect(new MayPayManaEffect("{2}", new GainLifeEffect(1), "Pay?")));
            gd.playerGraveyards.get(player1Id).add(card);

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.pendingMayAbilities).isEmpty();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("GRAVEYARD_UPKEEP_TRIGGERED with MetalcraftConditionalEffect triggers when metalcraft met")
        void graveyardMetalcraftTriggersWhenMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Metalcraft Card");
            card.addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                    new MetalcraftConditionalEffect(new MayPayManaEffect("{2}", new GainLifeEffect(1), "Pay?")));
            gd.playerGraveyards.get(player1Id).add(card);

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(true);

            sut.handleUpkeepTriggers(gd);

            // MayPayManaEffect goes through queueMayAbility, which adds to stack
            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Metalcraft Card");
        }

        @Test
        @DisplayName("EACH_UPKEEP_TRIGGERED fires for all players' permanents")
        void eachUpkeepTriggeredFiresForAllPlayers() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Each Upkeep Card");
            card.addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Each Upkeep Card");
        }

        @Test
        @DisplayName("EACH_UPKEEP_TRIGGERED with NoSpellsCastLastTurnConditionalEffect triggers when no spells cast")
        void noSpellsCastTriggersWhenConditionMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Werewolf");
            card.addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                    new NoSpellsCastLastTurnConditionalEffect(new GainLifeEffect(1)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            // No spells cast last turn (empty map)

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
        }

        @Test
        @DisplayName("EACH_UPKEEP_TRIGGERED with NoSpellsCastLastTurnConditionalEffect skips when spells were cast")
        void noSpellsCastSkipsWhenSpellsWereCast() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Werewolf");
            card.addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                    new NoSpellsCastLastTurnConditionalEffect(new GainLifeEffect(1)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            gd.spellsCastLastTurn.put(player1Id, 1);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("EACH_UPKEEP_TRIGGERED with TwoOrMoreSpellsCastLastTurnConditionalEffect triggers when condition met")
        void twoOrMoreSpellsTriggersWhenConditionMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Werewolf Reverse");
            card.addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                    new TwoOrMoreSpellsCastLastTurnConditionalEffect(new GainLifeEffect(1)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            gd.spellsCastLastTurn.put(player1Id, 2);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
        }

        @Test
        @DisplayName("EACH_UPKEEP_TRIGGERED with TwoOrMoreSpellsCastLastTurnConditionalEffect skips when no one cast two")
        void twoOrMoreSpellsSkipsWhenNotMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Werewolf Reverse");
            card.addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                    new TwoOrMoreSpellsCastLastTurnConditionalEffect(new GainLifeEffect(1)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            gd.spellsCastLastTurn.put(player1Id, 1);
            gd.spellsCastLastTurn.put(player2Id, 0);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("OPPONENT_UPKEEP_TRIGGERED fires for opponent's permanents during active player's upkeep")
        void opponentUpkeepTriggeredFiresDuringActivePlayersUpkeep() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Opponent Trigger Card");
            card.addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Opponent Trigger Card");
        }

        @Test
        @DisplayName("OPPONENT_UPKEEP_TRIGGERED does not fire for active player's own permanents")
        void opponentUpkeepTriggeredDoesNotFireForOwnPermanents() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Opponent Trigger Card");
            card.addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("OPPONENT_UPKEEP_TRIGGERED with DealDamageIfFewCardsInHandEffect triggers when hand size meets condition")
        void opponentUpkeepFewCardsTriggersWhenConditionMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Punisher Card");
            card.addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new DealDamageIfFewCardsInHandEffect(3, 2));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            // Active player has 2 cards in hand (≤ 3)
            gd.playerHands.get(player1Id).add(new Card());
            gd.playerHands.get(player1Id).add(new Card());

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
        }

        @Test
        @DisplayName("OPPONENT_UPKEEP_TRIGGERED with DealDamageIfFewCardsInHandEffect skips when hand too large")
        void opponentUpkeepFewCardsSkipsWhenHandTooLarge() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Punisher Card");
            card.addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new DealDamageIfFewCardsInHandEffect(3, 2));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            // Active player has 5 cards in hand (> 3)
            for (int i = 0; i < 5; i++) {
                gd.playerHands.get(player1Id).add(new Card());
            }

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED fires when enchanted permanent's controller is active")
        void enchantedPermanentControllerUpkeepFires() {
            gd.turnNumber = 2;
            // Create the enchanted permanent (controlled by active player)
            Card targetCard = createCardWithName("Enchanted Creature");
            Permanent targetPerm = new Permanent(targetCard);
            gd.playerBattlefields.get(player1Id).add(targetPerm);

            // Create the aura with the trigger (owned by player2)
            Card auraCard = createCardWithName("Numbing Dose");
            auraCard.addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                    new EnchantedCreatureControllerLosesLifeEffect(1, null));
            Permanent auraPerm = new Permanent(auraCard);
            auraPerm.setAttachedTo(targetPerm.getId());
            gd.playerBattlefields.get(player2Id).add(auraPerm);

            when(gameQueryService.findPermanentController(gd, targetPerm.getId())).thenReturn(player1Id);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Numbing Dose");
        }

        @Test
        @DisplayName("ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED does not fire when enchanted permanent's controller is not active")
        void enchantedPermanentControllerUpkeepDoesNotFireWhenNotActive() {
            gd.turnNumber = 2;
            // Enchanted permanent controlled by player2 (not active)
            Card targetCard = createCardWithName("Enchanted Creature");
            Permanent targetPerm = new Permanent(targetCard);
            gd.playerBattlefields.get(player2Id).add(targetPerm);

            Card auraCard = createCardWithName("Numbing Dose");
            auraCard.addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                    new EnchantedCreatureControllerLosesLifeEffect(1, null));
            Permanent auraPerm = new Permanent(auraCard);
            auraPerm.setAttachedTo(targetPerm.getId());
            gd.playerBattlefields.get(player1Id).add(auraPerm);

            when(gameQueryService.findPermanentController(gd, targetPerm.getId())).thenReturn(player2Id);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("ENCHANTED_PLAYER_UPKEEP_TRIGGERED fires when enchanted player is active")
        void enchantedPlayerUpkeepFires() {
            gd.turnNumber = 2;
            Card curseCard = createCardWithName("Curse of Oblivion");
            curseCard.addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
                    new ExileCardsFromOwnGraveyardEffect(2, null));
            Permanent cursePerm = new Permanent(curseCard);
            cursePerm.setAttachedTo(player1Id); // Attached to active player
            gd.playerBattlefields.get(player2Id).add(cursePerm);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Curse of Oblivion");
        }

        @Test
        @DisplayName("ENCHANTED_PLAYER_UPKEEP_TRIGGERED does not fire when enchanted player is not active")
        void enchantedPlayerUpkeepDoesNotFireWhenNotActive() {
            gd.turnNumber = 2;
            Card curseCard = createCardWithName("Curse of Oblivion");
            curseCard.addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
                    new ExileCardsFromOwnGraveyardEffect(2, null));
            Permanent cursePerm = new Permanent(curseCard);
            cursePerm.setAttachedTo(player2Id); // Attached to non-active player
            gd.playerBattlefields.get(player1Id).add(cursePerm);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("ENCHANTED_PLAYER_UPKEEP_TRIGGERED bakes DealDamageToEnchantedPlayerEffect with player ID")
        void enchantedPlayerUpkeepBakesDamageEffect() {
            gd.turnNumber = 2;
            Card curseCard = createCardWithName("Curse of the Bloody Tome");
            curseCard.addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
                    new DealDamageToEnchantedPlayerEffect(2, null));
            Permanent cursePerm = new Permanent(curseCard);
            cursePerm.setAttachedTo(player1Id);
            gd.playerBattlefields.get(player2Id).add(cursePerm);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            DealDamageToEnchantedPlayerEffect bakedEffect =
                    (DealDamageToEnchantedPlayerEffect) gd.stack.getFirst().getEffectsToResolve().getFirst();
            assertThat(bakedEffect.affectedPlayerId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("Opening hand triggers fire on turn 1 and push to stack")
        void openingHandTriggersFireOnTurn1() {
            gd.turnNumber = 1;
            Card card = createCardWithName("Chancellor");
            card.addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new GainLifeEffect(7));
            gd.playerHands.get(player1Id).add(card);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Chancellor");
        }

        @Test
        @DisplayName("Opening hand MayEffect queues may ability on turn 1")
        void openingHandMayEffectQueuesMayAbility() {
            gd.turnNumber = 1;
            Card card = createCardWithName("Chancellor May");
            card.addEffect(EffectSlot.ON_OPENING_HAND_REVEAL,
                    new MayEffect(new GainLifeEffect(1), "Do something?"));
            gd.playerHands.get(player1Id).add(card);

            sut.handleUpkeepTriggers(gd);

            // queueMayAbility adds to stack
            assertThat(gd.stack).isNotEmpty();
        }

        @Test
        @DisplayName("Opening hand Leyline MayEffect is skipped (handled during pregame)")
        void openingHandLeylineSkipped() {
            gd.turnNumber = 1;
            Card card = createCardWithName("Leyline");
            card.addEffect(EffectSlot.ON_OPENING_HAND_REVEAL,
                    new MayEffect(new LeylineStartOnBattlefieldEffect(), "Put onto battlefield?"));
            gd.playerHands.get(player1Id).add(card);

            sut.handleUpkeepTriggers(gd);

            // Leyline effects are skipped, stack should be empty
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Opening hand triggers do not fire on turn 2+")
        void openingHandTriggersDoNotFireOnTurn2() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Chancellor");
            card.addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new GainLifeEffect(7));
            gd.playerHands.get(player1Id).add(card);

            sut.handleUpkeepTriggers(gd);

            // Turn 2 — handleOpeningHandTriggers is not called
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("MayRevealSubtypeFromHandEffect queues may ability when hand contains matching subtype")
        void mayRevealSubtypeQueuesWhenSubtypeInHand() {
            gd.turnNumber = 2;
            Card priestCard = createCardWithName("Priest of the Wakening Sun");
            priestCard.addEffect(EffectSlot.UPKEEP_TRIGGERED,
                    new MayRevealSubtypeFromHandEffect(CardSubtype.DINOSAUR, new GainLifeEffect(2),
                            "Reveal a Dinosaur card from your hand to gain 2 life?"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(priestCard));

            // Add a Dinosaur card to the player's hand
            Card dinosaur = createCardWithName("Frenzied Raptor");
            dinosaur.setSubtypes(List.of(CardSubtype.DINOSAUR));
            gd.playerHands.get(player1Id).add(dinosaur);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst()).isInstanceOf(MayEffect.class);
        }

        @Test
        @DisplayName("MayRevealSubtypeFromHandEffect does not queue when hand lacks matching subtype")
        void mayRevealSubtypeSkipsWhenNoSubtypeInHand() {
            gd.turnNumber = 2;
            Card priestCard = createCardWithName("Priest of the Wakening Sun");
            priestCard.addEffect(EffectSlot.UPKEEP_TRIGGERED,
                    new MayRevealSubtypeFromHandEffect(CardSubtype.DINOSAUR, new GainLifeEffect(2),
                            "Reveal a Dinosaur card from your hand to gain 2 life?"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(priestCard));

            // Add a non-Dinosaur card to the player's hand
            Card bear = createCardWithName("Grizzly Bears");
            bear.setSubtypes(List.of(CardSubtype.BEAR));
            gd.playerHands.get(player1Id).add(bear);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("MayRevealSubtypeFromHandEffect does not queue when hand is empty")
        void mayRevealSubtypeSkipsWhenHandEmpty() {
            gd.turnNumber = 2;
            Card priestCard = createCardWithName("Priest of the Wakening Sun");
            priestCard.addEffect(EffectSlot.UPKEEP_TRIGGERED,
                    new MayRevealSubtypeFromHandEffect(CardSubtype.DINOSAUR, new GainLifeEffect(2),
                            "Reveal a Dinosaur card from your hand to gain 2 life?"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(priestCard));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("handleEndStepTriggers")
    class HandleEndStepTriggers {

        @Test
        @DisplayName("Permanent with controller end-step effect pushes trigger onto stack")
        void controllerEndStepEffectPushesTrigger() {
            Card card = createCardWithName("Jin-Gitaxias, Core Augur");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Jin-Gitaxias, Core Augur");
        }

        @Test
        @DisplayName("Controller end-step permanent does not trigger during opponent's end step")
        void controllerEndStepDoesNotTriggerDuringOpponentEndStep() {
            gd.activePlayerId = player2Id;
            Card card = createCardWithName("Jin-Gitaxias, Core Augur");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("No triggers pushed when no permanents with end-step effects")
        void noTriggersWhenNoPermanentsWithEndStepEffects() {
            Card card = createCardWithName("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("END_STEP_TRIGGERED fires for any player's permanents")
        void endStepTriggeredFiresForAnyPlayer() {
            Card card = createCardWithName("End Step Card");
            card.addEffect(EffectSlot.END_STEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("End Step Card");
        }

        @Test
        @DisplayName("END_STEP_TRIGGERED with MayEffect queues may ability")
        void endStepMayEffectQueuesMayAbility() {
            Card card = createCardWithName("Optional End Step Card");
            card.addEffect(EffectSlot.END_STEP_TRIGGERED, new MayEffect(new GainLifeEffect(1), "Gain life?"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            // MayEffect goes through queueMayAbility which adds to stack
            assertThat(gd.stack).isNotEmpty();
        }

        @Test
        @DisplayName("CONTROLLER_END_STEP_TRIGGERED with MayEffect queues may ability")
        void controllerEndStepMayEffectQueuesMayAbility() {
            Card card = createCardWithName("Controller May Card");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    new MayEffect(new GainLifeEffect(1), "Gain life?"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
        }

        @Test
        @DisplayName("DidntAttackConditionalEffect triggers when creature did not attack this turn")
        void didntAttackTriggersWhenNotAttacked() {
            Card card = createCardWithName("Vigilant Creature");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    new DidntAttackConditionalEffect(new GainLifeEffect(1)));
            Permanent perm = new Permanent(card);
            // Did not attack this turn (default)
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Vigilant Creature");
        }

        @Test
        @DisplayName("DidntAttackConditionalEffect skips when creature attacked this turn")
        void didntAttackSkipsWhenAttacked() {
            Card card = createCardWithName("Vigilant Creature");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    new DidntAttackConditionalEffect(new GainLifeEffect(1)));
            Permanent perm = new Permanent(card);
            perm.setAttackedThisTurn(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("CONTROLLER_END_STEP_TRIGGERED with RaidConditionalEffect wrapping MayEffect queues may ability when raid met")
        void raidConditionalEndStepMayEffectQueuedWhenRaidMet() {
            Card card = createCardWithName("Raiding Looter");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    new RaidConditionalEffect(new MayEffect(new GainLifeEffect(1), "Gain life?")));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            gd.playersDeclaredAttackersThisTurn.add(player1Id);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
        }

        @Test
        @DisplayName("CONTROLLER_END_STEP_TRIGGERED with RaidConditionalEffect skips when raid not met")
        void raidConditionalEndStepSkipsWhenRaidNotMet() {
            Card card = createCardWithName("Raiding Looter");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    new RaidConditionalEffect(new MayEffect(new GainLifeEffect(1), "Gain life?")));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            // Do NOT add player1Id to playersDeclaredAttackersThisTurn

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("CONTROLLER_END_STEP_TRIGGERED with RaidConditionalEffect wrapping non-MayEffect pushes to stack when raid met")
        void raidConditionalEndStepNonMayPushesToStackWhenRaidMet() {
            Card card = createCardWithName("Raiding Creature");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    new RaidConditionalEffect(new GainLifeEffect(1)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            gd.playersDeclaredAttackersThisTurn.add(player1Id);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Raiding Creature");
        }

        @Test
        @DisplayName("CONTROLLER_END_STEP_TRIGGERED with RaidConditionalEffect wrapping targeting effect queues for target selection when raid met")
        void raidConditionalEndStepTargetingEffectQueuesWhenRaidMet() {
            Card card = createCardWithName("Navigator's Ruin");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    new RaidConditionalEffect(new MillTargetPlayerEffect(4)));
            card.setCastTimeTargetFilter(new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                    "Target must be an opponent"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            gd.playersDeclaredAttackersThisTurn.add(player1Id);

            sut.handleEndStepTriggers(gd);

            // Should not push directly to stack — should queue for target selection
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingEndStepTriggerTargets).isEmpty(); // processed immediately
            // processNextEndStepTriggerTarget fires and presents choice
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id), any(), any());
        }

        @Test
        @DisplayName("CONTROLLER_END_STEP_TRIGGERED with RaidConditionalEffect wrapping targeting effect skips when raid not met")
        void raidConditionalEndStepTargetingEffectSkipsWhenRaidNotMet() {
            Card card = createCardWithName("Navigator's Ruin");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    new RaidConditionalEffect(new MillTargetPlayerEffect(4)));
            card.setCastTimeTargetFilter(new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                    "Target must be an opponent"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            // Do NOT add player1Id to playersDeclaredAttackersThisTurn

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingEndStepTriggerTargets).isEmpty();
            verify(playerInputService, never()).beginPermanentChoice(any(), any(), any(), any());
        }

        @Test
        @DisplayName("processNextEndStepTriggerTarget with PlayerPredicateTargetFilter OPPONENT excludes controller from valid targets")
        void endStepTriggerOpponentFilterExcludesController() {
            Card card = createCardWithName("Navigator's Ruin");
            card.setCastTimeTargetFilter(new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                    "Target must be an opponent"));
            gd.pendingEndStepTriggerTargets.add(new PermanentChoiceContext.EndStepTriggerTarget(
                    card, player1Id, new ArrayList<>(List.of(new MillTargetPlayerEffect(4))),
                    UUID.randomUUID()));

            sut.processNextEndStepTriggerTarget(gd);

            // Should present choice with only opponent (player2), not controller (player1)
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                    eq(List.of(player2Id)), any());
        }

        @Test
        @DisplayName("NotKickedConditionalEffect triggers when permanent was not kicked")
        void notKickedTriggersWhenNotKicked() {
            Card card = createCardWithName("Unkicked Elemental");
            card.addEffect(EffectSlot.END_STEP_TRIGGERED,
                    new NotKickedConditionalEffect(new SacrificeSelfEffect()));
            Permanent perm = new Permanent(card);
            // Not kicked (default)
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Unkicked Elemental");
        }

        @Test
        @DisplayName("NotKickedConditionalEffect skips when permanent was kicked")
        void notKickedSkipsWhenKicked() {
            Card card = createCardWithName("Kicked Elemental");
            card.addEffect(EffectSlot.END_STEP_TRIGGERED,
                    new NotKickedConditionalEffect(new SacrificeSelfEffect()));
            Permanent perm = new Permanent(card);
            perm.setKicked(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Pending token exiles are processed at end step")
        void pendingTokenExilesProcessed() {
            Card tokenCard = createCardWithName("Token");
            Permanent token = new Permanent(tokenCard);
            gd.playerBattlefields.get(player1Id).add(token);
            gd.pendingTokenExilesAtEndStep.add(token.getId());

            when(gameQueryService.findPermanentById(gd, token.getId())).thenReturn(token);

            sut.handleEndStepTriggers(gd);

            verify(permanentRemovalService).removePermanentToExile(gd, token);
            verify(permanentRemovalService).removeOrphanedAuras(gd);
            assertThat(gd.pendingTokenExilesAtEndStep).isEmpty();
        }

        @Test
        @DisplayName("Pending destroy at end step destroys permanents")
        void pendingDestroyAtEndStepProcessed() {
            Card card = createCardWithName("Doomed Creature");
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);
            gd.pendingDestroyAtEndStep.add(perm.getId());

            when(gameQueryService.findPermanentById(gd, perm.getId())).thenReturn(perm);
            when(permanentRemovalService.tryDestroyPermanent(gd, perm)).thenReturn(true);

            sut.handleEndStepTriggers(gd);

            verify(permanentRemovalService).tryDestroyPermanent(gd, perm);
            assertThat(gd.pendingDestroyAtEndStep).isEmpty();
        }

        @Test
        @DisplayName("Pending exile returns return cards from exile to battlefield")
        void pendingExileReturnsProcessed() {
            Card card = createCardWithName("Exiled Card");
            gd.getPlayerExiledCards(player1Id).add(card);
            gd.pendingExileReturns.add(new PendingExileReturn(card, player1Id, false));

            sut.handleEndStepTriggers(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player1Id), any(Permanent.class));
            verify(battlefieldEntryService).handleCreatureEnteredBattlefield(eq(gd), eq(player1Id), eq(card), any(), eq(false));
            assertThat(gd.getPlayerExiledCards(player1Id)).doesNotContain(card);
            assertThat(gd.pendingExileReturns).isEmpty();
        }

        @Test
        @DisplayName("Pending exile returns with returnTapped taps the permanent")
        void pendingExileReturnsTapped() {
            Card card = createCardWithName("Exiled Card");
            gd.getPlayerExiledCards(player1Id).add(card);
            gd.pendingExileReturns.add(new PendingExileReturn(card, player1Id, true));

            sut.handleEndStepTriggers(gd);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player1Id), any(Permanent.class));
        }

        @Test
        @DisplayName("Delayed +1/+1 counter triggers push onto stack")
        void delayedPlusOnePlusOneCountersPushed() {
            Card card = createCardWithName("Protean Hydra");
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);
            // 4 total counters to add = 2 triggers (each adds 2)
            gd.pendingDelayedPlusOnePlusOneCounters.put(perm.getId(), 4);

            when(gameQueryService.findPermanentById(gd, perm.getId())).thenReturn(perm);
            when(gameQueryService.findPermanentController(gd, perm.getId())).thenReturn(player1Id);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack.getFirst().getDescription()).contains("Protean Hydra");
            assertThat(gd.pendingDelayedPlusOnePlusOneCounters).isEmpty();
        }
    }

    @Nested
    @DisplayName("handlePrecombatMainTriggers")
    class HandlePrecombatMainTriggers {

        @Test
        @DisplayName("Does nothing when there are no opening hand mana triggers")
        void doesNothingWithNoOpeningHandTriggers() {
            sut.handlePrecombatMainTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Fires mana trigger for active player")
        void firesTriggerForActivePlayer() {
            Card card = createCardWithName("Chancellor of the Tangle");
            GainLifeEffect effect = new GainLifeEffect(1);
            gd.openingHandManaTriggers.add(new OpeningHandRevealTrigger(player1Id, card, effect));

            sut.handlePrecombatMainTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Chancellor of the Tangle");
            assertThat(gd.openingHandManaTriggers).isEmpty();
        }

        @Test
        @DisplayName("Does not fire trigger for non-active player")
        void doesNotFireTriggerForNonActivePlayer() {
            Card card = createCardWithName("Chancellor of the Tangle");
            GainLifeEffect effect = new GainLifeEffect(1);
            gd.openingHandManaTriggers.add(new OpeningHandRevealTrigger(player2Id, card, effect));

            sut.handlePrecombatMainTriggers(gd);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.openingHandManaTriggers).hasSize(1); // Not removed
        }

        @Test
        @DisplayName("Saga on active player's battlefield gets a lore counter at precombat main")
        void sagaGetsLoreCounterAtPrecombatMain() {
            Card saga = createSaga("Test Saga");
            Permanent perm = new Permanent(saga);
            perm.setLoreCounters(1); // already has 1 from ETB
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handlePrecombatMainTriggers(gd);

            assertThat(perm.getLoreCounters()).isEqualTo(2);
        }

        @Test
        @DisplayName("Saga triggers chapter ability matching new lore counter value")
        void sagaTriggersCorrectChapter() {
            Card saga = createSaga("Test Saga");
            saga.addEffect(EffectSlot.SAGA_CHAPTER_II, new GainLifeEffect(3));
            Permanent perm = new Permanent(saga);
            perm.setLoreCounters(1); // chapter II will trigger when counter goes to 2
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handlePrecombatMainTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getDescription()).contains("chapter II");
            assertThat(gd.stack.getFirst().getEffectsToResolve()).hasSize(1);
        }

        @Test
        @DisplayName("Saga chapter ability has correct source permanent ID")
        void sagaChapterHasSourcePermanentId() {
            Card saga = createSaga("Test Saga");
            saga.addEffect(EffectSlot.SAGA_CHAPTER_II, new GainLifeEffect(3));
            Permanent perm = new Permanent(saga);
            perm.setLoreCounters(1);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handlePrecombatMainTriggers(gd);

            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("Non-active player's Saga does not get a lore counter")
        void opponentSagaNotAffected() {
            Card saga = createSaga("Opponent Saga");
            saga.addEffect(EffectSlot.SAGA_CHAPTER_II, new GainLifeEffect(3));
            Permanent perm = new Permanent(saga);
            perm.setLoreCounters(1);
            gd.playerBattlefields.get(player2Id).add(perm);

            sut.handlePrecombatMainTriggers(gd);

            assertThat(perm.getLoreCounters()).isEqualTo(1); // unchanged
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Non-saga permanents are not affected by saga lore counter logic")
        void nonSagaPermanentNotAffected() {
            Card creature = createCardWithName("Regular Creature");
            creature.setType(CardType.CREATURE);
            Permanent perm = new Permanent(creature);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handlePrecombatMainTriggers(gd);

            assertThat(perm.getLoreCounters()).isZero();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Multiple Sagas each get a lore counter")
        void multipleSagasEachGetLoreCounter() {
            Card saga1 = createSaga("Saga A");
            saga1.addEffect(EffectSlot.SAGA_CHAPTER_II, new GainLifeEffect(1));
            Permanent perm1 = new Permanent(saga1);
            perm1.setLoreCounters(1);

            Card saga2 = createSaga("Saga B");
            saga2.addEffect(EffectSlot.SAGA_CHAPTER_III, new GainLifeEffect(2));
            Permanent perm2 = new Permanent(saga2);
            perm2.setLoreCounters(2);

            gd.playerBattlefields.get(player1Id).add(perm1);
            gd.playerBattlefields.get(player1Id).add(perm2);

            sut.handlePrecombatMainTriggers(gd);

            assertThat(perm1.getLoreCounters()).isEqualTo(2);
            assertThat(perm2.getLoreCounters()).isEqualTo(3);
            assertThat(gd.stack).hasSize(2); // both chapters triggered
        }

        @Test
        @DisplayName("Saga with no effects for the reached chapter doesn't push to stack")
        void sagaNoEffectsForChapterDoesNotTrigger() {
            Card saga = createSaga("Sparse Saga");
            saga.addEffect(EffectSlot.SAGA_CHAPTER_I, new GainLifeEffect(1));
            // no chapter II effects
            Permanent perm = new Permanent(saga);
            perm.setLoreCounters(1); // will go to 2, but chapter II has no effects
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handlePrecombatMainTriggers(gd);

            assertThat(perm.getLoreCounters()).isEqualTo(2); // counter still incremented
            assertThat(gd.stack).isEmpty(); // but no ability triggered
        }

        @Test
        @DisplayName("Saga lore counter logs are broadcast")
        void sagaLoreCounterLogsBroadcast() {
            Card saga = createSaga("Chainer's Torment");
            saga.addEffect(EffectSlot.SAGA_CHAPTER_II, new GainLifeEffect(2));
            Permanent perm = new Permanent(saga);
            perm.setLoreCounters(1);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handlePrecombatMainTriggers(gd);

            verify(gameBroadcastService).logAndBroadcast(gd,
                    "Chainer's Torment gets a lore counter (2).");
            verify(gameBroadcastService).logAndBroadcast(gd,
                    "Chainer's Torment's chapter II ability triggers.");
        }
    }

    // ---- Test helpers ----

    private static Card createCardWithName(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private static Card createSaga(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.SAGA));
        return card;
    }
}
