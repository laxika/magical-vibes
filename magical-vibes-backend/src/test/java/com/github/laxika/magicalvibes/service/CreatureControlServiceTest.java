package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreatureControlServiceTest extends BaseCardTest {

    private CreatureControlService creatureControlService;

    @BeforeEach
    void setUpService() {
        creatureControlService = new CreatureControlService(new GameBroadcastService(null, null, null, null, null), gqs);
    }

    private Permanent addCreature(UUID playerId, GrizzlyBears card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(playerId).add(permanent);
        return permanent;
    }

    private Permanent addCreature(UUID playerId, SerraAngel card) {
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
            Permanent bear = addCreature(player1.getId(), new GrizzlyBears());

            creatureControlService.stealPermanent(gd, player2.getId(), bear);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getId().equals(bear.getId()));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getId().equals(bear.getId()));
        }

        @Test
        @DisplayName("Sets creature as summoning sick after stealing")
        void setsSummoningSick() {
            Permanent bear = addCreature(player1.getId(), new GrizzlyBears());
            assertThat(bear.isSummoningSick()).isFalse();

            creatureControlService.stealPermanent(gd, player2.getId(), bear);

            assertThat(bear.isSummoningSick()).isTrue();
        }

        @Test
        @DisplayName("Records original owner in stolenCreatures map")
        void recordsOriginalOwner() {
            Permanent bear = addCreature(player1.getId(), new GrizzlyBears());

            creatureControlService.stealPermanent(gd, player2.getId(), bear);

            assertThat(gd.stolenCreatures).containsEntry(bear.getId(), player1.getId());
        }

        @Test
        @DisplayName("Does not overwrite stolenCreatures entry when creature is stolen a second time")
        void doesNotOverwriteOriginalOwnerOnSecondSteal() {
            Permanent bear = addCreature(player1.getId(), new GrizzlyBears());

            // First steal: player1 -> player2
            creatureControlService.stealPermanent(gd, player2.getId(), bear);
            assertThat(gd.stolenCreatures).containsEntry(bear.getId(), player1.getId());

            // Second steal: player2 -> player1 (back to original, but entry stays)
            creatureControlService.stealPermanent(gd, player1.getId(), bear);

            // Original owner should still be player1 (first entry preserved)
            assertThat(gd.stolenCreatures).containsEntry(bear.getId(), player1.getId());
        }

        @Test
        @DisplayName("Does nothing when creature is already controlled by the new controller")
        void doesNothingWhenAlreadyControlled() {
            Permanent bear = addCreature(player1.getId(), new GrizzlyBears());

            creatureControlService.stealPermanent(gd, player1.getId(), bear);

            // Creature should still be on player1's battlefield, unchanged
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getId().equals(bear.getId()));
            assertThat(bear.isSummoningSick()).isFalse();
            assertThat(gd.stolenCreatures).isEmpty();
        }

        @Test
        @DisplayName("Does nothing when creature is not found on any battlefield")
        void doesNothingWhenCreatureNotFound() {
            Permanent orphan = new Permanent(new GrizzlyBears());

            int p1Size = gd.playerBattlefields.get(player1.getId()).size();
            int p2Size = gd.playerBattlefields.get(player2.getId()).size();

            creatureControlService.stealPermanent(gd, player2.getId(), orphan);

            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(p1Size);
            assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(p2Size);
            assertThat(gd.stolenCreatures).isEmpty();
        }

        @Test
        @DisplayName("Logs the control change to the game log")
        void logsControlChange() {
            Permanent bear = addCreature(player1.getId(), new GrizzlyBears());
            int logSizeBefore = gd.gameLog.size();

            creatureControlService.stealPermanent(gd, player2.getId(), bear);

            assertThat(gd.gameLog).hasSizeGreaterThan(logSizeBefore);
            assertThat(gd.gameLog).anyMatch(log ->
                    log.contains("gains control of") && log.contains("Grizzly Bears"));
        }

        @Test
        @DisplayName("Log entry contains the new controller's name")
        void logContainsNewControllerName() {
            Permanent bear = addCreature(player1.getId(), new GrizzlyBears());
            String player2Name = gd.playerIdToName.get(player2.getId());

            creatureControlService.stealPermanent(gd, player2.getId(), bear);

            assertThat(gd.gameLog).anyMatch(log -> log.contains(player2Name));
        }

        @Test
        @DisplayName("Can steal multiple creatures independently")
        void canStealMultipleCreatures() {
            Permanent bear = addCreature(player1.getId(), new GrizzlyBears());
            Permanent angel = addCreature(player1.getId(), new SerraAngel());

            creatureControlService.stealPermanent(gd, player2.getId(), bear);
            creatureControlService.stealPermanent(gd, player2.getId(), angel);

            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .extracting(Permanent::getId)
                    .contains(bear.getId(), angel.getId());
            assertThat(gd.stolenCreatures)
                    .containsEntry(bear.getId(), player1.getId())
                    .containsEntry(angel.getId(), player1.getId());
        }

        @Test
        @DisplayName("Stealing opponent's creature and stealing it back preserves original owner")
        void stealAndStealBackPreservesOriginalOwner() {
            Permanent bear = addCreature(player2.getId(), new GrizzlyBears());

            // Player1 steals from player2
            creatureControlService.stealPermanent(gd, player1.getId(), bear);
            assertThat(gd.stolenCreatures).containsEntry(bear.getId(), player2.getId());

            // Player2 steals it back
            creatureControlService.stealPermanent(gd, player2.getId(), bear);

            // Original owner entry should still point to player2 (the true original)
            assertThat(gd.stolenCreatures).containsEntry(bear.getId(), player2.getId());
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getId().equals(bear.getId()));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getId().equals(bear.getId()));
        }
    }
}
