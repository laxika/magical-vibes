package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZephidTest extends BaseCardTest {

    @Test
    @DisplayName("Zephid cannot be targeted by spells because it has shroud")
    void cannotBeTargetedBySpells() {
        harness.addToBattlefield(player1, new Zephid());
        Permanent zephid = findPermanent(player1, "Zephid");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, zephid.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
