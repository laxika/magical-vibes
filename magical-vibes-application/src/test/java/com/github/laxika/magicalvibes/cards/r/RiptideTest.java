package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Riptide.class, AirElemental.class, GrizzlyBears.class})
class RiptideTest extends BaseCardTest {

    @Test
    @DisplayName("Taps all blue creatures and leaves nonblue creatures untapped")
    void tapsAllBlueCreatures() {
        Permanent ownBlueCreature = addCreatureReady(player1, new AirElemental());
        Permanent opposingBlueCreature = addCreatureReady(player2, new AirElemental());
        Permanent ownNonblueCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingNonblueCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Riptide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(ownBlueCreature.isTapped()).isTrue();
        assertThat(opposingBlueCreature.isTapped()).isTrue();
        assertThat(ownNonblueCreature.isTapped()).isFalse();
        assertThat(opposingNonblueCreature.isTapped()).isFalse();
    }
}
