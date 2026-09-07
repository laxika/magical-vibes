package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreeTreeCity.class, BearCub.class, GrizzlyBears.class, LlanowarElves.class})
class ThreeTreeCityTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Three Tree City asks for a creature type")
    void choosesCreatureTypeWhenEntering() {
        harness.setHand(player1, List.of(new ThreeTreeCity()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BEAR");

        assertThat(findPermanent(player1, "Three Tree City").getChosenSubtype())
                .isEqualTo(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void addsColorlessMana() {
        Permanent city = addCity(CardSubtype.BEAR);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(city.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds mana for matching creatures you control")
    void addsManaForChosenTypeCreatures() {
        addCity(CardSubtype.BEAR);
        harness.addToBattlefield(player1, new BearCub());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The second ability produces no mana when no matching creature is controlled")
    void producesNoManaWithoutMatchingCreatures() {
        addCity(CardSubtype.BEAR);
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    private Permanent addCity(CardSubtype chosenSubtype) {
        Permanent city = harness.addToBattlefieldAndReturn(player1, new ThreeTreeCity());
        city.setChosenSubtype(chosenSubtype);
        return city;
    }
}
