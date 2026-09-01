package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoltenDisaster.class, GrizzlyBears.class, AirElemental.class, Shock.class})
class MoltenDisasterTest extends BaseCardTest {

    @Test
    void dealsXDamageToPlayersAndNonFlyingCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new MoltenDisaster()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(harness.getGameData().getLife(player1.getId())).isEqualTo(18);
        assertThat(harness.getGameData().getLife(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    void kickedSpellHasSplitSecondWhileOnStack() {
        harness.setHand(player1, List.of(new MoltenDisaster()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castKickedSorcery(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        assertThat(harness.getGameData().stack).hasSize(1);
    }
}
