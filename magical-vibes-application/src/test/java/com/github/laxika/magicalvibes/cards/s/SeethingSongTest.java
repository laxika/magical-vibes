package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SeethingSong.class)
class SeethingSongTest extends BaseCardTest {

    @Test
    void addsFiveRedManaOnlyWhenSpellResolves() {
        harness.castFromHand(player1, new SeethingSong(), "{2}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
        harness.assertInGraveyard(player1, "Seething Song");
    }

    @Test
    void preservesManaAlreadyInPool() {
        harness.castFromHand(player1, new SeethingSong(), "{2}{R}");
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(7);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void nonactivePlayerReceivesManaDuringOpponentsTurn() {
        harness.castFromHand(player2, new SeethingSong(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertInGraveyard(player2, "Seething Song");
    }
}
