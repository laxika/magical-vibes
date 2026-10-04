package com.github.laxika.magicalvibes.cards.f;

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
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;




@CardUsed({FieryIslet.class, MotherBear.class})
class FieryIsletTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Fiery Islet for blue mana costs 1 life")
    void tapsForBlueMana() {
        Permanent islet = harness.addToBattlefieldAndReturn(player1, new FieryIslet());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(islet.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping Fiery Islet for red mana costs 1 life")
    void tapsForRedMana() {
        Permanent islet = harness.addToBattlefieldAndReturn(player1, new FieryIslet());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(islet.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying {1}, tapping, and sacrificing Fiery Islet draws a card")
    void sacrificesToDraw() {
        harness.addToBattlefield(player1, new FieryIslet());
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof FieryIslet);
        harness.assertInGraveyard(player1, "Fiery Islet");
    }
}

@CardUsed({FieryIslet.class, MotherBear.class})
class Mh1FieryIsletTest extends BaseCardTest {

    @Test
    @DisplayName("{T}, Pay 1 life: Add {U} produces blue mana and costs 1 life")
    void tapsForBlue() {
        addReadyIslet(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{T}, Pay 1 life: Add {R} produces red mana and costs 1 life")
    void tapsForRed() {
        addReadyIslet(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{1}, {T}, Sacrifice this land: Draw a card draws and sacrifices Fiery Islet")
    void sacrificesToDraw() {
        addReadyIslet(player1);
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() instanceof FieryIslet);
        harness.assertInGraveyard(player1, "Fiery Islet");
    }

    @Test
    @DisplayName("Fiery Islet is sacrificed and mana is paid before the draw resolves")
    void paysDrawCostsBeforeResolution() {
        Permanent islet = harness.addToBattlefieldAndReturn(player1, new FieryIslet());
        MotherBear cardToDraw = new MotherBear();
        harness.setLibrary(player1, List.of(cardToDraw));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(islet.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Fiery Islet");
        harness.assertInGraveyard(player1, "Fiery Islet");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(cardToDraw);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fiery Islet cannot be sacrificed to draw without paying mana")
    void cannotDrawWithoutMana() {
        Permanent islet = harness.addToBattlefieldAndReturn(player1, new FieryIslet());
        harness.setLibrary(player1, List.of(new MotherBear()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(islet.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Fiery Islet");
        harness.assertNotInGraveyard(player1, "Fiery Islet");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Fiery Islet cannot activate either ability")
    void tappedLandCannotActivateEitherAbility() {
        Permanent islet = harness.addToBattlefieldAndReturn(player1, new FieryIslet());
        islet.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, lifeBefore);
        harness.assertOnBattlefield(player1, "Fiery Islet");
        harness.assertNotInGraveyard(player1, "Fiery Islet");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
    private Permanent addReadyIslet(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new FieryIslet());
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }
}
