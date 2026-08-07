package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrcishSettlersTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 destroys two target lands and sacrifices Orcish Settlers")
    void destroysXTargetLands() {
        Permanent settlers = harness.addToBattlefieldAndReturn(player1, new OrcishSettlers());
        settlers.setSummoningSick(false);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Island());
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
        Permanent settlers = harness.addToBattlefieldAndReturn(player1, new OrcishSettlers());
        settlers.setSummoningSick(false);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("More targets than the paid X are rejected")
    void rejectsMoreTargetsThanX() {
        Permanent settlers = harness.addToBattlefieldAndReturn(player1, new OrcishSettlers());
        settlers.setSummoningSick(false);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature is an illegal target")
    void rejectsNonLandTarget() {
        Permanent settlers = harness.addToBattlefieldAndReturn(player1, new OrcishSettlers());
        settlers.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
