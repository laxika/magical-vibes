package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

@ExtendWith(MockitoExtension.class)
class GraveyardTargetingServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PlayerInputService playerInputService;

    private GraveyardTargetingService service;
    private GameData gd;
    private UUID player1Id;

    @BeforeEach
    void setUp() {
        service = new GraveyardTargetingService(predicateEvaluationService, gameBroadcastService, playerInputService);

        player1Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player1Id, new ArrayList<>());
    }

    @Test
    @DisplayName("handleGraveyardExileETBTargeting pushes stack entry with no targets when all graveyards are empty")
    void handleGraveyardExileETBTargeting_pushesEmptyTargetEntryWhenGraveyardsEmpty() {
        Card card = new Card();
        card.setName("Agent of Treachery");
        ExileCardsFromGraveyardEffect exile = new ExileCardsFromGraveyardEffect(3, 0);
        List<CardEffect> allEffects = List.of(exile);

        service.handleGraveyardExileETBTargeting(gd, player1Id, card, allEffects, exile);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(card);
        verify(gameBroadcastService).logAndBroadcast(gd, "Agent of Treachery's enter-the-battlefield ability triggers.");
    }

    @Test
    @DisplayName("handleBeginningOfCombatGraveyardTargeting pushes stack entry when no matching graveyard cards")
    void handleBeginningOfCombatGraveyardTargeting_pushesStackEntryWhenGraveyardEmpty() {
        Card card = new Card();
        card.setName("Ravenous Chupacabra");
        UUID sourcePermanentId = UUID.randomUUID();
        ExileTargetCardFromGraveyardEffect exileEffect = new ExileTargetCardFromGraveyardEffect(CardType.CREATURE);
        List<CardEffect> effects = List.of(exileEffect);

        service.handleBeginningOfCombatGraveyardTargeting(gd, player1Id, card, effects, sourcePermanentId, exileEffect);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(card);
    }

    @Test
    @DisplayName("handleBeginningOfCombatGraveyardTargeting skips non-matching card types in graveyard")
    void handleBeginningOfCombatGraveyardTargeting_skipsNonMatchingTypesInGraveyard() {
        Card card = new Card();
        card.setName("Ravenous Chupacabra");
        UUID sourcePermanentId = UUID.randomUUID();
        ExileTargetCardFromGraveyardEffect exileEffect = new ExileTargetCardFromGraveyardEffect(CardType.CREATURE);

        Card landCard = new Card();
        landCard.setName("Forest");
        landCard.setType(CardType.LAND);
        gd.playerGraveyards.get(player1Id).add(landCard);

        service.handleBeginningOfCombatGraveyardTargeting(gd, player1Id, card, List.of(exileEffect), sourcePermanentId, exileEffect);

        // No creature in graveyard — falls through to empty-target stack entry path
        assertThat(gd.stack).hasSize(1);
    }
}
