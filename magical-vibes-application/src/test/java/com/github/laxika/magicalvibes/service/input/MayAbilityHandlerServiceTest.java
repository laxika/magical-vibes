package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.MayEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.normalfx.BrilliantUltimatumSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.BendOrBreakEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.FightOrFlightSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.StandOrFallSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.target.TargetPredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Target enumeration for a targeted may-ability whose only restriction is its effect's
 * {@code TargetSpec} — no card-level {@code TargetFilter}, no effect-level
 * {@code PermanentPredicate}. That arm used to be an open-coded target-category switch whose
 * {@code default} rejected every permanent, so a {@code LAND} spec found nothing to target; it now
 * goes through the shared {@code TargetPredicateEvaluationService}, the same evaluation cast-time
 * validation performs.
 */
class MayAbilityHandlerServiceTest {

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    private GameQueryService gameQueryService;
    private PlayerInputService playerInputService;
    private ValidTargetService validTargetService;
    private MayEffectHandlerRegistry mayEffectHandlerRegistry;
    private MayAbilityHandlerService svc;

    private GameData gd;
    private Player player1;

    @BeforeEach
    void setUp() {
        gameQueryService = mock(GameQueryService.class);
        playerInputService = mock(PlayerInputService.class);
        validTargetService = mock(ValidTargetService.class);
        mayEffectHandlerRegistry = mock(MayEffectHandlerRegistry.class);

        PredicateEvaluationService predicateEvaluationService = new PredicateEvaluationService(gameQueryService);
        TargetPredicateEvaluationService targetPredicateEvaluationService =
                new TargetPredicateEvaluationService(predicateEvaluationService, mock(TargetLegalityService.class));

        svc = new MayAbilityHandlerService(
                mock(InputCompletionService.class),
                mock(MayCastHandlerService.class),
                mock(MayCopyHandlerService.class),
                mock(MayMiscHandlerService.class),
                gameQueryService,
                predicateEvaluationService,
                mock(GameLogService.class),
                playerInputService,
                mock(TurnProgressionService.class),
                mock(EffectResolutionService.class),
                mock(DestructionSupport.class),
                mock(BendOrBreakEffectHandler.class),
                mock(FightOrFlightSupport.class),
                mock(StandOrFallSupport.class),
                mock(GraveyardReturnSupport.class),
                mock(BrilliantUltimatumSupport.class),
                mock(MayAbilityTapCostService.class),
                mock(InteractionHandlerRegistry.class),
                validTargetService,
                targetPredicateEvaluationService,
                mayEffectHandlerRegistry);

        player1 = new Player(PLAYER1_ID, "Alice");

        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Alice");
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Alice");
        gd.playerIdToName.put(PLAYER2_ID, "Bob");
        gd.playerBattlefields.put(PLAYER1_ID, new ArrayList<>());
        gd.playerBattlefields.put(PLAYER2_ID, new ArrayList<>());
    }

    @Test
    @DisplayName("A bare LAND spec offers the lands on the battlefield, not nothing")
    void landSpecOffersLands() {
        Permanent forest = permanent("Forest", CardType.LAND);
        Permanent bear = permanent("Grizzly Bears", CardType.CREATURE);
        gd.playerBattlefields.get(PLAYER1_ID).add(forest);
        gd.playerBattlefields.get(PLAYER2_ID).add(bear);
        when(gameQueryService.isLand(gd, forest)).thenReturn(true);
        when(gameQueryService.isLand(gd, bear)).thenReturn(false);

        acceptMayAbility(specEffect(TargetPredicates.land()));

        assertThat(offeredTargets()).containsExactly(forest.getId());
    }

    @Test
    @DisplayName("A bare CREATURE spec still offers only creatures")
    void creatureSpecOffersOnlyCreatures() {
        Permanent forest = permanent("Forest", CardType.LAND);
        Permanent bear = permanent("Grizzly Bears", CardType.CREATURE);
        gd.playerBattlefields.get(PLAYER1_ID).add(forest);
        gd.playerBattlefields.get(PLAYER1_ID).add(bear);
        when(gameQueryService.isCreature(gd, forest)).thenReturn(false);
        when(gameQueryService.isCreature(gd, bear)).thenReturn(true);

        acceptMayAbility(specEffect(TargetPredicates.creature()));

        assertThat(offeredTargets()).containsExactly(bear.getId());
    }

    @Test
    @DisplayName("A spec with no legal permanent reports no valid targets instead of prompting")
    void noLegalPermanentDoesNotPrompt() {
        Permanent bear = permanent("Grizzly Bears", CardType.CREATURE);
        gd.playerBattlefields.get(PLAYER1_ID).add(bear);
        when(gameQueryService.isLand(gd, bear)).thenReturn(false);

        acceptMayAbility(specEffect(TargetPredicates.land()));

        verify(playerInputService, org.mockito.Mockito.never())
                .beginPermanentChoice(any(), any(), anyList(), anyString());
    }

    private void acceptMayAbility(CardEffect effect) {
        Card sourceCard = new Card();
        sourceCard.setName("Test Source");
        sourceCard.setType(CardType.ENCHANTMENT);
        PendingMayAbility ability =
                new PendingMayAbility(sourceCard, PLAYER1_ID, List.of(effect), "Test may ability");
        gd.pendingMayAbilities.add(ability);
        gd.interaction.beginInteraction(
                new PendingInteraction.MayAbilityChoice(PLAYER1_ID, "Test may ability", null));

        svc.handleMayAbilityChosen(gd, player1, true);
    }

    private List<UUID> offeredTargets() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UUID>> captor = ArgumentCaptor.forClass(List.class);
        verify(playerInputService).beginPermanentChoice(eq(gd), eq(PLAYER1_ID), captor.capture(), anyString());
        return captor.getValue();
    }

    private static CardEffect specEffect(TargetPredicate declaredTarget) {
        return new CardEffect() {
            @Override
            public TargetSpec targetSpec() {
                return TargetSpec.benign(declaredTarget);
            }
        };
    }

    private static Permanent permanent(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return new Permanent(card);
    }
}
