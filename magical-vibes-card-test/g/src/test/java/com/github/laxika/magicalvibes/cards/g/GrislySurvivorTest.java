package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.StripedRiverwinder;
import com.github.laxika.magicalvibes.cards.t.TormentOfVenom;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrislySurvivor.class, StripedRiverwinder.class, TormentOfVenom.class})
class GrislySurvivorTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card gives this creature +2/+0")
    void cyclingBoostsSelf() {
        harness.addToBattlefield(player1, new GrislySurvivor());
        harness.setHand(player1, List.of(new StripedRiverwinder()));
        harness.setLibrary(player1, List.of(new GrislySurvivor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // resolve the boost trigger

        Permanent survivor = getGrislySurvivor();
        assertThat(survivor.getPowerModifier()).isEqualTo(2);
        assertThat(survivor.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Each discard stacks another +2/+0")
    void discardsStack() {
        harness.addToBattlefield(player1, new GrislySurvivor());
        harness.setHand(player1, List.of(new StripedRiverwinder(), new StripedRiverwinder()));
        harness.setLibrary(player1, List.of(new GrislySurvivor(), new GrislySurvivor()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent survivor = getGrislySurvivor();
        assertThat(survivor.getPowerModifier()).isEqualTo(4);
        assertThat(survivor.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrislySurvivor());
        harness.setHand(player1, List.of(new StripedRiverwinder()));
        harness.setLibrary(player1, List.of(new GrislySurvivor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent survivor = getGrislySurvivor();
        assertThat(survivor.getPowerModifier()).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(survivor.getPowerModifier()).isEqualTo(0);
        assertThat(survivor.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("An ordinary discard gives only its controller's Survivor +2/+0")
    void ordinaryDiscardBoostsOnlyControllersSurvivor() {
        harness.addToBattlefield(player1, new GrislySurvivor());
        harness.addToBattlefield(player2, new GrislySurvivor());
        harness.addToBattlefield(player1, new StripedRiverwinder());
        harness.setHand(player1, List.of(new TormentOfVenom(), new StripedRiverwinder()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Striped Riverwinder"));
        harness.handleListChoice(player1, ChoiceContext.TormentPenaltyChoice.DISCARD);
        harness.handleCardChosen(player1, 0);

        assertThat(getGrislySurvivor().getPowerModifier()).isZero();
        harness.passBothPriorities();

        assertThat(getGrislySurvivor().getPowerModifier()).isEqualTo(2);
        assertThat(getGrislySurvivor().getToughnessModifier()).isZero();
        assertThat(findPermanent(player2, "Grisly Survivor").getPowerModifier()).isZero();
        harness.assertInGraveyard(player1, "Striped Riverwinder");
    }

    @Test
    @DisplayName("An opponent cycling does not boost this creature")
    void opponentCyclingDoesNotBoostSelf() {
        harness.addToBattlefield(player1, new GrislySurvivor());
        harness.setHand(player2, List.of(new StripedRiverwinder()));
        harness.setLibrary(player2, List.of(new GrislySurvivor()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        assertThat(getGrislySurvivor().getPowerModifier()).isZero();
        assertThat(getGrislySurvivor().getToughnessModifier()).isZero();
        harness.assertInGraveyard(player2, "Striped Riverwinder");
        harness.assertInHand(player2, "Grisly Survivor");
    }

    private Permanent getGrislySurvivor() {
        return findPermanent(player1, "Grisly Survivor");
    }
}
