package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GuidelightOptimizer;
import com.github.laxika.magicalvibes.cards.l.LoxodonSurveyor;
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

@CardUsed({Hulldrifter.class, LoxodonSurveyor.class, GuidelightOptimizer.class})
class HulldrifterTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws two cards")
    void entersAndDrawsTwoCards() {
        harness.setHand(player1, List.of(new Hulldrifter()));
        harness.setLibrary(player1, List.of(new LoxodonSurveyor(), new LoxodonSurveyor(), new LoxodonSurveyor()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Hulldrifter");
    }

    @Test
    @DisplayName("Crew 3 animates Hulldrifter and taps the crew")
    void crewAnimatesHulldrifterAndTapsCrew() {
        Permanent hulldrifter = addCreatureReady(player1, new Hulldrifter());
        Permanent crew = addCreatureReady(player1, new LoxodonSurveyor());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hulldrifter)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering without being cast draws for the Vehicle's controller")
    void enteringWithoutCastingDrawsForController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Hulldrifter(), new Hulldrifter(), new Hulldrifter()));

        harness.enterBattlefieldAndReturn(player2, new Hulldrifter());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Summoning-sick creatures can crew, and animation waits for resolution")
    void summoningSickCreatureCanCrew() {
        Permanent hulldrifter = harness.addToBattlefieldAndReturn(player1, new Hulldrifter());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new LoxodonSurveyor());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, hulldrifter)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hulldrifter)).isTrue();
        assertThat(hulldrifter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped creatures and opposing creatures cannot pay crew")
    void tappedAndOpposingCreaturesCannotCrew() {
        Permanent hulldrifter = harness.addToBattlefieldAndReturn(player1, new Hulldrifter());
        Permanent tappedCrew = addCreatureReady(player1, new LoxodonSurveyor());
        tappedCrew.tap();
        addCreatureReady(player2, new LoxodonSurveyor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");

        assertThat(gqs.isCreature(gd, hulldrifter)).isFalse();
    }

    @Test
    @DisplayName("An animated Hulldrifter cannot crew itself")
    void animatedVehicleCannotCrewItself() {
        Permanent hulldrifter = addCreatureReady(player1, new Hulldrifter());
        addCreatureReady(player1, new LoxodonSurveyor());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hulldrifter)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(hulldrifter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Crew requires at least three total power")
    void insufficientPowerCannotCrew() {
        Permanent hulldrifter = harness.addToBattlefieldAndReturn(player1, new Hulldrifter());
        Permanent crew = addCreatureReady(player1, new GuidelightOptimizer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");

        assertThat(crew.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, hulldrifter)).isFalse();
    }

    @Test
    @DisplayName("Multiple creatures can combine their power to crew")
    void multipleCreaturesCanCrew() {
        Permanent hulldrifter = harness.addToBattlefieldAndReturn(player1, new Hulldrifter());
        Permanent firstCrew = addCreatureReady(player1, new GuidelightOptimizer());
        Permanent secondCrew = addCreatureReady(player1, new GuidelightOptimizer());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, hulldrifter)).isTrue();
    }

    @Test
    @DisplayName("A crewed Hulldrifter cannot be blocked by a creature without flying or reach")
    void crewedVehicleHasFlyingEvasion() {
        Permanent hulldrifter = addCreatureReady(player1, new Hulldrifter());
        addCreatureReady(player1, new LoxodonSurveyor());
        Permanent blocker = addCreatureReady(player2, new LoxodonSurveyor());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, blocker, hulldrifter,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Crew animation ends at end of turn")
    void crewAnimationEndsAtEndOfTurn() {
        Permanent hulldrifter = addCreatureReady(player1, new Hulldrifter());
        addCreatureReady(player1, new LoxodonSurveyor());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, hulldrifter)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, hulldrifter)).isFalse();
    }
}
