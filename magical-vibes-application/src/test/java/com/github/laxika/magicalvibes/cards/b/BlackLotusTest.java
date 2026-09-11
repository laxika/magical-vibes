package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BlackLotus.class)
class BlackLotusTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and sacrificing Black Lotus adds three mana of the chosen color")
    void sacrificeAddsThreeManaOfChosenColor() {
        harness.addToBattlefield(player1, new BlackLotus());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Black Lotus");
        harness.assertInGraveyard(player1, "Black Lotus");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
