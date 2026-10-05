package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.w.WindingCanyons;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrcishSettlers.class, WindingCanyons.class, BenalishInfantry.class})
class OrcishSettlersTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 destroys two target lands and sacrifices Orcish Settlers")
    void destroysXTargetLands() {
        addCreatureReady(player1, new OrcishSettlers());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Orcish Settlers");
    }

    @Test
    @DisplayName("The double {X} costs twice the chosen X in generic mana")
    void chargesDoubleX() {
        addCreatureReady(player1, new OrcishSettlers());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("More targets than the paid X are rejected")
    void rejectsMoreTargetsThanX() {
        addCreatureReady(player1, new OrcishSettlers());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature is an illegal target")
    void rejectsNonLandTarget() {
        addCreatureReady(player1, new OrcishSettlers());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BenalishInfantry());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X requires exactly X target lands")
    void requiresExactlyXTargetLands() {
        addCreatureReady(player1, new OrcishSettlers());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=0 allows no targets and still sacrifices Orcish Settlers")
    void allowsZeroTargetsWhenXIsZero() {
        addCreatureReady(player1, new OrcishSettlers());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(land);
        harness.assertInGraveyard(player1, "Orcish Settlers");
    }

    @Test
    void sacrificesAsACostAndDestroysLandsOfBothPlayers() {
        addCreatureReady(player1, new OrcishSettlers());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new WindingCanyons());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2,
                List.of(ownLand.getId(), opposingLand.getId()));

        harness.assertInGraveyard(player1, "Orcish Settlers");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ownLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposingLand);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Winding Canyons");
        harness.assertInGraveyard(player2, "Winding Canyons");
    }

    @Test
    void rejectsTheSameLandTwice() {
        addCreatureReady(player1, new OrcishSettlers());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(land.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Orcish Settlers");
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent settlers = addCreatureReady(player1, new OrcishSettlers());
        settlers.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Orcish Settlers");
    }

    @Test
    void stillDestroysRemainingLandWhenOneTargetLeavesBeforeResolution() {
        addCreatureReady(player1, new OrcishSettlers());
        addCreatureReady(player2, new OrcishSettlers());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2, List.of(first.getId(), second.getId()));
        harness.activateAbilityWithMultiTargets(player2, 0, 0, 1, List.of(first.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(first.getCard(), second.getCard());
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent settlers = addCreatureReady(player1, new OrcishSettlers());
        settlers.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Orcish Settlers");
    }
}
