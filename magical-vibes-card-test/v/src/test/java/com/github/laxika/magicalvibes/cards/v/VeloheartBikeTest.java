package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BeastriderVanguard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VeloheartBike.class, BeastriderVanguard.class})
class VeloheartBikeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains its controller 2 life")
    void entersAndGainsLife() {
        harness.setHand(player1, List.of(new VeloheartBike()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 2);
    }

    @Test
    @DisplayName("Tapping the Bike prompts for a color and adds one mana")
    void tapsForAnyColorMana() {
        Permanent bike = addCreatureReady(player1, new VeloheartBike());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(bike.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Crew 2 animates the Bike and taps the crew")
    void crewAnimatesBikeAndTapsCrew() {
        Permanent bike = addCreatureReady(player1, new VeloheartBike());
        Permanent crew = addCreatureReady(player1, new BeastriderVanguard());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bike)).isTrue();
        assertThat(crew.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bike)).isFalse();
    }

    @Test
    @DisplayName("An uncrewed Bike can produce mana on the turn it enters")
    void newlyEnteredUncrewedBikeCanProduceMana() {
        Permanent bike = harness.addToBattlefieldAndReturn(player1, new VeloheartBike());
        bike.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(bike.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning-sick creatures can crew, but a newly entered crewed Bike cannot tap for mana")
    void summoningSicknessAllowsCrewButPreventsAnimatedBikeMana() {
        Permanent bike = harness.addToBattlefieldAndReturn(player1, new VeloheartBike());
        bike.setSummoningSick(true);
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new BeastriderVanguard());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, bike)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bike)).isTrue();
        assertThat(bike.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(bike.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Bike can be crewed and stays tapped")
    void tappedBikeCanBeCrewed() {
        Permanent bike = addCreatureReady(player1, new VeloheartBike());
        Permanent crew = addCreatureReady(player1, new BeastriderVanguard());
        bike.tap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bike)).isTrue();
        assertThat(bike.isTapped()).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures cannot pay the crew cost")
    void tappedCreatureCannotCrew() {
        Permanent bike = addCreatureReady(player1, new VeloheartBike());
        Permanent crew = addCreatureReady(player1, new BeastriderVanguard());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, bike)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
