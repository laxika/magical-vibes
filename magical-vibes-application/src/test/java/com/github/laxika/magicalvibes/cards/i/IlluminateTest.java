package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Illuminate.class, GrizzlyBears.class, HillGiant.class})
class IlluminateTest extends BaseCardTest {

    @Test
    void dealsXDamageToTargetCreatureWithoutKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Illuminate()));
        addMana(2, 1, 0, 0);

        cast(target.getId(), 2, false, List.of());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void redKickerAlsoDamagesTargetCreaturesController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Illuminate()));
        addMana(4, 2, 0, 0);

        cast(target.getId(), 2, true, List.of());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void blueKickerDrawsXCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Illuminate()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addMana(5, 1, 0, 1);

        cast(target.getId(), 2, false, List.of("{3}{U}"));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void bothKickersResolveTheirIndependentEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Illuminate()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addMana(7, 2, 0, 1);

        cast(target.getId(), 2, true, List.of("{3}{U}"));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void addMana(int colorless, int red, int green, int blue) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.RED, red);
        harness.addMana(player1, ManaColor.GREEN, green);
        harness.addMana(player1, ManaColor.BLUE, blue);
    }

    private void cast(java.util.UUID targetId, int xValue, boolean kicked, List<String> additionalCosts) {
        gs.playCard(gd, player1, 0, xValue, targetId, null, List.of(), List.of(), false,
                null, null, null, null, null, kicked, null, null, null, null,
                additionalCosts, false);
    }
}
