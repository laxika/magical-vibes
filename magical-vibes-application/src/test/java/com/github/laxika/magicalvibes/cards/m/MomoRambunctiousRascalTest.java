package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AardvarkSloth;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MomoRambunctiousRascal.class, AardvarkSloth.class, SolRing.class})
class MomoRambunctiousRascalTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 4 damage to a tapped creature an opponent controls")
    void etbDamagesTappedOpponentCreature() {
        Permanent bears = addTappedCreature(player2);

        castMomo();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aardvark Sloth");
        harness.assertOnBattlefield(player1, "Momo, Rambunctious Rascal");
    }

    @Test
    @DisplayName("An untapped opponent creature is not a legal target")
    void untappedOpponentCreatureIsNotTargetable() {
        harness.addToBattlefield(player2, new AardvarkSloth());

        castMomo();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Aardvark Sloth");
    }

    @Test
    @DisplayName("A tapped creature you control is not a legal target")
    void ownTappedCreatureIsNotTargetable() {
        addTappedCreature(player1);

        castMomo();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Aardvark Sloth");
    }

    @Test
    @DisplayName("A noncreature permanent is not a legal target")
    void noncreaturePermanentIsNotTargetable() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());
        artifact.tap();

        castMomo();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Sol Ring");
        harness.assertOnBattlefield(player1, "Momo, Rambunctious Rascal");
    }

    @Test
    @DisplayName("ETB deals exactly 4 damage to a creature that survives")
    void dealsExactlyFourDamage() {
        Permanent creature = addTappedCreature(player2);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castMomo();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Aardvark Sloth");
        assertThat(creature.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("The trigger does not damage a target that untaps before resolution")
    void untappedTargetIsIllegalAtResolution() {
        Permanent creature = addTappedCreature(player2);

        castMomo();
        harness.handlePermanentChosen(player1, creature.getId());
        creature.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Aardvark Sloth");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger still deals damage after Momo leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent creature = addTappedCreature(player2);

        castMomo();
        harness.handlePermanentChosen(player1, creature.getId());
        Permanent momo = findPermanent(player1, "Momo, Rambunctious Rascal");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, momo));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Momo, Rambunctious Rascal");
        harness.assertNotOnBattlefield(player2, "Aardvark Sloth");
        harness.assertInGraveyard(player2, "Aardvark Sloth");
    }

    private Permanent addTappedCreature(Player controller) {
        Permanent creature = harness.addToBattlefieldAndReturn(controller, new AardvarkSloth());
        creature.tap();
        return creature;
    }

    private void castMomo() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MomoRambunctiousRascal(), "{2}{W}");
        harness.passBothPriorities();
    }
}
