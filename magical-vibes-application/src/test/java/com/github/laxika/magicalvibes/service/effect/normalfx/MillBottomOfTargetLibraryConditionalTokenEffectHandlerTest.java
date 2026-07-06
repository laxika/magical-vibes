package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillBottomOfTargetLibraryConditionalTokenEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MillBottomOfTargetLibraryConditionalTokenEffectHandlerTest {

    @Mock
    private GraveyardService graveyardService;
    @Mock
    private GameBroadcastService gameBroadcastService;
    @Mock
    private PermanentControlSupport permanentControlSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private MillBottomOfTargetLibraryConditionalTokenEffectHandler millBottomOfTargetLibraryConditionalTokenEffectHandler;

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
        gd.activePlayerId = player1Id;
        millBottomOfTargetLibraryConditionalTokenEffectHandler =
                new MillBottomOfTargetLibraryConditionalTokenEffectHandler(
                        graveyardService, gameBroadcastService, permanentControlSupport);

    }

    private static Card createCard(String name) {
            Card card = new Card();
            card.setName(name);
            return card;
        }

        // =========================================================================
        // resolveMillByHandSize
        // =========================================================================

    private MillBottomOfTargetLibraryConditionalTokenEffect cellarDoorEffect() {
                return new MillBottomOfTargetLibraryConditionalTokenEffect(
                        CardType.CREATURE, "Zombie", 2, 2, CardColor.BLACK,
                        List.of(CardSubtype.ZOMBIE));
            }

            @Test
            @DisplayName("Empty library does nothing")
            void emptyLibraryDoesNothing() {
                MillBottomOfTargetLibraryConditionalTokenEffect effect = cellarDoorEffect();
                Card source = createCard("Cellar Door");
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source,
                        player1Id, "Cellar Door", List.of(effect), 0, player2Id, null);

                millBottomOfTargetLibraryConditionalTokenEffectHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("library is empty")));
                verifyNoInteractions(graveyardService);
                verifyNoInteractions(permanentControlSupport);
            }

            @Test
            @DisplayName("Bottom card is creature â€” mills and creates token")
            void creatureCardCreatesToken() {
                Card creature = createCard("Grizzly Bears");
                creature.setType(CardType.CREATURE);
                gd.playerDecks.get(player2Id).add(creature);

                MillBottomOfTargetLibraryConditionalTokenEffect effect = cellarDoorEffect();
                Card source = createCard("Cellar Door");
                source.setSetCode("ISD");
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source,
                        player1Id, "Cellar Door", List.of(effect), 0, player2Id, null);

                millBottomOfTargetLibraryConditionalTokenEffectHandler.resolve(gd, entry, effect);

                verify(graveyardService).addCardToGraveyard(gd, player2Id, creature);
                verify(permanentControlSupport).applyCreateToken(
                        eq(gd), eq(player1Id), any(CreateTokenEffect.class), eq("ISD"));
                assertThat(gd.playerDecks.get(player2Id)).isEmpty();
            }

            @Test
            @DisplayName("Bottom card is non-creature â€” mills but no token")
            void nonCreatureCardNoToken() {
                Card land = createCard("Forest");
                land.setType(CardType.LAND);
                gd.playerDecks.get(player2Id).add(land);

                MillBottomOfTargetLibraryConditionalTokenEffect effect = cellarDoorEffect();
                Card source = createCard("Cellar Door");
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source,
                        player1Id, "Cellar Door", List.of(effect), 0, player2Id, null);

                millBottomOfTargetLibraryConditionalTokenEffectHandler.resolve(gd, entry, effect);

                verify(graveyardService).addCardToGraveyard(gd, player2Id, land);
                verifyNoInteractions(permanentControlSupport);
                assertThat(gd.playerDecks.get(player2Id)).isEmpty();
            }
}
