package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RootwaterAlligatorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Forest grants Rootwater Alligator a regeneration shield")
    void sacrificingForestRegeneratesRootwaterAlligator() {
        harness.addToBattlefield(player1, new RootwaterAlligator());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        Permanent alligator = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(alligator.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without a Forest")
    void requiresForest() {
        harness.addToBattlefield(player1, new RootwaterAlligator());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot sacrifice a non-Forest land")
    void requiresForestSubtype() {
        harness.addToBattlefield(player1, new RootwaterAlligator());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
