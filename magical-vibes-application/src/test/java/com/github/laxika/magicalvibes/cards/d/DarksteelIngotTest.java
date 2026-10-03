package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.o.Oxidize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarksteelIngot.class, Oxidize.class})
class DarksteelIngotTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds one mana of the chosen color and taps Darksteel Ingot")
    void manaAbilityAddsChosenColor() {
        Permanent ingot = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(ingot.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana ability can produce each of the five colors")
    void manaAbilityCanProduceEachColor() {
        Permanent ingot = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());

        for (ManaColor color : ManaColor.COLORS) {
            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color.name());

            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
            ingot.untap();
        }
    }

    @Test
    @DisplayName("A newly cast Darksteel Ingot can immediately produce mana")
    void newlyCastIngotCanProduceMana() {
        harness.castFromHand(player1, new DarksteelIngot(), "{3}");
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Darksteel Ingot cannot produce mana again")
    void tappedIngotCannotProduceManaAgain() {
        Permanent ingot = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(ingot.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Indestructible — Oxidize does not destroy Darksteel Ingot")
    void survivesDestroy() {
        Permanent ingot = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());

        harness.setHand(player1, List.of(new Oxidize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, ingot.getId());

        harness.assertOnBattlefield(player1, "Darksteel Ingot");
    }
}
