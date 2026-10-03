package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BroadcastRambler.class, GrizzlyBears.class})
class BroadcastRamblerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 1/1 colorless Thopter artifact creature token with flying")
    void etbCreatesThopterToken() {
        harness.castFromHand(player1, new BroadcastRambler(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Thopter");

        assertThat(thopter).isNotNull();
        assertThat(thopter.getCard().isToken()).isTrue();
        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
        assertThat(thopter.getCard().getName()).isEqualTo("Thopter");
        assertThat(thopter.getCard().getPower()).isEqualTo(1);
        assertThat(thopter.getCard().getToughness()).isEqualTo(1);
        assertThat(thopter.getCard().getColors()).isEmpty();
        assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
    }

    @Test
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent vehicle = addVehicleReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent vehicle = addVehicleReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    private Permanent addVehicleReady(Player player) {
        return addCreatureReady(player, new BroadcastRambler());
    }

    @Test
    void newlyCreatedThopterCanCrewImmediately() {
        harness.castFromHand(player1, new BroadcastRambler(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent vehicle = findPermanent(player1, "Broadcast Rambler");
        Permanent thopter = findPermanent(player1, "Thopter");

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(thopter.isSummoningSick()).isTrue();
        harness.activateAbility(player1, 0, null, null);

        assertThat(thopter.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(vehicle.isTapped()).isFalse();
        assertThat(vehicle.isSummoningSick()).isTrue();
        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
    }

    @Test
    void opponentsCreatureCannotPayCrewCost() {
        Permanent vehicle = addVehicleReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedCreatureCannotPayCrewCost() {
        Permanent vehicle = addVehicleReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
