package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeoninSunStandard.class, AlphaMyr.class})
class LeoninSunStandardTest extends BaseCardTest {

    @Test
    @DisplayName("Activation gives creatures you control +1/+1 until end of turn")
    void boostsYourCreaturesOnly() {
        addStandard(player1);
        Permanent ownCreature = addCreatureReady(player1, new AlphaMyr());
        Permanent opposingCreature = addCreatureReady(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.WHITE, 2);

        activateAndResolve();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at the cleanup step")
    void boostWearsOff() {
        addStandard(player1);
        Permanent ownCreature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.WHITE, 2);

        activateAndResolve();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability does not tap the artifact")
    void activationDoesNotTapArtifact() {
        Permanent standard = addStandard(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        activateAndResolve();

        assertThat(standard.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each activation adds another +1/+1 until end of turn")
    void repeatedActivationsStack() {
        addStandard(player1);
        Permanent ownCreature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.WHITE, 4);

        activateAndResolve();
        activateAndResolve();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addStandard(Player player) {
        return addCreatureReady(player, new LeoninSunStandard());
    }
}
