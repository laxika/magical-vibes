package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RustElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices another artifact during its controller's upkeep")
    void sacrificesAnotherArtifact() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new RustElemental());
        harness.addToBattlefield(player1, new Ornithopter());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental);
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(elemental.isTapped()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Taps and makes its controller lose 4 life when no other artifact can be sacrificed")
    void tapsAndLosesLifeWithoutAnotherArtifact() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new RustElemental());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental);
        assertThat(elemental.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 4);
    }
}
