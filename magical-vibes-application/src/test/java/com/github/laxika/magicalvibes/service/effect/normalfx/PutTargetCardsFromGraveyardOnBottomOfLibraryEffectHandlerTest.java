package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PutTargetCardsFromGraveyardOnBottomOfLibraryEffectHandlerTest {

    @Mock
    private BattlefieldEntryService battlefieldEntryService;
    @Mock
    private PermanentRemovalService permanentRemovalService;
    @Mock
    private LegendRuleService legendRuleService;
    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private PredicateEvaluationService predicateEvaluationService;
    @Mock
    private GameLogService gameLogService;
    @Mock
    private PlayerInputService playerInputService;
    @Mock
    private LifeSupport lifeSupport;
    @Mock
    private ExileService exileService;
    @Mock
    private GraveyardService graveyardService;
    @Mock
    private InteractionHandlerRegistry interactionHandlerRegistry;
    @Mock
    private PermanentCounterSupport permanentCounterSupport;
    @Mock
    private ConditionEvaluationService conditionEvaluationService;
    @InjectMocks
    private GraveyardReturnSupport support;

    private GameData gd;
    private UUID playerId;
    private PutTargetCardsFromGraveyardOnBottomOfLibraryEffectHandler handler;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", playerId, "Player1");
        gd.playerIds.add(playerId);
        gd.orderedPlayerIds.add(playerId);
        gd.playerIdToName.put(playerId, "Player1");
        gd.playerBattlefields.put(playerId, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(playerId, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(playerId, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(playerId, Collections.synchronizedList(new ArrayList<>()));
        handler = new PutTargetCardsFromGraveyardOnBottomOfLibraryEffectHandler(support);
    }

    @Test
    @DisplayName("Moves targeted cards to the bottom in target order")
    void movesCardsToBottomInTargetOrder() {
        Card first = card("First");
        Card second = card("Second");
        Card existing = card("Existing");
        gd.playerGraveyards.get(playerId).addAll(List.of(first, second));
        gd.playerDecks.get(playerId).add(existing);

        PutTargetCardsFromGraveyardOnBottomOfLibraryEffect effect =
                new PutTargetCardsFromGraveyardOnBottomOfLibraryEffect(null, 4);
        StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, card("Bow of Nylea"),
                playerId, "Bow of Nylea's ability", List.of(effect),
                List.of(first.getId(), second.getId()));
        when(gameQueryService.findCardInGraveyardById(gd, first.getId())).thenReturn(first);
        when(gameQueryService.findCardInGraveyardById(gd, second.getId())).thenReturn(second);

        handler.resolve(gd, entry, effect);

        assertThat(gd.playerGraveyards.get(playerId)).isEmpty();
        assertThat(gd.playerDecks.get(playerId)).extracting(Card::getName)
                .containsExactly("Existing", "First", "Second");
        verify(gameLogService).append(eq(gd), argThat((GameLogEntry log) ->
                log.plainText().contains("on the bottom of their library")));
    }

    private static Card card(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
