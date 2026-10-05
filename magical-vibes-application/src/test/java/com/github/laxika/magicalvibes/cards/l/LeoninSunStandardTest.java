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

    @Test
    @DisplayName("Creatures entering before resolution receive the boost")
    void includesCreaturesEnteringBeforeResolution() {
        addStandard(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void excludesCreaturesEnteringAfterResolution() {
        addStandard(player1);
        Permanent original = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.WHITE, 2);

        activateAndResolve();
        Permanent later = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Standard can activate during an opponent's turn")
    void activatesWhileTappedOnOpponentsTurn() {
        Permanent standard = addStandard(player1);
        standard.setTapped(true);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.ensurePriority(player1);

        activateAndResolve();

        assertThat(standard.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addStandard(Player player) {
        return addCreatureReady(player, new LeoninSunStandard());
    }
}
