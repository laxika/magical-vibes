package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.EccentricFarmer;
import com.github.laxika.magicalvibes.cards.h.HarvesttideSentry;
import com.github.laxika.magicalvibes.cards.u.UnrulyMob;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({MightOfTheOldWays.class, Forest.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class,
        EccentricFarmer.class, HarvesttideSentry.class, UnrulyMob.class})
class MightOfTheOldWaysTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+2 until end of turn without coven")
    void boostsTargetWithoutCoven() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MightOfTheOldWays()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Coven draws a card after boosting the target creature")
    void drawsWithCoven() {
        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MightOfTheOldWays()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MightOfTheOldWays()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new MightOfTheOldWays()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID forestId = forest.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(forestId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostCanEstablishCoven() {
        harness.addToBattlefield(player1, new UnrulyMob());
        harness.addToBattlefield(player1, new EccentricFarmer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EccentricFarmer());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MightOfTheOldWays()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void boostCanRemoveCoven() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new UnrulyMob());
        harness.addToBattlefield(player1, new EccentricFarmer());
        harness.addToBattlefield(player1, new HarvesttideSentry());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MightOfTheOldWays()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void canBoostOpponentsCreatureAndDrawForCaster() {
        harness.addToBattlefield(player1, new UnrulyMob());
        harness.addToBattlefield(player1, new EccentricFarmer());
        harness.addToBattlefield(player1, new HarvesttideSentry());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EccentricFarmer());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MightOfTheOldWays()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void threeCreaturesWithOnlyTwoDistinctPowersDoNotEnableCoven() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EccentricFarmer());
        harness.addToBattlefield(player1, new EccentricFarmer());
        harness.addToBattlefield(player1, new EccentricFarmer());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MightOfTheOldWays()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentsCovenDoesNotEnableDraw() {
        harness.addToBattlefield(player2, new UnrulyMob());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EccentricFarmer());
        harness.addToBattlefield(player2, new HarvesttideSentry());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MightOfTheOldWays()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDrawWhenOnlyTargetLeavesBattlefield() {
        harness.addToBattlefield(player1, new UnrulyMob());
        harness.addToBattlefield(player1, new EccentricFarmer());
        harness.addToBattlefield(player1, new HarvesttideSentry());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EccentricFarmer());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MightOfTheOldWays()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Might of the Old Ways");
    }
}
