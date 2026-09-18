package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CranialPlating;
import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
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

@CardUsed({ViciousBetrayal.class, DrossCrocodile.class, CranialPlating.class})
class ViciousBetrayalTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Gives +2/+2 for each creature sacrificed")
    void boostsForEachCreatureSacrificed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        Permanent firstSacrifice = harness.addToBattlefieldAndReturn(player1, new DrossCrocodile());
        Permanent secondSacrifice = harness.addToBattlefieldAndReturn(player1, new DrossCrocodile());

        harness.setHand(player1, List.of(new ViciousBetrayal()));
        addMana();

        harness.castSorceryWithSacrifices(player1, 0, target.getId(),
                List.of(firstSacrifice.getId(), secondSacrifice.getId()));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Dross Crocodile");
    }

    @Test
    @DisplayName("Sacrificing no creatures gives +0/+0")
    void sacrificingNoCreaturesGivesNoBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());

        harness.setHand(player1, List.of(new ViciousBetrayal()));
        addMana();

        harness.castSorceryWithSacrifices(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DrossCrocodile());

        harness.setHand(player1, List.of(new ViciousBetrayal()));
        addMana();

        harness.castSorceryWithSacrifices(player1, 0, target.getId(), List.of(sacrifice.getId()));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature permanent")
    void cannotSacrificeNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        Permanent cranialPlating = harness.addToBattlefieldAndReturn(player1, new CranialPlating());

        harness.setHand(player1, List.of(new ViciousBetrayal()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, target.getId(),
                List.of(cranialPlating.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Cranial Plating");
    }

    @Test
    @DisplayName("Cannot sacrifice a creature controlled by an opponent")
    void cannotSacrificeOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());

        harness.setHand(player1, List.of(new ViciousBetrayal()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, target.getId(),
                List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Dross Crocodile");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent cranialPlating = harness.addToBattlefieldAndReturn(player2, new CranialPlating());

        harness.setHand(player1, List.of(new ViciousBetrayal()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, cranialPlating.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
