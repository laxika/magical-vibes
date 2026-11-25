package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GainControlOfTargetEffectHandlerTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameLogService gameLogService;
    @Mock private CreatureControlService creatureControlService;
    @Mock private SubjectResolutionService subjectResolutionService;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private GainControlOfTargetEffectHandler handler;

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
        handler = new GainControlOfTargetEffectHandler(gameQueryService, gameLogService, creatureControlService,
                subjectResolutionService);
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

    private StackEntry entryWithTargetAndSource(Card card, UUID controllerId, List<CardEffect> effects,
                                                UUID targetId, UUID sourcePermanentId) {
        return new StackEntry(StackEntryType.ACTIVATED_ABILITY, card, controllerId,
                card.getName(), effects, targetId, sourcePermanentId);
    }

    // =========================================================================
    // PERMANENT duration
    // =========================================================================

    @Nested
    class PermanentDuration {

        // ----- Single-target (legacy path via getTargetId()) -----

        @Test
        @DisplayName("Creates a permanent-duration control effect for a single targetId")
        void stealsSingleTarget() {
            Card card = createCard("Entrancing Melody");
            Permanent target = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(target);

            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.PERMANENT);
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);

            handler.resolve(gd, entry, effect);

            verify(creatureControlService).applyControlEffect(gd, player1Id, target,
                    effect, Duration.PERMANENT, null, "Entrancing Melody");
        }

        @Test
        @DisplayName("Does nothing when single target is not found")
        void doesNothingWhenSingleTargetNotFound() {
            Card card = createCard("Entrancing Melody");
            UUID missingId = UUID.randomUUID();
            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.PERMANENT);
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), missingId);

            when(gameQueryService.findPermanentById(gd, missingId)).thenReturn(null);

            handler.resolve(gd, entry, effect);

            verify(creatureControlService, never()).applyControlEffect(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Grants subtype when grantedSubtype is specified")
        void grantsSubtype() {
            Card card = createCard("Captivating Vampire");
            Permanent target = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(target);

            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.PERMANENT, CardSubtype.VAMPIRE);
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);

            when(creatureControlService.applyControlEffect(eq(gd), eq(player1Id), any(Permanent.class),
                    eq(effect), eq(Duration.PERMANENT), eq(null), eq("Captivating Vampire")))
                    .thenReturn(true);
            handler.resolve(gd, entry, effect);

            verify(creatureControlService).applyControlEffect(gd, player1Id, target,
                    effect, Duration.PERMANENT, null, "Captivating Vampire");
            assertThat(PermanentGrants.subtypesOf(gd, target)).contains(CardSubtype.VAMPIRE);
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Grizzly Bears becomes a Vampire in addition to its other types.")));
        }

        @Test
        @DisplayName("Does not duplicate granted subtype if already present")
        void doesNotDuplicateGrantedSubtype() {
            Card card = createCard("Captivating Vampire");
            Permanent target = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(target);
            PermanentGrants.subtype(gd, target, CardSubtype.VAMPIRE);

            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.PERMANENT, CardSubtype.VAMPIRE);
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);

            handler.resolve(gd, entry, effect);

            assertThat(PermanentGrants.subtypesOf(gd, target).stream()
                    .filter(s -> s == CardSubtype.VAMPIRE).count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Takes only the targets of its own target group, not another group's permanent")
        void takesOnlyItsOwnGroupsTargets() {
            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.PERMANENT);
            var marker = new com.github.laxika.magicalvibes.model.effect.TargetGroupMarkerEffect();
            Card card = new Card() {
                {
                    setName("Two Group Spell");
                    target(com.github.laxika.magicalvibes.model.filter.TargetFilters.permanent())
                            .addSpellEffect(marker);
                    target(com.github.laxika.magicalvibes.model.filter.TargetFilters.permanent())
                            .addSpellEffect(effect);
                }
            };
            Permanent otherGroupTarget = createCreature("Hill Giant");
            Permanent stolen = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(otherGroupTarget);
            gd.playerBattlefields.get(player2Id).add(stolen);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card, player1Id, card.getName(),
                    List.of(marker, effect), (UUID) null, List.of(otherGroupTarget.getId(), stolen.getId()));

            org.mockito.Mockito.lenient().when(gameQueryService.findPermanentById(gd, otherGroupTarget.getId()))
                    .thenReturn(otherGroupTarget);
            when(gameQueryService.findPermanentById(gd, stolen.getId())).thenReturn(stolen);

            handler.resolve(gd, entry, effect);

            verify(creatureControlService).applyControlEffect(gd, player1Id, stolen,
                    effect, Duration.PERMANENT, null, "Two Group Spell");
            verify(creatureControlService, never()).applyControlEffect(eq(gd), eq(player1Id), eq(otherGroupTarget),
                    any(), any(), any(), any());
        }

        // ----- Multi-target (via getTargetIds()) -----

        @Test
        @DisplayName("Creates a control effect per target via targetIds")
        void stealsMultipleTargets() {
            Card card = createCard("Jace, Ingenious Mind-Mage");
            Permanent target1 = createCreature("Grizzly Bears");
            Permanent target2 = createCreature("Hill Giant");
            Permanent target3 = createCreature("Air Elemental");
            gd.playerBattlefields.get(player2Id).add(target1);
            gd.playerBattlefields.get(player2Id).add(target2);
            gd.playerBattlefields.get(player2Id).add(target3);

            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.PERMANENT);
            List<UUID> targetIds = List.of(target1.getId(), target2.getId(), target3.getId());
            StackEntry entry = entryWithMultiTargets(card, player1Id, List.of(effect), targetIds);

            when(gameQueryService.findPermanentById(gd, target1.getId())).thenReturn(target1);
            when(gameQueryService.findPermanentById(gd, target2.getId())).thenReturn(target2);
            when(gameQueryService.findPermanentById(gd, target3.getId())).thenReturn(target3);

            handler.resolve(gd, entry, effect);

            verify(creatureControlService).applyControlEffect(gd, player1Id, target1,
                    effect, Duration.PERMANENT, null, "Jace, Ingenious Mind-Mage");
            verify(creatureControlService).applyControlEffect(gd, player1Id, target2,
                    effect, Duration.PERMANENT, null, "Jace, Ingenious Mind-Mage");
            verify(creatureControlService).applyControlEffect(gd, player1Id, target3,
                    effect, Duration.PERMANENT, null, "Jace, Ingenious Mind-Mage");
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

            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.PERMANENT);
            List<UUID> targetIds = List.of(target1.getId(), removedId, target2.getId());
            StackEntry entry = entryWithMultiTargets(card, player1Id, List.of(effect), targetIds);

            when(gameQueryService.findPermanentById(gd, target1.getId())).thenReturn(target1);
            when(gameQueryService.findPermanentById(gd, removedId)).thenReturn(null);
            when(gameQueryService.findPermanentById(gd, target2.getId())).thenReturn(target2);

            handler.resolve(gd, entry, effect);

            verify(creatureControlService, times(2)).applyControlEffect(any(), any(), any(), any(), any(), any(), any());
            verify(creatureControlService).applyControlEffect(gd, player1Id, target1,
                    effect, Duration.PERMANENT, null, "Jace, Ingenious Mind-Mage");
            verify(creatureControlService).applyControlEffect(gd, player1Id, target2,
                    effect, Duration.PERMANENT, null, "Jace, Ingenious Mind-Mage");
        }

        @Test
        @DisplayName("Does nothing when targetIds list is empty and targetId is null")
        void doesNothingWithNoTargets() {
            Card card = createCard("Jace, Ingenious Mind-Mage");
            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.PERMANENT);
            StackEntry entry = entryWithMultiTargets(card, player1Id, List.of(effect), List.of());

            handler.resolve(gd, entry, effect);

            verify(creatureControlService, never()).applyControlEffect(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Grants subtype to each target in multi-target mode")
        void grantsSubtypeToAllMultiTargets() {
            Card card = createCard("Captivating Vampire");
            Permanent target1 = createCreature("Grizzly Bears");
            Permanent target2 = createCreature("Hill Giant");
            gd.playerBattlefields.get(player2Id).add(target1);
            gd.playerBattlefields.get(player2Id).add(target2);

            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.PERMANENT, CardSubtype.VAMPIRE);
            List<UUID> targetIds = List.of(target1.getId(), target2.getId());
            StackEntry entry = entryWithMultiTargets(card, player1Id, List.of(effect), targetIds);

            when(gameQueryService.findPermanentById(gd, target1.getId())).thenReturn(target1);
            when(gameQueryService.findPermanentById(gd, target2.getId())).thenReturn(target2);

            when(creatureControlService.applyControlEffect(eq(gd), eq(player1Id), any(Permanent.class),
                    eq(effect), eq(Duration.PERMANENT), eq(null), eq("Captivating Vampire")))
                    .thenReturn(true);
            handler.resolve(gd, entry, effect);

            assertThat(PermanentGrants.subtypesOf(gd, target1)).contains(CardSubtype.VAMPIRE);
            assertThat(PermanentGrants.subtypesOf(gd, target2)).contains(CardSubtype.VAMPIRE);
            verify(gameLogService, times(2)).append(eq(gd), any(GameLogEntry.class));
        }
    }

    // =========================================================================
    // END_OF_TURN duration
    // =========================================================================

    @Nested
    class EndOfTurn {

        @Test
        @DisplayName("Creates an until-end-of-turn control effect for the target")
        void stealsUntilEndOfTurn() {
            Card card = createCard("Threaten");
            Permanent target = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(target);

            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN);
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);

            handler.resolve(gd, entry, effect);

            verify(creatureControlService).applyControlEffect(gd, player1Id, target,
                    effect, Duration.UNTIL_END_OF_TURN, null, "Threaten");
        }

        @Test
        @DisplayName("Does nothing when the target is not found")
        void doesNothingWhenTargetNotFound() {
            Card card = createCard("Threaten");
            UUID missingId = UUID.randomUUID();
            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN);
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), missingId);

            when(gameQueryService.findPermanentById(gd, missingId)).thenReturn(null);

            handler.resolve(gd, entry, effect);

            verify(creatureControlService, never()).applyControlEffect(any(), any(), any(), any(), any(), any(), any());
        }
    }

    // =========================================================================
    // WHILE_SOURCE_ON_BATTLEFIELD duration
    // =========================================================================

    @Nested
    class WhileSource {

        @Test
        @DisplayName("Creates a while-source control effect keyed to the source permanent")
        void stealsWhileSourceControlled() {
            Card card = createCard("Olivia Voldaren");
            Permanent source = createCreature("Olivia Voldaren");
            Permanent target = createCreature("Bloodghast");
            gd.playerBattlefields.get(player1Id).add(source);
            gd.playerBattlefields.get(player2Id).add(target);

            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.WHILE_SOURCE_ON_BATTLEFIELD);
            StackEntry entry = entryWithTargetAndSource(card, player1Id, List.of(effect), target.getId(), source.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);
            when(gameQueryService.findPermanentController(gd, source.getId())).thenReturn(player1Id);

            handler.resolve(gd, entry, effect);

            verify(creatureControlService).applyControlEffect(gd, player1Id, target,
                    effect, Duration.WHILE_SOURCE_ON_BATTLEFIELD, source.getId(), "Olivia Voldaren");
        }

        @Test
        @DisplayName("Has no effect when the source has left the battlefield")
        void noEffectWhenSourceGone() {
            Card card = createCard("Olivia Voldaren");
            Permanent target = createCreature("Bloodghast");
            UUID sourceId = UUID.randomUUID();
            gd.playerBattlefields.get(player2Id).add(target);

            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.WHILE_SOURCE_ON_BATTLEFIELD);
            StackEntry entry = entryWithTargetAndSource(card, player1Id, List.of(effect), target.getId(), sourceId);

            when(gameQueryService.findPermanentById(gd, sourceId)).thenReturn(null);

            handler.resolve(gd, entry, effect);

            verify(creatureControlService, never()).applyControlEffect(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Has no effect when the caster no longer controls the source")
        void noEffectWhenSourceControlLost() {
            Card card = createCard("Olivia Voldaren");
            Permanent source = createCreature("Olivia Voldaren");
            Permanent target = createCreature("Bloodghast");
            gd.playerBattlefields.get(player2Id).add(source);
            gd.playerBattlefields.get(player2Id).add(target);

            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.WHILE_SOURCE_ON_BATTLEFIELD);
            StackEntry entry = entryWithTargetAndSource(card, player1Id, List.of(effect), target.getId(), source.getId());

            when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);
            when(gameQueryService.findPermanentController(gd, source.getId())).thenReturn(player2Id);

            handler.resolve(gd, entry, effect);

            verify(creatureControlService, never()).applyControlEffect(any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Who gains control, and the GAIN_CONTROL step")
    class GainingPlayerAndStep {

        @Test
        @DisplayName("Another player the effect names gains control until end of turn")
        void anotherPlayerGainsControl() {
            Card card = createCard("Domineering Will");
            Permanent target = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(target);
            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN)
                    .gainedBy(Subjects.targetPlayer(0));
            StackEntry entry = entryWithMultiTargets(card, player1Id, List.of(effect), List.of(target.getId()));
            when(subjectResolutionService.players(gd, entry, effect, Subjects.targetPlayer(0)))
                    .thenReturn(List.of(player2Id));
            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1Id, player2Id);
            when(creatureControlService.applyControlEffect(gd, player2Id, target, effect, Duration.UNTIL_END_OF_TURN,
                    null, "Domineering Will")).thenReturn(true);

            handler.resolve(gd, entry, effect);

            verify(creatureControlService).applyControlEffect(gd, player2Id, target, effect,
                    Duration.UNTIL_END_OF_TURN, null, "Domineering Will");
            StepOutcome outcome = entry.getResolutionResults().outcome(StepKind.GAIN_CONTROL);
            assertThat(outcome.objectIds()).containsExactly(target.getId());
            assertThat(outcome.playerIds()).containsExactly(player2Id);
        }

        @Test
        @DisplayName("Records only the permanents whose controller changed")
        void recordsOnlyChangedPermanents() {
            Card card = createCard("Steal");
            Permanent stolen = createCreature("Grizzly Bears");
            Permanent own = createCreature("Hill Giant");
            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.PERMANENT);
            StackEntry entry = entryWithMultiTargets(card, player1Id, List.of(effect),
                    List.of(stolen.getId(), own.getId()));
            when(gameQueryService.findPermanentById(gd, stolen.getId())).thenReturn(stolen);
            when(gameQueryService.findPermanentById(gd, own.getId())).thenReturn(own);
            when(gameQueryService.findPermanentController(gd, stolen.getId())).thenReturn(player2Id, player1Id);
            when(gameQueryService.findPermanentController(gd, own.getId())).thenReturn(player1Id, player1Id);
            when(creatureControlService.applyControlEffect(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(true);

            handler.resolve(gd, entry, effect);

            assertThat(entry.getResolutionResults().outcome(StepKind.GAIN_CONTROL).objectIds())
                    .containsExactly(stolen.getId());
        }

        @Test
        @DisplayName("Changes nothing and records nothing when the named player is gone")
        void noGainingPlayer() {
            Card card = createCard("Domineering Will");
            Permanent target = createCreature("Grizzly Bears");
            GainControlOfTargetEffect effect = new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN)
                    .gainedBy(Subjects.targetPlayer(0));
            StackEntry entry = entryWithMultiTargets(card, player1Id, List.of(effect), List.of(target.getId()));
            when(subjectResolutionService.players(gd, entry, effect, Subjects.targetPlayer(0))).thenReturn(List.of());

            handler.resolve(gd, entry, effect);

            verify(creatureControlService, never()).applyControlEffect(any(), any(), any(), any(), any(), any(), any());
            assertThat(entry.getResolutionResults().outcome(StepKind.GAIN_CONTROL).amount()).isZero();
        }
    }
}
