package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HungerOfTheNimTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +1/+0 for each artifact its controller controls")
    void scalesPowerBoostWithControlledArtifacts() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HungerOfTheNim()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at the cleanup step")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HungerOfTheNim()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new HungerOfTheNim()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
