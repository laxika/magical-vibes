package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlacrianArmoryTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +0/+1 and vigilance")
    void boostsOwnCreaturesOnly() {
        harness.addToBattlefield(player1, new AlacrianArmory());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Beginning of combat saddles a targeted Mount until end of turn")
    void saddlesMount() {
        harness.addToBattlefield(player1, new AlacrianArmory());
        Permanent mount = addCreatureReady(player1, new GrizzlyBears());
        TestCards.mutableCard(mount).setSubtypes(List.of(CardSubtype.MOUNT));

        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, mount.getId());
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("Beginning of combat animates a targeted Vehicle and only permits your Vehicles or Mounts")
    void animatesVehicleAndRestrictsTargets() {
        harness.addToBattlefield(player1, new AlacrianArmory());
        Permanent ownVehicle = addVehicle(player1);
        Permanent opponentVehicle = addVehicle(player2);

        advanceToBeginningOfCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownVehicle.getId()).doesNotContain(opponentVehicle.getId());

        harness.handlePermanentChosen(player1, ownVehicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ownVehicle)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ownVehicle)).isFalse();
    }

    @Test
    @DisplayName("The beginning-of-combat target may be declined")
    void mayDeclineTarget() {
        harness.addToBattlefield(player1, new AlacrianArmory());
        Permanent vehicle = addVehicle(player1);

        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(vehicle.isSaddled()).isFalse();
    }

    private Permanent addVehicle(Player player) {
        return harness.addToBattlefieldAndReturn(player, new DuskLegionDreadnought());
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
