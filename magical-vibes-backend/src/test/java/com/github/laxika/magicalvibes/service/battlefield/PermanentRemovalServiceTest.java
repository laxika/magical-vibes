package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnUnattachEffect;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermanentRemovalServiceTest {

    @Mock
    private GraveyardService graveyardService;

    @Mock
    private BattlefieldEntryService battlefieldEntryService;

    @Mock
    private TriggerCollectionService triggerCollectionService;

    @Mock
    private DamagePreventionService damagePreventionService;

    @Mock
    private AuraAttachmentService auraAttachmentService;

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @Mock
    private ExileService exileService;

    @InjectMocks
    private PermanentRemovalService prs;

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
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
    }

    // ===== Helper methods =====

    private static Card createCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private static Card createArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        return card;
    }

    private static Card createEquipmentWithSacrificeOnUnattach(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{2}");
        card.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        card.addEffect(EffectSlot.STATIC, new SacrificeOnUnattachEffect());
        return card;
    }

    private static Card createIndestructibleCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        return card;
    }

    private Permanent addPermanent(UUID playerId, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(playerId).add(permanent);
        return permanent;
    }

    private void stubGraveyardForCreature(Permanent target, UUID ownerId) {
        when(gameQueryService.isCreature(gd, target)).thenReturn(true);
        when(gameQueryService.isArtifact(target)).thenReturn(false);
        when(graveyardService.addCardToGraveyard(eq(gd), eq(ownerId), any(Card.class), eq(Zone.BATTLEFIELD))).thenReturn(true);
    }

    // =========================================================================
    // removePermanentToGraveyard
    // =========================================================================

    @Nested
    @DisplayName("removePermanentToGraveyard")
    class RemovePermanentToGraveyard {

        @Test
        @DisplayName("Removes permanent from battlefield and puts card in graveyard")
        void removesFromBattlefieldAndAddsToGraveyard() {
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears"));
            stubGraveyardForCreature(bears, player1Id);

            boolean result = prs.removePermanentToGraveyard(gd, bears);

            assertThat(result).isTrue();
            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(bears);
            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player1Id), eq(bears.getOriginalCard()), eq(Zone.BATTLEFIELD));
        }

        @Test
        @DisplayName("Returns false when permanent is not on any battlefield")
        void returnsFalseWhenNotOnBattlefield() {
            Permanent bears = new Permanent(createCreature("Grizzly Bears"));

            boolean result = prs.removePermanentToGraveyard(gd, bears);

            assertThat(result).isFalse();
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any(Card.class), any());
        }

        @Test
        @DisplayName("Permanent with exileIfLeavesBattlefield is exiled instead of going to graveyard")
        void exileReplacementExileIfLeaves() {
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears"));
            bears.setExileIfLeavesBattlefield(true);
            when(auraAttachmentService.removeOrphanedAuras(any())).thenReturn(List.of());

            boolean result = prs.removePermanentToGraveyard(gd, bears);

            assertThat(result).isTrue();
            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(bears);
            verify(exileService).exileCard(gd, player1Id, bears.getOriginalCard());
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any(Card.class), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), contains("exiled instead of going to the graveyard"));
        }

        @Test
        @DisplayName("Permanent with exileInsteadOfDieThisTurn is exiled instead of going to graveyard")
        void exileReplacementExileInsteadOfDie() {
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears"));
            bears.setExileInsteadOfDieThisTurn(true);
            when(auraAttachmentService.removeOrphanedAuras(any())).thenReturn(List.of());

            boolean result = prs.removePermanentToGraveyard(gd, bears);

            assertThat(result).isTrue();
            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(bears);
            verify(exileService).exileCard(gd, player1Id, bears.getOriginalCard());
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any(Card.class), any());
        }

        @Test
        @DisplayName("Stolen creature goes to original owner's graveyard")
        void stolenCreatureGoesToOriginalOwnersGraveyard() {
            Permanent stolen = addPermanent(player1Id, createCreature("Grizzly Bears"));
            gd.stolenCreatures.put(stolen.getId(), player2Id);
            stubGraveyardForCreature(stolen, player2Id);

            prs.removePermanentToGraveyard(gd, stolen);

            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player2Id), eq(stolen.getOriginalCard()), eq(Zone.BATTLEFIELD));
            verify(graveyardService, never()).addCardToGraveyard(eq(gd), eq(player1Id), any(Card.class), any());
        }

        @Test
        @DisplayName("Cleans up stolenCreatures entry after removal")
        void cleansUpStolenCreaturesEntry() {
            Permanent stolen = addPermanent(player1Id, createCreature("Grizzly Bears"));
            gd.stolenCreatures.put(stolen.getId(), player2Id);
            stubGraveyardForCreature(stolen, player2Id);

            prs.removePermanentToGraveyard(gd, stolen);

            assertThat(gd.stolenCreatures).doesNotContainKey(stolen.getId());
        }

        @Test
        @DisplayName("Creature death increments death count this turn")
        void creatureDeathIncreasesDeathCount() {
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears"));
            stubGraveyardForCreature(bears, player1Id);
            int deathsBefore = gd.creatureDeathCountThisTurn.getOrDefault(player1Id, 0);

            prs.removePermanentToGraveyard(gd, bears);

            int deathsAfter = gd.creatureDeathCountThisTurn.getOrDefault(player1Id, 0);
            assertThat(deathsAfter).isEqualTo(deathsBefore + 1);
        }

        @Test
        @DisplayName("Non-creature permanent death does not increment creature death count")
        void nonCreatureDoesNotIncrementDeathCount() {
            Permanent artifact = addPermanent(player1Id, createArtifact("Spellbook"));
            when(gameQueryService.isCreature(gd, artifact)).thenReturn(false);
            when(gameQueryService.isArtifact(artifact)).thenReturn(true);
            when(graveyardService.addCardToGraveyard(eq(gd), eq(player1Id), any(Card.class), eq(Zone.BATTLEFIELD))).thenReturn(true);
            int deathsBefore = gd.creatureDeathCountThisTurn.getOrDefault(player1Id, 0);

            prs.removePermanentToGraveyard(gd, artifact);

            int deathsAfter = gd.creatureDeathCountThisTurn.getOrDefault(player1Id, 0);
            assertThat(deathsAfter).isEqualTo(deathsBefore);
        }

        @Test
        @DisplayName("Fires death triggers for creatures sent to graveyard")
        void firesDeathTriggersForCreature() {
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears"));
            stubGraveyardForCreature(bears, player1Id);

            prs.removePermanentToGraveyard(gd, bears);

            verify(triggerCollectionService).collectDeathTrigger(eq(gd), eq(bears.getCard()), eq(player1Id), eq(true), eq(bears));
            verify(triggerCollectionService).checkAllyCreatureDeathTriggers(gd, player1Id, bears.getCard());
            verify(triggerCollectionService).checkOpponentCreatureDeathTriggers(gd, player1Id);
            verify(triggerCollectionService).checkEquippedCreatureDeathTriggers(gd, bears.getId(), player1Id);
        }

        @Test
        @DisplayName("Fires artifact graveyard trigger for artifacts sent to graveyard")
        void firesArtifactGraveyardTrigger() {
            Permanent artifact = addPermanent(player1Id, createArtifact("Spellbook"));
            when(gameQueryService.isCreature(gd, artifact)).thenReturn(false);
            when(gameQueryService.isArtifact(artifact)).thenReturn(true);
            when(graveyardService.addCardToGraveyard(eq(gd), eq(player1Id), any(Card.class), eq(Zone.BATTLEFIELD))).thenReturn(true);

            prs.removePermanentToGraveyard(gd, artifact);

            verify(triggerCollectionService).checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gd, player1Id, player1Id);
        }

        @Test
        @DisplayName("Exiled card returns to battlefield when source permanent leaves")
        void exileReturnOnLeave() {
            Permanent source = addPermanent(player1Id, createCreature("Serra Angel"));
            stubGraveyardForCreature(source, player1Id);

            Card exiledCard = createCreature("Grizzly Bears");
            gd.addToExile(player2Id, exiledCard);
            gd.exileReturnOnPermanentLeave.put(source.getId(), new PendingExileReturn(exiledCard, player2Id));

            prs.removePermanentToGraveyard(gd, source);

            assertThat(gd.getPlayerExiledCards(player2Id))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.exileReturnOnPermanentLeave).doesNotContainKey(source.getId());
            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player2Id), any(Permanent.class));
            verify(battlefieldEntryService).handleCreatureEnteredBattlefield(eq(gd), eq(player2Id), eq(exiledCard), isNull(), eq(false));
        }

        @Test
        @DisplayName("Exiled card returns to hand when source leaves and returnToHand is true")
        void exileReturnToHandOnLeave() {
            Permanent source = addPermanent(player1Id, createCreature("Kitesail Freebooter"));
            stubGraveyardForCreature(source, player1Id);

            Card exiledCard = new Card();
            exiledCard.setName("Cancel");
            exiledCard.setType(CardType.INSTANT);
            gd.addToExile(player2Id, exiledCard);
            gd.exileReturnOnPermanentLeave.put(source.getId(), new PendingExileReturn(exiledCard, player2Id, false, true));

            prs.removePermanentToGraveyard(gd, source);

            assertThat(gd.getPlayerExiledCards(player2Id))
                    .noneMatch(c -> c.getName().equals("Cancel"));
            assertThat(gd.exileReturnOnPermanentLeave).doesNotContainKey(source.getId());
            assertThat(gd.playerHands.get(player2Id))
                    .anyMatch(c -> c.getName().equals("Cancel"));
            verify(battlefieldEntryService, never()).putPermanentOntoBattlefield(eq(gd), eq(player2Id), any(Permanent.class));
        }

        @Test
        @DisplayName("Equipment with SacrificeOnUnattachEffect sacrifices attached creature when removed")
        void sacrificeOnUnattachWhenEquipmentRemoved() {
            Permanent creature = addPermanent(player1Id, createCreature("Grizzly Bears"));
            Permanent equipment = addPermanent(player1Id, createEquipmentWithSacrificeOnUnattach("Grafted Exoskeleton"));
            equipment.setAttachedTo(creature.getId());

            when(auraAttachmentService.removeOrphanedAuras(any())).thenReturn(List.of());
            when(gameQueryService.isCreature(gd, equipment)).thenReturn(false);
            when(gameQueryService.isArtifact(equipment)).thenReturn(true);
            when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
            when(gameQueryService.isArtifact(creature)).thenReturn(false);
            when(graveyardService.addCardToGraveyard(eq(gd), eq(player1Id), any(Card.class), eq(Zone.BATTLEFIELD))).thenReturn(true);
            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);

            prs.removePermanentToGraveyard(gd, equipment);

            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(equipment);
            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(creature);
            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player1Id), eq(equipment.getOriginalCard()), eq(Zone.BATTLEFIELD));
            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player1Id), eq(creature.getOriginalCard()), eq(Zone.BATTLEFIELD));
        }
    }

    // =========================================================================
    // removePermanentToHand
    // =========================================================================

    @Nested
    @DisplayName("removePermanentToHand")
    class RemovePermanentToHand {

        @Test
        @DisplayName("Removes permanent from battlefield and adds card to owner's hand")
        void removesToHand() {
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears"));

            boolean result = prs.removePermanentToHand(gd, bears);

            assertThat(result).isTrue();
            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(bears);
            assertThat(gd.playerHands.get(player1Id))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Returns false when permanent is not on any battlefield")
        void returnsFalseWhenNotOnBattlefield() {
            Permanent bears = new Permanent(createCreature("Grizzly Bears"));

            boolean result = prs.removePermanentToHand(gd, bears);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Permanent with exileIfLeavesBattlefield is exiled instead of returning to hand")
        void exileReplacementExileIfLeaves() {
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears"));
            bears.setExileIfLeavesBattlefield(true);
            when(auraAttachmentService.removeOrphanedAuras(any())).thenReturn(List.of());

            boolean result = prs.removePermanentToHand(gd, bears);

            assertThat(result).isTrue();
            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(bears);
            assertThat(gd.playerHands.get(player1Id))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            verify(exileService).exileCard(gd, player1Id, bears.getOriginalCard());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), contains("exiled instead of returning to hand"));
        }

        @Test
        @DisplayName("exileInsteadOfDieThisTurn does NOT redirect bounce to exile")
        void exileInsteadOfDieDoesNotAffectBounce() {
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears"));
            bears.setExileInsteadOfDieThisTurn(true);

            prs.removePermanentToHand(gd, bears);

            assertThat(gd.playerHands.get(player1Id))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.getPlayerExiledCards(player1Id))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Stolen creature returns to original owner's hand")
        void stolenCreatureReturnsToOriginalOwner() {
            Permanent stolen = addPermanent(player1Id, createCreature("Grizzly Bears"));
            gd.stolenCreatures.put(stolen.getId(), player2Id);

            prs.removePermanentToHand(gd, stolen);

            assertThat(gd.playerHands.get(player2Id))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerHands.get(player1Id))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.stolenCreatures).doesNotContainKey(stolen.getId());
        }

        @Test
        @DisplayName("Exiled card returns to battlefield when source permanent is bounced")
        void exileReturnOnLeave() {
            Permanent source = addPermanent(player1Id, createCreature("Serra Angel"));

            Card exiledCard = createCreature("Grizzly Bears");
            gd.addToExile(player2Id, exiledCard);
            gd.exileReturnOnPermanentLeave.put(source.getId(), new PendingExileReturn(exiledCard, player2Id));

            prs.removePermanentToHand(gd, source);

            assertThat(gd.getPlayerExiledCards(player2Id))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.exileReturnOnPermanentLeave).doesNotContainKey(source.getId());
            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player2Id), any(Permanent.class));
        }
    }

    // =========================================================================
    // removePermanentToExile
    // =========================================================================

    @Nested
    @DisplayName("removePermanentToExile")
    class RemovePermanentToExile {

        @Test
        @DisplayName("Removes permanent from battlefield and adds card to exile zone")
        void removesToExile() {
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears"));

            boolean result = prs.removePermanentToExile(gd, bears);

            assertThat(result).isTrue();
            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(bears);
            verify(exileService).exileCard(gd, player1Id, bears.getOriginalCard());
        }

        @Test
        @DisplayName("Returns false when permanent is not on any battlefield")
        void returnsFalseWhenNotOnBattlefield() {
            Permanent bears = new Permanent(createCreature("Grizzly Bears"));

            boolean result = prs.removePermanentToExile(gd, bears);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Stolen creature goes to original owner's exile zone")
        void stolenCreatureGoesToOriginalOwnersExile() {
            Permanent stolen = addPermanent(player1Id, createCreature("Grizzly Bears"));
            gd.stolenCreatures.put(stolen.getId(), player2Id);

            prs.removePermanentToExile(gd, stolen);

            verify(exileService).exileCard(gd, player2Id, stolen.getOriginalCard());
            verify(exileService, never()).exileCard(eq(gd), eq(player1Id), any(Card.class));
            assertThat(gd.stolenCreatures).doesNotContainKey(stolen.getId());
        }

        @Test
        @DisplayName("Equipment with SacrificeOnUnattachEffect sacrifices creature when exiled")
        void sacrificeOnUnattachWhenEquipmentExiled() {
            Permanent creature = addPermanent(player1Id, createCreature("Grizzly Bears"));
            Permanent equipment = addPermanent(player1Id, createEquipmentWithSacrificeOnUnattach("Grafted Exoskeleton"));
            equipment.setAttachedTo(creature.getId());

            when(auraAttachmentService.removeOrphanedAuras(any())).thenReturn(List.of());
            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
            when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
            when(gameQueryService.isArtifact(creature)).thenReturn(false);
            when(graveyardService.addCardToGraveyard(eq(gd), eq(player1Id), any(Card.class), eq(Zone.BATTLEFIELD))).thenReturn(true);

            prs.removePermanentToExile(gd, equipment);

            verify(exileService).exileCard(gd, player1Id, equipment.getOriginalCard());
            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(creature);
            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player1Id), eq(creature.getOriginalCard()), eq(Zone.BATTLEFIELD));
        }

        @Test
        @DisplayName("Exiled card returns to battlefield when source permanent is exiled")
        void exileReturnOnLeave() {
            Permanent source = addPermanent(player1Id, createCreature("Serra Angel"));

            Card exiledCard = createCreature("Grizzly Bears");
            gd.addToExile(player2Id, exiledCard);
            gd.exileReturnOnPermanentLeave.put(source.getId(), new PendingExileReturn(exiledCard, player2Id));

            prs.removePermanentToExile(gd, source);

            assertThat(gd.getPlayerExiledCards(player2Id))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.exileReturnOnPermanentLeave).doesNotContainKey(source.getId());
            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player2Id), any(Permanent.class));
        }
    }

    // =========================================================================
    // tryDestroyPermanent
    // =========================================================================

    @Nested
    @DisplayName("tryDestroyPermanent")
    class TryDestroyPermanent {

        @Test
        @DisplayName("Destroys a normal permanent and sends it to graveyard")
        void destroysNormalPermanent() {
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears"));
            when(auraAttachmentService.removeOrphanedAuras(any())).thenReturn(List.of());
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);
            stubGraveyardForCreature(bears, player1Id);

            boolean result = prs.tryDestroyPermanent(gd, bears);

            assertThat(result).isTrue();
            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(bears);
            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player1Id), eq(bears.getOriginalCard()), eq(Zone.BATTLEFIELD));
            verify(auraAttachmentService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Indestructible permanent is not destroyed")
        void indestructibleSurvives() {
            Permanent golem = addPermanent(player1Id, createIndestructibleCreature("Indestructible Golem"));
            when(gameQueryService.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).thenReturn(true);

            boolean result = prs.tryDestroyPermanent(gd, golem);

            assertThat(result).isFalse();
            assertThat(gd.playerBattlefields.get(player1Id)).contains(golem);
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any(Card.class), any());
        }

        @Test
        @DisplayName("Indestructible is logged when destruction is prevented")
        void indestructibleIsLogged() {
            Permanent golem = addPermanent(player1Id, createIndestructibleCreature("Indestructible Golem"));
            when(gameQueryService.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).thenReturn(true);

            prs.tryDestroyPermanent(gd, golem);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Indestructible Golem") && msg.contains("indestructible")));
        }

        @Test
        @DisplayName("Permanent with regeneration shield survives destruction")
        void regenerationPreventsDestruction() {
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears"));
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, bears)).thenReturn(true);

            boolean result = prs.tryDestroyPermanent(gd, bears);

            assertThat(result).isFalse();
            assertThat(gd.playerBattlefields.get(player1Id)).contains(bears);
        }

        @Test
        @DisplayName("cannotBeRegenerated flag bypasses regeneration")
        void cannotBeRegeneratedBypassesRegeneration() {
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears"));
            when(auraAttachmentService.removeOrphanedAuras(any())).thenReturn(List.of());
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            stubGraveyardForCreature(bears, player1Id);

            boolean result = prs.tryDestroyPermanent(gd, bears, true);

            assertThat(result).isTrue();
            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(bears);
            verify(graveyardService, never()).tryRegenerate(any(), any());
        }

        @Test
        @DisplayName("cannotBeRegenerated still respects indestructible")
        void cannotBeRegeneratedStillRespectsIndestructible() {
            Permanent golem = addPermanent(player1Id, createIndestructibleCreature("Indestructible Golem"));
            when(gameQueryService.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).thenReturn(true);

            boolean result = prs.tryDestroyPermanent(gd, golem, true);

            assertThat(result).isFalse();
            assertThat(gd.playerBattlefields.get(player1Id)).contains(golem);
        }
    }

    // =========================================================================
    // removeCardFromGraveyardById
    // =========================================================================

    @Nested
    @DisplayName("removeCardFromGraveyardById")
    class RemoveCardFromGraveyardById {

        @Test
        @DisplayName("Removes card from graveyard by its ID")
        void removesCardFromGraveyard() {
            Card bears = createCreature("Grizzly Bears");
            gd.playerGraveyards.get(player1Id).add(bears);

            prs.removeCardFromGraveyardById(gd, bears.getId());

            assertThat(gd.playerGraveyards.get(player1Id))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Cleans up creature death tracking set")
        void cleansUpDeathTracking() {
            Card bears = createCreature("Grizzly Bears");
            gd.playerGraveyards.get(player1Id).add(bears);
            gd.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn
                    .computeIfAbsent(player1Id, k -> new HashSet<>())
                    .add(bears.getId());

            prs.removeCardFromGraveyardById(gd, bears.getId());

            assertThat(gd.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.get(player1Id))
                    .doesNotContain(bears.getId());
        }

        @Test
        @DisplayName("No-op when card is not in any graveyard")
        void noOpWhenNotInGraveyard() {
            UUID fakeId = UUID.randomUUID();
            int sizeP1 = gd.playerGraveyards.get(player1Id).size();
            int sizeP2 = gd.playerGraveyards.get(player2Id).size();

            prs.removeCardFromGraveyardById(gd, fakeId);

            assertThat(gd.playerGraveyards.get(player1Id)).hasSize(sizeP1);
            assertThat(gd.playerGraveyards.get(player2Id)).hasSize(sizeP2);
        }
    }

    // =========================================================================
    // redirectPlayerDamageToEnchantedCreature
    // =========================================================================

    @Nested
    @DisplayName("redirectPlayerDamageToEnchantedCreature")
    class RedirectPlayerDamageToEnchantedCreature {

        @Test
        @DisplayName("Returns damage unchanged when no redirect aura is present")
        void noRedirectWhenNoAura() {
            when(gameQueryService.findEnchantedCreatureByAuraEffect(eq(gd), eq(player1Id), eq(RedirectPlayerDamageToEnchantedCreatureEffect.class)))
                    .thenReturn(null);

            int result = prs.redirectPlayerDamageToEnchantedCreature(gd, player1Id, 5, "Lightning Bolt");

            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("Returns zero damage when redirect is <= 0")
        void returnsZeroOrNegativeDamageAsIs() {
            int result = prs.redirectPlayerDamageToEnchantedCreature(gd, player1Id, 0, "Source");

            assertThat(result).isEqualTo(0);
            verify(gameQueryService, never()).findEnchantedCreatureByAuraEffect(any(), any(), any());
        }

        @Test
        @DisplayName("Redirects damage to enchanted creature and returns 0")
        void redirectsDamageToEnchantedCreature() {
            Permanent creature = addPermanent(player1Id, createCreature("Serra Angel"));
            when(gameQueryService.findEnchantedCreatureByAuraEffect(eq(gd), eq(player1Id), eq(RedirectPlayerDamageToEnchantedCreatureEffect.class)))
                    .thenReturn(creature);
            when(damagePreventionService.applyCreaturePreventionShield(gd, creature, 3, false)).thenReturn(3);
            when(gameQueryService.getEffectiveToughness(gd, creature)).thenReturn(5);

            int result = prs.redirectPlayerDamageToEnchantedCreature(gd, player1Id, 3, "Lightning Bolt");

            assertThat(result).isEqualTo(0);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Serra Angel") && msg.contains("absorbs") && msg.contains("redirected")));
        }

        @Test
        @DisplayName("Enchanted creature is destroyed when redirected damage meets toughness")
        void enchantedCreatureDestroyedByLethalDamage() {
            Permanent creature = addPermanent(player1Id, createCreature("Serra Angel"));
            when(auraAttachmentService.removeOrphanedAuras(any())).thenReturn(List.of());
            when(gameQueryService.findEnchantedCreatureByAuraEffect(eq(gd), eq(player1Id), eq(RedirectPlayerDamageToEnchantedCreatureEffect.class)))
                    .thenReturn(creature);
            when(damagePreventionService.applyCreaturePreventionShield(gd, creature, 4, false)).thenReturn(4);
            when(gameQueryService.getEffectiveToughness(gd, creature)).thenReturn(4);
            // tryDestroyPermanent stubs
            when(gameQueryService.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, creature)).thenReturn(false);
            stubGraveyardForCreature(creature, player1Id);

            prs.redirectPlayerDamageToEnchantedCreature(gd, player1Id, 4, "Fireball");

            assertThat(gd.playerBattlefields.get(player1Id)).doesNotContain(creature);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Serra Angel") && msg.contains("destroyed")));
        }

        @Test
        @DisplayName("Indestructible enchanted creature survives lethal redirected damage")
        void indestructibleSurvivesLethalRedirect() {
            Permanent creature = addPermanent(player1Id, createIndestructibleCreature("Indestructible Golem"));
            when(gameQueryService.findEnchantedCreatureByAuraEffect(eq(gd), eq(player1Id), eq(RedirectPlayerDamageToEnchantedCreatureEffect.class)))
                    .thenReturn(creature);
            when(damagePreventionService.applyCreaturePreventionShield(gd, creature, 5, false)).thenReturn(5);
            when(gameQueryService.getEffectiveToughness(gd, creature)).thenReturn(2);
            when(gameQueryService.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).thenReturn(true);

            int result = prs.redirectPlayerDamageToEnchantedCreature(gd, player1Id, 5, "Fireball");

            assertThat(result).isEqualTo(0);
            assertThat(gd.playerBattlefields.get(player1Id)).contains(creature);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("indestructible")));
        }
    }
}
