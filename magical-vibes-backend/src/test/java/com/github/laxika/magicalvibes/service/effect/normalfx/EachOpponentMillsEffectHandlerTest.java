package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMillsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;
import com.github.laxika.magicalvibes.model.effect.MillBottomOfTargetLibraryConditionalTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MillHalfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentMillsEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileTopCardsRepeatOnDuplicateEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.MillBottomOfTargetLibraryConditionalTokenEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.MillByHandSizeEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.MillControllerEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.MillHalfLibraryEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.MillTargetPlayerByChargeCountersEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.MillTargetPlayerEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EachOpponentMillsEffectHandlerTest {

    @Mock
    private GraveyardService graveyardService;
    @Mock
    private GameBroadcastService gameBroadcastService;
    @Mock
    private PermanentControlSupport permanentControlSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private EachOpponentMillsEffectHandler eachOpponentMillsEffectHandler;

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
        eachOpponentMillsEffectHandler = new EachOpponentMillsEffectHandler(graveyardService);

    }

    private static Card createCard(String name) {
            Card card = new Card();
            card.setName(name);
            return card;
        }

        // =========================================================================
        // resolveMillByHandSize
        // =========================================================================

    @Test
            @DisplayName("Mills each opponent but not the controller")
            void millsEachOpponentNotController() {
                EachOpponentMillsEffect effect = new EachOpponentMillsEffect(2);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Undead Alchemist"),
                        player1Id, "Undead Alchemist", List.of(effect));

                eachOpponentMillsEffectHandler.resolve(gd, entry, effect);

                verify(graveyardService).resolveMillPlayer(gd, player2Id, 2);
                verify(graveyardService, never()).resolveMillPlayer(eq(gd), eq(player1Id), any(int.class));
            }
}
