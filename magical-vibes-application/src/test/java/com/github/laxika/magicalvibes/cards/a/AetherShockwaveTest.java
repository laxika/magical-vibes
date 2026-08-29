package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InnocenceKami;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AetherShockwaveTest extends BaseCardTest {

    @Test
    @DisplayName("First mode taps all Spirits and leaves non-Spirit creatures untapped")
    void tapsAllSpirits() {
        harness.addToBattlefield(player1, new InnocenceKami());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new InnocenceKami());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AetherShockwave()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(permanent(player1, "Innocence Kami").isTapped()).isTrue();
        assertThat(permanent(player2, "Innocence Kami").isTapped()).isTrue();
        assertThat(permanent(player1, "Grizzly Bears").isTapped()).isFalse();
        assertThat(permanent(player2, "Grizzly Bears").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Second mode taps all non-Spirit creatures and leaves Spirits untapped")
    void tapsAllNonSpiritCreatures() {
        harness.addToBattlefield(player1, new InnocenceKami());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new InnocenceKami());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AetherShockwave()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(permanent(player1, "Innocence Kami").isTapped()).isFalse();
        assertThat(permanent(player2, "Innocence Kami").isTapped()).isFalse();
        assertThat(permanent(player1, "Grizzly Bears").isTapped()).isTrue();
        assertThat(permanent(player2, "Grizzly Bears").isTapped()).isTrue();
    }

    private Permanent permanent(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
