package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrochiLeafcallerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {G} adds one mana of the chosen color without tapping")
    void filtersGreenIntoAnyColor() {
        harness.addToBattlefield(player1, new OrochiLeafcaller());
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent leafcaller = findPermanent(player1, "Orochi Leafcaller");

        harness.activateAbility(player1, 0, null, null);

        assertThat(leafcaller.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability cannot be activated without {G} available")
    void requiresGreenMana() {
        harness.addToBattlefield(player1, new OrochiLeafcaller());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
