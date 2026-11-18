package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.cards.i.IndebtedSamurai;
import com.github.laxika.magicalvibes.cards.m.MendingHands;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HundredTalonStrike.class, FirstVolley.class, GnarledMass.class,
        IndebtedSamurai.class, GodsEyeGateToTheReikai.class, MendingHands.class})
class HundredTalonStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +1/+0 and gains first strike")
    void pumpsAndGrantsFirstStrike() {
        Permanent mass = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        harness.setHand(player1, List.of(new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, mass.getId());

        assertThat(PermanentGrants.powerBoostOf(gd, mass)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, mass)).isZero();
        assertThat(gqs.hasKeyword(gd, mass, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The pump and first strike wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent mass = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        harness.setHand(player1, List.of(new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, mass.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, mass)).isZero();
        assertThat(gqs.hasKeyword(gd, mass, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GodsEyeGateToTheReikai());
        harness.setHand(player1, List.of(new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell by tapping a white creature and adds its effects")
    void splicesOntoArcaneSpell() {
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player1, new IndebtedSamurai());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        harness.setHand(player1, List.of(new FirstVolley(), new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithSplice(player1, 0, target.getId(), List.of(1), List.of(whiteCreature.getId()));
        harness.passBothPriorities();

        assertThat(whiteCreature.isTapped()).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Hundred-Talon Strike");
    }

    @Test
    @DisplayName("Cannot pay the splice cost by tapping a creature that is not white")
    void cannotTapNonWhiteCreatureForSplice() {
        Permanent nonWhiteCreature = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        harness.setHand(player1, List.of(new FirstVolley(), new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1),
                List.of(nonWhiteCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot pay the splice cost by tapping a white creature an opponent controls")
    void cannotTapOpponentWhiteCreatureForSplice() {
        Permanent opponentWhiteCreature = harness.addToBattlefieldAndReturn(player2, new IndebtedSamurai());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        harness.setHand(player1, List.of(new FirstVolley(), new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1),
                List.of(opponentWhiteCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot pay the splice cost by tapping a white creature that is already tapped")
    void cannotTapTappedWhiteCreatureForSplice() {
        Permanent tappedWhiteCreature = harness.addToBattlefieldAndReturn(player1, new IndebtedSamurai());
        tappedWhiteCreature.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        harness.setHand(player1, List.of(new FirstVolley(), new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1),
                List.of(tappedWhiteCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot splice onto a non-Arcane instant")
    void spliceRejectedOnNonArcaneInstant() {
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player1, new IndebtedSamurai());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        harness.setHand(player1, List.of(new MendingHands(), new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1),
                List.of(whiteCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick white creature can pay the splice cost and be the target")
    void canTapSummoningSickTargetForSplice() {
        Permanent samurai = harness.addToBattlefieldAndReturn(player1, new IndebtedSamurai());
        samurai.setSummoningSick(true);
        harness.setHand(player1, List.of(new FirstVolley(), new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithSplice(player1, 0, samurai.getId(), List.of(1), List.of(samurai.getId()));

        assertThat(samurai.isTapped()).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, samurai)).isZero();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, samurai)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, samurai)).isZero();
        assertThat(gqs.hasKeyword(gd, samurai, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(samurai.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
        harness.assertInHand(player1, "Hundred-Talon Strike");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, samurai)).isZero();
        assertThat(gqs.hasKeyword(gd, samurai, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Splice requires a creature to be selected for its tap cost")
    void cannotSpliceWithoutTapPayment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        harness.setHand(player1, List.of(new FirstVolley(), new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
