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

    private void declareAttackAndPrepareBlockers(Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
