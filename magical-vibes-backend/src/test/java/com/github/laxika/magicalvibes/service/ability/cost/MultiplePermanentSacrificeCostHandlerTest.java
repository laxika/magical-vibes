package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiplePermanentSacrificeCostHandlerTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private PermanentSacrificeAction sacrificeAction;

    private final PermanentPredicate filter = new PermanentIsArtifactPredicate();
    private final SacrificeMultiplePermanentsCost cost = new SacrificeMultiplePermanentsCost(3, filter);
    private MultiplePermanentSacrificeCostHandler handler;

    private GameData gameData;
    private Player player;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        handler = new MultiplePermanentSacrificeCostHandler(cost, gameQueryService, sacrificeAction);
        playerId = UUID.randomUUID();
        player = new Player(playerId, "TestPlayer");
        gameData = new GameData(UUID.randomUUID(), "test", playerId, "TestPlayer");
        gameData.playerBattlefields.put(playerId, new ArrayList<>());
    }

    @Test
    @DisplayName("costEffect returns the SacrificeMultiplePermanentsCost")
    void costEffectReturnsCost() {
        assertThat(handler.costEffect()).isSameAs(cost);
    }

    @Test
    @DisplayName("requiredCount returns count from cost record")
    void requiredCountReturnsCountFromCost() {
        assertThat(handler.requiredCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("getPromptMessage includes remaining count")
    void getPromptMessageIncludesRemaining() {
        assertThat(handler.getPromptMessage(2)).isEqualTo("Choose a permanent to sacrifice (2 remaining).");
    }

    // =========================================================================
    // validateCanPay
    // =========================================================================

    @Test
    @DisplayName("validateCanPay throws when not enough matching permanents")
    void validateCanPayThrowsWhenNotEnoughMatching() {
        Permanent artifact1 = createPermanent("Artifact A");
        Permanent artifact2 = createPermanent("Artifact B");
        gameData.playerBattlefields.get(playerId).addAll(List.of(artifact1, artifact2));

        when(gameQueryService.matchesPermanentPredicate(gameData, artifact1, filter)).thenReturn(true);
        when(gameQueryService.matchesPermanentPredicate(gameData, artifact2, filter)).thenReturn(true);

        // Only 2 matching but need 3
        assertThatThrownBy(() -> handler.validateCanPay(gameData, playerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("validateCanPay throws when battlefield is empty")
    void validateCanPayThrowsWhenBattlefieldEmpty() {
        assertThatThrownBy(() -> handler.validateCanPay(gameData, playerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("validateCanPay succeeds when exact count matches")
    void validateCanPaySucceedsWhenExactCount() {
        Permanent artifact1 = createPermanent("Artifact A");
        Permanent artifact2 = createPermanent("Artifact B");
        Permanent artifact3 = createPermanent("Artifact C");
        gameData.playerBattlefields.get(playerId).addAll(List.of(artifact1, artifact2, artifact3));

        when(gameQueryService.matchesPermanentPredicate(gameData, artifact1, filter)).thenReturn(true);
        when(gameQueryService.matchesPermanentPredicate(gameData, artifact2, filter)).thenReturn(true);
        when(gameQueryService.matchesPermanentPredicate(gameData, artifact3, filter)).thenReturn(true);

        handler.validateCanPay(gameData, playerId);
        // no exception = success
    }

    @Test
    @DisplayName("validateCanPay does not count non-matching permanents")
    void validateCanPayDoesNotCountNonMatching() {
        Permanent artifact = createPermanent("Artifact A");
        Permanent enchantment = createPermanent("Enchantment A");
        gameData.playerBattlefields.get(playerId).addAll(List.of(artifact, enchantment));

        when(gameQueryService.matchesPermanentPredicate(gameData, artifact, filter)).thenReturn(true);
        when(gameQueryService.matchesPermanentPredicate(gameData, enchantment, filter)).thenReturn(false);

        // Only 1 matching but need 3
        assertThatThrownBy(() -> handler.validateCanPay(gameData, playerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    // =========================================================================
    // getValidChoiceIds
    // =========================================================================

    @Test
    @DisplayName("getValidChoiceIds returns empty list when no matching permanents")
    void getValidChoiceIdsEmptyWhenNoMatching() {
        Permanent enchantment = createPermanent("Enchantment");
        gameData.playerBattlefields.get(playerId).add(enchantment);
        when(gameQueryService.matchesPermanentPredicate(gameData, enchantment, filter)).thenReturn(false);

        List<UUID> result = handler.getValidChoiceIds(gameData, playerId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getValidChoiceIds returns empty list when battlefield is null")
    void getValidChoiceIdsEmptyWhenBattlefieldNull() {
        UUID otherPlayerId = UUID.randomUUID();

        List<UUID> result = handler.getValidChoiceIds(gameData, otherPlayerId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getValidChoiceIds returns only matching permanent IDs")
    void getValidChoiceIdsFiltersToMatching() {
        Permanent artifact1 = createPermanent("Artifact A");
        Permanent enchantment = createPermanent("Enchantment");
        Permanent artifact2 = createPermanent("Artifact B");
        gameData.playerBattlefields.get(playerId).addAll(List.of(artifact1, enchantment, artifact2));

        when(gameQueryService.matchesPermanentPredicate(gameData, artifact1, filter)).thenReturn(true);
        when(gameQueryService.matchesPermanentPredicate(gameData, enchantment, filter)).thenReturn(false);
        when(gameQueryService.matchesPermanentPredicate(gameData, artifact2, filter)).thenReturn(true);

        List<UUID> result = handler.getValidChoiceIds(gameData, playerId);

        assertThat(result).containsExactly(artifact1.getId(), artifact2.getId());
    }

    // =========================================================================
    // validateAndPay
    // =========================================================================

    @Test
    @DisplayName("validateAndPay throws when chosen permanent does not match filter")
    void validateAndPayThrowsForNonMatching() {
        Permanent enchantment = createPermanent("Not Matching");
        when(gameQueryService.matchesPermanentPredicate(gameData, enchantment, filter)).thenReturn(false);

        assertThatThrownBy(() -> handler.validateAndPay(gameData, player, enchantment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice a matching permanent");

        verify(sacrificeAction, never()).sacrifice(any(), any(), any());
    }

    @Test
    @DisplayName("validateAndPay sacrifices the chosen matching permanent")
    void validateAndPaySacrificesMatching() {
        Permanent artifact = createPermanent("Sol Ring");
        when(gameQueryService.matchesPermanentPredicate(gameData, artifact, filter)).thenReturn(true);

        handler.validateAndPay(gameData, player, artifact);

        verify(sacrificeAction).sacrifice(gameData, player, artifact);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Permanent createPermanent(String name) {
        Card card = new Card();
        card.setName(name);
        card.setManaCost("{0}");
        return new Permanent(card);
    }
}
