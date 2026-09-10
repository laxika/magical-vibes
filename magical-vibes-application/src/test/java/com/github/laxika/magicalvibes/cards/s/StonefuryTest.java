package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Stonefury.class, GrizzlyBears.class, Mountain.class, Plains.class})
class StonefuryTest extends BaseCardTest {

    @Test
    @DisplayName("Stonefury deals damage equal to lands you control")
    void dealsDamageEqualToControlledLands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castStonefury(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Stonefury counts only lands controlled by its controller")
    void countsOnlyControllersLands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Plains());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castStonefury(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Stonefury counts lands at resolution")
    void countsLandsAtResolution() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Stonefury()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = target.getId();
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Plains"));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Stonefury cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Stonefury()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castStonefury(UUID targetId) {
        harness.setHand(player1, List.of(new Stonefury()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
