package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermanentControlResolutionServiceTest {

    @Mock private BattlefieldEntryService battlefieldEntryService;
    @Mock private LegendRuleService legendRuleService;
    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PlayerInputService playerInputService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private CreatureControlService creatureControlService;

    @InjectMocks private PermanentControlResolutionService service;

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

    // ===== Helper methods =====

    private Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private Permanent createCreature(String name) {
        Card card = createCard(name);
        return new Permanent(card);
    }

    private StackEntry entryWithTarget(Card card, UUID controllerId, List<CardEffect> effects, UUID targetId) {
        return new StackEntry(StackEntryType.ACTIVATED_ABILITY, card, controllerId,
                card.getName(), effects, 0, targetId, null);
    }

    private StackEntry entryWithMultiTargets(Card card, UUID controllerId, List<CardEffect> effects, List<UUID> targetIds) {
        return new StackEntry(StackEntryType.ACTIVATED_ABILITY, card, controllerId,
                card.getName(), effects, null, targetIds);
    }

    // =========================================================================
    // GainControlOfTargetPermanentEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveGainControlOfTargetPermanent")
    class ResolveGainControlOfTargetPermanent {

        // ----- Single-target (legacy path via getTargetId()) -----

        @Test
        @DisplayName("Steals target permanent via single targetId")
        void stealsSingleTarget() {
            Card card = createCard("Entrancing Melody");
            Permanent target = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(target);

            GainControlOfTargetPermanentEffect effect = new GainControlOfTargetPermanentEffect();
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);

            service.resolveGainControlOfTargetPermanent(gd, entry, effect);

            verify(creatureControlService).stealPermanent(gd, player1Id, target);
            assertThat(gd.permanentControlStolenCreatures).contains(target.getId());
        }

        @Test
        @DisplayName("Does nothing when single target is not found")
        void doesNothingWhenSingleTargetNotFound() {
            Card card = createCard("Entrancing Melody");
            UUID missingId = UUID.randomUUID();
            GainControlOfTargetPermanentEffect effect = new GainControlOfTargetPermanentEffect();
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), missingId);

            when(gameQueryService.findPermanentById(gd, missingId)).thenReturn(null);

            service.resolveGainControlOfTargetPermanent(gd, entry, effect);

            verify(creatureControlService, never()).stealPermanent(any(), any(), any());
            assertThat(gd.permanentControlStolenCreatures).isEmpty();
        }

        @Test
        @DisplayName("Does not steal when target is already controlled by the controller")
        void doesNotStealOwnPermanent() {
            Card card = createCard("Entrancing Melody");
            Permanent target = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(target);

            GainControlOfTargetPermanentEffect effect = new GainControlOfTargetPermanentEffect();
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1Id);

            service.resolveGainControlOfTargetPermanent(gd, entry, effect);

            verify(creatureControlService, never()).stealPermanent(any(), any(), any());
            assertThat(gd.permanentControlStolenCreatures).isEmpty();
        }

        @Test
        @DisplayName("Grants subtype when grantedSubtype is specified")
        void grantsSubtype() {
            Card card = createCard("Captivating Vampire");
            Permanent target = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(target);

            GainControlOfTargetPermanentEffect effect = new GainControlOfTargetPermanentEffect(CardSubtype.VAMPIRE);
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);

            service.resolveGainControlOfTargetPermanent(gd, entry, effect);

            verify(creatureControlService).stealPermanent(gd, player1Id, target);
            assertThat(target.getGrantedSubtypes()).contains(CardSubtype.VAMPIRE);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Grizzly Bears becomes a Vampire in addition to its other types."));
        }

        @Test
        @DisplayName("Does not duplicate granted subtype if already present")
        void doesNotDuplicateGrantedSubtype() {
            Card card = createCard("Captivating Vampire");
            Permanent target = createCreature("Grizzly Bears");
            target.getGrantedSubtypes().add(CardSubtype.VAMPIRE);
            gd.playerBattlefields.get(player2Id).add(target);

            GainControlOfTargetPermanentEffect effect = new GainControlOfTargetPermanentEffect(CardSubtype.VAMPIRE);
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);

            service.resolveGainControlOfTargetPermanent(gd, entry, effect);

            assertThat(target.getGrantedSubtypes().stream()
                    .filter(s -> s == CardSubtype.VAMPIRE).count()).isEqualTo(1);
        }

        // ----- Multi-target (via getTargetIds()) -----

        @Test
        @DisplayName("Steals multiple targets via targetIds")
        void stealsMultipleTargets() {
            Card card = createCard("Jace, Ingenious Mind-Mage");
            Permanent target1 = createCreature("Grizzly Bears");
            Permanent target2 = createCreature("Hill Giant");
            Permanent target3 = createCreature("Air Elemental");
            gd.playerBattlefields.get(player2Id).add(target1);
            gd.playerBattlefields.get(player2Id).add(target2);
            gd.playerBattlefields.get(player2Id).add(target3);

            GainControlOfTargetPermanentEffect effect = new GainControlOfTargetPermanentEffect();
            List<UUID> targetIds = List.of(target1.getId(), target2.getId(), target3.getId());
            StackEntry entry = entryWithMultiTargets(card, player1Id, List.of(effect), targetIds);

            when(gameQueryService.findPermanentById(gd, target1.getId())).thenReturn(target1);
            when(gameQueryService.findPermanentById(gd, target2.getId())).thenReturn(target2);
            when(gameQueryService.findPermanentById(gd, target3.getId())).thenReturn(target3);
            when(gameQueryService.findPermanentController(gd, target1.getId())).thenReturn(player2Id);
            when(gameQueryService.findPermanentController(gd, target2.getId())).thenReturn(player2Id);
            when(gameQueryService.findPermanentController(gd, target3.getId())).thenReturn(player2Id);

            service.resolveGainControlOfTargetPermanent(gd, entry, effect);

            verify(creatureControlService).stealPermanent(gd, player1Id, target1);
            verify(creatureControlService).stealPermanent(gd, player1Id, target2);
            verify(creatureControlService).stealPermanent(gd, player1Id, target3);
            assertThat(gd.permanentControlStolenCreatures)
                    .containsExactlyInAnyOrder(target1.getId(), target2.getId(), target3.getId());
        }

        @Test
        @DisplayName("Skips targets that are no longer on the battlefield in multi-target")
        void skipsRemovedTargetsInMultiTarget() {
            Card card = createCard("Jace, Ingenious Mind-Mage");
            Permanent target1 = createCreature("Grizzly Bears");
            Permanent target2 = createCreature("Hill Giant");
            UUID removedId = UUID.randomUUID();
            gd.playerBattlefields.get(player2Id).add(target1);
            gd.playerBattlefields.get(player2Id).add(target2);

            GainControlOfTargetPermanentEffect effect = new GainControlOfTargetPermanentEffect();
            List<UUID> targetIds = List.of(target1.getId(), removedId, target2.getId());
            StackEntry entry = entryWithMultiTargets(card, player1Id, List.of(effect), targetIds);

            when(gameQueryService.findPermanentById(gd, target1.getId())).thenReturn(target1);
            when(gameQueryService.findPermanentById(gd, removedId)).thenReturn(null);
            when(gameQueryService.findPermanentById(gd, target2.getId())).thenReturn(target2);
            when(gameQueryService.findPermanentController(gd, target1.getId())).thenReturn(player2Id);
            when(gameQueryService.findPermanentController(gd, target2.getId())).thenReturn(player2Id);

            service.resolveGainControlOfTargetPermanent(gd, entry, effect);

            verify(creatureControlService).stealPermanent(gd, player1Id, target1);
            verify(creatureControlService).stealPermanent(gd, player1Id, target2);
            verify(creatureControlService, times(2)).stealPermanent(any(), any(), any());
            assertThat(gd.permanentControlStolenCreatures)
                    .containsExactlyInAnyOrder(target1.getId(), target2.getId());
        }

        @Test
        @DisplayName("Does nothing when targetIds list is empty and targetId is null")
        void doesNothingWithNoTargets() {
            Card card = createCard("Jace, Ingenious Mind-Mage");
            GainControlOfTargetPermanentEffect effect = new GainControlOfTargetPermanentEffect();
            StackEntry entry = entryWithMultiTargets(card, player1Id, List.of(effect), List.of());

            service.resolveGainControlOfTargetPermanent(gd, entry, effect);

            verify(creatureControlService, never()).stealPermanent(any(), any(), any());
            assertThat(gd.permanentControlStolenCreatures).isEmpty();
        }

        @Test
        @DisplayName("Grants subtype to each target in multi-target mode")
        void grantsSubtypeToAllMultiTargets() {
            Card card = createCard("Captivating Vampire");
            Permanent target1 = createCreature("Grizzly Bears");
            Permanent target2 = createCreature("Hill Giant");
            gd.playerBattlefields.get(player2Id).add(target1);
            gd.playerBattlefields.get(player2Id).add(target2);

            GainControlOfTargetPermanentEffect effect = new GainControlOfTargetPermanentEffect(CardSubtype.VAMPIRE);
            List<UUID> targetIds = List.of(target1.getId(), target2.getId());
            StackEntry entry = entryWithMultiTargets(card, player1Id, List.of(effect), targetIds);

            when(gameQueryService.findPermanentById(gd, target1.getId())).thenReturn(target1);
            when(gameQueryService.findPermanentById(gd, target2.getId())).thenReturn(target2);
            when(gameQueryService.findPermanentController(gd, target1.getId())).thenReturn(player2Id);
            when(gameQueryService.findPermanentController(gd, target2.getId())).thenReturn(player2Id);

            service.resolveGainControlOfTargetPermanent(gd, entry, effect);

            assertThat(target1.getGrantedSubtypes()).contains(CardSubtype.VAMPIRE);
            assertThat(target2.getGrantedSubtypes()).contains(CardSubtype.VAMPIRE);
            verify(gameBroadcastService, times(2)).logAndBroadcast(eq(gd), any());
        }

        @Test
        @DisplayName("Multi-target skips targets already controlled by the controller")
        void multiTargetSkipsOwnPermanents() {
            Card card = createCard("Jace, Ingenious Mind-Mage");
            Permanent ownCreature = createCreature("Grizzly Bears");
            Permanent opponentCreature = createCreature("Hill Giant");
            gd.playerBattlefields.get(player1Id).add(ownCreature);
            gd.playerBattlefields.get(player2Id).add(opponentCreature);

            GainControlOfTargetPermanentEffect effect = new GainControlOfTargetPermanentEffect();
            List<UUID> targetIds = List.of(ownCreature.getId(), opponentCreature.getId());
            StackEntry entry = entryWithMultiTargets(card, player1Id, List.of(effect), targetIds);

            when(gameQueryService.findPermanentById(gd, ownCreature.getId())).thenReturn(ownCreature);
            when(gameQueryService.findPermanentById(gd, opponentCreature.getId())).thenReturn(opponentCreature);
            when(gameQueryService.findPermanentController(gd, ownCreature.getId())).thenReturn(player1Id);
            when(gameQueryService.findPermanentController(gd, opponentCreature.getId())).thenReturn(player2Id);

            service.resolveGainControlOfTargetPermanent(gd, entry, effect);

            verify(creatureControlService, times(1)).stealPermanent(gd, player1Id, opponentCreature);
            verify(creatureControlService, never()).stealPermanent(gd, player1Id, ownCreature);
            assertThat(gd.permanentControlStolenCreatures).containsExactly(opponentCreature.getId());
        }
    }
}
