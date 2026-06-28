package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ControlTargetPlayerNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.ControlTargetPlayerNextTurnEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.TurnSupport;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ControlTargetPlayerNextTurnEffectHandlerTest {

    @Mock private CombatService combatService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private AuraAttachmentService auraAttachmentService;
    @Mock private TurnCleanupService turnCleanupService;
    @Mock private ExileService exileService;
    @InjectMocks
    private TurnSupport turnSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private ControlTargetPlayerNextTurnEffectHandler controlTargetPlayerNextTurnEffectHandler;

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
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        controlTargetPlayerNextTurnEffectHandler = new ControlTargetPlayerNextTurnEffectHandler(turnSupport, gameBroadcastService);

    }

    // ===== Helper methods =====

        private Card createCard(String name, CardType type) {
            Card card = new Card();
            card.setName(name);
            card.setType(type);
            return card;
        }

        private StackEntry createTargetedEntry(Card card, UUID controllerId, UUID targetId, List<com.github.laxika.magicalvibes.model.effect.CardEffect> effects) {
            return new StackEntry(
                    StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(),
                    effects, 0, targetId, null
            );
        }

        private StackEntry createUntargetedEntry(Card card, UUID controllerId, List<com.github.laxika.magicalvibes.model.effect.CardEffect> effects) {
            return new StackEntry(
                    StackEntryType.INSTANT_SPELL, card, controllerId, card.getName(),
                    effects, 0
            );
        }

        // =========================================================================
        // ExtraTurnEffect
        // =========================================================================

    @Test
            @DisplayName("Registers pending turn control for the target player")
            void registersTurnControl() {
                Card card = createCard("Mindslaver", CardType.ARTIFACT);
                ControlTargetPlayerNextTurnEffect effect = new ControlTargetPlayerNextTurnEffect();
                StackEntry entry = createTargetedEntry(card, player1Id, player2Id, List.of(effect));

                controlTargetPlayerNextTurnEffectHandler.resolve(gd, entry, new ControlTargetPlayerNextTurnEffect());

                assertThat(gd.pendingTurnControl).containsEntry(player2Id, player1Id);
            }

            @Test
            @DisplayName("Logs the turn control message")
            void logsTurnControl() {
                Card card = createCard("Mindslaver", CardType.ARTIFACT);
                ControlTargetPlayerNextTurnEffect effect = new ControlTargetPlayerNextTurnEffect();
                StackEntry entry = createTargetedEntry(card, player1Id, player2Id, List.of(effect));

                controlTargetPlayerNextTurnEffectHandler.resolve(gd, entry, new ControlTargetPlayerNextTurnEffect());

                verify(gameBroadcastService).logAndBroadcast(eq(gd),
                        eq("Player1 will control Player2 during their next turn."));
            }

            @Test
            @DisplayName("Does nothing when target player ID is null")
            void doesNothingWhenTargetNull() {
                Card card = createCard("Mindslaver", CardType.ARTIFACT);
                ControlTargetPlayerNextTurnEffect effect = new ControlTargetPlayerNextTurnEffect();
                StackEntry entry = createTargetedEntry(card, player1Id, null, List.of(effect));

                controlTargetPlayerNextTurnEffectHandler.resolve(gd, entry, new ControlTargetPlayerNextTurnEffect());

                assertThat(gd.pendingTurnControl).isEmpty();
                verify(gameBroadcastService, never()).logAndBroadcast(eq(gd), org.mockito.ArgumentMatchers.anyString());
            }

            @Test
            @DisplayName("Does nothing when target player is not in the game")
            void doesNothingWhenTargetNotInGame() {
                UUID unknownId = UUID.randomUUID();
                Card card = createCard("Mindslaver", CardType.ARTIFACT);
                ControlTargetPlayerNextTurnEffect effect = new ControlTargetPlayerNextTurnEffect();
                StackEntry entry = createTargetedEntry(card, player1Id, unknownId, List.of(effect));

                controlTargetPlayerNextTurnEffectHandler.resolve(gd, entry, new ControlTargetPlayerNextTurnEffect());

                assertThat(gd.pendingTurnControl).isEmpty();
            }

            @Test
            @DisplayName("Overwrites previous turn control for the same target")
            void overwritesPreviousTurnControl() {
                gd.pendingTurnControl.put(player2Id, player2Id);

                Card card = createCard("Mindslaver", CardType.ARTIFACT);
                ControlTargetPlayerNextTurnEffect effect = new ControlTargetPlayerNextTurnEffect();
                StackEntry entry = createTargetedEntry(card, player1Id, player2Id, List.of(effect));

                controlTargetPlayerNextTurnEffectHandler.resolve(gd, entry, new ControlTargetPlayerNextTurnEffect());

                assertThat(gd.pendingTurnControl).containsEntry(player2Id, player1Id);
            }
}
