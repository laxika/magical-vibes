package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfControllerPoisonedEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.StateTriggerService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CounterResolutionServiceTest {

    @Mock private GraveyardService graveyardService;
    @Mock private ExileService exileService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private GameQueryService gameQueryService;
    @Mock private StateTriggerService stateTriggerService;

    @InjectMocks private CounterResolutionService service;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
    }

    // ===== Helper methods =====

    private Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private Card createCreatureCard(String name) {
        Card card = createCard(name);
        card.setType(CardType.CREATURE);
        return card;
    }

    private Card createInstantCard(String name) {
        Card card = createCard(name);
        card.setType(CardType.INSTANT);
        return card;
    }

    private StackEntry creatureSpellEntry(Card card, UUID controllerId) {
        return new StackEntry(StackEntryType.CREATURE_SPELL, card, controllerId,
                card.getName(), List.of());
    }

    private StackEntry instantSpellEntry(Card card, UUID controllerId, UUID targetPermanentId) {
        return new StackEntry(StackEntryType.INSTANT_SPELL, card, controllerId,
                card.getName(), List.of(new CounterSpellEffect()), 0, targetPermanentId, null);
    }

    private StackEntry counterSpellEntry(Card card, UUID controllerId, UUID targetCardId) {
        return new StackEntry(StackEntryType.INSTANT_SPELL, card, controllerId,
                card.getName(), List.of(new CounterSpellEffect()), 0, targetCardId, null);
    }

    private StackEntry counterAndExileEntry(Card card, UUID controllerId, UUID targetCardId) {
        return new StackEntry(StackEntryType.INSTANT_SPELL, card, controllerId,
                card.getName(), List.of(new CounterSpellAndExileEffect()), 0, targetCardId, null);
    }

    private StackEntry counterUnlessPaysEntry(Card card, UUID controllerId, UUID targetCardId, int amount) {
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                card.getName(), List.of(new CounterUnlessPaysEffect(amount)), 0, targetCardId, null);
    }

    private StackEntry counterIfPoisonedEntry(Card card, UUID controllerId, UUID targetCardId) {
        return new StackEntry(StackEntryType.INSTANT_SPELL, card, controllerId,
                card.getName(), List.of(new CounterSpellIfControllerPoisonedEffect()), 0, targetCardId, null);
    }

    // =========================================================================
    // resolveCounterSpell (CounterSpellEffect)
    // =========================================================================

    @Nested
    @DisplayName("resolveCounterSpell")
    class ResolveCounterSpell {

        @Test
        @DisplayName("Counters a creature spell and puts it in the graveyard")
        void countersCreatureSpellAndPutsInGraveyard() {
            Card bears = createCreatureCard("Grizzly Bears");
            StackEntry bearsEntry = creatureSpellEntry(bears, player1Id);
            gd.stack.add(bearsEntry);

            Card cancel = createInstantCard("Cancel");
            StackEntry cancelEntry = counterSpellEntry(cancel, player2Id, bears.getId());

            service.resolveCounterSpell(gd, cancelEntry);

            assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Grizzly Bears"));
            verify(graveyardService).addCardToGraveyard(gd, player1Id, bears);
            verify(stateTriggerService).cleanupResolvedStateTrigger(gd, bearsEntry);
        }

        @Test
        @DisplayName("Counters an instant spell and puts it in the graveyard")
        void countersInstantSpellAndPutsInGraveyard() {
            Card might = createInstantCard("Might of Oaks");
            StackEntry mightEntry = new StackEntry(StackEntryType.INSTANT_SPELL, might, player1Id,
                    might.getName(), List.of());
            gd.stack.add(mightEntry);

            Card cancel = createInstantCard("Cancel");
            StackEntry cancelEntry = counterSpellEntry(cancel, player2Id, might.getId());

            service.resolveCounterSpell(gd, cancelEntry);

            assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Might of Oaks"));
            verify(graveyardService).addCardToGraveyard(gd, player1Id, might);
        }

        @Test
        @DisplayName("Does nothing when target spell is no longer on the stack")
        void doesNothingWhenTargetNoLongerOnStack() {
            UUID removedCardId = UUID.randomUUID();
            Card cancel = createInstantCard("Cancel");
            StackEntry cancelEntry = counterSpellEntry(cancel, player2Id, removedCardId);

            service.resolveCounterSpell(gd, cancelEntry);

            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
            verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
        }

        @Test
        @DisplayName("Does not counter an uncounterable spell")
        void doesNotCounterUncounterableSpell() {
            Card bears = createCreatureCard("Grizzly Bears");
            StackEntry bearsEntry = creatureSpellEntry(bears, player1Id);
            gd.stack.add(bearsEntry);

            Card cancel = createInstantCard("Cancel");
            StackEntry cancelEntry = counterSpellEntry(cancel, player2Id, bears.getId());

            when(gameQueryService.isUncounterable(gd, bears)).thenReturn(true);

            service.resolveCounterSpell(gd, cancelEntry);

            assertThat(gd.stack).contains(bearsEntry);
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("Countered spell is logged via broadcast")
        void counteredSpellIsLogged() {
            Card bears = createCreatureCard("Grizzly Bears");
            StackEntry bearsEntry = creatureSpellEntry(bears, player1Id);
            gd.stack.add(bearsEntry);

            Card cancel = createInstantCard("Cancel");
            StackEntry cancelEntry = counterSpellEntry(cancel, player2Id, bears.getId());

            service.resolveCounterSpell(gd, cancelEntry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Grizzly Bears is countered."));
        }

        @Test
        @DisplayName("Countered copy ceases to exist and does not go to the graveyard")
        void counteredCopyDoesNotGoToGraveyard() {
            Card bears = createCreatureCard("Grizzly Bears");
            StackEntry bearsEntry = creatureSpellEntry(bears, player1Id);
            bearsEntry.setCopy(true);
            gd.stack.add(bearsEntry);

            Card cancel = createInstantCard("Cancel");
            StackEntry cancelEntry = counterSpellEntry(cancel, player2Id, bears.getId());

            service.resolveCounterSpell(gd, cancelEntry);

            assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Grizzly Bears"));
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Grizzly Bears is countered."));
        }

        @Test
        @DisplayName("Does nothing when targetPermanentId is null")
        void doesNothingWhenTargetIdIsNull() {
            Card cancel = createInstantCard("Cancel");
            StackEntry cancelEntry = counterSpellEntry(cancel, player2Id, null);

            service.resolveCounterSpell(gd, cancelEntry);

            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
            verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
        }

        @Test
        @DisplayName("Does not counter a spell protected from counter by spell color")
        void doesNotCounterProtectedBySpellColor() {
            Card bears = createCreatureCard("Grizzly Bears");
            StackEntry bearsEntry = creatureSpellEntry(bears, player1Id);
            gd.stack.add(bearsEntry);

            Card cancel = createInstantCard("Cancel");
            cancel.setColor(CardColor.BLUE);
            StackEntry cancelEntry = counterSpellEntry(cancel, player2Id, bears.getId());

            when(gameQueryService.isProtectedFromCounterBySpellColor(eq(gd), eq(player1Id), eq(cancelEntry)))
                    .thenReturn(true);

            service.resolveCounterSpell(gd, cancelEntry);

            assertThat(gd.stack).contains(bearsEntry);
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }
    }

    // =========================================================================
    // resolveCounterSpellAndExile (CounterSpellAndExileEffect)
    // =========================================================================

    @Nested
    @DisplayName("resolveCounterSpellAndExile")
    class ResolveCounterSpellAndExile {

        @Test
        @DisplayName("Counters a spell and exiles it instead of graveyard")
        void countersAndExiles() {
            Card bears = createCreatureCard("Grizzly Bears");
            StackEntry bearsEntry = creatureSpellEntry(bears, player1Id);
            gd.stack.add(bearsEntry);

            Card dissipate = createInstantCard("Dissipate");
            StackEntry dissipateEntry = counterAndExileEntry(dissipate, player2Id, bears.getId());

            service.resolveCounterSpellAndExile(gd, dissipateEntry);

            assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Grizzly Bears"));
            verify(exileService).exileCard(gd, player1Id, bears);
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Grizzly Bears is countered and exiled."));
        }

        @Test
        @DisplayName("Countered copy ceases to exist and is not exiled")
        void counteredCopyNotExiled() {
            Card bears = createCreatureCard("Grizzly Bears");
            StackEntry bearsEntry = creatureSpellEntry(bears, player1Id);
            bearsEntry.setCopy(true);
            gd.stack.add(bearsEntry);

            Card dissipate = createInstantCard("Dissipate");
            StackEntry dissipateEntry = counterAndExileEntry(dissipate, player2Id, bears.getId());

            service.resolveCounterSpellAndExile(gd, dissipateEntry);

            assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Grizzly Bears"));
            verify(exileService, never()).exileCard(any(), any(), any());
        }

        @Test
        @DisplayName("Does nothing when target spell is no longer on the stack")
        void doesNothingWhenTargetGone() {
            UUID removedCardId = UUID.randomUUID();
            Card dissipate = createInstantCard("Dissipate");
            StackEntry dissipateEntry = counterAndExileEntry(dissipate, player2Id, removedCardId);

            service.resolveCounterSpellAndExile(gd, dissipateEntry);

            verify(exileService, never()).exileCard(any(), any(), any());
            verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
        }

        @Test
        @DisplayName("Does not counter an uncounterable spell")
        void doesNotCounterUncounterable() {
            Card bears = createCreatureCard("Grizzly Bears");
            StackEntry bearsEntry = creatureSpellEntry(bears, player1Id);
            gd.stack.add(bearsEntry);

            Card dissipate = createInstantCard("Dissipate");
            StackEntry dissipateEntry = counterAndExileEntry(dissipate, player2Id, bears.getId());

            when(gameQueryService.isUncounterable(gd, bears)).thenReturn(true);

            service.resolveCounterSpellAndExile(gd, dissipateEntry);

            assertThat(gd.stack).contains(bearsEntry);
            verify(exileService, never()).exileCard(any(), any(), any());
        }

        @Test
        @DisplayName("Does nothing when targetPermanentId is null")
        void doesNothingWhenTargetIdIsNull() {
            Card dissipate = createInstantCard("Dissipate");
            StackEntry dissipateEntry = counterAndExileEntry(dissipate, player2Id, null);

            service.resolveCounterSpellAndExile(gd, dissipateEntry);

            verify(exileService, never()).exileCard(any(), any(), any());
        }
    }

    // =========================================================================
    // resolveCounterSpellIfControllerPoisoned (CounterSpellIfControllerPoisonedEffect)
    // =========================================================================

    @Nested
    @DisplayName("resolveCounterSpellIfControllerPoisoned")
    class ResolveCounterSpellIfControllerPoisoned {

        @Test
        @DisplayName("Counters spell when target controller is poisoned")
        void countersWhenPoisoned() {
            Card bears = createCreatureCard("Grizzly Bears");
            StackEntry bearsEntry = creatureSpellEntry(bears, player1Id);
            gd.stack.add(bearsEntry);
            gd.playerPoisonCounters.put(player1Id, 3);

            Card corruptedResolve = createInstantCard("Corrupted Resolve");
            StackEntry counterEntry = counterIfPoisonedEntry(corruptedResolve, player2Id, bears.getId());

            service.resolveCounterSpellIfControllerPoisoned(gd, counterEntry);

            assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Grizzly Bears"));
            verify(graveyardService).addCardToGraveyard(gd, player1Id, bears);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Grizzly Bears is countered."));
        }

        @Test
        @DisplayName("Does not counter spell when target controller is not poisoned")
        void doesNotCounterWhenNotPoisoned() {
            Card bears = createCreatureCard("Grizzly Bears");
            StackEntry bearsEntry = creatureSpellEntry(bears, player1Id);
            gd.stack.add(bearsEntry);

            Card corruptedResolve = createInstantCard("Corrupted Resolve");
            StackEntry counterEntry = counterIfPoisonedEntry(corruptedResolve, player2Id, bears.getId());

            service.resolveCounterSpellIfControllerPoisoned(gd, counterEntry);

            assertThat(gd.stack).contains(bearsEntry);
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("Does nothing when target spell is no longer on the stack")
        void doesNothingWhenTargetGone() {
            UUID removedCardId = UUID.randomUUID();
            Card corruptedResolve = createInstantCard("Corrupted Resolve");
            StackEntry counterEntry = counterIfPoisonedEntry(corruptedResolve, player2Id, removedCardId);

            service.resolveCounterSpellIfControllerPoisoned(gd, counterEntry);

            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("Does not counter uncounterable spell even when poisoned")
        void doesNotCounterUncounterableWhenPoisoned() {
            Card bears = createCreatureCard("Grizzly Bears");
            StackEntry bearsEntry = creatureSpellEntry(bears, player1Id);
            gd.stack.add(bearsEntry);
            gd.playerPoisonCounters.put(player1Id, 5);

            Card corruptedResolve = createInstantCard("Corrupted Resolve");
            StackEntry counterEntry = counterIfPoisonedEntry(corruptedResolve, player2Id, bears.getId());

            when(gameQueryService.isUncounterable(gd, bears)).thenReturn(true);

            service.resolveCounterSpellIfControllerPoisoned(gd, counterEntry);

            assertThat(gd.stack).contains(bearsEntry);
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("Does nothing when targetPermanentId is null")
        void doesNothingWhenTargetIdIsNull() {
            Card corruptedResolve = createInstantCard("Corrupted Resolve");
            StackEntry counterEntry = counterIfPoisonedEntry(corruptedResolve, player2Id, null);

            service.resolveCounterSpellIfControllerPoisoned(gd, counterEntry);

            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }
    }

    // =========================================================================
    // resolveCounterUnlessPays (CounterUnlessPaysEffect)
    // =========================================================================

    @Nested
    @DisplayName("resolveCounterUnlessPays")
    class ResolveCounterUnlessPays {

        @Test
        @DisplayName("Counters spell immediately when opponent cannot pay")
        void countersImmediatelyWhenCannotPay() {
            Card elves = createCreatureCard("Llanowar Elves");
            StackEntry elvesEntry = creatureSpellEntry(elves, player1Id);
            gd.stack.add(elvesEntry);

            // Player1 has 0 mana — cannot pay {1}
            gd.playerManaPools.put(player1Id, new ManaPool());

            Card hatchling = createCard("Spiketail Hatchling");
            StackEntry counterEntry = counterUnlessPaysEntry(hatchling, player2Id, elves.getId(), 1);

            service.resolveCounterUnlessPays(gd, counterEntry, new CounterUnlessPaysEffect(1));

            assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Llanowar Elves"));
            verify(graveyardService).addCardToGraveyard(gd, player1Id, elves);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Llanowar Elves is countered."));
        }

        @Test
        @DisplayName("Presents may ability choice when opponent can pay")
        void presentsMayAbilityChoiceWhenCanPay() {
            Card elves = createCreatureCard("Llanowar Elves");
            StackEntry elvesEntry = creatureSpellEntry(elves, player1Id);
            gd.stack.add(elvesEntry);

            // Player1 has 1 mana — can pay {1}
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.GREEN, 1);
            gd.playerManaPools.put(player1Id, pool);

            Card hatchling = createCard("Spiketail Hatchling");
            StackEntry counterEntry = counterUnlessPaysEntry(hatchling, player2Id, elves.getId(), 1);

            service.resolveCounterUnlessPays(gd, counterEntry, new CounterUnlessPaysEffect(1));

            // Elves still on stack — not countered yet
            assertThat(gd.stack).contains(elvesEntry);
            assertThat(gd.pendingMayAbilities).hasSize(1);

            PendingMayAbility ability = gd.pendingMayAbilities.getFirst();
            assertThat(ability.controllerId()).isEqualTo(player1Id);
            assertThat(ability.targetCardId()).isEqualTo(elves.getId());
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("PendingMayAbility prompt contains cost and spell name")
        void mayAbilityPromptContainsCostAndSpellName() {
            Card elves = createCreatureCard("Llanowar Elves");
            StackEntry elvesEntry = creatureSpellEntry(elves, player1Id);
            gd.stack.add(elvesEntry);

            ManaPool pool = new ManaPool();
            pool.add(ManaColor.GREEN, 2);
            gd.playerManaPools.put(player1Id, pool);

            Card hatchling = createCard("Spiketail Hatchling");
            StackEntry counterEntry = counterUnlessPaysEntry(hatchling, player2Id, elves.getId(), 1);

            service.resolveCounterUnlessPays(gd, counterEntry, new CounterUnlessPaysEffect(1));

            PendingMayAbility ability = gd.pendingMayAbilities.getFirst();
            assertThat(ability.description()).contains("{1}");
            assertThat(ability.description()).contains("Llanowar Elves");
        }

        @Test
        @DisplayName("Does nothing when target spell is no longer on the stack")
        void doesNothingWhenTargetNoLongerOnStack() {
            UUID removedCardId = UUID.randomUUID();
            Card hatchling = createCard("Spiketail Hatchling");
            StackEntry counterEntry = counterUnlessPaysEntry(hatchling, player2Id, removedCardId, 1);

            service.resolveCounterUnlessPays(gd, counterEntry, new CounterUnlessPaysEffect(1));

            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("Does not counter an uncounterable spell")
        void doesNotCounterUncounterableSpell() {
            Card elves = createCreatureCard("Llanowar Elves");
            StackEntry elvesEntry = creatureSpellEntry(elves, player1Id);
            gd.stack.add(elvesEntry);

            gd.playerManaPools.put(player1Id, new ManaPool());

            Card hatchling = createCard("Spiketail Hatchling");
            StackEntry counterEntry = counterUnlessPaysEntry(hatchling, player2Id, elves.getId(), 1);

            when(gameQueryService.isUncounterable(gd, elves)).thenReturn(true);

            service.resolveCounterUnlessPays(gd, counterEntry, new CounterUnlessPaysEffect(1));

            assertThat(gd.stack).contains(elvesEntry);
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("Countered copy does not go to the graveyard")
        void counteredCopyDoesNotGoToGraveyard() {
            Card elves = createCreatureCard("Llanowar Elves");
            StackEntry elvesEntry = creatureSpellEntry(elves, player1Id);
            elvesEntry.setCopy(true);
            gd.stack.add(elvesEntry);

            // Cannot pay — immediate counter
            gd.playerManaPools.put(player1Id, new ManaPool());

            Card hatchling = createCard("Spiketail Hatchling");
            StackEntry counterEntry = counterUnlessPaysEntry(hatchling, player2Id, elves.getId(), 1);

            service.resolveCounterUnlessPays(gd, counterEntry, new CounterUnlessPaysEffect(1));

            assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Llanowar Elves"));
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
        }

        @Test
        @DisplayName("Does nothing when targetPermanentId is null")
        void doesNothingWhenTargetIdIsNull() {
            Card hatchling = createCard("Spiketail Hatchling");
            StackEntry counterEntry = counterUnlessPaysEntry(hatchling, player2Id, null, 1);

            service.resolveCounterUnlessPays(gd, counterEntry, new CounterUnlessPaysEffect(1));

            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("PendingMayAbility effects contain CounterUnlessPaysEffect")
        void mayAbilityEffectsContainCounterUnlessPaysEffect() {
            Card elves = createCreatureCard("Llanowar Elves");
            StackEntry elvesEntry = creatureSpellEntry(elves, player1Id);
            gd.stack.add(elvesEntry);

            ManaPool pool = new ManaPool();
            pool.add(ManaColor.GREEN, 1);
            gd.playerManaPools.put(player1Id, pool);

            Card hatchling = createCard("Spiketail Hatchling");
            StackEntry counterEntry = counterUnlessPaysEntry(hatchling, player2Id, elves.getId(), 1);

            service.resolveCounterUnlessPays(gd, counterEntry, new CounterUnlessPaysEffect(1));

            PendingMayAbility ability = gd.pendingMayAbilities.getFirst();
            assertThat(ability.effects())
                    .hasSize(1)
                    .first()
                    .isInstanceOf(CounterUnlessPaysEffect.class);
        }
    }
}
