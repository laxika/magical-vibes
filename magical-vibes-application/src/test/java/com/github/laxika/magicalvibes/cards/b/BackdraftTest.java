package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.Earthquake;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Backdraft.class, Earthquake.class, LavaSpike.class})
class BackdraftTest extends BaseCardTest {

    @Test
    void dealsHalfTheDamageDealtByOneSorceryRoundedDown() {
        harness.setHand(player1, List.of(new Earthquake()));
        harness.setHand(player2, List.of(new Backdraft()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }

    @Test
    void onlyTargetsAPlayerWhoCastASorceryThisTurn() {
        harness.setHand(player1, List.of(new LavaSpike()));
        harness.setHand(player2, List.of(new Backdraft()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cast a sorcery");
    }
}
