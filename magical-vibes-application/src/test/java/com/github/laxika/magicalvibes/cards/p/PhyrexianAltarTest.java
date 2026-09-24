package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianAltar.class, RagingKavu.class})
class PhyrexianAltarTest extends BaseCardTest {

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
