package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;
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
class SubtypeSacrificeCostHandlerTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private PermanentSacrificeAction sacrificeAction;

    private final SacrificeSubtypeCreatureCost cost = new SacrificeSubtypeCreatureCost(CardSubtype.GOBLIN);
    private SubtypeSacrificeCostHandler handler;

    private GameData gameData;
    private Player player;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        handler = new SubtypeSacrificeCostHandler(cost, gameQueryService, sacrificeAction);
        playerId = UUID.randomUUID();
        player = new Player(playerId, "TestPlayer");
        gameData = new GameData(UUID.randomUUID(), "test", playerId, "TestPlayer");
        gameData.playerBattlefields.put(playerId, new ArrayList<>());
    }

    @Test
    @DisplayName("costEffect returns the SacrificeSubtypeCreatureCost")
    void costEffectReturnsCost() {
        assertThat(handler.costEffect()).isSameAs(cost);
    }

    @Test
    @DisplayName("requiredCount returns 1")
    void requiredCountReturnsOne() {
        assertThat(handler.requiredCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("getPromptMessage includes subtype name")
    void getPromptMessageIncludesSubtype() {
        assertThat(handler.getPromptMessage(1)).isEqualTo("Choose a Goblin to sacrifice.");
    }

    // =========================================================================
    // validateCanPay
    // =========================================================================

    @Test
    @DisplayName("validateCanPay throws when no matching subtype creatures exist")
    void validateCanPayThrowsWhenNoMatching() {
        Permanent human = createCreature("Human Soldier", List.of(CardSubtype.HUMAN));
        gameData.playerBattlefields.get(playerId).add(human);

        when(gameQueryService.isCreature(gameData, human)).thenReturn(true);

        assertThatThrownBy(() -> handler.validateCanPay(gameData, playerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Goblin");
    }

    @Test
    @DisplayName("validateCanPay throws when battlefield is empty")
    void validateCanPayThrowsWhenBattlefieldEmpty() {
        assertThatThrownBy(() -> handler.validateCanPay(gameData, playerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Goblin");
    }

    @Test
    @DisplayName("validateCanPay succeeds when a matching subtype creature exists")
    void validateCanPaySucceedsWhenMatchingExists() {
        Permanent goblin = createCreature("Goblin Recruit", List.of(CardSubtype.GOBLIN));
        gameData.playerBattlefields.get(playerId).add(goblin);

        when(gameQueryService.isCreature(gameData, goblin)).thenReturn(true);

        handler.validateCanPay(gameData, playerId);
        // no exception = success
    }

    @Test
    @DisplayName("validateCanPay does not count non-creatures with matching subtype")
    void validateCanPayDoesNotCountNonCreatures() {
        Permanent tribalEnchantment = createCreature("Goblin Enchantment", List.of(CardSubtype.GOBLIN));
        gameData.playerBattlefields.get(playerId).add(tribalEnchantment);

        when(gameQueryService.isCreature(gameData, tribalEnchantment)).thenReturn(false);

        assertThatThrownBy(() -> handler.validateCanPay(gameData, playerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Goblin");
    }

    // =========================================================================
    // getValidChoiceIds
    // =========================================================================

    @Test
    @DisplayName("getValidChoiceIds returns empty list when no matching creatures")
    void getValidChoiceIdsEmptyWhenNoMatching() {
        Permanent human = createCreature("Human Soldier", List.of(CardSubtype.HUMAN));
        gameData.playerBattlefields.get(playerId).add(human);

        when(gameQueryService.isCreature(gameData, human)).thenReturn(true);

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
    @DisplayName("getValidChoiceIds returns only matching subtype creature IDs")
    void getValidChoiceIdsFiltersToMatching() {
        Permanent goblinA = createCreature("Goblin A", List.of(CardSubtype.GOBLIN));
        Permanent human = createCreature("Human Soldier", List.of(CardSubtype.HUMAN));
        Permanent goblinB = createCreature("Goblin B", List.of(CardSubtype.GOBLIN));
        gameData.playerBattlefields.get(playerId).addAll(List.of(goblinA, human, goblinB));

        when(gameQueryService.isCreature(gameData, goblinA)).thenReturn(true);
        when(gameQueryService.isCreature(gameData, human)).thenReturn(true);
        when(gameQueryService.isCreature(gameData, goblinB)).thenReturn(true);

        List<UUID> result = handler.getValidChoiceIds(gameData, playerId);

        assertThat(result).containsExactly(goblinA.getId(), goblinB.getId());
    }

    @Test
    @DisplayName("getValidChoiceIds excludes non-creatures even with matching subtype")
    void getValidChoiceIdsExcludesNonCreatures() {
        Permanent goblinCreature = createCreature("Goblin Warrior", List.of(CardSubtype.GOBLIN));
        Permanent goblinNonCreature = createCreature("Goblin Banner", List.of(CardSubtype.GOBLIN));
        gameData.playerBattlefields.get(playerId).addAll(List.of(goblinCreature, goblinNonCreature));

        when(gameQueryService.isCreature(gameData, goblinCreature)).thenReturn(true);
        when(gameQueryService.isCreature(gameData, goblinNonCreature)).thenReturn(false);

        List<UUID> result = handler.getValidChoiceIds(gameData, playerId);

        assertThat(result).containsExactly(goblinCreature.getId());
    }

    // =========================================================================
    // validateAndPay
    // =========================================================================

    @Test
    @DisplayName("validateAndPay throws when chosen permanent is not a creature")
    void validateAndPayThrowsForNonCreature() {
        Permanent nonCreature = createCreature("Goblin Banner", List.of(CardSubtype.GOBLIN));
        when(gameQueryService.isCreature(gameData, nonCreature)).thenReturn(false);

        assertThatThrownBy(() -> handler.validateAndPay(gameData, player, nonCreature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice a creature");

        verify(sacrificeAction, never()).sacrifice(any(), any(), any());
    }

    @Test
    @DisplayName("validateAndPay throws when chosen creature has wrong subtype")
    void validateAndPayThrowsForWrongSubtype() {
        Permanent human = createCreature("Human Soldier", List.of(CardSubtype.HUMAN));
        when(gameQueryService.isCreature(gameData, human)).thenReturn(true);

        assertThatThrownBy(() -> handler.validateAndPay(gameData, player, human))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice a Goblin");

        verify(sacrificeAction, never()).sacrifice(any(), any(), any());
    }

    @Test
    @DisplayName("validateAndPay sacrifices the chosen matching creature")
    void validateAndPaySacrificesMatching() {
        Permanent goblin = createCreature("Goblin Recruit", List.of(CardSubtype.GOBLIN));
        when(gameQueryService.isCreature(gameData, goblin)).thenReturn(true);

        handler.validateAndPay(gameData, player, goblin);

        verify(sacrificeAction).sacrifice(gameData, player, goblin);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Permanent createCreature(String name, List<CardSubtype> subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setManaCost("{0}");
        card.setSubtypes(subtypes);
        return new Permanent(card);
    }
}
