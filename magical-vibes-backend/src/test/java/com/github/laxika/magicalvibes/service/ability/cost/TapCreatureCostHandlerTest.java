package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TapCreatureCostHandlerTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @Mock
    private TriggerCollectionService triggerCollectionService;

    private final TapCreatureCost cost = new TapCreatureCost(
            new PermanentColorInPredicate(Set.of(CardColor.BLUE)));
    private TapCreatureCostHandler handler;

    private GameData gameData;
    private Player player;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        handler = new TapCreatureCostHandler(cost, gameQueryService, gameBroadcastService,
                triggerCollectionService);
        playerId = UUID.randomUUID();
        player = new Player(playerId, "TestPlayer");
        gameData = new GameData(UUID.randomUUID(), "test", playerId, "TestPlayer");
        gameData.playerBattlefields.put(playerId, new ArrayList<>());
    }

    @Test
    @DisplayName("costEffect returns the TapCreatureCost")
    void costEffectReturnsCost() {
        assertThat(handler.costEffect()).isSameAs(cost);
    }

    @Test
    @DisplayName("requiredCount returns 1")
    void requiredCountReturnsOne() {
        assertThat(handler.requiredCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("getPromptMessage returns tap creature prompt")
    void getPromptMessageReturnsTapPrompt() {
        assertThat(handler.getPromptMessage(1)).isEqualTo("Choose an untapped creature to tap.");
    }

    // =========================================================================
    // validateCanPay
    // =========================================================================

    @Test
    @DisplayName("validateCanPay throws when no matching untapped creatures exist")
    void validateCanPayThrowsWhenNoMatching() {
        Permanent redCreature = createCreature("Red Warrior");
        gameData.playerBattlefields.get(playerId).add(redCreature);

        when(gameQueryService.isCreature(gameData, redCreature)).thenReturn(true);
        when(gameQueryService.matchesPermanentPredicate(gameData, redCreature, cost.predicate()))
                .thenReturn(false);

        assertThatThrownBy(() -> handler.validateCanPay(gameData, playerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("validateCanPay throws when battlefield is empty")
    void validateCanPayThrowsWhenBattlefieldEmpty() {
        assertThatThrownBy(() -> handler.validateCanPay(gameData, playerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("validateCanPay succeeds when a matching untapped creature exists")
    void validateCanPaySucceedsWhenMatchingExists() {
        Permanent blueCreature = createCreature("Blue Wizard");
        gameData.playerBattlefields.get(playerId).add(blueCreature);

        when(gameQueryService.isCreature(gameData, blueCreature)).thenReturn(true);
        when(gameQueryService.matchesPermanentPredicate(gameData, blueCreature, cost.predicate()))
                .thenReturn(true);

        handler.validateCanPay(gameData, playerId);
        // no exception = success
    }

    @Test
    @DisplayName("validateCanPay does not count non-creatures")
    void validateCanPayDoesNotCountNonCreatures() {
        Permanent nonCreature = createCreature("Blue Enchantment");
        gameData.playerBattlefields.get(playerId).add(nonCreature);

        when(gameQueryService.isCreature(gameData, nonCreature)).thenReturn(false);

        assertThatThrownBy(() -> handler.validateCanPay(gameData, playerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("validateCanPay does not count already tapped creatures")
    void validateCanPayDoesNotCountTappedCreatures() {
        Permanent tappedCreature = createCreature("Blue Wizard");
        tappedCreature.tap();
        gameData.playerBattlefields.get(playerId).add(tappedCreature);

        when(gameQueryService.isCreature(gameData, tappedCreature)).thenReturn(true);

        assertThatThrownBy(() -> handler.validateCanPay(gameData, playerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    // =========================================================================
    // getValidChoiceIds
    // =========================================================================

    @Test
    @DisplayName("getValidChoiceIds returns empty list when no matching creatures")
    void getValidChoiceIdsEmptyWhenNoMatching() {
        Permanent redCreature = createCreature("Red Warrior");
        gameData.playerBattlefields.get(playerId).add(redCreature);

        when(gameQueryService.isCreature(gameData, redCreature)).thenReturn(true);
        when(gameQueryService.matchesPermanentPredicate(gameData, redCreature, cost.predicate()))
                .thenReturn(false);

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
    @DisplayName("getValidChoiceIds returns only matching untapped creature IDs")
    void getValidChoiceIdsFiltersToMatching() {
        Permanent blueA = createCreature("Blue Wizard A");
        Permanent redCreature = createCreature("Red Warrior");
        Permanent blueB = createCreature("Blue Wizard B");
        gameData.playerBattlefields.get(playerId).addAll(List.of(blueA, redCreature, blueB));

        when(gameQueryService.isCreature(gameData, blueA)).thenReturn(true);
        when(gameQueryService.isCreature(gameData, redCreature)).thenReturn(true);
        when(gameQueryService.isCreature(gameData, blueB)).thenReturn(true);
        when(gameQueryService.matchesPermanentPredicate(gameData, blueA, cost.predicate()))
                .thenReturn(true);
        when(gameQueryService.matchesPermanentPredicate(gameData, redCreature, cost.predicate()))
                .thenReturn(false);
        when(gameQueryService.matchesPermanentPredicate(gameData, blueB, cost.predicate()))
                .thenReturn(true);

        List<UUID> result = handler.getValidChoiceIds(gameData, playerId);

        assertThat(result).containsExactly(blueA.getId(), blueB.getId());
    }

    @Test
    @DisplayName("getValidChoiceIds excludes tapped creatures")
    void getValidChoiceIdsExcludesTapped() {
        Permanent untapped = createCreature("Blue Wizard A");
        Permanent tapped = createCreature("Blue Wizard B");
        tapped.tap();
        gameData.playerBattlefields.get(playerId).addAll(List.of(untapped, tapped));

        when(gameQueryService.isCreature(gameData, untapped)).thenReturn(true);
        when(gameQueryService.isCreature(gameData, tapped)).thenReturn(true);
        when(gameQueryService.matchesPermanentPredicate(gameData, untapped, cost.predicate()))
                .thenReturn(true);

        List<UUID> result = handler.getValidChoiceIds(gameData, playerId);

        assertThat(result).containsExactly(untapped.getId());
    }

    @Test
    @DisplayName("getValidChoiceIds excludes non-creatures")
    void getValidChoiceIdsExcludesNonCreatures() {
        Permanent creature = createCreature("Blue Wizard");
        Permanent nonCreature = createCreature("Blue Enchantment");
        gameData.playerBattlefields.get(playerId).addAll(List.of(creature, nonCreature));

        when(gameQueryService.isCreature(gameData, creature)).thenReturn(true);
        when(gameQueryService.isCreature(gameData, nonCreature)).thenReturn(false);
        when(gameQueryService.matchesPermanentPredicate(gameData, creature, cost.predicate()))
                .thenReturn(true);

        List<UUID> result = handler.getValidChoiceIds(gameData, playerId);

        assertThat(result).containsExactly(creature.getId());
    }

    // =========================================================================
    // validateAndPay
    // =========================================================================

    @Test
    @DisplayName("validateAndPay throws when chosen permanent is not a creature")
    void validateAndPayThrowsForNonCreature() {
        Permanent nonCreature = createCreature("Blue Enchantment");
        when(gameQueryService.isCreature(gameData, nonCreature)).thenReturn(false);

        assertThatThrownBy(() -> handler.validateAndPay(gameData, player, nonCreature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must tap a creature");

        verify(triggerCollectionService, never())
                .checkEnchantedPermanentTapTriggers(any(), any());
    }

    @Test
    @DisplayName("validateAndPay throws when chosen creature is already tapped")
    void validateAndPayThrowsForAlreadyTapped() {
        Permanent tappedCreature = createCreature("Blue Wizard");
        tappedCreature.tap();
        when(gameQueryService.isCreature(gameData, tappedCreature)).thenReturn(true);

        assertThatThrownBy(() -> handler.validateAndPay(gameData, player, tappedCreature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creature is already tapped");

        verify(triggerCollectionService, never())
                .checkEnchantedPermanentTapTriggers(any(), any());
    }

    @Test
    @DisplayName("validateAndPay throws when chosen creature does not match predicate")
    void validateAndPayThrowsForPredicateMismatch() {
        Permanent redCreature = createCreature("Red Warrior");
        when(gameQueryService.isCreature(gameData, redCreature)).thenReturn(true);
        when(gameQueryService.matchesPermanentPredicate(gameData, redCreature, cost.predicate()))
                .thenReturn(false);

        assertThatThrownBy(() -> handler.validateAndPay(gameData, player, redCreature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creature does not match the required predicate");

        verify(triggerCollectionService, never())
                .checkEnchantedPermanentTapTriggers(any(), any());
    }

    @Test
    @DisplayName("validateAndPay taps the creature, fires triggers, and broadcasts")
    void validateAndPayTapsMatchingCreature() {
        Permanent blueCreature = createCreature("Blue Wizard");
        when(gameQueryService.isCreature(gameData, blueCreature)).thenReturn(true);
        when(gameQueryService.matchesPermanentPredicate(gameData, blueCreature, cost.predicate()))
                .thenReturn(true);

        handler.validateAndPay(gameData, player, blueCreature);

        assertThat(blueCreature.isTapped()).isTrue();
        verify(triggerCollectionService).checkEnchantedPermanentTapTriggers(gameData, blueCreature);
        verify(gameBroadcastService).logAndBroadcast(eq(gameData),
                eq("TestPlayer taps Blue Wizard as a cost."));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Permanent createCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setManaCost("{0}");
        return new Permanent(card);
    }
}
