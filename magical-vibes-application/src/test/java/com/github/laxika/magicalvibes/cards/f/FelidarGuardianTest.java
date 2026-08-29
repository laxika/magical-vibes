package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FelidarGuardianTest extends BaseCardTest {

    private void castGuardian() {
        harness.setHand(player1, List.of(new FelidarGuardian()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("ETB may flicker another permanent you control")
    void flickersChosenPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        castGuardian();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bearsId);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the target unchanged")
    void decliningDoesNotFlicker() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        castGuardian();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(bearsId);
    }

    @Test
    @DisplayName("The ETB ability is not put on the stack without another legal permanent")
    void noOtherPermanentMeansNoTrigger() {
        castGuardian();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Felidar Guardian");
    }

    @Test
    @DisplayName("An opponent's permanent is not a legal target")
    void cannotTargetOpponentsPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castGuardian();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Felidar Guardian");
    }
}
