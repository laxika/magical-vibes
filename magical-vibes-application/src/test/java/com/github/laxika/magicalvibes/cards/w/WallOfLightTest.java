package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfLight.class, DoomBlade.class})
class WallOfLightTest extends BaseCardTest {

    @Test
    @DisplayName("Wall of Light has protection from black")
    void hasProtectionFromBlack() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfLight());

        assertThat(gqs.hasProtectionFrom(gd, wall, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, wall, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Wall of Light cannot be targeted by a black spell")
    void cannotBeTargetedByBlackSpell() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfLight());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, wall.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }
}
