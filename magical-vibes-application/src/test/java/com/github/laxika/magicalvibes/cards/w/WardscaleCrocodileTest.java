package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WardscaleCrocodile.class, Shock.class})
class WardscaleCrocodileTest extends BaseCardTest {

    @Test
    @DisplayName("Wardscale Crocodile has hexproof")
    void hasHexproof() {
        Permanent crocodile = addCreatureReady(player1, new WardscaleCrocodile());

        assertThat(gqs.hasKeyword(gd, crocodile, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("An opponent cannot target Wardscale Crocodile with Shock")
    void opponentCannotTargetWithShock() {
        Permanent crocodile = addCreatureReady(player1, new WardscaleCrocodile());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, crocodile.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Its controller can target Wardscale Crocodile with Shock")
    void controllerCanTargetWithShock() {
        Permanent crocodile = addCreatureReady(player1, new WardscaleCrocodile());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, crocodile.getId());

        assertThat(crocodile.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Wardscale Crocodile");
        assertThat(gd.stack).isEmpty();
    }
}
