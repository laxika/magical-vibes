package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PutCardToBattlefieldEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    void retainsSourceSnapshotForTheHandFilterAfterSourceLeaves() {
        Card source = createCard("Subtype source");
        var snapshot = new com.github.laxika.magicalvibes.model.Permanent(source);
        CardPredicate predicate = new CardNamedPredicate("Test Filter");
        var effect = new PutCardToBattlefieldEffect(predicate, "creature");
        StackEntry entry = new StackEntry(com.github.laxika.magicalvibes.model.StackEntryType.ACTIVATED_ABILITY,
                source, player1Id, "Source ability", List.of(effect), null, snapshot.getId());
        entry.setSourcePermanentSnapshot(snapshot);
        Card creature = createCard("Chosen creature");
        gd.playerHands.get(player1Id).add(creature);
        when(predicateEvaluationService.matchesCardPredicate(eq(creature), eq(predicate), any(), eq(gd),
                eq(player1Id), eq(snapshot.getId()), isNull(), anyInt(), eq(snapshot))).thenReturn(true);

        resolveEffect(gd, entry, effect);

        verify(playerInputService).beginCardChoice(eq(gd), eq(player1Id), eq(List.of(0)), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), any(), anyBoolean(), eq(false), isNull(), isNull(),
                eq(false), eq(false), eq(0), eq(0), anySet(), isNull(), eq(false), eq(false),
                isNull(), isNull(), isNull(), isNull(), eq(snapshot.getId()), eq(Set.of()));
    }

    @Test
    @DisplayName("Presents card choice when matching cards exist in hand")
    void presentsChoiceWhenMatchingCards() {
        Card card = createCard("Elvish Piper");
        CardPredicate predicate = new CardNamedPredicate("Test Filter");
        PutCardToBattlefieldEffect effect = new PutCardToBattlefieldEffect(predicate, "creature");
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        Card creatureCard = createCard("Grizzly Bears");
        gd.playerHands.get(player1Id).add(creatureCard);

        when(predicateEvaluationService.matchesCardPredicate(eq(creatureCard), eq(predicate), any(), eq(gd), eq(player1Id)))
                .thenReturn(true);

        resolveEffect(gd, entry, effect);

        verify(playerInputService).beginCardChoice(eq(gd), eq(player1Id), any(), any(), anyBoolean(), anyBoolean(),
                anyBoolean(), any(), anyBoolean(), eq(false), isNull(), isNull(), eq(false), eq(false), eq(0), eq(0),
                anySet(), isNull(), eq(false), eq(false), isNull(), isNull(), isNull(), isNull(), isNull(), eq(Set.of()));
    }

    @Test
    @DisplayName("Passes haste and end-step-sacrifice flags through to the card choice")
    void passesHasteAndSacrificeFlags() {
        Card card = createCard("Incandescent Soulstoke");
        CardPredicate predicate = new CardNamedPredicate("Test Filter");
        PutCardToBattlefieldEffect effect =
                new PutCardToBattlefieldEffect(predicate, "Elemental creature", false, false, true, true);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        Card creatureCard = createCard("Air Elemental");
        gd.playerHands.get(player1Id).add(creatureCard);

        when(predicateEvaluationService.matchesCardPredicate(eq(creatureCard), eq(predicate), any(), eq(gd), eq(player1Id)))
                .thenReturn(true);

        resolveEffect(gd, entry, effect);

        verify(playerInputService).beginCardChoice(eq(gd), eq(player1Id), any(), any(), eq(false), eq(true), eq(true),
                isNull(), eq(false), eq(false), isNull(), isNull(), eq(false), eq(false), eq(0), eq(0), anySet(),
                isNull(), eq(false), eq(false), isNull(), isNull(), isNull(), isNull(), isNull(), eq(Set.of()));
    }

    @Test
    @DisplayName("Passes the end-step-return flag through to the card choice")
    void passesReturnToHandAtEndStepFlag() {
        Card card = createCard("Surprise Deployment");
        CardPredicate predicate = new CardNamedPredicate("Test Filter");
        PutCardToBattlefieldEffect effect = new PutCardToBattlefieldEffect(predicate, "nonwhite creature")
                .returningToHandAtEndStep();
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        Card creatureCard = createCard("Grizzly Bears");
        gd.playerHands.get(player1Id).add(creatureCard);

        when(predicateEvaluationService.matchesCardPredicate(eq(creatureCard), eq(predicate), any(), eq(gd), eq(player1Id)))
                .thenReturn(true);

        resolveEffect(gd, entry, effect);

        verify(playerInputService).beginCardChoice(eq(gd), eq(player1Id), any(), any(), anyBoolean(), anyBoolean(),
                anyBoolean(), any(), anyBoolean(), eq(false), isNull(), isNull(), eq(false), eq(false), eq(0), eq(0),
                anySet(), isNull(), eq(true), eq(false), isNull(), isNull(), isNull(), isNull(), isNull(), eq(Set.of()));
    }

    @Test
    @DisplayName("Filters the hand choice by the triggering event value")
    void filtersByEventValue() {
        Card card = createCard("Covert Technician");
        CardPredicate predicate = new CardNamedPredicate("artifact");
        PutCardToBattlefieldEffect effect = new PutCardToBattlefieldEffect(predicate, "artifact")
                .boundedByEventValue();
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        entry.setEventValue(2);
        Card eligibleCard = createCard("Mind Stone");
        eligibleCard.setManaCost("{2}");
        Card ineligibleCard = createCard("Solemn Simulacrum");
        ineligibleCard.setManaCost("{4}");
        gd.playerHands.get(player1Id).addAll(List.of(eligibleCard, ineligibleCard));

        when(predicateEvaluationService.matchesCardPredicate(any(Card.class), eq(predicate), any(), eq(gd), eq(player1Id)))
                .thenReturn(true);

        resolveEffect(gd, entry, effect);

        ArgumentCaptor<List<Integer>> validIndices = ArgumentCaptor.forClass(List.class);
        verify(playerInputService).beginCardChoice(eq(gd), eq(player1Id), validIndices.capture(), any(), anyBoolean(),
                anyBoolean(), anyBoolean(), any(), anyBoolean(), eq(false), isNull(), isNull(), eq(false), eq(false),
                eq(0), eq(0), anySet(), isNull(), eq(false), eq(false), isNull(), isNull(), isNull(), isNull(),
                isNull(), anySet());
        assertThat(validIndices.getValue()).containsExactly(0);
    }

    @Test
    @DisplayName("Passes the conditional tapped-and-attacking predicate through to the card choice")
    void passesConditionalTappedAndAttackingPredicate() {
        Card card = createCard("Summoner's Grimoire");
        CardPredicate predicate = new CardNamedPredicate("Test Filter");
        CardPredicate enterTappedAndAttackingIf = new CardNamedPredicate("Enchantment");
        PutCardToBattlefieldEffect effect = new PutCardToBattlefieldEffect(predicate, "creature")
                .withEnterTappedAndAttackingIf(enterTappedAndAttackingIf);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        Card creatureCard = createCard("Grizzly Bears");
        gd.playerHands.get(player1Id).add(creatureCard);

        when(predicateEvaluationService.matchesCardPredicate(eq(creatureCard), eq(predicate), any(), eq(gd), eq(player1Id)))
                .thenReturn(true);

        resolveEffect(gd, entry, effect);

        verify(playerInputService).beginCardChoice(eq(gd), eq(player1Id), any(), any(), anyBoolean(), anyBoolean(),
                anyBoolean(), any(), anyBoolean(), eq(false), isNull(), isNull(), eq(false), eq(false), eq(0), eq(0),
                anySet(), isNull(), eq(false), eq(false), isNull(), isNull(),
                eq(enterTappedAndAttackingIf), isNull(), isNull(), eq(Set.of()));
    }

    @Test
    @DisplayName("Passes drawAndRepeat flag and predicate through to the card choice")
    void passesDrawAndRepeatFlags() {
        Card card = createCard("Cultivator Colossus");
        CardPredicate predicate = new CardNamedPredicate("Test Filter");
        PutCardToBattlefieldEffect effect = PutCardToBattlefieldEffect.tappedDrawAndRepeat(predicate, "land");
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        Card landCard = createCard("Forest");
        gd.playerHands.get(player1Id).add(landCard);

        when(predicateEvaluationService.matchesCardPredicate(eq(landCard), eq(predicate), any(), eq(gd), eq(player1Id)))
                .thenReturn(true);

        resolveEffect(gd, entry, effect);

        verify(playerInputService).beginCardChoice(eq(gd), eq(player1Id), any(), any(), eq(true), eq(false), eq(false),
                isNull(), eq(false), eq(true), eq(predicate), eq("land"), eq(false), eq(false), eq(0), eq(0), anySet(),
                isNull(), eq(false), eq(false), isNull(), isNull(), isNull(), isNull(), isNull(), eq(Set.of()));
    }

    @Test
    @DisplayName("Passes putAnyNumber flag and predicate through to the card choice")
    void passesPutAnyNumberFlags() {
        Card card = createCard("Wrenn and Seven");
        CardPredicate predicate = new CardNamedPredicate("Test Filter");
        PutCardToBattlefieldEffect effect = PutCardToBattlefieldEffect.tappedAnyNumber(predicate, "land");
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        Card landCard = createCard("Forest");
        gd.playerHands.get(player1Id).add(landCard);

        when(predicateEvaluationService.matchesCardPredicate(eq(landCard), eq(predicate), any(), eq(gd), eq(player1Id)))
                .thenReturn(true);

        resolveEffect(gd, entry, effect);

        verify(playerInputService).beginCardChoice(eq(gd), eq(player1Id), any(), any(), eq(true), eq(false), eq(false),
                isNull(), eq(false), eq(false), eq(predicate), eq("land"), eq(true), eq(false), eq(0), eq(0), anySet(),
                isNull(), eq(false), eq(false), isNull(), isNull(), isNull(), isNull(), isNull(), eq(Set.of()));
    }

    @Test
    @DisplayName("Logs and does nothing when no matching cards in hand")
    void noMatchingCards() {
        Card card = createCard("Elvish Piper");
        CardPredicate predicate = new CardNamedPredicate("Test Filter");
        PutCardToBattlefieldEffect effect = new PutCardToBattlefieldEffect(predicate, "creature");
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        Card nonMatchingCard = createCard("Mountain");
        gd.playerHands.get(player1Id).add(nonMatchingCard);

        when(predicateEvaluationService.matchesCardPredicate(eq(nonMatchingCard), eq(predicate), any(), eq(gd), eq(player1Id)))
                .thenReturn(false);

        resolveEffect(gd, entry, effect);

        verifyNoInteractions(playerInputService);
        verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("no creature cards in hand")));
    }

    @Test
    @DisplayName("Does nothing when hand is empty")
    void emptyHand() {
        Card card = createCard("Elvish Piper");
        CardPredicate predicate = new CardNamedPredicate("Test Filter");
        PutCardToBattlefieldEffect effect = new PutCardToBattlefieldEffect(predicate, "creature");
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);

        verifyNoInteractions(playerInputService);
        verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("no creature cards in hand")));
    }
}
