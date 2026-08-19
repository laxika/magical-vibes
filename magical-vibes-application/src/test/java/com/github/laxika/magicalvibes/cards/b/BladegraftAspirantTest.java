package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BladegraftAspirantTest extends BaseCardTest {

    @Test
    @DisplayName("Equipment spells cost {1} less to cast")
    void equipmentSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BladegraftAspirant());
        harness.setHand(player1, List.of(new LeoninScimitar()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Equipment abilities targeting Bladegraft Aspirant cost {1} less")
    void equipmentAbilitiesTargetingThisCreatureCostOneLess() {
        Permanent aspirant = harness.addToBattlefieldAndReturn(player1, new BladegraftAspirant());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scimitar), null,
                aspirant.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(aspirant.getId());
    }

    @Test
    @DisplayName("Equipment abilities targeting another creature are not reduced")
    void equipmentAbilitiesTargetingAnotherCreatureAreNotReduced() {
        harness.addToBattlefield(player1, new BladegraftAspirant());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(scimitar), null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
