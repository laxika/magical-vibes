package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SceptreOfEternalGlory.class, Forest.class, Island.class})
class SceptreOfEternalGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for one mana of any color")
    void tapsForOneManaOfAnyColor() {
        Permanent sceptre = harness.addToBattlefieldAndReturn(player1, new SceptreOfEternalGlory());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(sceptre.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps for three mana with three lands that share a name")
    void tapsForThreeManaWithThreeLandsOfTheSameName() {
        Permanent sceptre = harness.addToBattlefieldAndReturn(player1, new SceptreOfEternalGlory());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(sceptre.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot tap for three mana without three lands that share a name")
    void requiresThreeLandsOfTheSameName() {
        Permanent sceptre = harness.addToBattlefieldAndReturn(player1, new SceptreOfEternalGlory());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three or more lands with the same name");
        assertThat(sceptre.isTapped()).isFalse();
    }

    @Test
    @DisplayName("More than three matching lands still produce exactly three mana of one color")
    void moreThanThreeMatchingLandsProduceThreeMana() {
        Permanent sceptre = harness.addToBattlefieldAndReturn(player1, new SceptreOfEternalGlory());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.addToBattlefield(player1, new Island());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(sceptre.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's matching lands do not satisfy the activation restriction")
    void opponentsLandsDoNotCount() {
        Permanent sceptre = harness.addToBattlefieldAndReturn(player1, new SceptreOfEternalGlory());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three or more lands with the same name");
        assertThat(sceptre.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(sceptre.isTapped()).isTrue();
    }
}
