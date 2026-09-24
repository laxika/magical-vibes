package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MomoRambunctiousRascal.class, GrizzlyBears.class, Forest.class})
class MomoRambunctiousRascalTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 4 damage to a tapped creature an opponent controls")
    void etbDamagesTappedOpponentCreature() {
        Permanent bears = addTappedCreature(player2);

        castMomo();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Momo, Rambunctious Rascal");
    }

    @Test
    @DisplayName("An untapped opponent creature is not a legal target")
    void untappedOpponentCreatureIsNotTargetable() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castMomo();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A tapped creature you control is not a legal target")
    void ownTappedCreatureIsNotTargetable() {
        addTappedCreature(player1);

        castMomo();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A noncreature permanent is not a legal target")
    void noncreaturePermanentIsNotTargetable() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.tap();

        harness.setHand(player1, List.of(new MomoRambunctiousRascal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature an opponent controls");
    }

    private Permanent addTappedCreature(Player controller) {
        Permanent creature = harness.addToBattlefieldAndReturn(controller, new GrizzlyBears());
        creature.tap();
        return creature;
    }

    private void castMomo() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MomoRambunctiousRascal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
