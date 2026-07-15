package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndChangeLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevealTopCardPutIntoHandAndChangeLifeEffectHandlerTest {

    @Mock
    private LifeSupport lifeSupport;
    @Mock
    private GameBroadcastService gameBroadcastService;
    private GameData gd;
    private UUID player1Id;
    private RevealTopCardPutIntoHandAndChangeLifeEffectHandler handler;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));

        handler = new RevealTopCardPutIntoHandAndChangeLifeEffectHandler(lifeSupport, gameBroadcastService);
    }

    private static Card createCard(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        return card;
    }

    private StackEntry entryFor(RevealTopCardPutIntoHandAndChangeLifeEffect effect, String sourceName) {
        Card source = new Card();
        source.setName(sourceName);
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, source, player1Id, sourceName, List.of(effect));
    }

    @Test
    @DisplayName("Empty library logs and changes nothing")
    void emptyLibraryLogs() {
        var effect = new RevealTopCardPutIntoHandAndChangeLifeEffect(true);
        handler.resolve(gd, entryFor(effect, "Augury Adept"), effect);

        verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("library is empty")));
        verifyNoInteractions(lifeSupport);
    }

    @Test
    @DisplayName("gainLife=true puts card in hand and gains life equal to mana value")
    void gainsLife() {
        Card topCard = createCard("Grizzly Bears", "{1}{G}");
        gd.playerDecks.get(player1Id).add(topCard);

        var effect = new RevealTopCardPutIntoHandAndChangeLifeEffect(true);
        handler.resolve(gd, entryFor(effect, "Augury Adept"), effect);

        assertThat(gd.playerHands.get(player1Id)).contains(topCard);
        assertThat(gd.playerDecks.get(player1Id)).isEmpty();
        verify(lifeSupport).applyGainLife(eq(gd), eq(player1Id), eq(2), eq("Augury Adept"), any(), any());
        verify(lifeSupport, never()).applyLifeLoss(any(), any(), anyInt(), any());
    }

    @Test
    @DisplayName("gainLife=false puts card in hand and loses life equal to mana value")
    void losesLife() {
        Card topCard = createCard("Grizzly Bears", "{1}{G}");
        gd.playerDecks.get(player1Id).add(topCard);

        var effect = new RevealTopCardPutIntoHandAndChangeLifeEffect(false);
        handler.resolve(gd, entryFor(effect, "Dark Tutelage"), effect);

        assertThat(gd.playerHands.get(player1Id)).contains(topCard);
        verify(lifeSupport).applyLifeLoss(eq(gd), eq(player1Id), eq(2), eq("Dark Tutelage"));
        verify(lifeSupport, never()).applyGainLife(any(), any(), anyInt(), any(), any(), any());
    }

    @Test
    @DisplayName("MV 0 card does not touch life total")
    void mvZeroNoLifeChange() {
        Card topCard = createCard("Ornithopter", "{0}");
        gd.playerDecks.get(player1Id).add(topCard);

        var effect = new RevealTopCardPutIntoHandAndChangeLifeEffect(true);
        handler.resolve(gd, entryFor(effect, "Augury Adept"), effect);

        assertThat(gd.playerHands.get(player1Id)).contains(topCard);
        verifyNoInteractions(lifeSupport);
    }
}
