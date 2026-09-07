package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BatheInDragonfireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature")
    void deals4DamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        castBatheInDragonfire(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Avatar of Might");
    }

    @Test
    @DisplayName("Deals lethal damage to a small creature")
    void dealsLethalDamageToSmallCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BatheInDragonfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new BatheInDragonfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castBatheInDragonfire(Permanent target) {
        harness.setHand(player1, List.of(new BatheInDragonfire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
