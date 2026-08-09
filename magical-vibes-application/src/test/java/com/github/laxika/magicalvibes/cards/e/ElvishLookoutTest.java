package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElvishLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Elvish Lookout cannot be targeted by an opponent's spell")
    void opponentSpellCannotTargetIt() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new ElvishLookout());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> gs.playCard(gd,
                player2,
                0,
                0,
                harness.getPermanentId(player1, "Elvish Lookout"),
                null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Elvish Lookout cannot be targeted by its controller's spell")
    void ownSpellCannotTargetIt() {
        harness.addToBattlefield(player1, new ElvishLookout());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> gs.playCard(gd,
                player1,
                0,
                0,
                harness.getPermanentId(player1, "Elvish Lookout"),
                null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
