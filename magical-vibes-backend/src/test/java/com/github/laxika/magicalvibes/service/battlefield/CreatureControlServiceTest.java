package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatureControlServiceTest {

    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private GameQueryService gameQueryService;

    @InjectMocks private CreatureControlService creatureControlService;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

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
    }

    private Card createCreatureCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private Permanent addCreature(UUID playerId, String name) {
        Card card = createCreatureCard(name);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(playerId).add(permanent);
        return permanent;
    }

    @Nested
    @DisplayName("stealPermanent")
    class StealPermanent {

        @Test
        @DisplayName("Moves creature from original owner's battlefield to new controller's battlefield")
        void movesCreatureBetweenBattlefields() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");
            when(gameQueryService.findPermanentController(gd, bear.getId())).thenReturn(player1Id);

            creatureControlService.stealPermanent(gd, player2Id, bear);

            assertThat(gd.playerBattlefields.get(player1Id))
                    .noneMatch(p -> p.getId().equals(bear.getId()));
            assertThat(gd.playerBattlefields.get(player2Id))
                    .anyMatch(p -> p.getId().equals(bear.getId()));
        }

        @Test
        @DisplayName("Sets creature as summoning sick after stealing")
        void setsSummoningSick() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");
            assertThat(bear.isSummoningSick()).isFalse();
            when(gameQueryService.findPermanentController(gd, bear.getId())).thenReturn(player1Id);

            creatureControlService.stealPermanent(gd, player2Id, bear);

            assertThat(bear.isSummoningSick()).isTrue();
        }

        @Test
        @DisplayName("Records original owner in stolenCreatures map")
        void recordsOriginalOwner() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");
            when(gameQueryService.findPermanentController(gd, bear.getId())).thenReturn(player1Id);

            creatureControlService.stealPermanent(gd, player2Id, bear);

            assertThat(gd.stolenCreatures).containsEntry(bear.getId(), player1Id);
        }

        @Test
        @DisplayName("Does not overwrite stolenCreatures entry when creature is stolen a second time")
        void doesNotOverwriteOriginalOwnerOnSecondSteal() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");

            // First steal: player1 -> player2
            when(gameQueryService.findPermanentController(gd, bear.getId())).thenReturn(player1Id);
            creatureControlService.stealPermanent(gd, player2Id, bear);
            assertThat(gd.stolenCreatures).containsEntry(bear.getId(), player1Id);

            // Second steal: player2 -> player1 (back to original, but entry stays)
            when(gameQueryService.findPermanentController(gd, bear.getId())).thenReturn(player2Id);
            creatureControlService.stealPermanent(gd, player1Id, bear);

            // Original owner should still be player1 (first entry preserved)
            assertThat(gd.stolenCreatures).containsEntry(bear.getId(), player1Id);
        }

        @Test
        @DisplayName("Does nothing when creature is already controlled by the new controller")
        void doesNothingWhenAlreadyControlled() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");
            when(gameQueryService.findPermanentController(gd, bear.getId())).thenReturn(player1Id);

            creatureControlService.stealPermanent(gd, player1Id, bear);

            // Creature should still be on player1's battlefield, unchanged
            assertThat(gd.playerBattlefields.get(player1Id))
                    .anyMatch(p -> p.getId().equals(bear.getId()));
            assertThat(bear.isSummoningSick()).isFalse();
            assertThat(gd.stolenCreatures).isEmpty();
        }

        @Test
        @DisplayName("Does nothing when creature is not found on any battlefield")
        void doesNothingWhenCreatureNotFound() {
            Permanent orphan = new Permanent(createCreatureCard("Grizzly Bears"));
            when(gameQueryService.findPermanentController(gd, orphan.getId())).thenReturn(null);

            int p1Size = gd.playerBattlefields.get(player1Id).size();
            int p2Size = gd.playerBattlefields.get(player2Id).size();

            creatureControlService.stealPermanent(gd, player2Id, orphan);

            assertThat(gd.playerBattlefields.get(player1Id)).hasSize(p1Size);
            assertThat(gd.playerBattlefields.get(player2Id)).hasSize(p2Size);
            assertThat(gd.stolenCreatures).isEmpty();
        }

        @Test
        @DisplayName("Logs the control change to the game log")
        void logsControlChange() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");
            when(gameQueryService.findPermanentController(gd, bear.getId())).thenReturn(player1Id);

            creatureControlService.stealPermanent(gd, player2Id, bear);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Player2 gains control of Grizzly Bears."));
        }

        @Test
        @DisplayName("Log entry contains the new controller's name")
        void logContainsNewControllerName() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");
            when(gameQueryService.findPermanentController(gd, bear.getId())).thenReturn(player1Id);

            creatureControlService.stealPermanent(gd, player2Id, bear);

            verify(gameBroadcastService).logAndBroadcast(any(),
                    eq("Player2 gains control of Grizzly Bears."));
        }

        @Test
        @DisplayName("Can steal multiple creatures independently")
        void canStealMultipleCreatures() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");
            Permanent angel = addCreature(player1Id, "Serra Angel");
            when(gameQueryService.findPermanentController(gd, bear.getId())).thenReturn(player1Id);
            when(gameQueryService.findPermanentController(gd, angel.getId())).thenReturn(player1Id);

            creatureControlService.stealPermanent(gd, player2Id, bear);
            creatureControlService.stealPermanent(gd, player2Id, angel);

            assertThat(gd.playerBattlefields.get(player1Id)).isEmpty();
            assertThat(gd.playerBattlefields.get(player2Id))
                    .extracting(Permanent::getId)
                    .contains(bear.getId(), angel.getId());
            assertThat(gd.stolenCreatures)
                    .containsEntry(bear.getId(), player1Id)
                    .containsEntry(angel.getId(), player1Id);
        }

        @Test
        @DisplayName("Stealing opponent's creature and stealing it back preserves original owner")
        void stealAndStealBackPreservesOriginalOwner() {
            Permanent bear = addCreature(player2Id, "Grizzly Bears");

            // Player1 steals from player2
            when(gameQueryService.findPermanentController(gd, bear.getId())).thenReturn(player2Id);
            creatureControlService.stealPermanent(gd, player1Id, bear);
            assertThat(gd.stolenCreatures).containsEntry(bear.getId(), player2Id);

            // Player2 steals it back
            when(gameQueryService.findPermanentController(gd, bear.getId())).thenReturn(player1Id);
            creatureControlService.stealPermanent(gd, player2Id, bear);

            // Original owner entry should still point to player2 (the true original)
            assertThat(gd.stolenCreatures).containsEntry(bear.getId(), player2Id);
            assertThat(gd.playerBattlefields.get(player2Id))
                    .anyMatch(p -> p.getId().equals(bear.getId()));
            assertThat(gd.playerBattlefields.get(player1Id))
                    .noneMatch(p -> p.getId().equals(bear.getId()));
        }

        @Test
        @DisplayName("Does not call broadcast when creature is not found")
        void doesNotBroadcastWhenNotFound() {
            Permanent orphan = new Permanent(createCreatureCard("Grizzly Bears"));
            when(gameQueryService.findPermanentController(gd, orphan.getId())).thenReturn(null);

            creatureControlService.stealPermanent(gd, player2Id, orphan);

            verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
        }

        @Test
        @DisplayName("Does not call broadcast when already controlled by new controller")
        void doesNotBroadcastWhenAlreadyControlled() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");
            when(gameQueryService.findPermanentController(gd, bear.getId())).thenReturn(player1Id);

            creatureControlService.stealPermanent(gd, player1Id, bear);

            verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
        }
    }
}
