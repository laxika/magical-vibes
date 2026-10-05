package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianAltar.class, RagingKavu.class})
class PhyrexianAltarTest extends BaseCardTest {

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The sacrificed creature's colors do not restrict the chosen mana color")
    void canProduceEveryColor(ManaColor color) {
        harness.addToBattlefield(player1, new PhyrexianAltar());
        harness.addToBattlefield(player1, new RagingKavu());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Raging Kavu");
        harness.assertNotOnBattlefield(player1, "Raging Kavu");
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Phyrexian Altar");
    }

    @Test
    @DisplayName("A tapped Altar can activate repeatedly by sacrificing tapped creatures")
    void tappedAltarCanActivateRepeatedly() {
        var altar = harness.addToBattlefieldAndReturn(player1, new PhyrexianAltar());
        altar.setTapped(true);
        var firstCreature = harness.addToBattlefieldAndReturn(player1, new RagingKavu());
        firstCreature.setTapped(true);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        var secondCreature = harness.addToBattlefieldAndReturn(player1, new RagingKavu());
        secondCreature.setTapped(true);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Raging Kavu");
        harness.assertOnBattlefield(player1, "Phyrexian Altar");
        assertThat(altar.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a creature adds one mana of the chosen color")
    void sacrificeAddsManaOfChosenColor() {
        harness.addToBattlefield(player1, new PhyrexianAltar());
        harness.addToBattlefield(player1, new RagingKavu());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Raging Kavu");
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        harness.addToBattlefield(player1, new PhyrexianAltar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate using a creature controlled by an opponent")
    void cannotActivateUsingOpponentsCreature() {
        harness.addToBattlefield(player1, new PhyrexianAltar());
        harness.addToBattlefield(player2, new RagingKavu());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Raging Kavu");
    }
}
