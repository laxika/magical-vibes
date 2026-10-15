package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WallOfResistance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloakOfInvisibility.class, IronTuskElephant.class, Island.class, WallOfResistance.class})
class CloakOfInvisibilityTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can't be blocked by a non-Wall creature")
    void cannotBeBlockedByNonWall() {
        Permanent attacker = addCreatureReady(player1, new IronTuskElephant());
        attacker.setAttacking(true);
        enchant(attacker);

        Permanent blocker = addCreatureReady(player2, new IronTuskElephant());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by a Wall")
    void canBeBlockedByWall() {
        Permanent attacker = addCreatureReady(player1, new IronTuskElephant());
        attacker.setAttacking(true);
        enchant(attacker);

        Permanent blocker = addCreatureReady(player2, new WallOfResistance());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantLand() {
        harness.addToBattlefield(player2, new IronTuskElephant());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new CloakOfInvisibility()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature phases out during its controller's untap step, taking the Cloak with it")
    void phasesOutDuringControllersUntapStep() {
        Permanent bears = addCreatureReady(player1, new IronTuskElephant());
        Permanent cloak = enchant(bears);

        harness.forceActivePlayer(player1);
        advanceTurn(); // opponent's untap step — nothing of player1's phases
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, cloak);

        advanceTurn(); // player1's untap step

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears, cloak);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(bears, cloak);
        assertThat(cloak.isPhasedOutIndirectly()).isTrue();
        assertThat(bears.isPhasedOutIndirectly()).isFalse();
    }

    @Test
    @DisplayName("Phased-out creature phases back in on its controller's next untap step")
    void phasesBackInOnNextUntapStep() {
        Permanent bears = addCreatureReady(player1, new IronTuskElephant());
        Permanent cloak = enchant(bears);

        harness.forceActivePlayer(player1);
        advanceTurn();
        advanceTurn(); // phases out
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);

        advanceTurn();
        advanceTurn(); // player1's next untap step

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, cloak);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).isEmpty();
        assertThat(cloak.isPhasedOutIndirectly()).isFalse();
        assertThat(cloak.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("A phased-out creature is treated as though it doesn't exist")
    void phasedOutCreatureDoesNotExist() {
        Permanent bears = addCreatureReady(player1, new IronTuskElephant());
        enchant(bears);

        harness.forceActivePlayer(player1);
        advanceTurn();
        advanceTurn();

        assertThat(gqs.findPermanentById(gd, bears.getId())).isNull();
        assertThat(gd.anyPermanentMatches(permanent -> permanent.getId().equals(bears.getId()))).isFalse();
    }

    @Test
    @DisplayName("A Cloak cast on an opponent's creature follows that creature's phasing cycle")
    void opponentCreatureAndCloakPhaseTogether() {
        Permanent creature = addCreatureReady(player2, new IronTuskElephant());
        harness.setHand(player1, List.of(new CloakOfInvisibility()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent cloak = findPermanent(player1, "Cloak of Invisibility");
        assertThat(cloak.getAttachedTo()).isEqualTo(creature.getId());

        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cloak);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);

        harness.performUntapStep(player2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cloak);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(cloak);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);

        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cloak);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(cloak);

        harness.performUntapStep(player2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cloak);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(cloak.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertNotInGraveyard(player1, "Cloak of Invisibility");
    }

    @Test
    @DisplayName("Phasing happens before untapping and does not affect other creatures")
    void phasesOutBeforeUntappingAndUntapsWhenPhasingIn() {
        Permanent creature = addCreatureReady(player1, new IronTuskElephant());
        Permanent otherCreature = addCreatureReady(player1, new IronTuskElephant());
        Permanent cloak = enchant(creature);
        creature.tap();
        otherCreature.tap();

        harness.performUntapStep(player1);

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature, cloak);
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherCreature);
        assertThat(otherCreature.isTapped()).isFalse();

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, cloak);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Other creatures remain blockable by non-Walls")
    void blockingRestrictionOnlyAppliesToEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player1, new IronTuskElephant());
        enchant(enchanted);
        Permanent attacker = addCreatureReady(player1, new IronTuskElephant());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new IronTuskElephant());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent enchant(Permanent host) {
        Permanent cloak = harness.addToBattlefieldAndReturn(player1, new CloakOfInvisibility());
        cloak.setAttachedTo(host.getId());
        return cloak;
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

}
