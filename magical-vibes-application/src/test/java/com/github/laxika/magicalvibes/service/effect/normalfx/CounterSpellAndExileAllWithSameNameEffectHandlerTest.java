package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileAllWithSameNameEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CounterSpellAndExileAllWithSameNameEffectHandlerTest {

    @Mock private GraveyardService graveyardService;
    @Mock private ExileService exileService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private GameQueryService gameQueryService;
    @Mock private StateTriggerService stateTriggerService;
    @InjectMocks private CounterSupport counterSupport;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private CounterSpellAndExileAllWithSameNameEffectHandler handler;

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
        gd.playerGraveyards.put(player1Id, new ArrayList<>());
        gd.playerHands.put(player1Id, new ArrayList<>());
        gd.playerDecks.put(player1Id, new ArrayList<>());
        handler = new CounterSpellAndExileAllWithSameNameEffectHandler(
                counterSupport, graveyardService, gameBroadcastService);
    }

    private Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        return card;
    }

    private StackEntry counterboreEntry(UUID targetCardId) {
        Card counterbore = new Card();
        counterbore.setName("Counterbore");
        counterbore.setType(CardType.INSTANT);
        return new StackEntry(StackEntryType.INSTANT_SPELL, counterbore, player2Id,
                "Counterbore", List.of(new CounterSpellAndExileAllWithSameNameEffect()), 0, targetCardId, null);
    }

    @Test
    @DisplayName("An uncounterable spell stays on the stack but same-name copies are still exiled")
    void uncounterableSpellStillTriggersSearch() {
        Card bears = creature("Grizzly Bears");
        StackEntry bearsEntry = new StackEntry(StackEntryType.CREATURE_SPELL, bears, player1Id,
                bears.getName(), List.of());
        gd.stack.add(bearsEntry);

        gd.playerGraveyards.get(player1Id).add(creature("Grizzly Bears"));
        gd.playerHands.get(player1Id).add(creature("Grizzly Bears"));
        Card plains = creature("Plains");
        gd.playerDecks.get(player1Id).add(creature("Grizzly Bears"));
        gd.playerDecks.get(player1Id).add(plains);

        when(gameQueryService.isUncounterable(gd, bears)).thenReturn(true);

        handler.resolve(gd, counterboreEntry(bears.getId()), new CounterSpellAndExileAllWithSameNameEffect());

        // Uncounterable — the spell remains on the stack.
        assertThat(gd.stack).contains(bearsEntry);

        // But the search still exiles the three same-name copies from graveyard, hand, and library.
        assertThat(gd.getPlayerExiledCards(player1Id))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(3);
        assertThat(gd.playerDecks.get(player1Id)).contains(plains);
    }

    @Test
    @DisplayName("Fizzles entirely when the target spell has left the stack")
    void fizzlesWhenTargetGone() {
        gd.playerGraveyards.get(player1Id).add(creature("Grizzly Bears"));

        handler.resolve(gd, counterboreEntry(UUID.randomUUID()),
                new CounterSpellAndExileAllWithSameNameEffect());

        assertThat(gd.getPlayerExiledCards(player1Id)).isEmpty();
    }
}
