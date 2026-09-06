package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Roast.class, GrizzlyBears.class, WindDrake.class})
class RoastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a target creature without flying")
    void dealsDamageToNonFlyingCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Roast()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetFlyingCreature() {
        harness.addToBattlefield(player2, new WindDrake());
        harness.setHand(player1, List.of(new Roast()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Wind Drake")))
                .isInstanceOf(IllegalStateException.class);
    }
}
