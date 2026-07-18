package com.github.laxika.magicalvibes.service.effect.normalfx;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfControllerPoisonedEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class CounterSpellEffectHandlerTest {

    @Mock private GraveyardService graveyardService;
    @Mock private ExileService exileService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private GameQueryService gameQueryService;
    @Mock private StateTriggerService stateTriggerService;
    @InjectMocks
    private CounterSupport counterSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private CounterSpellEffectHandler counterSpellHandler;

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
        counterSpellHandler = new CounterSpellEffectHandler(counterSupport);

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

        private StackEntry instantSpellEntry(Card card, UUID controllerId, UUID targetId) {
            return new StackEntry(StackEntryType.INSTANT_SPELL, card, controllerId,
                    card.getName(), List.of(new CounterSpellEffect()), 0, targetId, null);
        }

        private StackEntry counterSpellEntry(Card card, UUID controllerId, UUID targetCardId) {
            return new StackEntry(StackEntryType.INSTANT_SPELL, card, controllerId,
                    card.getName(), List.of(new CounterSpellEffect()), 0, targetCardId, null);
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

    @Test
            @DisplayName("Counters a creature spell and puts it in the graveyard")
            void countersCreatureSpellAndPutsInGraveyard() {
                Card bears = createCreatureCard("Grizzly Bears");
                StackEntry bearsEntry = creatureSpellEntry(bears, player1Id);
                gd.stack.add(bearsEntry);

                Card cancel = createInstantCard("Cancel");
                StackEntry cancelEntry = counterSpellEntry(cancel, player2Id, bears.getId());

                counterSpellHandler.resolve(gd, cancelEntry, new CounterSpellEffect());

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

                counterSpellHandler.resolve(gd, cancelEntry, new CounterSpellEffect());

                assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Might of Oaks"));
                verify(graveyardService).addCardToGraveyard(gd, player1Id, might);
            }

            @Test
            @DisplayName("Does nothing when target spell is no longer on the stack")
            void doesNothingWhenTargetNoLongerOnStack() {
                UUID removedCardId = UUID.randomUUID();
                Card cancel = createInstantCard("Cancel");
                StackEntry cancelEntry = counterSpellEntry(cancel, player2Id, removedCardId);

                counterSpellHandler.resolve(gd, cancelEntry, new CounterSpellEffect());

                verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
                verify(gameBroadcastService, never()).logAndBroadcast(any(), any(GameLogEntry.class));
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

                counterSpellHandler.resolve(gd, cancelEntry, new CounterSpellEffect());

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

                counterSpellHandler.resolve(gd, cancelEntry, new CounterSpellEffect());

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Grizzly Bears is countered.")));
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

                counterSpellHandler.resolve(gd, cancelEntry, new CounterSpellEffect());

                assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Grizzly Bears"));
                verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Grizzly Bears is countered.")));
            }

            @Test
            @DisplayName("Does nothing when targetId is null")
            void doesNothingWhenTargetIdIsNull() {
                Card cancel = createInstantCard("Cancel");
                StackEntry cancelEntry = counterSpellEntry(cancel, player2Id, null);

                counterSpellHandler.resolve(gd, cancelEntry, new CounterSpellEffect());

                verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
                verify(gameBroadcastService, never()).logAndBroadcast(any(), any(GameLogEntry.class));
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

                counterSpellHandler.resolve(gd, cancelEntry, new CounterSpellEffect());

                assertThat(gd.stack).contains(bearsEntry);
                verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
            }

            @Test
            @DisplayName("Counters an activated ability â€” removes from stack without graveyard")
            void countersActivatedAbilityWithoutGraveyard() {
                Card fumeSpitter = createCreatureCard("Fume Spitter");
                StackEntry abilityEntry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, fumeSpitter, player1Id,
                        "Fume Spitter's ability", List.of());
                gd.stack.add(abilityEntry);

                Card stormtamer = createCreatureCard("Siren Stormtamer");
                StackEntry counterEntry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, stormtamer, player2Id,
                        "Siren Stormtamer's ability", List.of(new CounterSpellEffect()), 0, fumeSpitter.getId(), null);

                counterSpellHandler.resolve(gd, counterEntry, new CounterSpellEffect());

                assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Fume Spitter"));
                verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Fume Spitter's ability is countered.")));
            }

            @Test
            @DisplayName("Counters a triggered ability â€” removes from stack without graveyard")
            void countersTriggeredAbilityWithoutGraveyard() {
                Card source = createCreatureCard("Some Creature");
                StackEntry abilityEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source, player1Id,
                        "Some Creature's ability", List.of());
                gd.stack.add(abilityEntry);

                Card counter = createInstantCard("Stifle");
                StackEntry counterEntry = counterSpellEntry(counter, player2Id, source.getId());

                counterSpellHandler.resolve(gd, counterEntry, new CounterSpellEffect());

                assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Some Creature"));
                verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Some Creature's ability is countered.")));
            }
}
