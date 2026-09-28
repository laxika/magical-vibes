package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RazorfootGriffin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunscapeMaster.class, RazorfootGriffin.class, Island.class})
class SunscapeMasterTest extends BaseCardTest {

    @Test
    @DisplayName("First ability gives +2/+2 to creatures you control only")
    void boostsOwnCreaturesOnly() {
        Permanent source = addCreatureReady(player1, new SunscapeMaster());
        Permanent ownGriffin = addCreatureReady(player1, new RazorfootGriffin());
        Permanent opposingGriffin = addCreatureReady(player2, new RazorfootGriffin());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(4);
        assertThat(source.getEffectiveToughness()).isEqualTo(4);
        assertThat(ownGriffin.getEffectivePower()).isEqualTo(4);
        assertThat(ownGriffin.getEffectiveToughness()).isEqualTo(4);
        assertThat(opposingGriffin.getEffectivePower()).isEqualTo(2);
        assertThat(opposingGriffin.getEffectiveToughness()).isEqualTo(2);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The +2/+2 boost does not affect creatures entering later")
    void boostDoesNotAffectCreaturesEnteringLater() {
        Permanent source = addCreatureReady(player1, new SunscapeMaster());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), 0, null, null);
        harness.passBothPriorities();

        Permanent laterGriffin = addCreatureReady(player1, new RazorfootGriffin());

        assertThat(source.getEffectivePower()).isEqualTo(4);
        assertThat(laterGriffin.getEffectivePower()).isEqualTo(2);
        assertThat(laterGriffin.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The +2/+2 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent source = addCreatureReady(player1, new SunscapeMaster());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), 0, null, null);
        harness.passBothPriorities();
        assertThat(source.getEffectivePower()).isEqualTo(4);
        assertThat(source.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(2);
        assertThat(source.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability returns a target creature to its owner's hand")
    void returnsTargetCreatureToOwnersHand() {
        Permanent source = addCreatureReady(player1, new SunscapeMaster());
        Permanent target = addCreatureReady(player2, new RazorfootGriffin());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Razorfoot Griffin");
        harness.assertInHand(player2, "Razorfoot Griffin");
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability returns a controlled creature to its owner's hand")
    void returnsControlledCreatureToItsOwnersHand() {
        Permanent source = addCreatureReady(player1, new SunscapeMaster());
        RazorfootGriffin card = new RazorfootGriffin();
        card.setOwnerId(player2.getId());
        Permanent target = addCreatureReady(player1, card);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Razorfoot Griffin");
        harness.assertNotInHand(player1, "Razorfoot Griffin");
        harness.assertInHand(player2, "Razorfoot Griffin");
    }

    @Test
    @DisplayName("Second ability cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent source = addCreatureReady(player1, new SunscapeMaster());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(source),
                1,
                null,
                island.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
    }
}
