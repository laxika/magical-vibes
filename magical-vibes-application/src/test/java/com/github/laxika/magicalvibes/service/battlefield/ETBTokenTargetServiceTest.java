package com.github.laxika.magicalvibes.service.battlefield;
import com.github.laxika.magicalvibes.model.GameLog;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ETBTokenTargetServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PlayerInputService playerInputService;
    @Mock private TargetLegalityService targetLegalityService;

    private ETBTokenTargetService service;
    private GameData gd;
    private UUID player1Id;

    @BeforeEach
    void setUp() {
        service = new ETBTokenTargetService(gameQueryService, new PredicateEvaluationService(gameQueryService),
                gameBroadcastService, playerInputService, targetLegalityService);

        player1Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
    }

    @Test
    @DisplayName("hasGroupWithMaxTargetsGreaterThanOne returns false for card with no spell target groups")
    void hasGroupWithMaxTargetsGreaterThanOne_returnsFalseWhenNoGroups() {
        Card card = new Card();
        card.setName("Test Card");

        assertThat(service.hasGroupWithMaxTargetsGreaterThanOne(card)).isFalse();
    }

    @Test
    @DisplayName("hasGroupWithMaxTargetsGreaterThanOne returns false when all groups have maxTargets of 1")
    void hasGroupWithMaxTargetsGreaterThanOne_returnsFalseWhenSingleTargetGroups() {
        Card card = new Card();
        card.setName("Test Card");
        card.target(null, 1, 1);

        assertThat(service.hasGroupWithMaxTargetsGreaterThanOne(card)).isFalse();
    }

    @Test
    @DisplayName("hasGroupWithMaxTargetsGreaterThanOne returns true when a group has maxTargets > 1")
    void hasGroupWithMaxTargetsGreaterThanOne_returnsTrueWhenGroupHasHighMaxTargets() {
        Card card = new Card();
        card.setName("Test Card");
        card.target(null, 1, 3);

        assertThat(service.hasGroupWithMaxTargetsGreaterThanOne(card)).isTrue();
    }

    @Test
    @DisplayName("processNextETBSpellTargetTrigger does nothing when queue is empty")
    void processNextETBSpellTargetTrigger_doesNothingWhenQueueEmpty() {
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class)).isFalse();

        service.processNextETBSpellTargetTrigger(gd);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("processNextETBSpellTargetTrigger removes trigger and logs when stack has no valid spell targets")
    void processNextETBSpellTargetTrigger_removesAndLogsWhenNoSpellsOnStack() {
        Card sourceCard = new Card();
        sourceCard.setName("Snapcaster Mage");
        var trigger = new PermanentChoiceContext.ETBSpellTargetTrigger(
                sourceCard, player1Id, List.of(new GainLifeEffect(1)), null);
        gd.queueInteraction(trigger);

        service.processNextETBSpellTargetTrigger(gd);

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class)).isFalse();
        assertThat(gd.stack).isEmpty();
        verify(gameBroadcastService).logAndBroadcast(gd, GameLog.text("Snapcaster Mage's enter-the-battlefield ability has no valid spell targets."));
    }

    @Test
    @DisplayName("processNextETBTokenTargetTrigger does nothing when queue is empty")
    void processNextETBTokenTargetTrigger_doesNothingWhenQueueEmpty() {
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)).isFalse();

        service.processNextETBTokenTargetTrigger(gd);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multi-target trigger skips a target group whose bound effect was gated out")
    void processNextETBTokenMultiTargetTrigger_skipsGatedOutGroup() {
        // Two target groups (like Noggle Hedge-Mage). The first group's effect was gated out
        // (its intervening-if wasn't met as the permanent entered), so only the second group's
        // effect survives in pending.effects(). The first group must be skipped so the second
        // group's target is still chosen — not treated as a mandatory group with no targets.
        Card card = new Card();
        card.setName("Noggle Hedge-Mage");
        var group0Effect = new DealDamageToTargetPlayerOrPlaneswalkerEffect(1);
        card.target(null, 1, 1).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, group0Effect);
        var group1Effect = new DealDamageToTargetPlayerOrPlaneswalkerEffect(2);
        card.target(null, 1, 1).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, group1Effect);

        gd.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                card, player1Id, List.of(group1Effect), UUID.randomUUID(), List.of(), 0, 0));

        service.processNextETBTokenMultiTargetTrigger(gd);

        // Group 0 (gated out) is skipped; the controller is prompted for group 1's target instead.
        verify(playerInputService).beginAnyTargetChoice(
                eq(gd), eq(player1Id), anyList(), eq(List.of(player1Id)), contains("target 2"));
    }
}
