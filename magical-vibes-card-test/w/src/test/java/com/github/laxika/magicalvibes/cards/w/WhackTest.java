package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EagerCadet;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.r.RakishRevelers;
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

@CardUsed({Whack.class, EagerCadet.class, ColossalDreadmaw.class, FountainOfYouth.class, RakishRevelers.class})
class WhackTest extends BaseCardTest {

    @Test
    @DisplayName("Costs only {B} when targeting a white creature")
    void reducedCostWhenTargetingWhiteCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EagerCadet());
        harness.setHand(player1, List.of(new Whack()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs {3}{B} when targeting a nonwhite creature")
    void fullCostWhenTargetingNonwhiteCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new Whack()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(-4);
    }

    @Test
    @DisplayName("The -4/-4 effect wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new Whack()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Whack()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A multicolored white creature grants the discount and dies to -4/-4")
    void multicoloredWhiteTargetGrantsDiscountAndDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RakishRevelers());
        harness.setHand(player1, List.of(new Whack()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotOnBattlefield(player2, "Rakish Revelers");
        harness.assertInGraveyard(player2, "Rakish Revelers");
        harness.assertInGraveyard(player1, "Whack");
    }

    @Test
    @DisplayName("A white creature elsewhere does not discount a nonwhite target")
    void whiteCreatureDoesNotDiscountNonwhiteTarget() {
        harness.addToBattlefield(player2, new RakishRevelers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new Whack()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The discount does not remove the black mana requirement")
    void discountStillRequiresBlackMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RakishRevelers());
        harness.setHand(player1, List.of(new Whack()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Rakish Revelers");
    }
}
