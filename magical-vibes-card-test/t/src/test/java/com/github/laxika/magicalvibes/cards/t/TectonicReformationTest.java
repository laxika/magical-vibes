package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;




@CardUsed({TectonicReformation.class, Forest.class, GrizzlyBears.class})
class TectonicReformationTest extends BaseCardTest {

    @Test
    @DisplayName("Grants cycling {R} to land cards in hand")
    void grantsCyclingToLandCardsInHand() {
        harness.addToBattlefield(player1, new TectonicReformation());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not grant cycling to nonland cards in hand")
    void doesNotGrantCyclingToNonlandCardsInHand() {
        harness.addToBattlefield(player1, new TectonicReformation());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card has no hand-activated ability");
    }

    @Test
    @DisplayName("Cycles Tectonic Reformation for two generic mana without entering the battlefield")
    void cyclesItselfForTwoGenericMana() {
        harness.setHand(player1, List.of(new TectonicReformation()));
        harness.setLibrary(player1, List.of(new TectonicReformation()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Tectonic Reformation");
        harness.assertNotInHand(player1, "Tectonic Reformation");
        harness.assertNotOnBattlefield(player1, "Tectonic Reformation");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Tectonic Reformation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Its static ability does not grant cycling while it is in hand")
    void doesNotGrantCyclingFromHand() {
        harness.setHand(player1, List.of(new Forest(), new TectonicReformation()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card has no hand-activated ability");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }


    @Test
    @DisplayName("Granted cycling requires red mana and does not discard on failed payment")
    void grantedCyclingRequiresRedMana() {
        harness.addToBattlefield(player1, new TectonicReformation());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

}

@CardUsed({TectonicReformation.class, Forest.class, GrizzlyBears.class})
class Mh1TectonicReformationTest extends BaseCardTest {

    @Test
    @DisplayName("Gives lands in its controller's hand cycling {R}")
    void givesLandsInControllersHandCycling() {
        harness.addToBattlefield(player1, new TectonicReformation());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not grant cycling to nonlands or to an opponent's hand")
    void onlyGrantsToControllerLands() {
        harness.addToBattlefield(player1, new TectonicReformation());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Card has no hand-activated ability");
        assertThatThrownBy(() -> harness.activateHandAbility(player2, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Card has no hand-activated ability");
    }
}
