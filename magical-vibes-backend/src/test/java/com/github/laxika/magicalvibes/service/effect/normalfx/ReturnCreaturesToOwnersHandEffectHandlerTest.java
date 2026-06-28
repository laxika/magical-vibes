package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BounceCreatureOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactsTargetPlayerOwnsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandOnCoinFlipLossEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.effect.normalfx.BounceCreatureOnUpkeepEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.BounceSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnArtifactsTargetPlayerOwnsToHandEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnCreaturesToOwnersHandEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnSelfToHandEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnSelfToHandOnCoinFlipLossEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnTargetPermanentToHandEffectHandler;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReturnCreaturesToOwnersHandEffectHandlerTest {

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
    private ReturnCreaturesToOwnersHandEffectHandler returnCreaturesToOwnersHandHandler;

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
        returnCreaturesToOwnersHandHandler = new ReturnCreaturesToOwnersHandEffectHandler(
                gameQueryService, gameBroadcastService, permanentRemovalService);

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
            @DisplayName("Returns all creatures on both sides")
            void returnsAllCreatures() {
                Card card = createCard("Evacuation");
                Permanent creature1 = createCreature("Grizzly Bears");
                Permanent creature2 = createCreature("Serra Angel");
                Permanent creature3 = createCreature("Grizzly Bears");
                gd.playerBattlefields.get(player1Id).add(creature1);
                gd.playerBattlefields.get(player1Id).add(creature2);
                gd.playerBattlefields.get(player2Id).add(creature3);

                ReturnCreaturesToOwnersHandEffect effect = new ReturnCreaturesToOwnersHandEffect(Set.of());
                StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), null);

                when(gameQueryService.isCreature(eq(gd), any())).thenReturn(true);
                when(gameQueryService.matchesFilters(any(), eq(Set.of()), any())).thenReturn(true);
                when(permanentRemovalService.removePermanentToHand(eq(gd), any())).thenReturn(true);

                returnCreaturesToOwnersHandHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService).removePermanentToHand(gd, creature1);
                verify(permanentRemovalService).removePermanentToHand(gd, creature2);
                verify(permanentRemovalService).removePermanentToHand(gd, creature3);
                verify(permanentRemovalService).removeOrphanedAuras(gd);
            }

            @Test
            @DisplayName("Does not return non-creature permanents")
            void doesNotReturnNonCreatures() {
                Card card = createCard("Evacuation");
                Permanent creature = createCreature("Grizzly Bears");
                Permanent enchantment = createEnchantment("Glorious Anthem");
                gd.playerBattlefields.get(player1Id).add(enchantment);
                gd.playerBattlefields.get(player1Id).add(creature);

                ReturnCreaturesToOwnersHandEffect effect = new ReturnCreaturesToOwnersHandEffect(Set.of());
                StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), null);

                when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
                when(gameQueryService.isCreature(gd, enchantment)).thenReturn(false);
                when(gameQueryService.matchesFilters(eq(creature), eq(Set.of()), any())).thenReturn(true);
                when(permanentRemovalService.removePermanentToHand(gd, creature)).thenReturn(true);

                returnCreaturesToOwnersHandHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService).removePermanentToHand(gd, creature);
                verify(permanentRemovalService, never()).removePermanentToHand(gd, enchantment);
            }

            @Test
            @DisplayName("Works with empty battlefields without crashing")
            void worksWithEmptyBattlefields() {
                Card card = createCard("Evacuation");
                ReturnCreaturesToOwnersHandEffect effect = new ReturnCreaturesToOwnersHandEffect(Set.of());
                StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), null);

                returnCreaturesToOwnersHandHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService, never()).removePermanentToHand(any(), any());
                verify(permanentRemovalService, never()).removeOrphanedAuras(any());
            }
}
