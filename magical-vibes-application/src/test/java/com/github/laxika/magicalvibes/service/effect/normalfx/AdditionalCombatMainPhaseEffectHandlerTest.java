package com.github.laxika.magicalvibes.service.effect.normalfx;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdditionalCombatMainPhaseEffectHandlerTest {

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
    private AdditionalCombatMainPhaseEffectHandler additionalCombatMainPhaseEffectHandler;

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
        additionalCombatMainPhaseEffectHandler = new AdditionalCombatMainPhaseEffectHandler(gameBroadcastService);

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
            @DisplayName("Adds one additional combat/main phase pair")
            void addsOnePair() {
                Card card = createCard("Relentless Assault", CardType.SORCERY);
                AdditionalCombatMainPhaseEffect effect = new AdditionalCombatMainPhaseEffect(1);
                StackEntry entry = createUntargetedEntry(card, player1Id, List.of(effect));

                additionalCombatMainPhaseEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
            }

            @Test
            @DisplayName("Adds multiple additional combat/main phase pairs")
            void addsMultiplePairs() {
                Card card = createCard("World at War", CardType.SORCERY);
                AdditionalCombatMainPhaseEffect effect = new AdditionalCombatMainPhaseEffect(2);
                StackEntry entry = createUntargetedEntry(card, player1Id, List.of(effect));

                additionalCombatMainPhaseEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(2);
            }

            @Test
            @DisplayName("Stacks with existing additional combat/main phase pairs")
            void stacksWithExistingPairs() {
                gd.additionalCombatMainPhasePairs = 1;

                Card card = createCard("Relentless Assault", CardType.SORCERY);
                AdditionalCombatMainPhaseEffect effect = new AdditionalCombatMainPhaseEffect(1);
                StackEntry entry = createUntargetedEntry(card, player1Id, List.of(effect));

                additionalCombatMainPhaseEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(2);
            }

            @Test
            @DisplayName("Does nothing when count is zero")
            void doesNothingWhenCountZero() {
                Card card = createCard("Noop", CardType.SORCERY);
                AdditionalCombatMainPhaseEffect effect = new AdditionalCombatMainPhaseEffect(0);
                StackEntry entry = createUntargetedEntry(card, player1Id, List.of(effect));

                additionalCombatMainPhaseEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(0);
                verify(gameBroadcastService, never()).logAndBroadcast(eq(gd), any(GameLogEntry.class));
            }

            @Test
            @DisplayName("Does nothing when count is negative")
            void doesNothingWhenCountNegative() {
                Card card = createCard("Noop", CardType.SORCERY);
                AdditionalCombatMainPhaseEffect effect = new AdditionalCombatMainPhaseEffect(-1);
                StackEntry entry = createUntargetedEntry(card, player1Id, List.of(effect));

                additionalCombatMainPhaseEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(0);
            }

            @Test
            @DisplayName("Log message uses singular for one pair")
            void logMessageSingular() {
                Card card = createCard("Relentless Assault", CardType.SORCERY);
                AdditionalCombatMainPhaseEffect effect = new AdditionalCombatMainPhaseEffect(1);
                StackEntry entry = createUntargetedEntry(card, player1Id, List.of(effect));

                additionalCombatMainPhaseEffectHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("After this main phase, there is an additional combat phase followed by an additional main phase.")));
            }

            @Test
            @DisplayName("Log message uses plural for multiple pairs")
            void logMessagePlural() {
                Card card = createCard("World at War", CardType.SORCERY);
                AdditionalCombatMainPhaseEffect effect = new AdditionalCombatMainPhaseEffect(3);
                StackEntry entry = createUntargetedEntry(card, player1Id, List.of(effect));

                additionalCombatMainPhaseEffectHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("After this main phase, there are 3 additional combat phases followed by additional main phases.")));
            }
}
