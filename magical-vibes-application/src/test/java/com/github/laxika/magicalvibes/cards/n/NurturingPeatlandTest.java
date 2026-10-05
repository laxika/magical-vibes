package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.PendingInteraction;




@CardUsed({NurturingPeatland.class, MotherBear.class})
class NurturingPeatlandTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice and mana costs are paid before the draw resolves")
    void paysCostsBeforeDrawing() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NurturingPeatland());
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Nurturing Peatland");
        harness.assertInGraveyard(player1, "Nurturing Peatland");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Mother Bear");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The draw ability cannot use the same land to pay its mana cost")
    void cannotDrawWithoutMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NurturingPeatland());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(land.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Nurturing Peatland");
        harness.assertNotInGraveyard(player1, "Nurturing Peatland");
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped land cannot activate either ability")
    void tappedLandCannotActivateEitherAbility() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NurturingPeatland());
        land.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertLife(player1, lifeBefore);
        harness.assertOnBattlefield(player1, "Nurturing Peatland");
        harness.assertNotInGraveyard(player1, "Nurturing Peatland");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping Nurturing Peatland for black mana costs 1 life")
    void tapsForBlackMana() {
        Permanent peatland = harness.addToBattlefieldAndReturn(player1, new NurturingPeatland());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(peatland.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping Nurturing Peatland for green mana costs 1 life")
    void tapsForGreenMana() {
        Permanent peatland = harness.addToBattlefieldAndReturn(player1, new NurturingPeatland());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(peatland.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying {1}, tapping, and sacrificing Nurturing Peatland draws a card")
    void sacrificesToDraw() {
        harness.addToBattlefield(player1, new NurturingPeatland());
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertNotOnBattlefield(player1, "Nurturing Peatland");
        harness.assertInGraveyard(player1, "Nurturing Peatland");
    }
}

@CardUsed({NurturingPeatland.class, MotherBear.class})
class Mh1NurturingPeatlandTest extends BaseCardTest {

    @Test
    @DisplayName("{T}, Pay 1 life: Add {B} or {G} offers black and green")
    void tapsForBlackOrGreen() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NurturingPeatland());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("BLACK", "GREEN");

        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{T}, Pay 1 life: Add {G} produces green mana")
    void tapsForGreen() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NurturingPeatland());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{1}, {T}, Sacrifice this land: Draw a card draws and sacrifices Nurturing Peatland")
    void sacrificesToDraw() {
        harness.addToBattlefield(player1, new NurturingPeatland());
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertNotOnBattlefield(player1, "Nurturing Peatland");
        harness.assertInGraveyard(player1, "Nurturing Peatland");
    }
}
