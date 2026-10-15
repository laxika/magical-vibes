package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CircleOfDreamsDruid;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WorldspineWurm;
import com.github.laxika.magicalvibes.cards.y.YouComeToARiver;
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

@CardUsed({EyesOfTheBeholder.class, FountainOfYouth.class, GrizzlyBears.class, WorldspineWurm.class,
        CircleOfDreamsDruid.class, YouComeToARiver.class})
class EyesOfTheBeholderTest extends BaseCardTest {

    @Test
    @DisplayName("Eyes of the Beholder puts a small creature into its owner's graveyard")
    void killsSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castEyesOfTheBeholder(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The -11/-11 effect wears off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());

        castEyesOfTheBeholder(target);

        assertThat(target.getPowerModifier()).isEqualTo(-11);
        assertThat(target.getToughnessModifier()).isEqualTo(-11);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player2, "Worldspine Wurm");
    }

    @Test
    @DisplayName("Eyes of the Beholder cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new EyesOfTheBeholder()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Eyes of the Beholder can target its controller's creature")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CircleOfDreamsDruid());

        castEyesOfTheBeholder(target);

        harness.assertNotOnBattlefield(player1, "Circle of Dreams Druid");
        harness.assertInGraveyard(player1, "Circle of Dreams Druid");
        harness.assertInGraveyard(player1, "Eyes of the Beholder");
    }

    @Test
    @DisplayName("Eyes of the Beholder does not affect another creature when its target leaves")
    void targetReturnedToHandBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CircleOfDreamsDruid());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new CircleOfDreamsDruid());
        harness.setHand(player1, List.of(new EyesOfTheBeholder()));
        addMana();
        harness.castInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new YouComeToARiver()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInHand(player2, "Circle of Dreams Druid");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Eyes of the Beholder");
        harness.assertNotInGraveyard(player2, "Circle of Dreams Druid");
        harness.assertOnBattlefield(player2, "Circle of Dreams Druid");
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }

    private void castEyesOfTheBeholder(Permanent target) {
        harness.setHand(player1, List.of(new EyesOfTheBeholder()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
