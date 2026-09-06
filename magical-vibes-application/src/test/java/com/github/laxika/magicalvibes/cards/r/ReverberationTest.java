package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.Earthquake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reverberation.class, Earthquake.class, GrizzlyBears.class, Shock.class})
class ReverberationTest extends BaseCardTest {

    @Test
    void redirectsAllDamageFromTargetSorceryToItsController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Earthquake earthquake = new Earthquake();
        harness.setHand(player1, List.of(earthquake));
        harness.setHand(player2, List.of(new Reverberation()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 3);
        harness.castInstant(player2, 0, earthquake.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    void onlyTargetsSorcerySpells() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.setHand(player2, List.of(new Reverberation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery spell");
    }
}
