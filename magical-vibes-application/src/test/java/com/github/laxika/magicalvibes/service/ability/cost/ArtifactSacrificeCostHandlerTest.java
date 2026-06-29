package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtifactSacrificeCostHandlerTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private PermanentSacrificeAction sacrificeAction;

    private final SacrificeArtifactCost cost = new SacrificeArtifactCost();
    private ArtifactSacrificeCostHandler handler;

    private GameData gameData;
    private Player player;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        handler = new ArtifactSacrificeCostHandler(cost, gameQueryService, sacrificeAction);
        playerId = UUID.randomUUID();
        player = new Player(playerId, "TestPlayer");
        gameData = new GameData(UUID.randomUUID(), "test", playerId, "TestPlayer");
        gameData.playerBattlefields.put(playerId, new ArrayList<>());
    }

    @Test
    @DisplayName("costEffect returns the SacrificeArtifactCost")
    void costEffectReturnsCost() {
        assertThat(handler.costEffect()).isSameAs(cost);
    }

    @Test
    @DisplayName("requiredCount is 1")
    void requiredCountIsOne() {
        assertThat(handler.requiredCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("getPromptMessage returns artifact sacrifice prompt")
    void getPromptMessage() {
        assertThat(handler.getPromptMessage(1)).isEqualTo("Choose an artifact to sacrifice.");
    }

    // =========================================================================
    // validateCanPay
    // =========================================================================

    @Test
    @DisplayName("validateCanPay throws when no artifacts on battlefield")
    void validateCanPayThrowsWhenNoArtifacts() {
        Permanent enchantment = createPermanent("Some Enchantment");
        gameData.playerBattlefields.get(playerId).add(enchantment);
        when(gameQueryService.isArtifact(enchantment)).thenReturn(false);

        assertThatThrownBy(() -> handler.validateCanPay(gameData, playerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No artifact to sacrifice");
    }

    @Test
    @DisplayName("validateCanPay throws when battlefield is empty")
    void validateCanPayThrowsWhenBattlefieldEmpty() {
        assertThatThrownBy(() -> handler.validateCanPay(gameData, playerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No artifact to sacrifice");
    }

    @Test
    @DisplayName("validateCanPay succeeds when an artifact is present")
    void validateCanPaySucceedsWithArtifact() {
        Permanent artifact = createPermanent("Sol Ring");
        gameData.playerBattlefields.get(playerId).add(artifact);
        when(gameQueryService.isArtifact(artifact)).thenReturn(true);

        handler.validateCanPay(gameData, playerId);
        // no exception = success
    }

    // =========================================================================
    // getValidChoiceIds
    // =========================================================================

    @Test
    @DisplayName("getValidChoiceIds returns empty list when no artifacts")
    void getValidChoiceIdsEmptyWhenNoArtifacts() {
        Permanent enchantment = createPermanent("Enchantment");
        gameData.playerBattlefields.get(playerId).add(enchantment);
        when(gameQueryService.isArtifact(enchantment)).thenReturn(false);

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
    @DisplayName("getValidChoiceIds returns only artifact permanent IDs")
    void getValidChoiceIdsFiltersToArtifacts() {
        Permanent artifact1 = createPermanent("Artifact A");
        Permanent enchantment = createPermanent("Enchantment");
        Permanent artifact2 = createPermanent("Artifact B");
        gameData.playerBattlefields.get(playerId).addAll(List.of(artifact1, enchantment, artifact2));

        when(gameQueryService.isArtifact(artifact1)).thenReturn(true);
        when(gameQueryService.isArtifact(enchantment)).thenReturn(false);
        when(gameQueryService.isArtifact(artifact2)).thenReturn(true);

        List<UUID> result = handler.getValidChoiceIds(gameData, playerId);

        assertThat(result).containsExactly(artifact1.getId(), artifact2.getId());
    }

    // =========================================================================
    // validateAndPay
    // =========================================================================

    @Test
    @DisplayName("validateAndPay throws when chosen permanent is not an artifact")
    void validateAndPayThrowsForNonArtifact() {
        Permanent enchantment = createPermanent("Not An Artifact");
        when(gameQueryService.isArtifact(enchantment)).thenReturn(false);

        assertThatThrownBy(() -> handler.validateAndPay(gameData, player, enchantment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice an artifact");

        verify(sacrificeAction, never()).sacrifice(any(), any(), any());
    }

    @Test
    @DisplayName("validateAndPay sacrifices the chosen artifact")
    void validateAndPaySacrificesArtifact() {
        Permanent artifact = createPermanent("Sol Ring");
        when(gameQueryService.isArtifact(artifact)).thenReturn(true);

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
