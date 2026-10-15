package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AutomaticLibrarian;
import com.github.laxika.magicalvibes.cards.m.MoltenMonstrosity;
import com.github.laxika.magicalvibes.cards.r.RelicOfLegends;
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

@CardUsed({FlowstoneInfusion.class, MoltenMonstrosity.class, RelicOfLegends.class,
        AutomaticLibrarian.class})
class FlowstoneInfusionTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/-2 until end of turn")
    void givesPlusTwoMinusTwo() {
        Permanent target = addCreatureReady(player2, new MoltenMonstrosity());
        castFlowstoneInfusion(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new MoltenMonstrosity());
        castFlowstoneInfusion(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RelicOfLegends());
        harness.setHand(player1, List.of(new FlowstoneInfusion()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void putsCreatureWithZeroToughnessIntoGraveyard() {
        Permanent target = addCreatureReady(player2, new AutomaticLibrarian());

        castFlowstoneInfusion(target);

        harness.assertNotOnBattlefield(player2, "Automatic Librarian");
        harness.assertInGraveyard(player2, "Automatic Librarian");
    }

    @Test
    void canBoostOwnCreatureAndMultipleCastsAccumulate() {
        Permanent target = addCreatureReady(player1, new MoltenMonstrosity());

        castFlowstoneInfusion(target);
        castFlowstoneInfusion(target);

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(-4);
        assertThat(target.getEffectivePower()).isEqualTo(9);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    void doesNotAffectAnotherCreatureWhenTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new MoltenMonstrosity());
        Permanent other = addCreatureReady(player2, new MoltenMonstrosity());
        harness.setHand(player1, List.of(new FlowstoneInfusion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Flowstone Infusion");
    }

    private void castFlowstoneInfusion(Permanent target) {
        harness.setHand(player1, List.of(new FlowstoneInfusion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
