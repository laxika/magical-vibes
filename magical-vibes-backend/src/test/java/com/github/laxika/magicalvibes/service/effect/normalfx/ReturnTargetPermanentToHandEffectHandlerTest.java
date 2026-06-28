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
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.effect.normalfx.BounceSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnTargetPermanentToHandEffectHandler;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReturnTargetPermanentToHandEffectHandlerTest {

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
    private ReturnTargetPermanentToHandEffectHandler returnTargetPermanentToHandHandler;

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
        returnTargetPermanentToHandHandler = new ReturnTargetPermanentToHandEffectHandler(
                gameQueryService, gameBroadcastService, gameOutcomeService, permanentRemovalService);

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
            @DisplayName("Returns target permanent to its owner's hand")
            void returnsTargetPermanentToHand() {
                Card card = createCard("Boomerang");
                Permanent target = createCreature("Grizzly Bears");
                gd.playerBattlefields.get(player2Id).add(target);

                StackEntry entry = entryWithTarget(card, player1Id,
                        List.of(new ReturnTargetPermanentToHandEffect()), target.getId());

                when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
                when(permanentRemovalService.removePermanentToHand(gd, target)).thenReturn(true);

                returnTargetPermanentToHandHandler.resolve(gd, entry, new ReturnTargetPermanentToHandEffect());

                verify(permanentRemovalService).removePermanentToHand(gd, target);
                verify(permanentRemovalService).removeOrphanedAuras(gd);
                verify(gameBroadcastService).logAndBroadcast(eq(gd),
                        eq("Grizzly Bears is returned to its owner's hand."));
            }

            @Test
            @DisplayName("Skips when target is removed before resolution")
            void skipsWhenTargetRemoved() {
                Card card = createCard("Boomerang");
                UUID targetId = UUID.randomUUID();

                StackEntry entry = entryWithTarget(card, player1Id,
                        List.of(new ReturnTargetPermanentToHandEffect()), targetId);

                when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);

                returnTargetPermanentToHandHandler.resolve(gd, entry, new ReturnTargetPermanentToHandEffect());

                verify(permanentRemovalService, never()).removePermanentToHand(any(), any());
                verify(permanentRemovalService).removeOrphanedAuras(gd);
            }

            @Test
            @DisplayName("Controller loses life when lifeLoss is set")
            void controllerLosesLifeWhenLifeLossSet() {
                Card card = createCard("Vapor Snag");
                Permanent target = createCreature("Grizzly Bears");
                gd.playerBattlefields.get(player2Id).add(target);
                gd.playerLifeTotals.put(player2Id, 20);

                StackEntry entry = entryWithTarget(card, player1Id,
                        List.of(new ReturnTargetPermanentToHandEffect(1)), target.getId());

                when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
                when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);
                when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);
                when(permanentRemovalService.removePermanentToHand(gd, target)).thenReturn(true);

                returnTargetPermanentToHandHandler.resolve(gd, entry, new ReturnTargetPermanentToHandEffect(1));

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(19);
                verify(gameOutcomeService).checkWinCondition(gd);
            }

            @Test
            @DisplayName("Life loss is skipped when player's life total can't change")
            void lifeLossSkippedWhenLifeCantChange() {
                Card card = createCard("Vapor Snag");
                Permanent target = createCreature("Grizzly Bears");
                gd.playerBattlefields.get(player2Id).add(target);
                gd.playerLifeTotals.put(player2Id, 20);

                StackEntry entry = entryWithTarget(card, player1Id,
                        List.of(new ReturnTargetPermanentToHandEffect(1)), target.getId());

                when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
                when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);
                when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(false);
                when(permanentRemovalService.removePermanentToHand(gd, target)).thenReturn(true);

                returnTargetPermanentToHandHandler.resolve(gd, entry, new ReturnTargetPermanentToHandEffect(1));

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
                verify(gameBroadcastService).logAndBroadcast(eq(gd),
                        eq("Player2's life total can't change."));
            }

            @Test
            @DisplayName("Can bounce own permanent")
            void canBounceOwnPermanent() {
                Card card = createCard("Boomerang");
                Permanent target = createCreature("Grizzly Bears");
                gd.playerBattlefields.get(player1Id).add(target);

                StackEntry entry = entryWithTarget(card, player1Id,
                        List.of(new ReturnTargetPermanentToHandEffect()), target.getId());

                when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
                when(permanentRemovalService.removePermanentToHand(gd, target)).thenReturn(true);

                returnTargetPermanentToHandHandler.resolve(gd, entry, new ReturnTargetPermanentToHandEffect());

                verify(permanentRemovalService).removePermanentToHand(gd, target);
            }
}
