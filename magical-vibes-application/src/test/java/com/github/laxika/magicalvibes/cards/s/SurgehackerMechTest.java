package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurgehackerMech.class, DuskLegionDreadnought.class, GrizzlyBears.class, SerraAngel.class})
class SurgehackerMechTest extends BaseCardTest {

    @Test
    void entersAndDealsDamageTwiceTheNumberOfVehiclesYouControl() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addVehicle(player1);
        addVehicle(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SurgehackerMech()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    void etbCannotTargetYourOwnCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentTarget = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SurgehackerMech()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opponentTarget.getId());
        assertThat(choice.validIds()).doesNotContain(target.getId());

        harness.handlePermanentChosen(player1, opponentTarget.getId());
        harness.passBothPriorities();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent vehicle = addCreatureReady(player1, new SurgehackerMech());
        Permanent crew = addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, vehicle, CardSubtype.VEHICLE)).isTrue();
    }

    private Permanent addVehicle(Player player) {
        return harness.addToBattlefieldAndReturn(player, new DuskLegionDreadnought());
    }
}
