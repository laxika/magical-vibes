package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ChannelTheSuns.class)
class ChannelTheSunsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving adds one mana of each color to its controller's pool")
    void resolvingAddsOneManaOfEachColor() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromHand(player1, new ChannelTheSuns(), "{3}{G}");

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Mana is added only when the sorcery resolves")
    void manaIsAddedOnlyOnResolution() {
        harness.castFromHand(player1, new ChannelTheSuns(), "{3}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        for (ManaColor color : new ManaColor[]{ManaColor.WHITE, ManaColor.BLUE,
                ManaColor.BLACK, ManaColor.RED, ManaColor.GREEN}) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        }
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Channel the Suns");
    }
}
