package com.github.laxika.magicalvibes.service.aura;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuraAttachmentServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private GraveyardService graveyardService;

    @InjectMocks private AuraAttachmentService service;

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

    // ===== removeOrphanedAuras — Aura behavior =====

    @Nested
    @DisplayName("removeOrphanedAuras - Aura behavior")
    class OrphanedAuras {

        @Test
        @DisplayName("Aura goes to graveyard when enchanted creature is destroyed")
        void auraGoesToGraveyardWhenCreatureDestroyed() {
            UUID deadCreatureId = UUID.randomUUID();
            Permanent aura = createAura("Holy Strength");
            aura.setAttachedTo(deadCreatureId);
            gd.playerBattlefields.get(player1Id).add(aura);

            when(gameQueryService.findPermanentById(gd, deadCreatureId)).thenReturn(null);

            service.removeOrphanedAuras(gd);

            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(aura);
            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player1Id), eq(aura.getOriginalCard()), eq(Zone.BATTLEFIELD));
        }

        @Test
        @DisplayName("Multiple auras on same creature all go to graveyard when creature dies")
        void multipleAurasRemovedWhenCreatureDies() {
            UUID deadCreatureId = UUID.randomUUID();
            Permanent aura1 = createAura("Holy Strength");
            aura1.setAttachedTo(deadCreatureId);
            Permanent aura2 = createAura("Holy Strength");
            aura2.setAttachedTo(deadCreatureId);
            gd.playerBattlefields.get(player1Id).add(aura1);
            gd.playerBattlefields.get(player1Id).add(aura2);

            when(gameQueryService.findPermanentById(gd, deadCreatureId)).thenReturn(null);

            service.removeOrphanedAuras(gd);

            assertThat(gd.playerBattlefields.get(player1Id)).isEmpty();
            verify(graveyardService).addCardToGraveyard(gd, player1Id, aura1.getOriginalCard(), Zone.BATTLEFIELD);
            verify(graveyardService).addCardToGraveyard(gd, player1Id, aura2.getOriginalCard(), Zone.BATTLEFIELD);
        }

        @Test
        @DisplayName("Aura from opponent goes to opponent's graveyard when creature dies")
        void opponentAuraGoesToOpponentGraveyard() {
            UUID deadCreatureId = UUID.randomUUID();
            Permanent aura = createAura("Holy Strength");
            aura.setAttachedTo(deadCreatureId);
            gd.playerBattlefields.get(player2Id).add(aura);

            when(gameQueryService.findPermanentById(gd, deadCreatureId)).thenReturn(null);

            service.removeOrphanedAuras(gd);

            assertThat(gd.playerBattlefields.get(player2Id)).doesNotContain(aura);
            verify(graveyardService).addCardToGraveyard(gd, player2Id, aura.getOriginalCard(), Zone.BATTLEFIELD);
        }

        @Test
        @DisplayName("Aura attached to a valid creature is not removed")
        void auraNotRemovedWhenCreatureStillExists() {
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(creature);

            Permanent aura = createAura("Holy Strength");
            aura.setAttachedTo(creature.getId());
            gd.playerBattlefields.get(player1Id).add(aura);

            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);

            service.removeOrphanedAuras(gd);

            assertThat(gd.playerBattlefields.get(player1Id)).contains(aura);
            assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Game log records aura going to graveyard when enchanted creature is destroyed")
        void gameLogRecordsAuraRemoval() {
            UUID deadCreatureId = UUID.randomUUID();
            Permanent aura = createAura("Holy Strength");
            aura.setAttachedTo(deadCreatureId);
            gd.playerBattlefields.get(player1Id).add(aura);

            when(gameQueryService.findPermanentById(gd, deadCreatureId)).thenReturn(null);

            service.removeOrphanedAuras(gd);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Holy Strength is put into the graveyard (enchanted creature left the battlefield)."));
        }

        @Test
        @DisplayName("Player 1's aura on player 2's creature goes to player 1's graveyard")
        void crossPlayerAuraGoesToControllerGraveyard() {
            UUID deadCreatureId = UUID.randomUUID();
            Permanent aura = createAura("Holy Strength");
            aura.setAttachedTo(deadCreatureId);
            gd.playerBattlefields.get(player1Id).add(aura);

            when(gameQueryService.findPermanentById(gd, deadCreatureId)).thenReturn(null);

            service.removeOrphanedAuras(gd);

            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player1Id), any(), eq(Zone.BATTLEFIELD));
            verify(graveyardService, never()).addCardToGraveyard(eq(gd), eq(player2Id), any(), any());
        }
    }

    // ===== removeOrphanedAuras — Equipment behavior =====

    @Nested
    @DisplayName("removeOrphanedAuras - Equipment behavior")
    class OrphanedEquipment {

        @Test
        @DisplayName("Equipment stays on battlefield unattached when equipped creature is destroyed")
        void equipmentStaysOnBattlefieldWhenCreatureDestroyed() {
            UUID deadCreatureId = UUID.randomUUID();
            Permanent equipment = createEquipment("Darksteel Axe");
            equipment.setAttachedTo(deadCreatureId);
            gd.playerBattlefields.get(player1Id).add(equipment);

            when(gameQueryService.findPermanentById(gd, deadCreatureId)).thenReturn(null);

            service.removeOrphanedAuras(gd);

            assertThat(gd.playerBattlefields.get(player1Id)).contains(equipment);
            assertThat(equipment.getAttachedTo()).isNull();
        }

        @Test
        @DisplayName("Game log records equipment becoming unattached")
        void gameLogRecordsEquipmentUnattached() {
            UUID deadCreatureId = UUID.randomUUID();
            Permanent equipment = createEquipment("Darksteel Axe");
            equipment.setAttachedTo(deadCreatureId);
            gd.playerBattlefields.get(player1Id).add(equipment);

            when(gameQueryService.findPermanentById(gd, deadCreatureId)).thenReturn(null);

            service.removeOrphanedAuras(gd);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Darksteel Axe becomes unattached (equipped creature left the battlefield)."));
        }

        @Test
        @DisplayName("Equipment remains attached when equipped creature survives")
        void equipmentStaysAttachedWhenCreatureSurvives() {
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(creature);

            Permanent equipment = createEquipment("Darksteel Axe");
            equipment.setAttachedTo(creature.getId());
            gd.playerBattlefields.get(player1Id).add(equipment);

            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);

            service.removeOrphanedAuras(gd);

            assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        }

        @Test
        @DisplayName("Aura and equipment on same creature: aura goes to graveyard, equipment unattaches")
        void auraAndEquipmentOnSameCreature() {
            UUID deadCreatureId = UUID.randomUUID();

            Permanent aura = createAura("Holy Strength");
            aura.setAttachedTo(deadCreatureId);
            Permanent equipment = createEquipment("Darksteel Axe");
            equipment.setAttachedTo(deadCreatureId);

            gd.playerBattlefields.get(player1Id).add(aura);
            gd.playerBattlefields.get(player1Id).add(equipment);

            when(gameQueryService.findPermanentById(gd, deadCreatureId)).thenReturn(null);

            service.removeOrphanedAuras(gd);

            // Aura should be removed from battlefield
            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(aura);
            verify(graveyardService).addCardToGraveyard(gd, player1Id, aura.getOriginalCard(), Zone.BATTLEFIELD);

            // Equipment should stay on battlefield, unattached
            assertThat(gd.playerBattlefields.get(player1Id)).contains(equipment);
            assertThat(equipment.getAttachedTo()).isNull();
        }
    }

    // ===== returnStolenCreatures — enchantment-based control =====

    @Nested
    @DisplayName("returnStolenCreatures - enchantment-based control")
    class ReturnStolenCreatures {

        @Test
        @DisplayName("Creature returns to owner when controlling aura is removed")
        void creatureReturnsWhenControlAuraRemoved() {
            Permanent creature = createCreature("Grizzly Bears");
            creature.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(creature);
            gd.stolenCreatures.put(creature.getId(), player2Id);

            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
            when(gameQueryService.hasAuraWithEffect(eq(gd), eq(creature), eq(ControlEnchantedCreatureEffect.class))).thenReturn(false);

            service.returnStolenCreatures(gd, false);

            // Creature should be on player2's battlefield
            assertThat(gd.playerBattlefields.get(player2Id)).contains(creature);
            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(creature);
        }

        @Test
        @DisplayName("Returned creature has summoning sickness")
        void returnedCreatureHasSummoningSickness() {
            Permanent creature = createCreature("Grizzly Bears");
            creature.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(creature);
            gd.stolenCreatures.put(creature.getId(), player2Id);

            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
            when(gameQueryService.hasAuraWithEffect(eq(gd), eq(creature), eq(ControlEnchantedCreatureEffect.class))).thenReturn(false);

            service.returnStolenCreatures(gd, false);

            assertThat(creature.isSummoningSick()).isTrue();
        }

        @Test
        @DisplayName("Stolen creature tracking is cleaned up after return")
        void stolenCreatureTrackingCleanedUp() {
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(creature);
            gd.stolenCreatures.put(creature.getId(), player2Id);

            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
            when(gameQueryService.hasAuraWithEffect(eq(gd), eq(creature), eq(ControlEnchantedCreatureEffect.class))).thenReturn(false);

            service.returnStolenCreatures(gd, false);

            assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
        }

        @Test
        @DisplayName("Creature stays stolen while controlling aura remains attached")
        void creatureStaysStolenWhileAuraAttached() {
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(creature);
            gd.stolenCreatures.put(creature.getId(), player2Id);

            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
            when(gameQueryService.hasAuraWithEffect(eq(gd), eq(creature), eq(ControlEnchantedCreatureEffect.class))).thenReturn(true);

            service.returnStolenCreatures(gd, false);

            assertThat(gd.playerBattlefields.get(player1Id)).contains(creature);
            assertThat(gd.stolenCreatures).containsKey(creature.getId());
        }

        @Test
        @DisplayName("Game log records creature returning to owner")
        void gameLogRecordsCreatureReturn() {
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(creature);
            gd.stolenCreatures.put(creature.getId(), player2Id);

            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
            when(gameQueryService.hasAuraWithEffect(eq(gd), eq(creature), eq(ControlEnchantedCreatureEffect.class))).thenReturn(false);

            service.returnStolenCreatures(gd, false);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Grizzly Bears returns to Player2's control."));
        }
    }

    // ===== returnStolenCreatures — until end of turn (Threaten) =====

    @Nested
    @DisplayName("returnStolenCreatures - until end of turn steals")
    class UntilEndOfTurnSteals {

        @Test
        @DisplayName("Until-end-of-turn stolen creature is returned when includeUntilEndOfTurn is true")
        void untilEndOfTurnCreatureReturned() {
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(creature);
            gd.stolenCreatures.put(creature.getId(), player2Id);
            gd.untilEndOfTurnStolenCreatures.add(creature.getId());

            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
            when(gameQueryService.hasAuraWithEffect(eq(gd), eq(creature), eq(ControlEnchantedCreatureEffect.class))).thenReturn(false);

            service.returnStolenCreatures(gd, true);

            assertThat(gd.playerBattlefields.get(player2Id)).contains(creature);
            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(creature);
        }

        @Test
        @DisplayName("Until-end-of-turn creature is NOT returned when includeUntilEndOfTurn is false")
        void untilEndOfTurnCreatureNotReturnedWhenFlagIsFalse() {
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(creature);
            gd.stolenCreatures.put(creature.getId(), player2Id);
            gd.untilEndOfTurnStolenCreatures.add(creature.getId());

            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);

            service.returnStolenCreatures(gd, false);

            assertThat(gd.playerBattlefields.get(player1Id)).contains(creature);
            assertThat(gd.stolenCreatures).containsKey(creature.getId());
        }

        @Test
        @DisplayName("Non-temporary steal is NOT returned when includeUntilEndOfTurn is true")
        void nonTemporaryStealNotReturnedWhenUntilEndOfTurnFlagIsTrue() {
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(creature);
            gd.stolenCreatures.put(creature.getId(), player2Id);
            // NOT in untilEndOfTurnStolenCreatures

            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);

            service.returnStolenCreatures(gd, true);

            assertThat(gd.playerBattlefields.get(player1Id)).contains(creature);
            assertThat(gd.stolenCreatures).containsKey(creature.getId());
        }

        @Test
        @DisplayName("Until-end-of-turn tracking is cleaned up after return")
        void untilEndOfTurnTrackingCleanedUp() {
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(creature);
            gd.stolenCreatures.put(creature.getId(), player2Id);
            gd.untilEndOfTurnStolenCreatures.add(creature.getId());

            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
            when(gameQueryService.hasAuraWithEffect(eq(gd), eq(creature), eq(ControlEnchantedCreatureEffect.class))).thenReturn(false);

            service.returnStolenCreatures(gd, true);

            assertThat(gd.untilEndOfTurnStolenCreatures).doesNotContain(creature.getId());
            assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
        }
    }

    // ===== Edge cases =====

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("No auras or equipment on battlefield does not cause errors")
        void noAttachmentsDoesNotCauseErrors() {
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(creature);

            service.removeOrphanedAuras(gd);

            assertThat(gd.playerBattlefields.get(player2Id)).contains(creature);
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Stolen creature that no longer exists is cleaned up")
        void stolenCreatureThatNoLongerExistsIsCleanedUp() {
            UUID goneCreatureId = UUID.randomUUID();
            gd.stolenCreatures.put(goneCreatureId, player2Id);
            gd.untilEndOfTurnStolenCreatures.add(goneCreatureId);
            gd.enchantmentDependentStolenCreatures.add(goneCreatureId);

            when(gameQueryService.findPermanentById(gd, goneCreatureId)).thenReturn(null);

            service.returnStolenCreatures(gd, false);

            assertThat(gd.stolenCreatures).doesNotContainKey(goneCreatureId);
            assertThat(gd.untilEndOfTurnStolenCreatures).doesNotContain(goneCreatureId);
            assertThat(gd.enchantmentDependentStolenCreatures).doesNotContain(goneCreatureId);
        }

        @Test
        @DisplayName("Permanent control steal is never returned")
        void permanentControlStealIsNeverReturned() {
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(creature);
            gd.stolenCreatures.put(creature.getId(), player2Id);
            gd.permanentControlStolenCreatures.add(creature.getId());

            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);

            service.returnStolenCreatures(gd, false);

            assertThat(gd.playerBattlefields.get(player1Id)).contains(creature);
            assertThat(gd.stolenCreatures).containsKey(creature.getId());
        }

        @Test
        @DisplayName("Enchantment-dependent steal is not returned while creature is still enchanted")
        void enchantmentDependentStealNotReturnedWhileEnchanted() {
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(creature);
            gd.stolenCreatures.put(creature.getId(), player2Id);
            gd.enchantmentDependentStolenCreatures.add(creature.getId());

            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
            when(gameQueryService.hasAuraWithEffect(eq(gd), eq(creature), eq(ControlEnchantedCreatureEffect.class))).thenReturn(false);
            when(gameQueryService.isEnchanted(gd, creature)).thenReturn(true);

            service.returnStolenCreatures(gd, false);

            assertThat(gd.playerBattlefields.get(player1Id)).contains(creature);
            assertThat(gd.stolenCreatures).containsKey(creature.getId());
        }

        @Test
        @DisplayName("Enchantment-dependent steal IS returned when creature is no longer enchanted")
        void enchantmentDependentStealReturnedWhenNoLongerEnchanted() {
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(creature);
            gd.stolenCreatures.put(creature.getId(), player2Id);
            gd.enchantmentDependentStolenCreatures.add(creature.getId());

            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
            when(gameQueryService.hasAuraWithEffect(eq(gd), eq(creature), eq(ControlEnchantedCreatureEffect.class))).thenReturn(false);
            when(gameQueryService.isEnchanted(gd, creature)).thenReturn(false);

            service.returnStolenCreatures(gd, false);

            assertThat(gd.playerBattlefields.get(player2Id)).contains(creature);
            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(creature);
            assertThat(gd.enchantmentDependentStolenCreatures).doesNotContain(creature.getId());
        }

        @Test
        @DisplayName("removeOrphanedAuras also triggers returnStolenCreatures for non-temporary steals")
        void removeOrphanedAurasTriggersReturnStolenCreatures() {
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(creature);
            gd.stolenCreatures.put(creature.getId(), player2Id);

            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
            when(gameQueryService.hasAuraWithEffect(eq(gd), eq(creature), eq(ControlEnchantedCreatureEffect.class))).thenReturn(false);

            service.removeOrphanedAuras(gd);

            assertThat(gd.playerBattlefields.get(player2Id)).contains(creature);
            assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
        }
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

    private Permanent createAura(String name) {
        Card card = createCard(name);
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.AURA));
        return new Permanent(card);
    }

    private Permanent createEquipment(String name) {
        Card card = createCard(name);
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        return new Permanent(card);
    }
}
