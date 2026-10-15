package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GenemorphImago.class, Forest.class, GrizzlyBears.class})
class GenemorphImagoTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall sets a target creature's base power and toughness to 3/3")
    void landfallSetsTargetToThreeThree() {
        Permanent target = addImagoAndTarget();
        harness.setHand(player1, List.of(new Forest()));

        triggerLandfall(target);

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Landfall sets a target creature's base power and toughness to 6/6 with six lands")
    void landfallSetsTargetToSixSixWithSixLands() {
        harness.addToBattlefield(player1, new GenemorphImago());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        triggerLandfall(target);

        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("The six-land threshold is checked when landfall resolves")
    void checksSixLandThresholdAtResolution() {
        harness.addToBattlefield(player1, new GenemorphImago());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("The landfall base power and toughness set wears off at end of turn")
    void basePowerAndToughnessSetWearsOffAtEndOfTurn() {
        Permanent target = addImagoAndTarget();
        harness.setHand(player1, List.of(new Forest()));

        triggerLandfall(target);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Landfall cannot target a noncreature permanent")
    void landfallRejectsNoncreatureTarget() {
        harness.addToBattlefield(player1, new GenemorphImago());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Landfall can target Genemorph Imago itself")
    void landfallCanTargetItself() {
        Permanent imago = harness.addToBattlefieldAndReturn(player1, new GenemorphImago());
        harness.setHand(player1, List.of(new Forest()));

        triggerLandfall(imago);

        assertThat(imago.getEffectivePower()).isEqualTo(3);
        assertThat(imago.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's land entering does not trigger landfall")
    void opponentsLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new GenemorphImago());

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Opponent lands do not count toward the six-land threshold")
    void opponentsLandsDoNotCountTowardThreshold() {
        Permanent imago = harness.addToBattlefieldAndReturn(player1, new GenemorphImago());
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new Forest());
        }
        harness.setHand(player1, List.of(new Forest()));

        triggerLandfall(imago);

        assertThat(imago.getEffectivePower()).isEqualTo(3);
        assertThat(imago.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Losing the sixth land before resolution gives the target base power and toughness 3/3")
    void losingSixthLandBeforeResolutionUsesThreeThree() {
        Permanent imago = harness.addToBattlefieldAndReturn(player1, new GenemorphImago());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, imago.getId());

        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerGraveyards.get(player1.getId()).add(land.getCard());
        harness.passBothPriorities();

        assertThat(imago.getEffectivePower()).isEqualTo(3);
        assertThat(imago.getEffectiveToughness()).isEqualTo(3);
    }

    private Permanent addImagoAndTarget() {
        harness.addToBattlefield(player1, new GenemorphImago());
        return harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
    }

    private void triggerLandfall(Permanent target) {
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
