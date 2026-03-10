package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.cards.g.GraftedExoskeleton;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.DeathTriggerService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermanentRemovalServiceTest extends BaseCardTest {

    @Mock
    private GraveyardService graveyardService;

    @Mock
    private BattlefieldEntryService battlefieldEntryService;

    @Mock
    private DeathTriggerService deathTriggerService;

    @Mock
    private DamagePreventionService damagePreventionService;

    @Mock
    private AuraAttachmentService auraAttachmentService;

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @InjectMocks
    private PermanentRemovalService prs;

    private Permanent addPermanent(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private static Card indestructibleCreature() {
        Card card = new Card();
        card.setName("Indestructible Golem");
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColor(null);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        return card;
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
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            stubGraveyardForCreature(bears, player1.getId());

            boolean result = prs.removePermanentToGraveyard(gd, bears);

            assertThat(result).isTrue();
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player1.getId()), eq(bears.getOriginalCard()), eq(Zone.BATTLEFIELD));
        }

        @Test
        @DisplayName("Returns false when permanent is not on any battlefield")
        void returnsFalseWhenNotOnBattlefield() {
            Permanent bears = new Permanent(new GrizzlyBears());

            boolean result = prs.removePermanentToGraveyard(gd, bears);

            assertThat(result).isFalse();
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any(Card.class), any());
        }

        @Test
        @DisplayName("Permanent with exileIfLeavesBattlefield is exiled instead of going to graveyard")
        void exileReplacementExileIfLeaves() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            bears.setExileIfLeavesBattlefield(true);

            boolean result = prs.removePermanentToGraveyard(gd, bears);

            assertThat(result).isTrue();
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any(Card.class), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), contains("exiled instead of going to the graveyard"));
        }

        @Test
        @DisplayName("Permanent with exileInsteadOfDieThisTurn is exiled instead of going to graveyard")
        void exileReplacementExileInsteadOfDie() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            bears.setExileInsteadOfDieThisTurn(true);

            boolean result = prs.removePermanentToGraveyard(gd, bears);

            assertThat(result).isTrue();
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any(Card.class), any());
        }

        @Test
        @DisplayName("Stolen creature goes to original owner's graveyard")
        void stolenCreatureGoesToOriginalOwnersGraveyard() {
            Permanent stolen = addPermanent(player1, new GrizzlyBears());
            gd.stolenCreatures.put(stolen.getId(), player2.getId());
            stubGraveyardForCreature(stolen, player2.getId());

            prs.removePermanentToGraveyard(gd, stolen);

            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player2.getId()), eq(stolen.getOriginalCard()), eq(Zone.BATTLEFIELD));
            verify(graveyardService, never()).addCardToGraveyard(eq(gd), eq(player1.getId()), any(Card.class), any());
        }

        @Test
        @DisplayName("Cleans up stolenCreatures entry after removal")
        void cleansUpStolenCreaturesEntry() {
            Permanent stolen = addPermanent(player1, new GrizzlyBears());
            gd.stolenCreatures.put(stolen.getId(), player2.getId());
            stubGraveyardForCreature(stolen, player2.getId());

            prs.removePermanentToGraveyard(gd, stolen);

            assertThat(gd.stolenCreatures).doesNotContainKey(stolen.getId());
        }

        @Test
        @DisplayName("Creature death increments death count this turn")
        void creatureDeathIncreasesDeathCount() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            stubGraveyardForCreature(bears, player1.getId());
            int deathsBefore = gd.creatureDeathCountThisTurn.getOrDefault(player1.getId(), 0);

            prs.removePermanentToGraveyard(gd, bears);

            int deathsAfter = gd.creatureDeathCountThisTurn.getOrDefault(player1.getId(), 0);
            assertThat(deathsAfter).isEqualTo(deathsBefore + 1);
        }

        @Test
        @DisplayName("Non-creature permanent death does not increment creature death count")
        void nonCreatureDoesNotIncrementDeathCount() {
            Permanent artifact = addPermanent(player1, new Spellbook());
            when(gameQueryService.isCreature(gd, artifact)).thenReturn(false);
            when(gameQueryService.isArtifact(artifact)).thenReturn(true);
            when(graveyardService.addCardToGraveyard(eq(gd), eq(player1.getId()), any(Card.class), eq(Zone.BATTLEFIELD))).thenReturn(true);
            int deathsBefore = gd.creatureDeathCountThisTurn.getOrDefault(player1.getId(), 0);

            prs.removePermanentToGraveyard(gd, artifact);

            int deathsAfter = gd.creatureDeathCountThisTurn.getOrDefault(player1.getId(), 0);
            assertThat(deathsAfter).isEqualTo(deathsBefore);
        }

        @Test
        @DisplayName("Fires death triggers for creatures sent to graveyard")
        void firesDeathTriggersForCreature() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            stubGraveyardForCreature(bears, player1.getId());

            prs.removePermanentToGraveyard(gd, bears);

            verify(deathTriggerService).collectDeathTrigger(eq(gd), eq(bears.getCard()), eq(player1.getId()), eq(true), eq(bears));
            verify(deathTriggerService).checkAllyCreatureDeathTriggers(gd, player1.getId());
            verify(deathTriggerService).checkOpponentCreatureDeathTriggers(gd, player1.getId());
            verify(deathTriggerService).checkEquippedCreatureDeathTriggers(gd, bears.getId(), player1.getId());
        }

        @Test
        @DisplayName("Fires artifact graveyard trigger for artifacts sent to graveyard")
        void firesArtifactGraveyardTrigger() {
            Permanent artifact = addPermanent(player1, new Spellbook());
            when(gameQueryService.isCreature(gd, artifact)).thenReturn(false);
            when(gameQueryService.isArtifact(artifact)).thenReturn(true);
            when(graveyardService.addCardToGraveyard(eq(gd), eq(player1.getId()), any(Card.class), eq(Zone.BATTLEFIELD))).thenReturn(true);

            prs.removePermanentToGraveyard(gd, artifact);

            verify(deathTriggerService).checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gd, player1.getId(), player1.getId());
        }

        @Test
        @DisplayName("Exiled card returns to battlefield when source permanent leaves")
        void exileReturnOnLeave() {
            Permanent source = addPermanent(player1, new SerraAngel());
            stubGraveyardForCreature(source, player1.getId());

            Card exiledCard = new GrizzlyBears();
            gd.playerExiledCards.get(player2.getId()).add(exiledCard);
            gd.exileReturnOnPermanentLeave.put(source.getId(), new PendingExileReturn(exiledCard, player2.getId()));

            prs.removePermanentToGraveyard(gd, source);

            assertThat(gd.playerExiledCards.get(player2.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.exileReturnOnPermanentLeave).doesNotContainKey(source.getId());
            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player2.getId()), any(Permanent.class));
            verify(battlefieldEntryService).handleCreatureEnteredBattlefield(eq(gd), eq(player2.getId()), eq(exiledCard), isNull(), eq(false));
        }

        @Test
        @DisplayName("Equipment with SacrificeOnUnattachEffect sacrifices attached creature when removed")
        void sacrificeOnUnattachWhenEquipmentRemoved() {
            Permanent creature = addPermanent(player1, new GrizzlyBears());
            Permanent equipment = addPermanent(player1, new GraftedExoskeleton());
            equipment.setAttachedTo(creature.getId());

            when(gameQueryService.isCreature(gd, equipment)).thenReturn(false);
            when(gameQueryService.isArtifact(equipment)).thenReturn(true);
            when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
            when(gameQueryService.isArtifact(creature)).thenReturn(false);
            when(graveyardService.addCardToGraveyard(eq(gd), eq(player1.getId()), any(Card.class), eq(Zone.BATTLEFIELD))).thenReturn(true);
            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);

            prs.removePermanentToGraveyard(gd, equipment);

            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(equipment);
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player1.getId()), eq(equipment.getOriginalCard()), eq(Zone.BATTLEFIELD));
            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player1.getId()), eq(creature.getOriginalCard()), eq(Zone.BATTLEFIELD));
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
            Permanent bears = addPermanent(player1, new GrizzlyBears());

            boolean result = prs.removePermanentToHand(gd, bears);

            assertThat(result).isTrue();
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Returns false when permanent is not on any battlefield")
        void returnsFalseWhenNotOnBattlefield() {
            Permanent bears = new Permanent(new GrizzlyBears());

            boolean result = prs.removePermanentToHand(gd, bears);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Permanent with exileIfLeavesBattlefield is exiled instead of returning to hand")
        void exileReplacementExileIfLeaves() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            bears.setExileIfLeavesBattlefield(true);

            boolean result = prs.removePermanentToHand(gd, bears);

            assertThat(result).isTrue();
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
            assertThat(gd.playerHands.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            verify(gameBroadcastService).logAndBroadcast(eq(gd), contains("exiled instead of returning to hand"));
        }

        @Test
        @DisplayName("exileInsteadOfDieThisTurn does NOT redirect bounce to exile")
        void exileInsteadOfDieDoesNotAffectBounce() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            bears.setExileInsteadOfDieThisTurn(true);

            prs.removePermanentToHand(gd, bears);

            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Stolen creature returns to original owner's hand")
        void stolenCreatureReturnsToOriginalOwner() {
            Permanent stolen = addPermanent(player1, new GrizzlyBears());
            gd.stolenCreatures.put(stolen.getId(), player2.getId());

            prs.removePermanentToHand(gd, stolen);

            assertThat(gd.playerHands.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerHands.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.stolenCreatures).doesNotContainKey(stolen.getId());
        }

        @Test
        @DisplayName("Exiled card returns to battlefield when source permanent is bounced")
        void exileReturnOnLeave() {
            Permanent source = addPermanent(player1, new SerraAngel());

            Card exiledCard = new GrizzlyBears();
            gd.playerExiledCards.get(player2.getId()).add(exiledCard);
            gd.exileReturnOnPermanentLeave.put(source.getId(), new PendingExileReturn(exiledCard, player2.getId()));

            prs.removePermanentToHand(gd, source);

            assertThat(gd.playerExiledCards.get(player2.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.exileReturnOnPermanentLeave).doesNotContainKey(source.getId());
            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player2.getId()), any(Permanent.class));
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
            Permanent bears = addPermanent(player1, new GrizzlyBears());

            boolean result = prs.removePermanentToExile(gd, bears);

            assertThat(result).isTrue();
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Returns false when permanent is not on any battlefield")
        void returnsFalseWhenNotOnBattlefield() {
            Permanent bears = new Permanent(new GrizzlyBears());

            boolean result = prs.removePermanentToExile(gd, bears);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Stolen creature goes to original owner's exile zone")
        void stolenCreatureGoesToOriginalOwnersExile() {
            Permanent stolen = addPermanent(player1, new GrizzlyBears());
            gd.stolenCreatures.put(stolen.getId(), player2.getId());

            prs.removePermanentToExile(gd, stolen);

            assertThat(gd.playerExiledCards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.stolenCreatures).doesNotContainKey(stolen.getId());
        }

        @Test
        @DisplayName("Equipment with SacrificeOnUnattachEffect sacrifices creature when exiled")
        void sacrificeOnUnattachWhenEquipmentExiled() {
            Permanent creature = addPermanent(player1, new GrizzlyBears());
            Permanent equipment = addPermanent(player1, new GraftedExoskeleton());
            equipment.setAttachedTo(creature.getId());

            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
            when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
            when(gameQueryService.isArtifact(creature)).thenReturn(false);
            when(graveyardService.addCardToGraveyard(eq(gd), eq(player1.getId()), any(Card.class), eq(Zone.BATTLEFIELD))).thenReturn(true);

            prs.removePermanentToExile(gd, equipment);

            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grafted Exoskeleton"));
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player1.getId()), eq(creature.getOriginalCard()), eq(Zone.BATTLEFIELD));
        }

        @Test
        @DisplayName("Exiled card returns to battlefield when source permanent is exiled")
        void exileReturnOnLeave() {
            Permanent source = addPermanent(player1, new SerraAngel());

            Card exiledCard = new GrizzlyBears();
            gd.playerExiledCards.get(player2.getId()).add(exiledCard);
            gd.exileReturnOnPermanentLeave.put(source.getId(), new PendingExileReturn(exiledCard, player2.getId()));

            prs.removePermanentToExile(gd, source);

            assertThat(gd.playerExiledCards.get(player2.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.exileReturnOnPermanentLeave).doesNotContainKey(source.getId());
            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player2.getId()), any(Permanent.class));
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
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);
            stubGraveyardForCreature(bears, player1.getId());

            boolean result = prs.tryDestroyPermanent(gd, bears);

            assertThat(result).isTrue();
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
            verify(graveyardService).addCardToGraveyard(eq(gd), eq(player1.getId()), eq(bears.getOriginalCard()), eq(Zone.BATTLEFIELD));
            verify(auraAttachmentService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Indestructible permanent is not destroyed")
        void indestructibleSurvives() {
            Permanent golem = addPermanent(player1, indestructibleCreature());
            when(gameQueryService.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).thenReturn(true);

            boolean result = prs.tryDestroyPermanent(gd, golem);

            assertThat(result).isFalse();
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(golem);
            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any(Card.class), any());
        }

        @Test
        @DisplayName("Indestructible is logged when destruction is prevented")
        void indestructibleIsLogged() {
            Permanent golem = addPermanent(player1, indestructibleCreature());
            when(gameQueryService.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).thenReturn(true);

            prs.tryDestroyPermanent(gd, golem);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Indestructible Golem") && msg.contains("indestructible")));
        }

        @Test
        @DisplayName("Permanent with regeneration shield survives destruction")
        void regenerationPreventsDestruction() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, bears)).thenReturn(true);

            boolean result = prs.tryDestroyPermanent(gd, bears);

            assertThat(result).isFalse();
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        }

        @Test
        @DisplayName("cannotBeRegenerated flag bypasses regeneration")
        void cannotBeRegeneratedBypassesRegeneration() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            stubGraveyardForCreature(bears, player1.getId());

            boolean result = prs.tryDestroyPermanent(gd, bears, true);

            assertThat(result).isTrue();
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
            verify(graveyardService, never()).tryRegenerate(any(), any());
        }

        @Test
        @DisplayName("cannotBeRegenerated still respects indestructible")
        void cannotBeRegeneratedStillRespectsIndestructible() {
            Permanent golem = addPermanent(player1, indestructibleCreature());
            when(gameQueryService.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).thenReturn(true);

            boolean result = prs.tryDestroyPermanent(gd, golem, true);

            assertThat(result).isFalse();
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(golem);
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
            Card bears = new GrizzlyBears();
            gd.playerGraveyards.get(player1.getId()).add(bears);

            prs.removeCardFromGraveyardById(gd, bears.getId());

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Cleans up creature death tracking set")
        void cleansUpDeathTracking() {
            Card bears = new GrizzlyBears();
            gd.playerGraveyards.get(player1.getId()).add(bears);
            gd.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn
                    .computeIfAbsent(player1.getId(), k -> new HashSet<>())
                    .add(bears.getId());

            prs.removeCardFromGraveyardById(gd, bears.getId());

            assertThat(gd.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.get(player1.getId()))
                    .doesNotContain(bears.getId());
        }

        @Test
        @DisplayName("No-op when card is not in any graveyard")
        void noOpWhenNotInGraveyard() {
            UUID fakeId = UUID.randomUUID();
            int sizeP1 = gd.playerGraveyards.get(player1.getId()).size();
            int sizeP2 = gd.playerGraveyards.get(player2.getId()).size();

            prs.removeCardFromGraveyardById(gd, fakeId);

            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(sizeP1);
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(sizeP2);
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
            when(gameQueryService.findEnchantedCreatureByAuraEffect(eq(gd), eq(player1.getId()), eq(RedirectPlayerDamageToEnchantedCreatureEffect.class)))
                    .thenReturn(null);

            int result = prs.redirectPlayerDamageToEnchantedCreature(gd, player1.getId(), 5, "Lightning Bolt");

            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("Returns zero damage when redirect is <= 0")
        void returnsZeroOrNegativeDamageAsIs() {
            int result = prs.redirectPlayerDamageToEnchantedCreature(gd, player1.getId(), 0, "Source");

            assertThat(result).isEqualTo(0);
            verify(gameQueryService, never()).findEnchantedCreatureByAuraEffect(any(), any(), any());
        }

        @Test
        @DisplayName("Redirects damage to enchanted creature and returns 0")
        void redirectsDamageToEnchantedCreature() {
            Permanent creature = addPermanent(player1, new SerraAngel());
            when(gameQueryService.findEnchantedCreatureByAuraEffect(eq(gd), eq(player1.getId()), eq(RedirectPlayerDamageToEnchantedCreatureEffect.class)))
                    .thenReturn(creature);
            when(damagePreventionService.applyCreaturePreventionShield(gd, creature, 3)).thenReturn(3);
            when(gameQueryService.getEffectiveToughness(gd, creature)).thenReturn(5);

            int result = prs.redirectPlayerDamageToEnchantedCreature(gd, player1.getId(), 3, "Lightning Bolt");

            assertThat(result).isEqualTo(0);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Serra Angel") && msg.contains("absorbs") && msg.contains("redirected")));
        }

        @Test
        @DisplayName("Enchanted creature is destroyed when redirected damage meets toughness")
        void enchantedCreatureDestroyedByLethalDamage() {
            Permanent creature = addPermanent(player1, new SerraAngel());
            when(gameQueryService.findEnchantedCreatureByAuraEffect(eq(gd), eq(player1.getId()), eq(RedirectPlayerDamageToEnchantedCreatureEffect.class)))
                    .thenReturn(creature);
            when(damagePreventionService.applyCreaturePreventionShield(gd, creature, 4)).thenReturn(4);
            when(gameQueryService.getEffectiveToughness(gd, creature)).thenReturn(4);
            // tryDestroyPermanent stubs
            when(gameQueryService.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, creature)).thenReturn(false);
            stubGraveyardForCreature(creature, player1.getId());

            prs.redirectPlayerDamageToEnchantedCreature(gd, player1.getId(), 4, "Fireball");

            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Serra Angel") && msg.contains("destroyed")));
        }

        @Test
        @DisplayName("Indestructible enchanted creature survives lethal redirected damage")
        void indestructibleSurvivesLethalRedirect() {
            Permanent creature = addPermanent(player1, indestructibleCreature());
            when(gameQueryService.findEnchantedCreatureByAuraEffect(eq(gd), eq(player1.getId()), eq(RedirectPlayerDamageToEnchantedCreatureEffect.class)))
                    .thenReturn(creature);
            when(damagePreventionService.applyCreaturePreventionShield(gd, creature, 5)).thenReturn(5);
            when(gameQueryService.getEffectiveToughness(gd, creature)).thenReturn(2);
            when(gameQueryService.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).thenReturn(true);

            int result = prs.redirectPlayerDamageToEnchantedCreature(gd, player1.getId(), 5, "Fireball");

            assertThat(result).isEqualTo(0);
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("indestructible")));
        }
    }
}
