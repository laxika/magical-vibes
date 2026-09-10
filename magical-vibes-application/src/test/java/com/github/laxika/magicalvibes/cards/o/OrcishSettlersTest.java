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
        Permanent settlers = addCreatureReady(player1, new OrcishSettlers());
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
        Permanent settlers = addCreatureReady(player1, new OrcishSettlers());
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
        Permanent settlers = addCreatureReady(player1, new OrcishSettlers());
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
        Permanent settlers = addCreatureReady(player1, new OrcishSettlers());
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
        Permanent settlers = addCreatureReady(player1, new OrcishSettlers());
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
        Permanent settlers = addCreatureReady(player1, new OrcishSettlers());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(land);
        harness.assertInGraveyard(player1, "Orcish Settlers");
    }
}
