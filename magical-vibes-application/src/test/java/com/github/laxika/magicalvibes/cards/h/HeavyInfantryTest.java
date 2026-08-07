package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeavyInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps target creature an opponent controls")
    void tapsTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

        castHeavyInfantry(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isZero();
        harness.assertOnBattlefield(player1, "Heavy Infantry");
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID ownBearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new HeavyInfantry()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castHeavyInfantry(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new HeavyInfantry()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
