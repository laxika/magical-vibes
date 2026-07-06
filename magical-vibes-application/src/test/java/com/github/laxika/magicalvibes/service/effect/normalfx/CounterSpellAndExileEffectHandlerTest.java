package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileEffect;
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
class CounterSpellAndExileEffectHandlerTest {

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
    private CounterSpellAndExileEffectHandler counterSpellAndExileHandler;

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
        counterSpellAndExileHandler = new CounterSpellAndExileEffectHandler(counterSupport);

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

    @Test
            @DisplayName("Counters a spell and exiles it instead of graveyard")
            void countersAndExiles() {
                Card bears = createCreatureCard("Grizzly Bears");
                StackEntry bearsEntry = creatureSpellEntry(bears, player1Id);
                gd.stack.add(bearsEntry);

                Card dissipate = createInstantCard("Dissipate");
                StackEntry dissipateEntry = counterAndExileEntry(dissipate, player2Id, bears.getId());

                counterSpellAndExileHandler.resolve(gd, dissipateEntry, new CounterSpellAndExileEffect());

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

                counterSpellAndExileHandler.resolve(gd, dissipateEntry, new CounterSpellAndExileEffect());

                assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Grizzly Bears"));
                verify(exileService, never()).exileCard(any(), any(), any());
            }

            @Test
            @DisplayName("Does nothing when target spell is no longer on the stack")
            void doesNothingWhenTargetGone() {
                UUID removedCardId = UUID.randomUUID();
                Card dissipate = createInstantCard("Dissipate");
                StackEntry dissipateEntry = counterAndExileEntry(dissipate, player2Id, removedCardId);

                counterSpellAndExileHandler.resolve(gd, dissipateEntry, new CounterSpellAndExileEffect());

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

                counterSpellAndExileHandler.resolve(gd, dissipateEntry, new CounterSpellAndExileEffect());

                assertThat(gd.stack).contains(bearsEntry);
                verify(exileService, never()).exileCard(any(), any(), any());
            }

            @Test
            @DisplayName("Does nothing when targetId is null")
            void doesNothingWhenTargetIdIsNull() {
                Card dissipate = createInstantCard("Dissipate");
                StackEntry dissipateEntry = counterAndExileEntry(dissipate, player2Id, null);

                counterSpellAndExileHandler.resolve(gd, dissipateEntry, new CounterSpellAndExileEffect());

                verify(exileService, never()).exileCard(any(), any(), any());
            }
}
