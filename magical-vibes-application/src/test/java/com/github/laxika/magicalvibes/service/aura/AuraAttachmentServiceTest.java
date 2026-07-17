package com.github.laxika.magicalvibes.service.aura;
import com.github.laxika.magicalvibes.model.GameLog;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuraAttachmentServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private GraveyardService graveyardService;
    @Mock private CreatureControlService creatureControlService;
    @Mock private PredicateEvaluationService predicateEvaluationService;

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

            verify(gameBroadcastService).logAndBroadcast(eq(gd), eq(GameLog.text("Holy Strength is put into the graveyard (enchanted creature left the battlefield).")));
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

            verify(gameBroadcastService).logAndBroadcast(eq(gd), eq(GameLog.text("Darksteel Axe becomes unattached (equipped creature left the battlefield).")));
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

    // ===== enforceAttachmentLegality — CR 704.5n / 704.5q =====

    @Nested
    @DisplayName("enforceAttachmentLegality - Aura legality (CR 704.5n)")
    class AuraAttachmentLegality {

        @Test
        @DisplayName("Aura is put into the graveyard when the enchanted permanent has protection from it")
        void auraEvictedWhenHostGainsProtection() {
            Permanent host = createCreature("White Knight");
            Permanent aura = createAura("Unholy Strength");
            aura.setAttachedTo(host.getId());
            gd.playerBattlefields.get(player1Id).add(host);
            gd.playerBattlefields.get(player1Id).add(aura);

            when(gameQueryService.findPermanentById(gd, host.getId())).thenReturn(host);
            when(gameQueryService.hasProtectionFromSource(gd, host, aura)).thenReturn(true);
            when(graveyardService.addCardToGraveyard(gd, player1Id, aura.getOriginalCard(), Zone.BATTLEFIELD)).thenReturn(true);

            var result = service.enforceAttachmentLegality(gd);

            assertThat(gd.playerBattlefields.get(player1Id)).containsExactly(host);
            assertThat(result.anyChange()).isTrue();
            assertThat(result.removals()).hasSize(1);
            assertThat(result.removals().get(0).controllerId()).isEqualTo(player1Id);
            verify(creatureControlService).reconcileControl(gd);
        }

        @Test
        @DisplayName("Aura is put into the graveyard when its enchant restriction no longer matches")
        void auraEvictedWhenEnchantRestrictionFails() {
            Permanent host = createCreature("Formerly Animated Land");
            var filter = new PermanentPredicateTargetFilter(
                    new PermanentIsCreaturePredicate(), "Target must be a creature");
            Permanent aura = createAuraWithEnchantRestriction("Spirit Link", filter);
            aura.setAttachedTo(host.getId());
            gd.playerBattlefields.get(player1Id).add(host);
            gd.playerBattlefields.get(player1Id).add(aura);

            when(gameQueryService.findPermanentById(gd, host.getId())).thenReturn(host);
            when(gameQueryService.hasProtectionFromSource(gd, host, aura)).thenReturn(false);
            when(predicateEvaluationService.checkTargetFilter(eq(filter), eq(host), any()))
                    .thenReturn(Optional.of("Target must be a creature"));
            when(graveyardService.addCardToGraveyard(gd, player1Id, aura.getOriginalCard(), Zone.BATTLEFIELD)).thenReturn(true);

            var result = service.enforceAttachmentLegality(gd);

            assertThat(gd.playerBattlefields.get(player1Id)).containsExactly(host);
            assertThat(result.removals()).hasSize(1);
        }

        @Test
        @DisplayName("Legally attached aura stays put")
        void legalAuraStaysAttached() {
            Permanent host = createCreature("Grizzly Bears");
            var filter = new PermanentPredicateTargetFilter(
                    new PermanentIsCreaturePredicate(), "Target must be a creature");
            Permanent aura = createAuraWithEnchantRestriction("Spirit Link", filter);
            aura.setAttachedTo(host.getId());
            gd.playerBattlefields.get(player1Id).add(host);
            gd.playerBattlefields.get(player1Id).add(aura);

            when(gameQueryService.findPermanentById(gd, host.getId())).thenReturn(host);
            when(gameQueryService.hasProtectionFromSource(gd, host, aura)).thenReturn(false);
            when(predicateEvaluationService.checkTargetFilter(eq(filter), eq(host), any()))
                    .thenReturn(Optional.empty());

            var result = service.enforceAttachmentLegality(gd);

            assertThat(gd.playerBattlefields.get(player1Id)).containsExactly(host, aura);
            assertThat(result.anyChange()).isFalse();
            verify(creatureControlService, never()).reconcileControl(gd);
        }

        @Test
        @DisplayName("Aura whose host already left the battlefield is left to the orphan cleanup")
        void auraOnDepartedHostIgnored() {
            UUID departedHostId = UUID.randomUUID();
            Permanent aura = createAura("Spirit Link");
            aura.setAttachedTo(departedHostId);
            gd.playerBattlefields.get(player1Id).add(aura);

            when(gameQueryService.findPermanentById(gd, departedHostId)).thenReturn(null);

            var result = service.enforceAttachmentLegality(gd);

            assertThat(gd.playerBattlefields.get(player1Id)).containsExactly(aura);
            assertThat(result.anyChange()).isFalse();
        }

        @Test
        @DisplayName("Aura enchanting a player with protection from its color is put into the graveyard")
        void auraOnProtectedPlayerEvicted() {
            Permanent aura = createAura("Curse of Thirst");
            aura.setAttachedTo(player2Id);
            gd.playerBattlefields.get(player1Id).add(aura);

            when(gameQueryService.getEffectiveColors(gd, aura)).thenReturn(Set.of(CardColor.BLACK));
            when(gameQueryService.playerHasProtectionFromColor(gd, player2Id, CardColor.BLACK)).thenReturn(true);
            when(graveyardService.addCardToGraveyard(gd, player1Id, aura.getOriginalCard(), Zone.BATTLEFIELD)).thenReturn(true);

            var result = service.enforceAttachmentLegality(gd);

            assertThat(gd.playerBattlefields.get(player1Id)).isEmpty();
            assertThat(result.removals()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("enforceAttachmentLegality - Equipment legality (CR 704.5q)")
    class EquipmentAttachmentLegality {

        @Test
        @DisplayName("Equipment becomes unattached when the equipped permanent stops being a creature")
        void equipmentUnattachesWhenHostNotACreature() {
            Permanent host = createCreature("Formerly Animated Land");
            Permanent equipment = createEquipment("Darksteel Axe");
            equipment.setAttachedTo(host.getId());
            gd.playerBattlefields.get(player1Id).add(host);
            gd.playerBattlefields.get(player1Id).add(equipment);

            when(gameQueryService.findPermanentById(gd, host.getId())).thenReturn(host);
            when(gameQueryService.hasProtectionFromSource(gd, host, equipment)).thenReturn(false);
            when(gameQueryService.isCreature(gd, host)).thenReturn(false);

            var result = service.enforceAttachmentLegality(gd);

            assertThat(gd.playerBattlefields.get(player1Id)).containsExactly(host, equipment);
            assertThat(equipment.getAttachedTo()).isNull();
            assertThat(result.anyChange()).isTrue();
            assertThat(result.removals()).isEmpty();
            verify(creatureControlService).reconcileControl(gd);
        }

        @Test
        @DisplayName("Equipment becomes unattached when the equipped creature has protection from it")
        void equipmentUnattachesWhenHostHasProtection() {
            Permanent host = createCreature("Pro-Artifacts Creature");
            Permanent equipment = createEquipment("Darksteel Axe");
            equipment.setAttachedTo(host.getId());
            gd.playerBattlefields.get(player1Id).add(host);
            gd.playerBattlefields.get(player1Id).add(equipment);

            when(gameQueryService.findPermanentById(gd, host.getId())).thenReturn(host);
            when(gameQueryService.hasProtectionFromSource(gd, host, equipment)).thenReturn(true);

            var result = service.enforceAttachmentLegality(gd);

            assertThat(equipment.getAttachedTo()).isNull();
            assertThat(result.anyChange()).isTrue();
        }

        @Test
        @DisplayName("Legally equipped equipment stays attached")
        void legalEquipmentStaysAttached() {
            Permanent host = createCreature("Grizzly Bears");
            Permanent equipment = createEquipment("Darksteel Axe");
            equipment.setAttachedTo(host.getId());
            gd.playerBattlefields.get(player1Id).add(host);
            gd.playerBattlefields.get(player1Id).add(equipment);

            when(gameQueryService.findPermanentById(gd, host.getId())).thenReturn(host);
            when(gameQueryService.hasProtectionFromSource(gd, host, equipment)).thenReturn(false);
            when(gameQueryService.isCreature(gd, host)).thenReturn(true);

            var result = service.enforceAttachmentLegality(gd);

            assertThat(equipment.getAttachedTo()).isEqualTo(host.getId());
            assertThat(result.anyChange()).isFalse();
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
        @DisplayName("removeOrphanedAuras reconciles the CR 613.2 control state afterwards")
        void removeOrphanedAurasReconcilesControl() {
            service.removeOrphanedAuras(gd);

            verify(creatureControlService).reconcileControl(gd);
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

    // The enchant restriction must be declared before Permanent creation freezes the card
    private Permanent createAuraWithEnchantRestriction(String name, PermanentPredicateTargetFilter filter) {
        Card card = createCard(name);
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.AURA));
        card.target(filter);
        return new Permanent(card);
    }

    private Permanent createEquipment(String name) {
        Card card = createCard(name);
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        return new Permanent(card);
    }
}
