package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AttachTargetToSourcePermanentEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.AttachTargetToSourcePermanentEffectHandler;
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
import static org.mockito.Mockito.when;

/**
 * CR 613.7 timestamp infrastructure: permanents are stamped when they enter a battlefield,
 * re-stamped when they become attached (CR 613.7e), NOT re-stamped on control changes
 * (CR 613.7c), and the counter/stamps survive the AI simulation deep copy.
 */
@ExtendWith(MockitoExtension.class)
class PermanentTimestampTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private com.github.laxika.magicalvibes.service.input.PlayerInputService playerInputService;
    @Mock private PermanentCopierService permanentCopierService;
    @Mock private com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService triggerCollectionService;
    @Mock private GraveyardTargetingService graveyardTargetingService;
    @Mock private ETBTokenTargetService etbTokenTargetService;
    @Mock private com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectResolver etbEffectResolver;
    @Mock private com.github.laxika.magicalvibes.service.effect.AmountEvaluationService amountEvaluationService;
    @Mock private com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService conditionEvaluationService;
    @Mock private com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService predicateEvaluationService;

    @InjectMocks private BattlefieldEntryService battlefieldEntryService;

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

    @Nested
    @DisplayName("Battlefield entry stamping")
    class EntryStamping {

        @Test
        @DisplayName("A permanent is stamped with a fresh timestamp as it enters the battlefield")
        void entryStampsTimestamp() {
            Permanent bears = createCreature("Grizzly Bears");

            battlefieldEntryService.putPermanentOntoBattlefield(gd, player1Id, bears);

            assertThat(bears.getTimestamp()).isPositive();
            assertThat(bears.getTimestamp()).isEqualTo(gd.timestampCounter);
        }

        @Test
        @DisplayName("Later entries get strictly greater timestamps, across battlefields")
        void laterEntriesGetGreaterTimestamps() {
            Permanent first = createCreature("Grizzly Bears");
            Permanent second = createCreature("Coral Merfolk");
            Permanent third = createCreature("Raging Goblin");

            battlefieldEntryService.putPermanentOntoBattlefield(gd, player1Id, first);
            battlefieldEntryService.putPermanentOntoBattlefield(gd, player2Id, second);
            battlefieldEntryService.putPermanentOntoBattlefield(gd, player1Id, third);

            assertThat(first.getTimestamp()).isLessThan(second.getTimestamp());
            assertThat(second.getTimestamp()).isLessThan(third.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Re-stamp on attach (CR 613.7e)")
    class AttachRestamp {

        @Test
        @DisplayName("An attachment gets a fresh timestamp when it becomes attached")
        void attachRestampsTimestamp() {
            AttachTargetToSourcePermanentEffectHandler handler =
                    new AttachTargetToSourcePermanentEffectHandler(gameQueryService, gameBroadcastService);
            Permanent equipment = createEquipment("Darksteel Axe");
            Permanent creature = createCreature("Grizzly Bears");
            battlefieldEntryService.putPermanentOntoBattlefield(gd, player1Id, equipment);
            battlefieldEntryService.putPermanentOntoBattlefield(gd, player1Id, creature);
            long stampBeforeAttach = equipment.getTimestamp();

            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, creature.getCard(),
                    player1Id, "attach", List.of(new AttachTargetToSourcePermanentEffect()),
                    equipment.getId(), creature.getId());
            when(gameQueryService.findPermanentById(gd, equipment.getId())).thenReturn(equipment);
            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
            handler.resolve(gd, entry, new AttachTargetToSourcePermanentEffect());

            assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
            assertThat(equipment.getTimestamp()).isGreaterThan(stampBeforeAttach);
            assertThat(equipment.getTimestamp()).isGreaterThan(creature.getTimestamp());
            assertThat(equipment.getTimestamp()).isEqualTo(gd.timestampCounter);
        }
    }

    @Nested
    @DisplayName("Control changes do not re-stamp (CR 613.7c)")
    class ControlChangeKeepsTimestamp {

        @Test
        @DisplayName("Stealing a permanent moves it between battlefields without changing its timestamp")
        void stealKeepsTimestamp() {
            CreatureControlService controlService =
                    new CreatureControlService(gameBroadcastService, gameQueryService);
            Permanent bears = createCreature("Grizzly Bears");
            battlefieldEntryService.putPermanentOntoBattlefield(gd, player2Id, bears);
            long entryStamp = bears.getTimestamp();

            when(gameQueryService.findPermanentController(gd, bears.getId())).thenReturn(player2Id);
            controlService.stealPermanent(gd, player1Id, bears);

            assertThat(gd.playerBattlefields.get(player1Id)).contains(bears);
            assertThat(bears.getTimestamp()).isEqualTo(entryStamp);
        }
    }

    @Nested
    @DisplayName("Battlefield-list stamping (GameData.newBattlefieldList)")
    class BattlefieldListStamping {

        @Test
        @DisplayName("Direct insertions are stamped in insertion order, across battlefields")
        void directInsertionsAreStampedInOrder() {
            List<Permanent> p1Battlefield = gd.newBattlefieldList();
            List<Permanent> p2Battlefield = gd.newBattlefieldList();
            Permanent first = createCreature("Grizzly Bears");
            Permanent second = createCreature("Coral Merfolk");
            Permanent third = createCreature("Raging Goblin");

            p1Battlefield.add(first);
            p2Battlefield.add(second);
            p1Battlefield.add(third);

            assertThat(first.getTimestamp()).isPositive();
            assertThat(first.getTimestamp()).isLessThan(second.getTimestamp());
            assertThat(second.getTimestamp()).isLessThan(third.getTimestamp());
        }

        @Test
        @DisplayName("Re-inserting an already-stamped permanent keeps its stamp (CR 613.7c move)")
        void reinsertionKeepsStamp() {
            List<Permanent> p1Battlefield = gd.newBattlefieldList();
            List<Permanent> p2Battlefield = gd.newBattlefieldList();
            Permanent bears = createCreature("Grizzly Bears");
            p1Battlefield.add(bears);
            long stamp = bears.getTimestamp();

            p1Battlefield.remove(bears);
            p2Battlefield.add(bears);

            assertThat(bears.getTimestamp()).isEqualTo(stamp);
        }
    }

    @Nested
    @DisplayName("Copy semantics")
    class CopySemantics {

        @Test
        @DisplayName("The Permanent copy constructor preserves the timestamp")
        void permanentCopyPreservesTimestamp() {
            Permanent bears = createCreature("Grizzly Bears");
            bears.setTimestamp(42L);

            Permanent copy = new Permanent(bears);

            assertThat(copy.getTimestamp()).isEqualTo(42L);
        }

        @Test
        @DisplayName("GameData.simulationCopy preserves the timestamp counter and permanent stamps")
        void simulationCopyPreservesCounter() {
            Permanent bears = createCreature("Grizzly Bears");
            battlefieldEntryService.putPermanentOntoBattlefield(gd, player1Id, bears);
            long counter = gd.timestampCounter;

            GameData copy = gd.simulationCopy();

            assertThat(copy.timestampCounter).isEqualTo(counter);
            assertThat(copy.playerBattlefields.get(player1Id).getFirst().getTimestamp())
                    .isEqualTo(bears.getTimestamp());
            // The copy's next timestamp continues after the original's, not from zero.
            assertThat(copy.nextTimestamp()).isEqualTo(counter + 1);
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

    private Permanent createEquipment(String name) {
        Card card = createCard(name);
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        return new Permanent(card);
    }
}
