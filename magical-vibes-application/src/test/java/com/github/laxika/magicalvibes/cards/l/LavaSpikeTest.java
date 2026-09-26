package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.e.ErrantEphemeron;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LavaSpike.class, ElspethKnightErrant.class, ErrantEphemeron.class})
class LavaSpikeTest extends BaseCardTest {

    // "Lava Spike deals 3 damage to target player or planeswalker."

    private void giveLavaSpike() {
        harness.setHand(player1, List.of(new LavaSpike()));
        harness.addMana(player1, ManaColor.RED, 1);
    }

    @Test
    @DisplayName("Deals 3 damage to the targeted player")
    void damageToTargetPlayer() {
        giveLavaSpike();
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Can target its controller")
    void damageToController() {
        giveLavaSpike();
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Deals 3 damage to a targeted planeswalker, removing loyalty")
    void damageToTargetPlaneswalker() {
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 4);
        giveLavaSpike();

        harness.castAndResolveSorcery(player1, 0, elspeth.getId());

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1); // 4 - 3
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new ErrantEphemeron());
        giveLavaSpike();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Errant Ephemeron")))
                .isInstanceOf(IllegalStateException.class);
    }
}
