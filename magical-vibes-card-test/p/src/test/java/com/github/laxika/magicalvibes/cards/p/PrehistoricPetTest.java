package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrehistoricPet.class, FugitiveWizard.class, GrizzlyBears.class})
class PrehistoricPetTest extends BaseCardTest {

    @Test
    @DisplayName("Has skulk")
    void hasSkulk() {
        Permanent pet = addCreatureReady(player1, new PrehistoricPet());

        assertThat(gqs.hasKeyword(gd, pet, Keyword.SKULK)).isTrue();
    }

    @Test
    @DisplayName("Cannot be blocked by a creature with greater power")
    void cannotBeBlockedByGreaterPower() {
        Permanent pet = addCreatureReady(player1, new PrehistoricPet());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackAndPrepareBlockers(pet);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(pet)
        )))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("skulk");
    }

    @Test
    @DisplayName("Can be blocked by a creature with equal power")
    void canBeBlockedByEqualPower() {
        Permanent pet = addCreatureReady(player1, new PrehistoricPet());
        Permanent blocker = addCreatureReady(player2, new FugitiveWizard());
        declareAttackAndPrepareBlockers(pet);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(pet)
        )));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Pays to return another creature you control to its owner's hand")
    void returnsAnotherCreatureToHand() {
        Permanent pet = addCreatureReady(player1, new PrehistoricPet());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(pet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target itself or activate during an opponent's turn")
    void enforcesTargetAndTimingRestrictions() {
        Permanent pet = addCreatureReady(player1, new PrehistoricPet());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, pet.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can return another copy during your end step")
    void returnsAnotherCopyDuringEndStep() {
        Permanent pet = addCreatureReady(player1, new PrehistoricPet());
        Permanent otherPet = addCreatureReady(player1, new PrehistoricPet());
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, otherPet.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pet).doesNotContain(otherPet);
        harness.assertInHand(player1, "Prehistoric Pet");
        assertThat(pet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent pet = addCreatureReady(player1, new PrehistoricPet());
        Permanent opponentPet = addCreatureReady(player2, new PrehistoricPet());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentPet.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(pet.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent pet = harness.addToBattlefieldAndReturn(player1, new PrehistoricPet());
        pet.setSummoningSick(true);
        Permanent target = addCreatureReady(player1, new PrehistoricPet());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(pet.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent pet = addCreatureReady(player1, new PrehistoricPet());
        Permanent target = addCreatureReady(player1, new PrehistoricPet());
        pet.tap();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent pet = addCreatureReady(player1, new PrehistoricPet());
        Permanent target = addCreatureReady(player1, new PrehistoricPet());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(pet.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability still resolves after its source is returned to hand")
    void resolvesAfterSourceLeavesBattlefield() {
        harness.setHand(player1, List.of());
        Permanent pet = addCreatureReady(player1, new PrehistoricPet());
        Permanent secondPet = addCreatureReady(player1, new PrehistoricPet());
        Permanent target = addCreatureReady(player1, new PrehistoricPet());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.activateAbility(player1, 1, 0, null, pet.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pet).contains(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(secondPet);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Target becoming opponent-controlled makes the ability fail to resolve")
    void rechecksTargetControllerAtResolution() {
        addCreatureReady(player1, new PrehistoricPet());
        Permanent target = addCreatureReady(player1, new PrehistoricPet());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertNotInHand(player1, "Prehistoric Pet");
        harness.assertNotInHand(player2, "Prehistoric Pet");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns a creature you control to its owner rather than its controller")
    void returnsBorrowedCreatureToOwnersHand() {
        addCreatureReady(player1, new PrehistoricPet());
        Permanent borrowed = addCreatureReady(player2, new PrehistoricPet());
        gd.playerBattlefields.get(player2.getId()).remove(borrowed);
        gd.playerBattlefields.get(player1.getId()).add(borrowed);
        gd.stolenCreatures.put(borrowed.getId(), player2.getId());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, borrowed.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(borrowed);
        harness.assertInHand(player2, "Prehistoric Pet");
        harness.assertNotInHand(player1, "Prehistoric Pet");
    }

    private void declareAttackAndPrepareBlockers(Permanent attacker) {
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
