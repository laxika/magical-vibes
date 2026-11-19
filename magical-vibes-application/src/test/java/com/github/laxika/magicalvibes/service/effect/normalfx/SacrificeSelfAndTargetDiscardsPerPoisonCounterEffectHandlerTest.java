package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SacrificeSelfAndTargetDiscardsPerPoisonCounterEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Sacrifices source and target discards per poison counter")
    void sacrificesAndDiscardsPerPoison() {
        Card card = createCard("Flesh-Eater Imp");
        Permanent source = new Permanent(card);
        gd.playerBattlefields.get(player1Id).add(source);
        SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect effect = new SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect();
        StackEntry entry = triggeredEntry(card, effect, source.getId());
        gd.playerPoisonCounters.put(player2Id, 3);
        gd.playerHands.get(player2Id).addAll(List.of(createCard("A"), createCard("B"), createCard("C")));
        stubSourceControlledBy(source, player1Id);
        sacrificeSucceeds();

        resolveEffect(gd, entry, effect);

        verify(stepDepartureSupport).sacrificeThenContinue(eq(gd), eq(entry), eq(effect), eq(source.getId()), any());
        verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), eq(3), any(DiscardFollowUp.class));
    }

    @Test
    @DisplayName("No discard when target has no poison counters")
    void noDiscardWhenNoPoisonCounters() {
        Card card = createCard("Flesh-Eater Imp");
        Permanent source = new Permanent(card);
        gd.playerBattlefields.get(player1Id).add(source);
        SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect effect = new SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect();
        StackEntry entry = triggeredEntry(card, effect, source.getId());
        stubSourceControlledBy(source, player1Id);
        sacrificeSucceeds();

        resolveEffect(gd, entry, effect);

        verify(stepDepartureSupport).sacrificeThenContinue(eq(gd), eq(entry), eq(effect), eq(source.getId()), any());
        verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt(), any(DiscardFollowUp.class));
        verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("no poison counters")));
    }

    @Test
    @DisplayName("Fizzles when source not found on battlefield")
    void fizzlesWhenSourceNotFound() {
        Card card = createCard("Flesh-Eater Imp");
        UUID fakePermanentId = UUID.randomUUID();
        SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect effect = new SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect();
        StackEntry entry = triggeredEntry(card, effect, fakePermanentId);

        when(gameQueryService.findPermanentById(gd, fakePermanentId)).thenReturn(null);

        resolveEffect(gd, entry, effect);

        verify(stepDepartureSupport, never()).sacrificeThenContinue(any(), any(), any(), any(), any());
        verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("fizzles")));
    }

    @Test
    @DisplayName("Does nothing when target or source permanent is null")
    void doesNothingWhenNull() {
        Card card = createCard("Flesh-Eater Imp");
        SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect effect = new SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect();
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);

        verify(stepDepartureSupport, never()).sacrificeThenContinue(any(), any(), any(), any(), any());
    }

    private StackEntry triggeredEntry(Card card, CardEffect effect, UUID sourcePermanentId) {
        StackEntry entry = createTriggeredEntry(card, player1Id, List.of(effect), sourcePermanentId);
        entry.setTriggeringPlayerId(player2Id);
        return entry;
    }

    private void stubSourceControlledBy(Permanent source, UUID controllerId) {
        when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);
        when(gameQueryService.findPermanentController(gd, source.getId())).thenReturn(controllerId);
    }

    /** The sacrifice goes through, as the departure pipeline would record it, before the continuation runs. */
    private void sacrificeSucceeds() {
        doAnswer(inv -> {
            GameData gameData = inv.getArgument(0);
            StackEntry entry = inv.getArgument(1);
            UUID permanentId = inv.getArgument(3);
            entry.recordDepartures(List.of(new DepartureResult(permanentId, Zone.GRAVEYARD, player1Id,
                    UUID.randomUUID(), 1, 1, false, true, player1Id)));
            inv.<ContinuesAfterDeparture>getArgument(4).continueAfterDeparture(gameData, entry, inv.getArgument(2));
            return null;
        }).when(stepDepartureSupport).sacrificeThenContinue(any(), any(), any(), any(), any());
    }
}
