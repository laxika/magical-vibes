package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fastbond.class, Forest.class})
class FastbondTest extends BaseCardTest {

    @Test
    @DisplayName("Allows multiple land plays and damages its controller after the first")
    void allowsMultipleLandPlaysAndDamagesControllerAfterFirst() {
        harness.setHand(player1, List.of(new Fastbond()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        harness.assertLife(player1, 20);

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(2);
        harness.assertLife(player1, 19);

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(3);
        harness.assertLife(player1, 18);
    }
}
