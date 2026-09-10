package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HuaTuoHonoredPhysician;
import com.github.laxika.magicalvibes.cards.w.WuInfantry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GuanYuSaintedWarrior.class)
class GuanYuSaintedWarriorTest extends BaseCardTest {

    // ===== Death trigger =====

    @Test
    @DisplayName("Accepting the death trigger shuffles Guan Yu from the graveyard into its owner's library")
    void diesThenAcceptShufflesIntoLibrary() {
        harness.setLibrary(player1, new ArrayList<>());
        Permanent guanYu = harness.addToBattlefieldAndReturn(player1, new GuanYuSaintedWarrior());
        guanYu.setMarkedDamage(5);

        harness.runStateBasedActions();

        // It first enters the graveyard, then its death trigger waits on the stack.
        harness.assertInGraveyard(player1, "Guan Yu, Sainted Warrior");

        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Guan Yu, Sainted Warrior");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Guan Yu, Sainted Warrior"));
    }

    @Test
    @DisplayName("Declining the death trigger leaves Guan Yu in the graveyard")
    void diesThenDeclineLeavesInGraveyard() {
        harness.setLibrary(player1, new ArrayList<>());
        Permanent guanYu = harness.addToBattlefieldAndReturn(player1, new GuanYuSaintedWarrior());
        guanYu.setMarkedDamage(5);

        harness.runStateBasedActions();

        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Guan Yu, Sainted Warrior");
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Guan Yu, Sainted Warrior"));
    }

    @Test
    @CardUsed(HuaTuoHonoredPhysician.class)
    @DisplayName("Accepting the death trigger does not shuffle Guan Yu after it leaves the graveyard")
    void doesNotShuffleIfItLeavesGraveyardBeforeResolution() {
        harness.setLibrary(player1, new ArrayList<>());
        Permanent guanYu = addCreatureReady(player1, new GuanYuSaintedWarrior());
        Permanent huaTuo = addCreatureReady(player1, new HuaTuoHonoredPhysician());
        guanYu.setMarkedDamage(5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.runStateBasedActions();

        int huaTuoIdx = gd.playerBattlefields.get(player1.getId()).indexOf(huaTuo);
        harness.activateAbility(player1, huaTuoIdx, null, guanYu.getCard().getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(guanYu.getCard().getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(guanYu.getCard().getId());
    }

    @Test
    @DisplayName("Does not trigger when an opponent controls Guan Yu")
    void doesNotTriggerWhenOpponentControlsIt() {
        GuanYuSaintedWarrior guanYuCard = new GuanYuSaintedWarrior();
        guanYuCard.setOwnerId(player1.getId());
        Permanent guanYu = addCreatureReady(player2, guanYuCard);
        gd.stolenCreatures.put(guanYu.getId(), player1.getId());
        guanYu.setMarkedDamage(5);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Guan Yu, Sainted Warrior");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    // ===== Horsemanship =====

    @Test
    @CardUsed(WuInfantry.class)
    @DisplayName("Guan Yu can't be blocked by a creature without horsemanship")
    void cannotBeBlockedByCreatureWithoutHorsemanship() {
        Permanent blockerPerm = addCreatureReady(player2, new WuInfantry());

        Permanent atkPerm = addCreatureReady(player1, new GuanYuSaintedWarrior());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    @DisplayName("Guan Yu can be blocked by a creature with horsemanship")
    void canBeBlockedByCreatureWithHorsemanship() {
        Permanent blockerPerm = addCreatureReady(player2, new GuanYuSaintedWarrior());

        Permanent atkPerm = addCreatureReady(player1, new GuanYuSaintedWarrior());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
