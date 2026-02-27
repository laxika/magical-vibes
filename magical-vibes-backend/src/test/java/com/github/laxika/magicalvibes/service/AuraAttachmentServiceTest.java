package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Persuasion;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.t.Threaten;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuraAttachmentServiceTest extends BaseCardTest {

    // ===== removeOrphanedAuras — Aura behavior =====

    @Nested
    @DisplayName("removeOrphanedAuras - Aura behavior")
    class OrphanedAuras {

        @Test
        @DisplayName("Aura goes to graveyard when enchanted creature is destroyed")
        void auraGoesToGraveyardWhenCreatureDestroyed() {
            Permanent creature = addCreatureReady(player2);
            addAuraAttachedTo(player1, new HolyStrength(), creature);

            // Terror destroys the creature, which triggers removeOrphanedAuras
            harness.setHand(player1, List.of(new Terror()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.castAndResolveInstant(player1, 0, creature.getId());

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            harness.assertNotOnBattlefield(player1, "Holy Strength");
            harness.assertInGraveyard(player1, "Holy Strength");
        }

        @Test
        @DisplayName("Multiple auras on same creature all go to graveyard when creature dies")
        void multipleAurasRemovedWhenCreatureDies() {
            Permanent creature = addCreatureReady(player2);
            addAuraAttachedTo(player1, new HolyStrength(), creature);
            addAuraAttachedTo(player1, new HolyStrength(), creature);

            harness.setHand(player1, List.of(new Terror()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.castAndResolveInstant(player1, 0, creature.getId());

            harness.assertNotOnBattlefield(player1, "Holy Strength");
            assertThat(gd.playerGraveyards.get(player1.getId())
                    .stream().filter(c -> c.getName().equals("Holy Strength")).count())
                    .isEqualTo(2);
        }

        @Test
        @DisplayName("Aura from opponent goes to opponent's graveyard when creature dies from damage")
        void opponentAuraGoesToOpponentGraveyard() {
            // Player 2 has a creature, player 2 also has an aura on it
            Permanent creature = addCreatureReady(player2);
            addAuraAttachedTo(player2, new HolyStrength(), creature);

            // Shock deals 2 damage to the 2/2 creature, killing it.
            // Note: Holy Strength gives +1/+2, making it 3/4, so Shock won't kill it.
            // Use Terror instead which destroys it outright.
            harness.setHand(player1, List.of(new Terror()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.castAndResolveInstant(player1, 0, creature.getId());

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            harness.assertNotOnBattlefield(player2, "Holy Strength");
            harness.assertInGraveyard(player2, "Holy Strength");
        }

        @Test
        @DisplayName("Aura attached to a valid creature is not removed")
        void auraNotRemovedWhenCreatureStillExists() {
            Permanent creature = addCreatureReady(player1);
            Permanent aura = addAuraAttachedTo(player1, new HolyStrength(), creature);

            // Kill a different creature — aura on living creature should stay
            Permanent otherCreature = addCreatureReady(player2);
            harness.setHand(player1, List.of(new Terror()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.castAndResolveInstant(player1, 0, otherCreature.getId());

            harness.assertOnBattlefield(player1, "Holy Strength");
            assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        }

        @Test
        @DisplayName("Game log records aura going to graveyard when enchanted creature is destroyed")
        void gameLogRecordsAuraRemoval() {
            Permanent creature = addCreatureReady(player2);
            addAuraAttachedTo(player1, new HolyStrength(), creature);

            harness.setHand(player1, List.of(new Terror()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.clearMessages();
            harness.castAndResolveInstant(player1, 0, creature.getId());

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Holy Strength") && log.contains("graveyard"));
        }

        @Test
        @DisplayName("Player 1's aura on player 2's creature goes to player 1's graveyard")
        void crossPlayerAuraGoesToControllerGraveyard() {
            Permanent creature = addCreatureReady(player2);
            addAuraAttachedTo(player1, new HolyStrength(), creature);

            harness.setHand(player1, List.of(new Terror()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.castAndResolveInstant(player1, 0, creature.getId());

            // Aura should go to player1's graveyard (the controller), not player2's
            harness.assertInGraveyard(player1, "Holy Strength");
            harness.assertNotInGraveyard(player2, "Holy Strength");
        }
    }

    // ===== removeOrphanedAuras — Equipment behavior =====

    @Nested
    @DisplayName("removeOrphanedAuras - Equipment behavior")
    class OrphanedEquipment {

        @Test
        @DisplayName("Equipment stays on battlefield unattached when equipped creature is destroyed")
        void equipmentStaysOnBattlefieldWhenCreatureDestroyed() {
            Permanent creature = addCreatureReady(player1);
            Permanent equipment = addEquipmentAttachedTo(player1, new DarksteelAxe(), creature);

            // Use Shock (2 damage) to kill the 2/2 creature
            // DarksteelAxe gives +2/+0 making it 4/2, but Shock deals 2 damage = lethal to 2 toughness
            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1);
            harness.passPriority(player1);
            harness.castAndResolveInstant(player2, 0, creature.getId());

            harness.assertOnBattlefield(player1, "Darksteel Axe");
            assertThat(equipment.getAttachedTo()).isNull();
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Game log records equipment becoming unattached")
        void gameLogRecordsEquipmentUnattached() {
            Permanent creature = addCreatureReady(player1);
            addEquipmentAttachedTo(player1, new DarksteelAxe(), creature);

            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1);
            harness.passPriority(player1);
            harness.castAndResolveInstant(player2, 0, creature.getId());

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Darksteel Axe") && log.contains("unattached"));
        }

        @Test
        @DisplayName("Equipment remains attached when equipped creature survives")
        void equipmentStaysAttachedWhenCreatureSurvives() {
            Permanent creature = addCreatureReady(player1);
            Permanent equipment = addEquipmentAttachedTo(player1, new DarksteelAxe(), creature);

            // Kill a different creature — equipment on surviving creature should stay attached
            Permanent otherCreature = addCreatureReady(player2);
            harness.setHand(player1, List.of(new Terror()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.castAndResolveInstant(player1, 0, otherCreature.getId());

            assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        }

        @Test
        @DisplayName("Aura and equipment on same creature: aura goes to graveyard, equipment unattaches")
        void auraAndEquipmentOnSameCreature() {
            Permanent creature = addCreatureReady(player1);
            addAuraAttachedTo(player1, new HolyStrength(), creature);
            Permanent equipment = addEquipmentAttachedTo(player1, new DarksteelAxe(), creature);

            // Terror destroys the creature — Holy Strength gives +1/+2, DarksteelAxe gives +2/+0
            // Terror ignores toughness
            harness.setHand(player2, List.of(new Terror()));
            harness.addMana(player2, ManaColor.BLACK, 2);
            harness.passPriority(player1);
            harness.castAndResolveInstant(player2, 0, creature.getId());

            // Aura should be in graveyard
            harness.assertNotOnBattlefield(player1, "Holy Strength");
            harness.assertInGraveyard(player1, "Holy Strength");

            // Equipment should stay on battlefield, unattached
            harness.assertOnBattlefield(player1, "Darksteel Axe");
            assertThat(equipment.getAttachedTo()).isNull();
        }
    }

    // ===== returnStolenCreatures — Persuasion (ControlEnchantedCreatureEffect) =====

    @Nested
    @DisplayName("returnStolenCreatures - enchantment-based control")
    class ReturnStolenCreatures {

        @Test
        @DisplayName("Creature returns to owner when Persuasion is destroyed by Demystify")
        void creatureReturnsWhenPersuasionDestroyed() {
            Permanent creature = addCreatureReady(player2);

            // Player 1 casts Persuasion to steal the creature
            harness.setHand(player1, List.of(new Persuasion()));
            harness.addMana(player1, ManaColor.BLUE, 5);
            harness.castEnchantment(player1, 0, creature.getId());
            harness.passBothPriorities();

            // Creature should be on player1's battlefield
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getId().equals(creature.getId()));

            // Player 2 casts Demystify targeting Persuasion
            Permanent persuasionPerm = findPermanent(player1, "Persuasion");
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new Demystify()));
            harness.addMana(player2, ManaColor.WHITE, 1);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, persuasionPerm.getId());
            harness.passBothPriorities();

            // Creature should return to player2's battlefield
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getId().equals(creature.getId()));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getId().equals(creature.getId()));
        }

        @Test
        @DisplayName("Returned creature has summoning sickness")
        void returnedCreatureHasSummoningSickness() {
            Permanent creature = addCreatureReady(player2);

            harness.setHand(player1, List.of(new Persuasion()));
            harness.addMana(player1, ManaColor.BLUE, 5);
            harness.castEnchantment(player1, 0, creature.getId());
            harness.passBothPriorities();

            // Destroy Persuasion
            Permanent persuasionPerm = findPermanent(player1, "Persuasion");
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new Demystify()));
            harness.addMana(player2, ManaColor.WHITE, 1);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, persuasionPerm.getId());
            harness.passBothPriorities();

            Permanent returnedCreature = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getId().equals(creature.getId()))
                    .findFirst().orElseThrow();
            assertThat(returnedCreature.isSummoningSick()).isTrue();
        }

        @Test
        @DisplayName("Stolen creature tracking is cleaned up after return")
        void stolenCreatureTrackingCleanedUp() {
            Permanent creature = addCreatureReady(player2);

            harness.setHand(player1, List.of(new Persuasion()));
            harness.addMana(player1, ManaColor.BLUE, 5);
            harness.castEnchantment(player1, 0, creature.getId());
            harness.passBothPriorities();

            assertThat(gd.stolenCreatures).containsKey(creature.getId());

            // Destroy Persuasion
            Permanent persuasionPerm = findPermanent(player1, "Persuasion");
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new Demystify()));
            harness.addMana(player2, ManaColor.WHITE, 1);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, persuasionPerm.getId());
            harness.passBothPriorities();

            assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
        }

        @Test
        @DisplayName("Creature stays stolen while Persuasion aura remains attached")
        void creatureStaysStolenWhileAuraAttached() {
            Permanent creature = addCreatureReady(player2);

            harness.setHand(player1, List.of(new Persuasion()));
            harness.addMana(player1, ManaColor.BLUE, 5);
            harness.castEnchantment(player1, 0, creature.getId());
            harness.passBothPriorities();

            // Destroy a different permanent — Persuasion should stay, creature should stay stolen
            Permanent otherCreature = addCreatureReady(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player1, List.of(new Terror()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.castAndResolveInstant(player1, 0, otherCreature.getId());

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getId().equals(creature.getId()));
            assertThat(gd.stolenCreatures).containsKey(creature.getId());
        }

        @Test
        @DisplayName("Game log records creature returning to owner")
        void gameLogRecordsCreatureReturn() {
            Permanent creature = addCreatureReady(player2);

            harness.setHand(player1, List.of(new Persuasion()));
            harness.addMana(player1, ManaColor.BLUE, 5);
            harness.castEnchantment(player1, 0, creature.getId());
            harness.passBothPriorities();

            Permanent persuasionPerm = findPermanent(player1, "Persuasion");
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new Demystify()));
            harness.addMana(player2, ManaColor.WHITE, 1);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, persuasionPerm.getId());
            harness.passBothPriorities();

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Grizzly Bears") && log.contains("returns"));
        }
    }

    // ===== returnStolenCreatures — until end of turn (Threaten) =====

    @Nested
    @DisplayName("returnStolenCreatures - until end of turn steals")
    class UntilEndOfTurnSteals {

        @Test
        @DisplayName("Threaten steals creature until end of turn")
        void threatenStealsCreatureUntilEndOfTurn() {
            Permanent creature = addCreatureReady(player2);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player1, List.of(new Threaten()));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.castSorcery(player1, 0, creature.getId());
            harness.passBothPriorities();

            // Creature should be on player1's battlefield
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getId().equals(creature.getId()));
            assertThat(gd.stolenCreatures).containsKey(creature.getId());
            assertThat(gd.untilEndOfTurnStolenCreatures).contains(creature.getId());
        }

        @Test
        @DisplayName("Until-end-of-turn stolen creature returns at end of turn")
        void stolenCreatureReturnsAtEndOfTurn() {
            Permanent creature = addCreatureReady(player2);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player1, List.of(new Threaten()));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.castSorcery(player1, 0, creature.getId());
            harness.passBothPriorities();

            // Advance to end step and pass through cleanup
            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // Creature should return to player2
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getId().equals(creature.getId()));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getId().equals(creature.getId()));
            assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
        }

        @Test
        @DisplayName("Until-end-of-turn stolen creature tracking is cleaned up after return")
        void untilEndOfTurnTrackingCleanedUp() {
            Permanent creature = addCreatureReady(player2);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player1, List.of(new Threaten()));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.castSorcery(player1, 0, creature.getId());
            harness.passBothPriorities();

            // Advance to end step / cleanup
            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gd.untilEndOfTurnStolenCreatures).doesNotContain(creature.getId());
            assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
        }
    }

    // ===== Edge cases =====

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Destroying creature with no auras or equipment does not cause errors")
        void destroyCreatureWithNoAttachments() {
            Permanent creature = addCreatureReady(player2);

            harness.setHand(player1, List.of(new Terror()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.castAndResolveInstant(player1, 0, creature.getId());

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            harness.assertInGraveyard(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Full lifecycle: Persuasion steal, Demystify destroys it, creature returns with correct state")
        void fullPersuasionLifecycle() {
            Permanent creature = addCreatureReady(player2);

            // Player 1 steals with Persuasion
            harness.setHand(player1, List.of(new Persuasion()));
            harness.addMana(player1, ManaColor.BLUE, 5);
            harness.castEnchantment(player1, 0, creature.getId());
            harness.passBothPriorities();

            // Verify steal
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getId().equals(creature.getId()));
            assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());

            // Player 2 destroys Persuasion with Demystify
            Permanent persuasionPerm = findPermanent(player1, "Persuasion");
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new Demystify()));
            harness.addMana(player2, ManaColor.WHITE, 1);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, persuasionPerm.getId());
            harness.passBothPriorities();

            // Creature returns to player2
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getId().equals(creature.getId()));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getId().equals(creature.getId()));
            assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
            assertThat(creature.isSummoningSick()).isTrue();

            // Persuasion should be in graveyard
            harness.assertInGraveyard(player1, "Persuasion");
        }
    }

    // ===== Helper methods =====

    private Permanent addCreatureReady(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAuraAttachedTo(Player owner, Card auraCard, Permanent target) {
        Permanent auraPerm = new Permanent(auraCard);
        auraPerm.setAttachedTo(target.getId());
        gd.playerBattlefields.get(owner.getId()).add(auraPerm);
        return auraPerm;
    }

    private Permanent addEquipmentAttachedTo(Player owner, Card equipmentCard, Permanent target) {
        Permanent equipPerm = new Permanent(equipmentCard);
        equipPerm.setAttachedTo(target.getId());
        gd.playerBattlefields.get(owner.getId()).add(equipPerm);
        return equipPerm;
    }

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
