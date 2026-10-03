package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DawnhartRejuvenator.class)
class DawnhartRejuvenatorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger causes controller to gain 3 life")
    void etbGainsLife() {
        harness.setHand(player1, List.of(new DawnhartRejuvenator()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Tapping adds one mana of the chosen color")
    void tapAddsManaOfAnyColor() {
        Permanent rejuvenator = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        rejuvenator.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(rejuvenator.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void producesExactlyOneManaWithoutUsingTheStack(ManaColor color) {
        Permanent rejuvenator = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        rejuvenator.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(rejuvenator.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        for (ManaColor poolColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(poolColor))
                    .isEqualTo(poolColor == color ? 1 : 0);
            assertThat(gd.playerManaPools.get(player2.getId()).get(poolColor)).isZero();
        }
    }

    @Test
    void summoningSicknessPreventsManaActivation() {
        Permanent rejuvenator = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        rejuvenator.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(rejuvenator.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }

    @Test
    void tappedCreatureCannotActivateManaAbility() {
        Permanent rejuvenator = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        rejuvenator.setSummoningSick(false);
        rejuvenator.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }

    @Test
    void entryTriggerGainsLifeForItsControllerEvenAfterSourceLeaves() {
        harness.enterBattlefieldAndReturn(player2, new DawnhartRejuvenator());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
        assertThat(gd.stack).isEmpty();
    }
}
