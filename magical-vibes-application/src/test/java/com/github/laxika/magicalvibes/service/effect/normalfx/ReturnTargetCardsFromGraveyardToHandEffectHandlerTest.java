package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReturnTargetCardsFromGraveyardToHandEffectHandlerTest {

    @Mock
    private BattlefieldEntryService battlefieldEntryService;
    @Mock
    private PermanentRemovalService permanentRemovalService;
    @Mock
    private LegendRuleService legendRuleService;
    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private GameBroadcastService gameBroadcastService;
    @Mock
    private PlayerInputService playerInputService;
    @Mock
    private LifeSupport lifeSupport;
    @Mock
    private ExileService exileService;
    @Mock
    private GraveyardService graveyardService;
    @InjectMocks
    private GraveyardReturnSupport support;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private ReturnTargetCardsFromGraveyardToHandEffectHandler returnTargetCardsToHandHandler;

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
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        returnTargetCardsToHandHandler = new ReturnTargetCardsFromGraveyardToHandEffectHandler(support);

    }

    private static Card createCard(String name) {
            Card card = new Card();
            card.setName(name);
            return card;
        }

        // =========================================================================
        // describeFilter â€” static utility method
        // =========================================================================

    @Test
            @DisplayName("Returns multiple targeted cards from graveyard to hand")
            void returnsMultipleTargetedCardsToHand() {
                Card creature1 = createCard("Grizzly Bears");
                Card creature2 = createCard("Llanowar Elves");
                gd.playerGraveyards.get(player1Id).addAll(List.of(creature1, creature2));

                ReturnTargetCardsFromGraveyardToHandEffect effect =
                        new ReturnTargetCardsFromGraveyardToHandEffect(null, 2);
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Morbid Plunder"),
                        player1Id, "Morbid Plunder", List.of(effect),
                        List.of(creature1.getId(), creature2.getId()));

                when(gameQueryService.findCardInGraveyardById(gd, creature1.getId())).thenReturn(creature1);
                when(gameQueryService.findCardInGraveyardById(gd, creature2.getId())).thenReturn(creature2);

                returnTargetCardsToHandHandler.resolve(gd, entry, effect);

                assertThat(gd.playerGraveyards.get(player1Id)).isEmpty();
                assertThat(gd.playerHands.get(player1Id)).extracting(Card::getName)
                        .containsExactlyInAnyOrder("Grizzly Bears", "Llanowar Elves");
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("Grizzly Bears") && msg.contains("Llanowar Elves")
                                && msg.contains("graveyard to hand")));
            }

            @Test
            @DisplayName("Does nothing when no targets are selected")
            void doesNothingWhenNoTargets() {
                ReturnTargetCardsFromGraveyardToHandEffect effect =
                        new ReturnTargetCardsFromGraveyardToHandEffect(null, 2);
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Morbid Plunder"),
                        player1Id, "Morbid Plunder", List.of(effect),
                        List.of());

                returnTargetCardsToHandHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService, never()).logAndBroadcast(any(), any());
            }

            @Test
            @DisplayName("Silently skips cards no longer in graveyard")
            void skipsCardsNoLongerInGraveyard() {
                Card creature1 = createCard("Grizzly Bears");
                Card creature2 = createCard("Llanowar Elves");
                // Only creature1 is still in graveyard
                gd.playerGraveyards.get(player1Id).add(creature1);

                ReturnTargetCardsFromGraveyardToHandEffect effect =
                        new ReturnTargetCardsFromGraveyardToHandEffect(null, 2);
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Morbid Plunder"),
                        player1Id, "Morbid Plunder", List.of(effect),
                        List.of(creature1.getId(), creature2.getId()));

                when(gameQueryService.findCardInGraveyardById(gd, creature1.getId())).thenReturn(creature1);
                when(gameQueryService.findCardInGraveyardById(gd, creature2.getId())).thenReturn(null);

                returnTargetCardsToHandHandler.resolve(gd, entry, effect);

                assertThat(gd.playerHands.get(player1Id)).extracting(Card::getName)
                        .containsExactly("Grizzly Bears");
                assertThat(gd.playerGraveyards.get(player1Id)).isEmpty();
            }
}
