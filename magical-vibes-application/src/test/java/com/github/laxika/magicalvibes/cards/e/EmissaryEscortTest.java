package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmissaryEscort.class, DarksteelIngot.class})
class EmissaryEscortTest extends BaseCardTest {

    @Test
    @DisplayName("Does not count itself")
    void doesNotCountItself() {
        var emissary = harness.addToBattlefieldAndReturn(player1, new EmissaryEscort());

        assertThat(gqs.getEffectivePower(gd, emissary)).isZero();
    }

    @Test
    @DisplayName("Gets power equal to the greatest other artifact mana value")
    void getsPowerFromGreatestOtherArtifactManaValue() {
        var emissary = harness.addToBattlefieldAndReturn(player1, new EmissaryEscort());
        harness.addToBattlefield(player1, new DarksteelIngot());

        assertThat(gqs.getEffectivePower(gd, emissary)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ignores artifacts controlled by an opponent")
    void ignoresOpponentArtifacts() {
        var emissary = harness.addToBattlefieldAndReturn(player1, new EmissaryEscort());
        harness.addToBattlefield(player2, new DarksteelIngot());

        assertThat(gqs.getEffectivePower(gd, emissary)).isZero();
    }

    @Test
    @DisplayName("Updates when the greatest other artifact leaves")
    void updatesWhenArtifactLeaves() {
        var emissary = harness.addToBattlefieldAndReturn(player1, new EmissaryEscort());
        var ingot = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());

        assertThat(gqs.getEffectivePower(gd, emissary)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(ingot);

        assertThat(gqs.getEffectivePower(gd, emissary)).isZero();
    }
}
