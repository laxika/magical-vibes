package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.PawpatchRecruit;
import com.github.laxika.magicalvibes.cards.i.IntrepidRabbit;
import com.github.laxika.magicalvibes.cards.p.PondProphet;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({ThreeTreeCity.class, PawpatchRecruit.class, IntrepidRabbit.class, PondProphet.class})
class ThreeTreeCityTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Three Tree City asks for a creature type")
    void choosesCreatureTypeWhenEntering() {
        harness.setHand(player1, List.of(new ThreeTreeCity()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RABBIT");

        assertThat(findPermanent(player1, "Three Tree City").getChosenSubtype())
                .isEqualTo(CardSubtype.RABBIT);
    }

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void addsColorlessMana() {
        Permanent city = addCity(CardSubtype.RABBIT);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(city.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds mana for matching creatures you control")
    void addsManaForChosenTypeCreatures() {
        addCity(CardSubtype.RABBIT);
        harness.addToBattlefield(player1, new PawpatchRecruit());
        harness.addToBattlefield(player1, new IntrepidRabbit());
        harness.addToBattlefield(player1, new PondProphet());
        harness.addToBattlefield(player2, new IntrepidRabbit());
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
        addCity(CardSubtype.RABBIT);
        harness.addToBattlefield(player1, new PondProphet());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Matching tapped creatures count and mana resolves without using the stack")
    void producesEachColorImmediatelyIncludingTappedCreatures(ManaColor color) {
        Permanent city = addCity(CardSubtype.RABBIT);
        Permanent rabbit = harness.addToBattlefieldAndReturn(player1, new PawpatchRecruit());
        rabbit.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(city.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(city.getChosenSubtype()).isEqualTo(CardSubtype.RABBIT);
    }

    @Test
    @DisplayName("The second ability requires two mana before producing mana")
    void cannotActivateWithInsufficientMana() {
        Permanent city = addCity(CardSubtype.RABBIT);
        harness.addToBattlefield(player1, new PawpatchRecruit());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(city.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCity(CardSubtype chosenSubtype) {
        Permanent city = harness.addToBattlefieldAndReturn(player1, new ThreeTreeCity());
        city.setChosenSubtype(chosenSubtype);
        return city;
    }
}
