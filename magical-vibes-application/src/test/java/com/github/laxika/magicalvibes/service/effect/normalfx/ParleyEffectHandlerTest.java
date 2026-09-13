package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ParleyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ParleyEffectHandlerTest {

    @Mock
    private GameLogService gameLogService;
    @Mock
    private AwardManaEffectHandler awardManaEffectHandler;
    @Mock
    private LifeSupport lifeSupport;
    @Mock
    private PlayerInteractionSupport playerInteractionSupport;

    private GameData gameData;
    private UUID player1Id;
    private UUID player2Id;
    private ParleyEffectHandler handler;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gameData.orderedPlayerIds.addAll(List.of(player1Id, player2Id));
        gameData.playerIdToName.put(player1Id, "Player1");
        gameData.playerIdToName.put(player2Id, "Player2");
        gameData.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gameData.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        handler = new ParleyEffectHandler(gameLogService, awardManaEffectHandler, lifeSupport,
                playerInteractionSupport);
    }

    @Test
    @DisplayName("Counts all revealed nonlands before rewarding and draws each player")
    void countsNonlandsAndDrawsAllPlayers() {
        gameData.playerDecks.get(player1Id).add(createCard("Grizzly Bears", CardType.CREATURE));
        gameData.playerDecks.get(player2Id).add(createCard("Llanowar Elves", CardType.CREATURE));
        ParleyEffect effect = new ParleyEffect();
        Card source = createCard("Selvala, Explorer Returned", CardType.CREATURE);
        StackEntry entry = createEntry(source, effect);

        handler.resolve(gameData, entry, effect);

        verify(awardManaEffectHandler).resolve(eq(gameData), eq(entry), eq(new AwardManaEffect(ManaColor.GREEN, 2)));
        verify(lifeSupport).applyGainLife(gameData, player1Id, 2, source.getName(), entry.getCard(),
                StackEntryType.ACTIVATED_ABILITY);
        verify(playerInteractionSupport).applyDrawCards(gameData, player1Id, 1);
        verify(playerInteractionSupport).applyDrawCards(gameData, player2Id, 1);
    }

    @Test
    @DisplayName("Does not reward revealed lands")
    void doesNotRewardLands() {
        gameData.playerDecks.get(player1Id).add(createCard("Forest", CardType.LAND));
        gameData.playerDecks.get(player2Id).add(createCard("Plains", CardType.LAND));
        ParleyEffect effect = new ParleyEffect();
        StackEntry entry = createEntry(createCard("Selvala, Explorer Returned", CardType.CREATURE), effect);

        handler.resolve(gameData, entry, effect);

        verifyNoInteractions(awardManaEffectHandler, lifeSupport);
        verify(playerInteractionSupport).applyDrawCards(gameData, player1Id, 1);
        verify(playerInteractionSupport).applyDrawCards(gameData, player2Id, 1);
    }

    private StackEntry createEntry(Card source, ParleyEffect effect) {
        return new StackEntry(StackEntryType.ACTIVATED_ABILITY, source, player1Id, source.getName(),
                List.of(effect), 0, null, null);
    }

    private static Card createCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }
}
