package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EyeblightAssassin.class, GrizzlyBears.class, Disperse.class})
class EyeblightAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives the targeted opponent creature -1/-1")
    void etbShrinksOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castAssassin(bearsId);
        resolveAllTriggers();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A 1/1 target dies to state-based actions")
    void oneOneTargetDies() {
        GrizzlyBears weakBear = new GrizzlyBears();
        weakBear.setPower(1);
        weakBear.setToughness(1);
        harness.addToBattlefield(player2, weakBear);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castAssassin(bearsId);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castAssassin(bearsId);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new EyeblightAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearsId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAssassin(UUID targetId) {
        harness.setHand(player1, List.of(new EyeblightAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }

    @Test
    @DisplayName("Can enter when the opponent controls no creatures")
    void entersWithoutLegalTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EyeblightAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Eyeblight Assassin");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The creature and its ETB ability resolve separately")
    void creatureEnteringDoesNotImmediatelyShrinkTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAssassin(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eyeblight Assassin");
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);

        resolveAllTriggers();

        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ETB ability resolves after the Assassin leaves")
    void triggerResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAssassin(harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Eyeblight Assassin"));
        harness.assertInHand(player1, "Eyeblight Assassin");
        harness.assertNotOnBattlefield(player1, "Eyeblight Assassin");

        resolveAllTriggers();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ETB ability does not affect another creature if its target leaves")
    void triggerDoesNotRetargetWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAssassin(target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInHand(player2, "Grizzly Bears");

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Eyeblight Assassin");
    }
}
