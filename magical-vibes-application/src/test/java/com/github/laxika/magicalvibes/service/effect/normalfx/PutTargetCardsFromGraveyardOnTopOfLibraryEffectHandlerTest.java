package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PutTargetCardsFromGraveyardOnTopOfLibraryEffectHandler;
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
class PutTargetCardsFromGraveyardOnTopOfLibraryEffectHandlerTest {

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
    private PutTargetCardsFromGraveyardOnTopOfLibraryEffectHandler putCardsOnTopOfLibraryHandler;

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
        putCardsOnTopOfLibraryHandler = new PutTargetCardsFromGraveyardOnTopOfLibraryEffectHandler(support);

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
            @DisplayName("Moves targeted cards from graveyard to top of library")
            void movesCardsToTopOfLibrary() {
                Card artifact1 = createCard("Leonin Scimitar");
                Card artifact2 = createCard("Rod of Ruin");
                gd.playerGraveyards.get(player1Id).addAll(List.of(artifact1, artifact2));

                PutTargetCardsFromGraveyardOnTopOfLibraryEffect effect =
                        new PutTargetCardsFromGraveyardOnTopOfLibraryEffect(new CardTypePredicate(CardType.ARTIFACT));
                StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Frantic Salvage"),
                        player1Id, "Frantic Salvage", List.of(effect),
                        List.of(artifact1.getId(), artifact2.getId()));

                when(gameQueryService.findCardInGraveyardById(gd, artifact1.getId())).thenReturn(artifact1);
                when(gameQueryService.findCardInGraveyardById(gd, artifact2.getId())).thenReturn(artifact2);

                putCardsOnTopOfLibraryHandler.resolve(gd, entry, effect);

                assertThat(gd.playerGraveyards.get(player1Id)).isEmpty();
                assertThat(gd.playerDecks.get(player1Id)).extracting(Card::getName)
                        .containsExactlyInAnyOrder("Leonin Scimitar", "Rod of Ruin");
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("on top of their library") && msg.contains("from graveyard")));
            }

            @Test
            @DisplayName("Silently skips cards no longer in graveyard")
            void skipsCardsNoLongerInGraveyard() {
                Card artifact = createCard("Leonin Scimitar");
                // Card is no longer in graveyard

                PutTargetCardsFromGraveyardOnTopOfLibraryEffect effect =
                        new PutTargetCardsFromGraveyardOnTopOfLibraryEffect(new CardTypePredicate(CardType.ARTIFACT));
                StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Frantic Salvage"),
                        player1Id, "Frantic Salvage", List.of(effect),
                        List.of(artifact.getId()));

                when(gameQueryService.findCardInGraveyardById(gd, artifact.getId())).thenReturn(null);

                putCardsOnTopOfLibraryHandler.resolve(gd, entry, effect);

                assertThat(gd.playerDecks.get(player1Id)).isEmpty();
                verify(gameBroadcastService, never()).logAndBroadcast(any(), any());
            }
}
