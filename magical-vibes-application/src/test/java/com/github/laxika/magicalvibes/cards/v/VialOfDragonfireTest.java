package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VialOfDragonfireTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing the Vial deals 2 damage to target creature")
    void dealsDamageToTargetCreature() {
        harness.addToBattlefield(player1, new VialOfDragonfire());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vial of Dragonfire");
        harness.assertInGraveyard(player1, "Vial of Dragonfire");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new VialOfDragonfire());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
