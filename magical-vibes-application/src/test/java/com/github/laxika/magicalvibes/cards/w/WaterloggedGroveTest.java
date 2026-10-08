package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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




@CardUsed({WaterloggedGrove.class, GrizzlyBears.class})
class WaterloggedGroveTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Waterlogged Grove for green mana costs 1 life")
    void tapsForGreenMana() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new WaterloggedGrove());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(grove.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping Waterlogged Grove for blue mana costs 1 life")
    void tapsForBlueMana() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new WaterloggedGrove());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(grove.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying {1}, tapping, and sacrificing Waterlogged Grove draws a card")
    void sacrificesToDraw() {
        harness.addToBattlefield(player1, new WaterloggedGrove());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertNotOnBattlefield(player1, "Waterlogged Grove");
        harness.assertInGraveyard(player1, "Waterlogged Grove");
    }

    @Test
    @DisplayName("Sacrifice and mana are paid before the draw ability resolves")
    void paysCostsBeforeDrawing() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new WaterloggedGrove());
        WaterloggedGrove drawnCard = new WaterloggedGrove();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(grove.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Waterlogged Grove");
        harness.assertInGraveyard(player1, "Waterlogged Grove");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Waterlogged Grove cannot activate its mana ability")
    void tappedLandCannotProduceMana() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new WaterloggedGrove());
        grove.setTapped(true);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Waterlogged Grove cannot be sacrificed for its draw ability")
    void tappedLandCannotActivateDrawAbility() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new WaterloggedGrove());
        grove.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Waterlogged Grove");
        harness.assertNotInGraveyard(player1, "Waterlogged Grove");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The draw ability requires one mana without paying life or sacrificing the land on failure")
    void drawAbilityRequiresMana() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new WaterloggedGrove());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(grove.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Waterlogged Grove");
        harness.assertNotInGraveyard(player1, "Waterlogged Grove");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }
}

@CardUsed({WaterloggedGrove.class, GrizzlyBears.class})
class Mh1WaterloggedGroveTest extends BaseCardTest {

    @Test
    @DisplayName("{T}, Pay 1 life: Add {G} or {U} offers green and blue")
    void tapsForGreenOrBlue() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WaterloggedGrove());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("BLUE", "GREEN");

        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{T}, Pay 1 life: Add {G} or {U} produces blue mana")
    void tapsForBlue() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WaterloggedGrove());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{1}, {T}, Sacrifice this land: Draw a card draws and sacrifices Waterlogged Grove")
    void sacrificesToDraw() {
        harness.addToBattlefield(player1, new WaterloggedGrove());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertNotOnBattlefield(player1, "Waterlogged Grove");
        harness.assertInGraveyard(player1, "Waterlogged Grove");
    }
}
