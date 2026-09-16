package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Solitude.class, GrizzlyBears.class, Forest.class})
class SolitudeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles another creature and its controller gains life equal to its power")
    void exilesAnotherCreatureAndItsControllerGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        giveSolitude();

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Can enter without choosing a creature")
    void canEnterWithoutChoosingACreature() {
        giveSolitude();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Solitude");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        giveSolitude();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature");
    }

    @Test
    @DisplayName("Evoke exiles the creature and sacrifices Solitude as it enters")
    void evokeExilesCreatureAndSacrificesSolitude() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Solitude()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithEvoke(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Solitude");
        harness.assertLife(player2, 22);
    }

    private void giveSolitude() {
        harness.setHand(player1, List.of(new Solitude()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
