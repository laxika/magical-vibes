package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DispersingOrb.class, Island.class, ElvishWarrior.class})
class DispersingOrbTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a permanent returns the target permanent to its owner's hand")
    void sacrificesPermanentAndReturnsTarget() {
        addOrb();
        Permanent sacrificeTarget = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        addAbilityMana();

        activate(target);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrificeTarget.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Island");
        harness.assertInHand(player2, "Elvish Warrior");
        harness.assertOnBattlefield(player1, "Dispersing Orb");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The ability can target a land")
    void canTargetLand() {
        addOrb();
        Permanent sacrificeTarget = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        addAbilityMana();

        activate(target);
        harness.handlePermanentChosen(player1, sacrificeTarget.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInHand(player2, "Island");
    }

    @Test
    @DisplayName("The ability can target a permanent its controller controls")
    void canTargetOwnPermanent() {
        addOrb();
        Permanent sacrificeTarget = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        addAbilityMana();

        activate(target);
        harness.handlePermanentChosen(player1, sacrificeTarget.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.assertInHand(player1, "Elvish Warrior");
    }

    @Test
    @DisplayName("A target sacrificed as the cost is not returned")
    void targetSacrificedAsCostIsNotReturned() {
        addOrb();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        addAbilityMana();

        activate(target);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Warrior");
        harness.assertNotInHand(player1, "Elvish Warrior");
        harness.assertOnBattlefield(player1, "Dispersing Orb");
    }

    @Test
    @DisplayName("The Orb itself may be sacrificed as the cost")
    void maySacrificeItself() {
        addOrb();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        addAbilityMana();

        activate(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dispersing Orb");
        harness.assertInHand(player2, "Elvish Warrior");
    }

    @Test
    @DisplayName("The Orb can return itself by sacrificing another permanent")
    void canReturnItself() {
        Permanent orb = addOrb();
        Permanent sacrificeTarget = harness.addToBattlefieldAndReturn(player1, new Island());
        addAbilityMana();

        activate(orb);
        harness.handlePermanentChosen(player1, sacrificeTarget.getId());

        harness.assertInGraveyard(player1, "Island");
        harness.assertOnBattlefield(player1, "Dispersing Orb");
        harness.assertNotInHand(player1, "Dispersing Orb");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Dispersing Orb");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(orb);
    }

    @Test
    @DisplayName("The ability can be activated during the opponent's turn")
    void canActivateDuringOpponentsTurn() {
        addOrb();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player1);

        activate(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dispersing Orb");
        harness.assertInHand(player2, "Elvish Warrior");
    }

    @Test
    @DisplayName("Generic mana cannot replace the blue mana in the activation cost")
    void cannotActivateWithoutBlueMana() {
        addOrb();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> activate(target)).isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Dispersing Orb");
        harness.assertOnBattlefield(player2, "Elvish Warrior");
        harness.assertNotInGraveyard(player1, "Dispersing Orb");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    private Permanent addOrb() {
        return harness.addToBattlefieldAndReturn(player1, new DispersingOrb());
    }

    private void addAbilityMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void activate(Permanent target) {
        harness.activateAbility(player1, 0, 0, null, target.getId());
    }
}
