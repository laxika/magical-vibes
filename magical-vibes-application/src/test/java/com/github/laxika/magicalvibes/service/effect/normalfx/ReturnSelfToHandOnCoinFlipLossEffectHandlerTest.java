package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandOnCoinFlipLossEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.effect.normalfx.BounceSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnSelfToHandOnCoinFlipLossEffectHandler;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReturnSelfToHandOnCoinFlipLossEffectHandlerTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private GameOutcomeService gameOutcomeService;
    @Mock private PlayerInputService playerInputService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @InjectMocks
    private BounceSupport bounceSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private ReturnSelfToHandOnCoinFlipLossEffectHandler returnSelfToHandOnCoinFlipLossHandler;

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
        returnSelfToHandOnCoinFlipLossHandler = new ReturnSelfToHandOnCoinFlipLossEffectHandler(
                gameBroadcastService, bounceSupport);

    }

    // ===== Helper methods =====

        private Card createCard(String name) {
            Card card = new Card();
            card.setName(name);
            return card;
        }

        private Permanent createCreature(String name) {
            Card card = createCard(name);
            card.setType(CardType.CREATURE);
            return new Permanent(card);
        }

        private Permanent createArtifact(String name) {
            Card card = createCard(name);
            card.setType(CardType.ARTIFACT);
            return new Permanent(card);
        }

        private Permanent createEnchantment(String name) {
            Card card = createCard(name);
            card.setType(CardType.ENCHANTMENT);
            return new Permanent(card);
        }

        private StackEntry entryWithSource(Card card, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) {
            return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                    card.getName() + " trigger", effects, (UUID) null, sourcePermanentId);
        }

        private StackEntry entryWithTarget(Card card, UUID controllerId, List<CardEffect> effects, UUID targetId) {
            return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                    card.getName(), effects, 0, targetId, null);
        }

        private StackEntry entryWithTargetAndSource(Card card, UUID controllerId, List<CardEffect> effects,
                                                    UUID targetId, UUID sourcePermanentId) {
            return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                    card.getName() + " trigger", effects, targetId, sourcePermanentId);
        }

        // =========================================================================
        // ReturnSelfToHandEffect
        // =========================================================================

    @Test
            @DisplayName("Coin flip is logged and bounce or stay occurs")
            void coinFlipIsLoggedAndBounceOrStay() {
                Card card = createCard("Scoria Wurm");
                Permanent permanent = createCreature("Scoria Wurm");
                gd.playerBattlefields.get(player1Id).add(permanent);

                StackEntry entry = entryWithSource(card, player1Id,
                        List.of(new ReturnSelfToHandOnCoinFlipLossEffect()), permanent.getId());

                // These stubs may or may not be used depending on coin flip outcome
                lenient().when(gameQueryService.findPermanentById(gd, permanent.getId())).thenReturn(permanent);
                lenient().when(permanentRemovalService.removePermanentToHand(gd, permanent)).thenReturn(true);

                returnSelfToHandOnCoinFlipLossHandler.resolve(gd, entry, new ReturnSelfToHandOnCoinFlipLossEffect());

                // Verify coin flip was logged
                ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
                verify(gameBroadcastService, atLeastOnce()).logAndBroadcast(eq(gd), logCaptor.capture());
                List<String> allLogs = logCaptor.getAllValues();
                assertThat(allLogs).anyMatch(log -> log.contains("coin flip for Scoria Wurm"));

                // Verify exactly one outcome: won (no bounce) or lost (bounced)
                boolean won = allLogs.stream().anyMatch(log -> log.contains("wins the coin flip"));
                if (won) {
                    verify(permanentRemovalService, never()).removePermanentToHand(any(), any());
                } else {
                    verify(permanentRemovalService).removePermanentToHand(gd, permanent);
                }
            }
}
