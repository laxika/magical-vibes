package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WretchedDoll;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurnerRocket.class, WretchedDoll.class, BrokenWings.class})
class BurnerRocketTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives a creature you control +2/+0 and trample until end of turn")
    void etbBoostsAndGrantsTrample() {
        Permanent doll = addCreatureReady(player1, new WretchedDoll());
        harness.setHand(player1, List.of(new BurnerRocket()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, doll.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, doll)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, doll)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, doll, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("ETB target must be a creature you control")
    void etbRejectsCreatureControlledByOpponent() {
        Permanent doll = addCreatureReady(player2, new WretchedDoll());
        harness.setHand(player1, List.of(new BurnerRocket()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, doll.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Crew 1 animates Burner Rocket and taps the crew")
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent vehicle = addCreatureReady(player1, new BurnerRocket());
        Permanent crew = addCreatureReady(player1, new WretchedDoll());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Crew animation wears off at end of turn")
    void crewAnimationResetsAtEndOfTurn() {
        Permanent vehicle = addCreatureReady(player1, new BurnerRocket());
        addCreatureReady(player1, new WretchedDoll());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's turn and choosing the ETB target after entry")
    void flashDuringOpponentsTurn() {
        Permanent doll = addCreatureReady(player1, new WretchedDoll());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new BurnerRocket()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Burner Rocket");
        harness.handlePermanentChosen(player1, doll.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, doll)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, doll, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Burner Rocket can enter when there is no creature to target")
    void canBeCastWithoutCreatureToTarget() {
        harness.setHand(player1, List.of(new BurnerRocket()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Burner Rocket");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The ETB power boost and trample both expire at cleanup")
    void etbBenefitsExpireAtEndOfTurn() {
        Permanent doll = addCreatureReady(player1, new WretchedDoll());
        harness.setHand(player1, List.of(new BurnerRocket()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0, doll.getId());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, doll)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, doll, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, doll)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, doll, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Destroying Burner Rocket does not stop its ETB ability")
    void etbResolvesAfterVehicleIsDestroyed() {
        Permanent doll = addCreatureReady(player1, new WretchedDoll());
        harness.setHand(player1, List.of(new BurnerRocket()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0, doll.getId());
        harness.passBothPriorities();
        Permanent vehicle = findPermanent(player1, "Burner Rocket");
        harness.setHand(player2, List.of(new BrokenWings()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, vehicle.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Burner Rocket");
        assertThat(gqs.getEffectivePower(gd, doll)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, doll, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Removing the ETB target leaves the Vehicle on the battlefield")
    void etbDoesNotAffectAnotherCreatureWhenTargetIsRemoved() {
        Permanent doll = addCreatureReady(player1, new WretchedDoll());
        Permanent other = addCreatureReady(player1, new WretchedDoll());
        harness.setHand(player1, List.of(new BurnerRocket()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0, doll.getId());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new BrokenWings()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, doll.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Burner Rocket");
        harness.assertInGraveyard(player1, "Wretched Doll");
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick creature can pay the crew cost")
    void summoningSickCreatureCanCrew() {
        Permanent vehicle = addCreatureReady(player1, new BurnerRocket());
        Permanent crew = addCreatureReady(player1, new WretchedDoll());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
    }

    @Test
    @DisplayName("An already tapped creature cannot pay crew 1")
    void tappedCreatureCannotCrew() {
        Permanent vehicle = addCreatureReady(player1, new BurnerRocket());
        Permanent crew = addCreatureReady(player1, new WretchedDoll());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }
}
