package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HallOfTagsinTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana without using the stack")
    void tapForColorless() {
        harness.addToBattlefield(player1, new HallOfTagsin());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The filter ability spends {1} and adds one mana of the chosen color")
    void filterAddsChosenColor() {
        harness.addToBattlefield(player1, new HallOfTagsin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The filter ability cannot be activated without {1}")
    void filterRequiresManaCost() {
        harness.addToBattlefield(player1, new HallOfTagsin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The third ability creates a tapped Powerstone token")
    void createsTappedPowerstone() {
        harness.addToBattlefield(player1, new HallOfTagsin());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent powerstone = findPermanent(player1, "Powerstone");
        assertThat(powerstone.isTapped()).isTrue();
        assertThat(powerstone.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(powerstone.getCard().getSubtypes()).contains(CardSubtype.POWERSTONE);
    }
}
